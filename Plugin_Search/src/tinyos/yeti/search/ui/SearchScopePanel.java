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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.search.model.SearchScope;
import tinyos.yeti.search.model.scope.LibraryScope;

public class SearchScopePanel{
	private Button searchLibrary;
	
	private Composite control;
	
	public SearchScope modify( SearchScope scope ){
		if( searchLibrary.getSelection() ){
			return new LibraryScope( scope );
		}
		return scope;
	}
	
	public void createControl( Composite parent ){
		control = new Composite( parent, SWT.NONE );
		control.setLayout( new GridLayout( 1, false ) );
		
		searchLibrary = new Button( control, SWT.CHECK );
		searchLibrary.setText( "Search entire library" );
		searchLibrary.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
	}
	
	public Control getControl(){
		return control;
	}
	
	public void readConfiguration( IDialogSettings settings ){
		if( settings == null )
			return;
		
		searchLibrary.setSelection( settings.getBoolean( "modify" ) );
	}
	
	public void writeConfiguration( IDialogSettings settings ){
		settings.put( "modify", searchLibrary.getSelection() );
	}
}
