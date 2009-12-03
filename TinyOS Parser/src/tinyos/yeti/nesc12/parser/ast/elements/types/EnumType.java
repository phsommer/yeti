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

import java.util.Map;

import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;

public class EnumType extends AbstractType{
    private static final Type REPRESENTATION = BaseType.S_INT;
    
    private String name;
    private String[] constants;
    
    public EnumType( String name, String[] constants ){
        this.name = name;
        this.constants = constants;
    }
    
    public String getName(){
        return name;
    }
    
    @Override
    protected String createId( Map<Type, String> putin, boolean typedefVisible ){
        StringBuilder builder = new StringBuilder();
        builder.append( "e" );
        if( name != null ){
            builder.append( "[" );
            builder.append( name );
            builder.append( "]" );
        }
        
        if( !isIncomplete() ){
            builder.append( "(" );
            if( constants != null ){
                for( int i = 0, n = constants.length; i<n; i++ ){
                    if( i > 0 )
                        builder.append( "," );
                    builder.append( constants[i] );
                }
            }
            builder.append( ")" );
        }
        return builder.toString();
    }

    public Value cast( Value value ){
        return REPRESENTATION.cast( value );
    }

    public int getInitializerLength(){
        return REPRESENTATION.getInitializerLength();
    }

    public boolean isLeafInitializerType() {
        return REPRESENTATION.isLeafInitializerType();
    }
    
    public Type getInitializerType( int index, Initializer initializer ) {
        return REPRESENTATION.getInitializerType( index, initializer );
    }
    
    public Type getInitializerType( int index, Initializer initializer, TypedefType self ){
    	return REPRESENTATION.getInitializerType( index, initializer, self );
    }

    public Value getStaticDefaultValue(){
        return REPRESENTATION.getStaticDefaultValue();
    }

    public boolean isIncomplete(){
        return constants == null;
    }
    
    public String[] getConstants(){
        return constants;
    }

    public Type replace( Map<GenericType, Type> generic ){
        return this;
    }

    public int sizeOf(){
        return REPRESENTATION.sizeOf();
    }

    public String toLabel( String name, Label label ){
        StringBuilder builder = new StringBuilder();
        builder.append( "enum" );
        if( this.name != null ){
            builder.append( " " );
            builder.append( this.name );
        }
        
        if( name != null ){
            builder.append( " " );
            builder.append( name );
        }
        
        return builder.toString();
    }
    
    @Override
    public String toString(){
        return toLabel( null, Label.EXTENDED );
    }

    public void resolveNameRanges(){
        // nothing to do
    }
    
    @Override
    public EnumType asEnumType(){
        return this;
    }
}
