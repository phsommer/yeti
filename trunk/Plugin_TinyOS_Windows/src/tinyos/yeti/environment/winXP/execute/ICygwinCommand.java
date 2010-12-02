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
package tinyos.yeti.environment.winXP.execute;

import java.io.File;

import tinyos.yeti.environment.basic.commands.ICommand;
import tinyos.yeti.environment.basic.path.IPathTranslator;

/**
 * A command which will be executed in cygwin, this command can tell which
 * cygwin installation to use.
 * @author Benjamin Sigg
 *
 * @param <R> the kind of result this command creates
 */
public interface ICygwinCommand<R> extends ICommand<R>{
    /**
     * Gets the start file for cygwin, normally something like
     * "bash.exe".
     * @return the absolute path to the start, can be <code>null</code>
     */
    public File getCygwinBash();
    
    /**
     * Gets the path translator used to translate paths to and from
     * cygwin.
     * @return the translator
     */
    public IPathTranslator getPathTranslator();
}
