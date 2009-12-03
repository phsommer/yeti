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
package tinyos.yeti.environment.basic.tools.ncg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;

import tinyos.yeti.environment.basic.commands.AbstractCommand;
import tinyos.yeti.environment.basic.commands.IExecutionResult;
import tinyos.yeti.make.IMakeTarget;

public class NcgCommand extends AbstractCommand<Object>{	
    public NcgCommand( NcgSetting setting ){
        
        /*
         * Check for:
         *  target available.
         *  nesc-file selected.
         *  constants or files selected.
         */
        
        setDirectory( setting.getProject().getProject().getLocation().toFile() );

        List<String> commands = new ArrayList<String>();
        commands.add( "ncg" );

        IMakeTarget target = setting.getTarget();
        if( target != null ){
        	List<String> nescc = target.getNesccFlags(); 
        		
        	if( nescc != null ){
                commands.addAll( nescc );
            }
        }

        String tool = setting.getTool();
        if( tool != null ){
            String[] options = setting.getToolOptions( tool );
            for( String option : options ){
                commands.add( option + "=" + setting.getToolOption( tool, option, "" ) );
            }
        }

        String outputFile = setting.getOutput();
        if( outputFile != null ){
            commands.add( "-o" + outputFile );
        }

        /*
         No need for this: ncg already sets the driver
        String driver = setting.getDriver();
        if( driver != null ){
            commands.add( "-nescc=" + driver );
        }
        */

        commands.add( tool );
        String nesCFile = setting.getNesCFile();
        if( nesCFile != null ){
        	IFile file = setting.getProject().getProject().getFile( new Path( nesCFile ) );
        	if( file.exists() ){
        		commands.add( file.getLocation().toOSString() );
        	}
        	else{
        	    IFolder container = setting.getProject().getLegacySourceContainer();
        	    commands.add( container.getLocation().toOSString() + File.separatorChar + nesCFile );
        	}
        }

        String[] filenamesOrConstants = setting.getFilenamesOrConstants();
        if( filenamesOrConstants != null ){
            for( String name : filenamesOrConstants )
                commands.add( name );
        }

        setCommand( commands.toArray( new String[ commands.size() ] ) );
    }

    public Object result( IExecutionResult result ){
        return null;
    }

    public boolean shouldPrintSomething(){
        return true;
    }
}
