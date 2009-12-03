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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.outline.OpenFileAction;
import tinyos.yeti.editors.outline.SortTreeAction;
import tinyos.yeti.ep.parser.ASTView;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.model.ViewModel;
import tinyos.yeti.widgets.ComboFilterViewer;
import tinyos.yeti.widgets.FilteredTree;
import tinyos.yeti.widgets.ITreeFilter;
import tinyos.yeti.widgets.helper.ASTModelFilter;

/**
 * This view contains a set of tabs. Each tab contains a tree with various
 * elements that are available for development.
 * @author Benjamin Sigg
 */
public class InterfacesView extends ViewPart {
    private FilteredTree[] viewers;
    /** the viewer that has been marked as the most important one */
    private TreeViewer center;

    private ComboFilterViewer filterViewer;
    private CTabFolder tabFolder;
    
    private ViewModel model;

    private LabelProvider labelProvider;
    private List<NodeContentProvider> contentProviders = new ArrayList<NodeContentProvider>();

    private String fContextMenuID = "interfacesMenu";

    private OpenFileAction openAction;

    public InterfacesView() {
        super();
        
        labelProvider = new NodeLabelProvider();
        openAction = new OpenFileAction();
        model = new ViewModel( this );
    }

    public void refresh( final ProjectModel backup ){
        UIJob job = new UIJob( "Refresh" ){
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ){
                if( viewers != null ){
                    if( !center.getControl().isDisposed() ){
                    	IASTModel source = model.getASTModel();
                    	for( NodeContentProvider provider : contentProviders ){
                            provider.setBackup( 0, backup );
                            provider.setModel( source );
                        }

//                        for( FilteredTree viewer : viewers ){
//                            viewer.getViewer().setInput( source );
//                        }
                    }
                }
                
                return Status.OK_STATUS;
            }
        };
        job.setPriority( Job.DECORATE );
        job.schedule();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#createTreeViewer(org.eclipse.swt.widgets.Composite)
     */
    private FilteredTree createTreeViewer( Composite parent, ITreeContentProvider provider, ComboFilterViewer filterViewer ) {
        ITreeFilter filter = new ASTModelFilter();

        int styleBits = SWT.SINGLE | SWT.H_SCROLL;
        
        FilteredTree filteredTree = new FilteredTree( parent, styleBits, filter, filterViewer );

        //filteredTree.setBackground( parent.getBackground() );
        //filteredTree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

        TreeViewer tree = filteredTree.getViewer();
        
        tree.setContentProvider(provider);
        tree.setLabelProvider(labelProvider);

        // make a dummy input, the content provider will handle the rest
        tree.setInput( new Object() );
        //tree.setComparator( new WorkbenchViewerComparator() );
        tree.setComparator( new RootNodeComparator( NodeContentProvider.PROPERTY_LABEL ) );
        tree.getTree().addMouseListener(new MouseListener(){
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


        tree.addSelectionChangedListener(openAction);

        return filteredTree;
    }


    @Override
    public void createPartControl( Composite parent ){
        Composite control = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 1, false );
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 1;
        layout.verticalSpacing = 0;
        control.setLayout( layout );
        
        createFilterViewer( control );
        
        Line line = new Line( control );
        GridData data = new GridData( SWT.FILL, SWT.CENTER, true, false );
        data.verticalIndent = 2;
        line.setLayoutData( data );
        
        createTabs( control );
    }
    
    protected void createFilterViewer( Composite parent ){
        filterViewer = new ComboFilterViewer( "type filter text", true ){
            private ViewerComparator comparator;
            
            @Override
            protected void createToolbar( ToolBarManager toolbar ){
                super.createToolbar( toolbar );
                
                SortTreeAction sort = new SortTreeAction( false,
                		new NodeLabelComparator( NodeContentProvider.PROPERTY_LABEL ),
                		new RootNodeComparator( NodeContentProvider.PROPERTY_LABEL ), true ){
                    @Override
                    protected void setComparator( ViewerComparator newComparator ){
                        comparator = newComparator;
                        
                        FilteredTree tree = getFocused();
                        if( tree != null )
                            tree.getViewer().setComparator( comparator );
                    }
                };
                
                toolbar.add( sort );
            }
            
            @Override
            public void setFocused( FilteredTree focused ){
                super.setFocused( focused );
                if( focused != null ){
                    focused.getViewer().setComparator( comparator );
                }
            }
        };
        
        filterViewer.createControl( parent );
        filterViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }
     
    protected void createTabs( Composite parent ){
        tabFolder = new CTabFolder( parent, SWT.FLAT | SWT.BOTTOM | SWT.MULTI );
        tabFolder.setBorderVisible( false );
        
        tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        ASTView[] views = TinyOSPlugin.getDefault().getParserFactory().getViews();
        viewers = new FilteredTree[ views.length ];

        int index = 0;

        for( ASTView view : views ){
            Composite composite = new Composite( tabFolder, SWT.NONE );

            composite.setLayout( new FillLayout() );
            CTabItem item = new CTabItem( tabFolder, SWT.NONE );
            item.setImage( view.getImage() );
            item.setText( view.getLabel() );
            item.setControl( composite );

            NodeContentProvider provider = new NodeContentProvider( view.getFilter() );
            provider.setBackupForPathResolvingOnly( false );
            contentProviders.add( provider );
            FilteredTree tree = createTreeViewer( composite, provider, filterViewer );
            viewers[ index++ ] = tree;
            TreeViewer viewer = tree.getViewer();

            viewer.addDoubleClickListener(new IDoubleClickListener() {
                public void doubleClick(DoubleClickEvent event) {
                    handleDoubleClick(event);
                }
            });
            viewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    handleSelectionChanged(event);
                }
            });
            createMenuManager( viewer );
        }

        for( int i = 0, n = views.length; i<n; i++ ){
            if( views[i].isSelectionProvider() ){
                center = viewers[i].getViewer();
                filterViewer.setFocused( viewers[i] );
                tabFolder.setSelection( i );
                getSite().setSelectionProvider( center );
                break;
            }
        }

        tabFolder.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                selectionChanged();
            }
            public void widgetSelected( SelectionEvent e ){
                selectionChanged();
            }
        });        
//      tabFolder.setInsertMark(tabItemUses,true);
    }
    
    private void selectionChanged(){
        int index = tabFolder.getSelectionIndex();
        if( index >= 0 ){
            filterViewer.setFocused( viewers[index] );
        }
    }

    private void createMenuManager(Viewer fViewer) {
        MenuManager manager= new MenuManager(fContextMenuID, fContextMenuID);
        manager.setRemoveAllWhenShown(false);

        Menu fMenu = manager.createContextMenu(fViewer.getControl());
        fViewer.getControl().setMenu(fMenu);
        getSite().registerContextMenu(fContextMenuID, manager, fViewer);


        manager.add(			
                openAction
        );

        manager.add(new Separator());

        fMenu= manager.createContextMenu(fViewer.getControl());

        //		 		required, for extensions
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        //-----------------------
    }



    protected void handleSelectionChanged(SelectionChangedEvent event) {
        // TODO Auto-generated method stub

    }

    protected void handleDoubleClick(DoubleClickEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFocus() {
        if( center != null )
            center.getTree().setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        model.dispose();
        model = null;
    }
}

