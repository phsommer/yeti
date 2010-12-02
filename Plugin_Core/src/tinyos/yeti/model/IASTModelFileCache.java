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

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;

/**
 * A cache for storing {@link IASTModelNode}s.
 * @author Benjamin Sigg
 */
public interface IASTModelFileCache extends IFileCache<SubASTModel>{
    /**
     * Tells whether the cache of <code>file</code> contains a fully
     * loaded model.
     * @param file the file to check
     * @return <code>true</code> if it can be proved that cache exists and is 
     * fully loaded, <code>false</code> in any other case.
     */
    public boolean isFullyLoaded( IParseFile file );
}
