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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

import tinyos.yeti.environment.basic.path.IPathTranslator;
import tinyos.yeti.environment.basic.preferences.AbstractEnvironmentPreferencePage;
import tinyos.yeti.environment.unix2.Environment;
import tinyos.yeti.environment.unix2.TinyOSUnixEnvironmentPlugin2;
import tinyos.yeti.environment.unix2.path.PathManager;
import tinyos.yeti.environment.unix2.platform.Platform;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.widgets.BooleanFieldEditor2;

public class EnvironmentPreferencePage extends AbstractEnvironmentPreferencePage{
    /** original default values of the preference store, to be reset in case of cancel */
    private Map<String, String> originals = new HashMap<String, String>();

    public EnvironmentPreferencePage(){
        setPreferenceStore( TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore() );
        setDescription( "Files and directories needed to work with a TinyOS 2.x application." );
        setTitle( "TinyOS 2.x unix-environment" );
    }

    @Override
    protected String getStoreKey( StoreKey key ){
        switch( key ){
            case GCC_INCLUDES: return PreferenceInitializer.GCC_INCLUDES;
            case MAKERULES: return PreferenceInitializer.MAKERULES;
            case TOSAPPS: return PreferenceInitializer.TOSAPPS;
            case TOSDIR: return PreferenceInitializer.TOSDIR;
            case TOSROOT: return PreferenceInitializer.TOSROOT;
            case TREE: return PreferenceInitializer.TREE_LAYOUT;
            default: throw new IllegalArgumentException( "unknown key: " + key );
        }
    }

    @Override
    protected void createFieldEditors(){
    	BooleanFieldEditor2 checkVariables = new BooleanFieldEditor2( PreferenceInitializer.CHECK_ENV_EVER, "Check environment variables", getFieldEditorParent() );
    	String tooltip = "If selected the environment variables known to Eclipse and to the shell will be compared before launching or debugging an application. A warning message is shown if they are not the same.";
    	checkVariables.getChangeControl( null ).setToolTipText( tooltip );
    	addField( checkVariables );
    	
    	super.createFieldEditors();
    }
    
    @Override
    public boolean performOk() {
        originals.clear();
        return super.performOk();
    }

    @Override
    public boolean performCancel() {
        PreferenceInitializer.setDefaults( originals );
        return super.performCancel();
    }

    @Override
    protected IEnvironment getEnvironment() {
        return Environment.getEnvironment();
    }

    @Override
    protected IPathTranslator getPathTranslator() {
        return Environment.getEnvironment();
    }

    @Override
    protected String[] getVisibleArchitectures() {
    	String tosdir = getTosdir();
    	String treeLayout = getTreeLayout();
    	if( tosdir == null || treeLayout == null )
    		return null;
    	
        return Environment.getEnvironment().getPlatformManager().getArchitectures( tosdir, treeLayout );
    }

    @Override
    protected void resetArchitectureDefaultsPaths( String[] architectures, IProgressMonitor monitor ) {
        String[] array = null;
        if( architectures != null ){
            array = new String[ architectures.length+1 ];
            System.arraycopy( architectures, 0, array, 1, architectures.length );
        }
        PreferenceInitializer.resetArchitectureDefaultPaths( monitor, array, getTosroot(), getTosdir(), getMakerules(), getTreeLayout(), originals );
    }

    @Override
    protected String getArchitecturePath( String architecture, boolean override ){
        return getPreferenceStore().getString( PreferenceInitializer.getArchitecturePathKey( architecture, override ) );
    }

    @Override
    protected boolean isArchitectureOverriden( String architecture ){
        return getPreferenceStore().getBoolean( PreferenceInitializer.getArchitectureOverrideKey( architecture ) );
    }

    @Override
    protected void storeArchitecture( String architecture, boolean override, String userPath ){
        PathManager pathManager = Environment.getEnvironment().getPathManager();
        File[] oldFiles = pathManager.getArchitectureIncludeDirectories( architecture );

        IPreferenceStore store = getPreferenceStore();
        store.setValue( PreferenceInitializer.getArchitectureOverrideKey( architecture ), override );
        store.setValue( PreferenceInitializer.getArchitecturePathKey( architecture, true ), userPath );

        File[] newFiles = pathManager.getArchitectureIncludeDirectories( architecture );
        if( !Arrays.equals( oldFiles, newFiles )){
            Platform[] platforms = Environment.getEnvironment().getPlatformManager().getPlatformsByArchitecture( architecture );
            for( Platform platform : platforms ){
                platform.firePathsChanged();
            }
        }
    }
}
