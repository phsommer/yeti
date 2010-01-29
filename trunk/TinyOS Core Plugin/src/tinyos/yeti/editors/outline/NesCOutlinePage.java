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
package tinyos.yeti.editors.outline;

import static tinyos.yeti.ep.parser.ASTNodeFilterFactory.and;
import static tinyos.yeti.ep.parser.ASTNodeFilterFactory.not;
import static tinyos.yeti.ep.parser.ASTNodeFilterFactory.subset;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.INesCEditorParserClient;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelElement;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.preferences.PreferenceConstants;
import tinyos.yeti.views.NodeContentProvider;
import tinyos.yeti.views.NodeLabelComparator;
import tinyos.yeti.views.NodeLabelProvider;
import tinyos.yeti.views.NodeLocationComparator;
import tinyos.yeti.views.NodeContentProvider.Element;

public class NesCOutlinePage extends ContentOutlinePage implements INesCEditorParserClient {
    private NesCEditor editor;

    private String contextMenuID = "outlineMenu";
    private Menu menu;

    private OpenFileAction openAction;

    private SorterAction sorterAction;

	private FilterSelectionAction filterAction;
	
	private MultiOutlineFilter filter;
	
    private IASTModel model;

    private NodeContentProvider provider;
    
    private boolean selecting = false;
    private long awaitSelectionEvent = 0;
    
    private org.eclipse.jface.util.IPropertyChangeListener propertyChangeListener;

    /**
     * Creates a content outline page using the given provider and the given editor.
     * @param editor the editor whose content this outline shows
     */
    public NesCOutlinePage( NesCEditor editor) {
        super();
        this.editor= editor;

        openAction = new OpenFileAction();
        
        editor.addParserClient( this );
        
        editor.getEditorSourceViewer().addPostSelectionChangedListener( new ISelectionChangedListener(){
        	public void selectionChanged( SelectionChangedEvent event ){
        		if( awaitSelectionEvent + 5000 > System.currentTimeMillis() ){
        			awaitSelectionEvent = 0;
        			return;
        		}
        		
        		if( !selecting ){
        			try{
        				selecting = true;
        			
        				IASTModelElement[] nodes = NesCOutlinePage.this.editor.getSelectedElements();
        				if( nodes != null ){
        					for( IASTModelElement node : nodes ){
        						Element element = provider.getElement( node );
        						if( element != null ){
        							TreeSelection selection = new TreeSelection( new TreePath[]{ element.getTreePath() } );
	        						getTreeViewer().setSelection( selection );
	        						break;
        						}
	        				}
        				}
        			}
        			finally{
        				selecting = false;
        			}
        		}
        	}
        });

        // shows only the range in editor
        editor.showHighlightRangeOnly( false );

        this.addSelectionChangedListener(openAction);	

    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        final TreeViewer viewer = getTreeViewer();

        provider = new NodeContentProvider( and( not( subset( Tag.INCLUDED ) ), subset( Tag.OUTLINE ) ) );
        provider.setExpandBaseTree( true );
        
        IProject project = editor.getProject();
        if( project != null ){
        	try{
        		ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
        		if( tos != null ){
        			provider.setBackup( 0, tos.getModel() );
        		}
        	}
        	catch( MissingNatureException ex ){
        		// ignore
        	}
        }
        
        viewer.setContentProvider( provider );
        viewer.setLabelProvider( new NodeLabelProvider() );
        viewer.addSelectionChangedListener(this);
        viewer.getTree().addMouseListener(new MouseListener(){
            public void mouseDoubleClick(MouseEvent e) {
                openAction.run();
            }

            public void mouseDown(MouseEvent e) {
                // ignore
            }

            public void mouseUp(MouseEvent e) {
                // ignore
            }
        });

        // Menu for Outlinepage
        MenuManager manager= new MenuManager(contextMenuID, contextMenuID);
        manager.setRemoveAllWhenShown(false);

        menu = manager.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);

        getSite().registerContextMenu(contextMenuID, manager, viewer);


        manager.add(
        		openAction
        );

        manager.add(new Separator());

        menu= manager.createContextMenu(viewer.getControl());

        //		 		required, for extensions
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        sorterAction = new SorterAction();
        filterAction = new FilterSelectionAction( this );
        
        contributeToActionBars();
        setFilters();
        
        // set a dummy input, the NodeContentProvider will take care of the rest
        viewer.setInput( new Object() );
        
        setModel( editor.getASTModel() );
        parent.layout();
    }

    private void contributeToActionBars() {
        IActionBars bars = getSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager toolBar) {
        toolBar.add(sorterAction);
        toolBar.add(filterAction);
    }

    private void setFilters(){
    	setFiltersSilent( getFilters() );
    }
    
    @Override
    public void dispose(){
    	if( propertyChangeListener != null ){
    		TinyOSPlugin plugin = TinyOSPlugin.getDefault();
    		if( plugin != null ){
    			plugin.getPreferenceStore().removePropertyChangeListener( propertyChangeListener );
    		}
    		propertyChangeListener = null;
    	}
    	super.dispose();
    }
    
    public void setFilters( String[] ids ){
    	IPreferenceStore store = TinyOSPlugin.getDefault().getPreferenceStore();
        StringBuilder builder = new StringBuilder();
        for( String id : ids ){
        	builder.append( id );
        	builder.append( ';');
        }
        store.setValue( PreferenceConstants.OUTLINE_FILTER, builder.toString() );
        
        setFiltersSilent( ids );
    }
    
    public String[] getFilters(){
    	IPreferenceStore store = TinyOSPlugin.getDefault().getPreferenceStore();
    	if( propertyChangeListener == null ){
    		propertyChangeListener = new IPropertyChangeListener(){
				public void propertyChange( PropertyChangeEvent event ){
					if( PreferenceConstants.OUTLINE_FILTER.equals( event.getProperty() )){
						setFilters();
					}
				}
			};
			store.addPropertyChangeListener( propertyChangeListener );
    	}
    	
        String ids = store.getString( PreferenceConstants.OUTLINE_FILTER );
        if( ids != null ){
        	return ids.split( ";" );
        }
        else{
        	return new String[]{};
        }
    }
    
    private void setFiltersSilent( String[] ids ){
        OutlineFilterFactory[] factories = TinyOSPlugin.getDefault().getOutlineFilters();
        Set<String> check = new HashSet<String>();
        for( String id : ids ){
        	check.add( id );
        }
        filter = new MultiOutlineFilter();
        for( OutlineFilterFactory factory : factories ){
        	if( check.contains( factory.getId() )){
        		filter.add( factory.create() );
        	}
        }
        
        setFilter( filter );
    }
    
    private void setFilter( IOutlineFilter filter ){
    	if( filter == null ){
    		getTreeViewer().setFilters( new ViewerFilter[]{} );
    	}
    	else{
    		filter.setEditor( editor );
    		getTreeViewer().setFilters( new ViewerFilter[]{ new OutlineViewFilter( filter ) } );
    	}
    }
    
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        super.selectionChanged(event);
        
        if( getTreeViewer().getControl().isFocusControl() ){
	        if( !selecting ){
				try{
					selecting = true;
	
					ISelection selection= event.getSelection();
	
					if (selection.isEmpty())
						editor.resetHighlightRange();
					else {
						Object first = ((IStructuredSelection)selection).getFirstElement();
						NodeContentProvider.Element element = (NodeContentProvider.Element)first;
						editor.selectAndReveal( element.getRegion() );
					}
					awaitSelectionEvent = System.currentTimeMillis();
				}
				finally{
					selecting = false;
				}
			}
        }
    }

    protected class SorterAction extends SortTreeAction {
        private static final String SORT_TREE = "SortMembers"; //$NON-NLS-1$

        public SorterAction() {
            super( getSettings().getBoolean( SORT_TREE ), 
            		new NodeLabelComparator( NodeContentProvider.PROPERTY_LABEL ),
            		new NodeLocationComparator( NodeContentProvider.PROPERTY_LABEL, NodeContentProvider.PROPERTY_ORDER ));
        }
        
        @Override
        protected void setComparator( ViewerComparator comparator ){
        	TreeViewer viewer = getTreeViewer();
        	
        	TreePath[] expanded = viewer.getExpandedTreePaths();
            viewer.setComparator( comparator );
            viewer.setExpandedTreePaths( expanded );
        }
        
        @Override
        protected void informSelected( boolean checked ){
            getSettings().put( SORT_TREE, checked );
        }
    }

    /**
     * Returns setting for this control.
     * 
     * @return Settings.
     */
    private IDialogSettings getSettings() {
        final String sectionName = "TinyOS.view.Outline";
        IDialogSettings settings = TinyOSPlugin.getDefault().getDialogSettings().getSection(sectionName);
        if (settings == null) {
            settings = TinyOSPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
        }
        return settings;
    }

    public void setupParser( NesCEditor editor, INesCParser parser ) {
        // nothing to do
    }
    
    public void closeParser( NesCEditor editor, boolean successful, INesCParser parser ) {
        if( successful || model == null )
            setModel( parser.getASTModel() );
    }
    
    public void setModel( final IASTModel model ){
    	if( this.model != model ){
    		this.model = model;

    		UIJob job = new UIJob( "Update outline" ){
    			@Override
    			public IStatus runInUIThread( IProgressMonitor monitor ){
    				IASTModel model = NesCOutlinePage.this.model;

    				TreeViewer viewer = getTreeViewer();
    				if( viewer != null ){
    					Control control = viewer.getControl();
    					if( !control.isDisposed() ){
    						provider.setModel( model );
    					}
    				}

    				return Status.OK_STATUS;
    			}
    		};

    		job.setSystem( true );
    		job.setPriority( Job.INTERACTIVE );
    		job.schedule();
    	}
    }
} 



