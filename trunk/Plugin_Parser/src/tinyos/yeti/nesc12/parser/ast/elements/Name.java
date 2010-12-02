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
import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.storage.GenericArrayFactory;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.preprocessor.RangeDescription;

/**
 * Represents the name of some object, for example the name of a field
 * "int x = 3;" would be "x".
 * @author Benjamin Sigg
 */
public abstract class Name extends AbstractBinding{
    public static final IGenericFactory<Name> FACTORY = new IGenericFactory<Name>(){
        public Name create(){
            return null;
        }
        
        public void write( Name value, IStorage storage ) throws IOException{
            storage.write( value.range );
        }
        
        public Name read( Name value, IStorage storage ) throws IOException{
            value.range = storage.read();
            return value;
        }
    };
    
    public static final IGenericFactory<Name[]> ARRAY_FACTORY = new GenericArrayFactory<Name>(){
        @Override
        public Name[] create( int size ){
            return new Name[ size ];
        }
    };
    
    public static String toIdentifier( Name name ){
    	if( name == null )
    		return null;
    	return name.toIdentifier();
    }
    
    public static String[] segments( Name name ){
    	if( name == null )
    		return null;
    	return name.segments();
    }
	
    private LazyRangeDescription range;
    
    protected Name(){
        super( "Name" );
    }
    
    /**
     * Creates a new name
     * @param range the range in which this name lies
     */
    public Name( LazyRangeDescription range ){
        super( "Name" );
        this.range = range;
    }
    
    public void resolveRange(){
        if( range != null )
            range.resolve();
    }
    
    /**
     * Gets the range in which this name lies.
     * @return the range
     */
    public RangeDescription getRange(){
        if( range == null )
            return null;
        return range.getRange();
    }
    
    public String[] segments(){
        List<String> list = new ArrayList<String>();
        segments( list );
        return list.toArray( new String[ list.size() ] );
    }
    
    protected abstract void segments( List<String> list );
    
    /**
     * Creates a string that identifies this name.
     * @return the name, does not have to be a C identifier
     */
    public String toIdentifier(){
        List<String> list = new ArrayList<String>();
        segments( list );
        
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        
        for( String segment : list ){
            if( first )
                first = false;
            else
                builder.append( "." );
            
            builder.append( segment );
        }
        
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return toIdentifier();
    }
    
    @Override
    public String getBindingValue() {
        return toString();
    }
    
    public int getSegmentCount() {
        return 0;
    }
    
    public String getSegmentName( int segment ) {
        return null;
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        return null;
    }
    
    public int getSegmentSize( int segment ) {
        return 0;
    }
}
