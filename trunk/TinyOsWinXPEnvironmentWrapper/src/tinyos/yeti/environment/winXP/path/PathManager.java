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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.environment.winXP.TinyOSWinXPEnvironmentWrapper;
import tinyos.yeti.environment.winXP.execute.GetIncludeDirectories;
import tinyos.yeti.environment.winXP.preference.PreferenceInitializer;
import tinyos.yeti.ep.IPlatform;

public class PathManager extends AbstractPathManager {
    private Environment environment;
    private IWinPathTranslator translator;
    
    private File cygwinBash;
    private File cygwinCygpath;
    private File cygwinRoot;
    
    private String tosroot;
    private String tosdir;
    private String tosapps;
    private String makerules;
    private String treeLayout;
    
    public PathManager( Environment environment ){
        this.environment = environment;
        
        getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener(){
            public void propertyChange( PropertyChangeEvent event ) {
                String key = event.getProperty();
                if( PreferenceInitializer.CYGWIN_BASH.equals( key ) ||
                        PreferenceInitializer.CYGWIN_CYGPATH.equals( key ) ||
                        PreferenceInitializer.CYGWIN_ROOT.equals( key ) ||
                        PreferenceInitializer.TREE_LAYOUT.equals( key ) ||
                        PreferenceInitializer.MAKERULES.equals( key ) ||
                        PreferenceInitializer.TOSAPPS.equals( key ) ||
                        PreferenceInitializer.TOSDIR.equals( key ) ||
                        PreferenceInitializer.TOSROOT.equals( key ) ||
                        PreferenceInitializer.TRANSLATOR.equals( key )){
                    
                    initVariables( true );
                }
            }
        });
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener( new IResourceChangeListener(){
            public void resourceChanged( IResourceChangeEvent event ) {
                changed( event.getDelta() );
            }
        }, IResourceChangeEvent.POST_CHANGE );
        
        initVariables( false );
    }
    
    /**
     * Checks whether a file has been added or removed (or renamed) and if
     * so calls {@link IWinPathTranslator#notifyResourceChanged()}
     * @param delta the changes
     */
    private void changed( IResourceDelta delta ){
        try {
            delta.accept( new IResourceDeltaVisitor(){
                private boolean changed = false;
                
                public boolean visit( IResourceDelta delta ) throws CoreException {
                    if( changed ){
                        return false;
                    }
                    
                    if( delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED ){
                        changed = true;
                    }
                    else if( delta.getKind() == IResourceDelta.CHANGED ){
                        int check = IResourceDelta.COPIED_FROM |
                            IResourceDelta.MOVED_FROM | 
                            IResourceDelta.MOVED_TO;
                        
                        if( (delta.getFlags() & check) != 0 ){
                            changed = true;
                        }
                    }
                    
                    if( changed ){
                        translator.notifyResourceChanged();
                        return false;
                    }
                    
                    return true;
                }
            });
        }
        catch( CoreException e ) {
            // just be on the safe side...
            translator.notifyResourceChanged();
            
            TinyOSWinXPEnvironmentWrapper.warning( e );
        }
    }
    
    private void initVariables( boolean changed ){
        IPreferenceStore store = getPreferenceStore();
        
        tosroot = store.getString( PreferenceInitializer.TOSROOT );
        tosdir = store.getString( PreferenceInitializer.TOSDIR );
        tosapps = store.getString( PreferenceInitializer.TOSAPPS );
        makerules = store.getString( PreferenceInitializer.MAKERULES );
        treeLayout = store.getString( PreferenceInitializer.TREE_LAYOUT );
        
        cygwinRoot = new File( store.getString( PreferenceInitializer.CYGWIN_ROOT ));
        
        String cygwinBash = store.getString( PreferenceInitializer.CYGWIN_BASH );
        if( cygwinBash.length() == 0 )
            this.cygwinBash = getDefaultCygwinBash( cygwinRoot );
        else
            this.cygwinBash = new File( cygwinBash );
        
        String cygwinCygpath = store.getString( PreferenceInitializer.CYGWIN_CYGPATH );
        if( cygwinCygpath.length() == 0 )
            this.cygwinCygpath = getDefaultCygwinCygpath( cygwinRoot );
        else
            this.cygwinCygpath = new File( cygwinCygpath );
        
        translator = initTranslator();
        
        if( changed ){
            environment.fireReinitialized();
        }
    }
    
    private IWinPathTranslator initTranslator(){
        String kind = getPreferenceStore().getString( PreferenceInitializer.TRANSLATOR );
        IWinPathTranslator translator;
        
        File cygpath = getCygwinCygpath();
        if( cygpath == null ){
            translator = new NullTranslator();
        }
        else{
            translator = new CygpathPathTranslator( cygpath );
        }
        
        if( PreferenceInitializer.TRANSLATOR_VALUE_DATABASE.equals( kind )){
            translator = new DatabaseTranslator( translator );
        }
        
        return translator;
    }
    
    protected IPreferenceStore getPreferenceStore(){
        return TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore();
    }

    public File getCygwinBash(){
        return cygwinBash;
    }

    public File getCygwinCygpath(){
        return cygwinCygpath;
    }
    
    public File getCygwinRoot(){
        return cygwinRoot;
    }

    public static File getDefaultCygwinBash( File cygwinRoot ){
        if( cygwinRoot == null )
            return null;
        
        return new File( cygwinRoot, "bin/bash.exe" );
    }
    
    public static File getDefaultCygwinCygpath( File cygwinRoot ){
        if( cygwinRoot == null )
            return null;
        
        return new File( cygwinRoot, "bin/cygpath.exe" );
    }
    
    public String getBinDir(){
        return "/bin";
    }

    @Override
    public String getTreeLayout(){
        return treeLayout;
    }
    
    public String getTosRootPath(){
        return tosroot;
    }

    public String getTosDirectoryPath(){
        return tosdir;
    }

    public String getMakerules(){
        return makerules;
    }

    public String getExampleDirectory(){
        return tosapps;
    }

    public String getSensorPath(){
        return getTosDirectoryPath() + "/sensorboards";
    }

    public String systemToModel( File file ) {
        return systemToCygwin( file );
    }

    public String systemToCygwin( File file ){
        return translator.systemToModel( file );
    }

    public File modelToSystem( String file ) {
        return cygwinToSystem( file );
    }

    public File cygwinToSystem( String file ){
        return translator.modelToSystem( file );
    }

    @Override
    protected ICollectStep createStandardSearch() {
        return new MultiCollectStep( new AnnotsStageStep(), new TosHCollectStep(), new TypesStep() );
    }


    @Override
    public File[] getArchitectureIncludeDirectories( String architecture ){
        IPreferenceStore store = TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore();
        boolean override = store.getBoolean( PreferenceInitializer.getArchitectureOverrideKey( architecture ) );
        String result = store.getString( PreferenceInitializer.getArchitecturePathKey( architecture, override ) );
        if( "".equals( result ) || result == null ){
            return null;
        }
        return getFiles( result );
    }

    protected File[] getFiles( String path ){
        String[] results = path.split( File.pathSeparator );
        List<File> files = new ArrayList<File>();
        for( String check : results ){
            File file = modelToSystem( check );
            if( file != null && file.exists() ){
                files.add( file );
            }
        }
        return files.toArray( new File[ files.size() ] );
    }

    @Override
    protected File[] getGccIncludeDirectories(){
        IPreferenceStore store = TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore();
        String result = store.getString( PreferenceInitializer.GCC_INCLUDES );
        if( "".equals( result ))
            return null;
        return getFiles( result );
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
        m.put( "APP_MAKERULES", getMakerules() );
        return m;
    }

    public String getMakerulesPath() {
        return getMakerules();
    }

    public String[] getIncludeSuggestions( PathSetting setting, String architecture ){
        // search for a platform with the given architecture
        if( setting.getTranslator() == null )
            setting.setTranslator( this.translator );
        
        IPlatform[] platforms = environment.getPlatformManager().getPlatforms( setting );
        if( platforms == null )
            return new String[]{};

        for( IPlatform platform : platforms ){
            IExtendedPlatform extended = (IExtendedPlatform)platform;
            IPlatformFile platformFile = extended.getPlatformFile();
            if( platformFile != null ){
                String platformArchitecture = platformFile.getArchitecture();
                
                if( (architecture == null && platformArchitecture == null) || (architecture != null && architecture.equals( platformArchitecture )) ){

                    GetIncludeDirectories command = new GetIncludeDirectories( extended, tosdir, cygwinBash, cygwinRoot, translator, createReplacer( platform.getName() ) );
                    command.putEnvironmentParameter( "TOSDIR", setting.getTosdir() );
                    command.putEnvironmentParameter( "TOSROOT", setting.getTosroot() );
                    command.putEnvironmentParameter( "MAKERULES", setting.getMakerules() );

                    return environment.execute( command );
                }
            }
        }

        if( architecture == null ){
            return getIncludeSuggestions( setting, "pc" );
        }

        return new String[]{};
    }
}
