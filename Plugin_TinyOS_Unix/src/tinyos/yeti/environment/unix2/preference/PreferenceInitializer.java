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
package tinyos.yeti.environment.unix2.preference;

import java.util.Arrays;
import java.util.Collection;
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
import tinyos.yeti.environment.basic.commands.Echo;
import tinyos.yeti.environment.basic.path.AbstractPathManager;
import tinyos.yeti.environment.unix2.Environment;
import tinyos.yeti.environment.unix2.TinyOSUnixEnvironmentPlugin2;
import tinyos.yeti.environment.unix2.executable.CommandExecuter;
import tinyos.yeti.utility.FileUtility;

public class PreferenceInitializer extends AbstractPreferenceInitializer{
    public static final String TREE_LAYOUT = "tos.treeLayout";
    
    public static final String TOSAPPS = "tos.apps";
    public static final String TOSDIR = "tos.dir";
    public static final String TOSROOT = "tos.root";
    public static final String MAKERULES = "tos.makerules";

    public static final String GCC_INCLUDES = "includes.gcc";
    public static final String ARCHITECTURE = "architecture";

    public static final String CHECK_ENV_EVER = "do.not.ask.environment.variables";
    
    private static final Job setDefaultsJob = new SetDefaultsJob();

    public static void addAllKeys( Collection<String> keys ){
    	keys.add( TREE_LAYOUT );
    	keys.add( TOSAPPS );
    	keys.add( TOSDIR );
    	keys.add( TOSROOT );
    	keys.add( MAKERULES );
    	keys.add( GCC_INCLUDES );
    	keys.add( ARCHITECTURE );
    }
    
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore();

        store.setDefault( TREE_LAYOUT, AbstractPathManager.TREE_TINYOS_2X );
        
        store.setDefault( TOSROOT, tryRead( "TOSROOT", "/opt/tinyos-2.x" ) );
        store.setDefault( TOSDIR, tryRead( "TOSDIR", "/opt/tinyos-2.x/tos" ) );
        store.setDefault( TOSAPPS, mergePath( store.getDefaultString( TOSROOT ), "apps" ) );
        store.setDefault( MAKERULES, tryRead( "MAKERULES", "/opt/tinyos-2.x/support/make/Makerules" ) );
        
        store.setDefault( GCC_INCLUDES, "" );
        store.setDefault( ARCHITECTURE, "" );
        
        store.setDefault( CHECK_ENV_EVER, false );
      
        store.addPropertyChangeListener( new IPropertyChangeListener(){
            public void propertyChange( PropertyChangeEvent event ){
                if( event.getProperty().equals( TREE_LAYOUT ) || event.getProperty().equals( TOSAPPS ) || event.getProperty().equals( TOSDIR ) || event.getProperty().equals( TOSROOT )){
                    setDefaultsJob.schedule( 250 );
                }
            }
        });
    }
    
    private String tryRead( String variable, String backup ){
    	try{
    		CommandExecuter commander = new CommandExecuter( null );
    		Echo echo = new Echo( "$" + variable, true );
    		String result = commander.execute( echo );
    		if( result == null || result.equals( "" ) )
    			return backup;
    		
    		return result;
    	}
    	catch( Exception ex ){
    		TinyOSUnixEnvironmentPlugin2.getDefault().getLog().log(
    				new Status( IStatus.ERROR, TinyOSUnixEnvironmentPlugin2.PLUGIN_ID, ex.getMessage(), ex ) );
    		
    		return backup;
    	}
    }
    
    private String mergePath( String root, String append ){
    	if( root.endsWith( "/" ))
    		return root + append;
    	else
    		return root + "/" + append;
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

    public static void resetArchitectureDefaultPaths( IProgressMonitor monitor, String tosroot, String tosdir, String makerules, String treeLayout ){
        resetArchitectureDefaultPaths( monitor, null, tosroot, tosdir, makerules, treeLayout, null );
    }

    /**
     * Recalculates the default paths of <code>architectures</code>.
     * @param monitor to report progress, can be <code>null</code>
     * @param architectures the architectures to update, can be <code>null</code> to update all known architectures
     * @param tosroot environment variable TOSROOT, can be <code>null</code>
     * @param tosdir environment variable TOSDIR, can be <code>null</code>
     * @param makerules environment variable MAKERULES, can be <code>null</code>
     * @param treeLayout the layout of the tos tree
     * @param originals map into which the original values of the preference store will
     * be written. No value within this map will be overridden, can be <code>null</code>
     */
    public static void resetArchitectureDefaultPaths( IProgressMonitor monitor, 
            String[] architectures, String tosroot, String tosdir, String makerules, String treeLayout, Map<String, String> originals ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Set Default Paths", 100 );

        IPreferenceStore store = TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore();

        if( architectures == null ){
            String[] paths = Environment.getEnvironment().getPathManager().getIncludeSuggestions( tosdir, tosroot, makerules, treeLayout, null );
            if( paths == null )
                setDefault( store, getArchitecturePathKey( null, false ), "", originals );
            else
                setDefault( store, getArchitecturePathKey( null, false ), FileUtility.putPathsTogether( paths ), originals );
        }
        monitor.worked( 10 );

        if( architectures == null )
            architectures = Environment.getEnvironment().getPlatformManager().getArchitectures( tosdir, treeLayout );

        SubProgressMonitor sub = new SubProgressMonitor( monitor, 90 );

        if( architectures != null ){
            sub.beginTask( "Architecture Paths", architectures.length );
            for( String architecture : architectures ){
                String[] paths = Environment.getEnvironment().getPathManager().getIncludeSuggestions( tosdir, tosroot, makerules, treeLayout, architecture );
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
    
    private static void setDefault( IPreferenceStore store, String key, String value, Map<String, String> originals ){
        if( originals != null && !originals.containsKey( key )){
            originals.put( key, store.getDefaultString( key ) );
        }
        store.setDefault( key, value );
    }
    
    public static void setDefaults( Map<String,String> originals ){
        IPreferenceStore store = TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore();
        for( Map.Entry<String, String> entry : originals.entrySet() ){
            store.setDefault( entry.getKey(), entry.getValue() );
        }
    }

    private static class SetDefaultsJob extends Job{
        public SetDefaultsJob(){
            super( "Initialize TinyOS 2.x unix environment" );
            setPriority( Job.LONG );
        }

        @Override
        protected IStatus run( IProgressMonitor monitor ){
            Debug.enter();
            Debug.info( "Unix: Set Default Paths" );
            
            IPreferenceStore store = TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore();
            String makerules = store.getString( MAKERULES );
            String tosdir = store.getString( TOSDIR );
            String tosroot = store.getString( TOSROOT );
            String treeLayout = store.getString( TREE_LAYOUT );
            
            resetArchitectureDefaultPaths( monitor, tosroot, tosdir, makerules, treeLayout );
            
            Debug.leave();
            if( monitor.isCanceled() )
                return Status.CANCEL_STATUS;
            
            return Status.OK_STATUS;
        }
    }
}
