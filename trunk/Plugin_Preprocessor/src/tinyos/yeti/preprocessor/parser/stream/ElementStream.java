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
package tinyos.yeti.preprocessor.parser.stream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * A stream that outputs {@link PreprocessorElement}s read from a tree of
 * {@link PreprocessorElement}s.
 * @author Benjamin Sigg
 */
public class ElementStream implements Iterator<PreprocessorElement>{
    private List<Level> stack = new ArrayList<Level>();
    private boolean parentFirst;
    
    public ElementStream( PreprocessorElement root, boolean parentFirst ){
        if( root == null )
            throw new IllegalArgumentException( "root must not be null" );
        
        Level level = new Level();
        level.index = -1;
        level.elements = new PreprocessorElement[]{ root };
        push( level );
        this.parentFirst = parentFirst;
    }
    
    public boolean hasNext() {
        return !stack.isEmpty();
    }
    
    public PreprocessorElement next() {
        if( !hasNext() )
            throw new NoSuchElementException();
        
        PreprocessorElement result = null;

        while( result == null ){
            Level top = peek();
            top.index++;
            if( top.index == top.elements.length ){
                pop();
                if( !parentFirst ){
                    top = peek();
                    result = top.elements[ top.index ];
                    if( top.index+1 == top.elements.length )
                        pop();
                }
            }
            else{
                PreprocessorElement[] children = top.elements[ top.index ].getChildren();
                if( children != null && children.length > 0 ){
                    Level next = new Level();
                    next.index = -1;
                    next.elements = children;
                    push( next );
                    if( parentFirst ){
                        result = top.elements[ top.index ];
                    }
                }
                else{
                    result = top.elements[ top.index ];
                }
            }
        }
        
        if( parentFirst && !stack.isEmpty() ){
            Level top = peek();
            while( top != null && top.index+1 == top.elements.length ){
                pop();
                if( stack.isEmpty() )
                    top = null;
                else
                    top = peek();
            }
        }
        
        return result;
    }
    
    private Level peek(){
        return stack.get( stack.size()-1 );
    }
    
    private Level pop(){
        return stack.remove( stack.size()-1 );
    }
    
    private void push( Level level ){
        stack.add( level );
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    private static class Level{
        int index;
        PreprocessorElement[] elements;
    }
}
