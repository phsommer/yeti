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

import java.io.File;

import tinyos.yeti.environment.basic.path.IPathRequest;
import tinyos.yeti.environment.basic.path.IPathSet;
import tinyos.yeti.environment.basic.path.IPlatformFile;
import tinyos.yeti.environment.basic.platform.IExtendedPlatform;
import tinyos.yeti.environment.basic.progress.ICancellation;

/**
 * Takes the processor of the platform - if known - into account.
 * @author Benjamin Sigg
 */
public abstract class ArchitectureStep extends AbstractSearchStep{
	public String getName(){
		return "ArchitectureStep";
	}
	
    public void locate( String fileName, IPathRequest request, IPathSet paths, ICancellation cancellation ){
        File[] includes = getIncludeDirectories( request, paths );
        if( includes != null ){
            for( File include : includes ){
                locateRecursive( fileName, paths, include, cancellation );
            }
        }
    }

    public void collect( IPathRequest request, IPathSet paths, ICancellation cancellation ){
        File[] includes = getIncludeDirectories( request, paths );
        if( includes != null ){
            for( File include : includes ){
                collect( paths, include, true, cancellation );
            }
        }
    }
    
    protected File[] getIncludeDirectories( IPathRequest request, IPathSet paths ){
        IExtendedPlatform platform = paths.getPlatform( request.getPlatformName() );
        if( platform == null )
            return null;
        IPlatformFile platformFile = platform.getPlatformFile();
        String processor = platformFile.getArchitecture();
        if( processor == null )
            return null;
        
        return getIncludeDirectories( processor );
    }
    
    protected abstract File[] getIncludeDirectories( String processor );
}
