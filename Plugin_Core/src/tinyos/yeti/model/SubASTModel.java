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
package tinyos.yeti.model;

import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;

/**
 * A part of an {@link IASTModel}.
 * @author Benjamin Sigg
 *
 */
public class SubASTModel{
    private boolean fullyLoaded;
    private IASTModelNode[] nodes;
    
    public SubASTModel( boolean fullyLoaded, IASTModelNode[] nodes ){
        this.fullyLoaded = fullyLoaded;
        this.nodes = nodes;
    }
    
    public boolean isFullyLoaded(){
        return fullyLoaded;
    }
    
    public IASTModelNode[] getNodes(){
        return nodes;
    }
}
