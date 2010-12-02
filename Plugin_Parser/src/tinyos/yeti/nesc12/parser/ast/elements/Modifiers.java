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
package tinyos.yeti.nesc12.parser.ast.elements;

import java.io.IOException;

import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

public class Modifiers {
    public static final IGenericFactory<Modifiers> FACTORY = new IGenericFactory<Modifiers>(){
        public Modifiers create(){
            return new Modifiers();
        }
        
        public void write( Modifiers value, IStorage storage ) throws IOException{
            storage.out().writeInt( value.value );
        }
        
        public Modifiers read( Modifiers value, IStorage storage ) throws IOException{
            value.value = storage.in().readInt();
            return value;
        }
    };
    
    public static final int TYPEDEF    = 1;
    
    public static final int STATIC     = 1 << 1;
    public static final int AUTO       = 1 << 2;
    public static final int REGISTER   = 1 << 3;
    public static final int EXTERN     = 1 << 4;
    
    public static final int CONST      = 1 << 5;
    public static final int RESTRICT   = 1 << 6;
    public static final int VOLATILE   = 1 << 7;
        
    public static final int INLINE     = 1 << 8;
    public static final int DEFAULT    = 1 << 9;
    
    public static final int COMMAND    = 1 << 10;
    public static final int EVENT      = 1 << 11;
    public static final int TASK       = 1 << 12;
    public static final int ASYNC      = 1 << 13;
    public static final int NORACE     = 1 << 14;
    
    public static final int ALL;
    
    public static final int ALL_STORAGE_CLASSES =       AUTO | REGISTER | STATIC | EXTERN | TYPEDEF;
    public static final int ALL_TYPE_QUALIFIER =        CONST | VOLATILE | RESTRICT;
    public static final int ALL_NESC =                  COMMAND | EVENT | TASK | DEFAULT | ASYNC | NORACE;
    public static final int ALL_REMAINING =             INLINE;
    
    public static final int[] ALL_MODIFIERS = {
        TYPEDEF,
        STATIC,
        AUTO,
        REGISTER,
        EXTERN,
        CONST,
        RESTRICT,
        VOLATILE,
        INLINE,
        DEFAULT,
        COMMAND,
        EVENT,
        TASK,
        ASYNC,
        NORACE
    };  
    
    
    static{
        int all = 0;
        
        for( int word : ALL_MODIFIERS ){
            all |= word;
        }
        ALL = all;
    }
    
    private int value;
    
    public Modifiers(){
    	// nothing
    }
    
    public Modifiers( int flags ){
    	this.value = flags;
    }
    
    public boolean is( int flags ){
        return (value & flags) == flags;
    }
    
    public boolean isAtLeastOneOf( int flags ){
        return (value & flags) != 0;
    }
    
    public void set( int flags, boolean value ){
        if( value )
            this.value |= flags;
        else
            this.value &= ~flags;
    }
    
    public int getFlags(){
        return value;
    }
    
    public Modifiers subModifier( int flags ){
        if( !isAtLeastOneOf( flags ))
            return null;
        
        if( !isAtLeastOneOf( ~flags ))
            return this;
        
        return new Modifiers( flags & value );
    }
    
    public boolean isTypedef(){  return is( TYPEDEF ); }
    public boolean isStatic(){   return is( STATIC ); }
    public boolean isAuto(){     return is( AUTO ); }
    public boolean isRegister(){ return is( REGISTER ); }
    public boolean isExtern(){   return is( EXTERN ); }
    public boolean isConst(){    return is( CONST ); }
    public boolean isRestrict(){ return is( RESTRICT ); }
    public boolean isVolatile(){ return is( VOLATILE ); }
    public boolean isInline(){   return is( INLINE ); }
    public boolean isDefault(){  return is( DEFAULT ); }
    public boolean isCommand(){  return is( COMMAND ); }
    public boolean isEvent(){    return is( EVENT ); }
    public boolean isTask(){    return is( TASK ); }
    public boolean isAsync(){    return is( ASYNC ); }
    public boolean isNorace(){    return is( NORACE ); }
    
    public void setTypedef( boolean value ){  set( TYPEDEF, value ); }
    public void setStatic( boolean value ){   set( STATIC, value ); }
    public void setAuto( boolean value ){     set( AUTO, value ); }
    public void setRegister( boolean value ){ set( REGISTER, value ); }
    public void setExtern( boolean value ){   set( EXTERN, value ); }
    public void setConst( boolean value ){    set( CONST, value ); }
    public void setRestrict( boolean value ){ set( RESTRICT, value ); }
    public void setVolatile( boolean value ){ set( VOLATILE, value ); }
    public void setInline( boolean value ){   set( INLINE, value ); }
    public void setDefault( boolean value ){  set( DEFAULT, value ); }
    public void setCommand( boolean value ){  set( COMMAND, value ); }
    public void setEvent( boolean value ){    set( EVENT, value ); }
    public void setTask( boolean value ){    set( TASK, value ); }
    public void setAsync( boolean value ){    set( ASYNC, value ); }
    public void setNorace( boolean value ){    set( NORACE, value ); }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if( isTypedef() )
            append( builder, "typedef" );
        if( isInline() )
            append( builder, "inline" );
        if( isStatic() )
            append( builder, "static" );
        if( isAuto() )
            append( builder, "auto" );
        if( isRegister() )
            append( builder, "register" );
        if( isExtern() )
            append( builder, "extern" );
        if( isConst() )
            append( builder, "const" );
        if( isRestrict() )
            append( builder, "restrict" );
        if( isVolatile() )
            append( builder, "volatile" );
        if( isAsync() )
            append( builder, "async" );
        if( isNorace() )
            append( builder, "norace" );
        if( isDefault() )
            append( builder, "default" );
        if( isCommand() )
            append( builder, "command" );
        if( isEvent() )
            append( builder, "event" );
        if( isTask() )
            append( builder, "task" );
        
        
        return builder.toString();
    }
    
    private void append( StringBuilder builder, String word ){
        if( builder.length() > 0 )
            builder.append( " " );
        
        builder.append( word );
    }
    
    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals( Object obj ) {
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        final Modifiers other = (Modifiers)obj;
        if( value != other.value )
            return false;
        return true;
    }
    
    
}
