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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;

public class SearchForPanel{
	private Tag[] toplevelTags;
	private Button[] toplevelSearches;

	private Button simpleSearch;
	private Button advancedSearch;
	private Button openAdvanced;

	private Tag[] advancedTags;
	private AdvancedSearchForPanel.Selection advancedSelection;
	
	private Composite control;

	private boolean onCall = false;

	public SearchForPanel( Tag[] toplevel, Tag[] tags ){
		toplevelTags = toplevel;
		advancedTags = tags;
	}
	
	public String getFilterDescription(){
		if( advancedSearch.getSelection() ){
			return advancedSelection.getDescription();
		}
		else{
			TagSet tags = getSimpleTags();
			StringBuilder builder = new StringBuilder();
			for( Tag tag : tags ){
				if( builder.length() > 0 )
					builder.append( ", " );
				builder.append( tag.getDescription().getName() );
			}
			return builder.toString();
		}
	}
	
	public void writeConfiguration( IDialogSettings settings ){
		settings.put( "simple", simpleSearch.getSelection() );
		
		IDialogSettings selection = settings.addNewSection( "selection" );
		TagSet set = getSimpleTags();
		selection.put( "count", set.size() );
		int index = 0;
		for( Tag tag : set ){
			selection.put( "tag." + index, tag.getId() );
			index++;
		}
		
		if( advancedSelection != null ){
			advancedSelection.write( settings.addNewSection( "advanced" ) );
		}
	}
	
	public void readConfiguration( IDialogSettings settings ){
		if( settings == null )
			return;
		
		if( settings.get( "simple" ) != null ){
			select( settings.getBoolean( "simple" ) );
		}
		
		IDialogSettings selection = settings.getSection( "selection" );
		if( selection != null && selection.get( "count" ) != null ){
			int count = selection.getInt( "count" );
			for( int i = 0; i < count; i++ ){
				String id = selection.get( "tag." + i );
				for( int j = 0; j < toplevelTags.length; j++ ){
					if( toplevelTags[j].getId().equals( id )){
						toplevelSearches[j].setSelection( true );
						break;
					}
				}
			}
		}
		
		IDialogSettings advanced = settings.getSection( "advanced" );
		if( advanced != null ){
			advancedSelection = new AdvancedSearchForPanel.Selection();
			advancedSelection.read( advancedTags, advanced );
		}
	}

	private void openAdvanced(){
		final AdvancedSearchForPanel advanced = new AdvancedSearchForPanel( advancedTags );

		Dialog advancedDialog = new Dialog( control.getShell() ){
			{
				setShellStyle( SWT.RESIZE | SWT.CLOSE | SWT.TITLE );
			}
			@Override
			protected Control createDialogArea( Composite parent ){
				Composite panel = (Composite)super.createDialogArea( parent );
				advanced.createControl( panel );
				advanced.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ));
				advanced.setSelection( advancedSelection );
				
				getShell().setText( "Tags" );
				
				return panel;
			}

			@Override
			protected void okPressed(){
				advancedSelection = advanced.getSelection();
				super.okPressed();
			}
		};

		advancedDialog.open();
	}

	public IASTModelNodeFilter getFilter(){
		if( advancedSearch.getSelection() ){
			if( advancedSelection != null ){
				return advancedSelection.getFilter();
			}
			else{
				return new IASTModelNodeFilter(){
					public boolean include( IASTModelNode node ){
						return false;
					}
				};
			}
		}
		else{
			return new IASTModelNodeFilter(){
				private TagSet search = getSimpleTags(); 
 
				public boolean include( IASTModelNode node ){
					TagSet tags = node.getTags();
					if( tags == null )
						return false;

					return tags.containsOneOf( search );
				}
			};
		}
	}
	
	private TagSet getSimpleTags(){
		TagSet set = new TagSet();
		for( int i = 0; i < toplevelTags.length; i++ ){
			if( toplevelSearches[i].getSelection() ){
				set.add( toplevelTags[i] );
			}
		}
		return set;
	}

	public void createControl( Composite parent ){
		control = new Composite( parent, SWT.NONE );
		control.setLayout( new GridLayout( 3, false ) );

		Control left = createSimpleSearch( control );
		left.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

		Label separator = new Label( control, SWT.SEPARATOR | SWT.VERTICAL );
		separator.setLayoutData( new GridData( SWT.CENTER, SWT.FILL, false, true ) );

		Control right = createAdvancedSearch( control );
		right.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
	}

	public Control getControl(){
		return control;
	}

	private Control createSimpleSearch( Composite parent ){
		Composite panel = new Composite( parent, SWT.NONE );
		panel.setLayout( new GridLayout( 2, true ) );

		toplevelSearches = new Button[ toplevelTags.length ];
		for( int i = 0; i < toplevelTags.length; i++ ){
			Tag tag = toplevelTags[i];

			Button button = new Button( panel, SWT.CHECK );
			button.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
			button.setText( tag.getDescription().getName() );
			button.setToolTipText( tag.getDescription().getDescription() );

			toplevelSearches[i] = button;
		}

		return panel;
	}

	private Control createAdvancedSearch( Composite parent ){
		Composite panel = new Composite( parent, SWT.NONE );
		panel.setLayout( new GridLayout( 1, false ) );

		simpleSearch = new Button( panel, SWT.RADIO );
		simpleSearch.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
		simpleSearch.setSelection( true );
		simpleSearch.setText( "Direct search" );

		advancedSearch = new Button( panel, SWT.RADIO );
		advancedSearch.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
		advancedSearch.setText( "Advanced search" );

		openAdvanced = new Button( panel, SWT.PUSH );
		openAdvanced.setLayoutData( new GridData( SWT.END, SWT.BEGINNING, false, false ) );
		openAdvanced.setEnabled( false );
		openAdvanced.setText( "Choose..." );

		simpleSearch.addSelectionListener( new SelectionListener(){
			public void widgetSelected( SelectionEvent e ){
				if( simpleSearch.getSelection() ){
					select( true );
				}
			}
			public void widgetDefaultSelected( SelectionEvent e ){
				// not called
			}
		});

		advancedSearch.addSelectionListener( new SelectionListener(){
			public void widgetSelected( SelectionEvent e ){
				if( advancedSearch.getSelection() ){
					select( false );
				}
			}
			public void widgetDefaultSelected( SelectionEvent e ){
				// not called
			}
		});

		openAdvanced.addSelectionListener( new SelectionListener(){
			public void widgetSelected( SelectionEvent e ){
				openAdvanced();
			}
			public void widgetDefaultSelected( SelectionEvent e ){
				// not called
			}
		});

		return panel;
	}
	
	private void select( boolean simple ){
		if( !onCall ){
			try{
				onCall = true;
				simpleSearch.setSelection( simple );
				advancedSearch.setSelection( !simple );

				for( Button button : toplevelSearches ){
					button.setEnabled( simple );
				}

				openAdvanced.setEnabled( !simple );
			}
			finally{
				onCall = false;
			}
		}
	}
}
