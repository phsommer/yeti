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
package tinyos.yeti.environment.winXP.platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import tinyos.yeti.environment.basic.path.IPlatformFile;
import tinyos.yeti.environment.basic.platform.AbstractPlatformManager;
import tinyos.yeti.environment.basic.platform.IExtendedPlatform;
import tinyos.yeti.environment.basic.platform.MMCUConverter;
import tinyos.yeti.environment.basic.platform.PlatformUtility;
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.environment.winXP.TinyOSWinXPEnvironmentWrapper;
import tinyos.yeti.environment.winXP.path.PathSetting;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.EnvironmentVariable;
import tinyos.yeti.make.MakeInclude;

public class PlatformManager extends AbstractPlatformManager{
    private Environment environment;
    
    public PlatformManager( Environment environment ){
        this.environment = environment;
        super.setDefaultMakeIncludes( PlatformUtility.loadGeneral( getStore() ) );
        super.setDefaultVariables( PlatformUtility.loadGeneralEnvironmentVariables( getStore() ) );
    }
    
    @Override
    public void setDefaultMakeIncludes( MakeInclude[] defaultMakeIncludes ){
        boolean change = !Arrays.equals( defaultMakeIncludes, getDefaultMakeIncludes() );
        
        super.setDefaultMakeIncludes( defaultMakeIncludes );

        if( change ){
            PlatformUtility.storeGeneral( defaultMakeIncludes, getStore() );
            IPlatform[] platforms = getCurrentPlatforms();
            if( platforms != null ){
                for( IPlatform platform : platforms ){
                    ((Platform)platform).fireMakeIncludesChanged();
                }
            }
        }
    }
    
    @Override
    public void setDefaultVariables( EnvironmentVariable[] defaultVariables ){
    	boolean change = !Arrays.equals( defaultVariables, getDefaultEnvironmentVariables() );
    	super.setDefaultVariables( defaultVariables );
    	if( change ){
    		PlatformUtility.storeGeneral( defaultVariables, getStore() );
    	}
    }
    
    public Platform[] getPlatformsByArchitecture( String architecture ){
    	List<Platform> platforms = new ArrayList<Platform>();
    	
    	for( IPlatform iPlatform : getPlatforms() ){
    		Platform platform = (Platform)iPlatform;
    	    IPlatformFile platformFile = platform.getPlatformFile();
    	    String platformArchitecture = platformFile == null ? null : platformFile.getArchitecture();
    	    
    	    if( (architecture == null && platformArchitecture == null) || 
    	    		(architecture != null && architecture.equals( platformArchitecture ))){
    	    	platforms.add( platform );
    	    }
    	}
    	
    	return platforms.toArray( new Platform[ platforms.size() ]);
    }
    

    public String[] getArchitectures( PathSetting setting ){
        IPlatform[] platforms = getPlatforms( setting );
        if( platforms == null )
            return new String[]{ null };
        
        Set<String> result = new HashSet<String>();
        for( IPlatform platform : platforms ){
            IPlatformFile platformFile = ((IExtendedPlatform)platform).getPlatformFile();
            String architecture = platformFile == null ? null : platformFile.getArchitecture();
            if( architecture != null ){
                result.add( architecture );
            }
        }
        
        String[] array = result.toArray( new String[ result.size() ] );
        Arrays.sort( array );
        return array;
    }
    
    
    protected IPreferenceStore getStore(){
        return TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore();
    }
    

    @Override
    protected IPlatform[] loadPlatforms(){
        return loadPlatforms( new PathSetting() );
    }

    public IPlatform[] getPlatforms( PathSetting setting ){
        return loadPlatforms( setting );
    }
    
    protected IPlatform[] loadPlatforms( PathSetting setting ){
        if( setting.getTranslator() == null )
            setting.setTranslator( environment );
        
        String path = environment.getPathManager().getPlatformDirectory( setting.getTosdir(), setting.getTreeLayout() );
        File root = setting.getTranslator().modelToSystem( path );
        if( root == null ){
            return new IPlatform[]{};
        }
        
        File[] candidates = guessPlatformDirectories( root );
        
        IPlatform[] platforms = new IPlatform[ candidates.length ];
        MMCUConverter converter = createDefaultMMCUConverter();
        IPreferenceStore store = getStore();
        
        for( int i = 0, n = candidates.length; i<n; i++ ){
            platforms[i] = new Platform( environment, null, candidates[i], root, converter, store );
        }
        
        return platforms;
    }
}
