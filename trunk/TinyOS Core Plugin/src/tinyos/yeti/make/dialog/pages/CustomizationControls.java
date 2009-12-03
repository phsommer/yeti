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
package tinyos.yeti.make.dialog.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Some controls allowing the user to select whether the final properties
 * should be calculated from "custom", "default" or "default and custom"
 * properties.
 * @author Benjamin Sigg
 */
public class CustomizationControls{
	public static enum Selection{
		DEFAULT( false, true ),
		CUSTOM( true, false ), 
		DEFAULT_AND_CUSTOM( true, true );
		
		private Selection( boolean local, boolean defaults ){
			this.local = local;
			this.defaults = defaults;
		}
		
		private boolean local;
		private boolean defaults;
		
		public boolean isLocal(){
			return local;
		}
		
		public boolean isDefaults(){
			return defaults;
		}
	}
	
	private ICustomizeablePage page;
	
	private Combo combo;
	
	private Composite control;
	
	private boolean onCall = false;
	
	private Selection selection;
	
	public Control createControl( Composite parent, boolean allowDefaultAndCustom ){
		control = new Composite( parent, SWT.NONE );
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		control.setLayout( layout );
	
		Label label = new Label( control, SWT.NONE );
		label.setText( "Settings:" );
		label.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
		
		combo = new Combo( control, SWT.DROP_DOWN | SWT.READ_ONLY );
		combo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
		
		combo.add( "-" );
		combo.add( "Custom settings only" );
		if( allowDefaultAndCustom ){
			combo.add( "Union of default and custom settings" );
		}
		
		setDefaultValue( null );
		
		combo.addSelectionListener( new SelectionListener(){
			public void widgetDefaultSelected( SelectionEvent e ){
				update();
			}
			public void widgetSelected( SelectionEvent e ){
				update();
			}
		});
		
		return control;
	}
	
	private void update(){
		if( !onCall ){
			try{
				onCall = true;
				
				int index = combo.getSelectionIndex();
			
				Selection next = null;
				switch( index ){
					case 0:
						next = Selection.DEFAULT;
						break;
					case 1:
						next = Selection.CUSTOM;
						break;
					case 2:
						next = Selection.DEFAULT_AND_CUSTOM;
						break;
				}
				
				if( selection != next ){
					selection = next;
					
					if( page != null ){
						page.setCustomEnabled( selection == Selection.CUSTOM || selection == Selection.DEFAULT_AND_CUSTOM );
					}
				}
			}
			finally{
				onCall = false;
			}
		}
	}
	
	public void setSelection( boolean local, boolean defaults ){
		if( local && defaults )
			setSelection( Selection.DEFAULT_AND_CUSTOM );
		else if( local )
			setSelection( Selection.CUSTOM );
		else
			setSelection( Selection.DEFAULT );
	}

	public void setSelection( Selection selection ){
		try{
			onCall = true;
			int index = 0;
			
			switch( selection ){
				case DEFAULT:
					index = 0;
					break;
				case CUSTOM:
					index = 1;
					break;
				case DEFAULT_AND_CUSTOM:
					index = 2;
					break;
			}
			
			combo.select( index );
			
			this.selection = selection;
			
			if( page != null ){
				page.setCustomEnabled( selection == Selection.CUSTOM || selection == Selection.DEFAULT_AND_CUSTOM );
			}
		}
		finally{
			onCall = false;
		}
	}
	
	public Selection getSelection(){
		return selection;
	}
	
	public Control getControl(){
		return control;
	}
	
	public void setPage( ICustomizeablePage page ){
		this.page = page;
	}
	
	public void setDefaultValue( String value ){
		boolean call = onCall;
		
		try{
			onCall = true;
			
			String next;
			
			if( value == null || value.length() == 0 ){
				next = "Default settings only";
			}
			else{
				next = "Default settings only: " + value;
			}
			
			int selection = combo.getSelectionIndex();
			
			combo.remove( 0 );
			combo.add( next, 0 );
			
			combo.select( selection );
			
			control.layout();
		}
		finally{
			onCall = call;
		}
	}
}
