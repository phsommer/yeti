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
package tinyos.yeti.environment.basic.path;

import java.util.Set;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.ISensorBoard;
import tinyos.yeti.ep.IEnvironment.SearchFlag;
import tinyos.yeti.make.MakeExclude;
import tinyos.yeti.make.MakeInclude;

/**
 * A path request contains various properties that are needed to search
 * a file or a set of files by the {@link IPathManager} 
 * @author Benjamin Sigg
 */
public interface IPathRequest{
    /**
     * Gets the project for which the request is stated.
     * @return the project, may not be <code>null</code>
     */
    public ProjectTOS getProject();
    
    /**
     * Gets the projects include directives (option -I for ncc).
     * @return the include directives, may be <code>null</code>
     */
    public MakeInclude[] getDirectives();
    
    /**
     * Gets the rules to which directories are excluded from any search.
     * @return the excluded directories, can be <code>null</code>
     */
    public MakeExclude[] getExcludes();
    
    /**
     * Gets the boards (option -board for ncc).
     * @return the boards, may be <code>null</code>
     */
    public ISensorBoard[] getBoards();
    
    /**
     * Gets the name of the platform for which the project gets compiled.
     * @return the platform, may be <code>null</code>
     */
    public String getPlatformName();
    
    /**
     * Whether only system files should be taken into account. System files
     * are defined as all files except the files of the project.
     * @return whether only system files should be included in any kind of search
     */
    public boolean isSystemFiles();
    
    /**
     * Whether the standard includes should not be resolved (option -nostdinc for ncc).
     * @return whether standard includes should be omitted
     */
    public boolean isNostdinc();
    
    /**
     * A set of extensions, each file that is found must have one of the extensions.
     * If set to <code>null</code>, then extensions are ignored.
     * @return the extensions or <code>null</code> if extensions should be ignored
     */
    public String[] getFileExtensions();
    
    /**
     * Additional search flags specified by the caller.
     * @return the flags
     */
    public Set<SearchFlag> getFlags();
}
