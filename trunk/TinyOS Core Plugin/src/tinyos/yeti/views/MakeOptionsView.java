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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.make.IProjectMakeTargets;
import tinyos.yeti.make.MakeTargetEvent;
import tinyos.yeti.make.MakeTargetManager;
import tinyos.yeti.views.make.AddTargetAction;
import tinyos.yeti.views.make.BuildTargetAction;
import tinyos.yeti.views.make.CopyTargetAction;
import tinyos.yeti.views.make.CutTargetAction;
import tinyos.yeti.views.make.DeleteTargetAction;
import tinyos.yeti.views.make.EditProperties;
import tinyos.yeti.views.make.EditTargetAction;
import tinyos.yeti.views.make.MakeLabelProvider;
import tinyos.yeti.views.make.MakeOptionsContentProvider;
import tinyos.yeti.views.make.PasteTargetAction;
import tinyos.yeti.views.make.SetDefaultTargetAction;

/**
 * The view for the make-targets
 */
public class MakeOptionsView extends ViewPart {
	private static final String EXPANSION_STATE = "MakeOptionsView.expansionState";
	
    private BuildTargetAction buildTargetAction;
    private EditTargetAction editTargetAction;
    private EditProperties editProperties;

    private CopyTargetAction copyTargetAction;
    private CutTargetAction cutTargetAction;
    private PasteTargetAction pasteTargetAction;
    private DeleteTargetAction deleteTargetAction;
    
    private SetDefaultTargetAction setDefaultTargetAction;
    private AddTargetAction addTargetAction;

    private Action trimEmptyFolderAction;

    private DrillDownAdapter drillDownAdapter;

    private TreeViewer treeViewer = null;

    private boolean onExpansion = false;
    
    public MakeOptionsView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {
    	treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

        drillDownAdapter = new DrillDownAdapter(treeViewer);

        treeViewer.setContentProvider(new MakeOptionsContentProvider(){
        	@Override
        	protected void syncTargetChange( MakeTargetEvent event ){
        		super.syncTargetChange( event );
        		switch( event.getType() ){
                    case MakeTargetEvent.PROJECT_ADDED:
                    case MakeTargetEvent.PROJECT_REFRESH:
                    	checkExpand( event.getProject() );
                    	break;
        		}
        	}
        });
        treeViewer.setLabelProvider(new MakeLabelProvider());

        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                handleDoubleClick( event );
            }
        });
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                handleSelectionChanged( event );
            }
        });
        treeViewer.addTreeListener( new ITreeViewerListener(){
        	public void treeExpanded( TreeExpansionEvent event ){
        		storeExpanded( event.getElement(), true );
        	}
        	public void treeCollapsed( TreeExpansionEvent event ){
        		storeExpanded( event.getElement(), false );
        	}
        });

        treeViewer.getControl().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if( event.character == SWT.DEL && event.stateMask == 0 ){
                    deleteTargetAction.run();
                }
                if( event.keyCode == 'c' && event.stateMask == SWT.CTRL ){
                	copyTargetAction.run();
                }
                if( event.keyCode == 'x' && event.stateMask == SWT.CTRL ){
                	cutTargetAction.run();
                }
                if( event.keyCode == 'v' && event.stateMask == SWT.CTRL ){
                	pasteTargetAction.run();
                }
            }
        });

        //fViewer.setLabelProvider(new MakeLabelProvider());
        treeViewer.setSorter(new ViewerSorter() {
            @Override
            public int category(Object element) {
                if (element instanceof IResource) {
                    return 0;
                }
                return 1;
            }
        });

        onExpansion = true; // disable storage for expansion
        treeViewer.setInput( TinyOSPlugin.getDefault().getTargetManager() );
        getSite().setSelectionProvider(treeViewer);

        makeActions();
        hookContextMenu();
        contributeToActionBars();
        
        for( IProject project : TinyOSPlugin.getDefault().getTargetManager().getTargetBuilderProjects() ){
        	checkExpand( project );
        }
        onExpansion = false;
    }

    private void checkExpand( IProject project ){
    	try{
    		onExpansion = true;
    		
    		if( project.isAccessible() ){
    			if( project.hasNature( TinyOSCore.NATURE_ID )){
    				IProjectMakeTargets targets = TinyOSPlugin.getDefault().getTargetManager().getProjectTargets( project );
    				String value = targets.get( EXPANSION_STATE );
    				boolean state = Boolean.parseBoolean( value );
    				treeViewer.setExpandedState( project, state );
    			}
    		}
    		
    	}
    	catch( CoreException ex ){
    		TinyOSPlugin.log( ex );
    	}
    	finally{
    		onExpansion = false;
    	}
    }
    
    private void storeExpanded( Object object, boolean expanded ){
    	if( !onExpansion ){
	    	if( object instanceof IProject ){
	    		IProject project = (IProject)object;
	    		MakeTargetManager manager = TinyOSPlugin.getDefault().getTargetManager();
	    		IProjectMakeTargets targets = manager.getProjectTargets( project );
	    		targets.put( EXPANSION_STATE, String.valueOf( expanded ) );
	    	}
    	}
    }
    
    protected void handleDoubleClick(DoubleClickEvent event) {
        buildTargetAction.run();
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager toolBar) {
        drillDownAdapter.addNavigationActions(toolBar);
        toolBar.add(buildTargetAction);
        toolBar.add(trimEmptyFolderAction);
    }

    private void fillLocalPullDown(IMenuManager menuManager) {

    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                MakeOptionsView.this.fillContextMenu(manager);
                updateActions((IStructuredSelection)treeViewer.getSelection());
            }
        });
        Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(menu);
    }

    protected void fillContextMenu(IMenuManager manager) {
        manager.add(buildTargetAction);
        manager.add(new Separator());
        manager.add(addTargetAction);
        manager.add(editTargetAction);
        manager.add(editProperties);
        manager.add(new Separator());
        manager.add(copyTargetAction);
        manager.add(cutTargetAction);
        manager.add(deleteTargetAction);
        manager.add(pasteTargetAction);
        manager.add(new Separator());
        manager.add(setDefaultTargetAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);

        // Other plug-ins can contribute there actions here
        // manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void makeActions() {
        buildTargetAction = new BuildTargetAction();
        editProperties = new EditProperties(treeViewer.getControl().getShell());
        addTargetAction = new AddTargetAction(treeViewer.getControl().getShell());
        copyTargetAction = new CopyTargetAction( treeViewer.getControl().getDisplay() );
        cutTargetAction = new CutTargetAction( treeViewer.getControl().getDisplay() );
        pasteTargetAction = new PasteTargetAction( treeViewer.getControl().getDisplay() );
        deleteTargetAction = new DeleteTargetAction(treeViewer.getControl().getShell());
        editTargetAction = new EditTargetAction(treeViewer.getControl().getShell());
        setDefaultTargetAction = new SetDefaultTargetAction(treeViewer.getControl().getShell());
        trimEmptyFolderAction = new FilterEmtpyFoldersAction();
    }

    @Override
    public void dispose() {
    	super.dispose();
    	if( cutTargetAction != null )
    		cutTargetAction.dispose();
    	
    	if( copyTargetAction != null )
    		copyTargetAction.dispose();
    	
    	if( pasteTargetAction != null )
    		pasteTargetAction.dispose();
    }
    
    @Override
    public void setFocus() {
        treeViewer.getTree().setFocus();
    }


    protected class FilterEmtpyFoldersAction extends Action {

        private static final String FILTER_EMPTY_FOLDERS = "FilterEmptyFolders"; //$NON-NLS-1$

        public FilterEmtpyFoldersAction() {
            super("Filter Empty Actions", IAction.AS_CHECK_BOX);
            setToolTipText("Filter Empty Projects");
            setChecked(getSettings().getBoolean(FILTER_EMPTY_FOLDERS));
            setImageDescriptor(NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_MAKE_FILTER));
            treeViewer.addFilter(new ViewerFilter() {

                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    //if (isChecked() && element instanceof IFolder) {
                    if (isChecked() && element instanceof IProject) {
                        ITreeContentProvider provider = (ITreeContentProvider) ((ContentViewer)viewer).getContentProvider();
                        Object[] children = provider.getChildren(element);
                        for (int i = 0; i < children.length; i++) {
                            if (select(viewer, element, children[i]))
                                return true;
                        }
                        return false;
                    }
                    return true;
                }
            });
        }

        @Override
        public void run() {
            treeViewer.refresh();
            getSettings().put(FILTER_EMPTY_FOLDERS, isChecked());
        }
    }

    /**
     * Returns setting for this control.
     * 
     * @return Settings.
     */
    private IDialogSettings getSettings() {
        final String sectionName = "TinyOS.view.makeOptions";
        IDialogSettings settings = TinyOSPlugin.getDefault().getDialogSettings().getSection(sectionName);
        if (settings == null) {
            settings = TinyOSPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
        }
        return settings;
    }

    void handleSelectionChanged(SelectionChangedEvent event) {
        IStructuredSelection sel = (IStructuredSelection)event.getSelection();
        //System.out.println(sel);
        updateActions(sel);
    }

    void updateActions(IStructuredSelection sel) {
        addTargetAction.selectionChanged(sel);
        editProperties.selectionChanged(sel);
        buildTargetAction.selectionChanged(sel);
        deleteTargetAction.selectionChanged(sel);
        editTargetAction.selectionChanged(sel);
        setDefaultTargetAction.selectionChanged(sel);
        copyTargetAction.selectionChanged(sel);
        cutTargetAction.selectionChanged(sel);
        pasteTargetAction.selectionChanged(sel);
    }

}
