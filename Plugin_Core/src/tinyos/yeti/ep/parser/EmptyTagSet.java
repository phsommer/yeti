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
package tinyos.yeti.ep.parser;

import java.io.IOException;

import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

/**
 * A TagSet that is always empty.
 * @author Benjamin Sigg
 *
 */
public class EmptyTagSet extends TagSet{
    public static final EmptyTagSet EMPTY = new EmptyTagSet();
    
    public static final IGenericFactory<EmptyTagSet> FACTORY = new IGenericFactory<EmptyTagSet>(){
        public EmptyTagSet create(){
            return EMPTY;
        }

        public void write( EmptyTagSet value, IStorage storage ) throws IOException{
            // nothing
        }
        
        public EmptyTagSet read( EmptyTagSet value, IStorage storage ) throws IOException{
            return value;
        }
    };
    
    @Override
    public boolean add( Tag tag ){
        throw new UnsupportedOperationException( "can't add anything to EMPTY set" );
    }
    @Override
    public void add( TagSet tags, Tag tag ){
        throw new UnsupportedOperationException( "can't add anything to EMPTY set" );
    }

}
