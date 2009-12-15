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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import tinyos.yeti.ep.parser.macros.ConstantMacro;
import tinyos.yeti.make.MakeMacro;

/**
 * A small dialog that allows the user to create or modify {@link ConstantMacro}s.
 * @author Benjamin Sigg
 */
public class ConstantMacroDialog extends KeyValueDialog<MakeMacro>{
	private Button includeYetiButton;
	private boolean includeYeti;
	
	private Button includeNccButton;
	private boolean includeNcc;
	
	public ConstantMacroDialog( Shell shell, ConstantMacroPage page ){
		super( shell, page );
	}
	
	@Override
	public boolean open( String key, String value, MakeMacro item ){
		if( item == null ){
			includeYeti = true;
			includeNcc = false;
		}
		else{
			includeYeti = item.isIncludeYeti();
			includeNcc = item.isIncludeNcc();
		}
		
		if( includeYetiButton != null )
			includeYetiButton.setSelection( includeYeti );
		if( includeNccButton != null )
			includeNccButton.setSelection( includeNcc );
		
		return super.open( key, value, item );
	}
	
	@Override
	protected boolean checkOk( String key, String value ){
		return key.length() > 0;
	}
	
	@Override
	protected void createFields( Composite parent ){
		super.createFields( parent );

        includeYetiButton = new Button( parent, SWT.CHECK );
        includeYetiButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
        includeYetiButton.setText( "Macro visible in all files in Eclipse" );
        includeYetiButton.setSelection( includeYeti );
        includeYetiButton.addSelectionListener( new SelectionListener(){
			public void widgetSelected( SelectionEvent e ){
				includeYeti = includeYetiButton.getSelection();
			}
			public void widgetDefaultSelected( SelectionEvent e ){
				includeYeti = includeYetiButton.getSelection();
			}
		});
        
        includeNccButton = new Button( parent, SWT.CHECK );
        includeNccButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
        includeNccButton.setText( "Macro forwarded to ncc" );
        includeNccButton.setSelection( includeNcc );
        includeNccButton.addSelectionListener( new SelectionListener(){
			public void widgetSelected( SelectionEvent e ){
				includeNcc = includeNccButton.getSelection();
			}
			public void widgetDefaultSelected( SelectionEvent e ){
				includeNcc = includeNccButton.getSelection();
			}
		});
	}
	
	@Override
	protected MakeMacro create( String key, String value ){
		return new MakeMacro( new ConstantMacro( key, value ), includeYeti, includeNcc );
	}
}
