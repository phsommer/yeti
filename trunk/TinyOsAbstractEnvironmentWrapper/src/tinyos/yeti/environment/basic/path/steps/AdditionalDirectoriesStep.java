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

import tinyos.yeti.environment.basic.path.IPathManager;
import tinyos.yeti.environment.basic.path.IPathRequest;
import tinyos.yeti.environment.basic.path.IPathSet;
import tinyos.yeti.environment.basic.platform.IExtendedPlatform;
import tinyos.yeti.environment.basic.progress.ICancellation;

public class AdditionalDirectoriesStep extends AbstractSearchStep{
	public String getName(){
		return "AdditionalDirectoryStep";
	}

    public void collect( IPathRequest request, IPathSet paths, ICancellation cancellation ) {
        String[] additional = list( request, paths );
        if( additional != null ){
            IPathManager manager = paths.getPathManager();
            
            for( String directive : additional ){
                directive = paths.replace( directive );
                String[] absoluteDirectives = paths.relativeToAbsolute( directive );
                for( String absoluteDirective : absoluteDirectives ){
                	if( absoluteDirective != null ){
                		collect( paths, manager.modelToSystem( absoluteDirective ), false, cancellation );
                		if( cancellation.isCanceled() )
                			return;
                	}
                }
            }
        }
    }

    public void locate( String fileName, IPathRequest request, IPathSet paths, ICancellation cancellation ) {
        String[] additional = list( request, paths );
        if( additional != null ){
            IPathManager manager = paths.getPathManager();
            
            for( String directive : additional ){
                directive = paths.replace( directive );
                String[] absoluteDirectives = paths.relativeToAbsolute( directive );
                for( String absoluteDirective : absoluteDirectives ){
                	if( absoluteDirective != null ){
                		if( locate( fileName, paths, manager.modelToSystem( absoluteDirective ), cancellation ))
                			return;
                
                		if( cancellation.isCanceled() )
                			return;
                	}
                }
            }
        }        
    }
    
    protected String[] list( IPathRequest request, IPathSet paths ){
        if( request.isNostdinc() )
            return null;
        
        String platformName = request.getPlatformName();
        if( platformName == null )
            return null;
        
        IExtendedPlatform platform = paths.getPlatform( platformName );
        if( platform == null )
            return null;
        
        return platform.getPlatformFile().getIncludes();
    }
}
