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
import tinyos.yeti.environment.basic.commands.IExecutionResult;
import tinyos.yeti.environment.basic.commands.ncc.PrintPlatforms;
import tinyos.yeti.environment.basic.helper.StringGobbler;
import tinyos.yeti.environment.basic.path.IPathTranslator;
import tinyos.yeti.environment.winXP.Environment;

public class CommandExecuter extends AbstractCommandExecuter {
    /**
     * Just a little check whether the executer works or not.
     */
    public static void main( String[] args ) {
        Environment env = new Environment();
        
        System.out.println( Arrays.toString( env.execute( PrintPlatforms.COMMAND ) ) );
    }
    
    private Environment environment;
    
    public CommandExecuter( Environment environment ){
        this.environment = environment;
    }
    
    @Override
    protected IExecutionResult run( ICommand<?> command,
            IProgressMonitor monitor, OutputStream info, OutputStream out,
            OutputStream error ) throws InterruptedException, IOException {

        // start bash
        ProcessBuilder pb;
        File cygwinApplication = null;
        IPathTranslator translator = null;
        
        if( command instanceof ICygwinCommand ){
            cygwinApplication = ((ICygwinCommand<?>)command).getCygwinBash();
            translator = ((ICygwinCommand<?>)command).getPathTranslator();
        }
        
        if( cygwinApplication == null )
            cygwinApplication = environment.getPathManager().getCygwinBash();
        
        if( translator == null )
            translator = environment;
        
        pb = new ProcessBuilder( cygwinApplication.getPath(), "--login", "-i" );
        pb.directory( cygwinApplication.getParentFile() );

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
        
        PrintWriter writer = new PrintWriter( p.getOutputStream() );
        

        // set environment variables and start new bash
        Map<String,String> envp = command.useDefaultParameters() ? environment.getPathManager().getEnvironmentVariables() : null;
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
        if( envp == null ){
        	envp = Collections.emptyMap();
        }
        writer.print( "env " );
        
        if( envp != null ){
            for( Map.Entry<String, String> entry : envp.entrySet() ){
                writer.print( entry.getKey() );
                writer.print( "=" );
                
                String value = entry.getValue().trim();
                if( !value.startsWith( "\"" ) || !value.endsWith( "\"" )){
                	writer.print( "\"" );
                	writer.print( value );
                	writer.print( "\"" );
                }
                else{
                	writer.print( value );
                }
                
                writer.print( " " );
            }
        }
        writer.print( "bash" );
        writer.print( "\n" );
        
        // navigate to execution directory
        File systemDir = command.getDirectory();
        String dir = systemDir == null ? null : translator.systemToModel( systemDir );
        
        // DEBUG
        if( info != null || Debug.DEBUG ){
            StringBuilder builder = new StringBuilder();
            if( dir != null ){
            	builder.append( "working directory = " );
            	builder.append( dir );
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
        
        if( dir != null ){
        	writer.print( "cd " );
        	writer.print( dir );
        	writer.print( "\n" );
        }
        
        // execute a little script in the bash
        Debug.info( "command starting" );
        String[] commandArray = command.getCommand();
        Debug.info( Arrays.toString( commandArray ) );
        for( int i = 0; i < commandArray.length; i++ ){
            if( i > 0 )
                writer.print( " " );
            writer.print( "\"" );
            writer.print( commandArray[i] );
            writer.print( "\"" );
        }
        writer.print( "\n" );
        writer.print( "exit" );
        writer.print( "\n" );
        writer.flush();
        Debug.info( "command running" );

        Debug.info( "bash stopping" );
        writer.print( "exit" );
        writer.print( "\n" );
        writer.flush();
        int exitValue = waitFor( p, monitor );
        Debug.info( "bash stopped" );
        
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

    @Override
    public int waitFor( Process process, IProgressMonitor monitor ) {
    	return super.waitFor(process, monitor);
    }
}
