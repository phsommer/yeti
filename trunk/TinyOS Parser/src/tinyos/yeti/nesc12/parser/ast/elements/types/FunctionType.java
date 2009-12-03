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

import tinyos.yeti.nesc12.parser.ast.ASTMessageHandler;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionMap;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class FunctionType extends AbstractType{
    private Type result;
    private Type[] arguments;
    private boolean vararg;
    
    public FunctionType( Type result, Type[] arguments ){
        this( result, arguments, false );
    }
    
    public FunctionType( Type result, Type[] arguments, boolean vararg ){
        this.result = result;
        this.arguments = arguments;
        this.vararg = vararg;
    }
    
    public boolean isIncomplete(){
        return false;
    }
    
    @Override
    protected String createId( Map<Type, String> putin, boolean typedefVisible ){
        StringBuilder builder = new StringBuilder();
        builder.append( "f" );
        builder.append( "{" );
        if( result != null )
            builder.append( result.id( putin, typedefVisible ) );
        builder.append( "(" );
        if( arguments != null ){
            for( int i = 0, n = arguments.length; i<n; i++ ){
                if( i > 0 )
                    builder.append( "," );
                if( arguments[i] != null )
                    builder.append( arguments[i].id( putin, typedefVisible ) );
            }
        }
        if( vararg )
            builder.append( "..." );
        builder.append( ")}" );
        return builder.toString();
    }
    
    public void resolveNameRanges(){
        if( result != null )
            result.resolveNameRanges();
        
        if( arguments != null ){
            for( Type type : arguments ){
                if( type != null ){
                    type.resolveNameRanges();
                }
            }
        }
    }
    
    public Type replace( Map<GenericType, Type> generic ) {
        Type resultCheck = result == null ? null : result.replace( generic );
        Type[] argumentCheck = new Type[ arguments.length ];
        for( int i = 0; i < arguments.length; i++ ){
            if( arguments[i] != null )
                argumentCheck[i] = arguments[i].replace( generic );
        }
        
        boolean change = resultCheck != result;
        for( int i = 0, n = arguments.length; !change && i<n; i++ ){
            change = arguments[i] != argumentCheck[i];
        }
        
        if( !change )
            return this;
        
        return new FunctionType( resultCheck, argumentCheck, vararg );
    }
    
    public Type getResult() {
        return result;
    }

    public int getArgumentCount(){
        return arguments.length;
    }
    
    public Type getArgument( int index ){
        return arguments[ index ];
    }
    
    public boolean isVararg() {
        return vararg;
    }
    
    @Override
    public FunctionType asFunctionType(){
        return this;
    }
    
    public Value cast( Value value ) {
        return null;
    }
    
    public void checkConversion( ConversionMap map, Type valueType, ASTMessageHandler handler, ASTNode location ) {
        handler.report( map.getErrorSeverity(), "can't assign a value to a function", null, location );
    }
    
    public int getInitializerLength() {
        return 0;
    }
    
    public boolean isLeafInitializerType() {
        return true;
    }
    
    public Type getInitializerType( int index, Initializer initializer ) {
        if( index == 0 )
            return this;
        
        return null;
    }
    
    public Type getInitializerType( int index, Initializer initializer, TypedefType self ){
    	if( index == 0 )
    		return self == null ? this : self;
    	
    	return null;
    }
    
    public Value getStaticDefaultValue() {
        return null;
    }
    
    public int sizeOf() {
        return -1;
    }
    
    @Override
    public int getSegmentCount(){
        return 2;
    }
    
    @Override
    public int getSegmentSize( int segment ){
        switch( segment ){
            case 0: return 1;
            case 1: return getArgumentCount();
            default: return 0;
        }
    }
    
    @Override
    public String getSegmentName( int segment ){
        switch( segment ){
            case 0: return "result";
            case 1: return "argument";
            default: return null;
        }
    }
    
    @Override
    public Binding getSegmentChild( int segment, int index ){
        switch( segment ){
            case 0: return getResult();
            case 1: return getArgument( index );
            default: return null;
        }
    }
    
    public String toLabel( String name, Label label ){
        StringBuilder builder = new StringBuilder();
        
        if( name != null )
            builder.append( name );
        
        builder.append( "(" );
        for( int i = 0, n = getArgumentCount(); i<n; i++ ){
            if( i > 0 )
                builder.append( "," );
            if( getArgument( i ) == null )
                builder.append( "?" );
            else
                builder.append( getArgument( i ).toLabel( null, label ) );
        }
        builder.append( ")" );
        if( result != null ){
            builder.append( " - " );
            builder.append( result.toLabel( null, label ) );
        }
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return toLabel( null, Label.EXTENDED );
    }
    
    @Override
    public boolean equals( Object obj ) {
        if( this == obj )
            return true;
        if( !(obj instanceof Type ))
            return false;
        return ConversionTable.instance().equals( this, (Type)obj );
    }
}
