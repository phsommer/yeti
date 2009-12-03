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
package tinyos.yeti.nesc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelNode;

public class ComponentASTModelNode extends ASTModelNode{
    private Map<String, String> uses = new HashMap<String, String>();
    private Map<String, String> provides = new HashMap<String, String>();
    
    public ComponentASTModelNode( IASTModelNode parent, String identifier, String name, String label, IParseFile file, IFileRegion[] origin, Tag... tags ){
        super( parent, identifier, name, label, file, origin, tags );
    }

    public ComponentASTModelNode( IASTModelNode parent, String identifier, String name, String label, IParseFile file, IFileRegion[] origin, TagSet tags ){
        super( parent, identifier, name, label, file, origin, tags );
    }
    
    public void addUses( String name, String renamed ){
        uses.put( renamed, name );
    }
    public void addProvides( String name, String renamed ){
        provides.put( renamed, name );
    }
    
    public String getUses( String renamed ){
        return uses.get( renamed );
    }
    public String getProvides( String renamed ){
        return provides.get( renamed );
    }
    
    public String[] getUsesProvides(){
        String[] result = new String[ uses.size() + provides.size() ];
        int index = 0;
        
        for( String name : uses.keySet() )
            result[ index++ ] = name;
        
        for( String name : provides.keySet() )
            result[ index++ ] = name;
        
        return result;
    }
    
    public String[] getRenamedUsesProvides( String name ){
        List<String> result = new ArrayList<String>();
        
        for( int i = 0; i < 2; i++ ){
            Map<String,String> map = null;
            switch( i ){
                case 0: 
                    map = uses;
                    break;
                case 1:
                    map = provides;
                    break;
            }
            
            for( Map.Entry<String, String> entry : map.entrySet() ){
                if( name.equals( entry.getValue() ))
                    result.add( entry.getKey() );
            }
        }
        
        return result.toArray( new String[ result.size() ] );
    }
    
    public String get( String renamed ){
        String check = getUses( renamed );
        if( check != null )
            return check;
        
        return getProvides( renamed );
    }
}
