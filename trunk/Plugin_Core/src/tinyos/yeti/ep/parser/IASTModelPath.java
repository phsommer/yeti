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

import tinyos.yeti.ep.IParseFile;

/**
 * An {@link IASTModelPath} is a path to one {@link IASTModelNode}. Paths should
 * not be ambiguous. They identify a node, and must not be confused when an
 * {@link IASTModel} has changed since their creation. In fact, they must be
 * independent from the {@link IASTModel} and apply to more than just one model.<br>
 * Path can be invalid, for example by pointing to a node that does no longer
 * exist. Every method that works with paths has to be aware of faulty paths. Since
 * an invalid path can become valid again, there is no way to tell the validity
 * of a path before using it.<br>
 * Paths start at an imaginary root. The root path has <code>null</code> as parent
 * path. The root path should equal the result of 
 * {@link INesCParserFactory#createRoot(org.eclipse.core.resources.IProject)}.
 * @author Benjamin Sigg
 */
public interface IASTModelPath {
    /**
     * Gets the file which would need to be parsed in order to get the {@link IASTModelNode}
     * to which this path points.
     * @return the source file
     */
    public IParseFile getParseFile();
    
    /**
     * Returns the path to the parent node of the node that is described by this
     * path.
     * @return the path to the parent or <code>null</code> if this path describes
     * the imaginary root
     * @see INesCParserFactory#createRoot(org.eclipse.core.resources.IProject)
     */
    public IASTModelPath getParent();
    
    /**
     * Gets the size of this path.
     * @return the number of elements of this path
     */
    public int getDepth();
    
    /**
     * Returns the path that leads to <code>node</code>, which is a child
     * of the node described by this path. The behavior is undefined if
     * <code>node</code> is not a true child.
     * @param node the child
     * @return a path to <code>node</code>, assuming that <code>node</code>
     * is a child
     */
    public IASTModelPath getChild( IASTModelNode node );
}
