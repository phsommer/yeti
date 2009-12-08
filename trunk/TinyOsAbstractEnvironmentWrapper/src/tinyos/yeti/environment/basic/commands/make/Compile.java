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
package tinyos.yeti.environment.basic.commands.make;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.commands.AbstractCommand;
import tinyos.yeti.environment.basic.commands.IExecutionResult;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.EnvironmentVariable;
import tinyos.yeti.make.IMakeTarget;

public class Compile extends AbstractCommand<Object>{
    public Compile( File directory, ProjectTOS project, IMakeTarget target, AbstractEnvironment environment ) throws CoreException{
        String directoryName = environment.systemToModel( directory );
        if( directoryName == null )
            throw new CoreException( new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "Cannot convert '" + directory.getPath() + "' to model path" ));
        
        List<String> command = new ArrayList<String>();
        command.add( "make" );
        command.add( "-C" );
        command.add( directoryName );
        command.add( "-f" );
        
        String makefilePath = environment.systemToModel( project.ensureMakefilePath() );
        if( makefilePath == null )
            throw new CoreException( new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, 
                    "Cannot convert '" + project.ensureMakefilePath().getPath() + "' to model path" ));
        
        command.add( makefilePath );
       
        command.addAll( target.getMakeCommands() );
        
        setCommand( command.toArray( new String[ command.size() ] ) );
        
        putEnvironmentParameters( environment.getPathManager().getEnvironmentVariables() );
        
        putEnvironmentParameter( "ECLIPSE_PROJECT", directoryName );
        
        IFile componentFile = target.getComponentFile();
        if( componentFile != null ){
        	File file = componentFile.getLocation().toFile();
        	String path = environment.systemToModel( file );
        	if( path.endsWith( ".nc" ))
        		path = path.substring( 0, path.length()-3 );
        	putEnvironmentParameter( "COMPONENT", path.replace( " ", "\\ " ) );
        }
        else{
        	String component = target.getComponent();
        	if( component != null ){
        		IContainer container = project.getLegacySourceContainer();
        		putEnvironmentParameter( "COMPONENT", container.getProjectRelativePath().toString() + "/" + component );
        	}
        }
        
        List<String> pflags = target.getPFlags();
        StringBuilder builder = new StringBuilder();
        for( String flag : pflags ){
        	if( builder.length() > 0 )
        		builder.append( " " );
            builder.append( "\\\"" );
            builder.append( flag );
            builder.append( "\\\"" );
        }
        
        putEnvironmentParameter( "PFLAGS", builder.toString() );
        
        IPlatform platform = target.getPlatform();
        if( platform != null ){
        	EnvironmentVariable[] variables = platform.getDefaultEnvironmentVariables();
        	if( variables != null ){
        		for( EnvironmentVariable variable : variables ){
        			putEnvironmentParameter( variable.getKey(), variable.getValue() );
        		}
        	}	 
        }
        
        EnvironmentVariable[] variables = target.getEnvironmentVariables();
        if( variables != null ){
        	for( EnvironmentVariable variable : variables ){
        		putEnvironmentParameter( variable.getKey(), variable.getValue() );
        	}
        }
        
        setAssumesInteractive( false );
    }
    
    public Object result( IExecutionResult result ){
        return null;
    }

    public boolean shouldPrintSomething(){
        return false;
    }
}
