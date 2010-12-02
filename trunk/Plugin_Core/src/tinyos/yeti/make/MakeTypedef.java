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
package tinyos.yeti.make;

public class MakeTypedef{
    private String type;
    private String name;
    
    public MakeTypedef( String type, String name ){
        this.type = type;
        this.name = name;
    }
    
    public String getType(){
        return type;
    }
    
    public String getName(){
        return name;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result
                + ( ( type == null ) ? 0 : type.hashCode() );
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
        final MakeTypedef other = ( MakeTypedef )obj;
        if( name == null ){
            if( other.name != null )
                return false;
        }else if( !name.equals( other.name ) )
            return false;
        if( type == null ){
            if( other.type != null )
                return false;
        }else if( !type.equals( other.type ) )
            return false;
        return true;
    }
}
