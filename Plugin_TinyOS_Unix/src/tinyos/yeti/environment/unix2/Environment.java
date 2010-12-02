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
package tinyos.yeti.environment.unix2;

import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.environment.basic.commands.ICommandExecuter;
import tinyos.yeti.environment.basic.example.IExampleManager;
import tinyos.yeti.environment.basic.path.IPathManager;
import tinyos.yeti.environment.basic.platform.IPlatformManager;
import tinyos.yeti.environment.basic.test.EchoMakerules;
import tinyos.yeti.environment.basic.test.EchoTosdir;
import tinyos.yeti.environment.basic.test.EchoTosroot;
import tinyos.yeti.environment.basic.test.ListPlatformsTest;
import tinyos.yeti.environment.basic.test.MakerulesValid;
import tinyos.yeti.environment.basic.test.TosdirValid;
import tinyos.yeti.environment.basic.test.TosrootValid;
import tinyos.yeti.environment.unix2.example.ExampleManager;
import tinyos.yeti.environment.unix2.executable.CommandExecuter;
import tinyos.yeti.environment.unix2.path.PathManager;
import tinyos.yeti.environment.unix2.platform.PlatformManager;
import tinyos.yeti.environment.unix2.preference.PreferenceInitializer;
import tinyos.yeti.environment.unix2.test.EnvironmentPathTest;

public class Environment extends AbstractEnvironment{
    private static Environment environment;
    
    public static Environment getEnvironment(){
        return environment;
    }
    
    public Environment(){
    	environment = this;
        
        // force the platforms to be loaded
        getPlatforms();
        
        setEnvironmentID( "tinyos_unix_wrapper_2" );
        setEnvironmentName( "TinyOS Unix Wrapper 2" );
        setEnvironmentDescription( "Wrapper for the TinyOS 2.x environment" );
        
        setEnvironmentImportance( 20 );
        
        addTest( new EnvironmentPathTest());
        addTest( new TosrootValid( this ));
        addTest( new TosdirValid( this ));
        addTest( new MakerulesValid( this ));
        addTest( new EchoTosroot( this ));
        addTest( new EchoTosdir( this ));
        addTest( new EchoMakerules( this ));
        addTest( new ListPlatformsTest( this ) );
        
        PreferenceInitializer.scheduleDefaultsUpdate();
    }
    
    public void fireReinitialized() {
        getPlatformManager().clearPlatforms();
        super.fireReinitialized( 250 );
    }
    
    @Override
    protected IExampleManager createExampleManager(){
        return new ExampleManager( this );
    }

    @Override
    public ExampleManager getExampleManager(){
        return (ExampleManager)super.getExampleManager();
    }
    
    @Override
    protected IPathManager createPathManager(){
        return new PathManager( this );
    }

    @Override
    public PathManager getPathManager(){
        return (PathManager)super.getPathManager();
    }
    
    @Override
    public PlatformManager getPlatformManager(){
        return (PlatformManager)super.getPlatformManager();
    }
    
    @Override
    protected IPlatformManager createPlatformManager(){
        return new PlatformManager( this );
    }
    
    @Override
    public CommandExecuter getCommandExecuter(){
        return (CommandExecuter)super.getCommandExecuter();
    }
    
    @Override
    protected ICommandExecuter createCommandExecuter(){
        return new CommandExecuter( this );
    }
}
