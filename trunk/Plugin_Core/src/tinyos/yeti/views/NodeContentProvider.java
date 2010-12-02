/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.Debug;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.parser.ASTNodeFilterFactory;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelAttribute;
import tinyos.yeti.ep.parser.IASTModelElement;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.jobs.FerryJob;
import tinyos.yeti.jobs.ResolveConnectionJob;
import tinyos.yeti.model.ProjectModel;

/**
 * A {@link ITreeContentProvider} that uses an {@link IASTModel} as base and
 * shows the contents of {@link IASTModelNode}s.<br>
 * Note: a {@link NodeContentProvider} should be used only on one
 * {@link TreeViewer}.
 * @author Benjamin Sigg
 */
public class NodeContentProvider implements ITreeContentProvider {
	public static final String PROPERTY_LABEL = "label";
	public static final String PROPERTY_ICON = "icon";
	public static final String PROPERTY_ORDER = "order";
	
    private IASTModelNodeFilter rootFilter;

    private Element[] roots;
    private INesCParserFactory factory;
    private TreeViewer viewer;

    /** whether to use the backup model only to resolve paths */
    private boolean backupForPathResolvingOnly = false;

    private boolean expandBaseTree = false;
    
    private SetContentJob setContent = new SetContentJob();

    //private RefreshJob refresh = null;

    private List<ModelInfo> models = new ArrayList<ModelInfo>();
    
    private Map<Integer, ProjectModel> backups = new HashMap<Integer, ProjectModel>();
    
    private boolean disposed = false;
    
    private AddJob addJob = new AddJob();
    
    /**
     * Creates a content provider that shows all nodes whose tagset is
     * a superset of <code>tags</code> as roots.
     * @param tags the subset of tags
     */
    public NodeContentProvider( TagSet tags ){
        this( ASTNodeFilterFactory.subset( tags ));
    }

    /**
     * Creates a content that shows all nodes which pass <code>rootFilter</code>
     * as roots.
     * @param rootFilter the filter for roots
     */
    public NodeContentProvider( IASTModelNodeFilter rootFilter ){
        if( rootFilter == null )
            throw new IllegalArgumentException( "rootFilter must not be null" );
        this.rootFilter = rootFilter;
        this.factory = TinyOSPlugin.getDefault().getParserFactory();
    }

    /**
     * Gets the filter which is used to find the roots of the tree.
     * @return the roots
     */
    public IASTModelNodeFilter getRootFilter() {
		return rootFilter;
	}
    
    /**
     * Tells whether <code>element</code> is currently expanded in the view. This
     * method is intended to be called from a non-ui thread.
     * @param element the element to check
     * @return <code>true</code> if the element is expanded
     */
    public boolean isExpanded( final Element element ){
    	try{
    		if( viewer == null )
    			return false;
    		
    		class Query implements Runnable{
    			boolean answer;
    			
    			public void run() {
    				if( viewer != null && !viewer.getTree().isDisposed() ){
    					answer = viewer.getExpandedState( element );
    				}
    			}
    		}
    		
    		Query query = new Query();
    		viewer.getTree().getDisplay().syncExec( query );
    		return query.answer;
    	}
    	catch( SWTException ex ){
    		return false;
    	}
    }
    
    /**
     * Tells whether the base tree should automatically be expanded.
     * @return <code>true</code> if the tree should be expanded
     */
    public boolean isExpandBaseTree() {
		return expandBaseTree;
	}
    
    /**
     * Sets the resolved values of <code>element</code>. This method starts
     * its own thread to run in the ui-thread, <code>callback</code> will
     * be called once this method has completed its job.
     * @param element the element to update
     * @param resolved the new resolved state of the element
     * @param node the new node of the element, may be <code>null</code>
     * @param connection the new connection of the element, may be <code>null</code>
     * @param children the new children of the element
     * @param callback to be called once the element is updated, can be <code>null</code>
     */
    public void setContent( Element element, boolean resolved, IASTModelNode node, IASTModelNodeConnection connection, Element[] children, ResolveCallback callback ){
    	setContent.dispatch( new Content( element, resolved, node, connection, children, callback ));
    }
    
    private void updateViewer( Element parent, Element[] oldChildren, Element[] newChildren, boolean labelChanged, boolean iconChanged ){
    	try{
    		if( viewer == null || viewer.getControl().isDisposed() )
    			return;

    		boolean childrenChanged = false;
    		
    		if( oldChildren != null && newChildren != null ){
    			// find those children that have been removed
    			List<Element> removed = new ArrayList<Element>();
    			for( Element check : oldChildren ){
    				boolean found = false;
    				for( Element child : newChildren ){
    					if( child == check ){
    						found = true;
    						break;
    					}
    				}
    				if( !found ){
    					removed.add( check );
    					check.setViewer( null );
    				}
    			}

    			if( removed.size() > 0 ){
    				childrenChanged = true;
					TreePath[] paths = new TreePath[ removed.size() ];
					for( int i = 0; i < paths.length; i++ ){
						paths[i] = removed.get( i ).getTreePath();
					}
					
					viewer.remove( paths );
    			}

    			// add new children at correct position
    			List<Element> added = new ArrayList<Element>();
    			for( int i = 0; i < newChildren.length; i++ ){
    				Element check = newChildren[ i ];
    				boolean found = false;

    				for( Element child : oldChildren ){
    					if( child == check ){
    						found = true;
    						break;
    					}
    				}

    				if( !found ){
    					added.add( check );
    					check.setViewer( viewer );
    				}
    			}
    			
    			if( added.size() > 0 ){
    				childrenChanged = true;
    				if( parent == null ){
    				// viewer.add( new TreePath( new Object[]{} ), added.toArray() );

    				// TODO this call depends on some internal behavior of TreeViewer,
    				// "input" is not really the root.
    					viewer.add( viewer.getInput(), added.toArray() );
    				}
    				else{
    					viewer.add( parent.getTreePath(), added.toArray() );
    				}
    			}
    		}
    		else{
    			if( oldChildren != null ){
    				childrenChanged = true;
					TreePath[] paths = new TreePath[ oldChildren.length ];
					for( int i = 0; i < paths.length; i++ ){
						paths[i] = oldChildren[i].getTreePath();
					}
					
					viewer.remove( paths );
    			}

    			if( newChildren != null ){
    				childrenChanged = true;
    				if( parent == null ){
    					viewer.add( new TreePath( new Object[]{}), newChildren );
    				}
    				else{
    					viewer.add( parent.getTreePath(), newChildren );
    				}
    			}
    		}

    		List<String> properties = new ArrayList<String>();
    		if( iconChanged )
    			properties.add( PROPERTY_ICON );
    		
    		if( labelChanged )
    			properties.add( PROPERTY_LABEL );
    		
    		if( !childrenChanged )
    			properties.add( PROPERTY_ORDER );
    		
    		String[] propertiesArray = properties.toArray( new String[ properties.size() ] );
    		
    		if( parent != null ){
    			viewer.update( getPathElement( parent ), propertiesArray );
    		}
    		else{
    			viewer.update( viewer.getInput(), propertiesArray );
    		}
    	}
    	catch( SWTException ex ){
    		// Sometime something is disposed...
    		Debug.error( ex );
    	}
    }
    
    /**
     * Sets whether the base tree should be automatically expanded or not.
     * @param expand <code>true</code> if expansion should be done automatically
     */
    public void setExpandBaseTree( boolean expand ){
        expandBaseTree = expand;
    }

    /**
     * Sets a backup model that will be used to search nodes that can't
     * be found in the ordinary source of this provider.
     * @param model the index of the model for which the backup will be used
     * @param backup the backup, can be <code>null</code>
     */
    public void setBackup( int model, ProjectModel backup ) {
    	if( backup == null ){
    		backups.remove( models );
    	}
    	else{
    		backups.put( model, backup );
    	}
    	
    	if( model < models.size() ){
    		models.get( model ).backup = backup;
    	}
    }

    public ProjectModel getBackup( int model ) {
        return backups.get( model );
    }
    
    public ProjectModel getBackup( IASTModel model ){
    	List<ModelInfo> models = this.models;
    	if( models == null )
    		return null;
    	for( ModelInfo info : models ){
	    	if( info.model == model ){
	    		return info.backup;
	    	}
    	}
    	return null;
    }

    public void setBackupForPathResolvingOnly( boolean backupForPathResolvingOnly ) {
        this.backupForPathResolvingOnly = backupForPathResolvingOnly;
    }

    public boolean isBackupForPathResolvingOnly() {
        return backupForPathResolvingOnly;
    }

    public Object[] getChildren( Object parentElement ){
        return ((Element)parentElement).getChildren();
    }

    public Object getParent( Object element ){
        return ((Element)element).getParent();
    }

    public boolean hasChildren( Object element ){
        return ((Element)element).hasChildren();
    }

    public Object[] getElements( Object inputElement ){
    	return getElements();
    }
    
    public Object[] getElements(){
        if( roots != null )
            return roots;

        List<Element> result = new ArrayList<Element>();
        for( ModelInfo info : models ){
        	IASTModelNode[] nodes = info.model.getNodes( rootFilter );
        	for( int i = 0, n = nodes.length; i<n; i++ ){
                IASTModelNode node = nodes[i];
                result.add( new Element( null, node, viewer, info ) );
            }
        }
        
        roots = result.toArray( new Element[ result.size() ] );
        
        return roots;
    }
    
    public Element getElement( IASTModelElement element ){
    	if( roots == null )
    		return null;
    	
    	for( Element root : roots ){
    		Element result = root.getElement( element );
    		if( result != null )
    			return result;
    	}
    	return null;
    }
    
    public void dispose(){
    	disposed = true;
        roots = null;
        viewer = null;
        models.clear();
    }
    
    public boolean isDisposed(){
		return disposed;
	}
    
    /**
     * Can be called by clients to inform this {@link NodeContentProvider} that
     * <code>element</code> has been removed from the underlying {@link IASTModel}s.
     * This content provider will inform its {@link Viewer} about the removal
     * of the node.<br>
     * This method is intended to be called from the UI thread.
     * @param element the node that was removed
     */
    public void removed( IASTModelElement element ){
    	Element node;
    	
    	while( (node = getElement( element )) != null ){
	    	Element parent = node.getParent();
	    	if( parent == null ){
	    		if( roots != null ){
	    			for( int i = 0; i < roots.length; i++ ){
	    				if( roots[i] == node ){
	    					TreePath path = node.getTreePath();
	    					roots = remove( roots, i );
	    					fireRemoved( viewer, path, node );
	    				}
	    			}
	    		}
	    	}
	    	else{
	    		parent.removeChild( node );
	    	}
    	}
    }
    
    /**
     * Can be called by clients to inform this {@link NodeContentProvider} that
     * <code>element</code> has been added to the underlying {@link IASTModel}s.
     * This content provider will inform its {@link Viewer} about the
     * new element.<br>
     * This method is thread safe.
     * @param model the model where <code>element</code> was inserted
     * @param element the new element
     */
    public void addRoot( IASTModel model, IASTModelElement element ){
    	addJob.dispatch( model, element );
    }
    
    /**
     * Informs the <code>viewer</code> that <code>element</code> has been
     * added, <code>element</code> is a root in this content provider.
     * @param viewer the viewer
     * @param element the new root
     */
    protected void addRoot( TreeViewer viewer, Element element ){
    	Object[] path = getRootPath( element );
    	Object self = getPathElement( element );
    	
    	if( path.length == 0 ){
    		viewer.add( viewer.getInput(), self );
    	}
    	else{
    		viewer.add( new TreePath( path ), self );
    	}
    }
    
    /**
     * Called when a root element was removed.
     * @param viewer the viewer to inform
     * @param path to the removed element
     * @param removed the element that was removed
     */
    protected void fireRemoved( TreeViewer viewer, TreePath path, Element removed ){
    	viewer.remove( path );
    }
    
    /**
     * Called if an element was removed from its parent.
     * @param viewer the viewer to inform
     * @param parent the parent element
     * @param path path to the removed element
     * @param removed the element that was removed
     */
    protected void fireRemoved( TreeViewer viewer, Element parent, TreePath path, Element removed ){
    	viewer.remove( path );
    }
    
    protected Element[] remove( Element[] children, int index ){
    	Element[] copy = new Element[ children.length-1 ];
    	if( index > 0 )
    		System.arraycopy( children, 0, copy, 0, index );
    	if( index+1 < children.length )
    		System.arraycopy( children, index+1, copy, index, children.length-index-1 );
    	return copy;
    }

    /**
     * Gets the path that leads to the parent of <code>root</code>.
     * @param root some root element
     * @return the path to the parent of <code>root</code>
     */
    protected Object[] getRootPath( Element root ){
    	return new Object[]{};
    }
    
    /**
     * If the elements of this provider are wrapped into other elements, then
     * this method can be used by subclasses to convert elements to 
     * wrapper.
     * @param element some element
     * @return the wrapper or <code>element</code>
     */
    protected Object getPathElement( Element element ){
    	return element;
    }
    
    protected Image image( ImageDescriptor descriptor, IASTModelAttribute[] attributes ){
        if( descriptor == null )
            return null;

        return NesCIcons.icons().get( descriptor, attributes );
    }

    public void update(Observable arg0, Object arg1) {
        viewer.getControl().getDisplay().asyncExec(new Runnable(){
            public void run() {
                roots = null;
                viewer.refresh();
            }
        });
    }

    public TreeViewer getViewer(){
		return viewer;
	}
    
    public void setViewer( TreeViewer viewer ){
		this.viewer = viewer;
	}
    
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ){
        this.viewer = (TreeViewer)viewer;

        if( newInput instanceof IASTModel ){
            setModel( (IASTModel)newInput );
        }
        else if( newInput instanceof IASTModel[] ){
        	setModels( (IASTModel[])newInput );
        }
        else{
            setModels( new IASTModel[]{} );
        }
    }

    public void setModel( IASTModel model ){
    	if( model == null )
    		setModels( new IASTModel[]{} );
    	else
    		setModels( new IASTModel[]{ model } );
    }
    
    public void setModels( IASTModel[] models ){
    	List<ModelInfo> infos = new ArrayList<ModelInfo>();
    	for( int i = 0; i < models.length; i++ ){
    		ModelInfo info;
    		
    		if( i < this.models.size() ){
    			info = this.models.get( i );
    			infos.add( info );
    		}
    		else{
    			info = new ModelInfo();
    			info.backup = backups.get( i );
    			infos.add( info );
    		}
    		
    		info.model = models[i];
    	}
    	
    	if( this.models.size() > 0 && models.length > 0 && viewer != null ){
    		this.models = infos;
        	
        	NodeTreeRefiller refiller = new NodeTreeRefiller( this, roots );
        	refiller.refill( infos.toArray( new ModelInfo[ infos.size() ] ) );
        }
        else{
            // just replace
            roots = null;
            this.models = infos;
            if( viewer != null ){
                viewer.refresh();
                if( expandBaseTree ){
                	expandBaseTree();
                }
            }
        }
    }
    
    /**
     * Adds <code>model</code> to this provider, the viewer is informed
     * about the new roots.
     * @param model the new model
     * @param backup its backup, may be <code>null</code>
     */
    public void addModel( IASTModel model, ProjectModel backup ){
    	ModelInfo info = new ModelInfo();
    	info.model = model;
    	info.backup = backup;
    	
    	models.add( info );
    	
    	for( IASTModelNode node : model.getNodes( rootFilter ) ){
    		addRoot( model, node );
    	}
    }
    
    /**
     * Ensures that <code>model</code> is present. If <code>model</code>
     * is missing then it is added, but the viewer is not informed
     * about the new model.
     * @param model the model which must be present
     * @param backup its backup
     * @return <code>true</code> if the model was present
     */
    public boolean ensureModel( IASTModel model, ProjectModel backup ){
    	for( ModelInfo info : models ){
    		if( info.model == model ){
    			return true;
    		}
    	}
    	
    	ModelInfo info = new ModelInfo();
    	info.backup = backup;
    	info.model = model;
    	
    	models.add( info );
    	
    	return false;
    }

    /**
     * Forces the view to an update using <code>roots</code> as new roots.
     * @param roots the new roots
     */
    public void setRoots( Element[] roots ){
    	Set<Element> noExpansion = selectNoReexpansionElements();
    	Element[] oldRoots = this.roots;
    	this.roots = roots;
    	
    	if( viewer != null ){
    		updateViewer( null, oldRoots, roots, false, false );
    	}
    	
    	if( isExpandBaseTree() && viewer != null ){
    		expandBaseTree( noExpansion );
    	}
    }
    
    /**
     * Creates a selection of those elements that are currently in the tree, 
     * have not {@link Tag#NO_BASE_EXPANSION}, and are collapsed. This method
     * is intended to be called from the ui thread.
     */
    public Set<Element> selectNoReexpansionElements(){
    	Set<Element> result = new HashSet<Element>();
    	if( roots != null ){
    		for( Element root : roots ){
    			selectNoReexpansionElements( root, result );
    		}
    	}
    	return result;
    }
    
    private void selectNoReexpansionElements( Element check, Set<Element> result ){
    	if( viewer.getExpandedState( check ) ){
    		Element[] children = check.getBaseChildren();
    		if( children != null ){
    			for( Element child : children ){
    				selectNoReexpansionElements( child, result );
    			}
    		}
    	}
    	else{
    		if( check.getBaseChildren() != null ){
    			if( !check.getTags().contains( Tag.NO_BASE_EXPANSION )){
    				result.add( check );
    			}
    		}
    	}
    }
    
    /**
     * Expands the base tree: those connections which are not references.
     */
    public void expandBaseTree(){
    	expandBaseTree( Collections.<Element>emptySet() );
    }
    
    public void expandBaseTree( Set<Element> noExpansion ){
        if( viewer != null ){
        	// make sure the elements are initialized
            getElements();

            if( roots != null ){
                for( Element root : roots ){
                    expandBaseTree( root, noExpansion );
                }
            }
        }
    }

    private void expandBaseTree( final Element root, final Set<Element> noExpansion ){
        Job job = new CancelingJob( "Expand" ){
            @Override
            public IStatus run(IProgressMonitor monitor) {
            	monitor.beginTask( "Expand", 1 );

            	expandBaseTreeNow( root, noExpansion );

            	monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setPriority( Job.DECORATE );
        job.setSystem( true );
        job.schedule();
    }

    private void expandBaseTreeNow( final Element root, final Set<Element> noExpansion ){
        root.resolve( new ResolveCallback(){
            public void finished(Element element) {
                if( !root.getTags().contains( Tag.NO_BASE_EXPANSION ) && !noExpansion.contains( root )){
                    if( viewer != null &&  viewer.getControl() != null ){
                        if( !viewer.getControl().isDisposed() ){
                            viewer.setExpandedState( root, true );

                            for( Element child : root.getBaseChildren() ){
                                expandBaseTree( child, noExpansion );
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Searches for the node to which <code>connection</code> points.
     * @param element the element in which <code>connection</code> is declared
     * @param connection the connection to resolve
     * @param monitor to report progress
     * @return the node to which <code>connection</code> points or <code>null</code>
     */
    public IASTModelNode resolveNode( Element element, IASTModelNodeConnection connection, IProgressMonitor monitor ){
    	IASTModelNode node = null;

    	IASTModel model = element.getModel();
    	ProjectModel backup = element.getBackup();
    	
        if( model != null ){
            node = model.getNode( connection );
        }
        
        if( model != null && connection != null && node == null && backup != null && !isBackupForPathResolvingOnly() ){
        	ResolveConnectionJob job = new ResolveConnectionJob( backup, connection );
        	job.setPriority( Job.INTERACTIVE );
        	backup.runJob( job, monitor );
        	node = job.getContent();
        }
        
        return node;
    }

    /**
     * Information about the model from which an {@link Element} was
     * derived. 
     * @author Benjamin Sigg
     */
    public class ModelInfo{
    	public IASTModel model;
    	public ProjectModel backup;
    }
    
    /**
     * A node that wraps around an (imaginary) {@link IASTModelNode}.
     * @author Benjamin Sigg
     */
    public class Element implements Comparable<Element>{
    	private TreeViewer viewer;
    	
        private Element parent;

        private IASTModelNode node;
        private IASTModelNodeConnection connection;

        private boolean pathResolved = false;
        private IASTModelPath path;

        private boolean resolved = false;
        private Element[] children;

        private ModelInfo model;
        
        public Element( Element parent, IASTModelNodeConnection connection, TreeViewer viewer, ModelInfo model ){
            this.parent = parent;
            this.connection = connection;
            this.viewer = viewer;
            this.model = model;
        }
        
        public Element( Element parent, IASTModelNode node, TreeViewer viewer, ModelInfo model ){
            this.parent = parent;
            this.node = node;
            this.viewer = viewer;
            this.model = model;
        }
        
        public Element getElement( IASTModelElement element ){
        	if( element instanceof IASTModelNode ){
        		if( node != null ){
        			if( node.getPath().equals( ((IASTModelNode)element).getPath() ) )
        				return this;
        		}
        	}
        	if( element instanceof IASTModelNodeConnection ){
        		if( connection != null ){
        			IASTModelNodeConnection connection2 = (IASTModelNodeConnection)element;
        			if( connection2.getIdentifier().equals( connection.getIdentifier() ) &&
        				connection2.getPath().equals( connection.getPath() ) && 
        				(connection2.getRegion() != null && connection2.getRegion().equals( connection.getRegion() ))){
        				return this;
        			}
        		}
        	}
        
        	if( children == null )
        		return null;
        	
        	for( Element child : children ){
        		Element result = child.getElement( element );
        		if( result != null )
        			return result;
        	}
        	return null;
        }
        
        public IASTModel getModel(){
			return model.model;
		}
        
        public ProjectModel getBackup(){
        	return model.backup;
        }
        
        public ModelInfo getModelInfo(){
        	return model;
        }
        
        public void setViewer(TreeViewer viewer) {
			this.viewer = viewer;
		}
        
        public void removeChild( Element child ){
        	if( child.getParent() != this )
        		throw new IllegalArgumentException( "not a child of this node" );
        	
        	if( children == null )
        		return;
        	
        	int index = child.indexInParent();
        	TreePath path = child.getTreePath();
        	children = remove( children, index );
        	
        	if( viewer != null ){
        		fireRemoved( viewer, this, path, child );
        	}
        }
        
        public int indexInParent(){
        	if( parent == null ){
        		for( int i = 0; i < roots.length; i++ ){
        			if( roots[i] == this )
        				return i;
        		}
        	}
        	else{
        		for( int i = 0; i < parent.children.length; i++ ){
        			if( parent.children[i] == this ){
        				return i;
        			}
        		}
        	}
        	
        	return -1;
        }
        
        public int compareTo(Element o) {
        	int myIndex = indexInParent();
        	int otherIndex = o.indexInParent();
        	
        	if( myIndex < otherIndex )
        		return -1;
        	if( myIndex > otherIndex )
        		return 1;
        	return 0;
        }
        
        public Image getImage(){
            if( connection != null ){
                if( connection.getTags().contains( Tag.AST_CONNECTION_ICON_RESOLVE )){
                    return image( factory.getImageFor( connection.getTags() ), connection.getAttributes() );
                }
                else{
                    resolve();
                    if( node != null )
                        return image( factory.getImageFor( node.getTags() ), node.getAttributes() );
                    else
                        return image( factory.getImageFor( connection.getTags() ), connection.getAttributes() );
                }
            }

            return image( factory.getImageFor( getTags() ), getAttributes() );
        }

        public String getIdentifier(){
            resolve();
            if( node != null )
                return node.getIdentifier();
            else
                return connection.getIdentifier();
        }
        
        public String getLabel(){
        	return getLabel( true );
        }
        
        public String getLabel( boolean resolve ){
            if( connection != null ){
                if( connection.getTags().contains( Tag.AST_CONNECTION_LABEL_RESOLVE  ))
                    return connection.getLabel();
            }

            if( resolve ){
            	resolve();
            }
            if( node != null )
                return node.getLabel();
            else
                return connection.getLabel();
        }

        public TagSet getTags(){
            if( connection != null )
                return connection.getTags();
            else
                return node.getTags();
        }
        
        public IASTModelAttribute[] getAttributes(){
        	if( connection != null )
        		return connection.getAttributes();
        	else
        		return node.getAttributes();
        }

        public int getDepth(){
            if( parent == null )
                return 0;
            return parent.getDepth()+1;
        }

        public ElementPath getFullPath(){
            ElementPath path;
            if( parent == null ){
                path = new ElementPath();
                path.add( getPath(), 0 );
            }
            else{
                path = parent.getFullPath();
                path.add( getPath(), parent.samePathIndex( this ));
            }

            return path;
        }

        public TreePath getTreePath(){
        	LinkedList<Object> list = new LinkedList<Object>();
        	Element element = this;
        	Element root = this;
        	
        	while( element != null ){
        		list.addFirst( getPathElement( element ) );
        		root = element;
        		element = element.getParent();
        	}
        	
        	Object[] rootPath = getRootPath( root );
        	for( int i = rootPath.length-1; i >= 0; i-- ){
        		list.addFirst( rootPath[i] );
        	}
        	
        	return new TreePath( list.toArray() );
        }
        
        private int samePathIndex( Element child ){
            IASTModelPath path = child.getPath();
            int count = 0;
            for( Element check : getChildren() ){
                if( check == child )
                    return count;
                
                IASTModelPath checkPath = check.getPath();
                if( (path == null && checkPath == null) || (path != null && path.equals( checkPath )))
                    count++;
            }
            
            return -1;
        }
        
        public IASTModelPath getPath(){
            if( pathResolved )
                return path;

            pathResolved = true;
            resolve();

            if( node != null ){
                path = node.getPath();
                return path;
            }

            ProjectModel backup = getBackup();
            
            if( backup != null ){
                IDeclaration declaration;
                if( backup.secureThread() ){
                    declaration = backup.getDeclaration( connection );
                }
                else{
                    FerryJob<IDeclaration> job = new FerryJob<IDeclaration>( "Resolve connection" ){
                        @Override
                        public IStatus run( IProgressMonitor monitor ){
                            monitor.beginTask( "resolve connection", 1 );
                            ProjectModel backup = getBackup();
                            content = backup.getDeclaration( connection );
                            monitor.done();
                            return Status.OK_STATUS;
                        }
                    };
                    job.setPriority( Job.INTERACTIVE );
                    job.setSystem( true );
                    backup.runJob( job, null );
                    declaration = job.getContent();
                }

                if( declaration != null ){
                    path = declaration.getPath();
                    return path;
                }
            }

            return null;
        }

        public IFileRegion getNodeRegion(){
            resolve();
            if( node != null ){
                return node.getRegion();
            }

            return null;
        }

        public IASTModelNode getNode(){
			return node;
		}
        
        public IFileRegion getRegion(){
            if( connection != null ){
                IFileRegion region = connection.getRegion();
                if( region != null )
                    return region;
            }

            return getNodeRegion();
        }

        public Element getParent(){
            return parent;
        }

        @Override
        public String toString(){
            return getLabel( false );
        }

        public void resolve(){
            resolve( null );
        }

        public synchronized void resolve( final ResolveCallback callback ){
            if( !resolved ){
                resolved = true;

                Job update = new UpdateElementJob( this, callback );
                update.schedule();
            }
            else if( callback != null ){
                Job job = new UIJob( "Callback" ){
                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        monitor.beginTask( "Call", IProgressMonitor.UNKNOWN );
                        callback.finished( Element.this );
                        monitor.done();
                        return Status.OK_STATUS;
                    }
                };
                job.setSystem( true );
                job.setPriority( Job.INTERACTIVE );
                job.schedule();
            }
        }

        public boolean hasChildren(){
            resolve();
            return children != null && children.length > 0;
        }

        public boolean isReference(){
            return connection != null && connection.isReference();
        }

        public Element[] getBaseChildren(){
            if( children == null )
                return new Element[]{};

            List<Element> list = new ArrayList<Element>();
            for( Element child : children ){
                if( !child.isReference() ){
                    list.add( child );
                }
            }
            return list.toArray( new Element[ list.size() ] );
        }

        public Element[] getChildren(){
            resolve();
            return children;
        }

        public IASTModelNode getUnresolvedNode() {
            return node;
        }

        public IASTModelNodeConnection getUnresolvedConnection() {
            return connection;
        }

        /**
         * Completely exchanges the content of this element, also notifies
         * the viewer that this element has changed.
         * @param resolved whether the information is resolved or not
         * @param node the new node
         * @param connection the new connection
         * @param children the new children
         */
        public void setContent( boolean resolved, IASTModelNode node, IASTModelNodeConnection connection, Element[] children ){
        	Element[] oldChildren = this.children;
        	
        	String oldLabel = getLabel();
        	Image oldIcon = getImage();
        	
        	this.resolved = resolved;
        	this.node = node;
        	this.connection = connection;
            this.children = children;

            String newLabel = getLabel();
            Image newIcon = getImage();
            
            if( viewer != null ){
            	boolean labelChanged = oldLabel == null ? (newLabel != null) : (!oldLabel.equals( newLabel ));
            	boolean iconChanged = oldIcon != newIcon;
            	
            	updateViewer( this, oldChildren, children, labelChanged, iconChanged );
            }
        }
    }

    private static interface ResolveCallback{
        public void finished( Element element );
    }

    private class UpdateElementJob extends CancelingJob{
        private Element element;
        private ResolveCallback callback;

        public UpdateElementJob( Element element, ResolveCallback callback ){
            super( "Resolve '" + element.getLabel( false ) + "'" );
            this.element = element;
            this.callback = callback;
            setPriority( Job.INTERACTIVE );
        }

        @Override
        public IStatus run(IProgressMonitor monitor) {
            monitor.beginTask( "Update", 3 );

            IASTModel model = element.getModel();
            
            if( model != null ){
            	IASTModelNode node = element.getUnresolvedNode();
                if( node == null ){
                	node = resolveNode( element, element.getUnresolvedConnection(), new SubProgressMonitor( monitor, 1 ) );
                }

                monitor.worked( 1 );

                if( node != null ){
                    Element[] children = null;

                    IASTModelNodeConnection[] connections = node.getChildren();
                    if( connections != null ){
                        children = new Element[ connections.length ];

                        for( int i = 0, n = children.length; i<n; i++ ){
                            children[i] = new Element( element, connections[i], viewer, element.getModelInfo() );
                        }
                    }

                    setContent( element, true, node, element.getUnresolvedConnection(), children, callback );
                }
            }

            monitor.done();
            return Status.OK_STATUS;
        }
    }

    private class Content{
    	private Element element;
        private IASTModelNode node;
        private IASTModelNodeConnection connection;
        private Element[] children;
        private ResolveCallback callback;
        private boolean resolved;
        
        public Content( Element element, boolean resolved, IASTModelNode node, IASTModelNodeConnection connection, Element[] children, ResolveCallback callback ){
        	this.resolved = resolved;
            this.element = element;
            this.node = node;
            this.connection = connection;
            this.children = children;
            this.callback = callback;
        }
        
        public void transmit(){
        	element.setContent( resolved, node, connection, children );
            // Debug.info( String.valueOf( node ) );
            if( callback != null )
                callback.finished( element );
        }
    }
    
    private class SetContentJob extends UIJob{
    	private Queue<Content> contents = new LinkedList<Content>();
    	private boolean running = false;
    	
        public SetContentJob(){
            super( "Update UI" );
            setSystem( true );
            setPriority( Job.INTERACTIVE );
        }
        
        public void dispatch( Content content ){
        	synchronized( contents ){
        		contents.add( content );
        		if( !running ){
        			schedule();
        		}
        	}
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
        	monitor.beginTask( "Set Content", IProgressMonitor.UNKNOWN );

        	while( true ){
        		Content content;
        		synchronized( contents ){
        			if( contents.isEmpty() ){
        				running = false;
        				break;
        			}
        			else{
        				content = contents.remove();
        			}
        		}
        		
        		content.transmit();
        	}
        	
            monitor.done();
            return Status.OK_STATUS;
        }
    };
    
    private class AddJob extends UIJob{
    	private final Object lock = new Object();
    	
    	private List<IASTModel> models = new ArrayList<IASTModel>();
    	private List<IASTModelElement> elements = new ArrayList<IASTModelElement>();
    	
    	public AddJob(){
    		super( "Add Node" );
    		setSystem( true );
    		setPriority( Job.DECORATE );
    	}
    	
    	public void dispatch( IASTModel model, IASTModelElement element ){
    		synchronized( lock ){
    			models.add( model );
    			elements.add( element );
    			this.schedule();
			}
    	}
    	
    	@Override
    	public IStatus runInUIThread( IProgressMonitor monitor ){
    		IASTModel[] models;
    		IASTModelElement[] elements;
    		
    		synchronized( lock ){
				models = this.models.toArray( new IASTModel[ this.models.size() ] );
				elements = this.elements.toArray( new IASTModelElement[ this.elements.size() ] );
				
				this.models.clear();
				this.elements.clear();
			}
    		
    		if( isDisposed() ){
    			return Status.OK_STATUS;
    		}
    		
			monitor.beginTask( "add node", models.length );
			
			if( roots == null ){
				monitor.done();
				return Status.OK_STATUS;
			}
			
			List<Element> newElements = new ArrayList<Element>( elements.length );
			
			for( int i = 0; i < models.length; i++ ){
				IASTModel model = models[i];
				IASTModelElement element = elements[i];
				
				Element node = null;
				
				for( ModelInfo info : NodeContentProvider.this.models ){
					if( info.model == model ){
						if( element instanceof IASTModelNodeConnection )
							node = new Element( null, (IASTModelNodeConnection)element, viewer, info );
						else if( element instanceof IASTModelNode )
							node = new Element( null, (IASTModelNode)element, viewer, info );
						
						break;
					}
				}
				
				if( node != null ){
					newElements.add( node );
				}
				
				monitor.worked( 1 );
			}
			
			if( newElements.size() > 0 ){
				Element[] newRoots = new Element[ roots.length+newElements.size() ];
				System.arraycopy( roots, 0, newRoots, 0, roots.length );
				
				for( int i = 0, n = newElements.size(); i<n; i++ ){
					newRoots[ roots.length + i ] = newElements.get( i );
				}
				
				roots = newRoots;
				for( Element newElement : newElements ){
					addRoot( viewer, newElement );
				}
			}
			
			monitor.done();
			return Status.OK_STATUS;
    	}
    }
}
