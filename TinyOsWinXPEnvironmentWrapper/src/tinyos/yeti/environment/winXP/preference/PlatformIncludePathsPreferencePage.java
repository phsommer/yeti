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
package tinyos.yeti.environment.winXP.preference;

import org.eclipse.jface.preference.IPreferenceStore;

import tinyos.yeti.Debug;
import tinyos.yeti.environment.basic.platform.PlatformUtility;
import tinyos.yeti.environment.basic.preferences.AbstractPlatformIncludePathsPreferencePage;
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.environment.winXP.TinyOSWinXPEnvironmentWrapper;
import tinyos.yeti.environment.winXP.platform.Platform;
import tinyos.yeti.environment.winXP.platform.PlatformManager;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.MakeInclude;

public class PlatformIncludePathsPreferencePage extends AbstractPlatformIncludePathsPreferencePage{
    @Override
    protected IEnvironment getEnvironment() {
        return Environment.getEnvironment();
    }
    
    @Override
    protected MakeInclude[] getIncludes( IPlatform platform ){
        try{
            Debug.enter();
            
            if( platform == null )
                return getManager().getDefaultMakeIncludes();
        
            return ((Platform)platform).getIncludes();
        }
        finally{
            Debug.leave();
        }
    }
    
    @Override
    protected MakeInclude[] getDefaults( IPlatform platform ){
        try{
            Debug.enter();
            
            if( platform == null )
                return PlatformUtility.loadDefaultGeneral( getStore() );
                 
            return PlatformUtility.loadDefault( platform, getStore() );
        }
        finally{
            Debug.leave();
        }
    }

    @Override
    protected IPlatform[] getPlatforms(){
        return getManager().getPlatforms();
    }

    @Override
    protected void setIncludes( IPlatform platform, MakeInclude[] includes ){
        if( platform == null ){
            getManager().setDefaultMakeIncludes( includes );
            PlatformUtility.storeGeneral( includes, getStore() );
        }
        else{
            ((Platform)platform).setIncludes( includes );
            PlatformUtility.store( platform, includes, getStore() );
        }
    }

    private PlatformManager getManager(){
    	return Environment.getEnvironment().getPlatformManager();
    }
    
    private IPreferenceStore getStore(){
    	return TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore();
    }
}
