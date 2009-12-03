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
package tinyos.yeti.environment.basic.test;

import java.io.OutputStream;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.commands.ncc.PrintPlatforms;
import tinyos.yeti.ep.IPlatform;

public class ListPlatformsTest extends AbstractTest{
    private AbstractEnvironment environment;

    public ListPlatformsTest( AbstractEnvironment environment ){
        this.environment = environment;

        setName( "Platforms" );
        setDescription( "Calls 'ncc -print-platforms' to find all platforms" );
    }

    public IStatus run( OutputStream out, OutputStream err, IProgressMonitor monitor ) throws Exception{
        String[] result = environment.getCommandExecuter().execute(  PrintPlatforms.COMMAND, monitor, out, out, err );
        if( result == null || result.length == 0 ){
            return new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "No platforms found" );
        }
        else{
            IPlatform[] platforms = environment.getPlatformManager().getPlatforms();
            String[] expected = new String[ platforms.length ];
            for( int i = 0, n = platforms.length; i<n; i++ ){
                expected[i] = platforms[i].getName();
            }

            Arrays.sort( expected );
            Arrays.sort( result );

            if( Arrays.equals( result, expected )){
                return new Status( IStatus.OK, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "Platforms found: " + Arrays.toString( result ));    
            }
            else{
                return new Status( IStatus.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "Platforms found: " + Arrays.toString( result ) + ", but expected: " + Arrays.toString( expected ));
            }
        }
    }
}
