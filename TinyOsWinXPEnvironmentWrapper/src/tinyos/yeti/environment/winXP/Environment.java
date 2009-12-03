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
package tinyos.yeti.environment.winXP;

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
import tinyos.yeti.environment.winXP.example.ExampleManager;
import tinyos.yeti.environment.winXP.execute.CommandExecuter;
import tinyos.yeti.environment.winXP.path.PathManager;
import tinyos.yeti.environment.winXP.platform.PlatformManager;
import tinyos.yeti.environment.winXP.preference.PreferenceInitializer;
import tinyos.yeti.environment.winXP.test.CanCallCygpath;
import tinyos.yeti.environment.winXP.test.CanCallCygwin;

public class Environment extends AbstractEnvironment{
    private static Environment environment;
    
    public static Environment getEnvironment(){
        return environment;
    }
    
    public Environment(){
        environment = this;
        
        setEnvironmentName( "TinyOS WinXP Cygwin Wrapper" );
        setEnvironmentID( "tinyos_winxp_wrapper" );
        setEnvironmentDescription( "TinyOS 2.x in Cygwin on Windows" );
        setEnvironmentImportance( 20 );
        
        addTest( new CanCallCygwin() );
        addTest( new CanCallCygpath() );
        addTest( new TosrootValid( this ));
        addTest( new TosdirValid( this ));
        addTest( new MakerulesValid( this ));
        addTest( new EchoTosroot( this ));
        addTest( new EchoTosdir( this ));
        addTest( new EchoMakerules( this ));
        addTest( new ListPlatformsTest( this ));
        
        PreferenceInitializer.scheduleDefaultsUpdate();
    }
    
    public void fireReinitialized(){
        getPlatformManager().clearPlatforms();
        super.fireReinitialized( 250 );
    }
    
    @Override
    public CommandExecuter getCommandExecuter() {
        return (CommandExecuter)super.getCommandExecuter();
    }
    
    @Override
    protected ICommandExecuter createCommandExecuter() {
        return new CommandExecuter( this );
    }

    @Override
    public ExampleManager getExampleManager() {
        return (ExampleManager)super.getExampleManager();
    }
    
    @Override
    protected IExampleManager createExampleManager() {
        return new ExampleManager( this );
    }

    @Override
    public PathManager getPathManager() {
        return (PathManager)super.getPathManager();
    }
    
    @Override
    protected IPathManager createPathManager() {
        return new PathManager( this );
    }

    @Override
    public PlatformManager getPlatformManager() {
        return (PlatformManager)super.getPlatformManager();
    }
    
    @Override
    protected IPlatformManager createPlatformManager() {
        return new PlatformManager( this );
    }
}
