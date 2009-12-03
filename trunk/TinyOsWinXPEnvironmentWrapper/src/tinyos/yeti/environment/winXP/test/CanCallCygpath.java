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
package tinyos.yeti.environment.winXP.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.environment.basic.helper.StreamGobbler;
import tinyos.yeti.environment.basic.helper.StringGobbler;
import tinyos.yeti.environment.basic.test.AbstractTest;
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.environment.winXP.TinyOSWinXPEnvironmentWrapper;

public class CanCallCygpath extends AbstractTest {
    public CanCallCygpath(){
        setName( "Cygpath" );
        setDescription( "Tries to call 'cygpath -W', which should print out the location of the Windows directory" );
    }
    
    public IStatus run( OutputStream out, OutputStream err, IProgressMonitor monitor ) {
        File path = Environment.getEnvironment().getPathManager().getCygwinCygpath();
        if( path == null ){
            return new Status( IStatus.ERROR, TinyOSWinXPEnvironmentWrapper.PLUGIN_ID, "Cygpath is not set up" );
        }
        if( !path.exists() ){
            return new Status( IStatus.ERROR, TinyOSWinXPEnvironmentWrapper.PLUGIN_ID, "Cygpath path is not correct: '" + path.getAbsolutePath() + "'" );
        }
        
        ProcessBuilder builder = new ProcessBuilder( path.getAbsolutePath(), "-W" );
        
        try {
            Process process = builder.start();
            StringGobbler outGobbler = new StringGobbler( process.getInputStream(), out );
            StreamGobbler errGobbler = new StreamGobbler( process.getErrorStream(), err );
            
            outGobbler.start();
            errGobbler.start();
            
            Environment.getEnvironment().getCommandExecuter().waitFor( process, monitor );
            
            if( monitor.isCanceled() ){
                outGobbler.stop();
                errGobbler.stop();
            }
            else{
                outGobbler.join();
                errGobbler.join();
                
                String output = outGobbler.getOutputString();
                if( output == null || output.length() == 0 ){
                    return new Status( IStatus.ERROR, TinyOSWinXPEnvironmentWrapper.PLUGIN_ID, "Cygpath did not return anything" );
                }
                if( !output.contains( "cygdrive" )){
                    return new Status( IStatus.WARNING, TinyOSWinXPEnvironmentWrapper.PLUGIN_ID, "Cygpath returned a path for WINDOWS that does not contain 'cygdrive', as was expected"); 
                }
            }
        }
        catch (IOException e) {
            return new Status( IStatus.ERROR, TinyOSWinXPEnvironmentWrapper.PLUGIN_ID, e.getMessage(), e );
        }
        
        return Status.OK_STATUS;
    }
}
