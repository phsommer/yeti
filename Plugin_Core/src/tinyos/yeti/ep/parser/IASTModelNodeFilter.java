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

/**
 * A filter that tells whether to include some {@link IASTModelNode} into
 * a set or not.
 * @author Benjamin Sigg
 */
public interface IASTModelNodeFilter {
    /**
     * Tells whether <code>node</code> should be included or not.
     * @param node the node to check
     * @return <code>true</code> if this filter approves of the node 
     */
    public boolean include( IASTModelNode node );
}
