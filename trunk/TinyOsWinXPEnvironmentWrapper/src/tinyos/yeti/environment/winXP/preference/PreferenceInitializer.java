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

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import tinyos.yeti.Debug;
import tinyos.yeti.environment.basic.path.AbstractPathManager;
import tinyos.yeti.environment.basic.path.IPathTranslator;
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.environment.winXP.TinyOSWinXPEnvironmentWrapper;
import tinyos.yeti.environment.winXP.path.CygpathPathTranslator;
import tinyos.yeti.environment.winXP.path.DatabaseTranslator;
import tinyos.yeti.environment.winXP.path.PathManager;
import tinyos.yeti.environment.winXP.path.PathSetting;
import tinyos.yeti.utility.FileUtility;

public class PreferenceInitializer extends AbstractPreferenceInitializer{
    public static final String TREE_LAYOUT = "tos.tree";
    
    public static final String TOSAPPS = "tos.apps";
    public static final String TOSDIR = "tos.dir";
    public static final String TOSROOT = "tos.root";
    public static final String MAKERULES = "tos.makerules";
    public static final String CYGWIN_ROOT = "cygwin.root";
    public static final String CYGWIN_CYGPATH = "cygwin.cygpath";
    public static final String CYGWIN_BASH = "cygwin.bash";
    
    public static final String GCC_INCLUDES = "includes.gcc";
    public static final String ARCHITECTURE = "architecture";
    
    public static final String TRANSLATOR = "translator";
    public static final String TRANSLATOR_VALUE_CYGPATH = "cygpath";
    public static final String TRANSLATOR_VALUE_DATABASE = "database";
    
    private static final Job setDefaultsJob = new SetDefaultsJob();
    
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore();

        store.setDefault( TREE_LAYOUT, AbstractPathManager.TREE_TINYOS_2X );
        store.setDefault( TOSAPPS, "/opt/tinyos-2.x/apps" );
        store.setDefault( TOSDIR, "/opt/tinyos-2.x/tos" );
        store.setDefault( TOSROOT, "/opt/tinyos-2.x" );
        store.setDefault( MAKERULES,"/opt/tinyos-2.x/support/make/Makerules" );
        store.setDefault( CYGWIN_ROOT, "C:\\cygwin" );
        store.setDefault( CYGWIN_BASH, "" );
        store.setDefault( CYGWIN_CYGPATH, "" );
        store.setDefault( TRANSLATOR, TRANSLATOR_VALUE_DATABASE );
        
        store.setDefault( GCC_INCLUDES, "" );
        store.setDefault( ARCHITECTURE, "" );
        
        store.addPropertyChangeListener( new IPropertyChangeListener(){
            public void propertyChange( PropertyChangeEvent event ){
                if( event.getProperty().equals( TREE_LAYOUT ) || event.getProperty().equals( TOSAPPS ) || event.getProperty().equals( TOSDIR ) || event.getProperty().equals( TOSROOT ) ||
                        event.getProperty().equals( CYGWIN_BASH ) || event.getProperty().equals( CYGWIN_CYGPATH ) || event.getProperty().equals( CYGWIN_ROOT )){
                    setDefaultsJob.schedule( 250 );
                }
            }
        });
    }
    
    public static void scheduleDefaultsUpdate(){
        setDefaultsJob.schedule( 500 );
    }

    public static String getArchitecturePathKey( String architecture, boolean override ){
        if( architecture == null ){
            if( override ){
                return ARCHITECTURE + ".user";
            }
            else{
                return ARCHITECTURE + ".default";
            }
        }
        else{
            if( override ){
                return ARCHITECTURE + ".user." + architecture;
            }
            else{
                return ARCHITECTURE + ".default." + architecture;
            }
        }
    }
    
    public static String getArchitectureOverrideKey( String architecture ){
        if( architecture == null )
            return ARCHITECTURE + ".override";
        else
            return ARCHITECTURE + ".override." + architecture;
    }
    
    public static void resetArchitectureDefaultPaths( IProgressMonitor monitor, PathSetting setting ){
        resetArchitectureDefaultPaths( monitor, setting, null, null );
    }
    
    /**
     * Updates the default paths of the architectures <code>architectures</code>.
     * @param monitor to report progress, can be <code>null</code>
     * @param setting the paths to use, will be modified by this method
     * @param architectures the architectures to update or <code>null</code> to indicate that
     * all architectures should be updated
     * @param originals a map into which this method will write all the values
     * that are changed, this method will not override pairs that are already
     * in the map.
     */
    public static void resetArchitectureDefaultPaths( IProgressMonitor monitor,
            PathSetting setting, String[] architectures, Map<String, String> originals ){
        
        if( setting.getCygwinBash() == null )
            setting.setCygwinBash( PathManager.getDefaultCygwinBash( setting.getCygwinRoot() ) );
        
        if( setting.getTranslator() == null )
            setting.setTranslator( Environment.getEnvironment() );
        
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Set Default Paths", 100 );
        
        IPreferenceStore store = TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore();
        
        // update default architecture if all architectures are to be updated
        if( architectures == null ){
            String[] paths = Environment.getEnvironment().getPathManager().getIncludeSuggestions( setting, null );
            if( paths == null )
                setDefault( store, getArchitecturePathKey( null, false ), "", originals );
            else
                setDefault( store, getArchitecturePathKey( null, false ), FileUtility.putPathsTogether( paths ), originals );
        }
        
        monitor.worked( 10 );

        if( architectures == null )
            architectures = Environment.getEnvironment().getPlatformManager().getArchitectures( setting );
        
        SubProgressMonitor sub = new SubProgressMonitor( monitor, 90 );
        
        if( architectures != null ){
            sub.beginTask( "Architecture Paths", architectures.length );
            for( String architecture : architectures ){
                String[] paths = Environment.getEnvironment().getPathManager().getIncludeSuggestions( setting, architecture );
                if( Debug.DEBUG ){
                    Debug.info( "architecture '" + architecture + "' = " + Arrays.toString( paths ) );
                }
                
                if( paths == null )
                    setDefault( store, getArchitecturePathKey( architecture, false ), "", originals );
                else
                    setDefault( store, getArchitecturePathKey( architecture, false ), FileUtility.putPathsTogether( paths ), originals );
                
                sub.worked( 1 );
            }
            sub.done();
        }
        
        Environment.getEnvironment().setStartedUp();
        monitor.done();
    }
    
    /**
     * Intended to be used together with 
     * {@link #resetArchitectureDefaultPaths(IProgressMonitor, PathSetting, String[], Map)}
     * unmades any changes made by that method.
     * @param originals the original values
     */
    public static void setDefaults( Map<String, String> originals ){
        IPreferenceStore store = TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore();
        for( Map.Entry<String, String> entry : originals.entrySet() ){
            store.setDefault( entry.getKey(), entry.getValue() );
        }
    }

    private static void setDefault( IPreferenceStore store, String key, String value, Map<String, String> originals ){
        if( originals != null && !originals.containsKey( key )){
            String originalValue = store.getDefaultString( key );
            originals.put( key, originalValue );
        }
            
        store.setDefault( key, value );
    }
    
    private static class SetDefaultsJob extends Job{
        public SetDefaultsJob(){
            super( "Initialize TinyOS 2.x Windows einvironment");
            setPriority( Job.LONG );
        }
        
        @Override
        protected IStatus run( IProgressMonitor monitor ){
            IPreferenceStore store = TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore();
            String makerules = store.getString( MAKERULES );
            String tosdir = store.getString( TOSDIR );
            String tosroot = store.getString( TOSROOT );
            String treeLayout = store.getString( TREE_LAYOUT );
            
            File cygwinRoot = new File( store.getString( CYGWIN_ROOT ));
            
            String cygwinBashString = store.getString( CYGWIN_BASH );
            File cygwinBash;
            if( cygwinBashString.length() > 0 ){
                cygwinBash = new File( cygwinBashString );
            }
            else{
                cygwinBash = PathManager.getDefaultCygwinBash( cygwinRoot );
            }
            
            IPathTranslator translator;
            String kind = store.getString( TRANSLATOR );
            
            String cygwinCygpathString = store.getString( CYGWIN_CYGPATH );
            File cygwinCygpath;
            if( cygwinCygpathString.length() > 0 ){
                cygwinCygpath = new File( cygwinCygpathString );
            }
            else{
                cygwinCygpath = PathManager.getDefaultCygwinCygpath( cygwinRoot );
            }
            
            translator = new CygpathPathTranslator( cygwinCygpath );
         
            if( TRANSLATOR_VALUE_DATABASE.equals( kind )){
                translator = new DatabaseTranslator( translator );
            }
            
            resetArchitectureDefaultPaths( monitor, new PathSetting( tosroot, tosdir, makerules, treeLayout, cygwinRoot, cygwinBash, translator ));
            return Status.OK_STATUS;
        }
    }
}
