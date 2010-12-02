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
import tinyos.yeti.environment.basic.progress.ICancellation;
import tinyos.yeti.ep.ISensorBoard;

public class SensorBoardStep extends AbstractSearchStep{
	public String getName(){
		return "SensorBoardStep";
	}
	
    public void collect( IPathRequest request, IPathSet paths, ICancellation cancellation ){
        if( request.isNostdinc() )
            return;
        
        ISensorBoard[] boards = request.getBoards();
        if( boards == null )
            return;
        
        for( ISensorBoard board : boards ){
            String[] directives = board.getDirectives();
            if( directives != null ){
                for( String directive : directives ){
                    directive = paths.replace( directive );
                    
                    String[] absoluteDirectives = paths.relativeToAbsolute( directive );
                    for( String absoluteDirective : absoluteDirectives ){
                    	if( absoluteDirective != null ){
                            File file = paths.getPathManager().modelToSystem( absoluteDirective );
                            if( file != null ){
                                collect( paths, file, false, cancellation );
                            }
                            
                            if( cancellation.isCanceled() )
                                return;		
                    	}
                    }
                }
            }
            
            collect( paths, board.getDirectory(), true, cancellation );
            if( cancellation.isCanceled() )
                return;
        }
    }

    public void locate( String fileName, IPathRequest request, IPathSet paths, ICancellation cancellation ){
        if( request.isNostdinc() )
            return;
        
        ISensorBoard[] boards = request.getBoards();
        if( boards == null )
            return;
        
        for( ISensorBoard board : boards ){
            if( cancellation.isCanceled() )
                return;
            
            String[] directives = board.getDirectives();
            if( directives != null ){
                for( String directive : directives ){
                    if( cancellation.isCanceled() )
                        return;
                    
                    directive = paths.replace( directive );
                    String[] absoluteDirectives = paths.relativeToAbsolute( directive );
                    for( String absoluteDirective : absoluteDirectives ){
                    	if( absoluteDirective != null ){
                    		File file = paths.getPathManager().modelToSystem( absoluteDirective );
                    		if( file != null ){
                    			if( locate( fileName, paths, file, cancellation ))
                    				return;
                    		}
                    	}
                    }
                }
            }
            
            if( locateRecursive( fileName, paths, board.getDirectory(), cancellation ))
                return;
        }
    }

}
