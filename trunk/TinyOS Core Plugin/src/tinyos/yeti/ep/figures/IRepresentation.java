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
package tinyos.yeti.ep.figures;

import org.eclipse.draw2d.IFigure;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;

/**
 * A figure that represents an {@link IASTModelNode} and that can be
 * selected.
 * @author Benjamin Sigg
 */
public interface IRepresentation extends IFigure{
    public enum Highlight{
        NONE, ON_PATH, ALTERNATIVE, SELECTED
    }
    
    /**
     * Gets the paths to the node which is represented by this representation,
     * can be <code>null</code>. If many paths are represented by this figure,
     * then the paths should be returned in the order they appear in a parent first
     * traversal of the tree.
     * @return the paths or <code>null</code> if this does currently not
     * represent a node 
     */
    public IASTModelPath[] getPaths();
    
    /**
     * Highlights this representation, normally that would mean to draw
     * something like a red border or use bold font for the content...
     * @param highlighted the highlight state
     * @param path the path that is actually selected
     */
    public void setHighlighted( Highlight highlighted, IASTModelPath path );
}
