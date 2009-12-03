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

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.parser.ASTNodeFilterFactory;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagDescription;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.search.SearchPlugin;
import tinyos.yeti.search.SearchPluginCore;
import tinyos.yeti.search.model.SearchScope;
import tinyos.yeti.search.model.scope.EnclosingProjectScope;
import tinyos.yeti.search.model.scope.SelectionScope;
import tinyos.yeti.search.model.scope.WorkingSetScope;
import tinyos.yeti.search.model.scope.WorkspaceScope;

public class SearchPage extends DialogPage implements ISearchPage{
	private ISearchPageContainer container;
	
	private SearchPatternPanel pattern;
	private SearchForPanel searchFor;
	private SearchScopePanel searchScope;
	private SearchKindPanel searchKind;
	
	public SearchPage(){
		TagSet set = TinyOSPlugin.getDefault().getParserFactory().getSupportedTags();
		
		TagSet searchable = new TagSet();
		TagSet toplevel = new TagSet();
		
		for( Tag tag : set ){
			TagDescription description = tag.getDescription();
			
			if( description != null ){
				searchable.add( tag );
				if( description.isToplevelSearch() ){
					toplevel.add( tag );
				}
			}
		}
		
		searchFor = new SearchForPanel( toplevel.toArray(), searchable.toArray() );
		
	}
	
	public boolean performAction(){
		NewSearchUI.runQueryInBackground( createQuery() );
		return true;
	}
	
	@Override
	public void dispose(){
		writeConfiguration();
		super.dispose();
	}
	
	private ISearchQuery createQuery(){
		IASTModelNodeFilter searchForFilter = searchFor.getFilter();
		
		IASTModelNodeFilter textFilter = pattern.getFilter();

		IASTModelNodeFilter filter = ASTNodeFilterFactory.and( searchForFilter, textFilter );
		
		SearchScope scope = createSearchScope();
		scope = searchScope.modify( scope );
		
		String name = "'" + pattern.getText() + "' - in '" + scope.getDescription() + "' search for: '" + searchFor.getFilterDescription() + "'";
		
		return searchKind.getQuery( name, scope, filter );
	}

	private SearchScope createSearchScope(){
		switch( container.getSelectedScope() ){
			case ISearchPageContainer.WORKSPACE_SCOPE:
				return new WorkspaceScope();
			case ISearchPageContainer.SELECTION_SCOPE:
				return new SelectionScope( container.getSelection() );
			case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
				return new EnclosingProjectScope( container.getSelectedProjectNames() );
			case ISearchPageContainer.WORKING_SET_SCOPE:
				return new WorkingSetScope( container.getSelectedWorkingSets() );
			default:
				return new WorkspaceScope();
		}
	}
	
	public void setContainer( ISearchPageContainer container ){
		this.container = container;
	}

	public void createControl( Composite parent ){
		Composite panel = new Composite( parent, SWT.NONE );
		panel.setLayout( new GridLayout( 2, false ) );
		
		Group patternParent = new Group( panel, SWT.NONE );
		patternParent.setText( "Search string" );
		patternParent.setLayout( new FillLayout() );
		pattern = new SearchPatternPanel();
		pattern.createControl( patternParent );
		patternParent.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

		Group searchForParent = new Group( panel, SWT.NONE	 );
		searchForParent.setLayout( new FillLayout() );
		searchForParent.setText( "Search for" );
		searchFor.createControl( searchForParent );
		searchForParent.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 2 ) );
		
		Group scopeParent = new Group( panel, SWT.NONE );
		scopeParent.setText( "Include" );
		scopeParent.setLayout( new FillLayout() );
		searchScope = new SearchScopePanel();
		searchScope.createControl( scopeParent );
		scopeParent.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1 ));
		
		Group kindParent = new Group( panel, SWT.NONE );
		kindParent.setText( "Limit to" );
		kindParent.setLayout( new FillLayout() );
		searchKind = new SearchKindPanel();
		searchKind.createControl( kindParent );
		kindParent.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
		
		setControl( panel );
		
		readConfiguration();
	}
	
	private IDialogSettings getDialogSettings(){
		IDialogSettings settings = SearchPlugin.getDefault().getDialogSettings();
		IDialogSettings self = settings.getSection( SearchPluginCore.SEARCH_PAGE_DIALOG_ID );
		if( self == null ){
			self = settings.addNewSection( SearchPluginCore.SEARCH_PAGE_DIALOG_ID );
		}
		return self;
	}
	
	private void readConfiguration(){
		IDialogSettings settings = getDialogSettings();
		if( settings.get( "scope" ) != null ){
			container.setSelectedScope( settings.getInt( "scope" ) );
		}
		
		pattern.readConfiguration( settings.getSection( "pattern" ) );
		searchFor.readConfiguration( settings.getSection( "target" ) );
		searchScope.readConfiguration( settings.getSection( "modify" ) );
		searchKind.readConfiguration( settings.getSection( "kind" ) );
	}
	
	private void writeConfiguration(){
		IDialogSettings settings = getDialogSettings();
		settings.put( "scope", container.getSelectedScope() );
		
		pattern.writeConfiguration( settings.addNewSection( "pattern" ) );
		searchFor.writeConfiguration( settings.addNewSection( "target" ) );
		searchScope.writeConfiguration( settings.addNewSection( "modify" ) );
		searchKind.writeConfiguration( settings.addNewSection( "kind" ) );
	}
}
