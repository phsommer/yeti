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
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.search.model.ASTNodeSearchQuery;
import tinyos.yeti.search.model.ASTReferenceSearchQuery;
import tinyos.yeti.search.model.SearchScope;

public class SearchKindPanel{
	private Button searchDeclarations;
	private Button searchReferences;
	
	private Composite control;
	
	public void createControl( Composite parent ){
		control = new Composite( parent, SWT.NONE );
		control.setLayout( new GridLayout( 1, false ) );
	
		searchDeclarations = new Button( control, SWT.RADIO );
		searchDeclarations.setText( "Declarations" );
		searchDeclarations.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
		
		searchReferences = new Button( control, SWT.RADIO );
		searchReferences.setText( "References" );
		searchReferences.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
		
		searchDeclarations.setSelection( true );
	}
	
	public Control getControl(){
		return control;
	}
	
	public ISearchQuery getQuery( String name, SearchScope scope, IASTModelNodeFilter filter ){
		if( searchReferences.getSelection() )
			return new ASTReferenceSearchQuery( name, scope, filter );
		
		return new ASTNodeSearchQuery( name, scope, filter );
	}
	
	public void readConfiguration( IDialogSettings settings ){
		if( settings == null )
			return;
		
		searchDeclarations.setSelection( settings.getBoolean( "declarations" ) );
		searchReferences.setSelection( settings.getBoolean( "references" ) );
	}
	
	public void writeConfiguration( IDialogSettings settings ){
		settings.put( "declarations", searchDeclarations.getSelection() );
		settings.put( "references", searchReferences.getSelection() );
	}
}
