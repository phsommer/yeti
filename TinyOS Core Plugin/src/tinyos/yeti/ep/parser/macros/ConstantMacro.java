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
package tinyos.yeti.ep.parser.macros;

import tinyos.yeti.ep.parser.IMacro;

/**
 * A macro holding a simple constant
 * @author Benjamin Sigg
 *
 */
public class ConstantMacro implements IMacro{
    private String name;
    private String constant;
    
    public ConstantMacro( String name, String constant ){
        this.name = name;
        this.constant = constant;
    }
    
    public boolean isFunctionMacro() {
        return false;
    }
    
    public int getArgumentCount(){
        return 0;
    }

    public String getName(){
        return name;
    }
    
    public String getConstant(){
        return constant;
    }

    public boolean isVararg(){
        return false;
    }

    public String run( String... arguments ){
        return constant;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ( ( constant == null ) ? 0 : constant.hashCode() );
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ){
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        final ConstantMacro other = ( ConstantMacro )obj;
        if( constant == null ){
            if( other.constant != null )
                return false;
        }else if( !constant.equals( other.constant ) )
            return false;
        if( name == null ){
            if( other.name != null )
                return false;
        }else if( !name.equals( other.name ) )
            return false;
        return true;
    }
    
    
}
