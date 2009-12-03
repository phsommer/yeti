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
import java.util.List;

import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;


public class SimpleName extends Name{
    public static final IGenericFactory<SimpleName> FACTORY = new ReferenceFactory<SimpleName>( Name.FACTORY ){
        public SimpleName create(){
            return new SimpleName();
        }
        
        @Override
        public void write( SimpleName value, IStorage storage ) throws IOException{
            super.write( value, storage );
            storage.writeString( value.name );
        }
        
        @Override
        public SimpleName read( SimpleName value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.name = storage.readString();
            return value;
        }
    };
    
    private String name;
    
    private SimpleName(){
        // nothing
    }
    
    public SimpleName( LazyRangeDescription range, String name ){
        super( range );
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
    @Override
    public String toIdentifier(){
        return name;
    }
    
    @Override
    public String[] segments(){
        return new String[]{ name };
    }
    
    @Override
    protected void segments( List<String> list ){
        list.add( name );
    }

    @Override
    public int hashCode(){
        return name == null ? 0 : name.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        final SimpleName other = ( SimpleName )obj;
        if( name == null ){
            if( other.name != null )
                return false;
        }else if( !name.equals( other.name ) )
            return false;
        return true;
    }
    
    
}
