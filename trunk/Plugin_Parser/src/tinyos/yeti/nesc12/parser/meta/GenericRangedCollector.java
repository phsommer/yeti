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
package tinyos.yeti.nesc12.parser.meta;

import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;

/**
 * A {@link RangedCollector} that handles various kind of objects at the same time
 * @author Benjamin Sigg
 */
public class GenericRangedCollector extends RangedCollector<GenericRangedCollector.GenericKey, GenericRangedCollector.GenericValue>{
    public static final int KIND_FIELD = 0;
    public static final int KIND_COMPONENT_REFERENCE = 1;
    public static final int KIND_INTERFACE_REFERENCE = 2;
    public static final int KIND_TYPE_TAG = 3;
    
    public GenericRangedCollector( AnalyzeStack stack ){
        super( stack );
    }
    
    @Override
    protected RangedCollection<GenericValue> createCollection(){
        return new GenericRangedCollection();
    }
    
    public void putField( Field field, int accessible, int top, boolean overriding ){
        Name name = field.getName();
        if( name != null ){
            active( accessible, new GenericKey( KIND_FIELD, name.toIdentifier() ), new GenericValue( KIND_FIELD, field ), top, overriding );
        }
    }
    
    public void putComponentReference( ComponentReferenceModelConnection reference, int accessible, int top ){
        String name = reference.getName();
        if( name != null ){
            active( accessible, new GenericKey( KIND_COMPONENT_REFERENCE, name ), new GenericValue( KIND_COMPONENT_REFERENCE, reference ), top );
        }
    }
    
    public void putInterfaceReference( InterfaceReferenceModelConnection reference, int accessible, int top ){
        Name name = reference.getName();
        if( name != null ){
            active( accessible, new GenericKey( KIND_INTERFACE_REFERENCE, name ), new GenericValue( KIND_INTERFACE_REFERENCE, reference ), top );
        }
    }
    
    public void putTypeTag( Name name, Type type, ModelAttribute[] attributes, int accessible, int top ){
        name.resolveRange();
        active( accessible, new GenericKey( KIND_TYPE_TAG, name ), new GenericValue( KIND_TYPE_TAG, new NamedType( name, type, attributes ) ), top );
    }
    
    @Override
    public GenericRangedCollection close( int outputLocation ){
        return (GenericRangedCollection)super.close( outputLocation );
    }
    
    public static class GenericValue{
        private int kind;
        private Object value;
        
        public GenericValue( int kind, Object value ){
            this.kind = kind;
            this.value = value;
        }
        
        public int getKind(){
            return kind;
        }
        
        public Object getValue(){
            return value;
        }
        
        @Override
        public String toString(){
            return value.toString();
        }
    }
    
    public static class GenericKey{
        private int kind;
        private Object key;
        
        public GenericKey( int kind, Object key ){
            this.kind = kind;
            this.key = key;
        }
        
        @Override
        public int hashCode(){
            if( key == null )
                return kind;
            
            return key.hashCode() + kind;
        }
        @Override
        public boolean equals( Object obj ){
            if( this == obj )
                return true;
            if( obj == null )
                return false;
            
            final GenericKey other = ( GenericKey )obj;

            if( kind != other.kind )
                return false;
            
            if( key == null ){
                if( other.key != null )
                    return false;
            }
            else if( !key.equals( other.key ) )
                return false;
            
            return true;
        }
        
        
    }
}
