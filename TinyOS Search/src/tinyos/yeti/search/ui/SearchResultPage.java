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
package tinyos.yeti.search.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.editors.outline.OpenFileAction;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.search.SearchPlugin;
import tinyos.yeti.search.model.ASTReferenceSearchResult;
import tinyos.yeti.search.model.ISearchTreeContentProvider;
import tinyos.yeti.search.model.ITinyOSSearchResult;
import tinyos.yeti.search.model.group.DelegateBinder;
import tinyos.yeti.search.model.group.Group;
import tinyos.yeti.search.model.group.GroupComparator;
import tinyos.yeti.search.model.group.GroupContentProvider;
import tinyos.yeti.search.model.group.GroupGenerator;
import tinyos.yeti.search.model.group.GroupLabelProvider;
import tinyos.yeti.search.util.SearchIcons;
import tinyos.yeti.views.NodeContentProvider;
import tinyos.yeti.views.RootNodeComparator;

public abstract class SearchResultPage<N> implements ISearchResultPage{
	private String pageId;
	
	private final String contextMenuID = "search results";
	
	private String id;
	private IPageSite site;
	
	private TreeViewer viewer;
	
	private OpenFileAction openAction;
	private IAction removeAction;
	private IAction removeAllAction;
	private List<GroupAction<N>> groupActions = new ArrayList<GroupAction<N>>();
	
	private MenuManager contextManager;
	
	private GroupGenerator<?,N> groupGenerator;
	private GroupComparator groupComparator;
	private GroupLabelProvider groupLabelProvider;
	private DelegateBinder binder;
	private ISearchTreeContentProvider provider;
	
	private ITinyOSSearchResult result;
	
	private Listener listener;
	
	private UIJob updateJob = new UIJob( "update search content"){
		{
			setPriority( INTERACTIVE );
			setSystem( true );
		}
		
		@Override
		public IStatus runInUIThread( IProgressMonitor monitor ){
			monitor.beginTask( "update", 1 );
			
			updateContentSynchron();
			
			monitor.done();
			return Status.OK_STATUS;
		}
	};
	
	public abstract void doRemoveAll();
	
	public abstract void doRemoveSelection();
	
	protected abstract boolean hasSelection();
		
	public SearchResultPage( String pageId ){
		this.pageId = pageId;
	}
	
	public String getID(){
		return id;
	}

	public void setID( String id ){
		this.id = id;
	}
	
	public String getLabel(){
		if( result == null )
			return "TinyOS";
		
		return result.getLabel();
	}

	public Object getUIState(){
		return null;
	}

	public void restoreState( IMemento memento ){
		// nothing (yet)
	}

	public void saveState( IMemento memento ){
		// nothing (yet)
	}

	public void setViewPart( ISearchResultViewPart part ){
		// ignore
	}

	public IPageSite getSite(){
		return site;
	}

	public void init( IPageSite site ) throws PartInitException{
		this.site = site;
	}

	public void createControl( Composite parent ){
		groupComparator =  new GroupComparator( null, new RootNodeComparator( NodeContentProvider.PROPERTY_LABEL ) ); 
		groupLabelProvider = new GroupLabelProvider( null, createLabelProvider() );
		
		viewer = new TreeViewer( parent );
		viewer.setComparator( groupComparator );
		viewer.setLabelProvider( groupLabelProvider );
		viewer.addSelectionChangedListener( openAction() );
		viewer.addDoubleClickListener( new IDoubleClickListener(){
			public void doubleClick( DoubleClickEvent event ){
				openAction().run();
			}
		});
		viewer.addSelectionChangedListener( new ISelectionChangedListener(){
			public void selectionChanged( SelectionChangedEvent event ){
				updateRemoveActions();
			}
		});
	}
	
	private void updateRemoveActions(){
		boolean content = false;
		IContentProvider provider = viewer.getContentProvider();
		
		if( provider instanceof ITreeContentProvider ){
			Object[] elements = ((ITreeContentProvider)provider).getElements( viewer.getInput() );
			content = elements != null && elements.length > 0;
		}
		
		boolean selected = hasSelection();
		
		removeAction().setEnabled( content && selected );
		removeAllAction().setEnabled( content );
	}
	
	protected void addGroup( String id, String name, ImageDescriptor image, GroupGenerator<?, N> generator ){
		groupActions.add( new GroupAction<N>( id, name, image, this, generator ));
	}
	
	public void dispose(){
		viewer.getControl().dispose();
	}

	public Control getControl(){
		return viewer.getControl();
	}

	public void setActionBars( IActionBars actionBars ){
		contributeToolbar( actionBars );
		contributeContextMenu();
	}

	private void contributeToolbar( IActionBars actionBars ){
		IToolBarManager manager = actionBars.getToolBarManager();
		
		manager.prependToGroup( IContextMenuConstants.GROUP_REMOVE_MATCHES, removeAllAction() );
		manager.prependToGroup( IContextMenuConstants.GROUP_REMOVE_MATCHES, removeAction() );
		
		for( IAction action : groupActions ){
			actionBars.getMenuManager().appendToGroup( IContextMenuConstants.GROUP_REORGANIZE, action );
		}
		
		/*Action reorganize = new Action( "Reorganize", IAction.AS_DROP_DOWN_MENU ){};
		reorganize.setMenuCreator( new IMenuCreator(){
			private Menu menu;
			
			public Menu getMenu( Control parent ){
				menu = new Menu( parent );
				fill();
				return menu;
			}
			
			public Menu getMenu( Menu parent ){
				menu = new Menu( parent );
				fill();
				return menu;
			}
			
			private void fill(){
				int index = 0;
				for( IAction action : groupActions ){
					ActionContributionItem item = new ActionContributionItem( action );
					item.fill( menu, index++ );
				}
			}
			
			public void dispose(){
				if( menu != null ){
					menu.dispose();
					menu = null;
				}
			}
		});
		
		manager.appendToGroup( IContextMenuConstants.GROUP_REORGANIZE, reorganize );*/
		
		String group = SearchPlugin.getDefault().getPluginPreferences().getString( pageId );
		if( group == null )
			group = "null";
		
		for( GroupAction<N> action : groupActions ){
			if( action.getGroupId().equals( group )){
				setGroup( action.getGenerator(), true );
				break;
			}
		}
	}

	private void contributeContextMenu(){
        contextManager = new MenuManager( contextMenuID, contextMenuID );
        contextManager.setRemoveAllWhenShown(false);

        Menu menu = contextManager.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        
        getSite().registerContextMenu( contextMenuID, contextManager, viewer );


        contextManager.add(			
                openAction
        );
        
        contextManager.add(new Separator());
	}

	
	public void setFocus(){
		viewer.getControl().setFocus();
	}

	public void setInput( ISearchResult search, Object uiState ){
		result = (ITinyOSSearchResult)search;
		updateContent();
	}
	
	public ISearchResult getResult(){
		return result;
	}
	
	public TreeViewer getViewer(){
		return viewer;
	}
	
	public DelegateBinder getBinder(){
		return binder;
	}
	
	public ITreeContentProvider getProvider(){
		return provider;
	}
	
	public void setGroup( GroupGenerator<?,N> group, boolean force ){
		if( this.groupGenerator != group || force ){
			GroupGenerator<?,?> old = this.groupGenerator;
			this.groupGenerator = group;
			updateContentSynchron();
			
			if( old != null ){
				old.clear();
			}
			
			for( GroupAction<N> action : groupActions ){
				if( action.setState( group ) ){
					SearchPlugin.getDefault().getPluginPreferences().setValue( pageId, action.getGroupId() );
				}
			}
		}
	}
	
	private void updateContent(){
		updateJob.schedule();
	}
	
	/**
	 * Creates the basic content provider
	 * @param result the current result, might be <code>null</code>
	 * @param generator the generator that will create groups, might be <code>null</code>
	 * @return a new content provider
	 */
	protected abstract ISearchTreeContentProvider createContentProvider( GroupGenerator<?, N> generator );
	
	/**
	 * Creates the label provider for nodes.
	 * @return the new label provider
	 */
	protected abstract ILabelProvider createLabelProvider();
	
	/**
	 * Creates the empty input for the content provider of {@link #createContentProvider()}.
	 * @return the input
	 */
	protected abstract Object createInput();
	
	/**
	 * Creates the {@link DelegateBinder} which will mediate between <code>provider</code>
	 * and <code>generator</code>
	 * @param provider
	 * @param generator the {@link GroupGenerator} to fill, might be <code>null</code>
	 * @return the new delegate binder
	 */
	protected abstract DelegateBinder createBinder( ITreeContentProvider provider, GroupGenerator<?, N> generator );
	
	private void updateContentSynchron(){
		if( listener != null ){
			listener.dispose();
			listener = null;
		}
		
		provider = createContentProvider( groupGenerator );
		
		if( groupGenerator == null ){
			DelegateBinder binder = createBinder( provider, null );
			setContent( binder, provider, createInput() );
		}
		else{
			groupGenerator.clear();
			groupGenerator.setViewer( viewer );
			
			DelegateBinder binder = createBinder( provider, groupGenerator );
			
			setContent( 
					binder, 
					new GroupContentProvider( binder, provider ),
					groupGenerator.getRoot() );
		}
		
		if( result != null ){
			listener = new Listener( provider, result );
			result.connect( provider, listener );
		}
		
		updateRemoveActions();
	}
	
	private void setContent( DelegateBinder binder, IContentProvider provider, Object input ){
		this.binder = binder;
		groupComparator.setBinder( binder );
		groupLabelProvider.setBinder( binder );
		viewer.setContentProvider( provider );
		viewer.setInput( input );
	}
	
	private class Listener implements ISearchResultListener{
		private ISearchTreeContentProvider provider;
		private ITinyOSSearchResult result;
		
		public Listener( ISearchTreeContentProvider provider, ITinyOSSearchResult result ){
			this.provider = provider;
			this.result = result;
		}
		
		public void dispose(){
			result.removeListener( this );
		}
		
		public void searchResultChanged( SearchResultEvent e ){
			if( provider == null || !provider.handle( e )){
				updateContent();
			}
		}
	}
	
	private OpenFileAction openAction(){
		if( openAction == null ){
			openAction = new OpenFileAction(){
				@Override
				protected IParseFile getFile( Object selection ){
					if( selection instanceof Group.Wrapper )
						selection = ((Group.Wrapper)selection).getNode();
					
					if( selection instanceof Group )
						selection = ((Group)selection).getLocation();
					
					if( selection instanceof IParseFile )
						return (IParseFile)selection;
					
					return super.getFile( selection );
				}
				
				@Override
				protected IFileRegion getRegion( Object selection ){
					if( selection instanceof Group.Wrapper )
						selection = ((Group.Wrapper)selection).getNode();
					
					if( selection instanceof Group ){
						Group group = (Group)selection;
						IFileRegion result = getRegion( group.getLocation() );
						if( result != null ){
							return result;
						}
					}
					else{
						selection = binder.groupChildToContentNode( selection );
					}
					
					if( selection instanceof IFileRegion )
						return (IFileRegion)selection;
					
					if( selection instanceof ASTReferenceSearchResult.Result ){
						return ((ASTReferenceSearchResult.Result)selection).getReference().getSource();
					}
					
					return super.getRegion( selection );
				}
			};
		}
		return openAction;
	}
	
	private IAction removeAction(){
		if( removeAction != null )
			return removeAction;
		
		removeAction = new Action( "Remove Selected Matches", SearchIcons.icons().getImageDescriptor( SearchIcons.REMOVE_SELECTION ) ){
			@Override
			public void run(){
				doRemoveSelection();
			}
		};
		
		return removeAction;
	}
	
	private IAction removeAllAction(){
		if( removeAllAction != null )
			return removeAllAction;
		
		removeAllAction = new Action( "Remove All Matches", SearchIcons.icons().getImageDescriptor( SearchIcons.REMOVE_ALL ) ){
			@Override
			public void run(){
				try{
					result.removeListener( listener );
					doRemoveAll();
				}
				finally{
					result.addListener( listener );
				}
				updateContentSynchron();
			}
		};
		
		return removeAllAction;
	}

}
