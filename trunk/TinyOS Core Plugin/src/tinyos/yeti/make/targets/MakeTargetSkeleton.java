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
package tinyos.yeti.make.targets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.ep.ISensorBoard;
import tinyos.yeti.ep.MakeExtra;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.macros.ConstantMacro;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.IProjectMakeTargets;
import tinyos.yeti.make.EnvironmentVariable;
import tinyos.yeti.make.MakeExclude;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.MakeTypedef;
import tinyos.yeti.nature.MissingNatureException;

/**
 * Incomplete {@link IMakeTarget} containing all those methods that are
 * necessary to edit the properties, but does not introduce special custom
 * properties.
 * @author Benjamin Sigg
 */
public class MakeTargetSkeleton implements ILocalMutableMakeTargetProperties, IMakeTargetMorpheable{
    private IMutableMakeTargetProperties properties = new DefaultMakeTargetProperties( 0 );

    private IMakeTargetProperties defaults;
    
    private PlatformProperties platformProperties;
    private LastBuildProperties lastBuildProperties;
    
    public MakeTargetSkeleton( IProject project ) {
        for( MakeTargetPropertyKey<?> key : MakeTargetPropertyKey.KEYS ){
        	setUseLocalProperty( key, true );
        	setUseDefaultProperty( key, false );
        }
        
    	setProject( project );
        
        platformProperties = new PlatformProperties( properties );
        lastBuildProperties = new LastBuildProperties( properties );
        
        setCustomNostdinc( false );
        setUsingLastBuildIncludes( true );
        setUsingPlatformIncludes( true );
        
        properties.addBackup( MakeTargetPropertyKey.INCLUDES, platformProperties );
        properties.addBackup( MakeTargetPropertyKey.INCLUDES, lastBuildProperties );
        
        properties.addBackup( MakeTargetPropertyKey.MACROS, platformProperties );
    }
    
    /**
     * Creates and returns a new {@link IMakeTarget} that uses the properties
     * of this skeleton. 
     * @return a new make-target
     */
    public MakeTarget toMakeTarget(){
    	MakeTarget target = new MakeTarget( getProject(), "project", "project" );
    	target.setDefaults( this );
    	
    	for( MakeTargetPropertyKey<?> key : MakeTargetPropertyKey.KEYS ){
    		target.setUseDefaultProperty( key, true );
    		target.setUseLocalProperty( key, false );
    	}
    	
    	target.setCustomNostdinc( false );
    	target.setLoop( false );
    	target.setLoopTime( 0 );
    	
    	return target;
    }
    
    /**
     * Sets the default values, the default values are only used if
     * {@link #isUseLocalProperty(MakeTargetPropertyKey)} is set to <code>true</code>.
     * The default properties are added as backup properties to all known
     * properties.
     * @param properties the new default values, should not be <code>null</code>
     */
    public void setDefaults( IMakeTargetProperties properties ){
    	if( properties == null )
    		throw new IllegalArgumentException( "properties must not be null" );
    	
    	if( defaults != properties ){
    		boolean[] using = new boolean[ MakeTargetPropertyKey.KEYS.length ];
    		if( defaults != null ){
    			for( int i = 0; i < using.length; i++ ){
    				using[i] = isUseDefaultProperty( MakeTargetPropertyKey.KEYS[i] );
    				if( using[i] ){
    					this.properties.removeBackup( MakeTargetPropertyKey.KEYS[i], defaults );
    				}
    			}
    		}
    		
    		defaults = properties;
    		
    		for( int i = 0; i < using.length; i++ ){
    			if( using[i] ){
    				this.properties.addBackup( MakeTargetPropertyKey.KEYS[i], defaults );
    			}
    		}
    	}
    }
    
    /**
     * Gets the default properties.
     * @return the defaults
     * @see #setDefaults(IMakeTargetProperties)
     */
    public IMakeTargetProperties getDefaults(){
		return defaults;
	}
    
    public int getPriority(){
    	return properties.getPriority();
    }
    
    public <T> T getProperty( MakeTargetPropertyKey<T> key ){
	    return properties.getProperty( key );
    }
    
    public <T> T getLocalProperty( MakeTargetPropertyKey<T> key ){
    	return properties.getLocalProperty( key );
    }
    
    public <T> void putLocalProperty( MakeTargetPropertyKey<T> key, T value ){
    	properties.putLocalProperty( key, value );
    }
    
    public <T> void setUseLocalProperty( MakeTargetPropertyKey<T> key, boolean useLocalProperty ){
	    properties.setUseLocalProperty( key, useLocalProperty );
    }
    
    public <T> boolean isUseLocalProperty( MakeTargetPropertyKey<T> key ){
    	return properties.isUseLocalProperty( key );
    }
    
    public <T> T getBackupProperty( MakeTargetPropertyKey<T> key ){
    	if( defaults == null )
    		return null;
    	
    	return defaults.getProperty( key );
    }
    
    /**
     * Connects or disconnects property <code>key</code> with the
     * {@link #setDefaults(IMakeTargetProperties) default} properties.<br>
     * Note: this method requires that {@link #setDefaults(IMakeTargetProperties)} was
     * called in advance.
     * @param key the name of a property
     * @param useDefaultProperty whether to use the backup
     */
    public <T> void setUseDefaultProperty( MakeTargetPropertyKey<T> key, boolean useDefaultProperty ){
    	if( useDefaultProperty && defaults == null )
    		throw new IllegalStateException( "require non-null defaults" );
    	if( isUseDefaultProperty( key ) != useDefaultProperty ){
    		if( useDefaultProperty ){
    			properties.addBackup( key, defaults );
    		}
    		else{
    			properties.removeBackup( key, defaults );
    		}
    	}
    }
    
    public <T> boolean isUseDefaultProperty( MakeTargetPropertyKey<T> key ){
    	for( IMakeTargetProperties backup : properties.getBackups( key ) ){
    		if( backup == defaults ){
    			return true;
    		}
    	}
    	return false;
    }
    
    public MakeTargetSkeleton copy(){
    	MakeTargetSkeleton sceleton = new MakeTargetSkeleton( getProject() );
    	sceleton.copy( this );
    	return sceleton;
    }
    
    /**
     * Copies all the contents of <code>target</code> into this.
     * @param source the source off all new information
     */
    @SuppressWarnings("unchecked")
	public void copy( MakeTargetSkeleton source ){
    	if( source.getDefaults() != null )
    		setDefaults( source.getDefaults() );
    	
    	for( MakeTargetPropertyKey key : MakeTargetPropertyKey.KEYS ){
    		setUseLocalProperty( key, source.isUseLocalProperty( key ) );
    		setUseDefaultProperty( key, source.isUseDefaultProperty( key ) );
    		putLocalProperty( key, source.getLocalProperty( key ) );
    	}
        
        setUsingLastBuildIncludes( source.isUsingLastBuildIncludes() );
        setUsingPlatformIncludes( source.isUsingPlatformIncludes() );
    }
    
    public ProjectTOS getProjectTOS(){
    	IProject project = getProject();
    	if( project == null )
    		return null;
    	
    	try{
    		return TinyOSPlugin.getDefault().getProjectTOS( project );
    	}
    	catch( MissingNatureException ex ){
    		// silent
    		return null;
    	}
    }
    
    public IProjectMakeTargets getTargets(){
    	return TinyOSPlugin.getDefault().getTargetManager().getProjectTargets( getProject() );
    }

    @Deprecated
    public void setCustomComponent( String component ){
    	putLocalProperty( MakeTargetPropertyKey.COMPONENT, component );
    }
    
    @Deprecated
    public String getCustomComponent(){
    	return getLocalProperty( MakeTargetPropertyKey.COMPONENT );
    }

    @Deprecated
    public String getComponent(){
    	return getProperty( MakeTargetPropertyKey.COMPONENT );
    }
    
    public void setCustomComponentFile( IFile componentFile ){
    	putLocalProperty( MakeTargetPropertyKey.COMPONENT_FILE, componentFile );
    }
    
    public IFile getCustomComponentFile(){
    	return getLocalProperty( MakeTargetPropertyKey.COMPONENT_FILE );
    }
    
    public IFile getComponentFile(){
    	return getProperty( MakeTargetPropertyKey.COMPONENT_FILE );
    }

    public void setCustomTarget(String target) {
     	putLocalProperty( MakeTargetPropertyKey.TARGET, target );
    }
    
    public String getCustomTarget(){
    	return getLocalProperty( MakeTargetPropertyKey.TARGET );
    }

    public String getTarget() {
    	return getProperty( MakeTargetPropertyKey.TARGET );
    }

    public IProject getProject() {
     	return getProperty( MakeTargetPropertyKey.PROJECT );
    }
    
    public void setProject( IProject project ){
    	putLocalProperty( MakeTargetPropertyKey.PROJECT, project );
    }
    
    public boolean isUsingLastBuildIncludes(){
    	return getProperty( MakeTargetPropertyKey.INCLUDE_LAST_BUILD );
    }
    
    public void setUsingLastBuildIncludes( boolean includeBuildPlatform ){
    	putLocalProperty( MakeTargetPropertyKey.INCLUDE_LAST_BUILD, includeBuildPlatform );
    }
    
    public boolean isUsingPlatformIncludes(){
    	return getProperty( MakeTargetPropertyKey.INCLUDE_ENVIRONMENT_DEFAULT_PATHS );
    }
    
    public void setUsingPlatformIncludes( boolean use ){
    	putLocalProperty( MakeTargetPropertyKey.INCLUDE_ENVIRONMENT_DEFAULT_PATHS, use );
    }
    
    public void setCustomIncludes( MakeInclude[] includes ){
    	putLocalProperty( MakeTargetPropertyKey.INCLUDES, includes );
    }
    
    public MakeInclude[] getCustomIncludes(){
    	return getLocalProperty( MakeTargetPropertyKey.INCLUDES );
    }
    
    public MakeInclude[] getIncludes(){
    	return getProperty( MakeTargetPropertyKey.INCLUDES );
    }
    
    public void setCustomExcludes( MakeExclude[] excludes ){
    	putLocalProperty( MakeTargetPropertyKey.EXCLUDES, excludes );
    }
    
    public MakeExclude[] getCustomExcludes(){
    	return getLocalProperty( MakeTargetPropertyKey.EXCLUDES );
    }
    
    public MakeExclude[] getExcludes(){
    	return getProperty( MakeTargetPropertyKey.EXCLUDES );
    }
    
    public void setCustomMacros( ConstantMacro[] macros ){
    	putLocalProperty( MakeTargetPropertyKey.MACROS, macros );
    }

    public IMacro[] getMacros(){
    	return getProperty( MakeTargetPropertyKey.MACROS );
    }
    
    public ConstantMacro[] getCustomMacros(){
        return (ConstantMacro[])getLocalProperty( MakeTargetPropertyKey.MACROS );
    }
    
    public void setCustomTypedefs( MakeTypedef[] typedefs ){
        putLocalProperty( MakeTargetPropertyKey.TYPEDEFS, typedefs );
    }
    
    public EnvironmentVariable[] getEnvironmentVariables(){
    	return getProperty( MakeTargetPropertyKey.ENVIRONMENT_VARIABLES );
    }
    
    public EnvironmentVariable[] getCustomEnvironmentVariables(){
    	return getLocalProperty( MakeTargetPropertyKey.ENVIRONMENT_VARIABLES );
    }
    
    public void setCustomEnvironmentVariables( EnvironmentVariable[] variables ){
    	putLocalProperty( MakeTargetPropertyKey.ENVIRONMENT_VARIABLES, variables );
    }
    
    public MakeTypedef[] getCustomTypedefs(){
    	return getLocalProperty( MakeTargetPropertyKey.TYPEDEFS );
    }
    
    public MakeTypedef[] getTypedefs(){
     	return getProperty( MakeTargetPropertyKey.TYPEDEFS );
    }
    
    public IPlatform getPlatform(){
    	return platformProperties.getPlatform();
    }

    public MakeExtra[] getMakeExtras() {
        return getProperty( MakeTargetPropertyKey.MAKE_EXTRAS );
    }
    
    public void setCustomMakeExtras( MakeExtra[] extras ){
    	putLocalProperty( MakeTargetPropertyKey.MAKE_EXTRAS, extras );
    }
    
    public MakeExtra[] getCustomMakeExtras(){
    	return getLocalProperty( MakeTargetPropertyKey.MAKE_EXTRAS );
    }

    public void setCustomBoards(String[] selectedBoard) {
        putLocalProperty( MakeTargetPropertyKey.BOARDS, selectedBoard );
    }

    public String[] getCustomBoards(){
    	return getLocalProperty( MakeTargetPropertyKey.BOARDS );
    }

    public String[] getBoards() {
    	return getProperty( MakeTargetPropertyKey.BOARDS );
    }

    public ISensorBoard[] getSensorBoards(){
    	String[] boards = getBoards();
    	
        if( boards == null )
            return new ISensorBoard[]{};
        
        IPlatform platform = getPlatform();
        if( platform == null )
            return new ISensorBoard[]{};
        
        ISensorBoard[] choices = platform.getSensorboards();
        if( choices == null )
            return new ISensorBoard[]{};
        
        List<ISensorBoard> result = new ArrayList<ISensorBoard>();
        for( String board : boards ){
            for( ISensorBoard choice : choices ){
                if( choice.getName().equals( board )){
                    result.add( choice );
                    break;
                }
            }
        }
        
        return result.toArray( new ISensorBoard[ result.size() ] );
    }

    public boolean isNostdinc() {
    	return getProperty( MakeTargetPropertyKey.NO_STD_INCLUDE );
    }

    public void setCustomNostdinc( boolean nostdinc ){
    	properties.putLocalProperty( MakeTargetPropertyKey.NO_STD_INCLUDE, nostdinc );
    }
    
    public boolean isCustomNostdinc(){
    	return getLocalProperty( MakeTargetPropertyKey.NO_STD_INCLUDE );
    }
}
