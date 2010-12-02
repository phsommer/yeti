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

public class PathRequest implements IPathRequest{
    private ISensorBoard[] boards;
    private MakeInclude[] directives;
    private MakeExclude[] excludes;
    private String[] fileExtensions;
    private String platformName;
    private ProjectTOS project;
    private boolean nostdinc;
    private boolean systemFiles;
    private Set<SearchFlag> flags;
    
    public void setBoards( ISensorBoard[] boards ){
        this.boards = boards;
    }
    
    public ISensorBoard[] getBoards(){
        return boards;
    }

    public void setDirectives( MakeInclude[] directives ){
        this.directives = directives;
    }
    
    public MakeInclude[] getDirectives(){
        return directives;
    }
    
    public void setExcludes( MakeExclude[] excludes ){
        this.excludes = excludes;
    }
    
    public MakeExclude[] getExcludes(){
        return excludes;
    }
    
    public void setFileExtensions( String[] fileExtensions ){
        this.fileExtensions = fileExtensions;
    }

    public String[] getFileExtensions(){
        return fileExtensions;
    }

    public void setPlatformName( String platformName ){
        this.platformName = platformName;
    }
    
    public String getPlatformName(){
        return platformName;
    }

    public void setProject( ProjectTOS project ){
        this.project = project;
    }
    
    public ProjectTOS getProject(){
        return project;
    }

    public void setNostdinc( boolean nostdinc ){
        this.nostdinc = nostdinc;
    }
    
    public boolean isNostdinc(){
        return nostdinc;
    }

    public void setSystemFiles( boolean systemFiles ){
        this.systemFiles = systemFiles;
    }
    
    public boolean isSystemFiles(){
        return systemFiles;
    }
    
    public void setFlags( Set<SearchFlag> flags ){
		this.flags = flags;
	}
    
    public Set<SearchFlag> getFlags(){
	    return flags;
    }
}
