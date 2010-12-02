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
package tinyos.yeti.nesc12.parser.ast.elements.types;

import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;

/**
 * An abstract class implementing the most common methods of {@link Type}.
 * @author Benjamin Sigg
 */
public abstract class AbstractType implements Type{
    public BaseType asBase(){
        return null;
    }
    
    public DataObjectType asDataObjectType(){
        return null;
    }
    
    public FunctionType asFunctionType(){
        return null;
    }

    public ArrayType asArrayType(){
        return null;
    }
    
    public PointerType asPointerType(){
        return null;
    }
    
    public EnumType asEnumType(){
        return null;
    }
    
    public ConstType asConstType(){
        return null;
    }
    
    public GenericType asGenericType(){
        return null;
    }
    
    public TypedefType asTypedefType() {
    	return null;
    }
    
    public Type asType() {
        return this;
    }

    public Value asValue() {
        return null;
    }

    public String getBindingType() {
        return "Type";
    }

    public String getBindingValue() {
        return toString();
    }

    public Binding getSegmentChild( int segment, int index ) {
        return null;
    }

    public int getSegmentCount() {
        return 0;
    }

    public String getSegmentName( int segment ) {
        return null;
    }

    public int getSegmentSize( int segment ) {
        return 0;
    }
    
    public String id( boolean typedefVisible ){
        return id( new HashMap<Type, String>(), typedefVisible );
    }
    
    public String id( Map<Type, String> putin, boolean typedefVisible ){
        String id = putin.get( this );
        if( id == null ){
            id = createId( putin, typedefVisible );
            putin.put( this, id );
        }
        return id;
    }
    
    protected abstract String createId( Map<Type, String> putin, boolean typedefVisible );
}
