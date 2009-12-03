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
package tinyos.yeti.environment.winXP.path;

import java.io.File;
import java.io.IOException;

import tinyos.yeti.environment.basic.helper.StringGobbler;
import tinyos.yeti.environment.winXP.TinyOSWinXPEnvironmentWrapper;

/**
 * A path translator which uses a concrete instance of a cygpath.exe
 * to translate paths;
 * @author Benjamin Sigg
 */
public class CygpathPathTranslator implements IWinPathTranslator{
    private File cygpath;
    
    public CygpathPathTranslator( File cygpath ){
        if( cygpath == null )
            throw new IllegalArgumentException( "cygpath must not be null" );
        this.cygpath = cygpath;
    }
    
    public File modelToSystem( String file ){
        return cygwinToSystem( cygpath, file );
    }
    public String systemToModel( File file ){
        return systemToCygwin( cygpath, file );
    }

    public void notifyResourceChanged() {
        // ignore
    }
    
    public static File cygwinToSystem( File cygpath, String file ){
        try{
            ProcessBuilder builder = new ProcessBuilder( cygpath.getAbsolutePath(), "-w", file );
            builder.directory( cygpath.getParentFile() );

            Process process = builder.start();

            StringGobbler error = new StringGobbler( process.getErrorStream(), null );
            StringGobbler input = new StringGobbler( process.getInputStream(), null );

            error.start();
            input.start();

            process.waitFor();

            error.stop();
            input.stop();

            String result = input.getOutputString();
            if( result == null || "".equals( result.trim() ))
                return null;

            return new File( result.trim() );
        }
        catch( IOException e ){
            TinyOSWinXPEnvironmentWrapper.warning( e );
        }
        catch( InterruptedException e ){
            TinyOSWinXPEnvironmentWrapper.warning( e );
        }
        
        return null;
    }

    public static String systemToCygwin( File cygpath, File file ){
        try{
            ProcessBuilder builder = new ProcessBuilder( cygpath.getAbsolutePath(), "-u", file.getAbsolutePath() );
            builder.directory( cygpath.getParentFile() );

            Process process = builder.start();

            StringGobbler error = new StringGobbler( process.getErrorStream(), null );
            StringGobbler input = new StringGobbler( process.getInputStream(), null );

            error.start();
            input.start();

            process.waitFor();

            error.stop();
            input.stop();

            String result = input.getOutputString();
            if( result == null || "".equals( result.trim() ))
                return null;

            return result.trim();
        }
        catch( IOException e ){
            TinyOSWinXPEnvironmentWrapper.warning( e );
        }
        catch( InterruptedException e ){
            TinyOSWinXPEnvironmentWrapper.warning( e );
        }
        
        return null;
    }
}
