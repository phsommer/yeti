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
 * A filter that tells whether to include some {@link IASTModelNodeConnection} into
 * a set or not.
 * @author Benjamin Sigg
 */
public interface IASTModelConnectionFilter{
    /**
     * Tells whether <code>connection</code> should be included or not.
     * @param parent the parent of the connection
     * @param connection the connection to check
     * @return <code>true</code> if this filter approves of the connection 
     */
    public boolean include( IASTModelNode parent, IASTModelNodeConnection connection );
}
