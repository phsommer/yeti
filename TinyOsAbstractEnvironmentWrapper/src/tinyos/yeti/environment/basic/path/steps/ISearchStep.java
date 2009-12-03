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
package tinyos.yeti.environment.basic.path.steps;

import tinyos.yeti.environment.basic.path.AbstractPathManager;
import tinyos.yeti.environment.basic.path.IPathRequest;
import tinyos.yeti.environment.basic.path.IPathSet;
import tinyos.yeti.environment.basic.progress.ICancellation;

/**
 * Used by the {@link AbstractPathManager} to simulate the steps of searching
 * files.
 * @author Benjamin Sigg
 */
public interface ISearchStep extends ICollectStep{
    /**
     * Searches for the file <code>fileName</code> and stores it using
     * {@link IPathSet#store(java.io.File)}.
     * @param fileName the file for which is searched
     * @param request where to search the file
     * @param paths the storage
     * @param cancellation to cancel this operation
     */
    public void locate( String fileName, IPathRequest request, IPathSet paths, ICancellation cancellation );
}
