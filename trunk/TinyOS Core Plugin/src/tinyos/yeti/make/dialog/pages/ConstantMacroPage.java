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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import tinyos.yeti.ep.parser.macros.ConstantMacro;
import tinyos.yeti.make.MakeMacro;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;

public class ConstantMacroPage extends KeyValuePage<MakeMacro>{
    private final String TRUE = "visible";
    private final String FALSE = "ignore";
	
    public ConstantMacroPage( boolean showCustomization ){
    	super( showCustomization, "Macros", "Macros that may be visible in any file." );
    }
    
    @Override
    protected void createTableColumns( Table table ){
    	super.createTableColumns( table );
    	
        TableColumn yeti = new TableColumn( table, SWT.LEFT );
        yeti.setText( "Eclipse" );
        yeti.setResizable( true );
        yeti.setMoveable( false );
        yeti.setWidth( 75 );
        
        TableColumn ncc = new TableColumn( table, SWT.LEFT );
        ncc.setText( "ncc" );
        ncc.setResizable( true );
        ncc.setMoveable( false );
        ncc.setWidth( 75 ); 
    }

    @Override
    protected KeyValueDialog<MakeMacro> createDialog( Shell shell ){
    	return new ConstantMacroDialog( shell, this );
    }
    
    
	@Override
	protected String checkValid( String[][] table ){
		String[] names = table[0];
		for( String name : names ){
			int length = name.length();
			if( length == 0 )
				return "Variable without name";
			
			if( !Character.isJavaIdentifierStart( name.charAt( 0 ) ))
				return "Illegal name: '" + name + "'";
			
			for( int i = 1; i < length; i++ ){
				if( !Character.isJavaIdentifierPart( name.charAt( i ) ))
					return "Illegal name: '" + name + "'";
			}
		}
		
		return null;
	}

	@Override
	protected MakeMacro create( String key, String value, TableItem row ){
		ConstantMacro macro = new ConstantMacro( key, value );
		boolean yeti = TRUE.equals( row.getText( 2 ) );
		boolean ncc = TRUE.equals( row.getText( 3 ) );
		return new MakeMacro( macro, yeti, ncc );
	}

	@Override
	protected void show( MakeMacro entry, TableItem item ){
		item.setText( new String[]{ getKey( entry ), getValue( entry ), getBoolean( entry.isIncludeYeti() ), getBoolean( entry.isIncludeNcc() ) } );
	}
	
	private String getBoolean( boolean value ){
		return value ? TRUE : FALSE;
	}
	
	@Override
	public String getDialogExample(){
		return "Example: '#define PI 3.14' would be 'name=PI', 'value=3.14'";
	}

	@Override
	public String getEditDialogTitle(){
		return "Edit macro";
	}

	@Override
	protected String getKey( MakeMacro entry ){
		return entry.getMacro().getName();
	}

	@Override
	protected MakeTargetPropertyKey<MakeMacro[]> getKey(){
		return MakeTargetPropertyKey.MACROS;
	}

	@Override
	public String getKeyName(){
		return "Name";
	}

	@Override
	public String getNewDialogTitle(){
		return "Create new macro";
	}

	@Override
	protected String getValue( MakeMacro entry ){
		return ((ConstantMacro)entry.getMacro()).getConstant();
	}

	@Override
	public String getValueName(){
		return "Value";
	}
    
}
