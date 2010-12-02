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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.make.MakeTypedef;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;

public class TypedefPage extends KeyValuePage<MakeTypedef>{
    public TypedefPage( boolean showCustomization ){
        super( showCustomization, "Typedefs", "Types which are used in any file of this plugin, but are not forwarded to 'ncc'" );
    }
    
    @Override
    public String checkValid( String[][] table ){
    	String[] keys = table[0];
    	String[] values = table[1];
    	
    	INesCParserFactory factory = TinyOSPlugin.getDefault().getParserFactory();
    	
    	List<IDeclaration> declarations = new ArrayList<IDeclaration>();
    	for( int i = 0; i < keys.length; i++ ){
    		try{
    			IDeclaration next = factory.toBasicType( values[i], keys[i], declarations.toArray( new IDeclaration[ declarations.size() ] ));
    			declarations.add( next );
    		}
    		catch( IllegalArgumentException ex ){
    			return "Unable to parse '" + values[i] + "'";
    		}
    	}
    	
    	return null;
    }
    
    @Override
    protected KeyValueDialog<MakeTypedef> createDialog( Shell shell ){
    	return new KeyValueDialog<MakeTypedef>( shell, this ){
			@Override
			protected MakeTypedef create( String key, String value ){
				return new MakeTypedef( value, key );
			}
			@Override
			protected boolean checkOk( String key, String value ){
				return key.length() > 0 && value.length() > 0;
			}
		};
    }
        
    @Override
    public String getDialogExample(){
	    return "Example: 'typedef int x[47]' becomes 'name = x', 'type = int[47]'"; 
    }
    
    @Override
    public String getEditDialogTitle(){
	    return "Edit typedef";
    }
    
    @Override
    public String getKeyName(){
    	return "Name";
    }
    
    @Override
    public String getNewDialogTitle(){
    	return "Create new typedef";
    }
    
    @Override
    public String getValueName(){
    	return "Type";
    }
    
    @Override
    protected MakeTargetPropertyKey<MakeTypedef[]> getKey(){
    	return MakeTargetPropertyKey.TYPEDEFS;
    }
    
    @Override
    protected String getKey( MakeTypedef entry ){
    	return entry.getName();
    }
    
    @Override
    protected String getValue( MakeTypedef entry ){
	    return entry.getType();
    }
    
    @Override
    protected MakeTypedef create( String key, String value, TableItem item ){
    	return new MakeTypedef( value, key );
    }
}
