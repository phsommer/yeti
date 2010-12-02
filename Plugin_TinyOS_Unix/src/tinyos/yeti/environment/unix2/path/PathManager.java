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
package tinyos.yeti.environment.unix2.path;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import tinyos.yeti.environment.basic.path.AbstractPathManager;
import tinyos.yeti.environment.basic.path.IPlatformFile;
import tinyos.yeti.environment.basic.path.steps.AnnotsStageStep;
import tinyos.yeti.environment.basic.path.steps.ICollectStep;
import tinyos.yeti.environment.basic.path.steps.MultiCollectStep;
import tinyos.yeti.environment.basic.path.steps.TosHCollectStep;
import tinyos.yeti.environment.basic.path.steps.TypesStep;
import tinyos.yeti.environment.basic.platform.IExtendedPlatform;
import tinyos.yeti.environment.unix2.Environment;
import tinyos.yeti.environment.unix2.TinyOSUnixEnvironmentPlugin2;
import tinyos.yeti.environment.unix2.executable.GetIncludeDirectories;
import tinyos.yeti.environment.unix2.preference.PreferenceInitializer;
import tinyos.yeti.ep.IPlatform;

public class PathManager extends AbstractPathManager{
    private String tosroot;
    private String tosdir;
    private String tosapps;
    private String makerules;
    
    private String treeLayout;
    
    private Environment environment;
    
    public PathManager( Environment environment ){
        this.environment = environment;
        
        TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener(){
            public void propertyChange( PropertyChangeEvent event ) {
                String key = event.getProperty();
                if( PreferenceInitializer.TOSROOT.equals( key ) ||
                        PreferenceInitializer.TOSDIR.equals( key ) ||
                        PreferenceInitializer.TOSAPPS.equals( key ) ||
                        PreferenceInitializer.MAKERULES.equals( key ) ||
                        PreferenceInitializer.TREE_LAYOUT.equals( key )){
                    
                    updatePreferences( true );
                }
            }
        });
        
        updatePreferences( false );
    }
    
    private void updatePreferences( boolean changed ){
        IPreferenceStore store = TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore();
        
        tosroot = store.getString( PreferenceInitializer.TOSROOT );
        tosdir = store.getString( PreferenceInitializer.TOSDIR );
        tosapps = store.getString( PreferenceInitializer.TOSAPPS );
        makerules = store.getString( PreferenceInitializer.MAKERULES );
        treeLayout = store.getString( PreferenceInitializer.TREE_LAYOUT );
        
        if( changed ){
            environment.fireReinitialized();
        }
    }
    
    public boolean areSamePaths( String tosroot, String tosdir, String tosapps, String tosrules ){
        return tosroot.equals( this.tosroot ) && tosdir.equals( this.tosdir ) && tosapps.equals( this.tosapps ) && tosrules.equals( makerules );
    }
     
    @Override
    protected ICollectStep createStandardSearch(){
        return new MultiCollectStep( 
                new AnnotsStageStep(), 
                new TosHCollectStep(),
                new TypesStep() );
    }
    
    public File modelToSystem( String file ) {
        return new File( file );
    }
    
    public String systemToModel( File file ) {
        return file.getPath();
    }
    
    public String getMakerulesPath(){
        return makerules;
    }
    
    public String getTosRootPath(){
        return tosroot;
    }
    
    public String getTosDirectoryPath(){
        return tosdir;
    }
    
    @Override
    public String getTreeLayout(){
        return treeLayout;
    }
    
    public String getPlatformPath( String platform ){
        return getPlatformDirectory() + "/" + platform;
    }
    
    public String getSensorPath(){
        return getTosDirectoryPath() + "/sensorboards";
    }
    
    public String getAppDir(){
        return tosapps;
    }
    
    @Override
    public File[] getArchitectureIncludeDirectories( String architecture ){
    	TinyOSUnixEnvironmentPlugin2 plugin = TinyOSUnixEnvironmentPlugin2.getDefault();
    	if( plugin == null )
    		return null;
    	
        IPreferenceStore store = plugin.getPreferenceStore();
        boolean override = store.getBoolean( PreferenceInitializer.getArchitectureOverrideKey( architecture ) );
        String result = store.getString( PreferenceInitializer.getArchitecturePathKey( architecture, override ) );
        if( "".equals( result ) || result == null ){
            return null;
        }
        return getFiles( result );
    }
    
    @Override
    protected File[] getGccIncludeDirectories(){
        IPreferenceStore store = TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore();
        String result = store.getString( PreferenceInitializer.GCC_INCLUDES );
        if( "".equals( result ))
            return null;
        return getFiles( result );
    }
    
    protected File[] getFiles( String path ){
        String[] results = path.split( File.pathSeparator );
        List<File> files = new ArrayList<File>();
        for( String check : results ){
            File file = new File( check );
            if( file.exists() ){
                files.add( file );
            }
        }
        return files.toArray( new File[ files.size() ] );
    }
    
    @Override
    protected IExtendedPlatform getPlatform( String name ) {
        IPlatform[] platforms = environment.getPlatforms();
        if( platforms == null )
            return null;
        
        for( IPlatform platform : platforms ){
            if( platform.getName().equals( name ))
                return (IExtendedPlatform)platform;
        }
        
        return null;
    }

    public Map<String, String> getEnvironmentVariables(){
        Map<String, String> m = new HashMap<String, String>();
        m.put( "TOSDIR", getTosDirectoryPath() );
        m.put( "TOSROOT", getTosRootPath() );
        m.put( "MAKERULES", getMakerulesPath() );
        return m;
    }
    
    public String[] getIncludeSuggestions( String tosdir, String tosroot, String makerules, String treeLayout, String architecture ){
        // search for a platform with the given architecture
        IPlatform[] platforms = environment.getPlatformManager().getPlatforms( tosdir, treeLayout );
        if( platforms == null )
            return new String[]{};
        
        for( IPlatform platform : platforms ){
            IExtendedPlatform extended = (IExtendedPlatform)platform;
            IPlatformFile platformFile = extended.getPlatformFile();
            if( platformFile != null ){
                String platformArchitecture = platformFile.getArchitecture();
                if( (architecture == null && platformArchitecture == null) || (architecture != null && architecture.equals( platformArchitecture )) ){

                    GetIncludeDirectories command = new GetIncludeDirectories( extended, tosdir, createReplacer( platform.getName() ) );
                    command.putEnvironmentParameter( "TOSDIR", tosdir );
                    command.putEnvironmentParameter( "TOSROOT", tosroot );
                    command.putEnvironmentParameter( "MAKERULES", makerules );
                    
                    return environment.execute( command );
                }
            }
        }
        
        if( architecture == null ){
            return getIncludeSuggestions( tosdir, tosroot, makerules, treeLayout, "pc" );
        }
        
        return new String[]{};
    }
}
