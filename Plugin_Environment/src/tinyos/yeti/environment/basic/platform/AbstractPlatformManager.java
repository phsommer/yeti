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
package tinyos.yeti.environment.basic.platform;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.path.IPlatformFile;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.make.EnvironmentVariable;
import tinyos.yeti.make.MakeInclude;

/**
 * An {@link IPlatformManager} that loads its platforms lazily.
 * @author Benjamin Sigg
 */
public abstract class AbstractPlatformManager implements IPlatformManager{
    private IPlatform[] platforms;
    private MakeInclude[] defaultMakeIncludes;
    private EnvironmentVariable[] defaultVariables;
    private List<MMCUConverter> mmcuConverters = new ArrayList<MMCUConverter>();
    
    public AbstractPlatformManager(){
    	mmcuConverters = TinyOSAbstractEnvironmentPlugin.loadMMCUConverters();
    }
    
    public IPlatform[] getPlatforms(){
        if( platforms == null ){
            synchronized( this ){
                if( platforms == null ){
                    platforms = loadPlatforms();
                    if( platforms == null )
                        platforms = new IPlatform[]{};
                }
                
                Arrays.sort( platforms, new Comparator<IPlatform>(){
                    private Collator collator = Collator.getInstance();
                    
                    public int compare( IPlatform o1, IPlatform o2 ){
                        return collator.compare( o1.getName(), o2.getName() );
                    } 
                });
            }
        }
        return platforms;
    }
    
    /**
     * Deletes the current set of platforms.
     */
    public void clearPlatforms(){
        platforms = null;
    }
    
    protected IPlatform[] getCurrentPlatforms(){
        return platforms;
    }
    
    public IMacro[] convertMMCU( IPlatformFile file ){
    	for( MMCUConverter converter : mmcuConverters ){
    		if( converter.interested( file )){
    			return converter.convert( file );
    		}
    	}
    	return null;
    }
    
    /**
     * Creates a new {@link MMCUConverter} that can convert almost every
     * mmcu. Can be used as argument for the constructor of {@link AbstractPlatform}.
     * @return the converter
     */
    public MMCUConverter createDefaultMMCUConverter(){
    	return new MMCUConverter(){
    		public boolean interested( IPlatformFile file ){
    			for( MMCUConverter converter : mmcuConverters ){
    				if( converter.interested( file ))
    					return true;
    			}
    			return false;
    		}
    		
    		public IMacro[] convert( IPlatformFile file ){
    			for( MMCUConverter converter : mmcuConverters ){
    				if( converter.interested( file ))
    					return converter.convert( file );
    			}
    			return null;
    		}
    	};
    }
    
    protected abstract IPlatform[] loadPlatforms();
    
    /**
     * Beginning at <code>directory</code>, this method searches recursively
     * for directories which contain a <code>.platform</code> file. If such
     * a file is found, the search stops and the subtree is ignored. All the
     * directories with such a file are returned.
     * @param directory the directory to search in
     * @return a list of directories which might be platforms
     */
    protected File[] guessPlatformDirectories( File directory ){
        List<File> result = new ArrayList<File>();
        guessPlatformDirectories( directory, result );
        return result.toArray( new File[ result.size() ] );
    }
    
    private void guessPlatformDirectories( File directory, List<File> result ){
        if( directory.isDirectory() ){
            File check = new File( directory, ".platform" );
            if( check.exists() ){
                result.add( directory );
            }
            else{
                File[] children = directory.listFiles();
                if( children != null ){
                    for( File child : children ){
                        guessPlatformDirectories( child, result );
                    }
                }
            }
        }
    }
    
    public void setDefaultMakeIncludes( MakeInclude[] defaultMakeIncludes ){
        this.defaultMakeIncludes = defaultMakeIncludes;
    }
    
    public MakeInclude[] getDefaultMakeIncludes(){
        return defaultMakeIncludes;
    }
    
    public void setDefaultVariables( EnvironmentVariable[] defaultVariables ){
		this.defaultVariables = defaultVariables;
	}
    
    public EnvironmentVariable[] getDefaultEnvironmentVariables(){
	    return defaultVariables;
    }
}
