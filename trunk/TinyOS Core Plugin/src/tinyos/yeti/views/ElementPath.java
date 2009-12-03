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
package tinyos.yeti.views;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.parser.IASTModelPath;

/**
 * Describes the location of an {@link NodeContentProvider.Element}.
 * @author Benjamin Sigg
 */
public class ElementPath{
    private List<IASTModelPath> paths = new ArrayList<IASTModelPath>();
    private List<Integer> indices = new ArrayList<Integer>();
    
    /**
     * Adds a new entry.
     * @param path the path of the elements node
     * @param sameIndex given all the children with the same <code>path</code>,
     * this is the index of the element that should be described
     */
    public void add( IASTModelPath path, int sameIndex ){
        paths.add( path );
        indices.add( sameIndex );
    }

    public int getSize(){
        return paths.size();
    }
    
    public int getSamePathIndex( int element ){
        return indices.get( element );
    }
    
    public IASTModelPath getModelPath( int element ){
        return paths.get( element );
    }
}
