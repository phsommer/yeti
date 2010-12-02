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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;

import tinyos.yeti.environment.basic.path.IPathTranslator;
import tinyos.yeti.environment.basic.preferences.AbstractEnvironmentPreferencePage;
import tinyos.yeti.environment.basic.preferences.widgets.ComboFieldEditor;
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.environment.winXP.TinyOSWinXPEnvironmentWrapper;
import tinyos.yeti.environment.winXP.path.*;
import tinyos.yeti.environment.winXP.platform.Platform;
import tinyos.yeti.ep.IEnvironment;

public class EnvironmentPreferencePage extends AbstractEnvironmentPreferencePage{
    private DirectoryFieldEditor cygwinRoot;
    private FileFieldEditor cygwinBash;
    private FileFieldEditor cygwinCygpath;
    private ComboFieldEditor translator;
    
    private Map<String, String> originals = new HashMap<String, String>();
	
    private IPathTranslator pathTranslator = new IPathTranslator(){
        // Note: always create a new translator to be sure using the most current one
        public String systemToModel( File file ) {
            return getTranslator().systemToModel( file );
        }
        public File modelToSystem( String file ) {
            return getTranslator().modelToSystem( file );
        }
    };
    
    public EnvironmentPreferencePage(){
        setPreferenceStore( TinyOSWinXPEnvironmentWrapper.getDefault().getPreferenceStore() );
        setDescription( "Files and directories needed to work with a TinyOS 2.x application.\nNote that changes on the paths will lead to a rebuild." );
        
        setTitle( "TinyOS 2.x win-environment" );
    }
    
    @Override
    protected void createFieldEditors() {
    	cygwinRoot = new DirectoryFieldEditor( PreferenceInitializer.CYGWIN_ROOT, "Cygwin Root Directory", getFieldEditorParent() );
    	setTooltip( cygwinRoot, "The directory in which cygwin is installed." );
    	addField( cygwinRoot );
    	
    	cygwinBash = new FileFieldEditor( PreferenceInitializer.CYGWIN_BASH, "Cygwin Bash", getFieldEditorParent() );
    	cygwinBash.setEmptyStringAllowed( true );
    	setTooltip( cygwinBash, "The executable file which starts cygwin, normally something like 'Cygwin Root Directory\\bin\\bash.exe'. Leave empty to use default path." );
    	addField( cygwinBash );
    	
    	cygwinCygpath = new FileFieldEditor( PreferenceInitializer.CYGWIN_CYGPATH, "Cygwin Cygpath", getFieldEditorParent() );
    	cygwinCygpath.setEmptyStringAllowed( true );
        setTooltip( cygwinBash, "The executable file which translates paths from cygwin to windows, normally something like 'Cygwin Root Directory\\bin\\cygpath.exe'. Leave empty to use default path." );
        addField( cygwinCygpath );
        
        translator = new ComboFieldEditor( PreferenceInitializer.TRANSLATOR, 
                "Path translation", 
                new String[][]{ { "Cygpath", PreferenceInitializer.TRANSLATOR_VALUE_CYGPATH },
                                { "Database", PreferenceInitializer.TRANSLATOR_VALUE_DATABASE }},
                getFieldEditorParent() );
    	translator.getLabelControl( getFieldEditorParent() ).setToolTipText(
    	        "How to convert file- and directory-paths from cygwin to Windows or from Windows to cygwin.\n" +
    	        "Cygpath: use only 'cygpath', slow but secure.\n" +
    	        "Database: build up a database with translations, fast but might suffer from synchronisation problems." );
    	addField( translator );
    	
    	
    	linkEditor( cygwinRoot );
    	linkEditor( cygwinBash );
    	linkEditor( cygwinCygpath );
    	
    	super.createFieldEditors();
    }
    
    public File getCygwinRoot(){
    	return synchron( new Invocation<File>(){
    		public File run() {
    			return new File( cygwinRoot.getStringValue() );
    		}
    	});
    }
    
    public File getCygwinCygpath(){
        return synchron( new Invocation<File>(){
            public File run() {
                String path = cygwinCygpath.getStringValue();
                if( path.length() == 0 )
                    return null;
                return new File( path );
            }
        });
    }
    
    public File getCygwinBash(){
    	return synchron( new Invocation<File>(){
    		public File run() {
    		    String path = cygwinBash.getStringValue();
    		    if( path.length() == 0 )
    		        return null;
    			return new File( path );
    		}
    	});
    }
    
    /**
     * Creates a new translator using the current settings of this page.
     * @return the new translator
     */
    public IPathTranslator getTranslator(){
        return synchron( new Invocation<IPathTranslator>(){
            public IPathTranslator run() {
                String kind = translator.getSelection();
                
                IPathTranslator translator = null;
            
                File cygpath = getCygwinCygpath();
                if( cygpath == null )
                    cygpath = PathManager.getDefaultCygwinCygpath( getCygwinRoot() );
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
        });
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
    protected IEnvironment getEnvironment(){
        return Environment.getEnvironment();
    }
    
    @Override
    protected IPathTranslator getPathTranslator() {
    	return pathTranslator;
    }
    
    @Override
    protected String[] getVisibleArchitectures() {
    	String tosroot = getTosroot();
    	String tosdir = getTosdir();
    	String makerules = getMakerules();
    	String treeLayout = getTreeLayout();
    	File cygwinRoot = getCygwinRoot();
    	File cygwinBash = getCygwinBash();
    	IPathTranslator translator = getTranslator();
    	
    	if( isDisposed() )
    		return null;
    	
    	return Environment.getEnvironment().getPlatformManager().getArchitectures( 
    	        new PathSetting( tosroot, tosdir, makerules, treeLayout, cygwinRoot, cygwinBash, translator ));
    }
    
    @Override
    protected void resetArchitectureDefaultsPaths( String[] architectures, IProgressMonitor monitor ) {
        String[] array = null;
        if( architectures != null ){
            array = new String[ architectures.length+1 ];
            System.arraycopy( architectures, 0, array, 1, architectures.length );
        }
        
    	PreferenceInitializer.resetArchitectureDefaultPaths( 
    	        monitor,
    	        new PathSetting( getTosroot(), getTosdir(), getMakerules(), getTreeLayout(), getCygwinRoot(), getCygwinBash(), getTranslator()),
    	        array, 
    	        originals );
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
