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
package tinyos.yeti.wizards.content;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.nature.MissingNatureException;

/**
 * Tree showing files and folders of some project.
 * @author Benjamin Sigg
 */
public abstract class ResourceTree extends GenericObserved<IContainer>{
    private TreeViewer tree;
    private IProject project;
    private MakeTarget target;
    private Provider provider;
    
    public ResourceTree( Composite parent, int style ){
        provider = new Provider();
        
        tree = new TreeViewer( parent, style | SWT.SINGLE );
        tree.setContentProvider( provider );
        tree.setLabelProvider( new WorkbenchLabelProvider() );
        tree.setInput( new Root( null ) );
        
        tree.addSelectionChangedListener( new ISelectionChangedListener(){
            public void selectionChanged( SelectionChangedEvent event ){
                changed();
            }
        });
        
    }
    
    /**
     * Gets the project whose contents are shown in this tree.
     * @return the project
     */
    public IProject getProject(){
        return project;
    }
    
    public TreeViewer getTree(){
        return tree;
    }
    
    public Control getControl(){
        return tree.getControl();
    }
    
    public void setProject( IProject project ){
        if( this.project != project ){
            target = null;
            
            this.project = project;
            tree.setInput( new Root( project ) );

            if( project != null ){
                setContainers( getRoots( project ) );
            
                try{
                	ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
                	target = tos.getMakeTarget();
                }
                catch( MissingNatureException ex ){
                	target = null;
                }
            }
            else{
            	target = null;
            }
        }
    }
    
    /**
     * Tries to select the file with the path <code>root + path</code>. Does
     * fail if that resource does not exist
     * @param path some path
     */
    public void selectFile( String path ){
    	if( project != null ){
    		IPath location = new Path( path );
    		List<TreePath> selection = new ArrayList<TreePath>();
    		
    		for( IContainer root : getRoots( project )){
    			IFile file = root.getFile( location );
    			if( file.exists() ){
    				selection.add( getTreePath( root, file ) );
    			}
    		}
    		
    		if( !selection.isEmpty() ){
    			tree.setSelection( new TreeSelection( selection.toArray( new TreePath[ selection.size() ] )) );
    		}
    	}
    }
    
    /**
     * Tries to select <code>resource</code>.
     * @param resource the resource to select
     */
    public void select( IResource resource ){
    	IContainer container = sourceContainerOf( resource );
    	if( container != null ){
    		tree.setSelection( new TreeSelection( getTreePath( container, resource )) );
    	}
    }
    
    public IContainer sourceContainerOf( IResource resource ){
    	IPath resourcePath = resource.getFullPath();
    	
    	IContainer[] containers = getRoots( project );
    	if( containers != null ){
    		for( IContainer container : containers ){
    			if( container.getFullPath().isPrefixOf( resourcePath )){
    				return container;
    			}
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Gets the path to the selected resource
     * @return the path or <code>null</code>
     */
    public IPath getPath( IResource resource ){
    	IContainer root = sourceContainerOf( resource );
    	if( root == null )
    		return null;
    	return getPath( root, resource );
    }
    
    /**
     * Gets the path to the selected resource
     * @param root the source container in which <code>resource</code> is
     * @return the path or <code>null</code>
     */
    public IPath getPath( IContainer root, IResource resource ){
        IPath projectPath = resource.getProjectRelativePath();
        if( projectPath == null )
            return null;
        
        IPath rootPath = root.getProjectRelativePath();
        if( rootPath.isPrefixOf( projectPath ))
            return projectPath.removeFirstSegments( rootPath.segmentCount() );
        
        return null;
    }
    
    public TreePath getTreePath( IContainer root, IResource resource ){
        LinkedList<IResource> path = new LinkedList<IResource>();
        path.addFirst( resource );
        
        while( resource != null && !resource.equals( root )){
            resource = resource.getParent();
            if( resource != null ){
                path.addFirst( resource );
            }
        }
        
        return new TreePath( path.toArray() );
    }
    
    /**
     * Gets the root container of <code>project</code>.
     * @param project some project
     * @return the root of all files
     */
    protected abstract IContainer[] getRoots( IProject project );
    
    /**
     * Tells whether <code>resource</code> should be visible in the tree
     * @param resource some resource
     * @return <code>true</code> if <code>resource</code> should be visible
     */
    protected boolean visible( IResource resource ){
        if( target != null && resource instanceof IFolder ){
            IPath location = resource.getLocation();            
            if( location != null ){
                if( target.getExclude().shouldExclude( location.toFile() ) )
                    return false;
            }
        }
        
        return true;
    }
    
    public void setContainers( IContainer[] containers ){
    	for( IContainer container : containers ){
    		tree.expandToLevel( container, 0 );
    	}
        if( containers.length > 0 ){
        	tree.setSelection( new TreeSelection( path( containers[0] ) ) );
        }
    }
    
    private TreePath path( IContainer container ){
        List<IContainer> elements = new ArrayList<IContainer>();
        elements.add( container );
        while( !(container instanceof IProject) ){
            container = container.getParent();
            elements.add( container );
        }
        
        Object[] path = new Object[ elements.size() ];
        for( int i = path.length-1, j = 0; i >= 0; i--, j++ )
            path[i] = elements.get( j );
        
        return new TreePath( path );
    }
    
    /**
     * Gets the selected resource. 
     * @return the selected resource
     */
    public IResource getResource(){
        ITreeSelection selection = (ITreeSelection)tree.getSelection();
        if( selection.isEmpty() )
            return null;
        
        Object first = selection.getFirstElement();
        if( first instanceof IResource )
            return (IResource)first;
        
        return null;
    }
    
    /**
     * Gets the selected container
     * @return the selected container
     */
    public IContainer getContainer(){
        IResource resource = getResource();
        if( resource instanceof IContainer )
            return (IContainer)resource;
        
        return null;
    }
    
    private void changed(){
        trigger( getContainer() );
    }
    
    private class Root{
        public IProject project;
        
        public Root( IProject project ){
            this.project = project;
        }
    }
    
    private class Provider implements ITreeContentProvider{
        private Root root;
        
        public Object[] getChildren( Object parentElement ){
            try{
                if( parentElement == root )
                    return new Object[]{ root.project };
                
                if( parentElement instanceof IContainer ){
                    IResource[] resources = ResourceUtil.resources( ((IContainer)parentElement).members() );
                    List<IResource> result = new ArrayList<IResource>();
                    for( IResource resource : resources ){
                        if( visible( resource ))
                            result.add( resource );
                    }
                    return result.toArray();
                }
            }
            catch( CoreException ex ){
                // ignore
            }
            
            return new Object[]{};
        }

        public Object getParent( Object element ){
            if( root != null && element == root.project )
                return root;
            else
                return ((IResource)element).getParent();
        }

        public boolean hasChildren( Object element ){
            if( element instanceof IContainer )
                return true;
            
            return false;
        }

        public Object[] getElements( Object inputElement ){
            if( root == null || root.project == null )
                return new Object[]{};
            
            return getRoots( root.project );
        }

        public void dispose(){
        	// ignore
        }

        public void inputChanged( Viewer viewer, Object oldInput, Object newInput ){
        	root = (Root)newInput;
        	if( root != null ){
        		viewer.refresh();
        	}
        }
    }
}
