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
package tinyos.yeti.environment.unix2.executable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.Debug;
import tinyos.yeti.environment.basic.commands.AbstractCommandExecuter;
import tinyos.yeti.environment.basic.commands.ICommand;
import tinyos.yeti.environment.basic.commands.ICommandExecuter;
import tinyos.yeti.environment.basic.commands.IExecutionResult;
import tinyos.yeti.environment.basic.helper.StringGobbler;
import tinyos.yeti.environment.unix2.Environment;

/**
 * {@link ICommandExecuter} that works directly in a bash.
 * @author Benjamin Sigg
 */
public class CommandExecuter extends AbstractCommandExecuter{
    private Environment environment;
    
    public CommandExecuter( Environment environment ){
        this.environment = environment;
    }
    
    @Override
    protected IExecutionResult run( ICommand<?> command,
            IProgressMonitor monitor, OutputStream info, OutputStream out, OutputStream error ) throws InterruptedException, IOException{

        ProcessBuilder pb;
        
        if( command.assumesInteractive() ){
        	pb = new ProcessBuilder( "bash", "-l", "-i" );
        }
        else{
        	// pb = new ProcessBuilder( "bash" );
        	pb = new ProcessBuilder( command.getCommand() );
        }
        
        File dir = command.getDirectory();
        if( dir != null ){
        	pb.directory( dir );
        }
        
        Map<String,String> env = pb.environment();
        Map<String, String> envp = null;
        if( command.useDefaultParameters() && environment != null ){
        	envp = environment.getPathManager().getEnvironmentVariables();
        }
        Map<String,String> moreEnvp = command.getEnvironmentParameters();
        
        if( moreEnvp != null ){
        	if( envp == null ){
        		envp = moreEnvp;
        	}
        	else{
        		envp = new HashMap<String, String>( envp );
        		envp.putAll( moreEnvp );
        	}
        }
        if( envp == null )
        	envp = Collections.emptyMap();
        
        transferAll( envp, env );

        if( info != null || Debug.DEBUG ){
            StringBuilder builder = new StringBuilder();
            
            if( dir != null ){
            	builder.append( "working directory = " );
            	builder.append( dir.getPath() );
            	builder.append( "\n" );
            }
            for( Map.Entry<String, String> entry : envp.entrySet() ){
                builder.append( "set: " );
                builder.append( entry.getKey() );
                builder.append( " = " );
                builder.append( entry.getValue() );
                builder.append( "\n" );
            }

            if( info != null ){
                info.write( builder.toString().getBytes() );
                info.flush();
            }
            if( Debug.DEBUG ){
                Debug.info( builder.toString() );
            }
        }

        Debug.info( "bash starting" );
        Process p = pb.start();
        Debug.info( "bash running" );
        
        Debug.info( "globber starting" );
        StringGobbler errorGobbler = new StringGobbler( p.getErrorStream(), error );
        StringGobbler outputGobbler = new StringGobbler( p.getInputStream(), out );
        
        errorGobbler.setPartner( outputGobbler );
        outputGobbler.setPartner( errorGobbler );

        errorGobbler.start();
        outputGobbler.start();
        Debug.info( "globber running" );

        // execute a little script in the bash
        if( command.assumesInteractive() ){
            PrintWriter writer = new PrintWriter( p.getOutputStream() );
            
//            if( envp != null ){
//            	for( Map.Entry<String, String> entry : envp.entrySet() ){
//            		writer.println( entry.getKey() + "=" + entry.getValue() );
//            	}
//            }
            
	        String[] commandArray = command.getCommand();
	        Debug.info( Arrays.toString( commandArray ) );
	        for( int i = 0; i < commandArray.length; i++ ){
	            if( i > 0 )
	                writer.print( " " );
	            writer.print( "\"" );
	            writer.print( commandArray[i] );
	            writer.print( "\"" );
	        }
	        writer.println();
	        writer.print( "exit" );
	        writer.println();
	        writer.flush();
        }
        
        int exitValue = waitFor( p, monitor );
        
        Debug.info( "globber stopping" );
        if( monitor.isCanceled() ){
            outputGobbler.stop();
            errorGobbler.stop();
        }
        else{
            outputGobbler.join();
            errorGobbler.join();
        }
        Debug.info( "globber stopped" );
        
        String outputGlobber = outputGobbler.getOutputString().trim();
        String errorGlobber = errorGobbler.getOutputString().trim();

        if( Debug.DEBUG ){
            Debug.info( "output = " + outputGlobber );
            Debug.info( "error = " + errorGlobber );
        }
        
        return toResult( outputGlobber, errorGlobber, exitValue );
    }
    
    private void transferAll( Map<String,String> source, Map<String,String> destination ){
    	for( Map.Entry<String, String> entry : source.entrySet() ){
    		destination.put( entry.getKey(), toEnvValue( entry.getValue() )); 
    	}
    }
    
    private String toEnvValue( String value ){
    	if( value.length() >= 2 ){
    		if( value.charAt( 0 ) == '"' && value.charAt( value.length()-1 ) == '"' ){
    			value = value.substring( 1, value.length()-1 );
    		}
    	}
    	
    	value = value.replace( "\\\"", "\"" );
    	return value;
    }
}
