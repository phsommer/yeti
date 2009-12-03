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

/**
 * Observes a {@link ProjectModel} and gets informed when the model parses
 * files.
 * @author Benjamin Sigg
 */
public interface IProjectModelListener{
    /**
     * Called when a file somehow changed (that includes removing).
     * @param parseFile the file that was changed
     * @param continuous if set, then a call to {@link #changed(IParseFile[])} will
     * follow that contains all changed files
     */
    public void changed( IParseFile parseFile, boolean continuous );
    
    /**
     * May be called when several files were changed and the flag
     * <code>continuous</code> of {@link #changed(IParseFile, boolean)} was
     * set to <code>true</code>
     * @param parseFiles the set of changed files
     */
    public void changed( IParseFile[] parseFiles );
    
    /**
     * Called when the model was (re)initialized. All files were parsed anew.
     */
    public void initialized();
}
