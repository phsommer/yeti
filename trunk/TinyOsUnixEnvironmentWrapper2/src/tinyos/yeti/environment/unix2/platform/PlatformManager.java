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
package tinyos.yeti.environment.unix2.platform;

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
import tinyos.yeti.environment.unix2.Environment;
import tinyos.yeti.environment.unix2.TinyOSUnixEnvironmentPlugin2;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.MakeInclude;

public class PlatformManager extends AbstractPlatformManager{
    private Environment environment;
    
    public PlatformManager( Environment environment ){
        this.environment = environment;
        setDefaultMakeIncludes( PlatformUtility.loadGeneral( getStore() ) );
    }
    
    @Override
    public void setDefaultMakeIncludes( MakeInclude[] defaultMakeIncludes ){
        boolean change = !Arrays.equals( defaultMakeIncludes, getDefaultMakeIncludes() );
        
        super.setDefaultMakeIncludes( defaultMakeIncludes );

        if( change ){
            IPlatform[] platforms = getCurrentPlatforms();
            if( platforms != null ){
                for( IPlatform platform : platforms ){
                    ((Platform)platform).fireMakeIncludesChanged();
                }
            }
        }
    }
    
    protected IPreferenceStore getStore(){
        return TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore();
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
    
    public IPlatform[] getPlatforms( String tosdir, String treeLayout ){
        return loadPlatforms( tosdir, treeLayout );
    }
    
    public String[] getArchitectures( String tosdir, String treeLayout ){
        IPlatform[] platforms = getPlatforms( tosdir, treeLayout );
        if( platforms == null )
            return new String[]{};
        
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
    
    @Override
    protected IPlatform[] loadPlatforms(){
        return loadPlatforms( null, null );
    }
    
    protected IPlatform[] loadPlatforms( String tosdir, String treeLayout ){
        File root = new File( environment.getPathManager().getPlatformDirectory( tosdir, treeLayout ));
        File[] candidates = guessPlatformDirectories( root );
        
        IPlatform[] platforms = new IPlatform[ candidates.length ];
        MMCUConverter converter = createDefaultMMCUConverter();
        
        for( int i = 0, n = candidates.length; i<n; i++ ){
            platforms[i] = new Platform( environment, candidates[i], root, converter );
        }
        
        return platforms;
    }
}
