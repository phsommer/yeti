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
package tinyos.yeti.nesc12.ep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;

public class StandardBindingResolver implements BindingResolver{
    private Map<Key, Binding> bindings = new HashMap<Key, Binding>();
    
    public Binding getBinding( IASTModelPath path, String identifier, Map<GenericType, Type> generics ){
        return bindings.get( new Key( path, identifier, generics ) );
    }
    
    public Binding getBinding( IASTModelPath path, String identifier ){
        return getBinding( path, identifier, null );
    }
    
    public void putBinding( IASTModelPath path, String identifier, Map<GenericType, Type> generics, Binding binding ){
        bindings.put( new Key( path, identifier, generics ), binding );
    }
    
    public void putBinding( IASTModelPath path, String identifier, Binding binding ){
        putBinding( path, identifier, null, binding );
    }
    
    public ICancellationMonitor getCancellationMonitor(){
        return ICancellationMonitor.NULL;
    }
    
    private static class Key{
        private IASTModelPath path;
        private String identifier;
        private String generics;
        
        public Key( IASTModelPath path, String identifier, Map<GenericType, Type> generics ){
            this.path = path;
            this.identifier = identifier;
            this.generics = toString( generics );
        }
        
        @Override
        public boolean equals( Object obj ) {
            Key other = (Key)obj;
            return identifier.equals( other.identifier ) && path.equals( other.path ) && generics.equals( other.generics );
        }
        
        @Override
        public int hashCode() {
            int p = path.hashCode();
            p = (p << 8 | p >>> 24) ^ identifier.hashCode();
            p = (p << 8 | p >>> 24) ^ generics.hashCode();
            return p;
        }
        
        private String toString( Map<GenericType, Type> generics ){
            if( generics == null )
                return "";
            
            List<Map.Entry<GenericType, Type>> list = new ArrayList<Map.Entry<GenericType,Type>>( generics.entrySet() );
            Collections.sort( list, new Comparator<Map.Entry<GenericType, Type>>(){
                public int compare( Entry<GenericType, Type> o1, Entry<GenericType, Type> o2 ){
                    return o1.getKey().getName().compareTo( o2.getKey().getName() );
                }
            });
            StringBuilder builder = new StringBuilder();
            for( Map.Entry<GenericType, Type> entry : list ){
                builder.append( entry.getKey().getName() );
                builder.append( "=" );
                builder.append( entry.getValue().id( false ) );
                builder.append( ";" );
            }
            return builder.toString();
        }
    }
}
