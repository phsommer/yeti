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
package tinyos.yeti.nesc12.parser.ast.elements.types.conversion;

import java.util.Iterator;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.elements.Type;

public class InfiniteRecursionPreventer{
    private LinkedList<Type> stack = new LinkedList<Type>();
    
    public boolean push( Type source, Type destination ){
        // if the pair of types already gets checked, then stop the recursion here
        Iterator<Type> stackIterator = stack.iterator();
        while( stackIterator.hasNext() ){
            // &, not &&: to ask for two next()'s
            if( source == stackIterator.next() & destination == stackIterator.next() )
                return false;
        }            
        
        stack.addLast( source );
        stack.addLast( destination );
        
        return true;
    }
    
    public void pop(){
        stack.removeLast();
        stack.removeLast();
    }
}
