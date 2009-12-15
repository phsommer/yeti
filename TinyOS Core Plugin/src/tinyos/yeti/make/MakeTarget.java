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
package tinyos.yeti.make;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.MakeExtra;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.macros.ConstantMacro;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;


public class MakeTarget extends MakeTargetSkeleton implements IMakeTarget {
    private String name;
    private String id;

    private boolean loop = false;
    private double loopTime = 2;
    
    public MakeTarget( IProject project, String name, String id ) {
    	super( project );
        this.name = name;
        this.id = id;
    }
    
    public MakeTarget(){
    	super( null );
    }
    
    /**
     * Creates a copy of this target.
     * @return a copy
     */
    @Override
	public MakeTarget copy() {
        MakeTarget target = new MakeTarget( getProject(), name, id );
        target.copy( this );
        return target;
    }
    
    @Override
    public MakeTarget toMakeTarget(){
	    return this;
    }

    /**
     * Copies all the contents of <code>target</code> into this.
     * @param source the source off all new information
     */
	public void copy( MakeTarget source ){
    	super.copy( source );
    	
        name = source.name;
        
        loop = source.loop;
        loopTime = source.loopTime;
    }
    /**
     * Makes a copy of <code>source</code>, ensures that any change in
     * <code>source</code> or the defaults of <code>source</code> do not
     * have any effect in this skeleton. The methods {@link #getProperty(MakeTargetPropertyKey)}
     * of <code>this</code> and of <code>source</code> will return the same,
     * but {@link #getLocalProperty(MakeTargetPropertyKey)} will not.
     * @param source the element to copy
     */
    public void copyFull( MakeTarget source ){
    	for( MakeTargetPropertyKey<?> key : MakeTargetPropertyKey.KEYS ){
    		setUseLocalProperty( key, true );
    		setUseDefaultProperty( key, false );
    		copyFull( source, key );
    	}

        setUsingLastBuildIncludes( source.isUsingLastBuildIncludes() );
        setUsingPlatformIncludes( source.isUsingPlatformIncludes() ); 
	}

    private <T> void copyFull( MakeTarget source, MakeTargetPropertyKey<T> key ){
    	putLocalProperty( key, source.getProperty( key ) );
    }
    
    /**
     * Compares the content of <code>source</code> to <code>this</code>. If the
     * changes when calling {@link #copy(MakeTarget)} are high, then the project
     * needs to be rebuilt.
     * @param source a potential argument for {@link #copy(MakeTarget)}
     * @return <code>true</code> if a project changing from <code>this</code>
     * to <code>source</code> target needs to be rebuilt or not.
     */
    public boolean copyTriggersUpdate( IMakeTarget source ){
        if( !equals( getTarget(), source.getTarget() ) )
            return true;
        
        if( isNostdinc() != source.isNostdinc() )
            return true;
        
        if( !Arrays.equals( getIncludes(), source.getIncludes() ))
            return true;
        
        if( !Arrays.equals( getBoards(), source.getBoards() ))
            return true;
        
        if( !Arrays.equals( getExcludes(), source.getExcludes() ))
            return true;
        
        if( !Arrays.equals( getMacros(), source.getMacros() ))
            return true;
        
        if( !Arrays.equals( getTypedefs(), source.getTypedefs() ))
            return true;
        
        return false;
    }

    private boolean equals( Object a, Object b ){
    	if( a == null )
    		return b == null;
    	else
    		return a.equals( b );
    }
    
    public List<String> getNesccFlags() {
    	List<String> result = new ArrayList<String>();
    	String target = getTarget();
    	if( target != null ){
    		result.add( "-target=" + target );
    	}
    	
    	getPFlags( result );
    	return result;
    }
    
    public List<String> getMakeCommands(){
        List<String> command = new ArrayList<String>();
        String target = getTarget();
        
        if( target == null )
            command.add( "null" );
        else
            command.add( target );

        MakeExtra[] makeExtras = getProperty( MakeTargetPropertyKey.MAKE_EXTRAS );
        if( makeExtras != null ){
            for( MakeExtra extra : makeExtras ){
                if( extra.hasParameter() && extra.getParameterValue() != null && !extra.getParameterValue().equals( "" )){
                    command.add( extra.getName() + "." + extra.getParameterValue() );
                }
                else{
                    command.add( extra.getName() );
                }
            }
        }

        return command;
    }

    public List<String> getPFlags(){
        List<String> result = new ArrayList<String>();
        getPFlags( result );
        return result;
    }
    
    public void getPFlags( List<String> flags ){
        ProjectTOS tos = getProjectTOS();

        for( String source : tos.getSourceIncludes( this ) ){
            flags.add( "-I" + source );
        }

        listIncludes( flags, true );
        
        MakeMacro[] macros = getMacros();
        if( macros != null ){
        	for( MakeMacro make : macros ){
        		if( make.isIncludeNcc() ){
	        		IMacro macro = make.getMacro();
	        		if( macro instanceof ConstantMacro ){
	        			ConstantMacro constant = (ConstantMacro)macro;
	        			flags.add( "-D" + constant.getName() + "=" + constant.getConstant() );
	        		}
        		}
        	}
        }

        String[] boards = getBoards();
        if( boards != null ){
            for( String board : boards ){
                flags.add( "-board=" + board );
            }
        }

        if( isNostdinc() )
            flags.add( "-nostdinc" );
    }

    private void listIncludes( List<String> list, boolean directivePrefix ){
        MakeInclude[] includes = getIncludes();
        MakeExclude[] excludes = getExcludes();
        
        if( includes != null ){
            for( MakeInclude source : includes ){
                if( source.isNcc() ){
                    if( source.isRecursive() ){
                        File file = modelToSystem( source.getPath() );
                        if( file != null ){
                            recursiveInclude( list, file, directivePrefix, excludes );
                        }
                    }
                    else{
                        String path = source.getPath();
                        
                        if( !shouldExclude( path, excludes )){
                            if( directivePrefix )
                                list.add( "-I" + path );
                            else
                                list.add( path );
                        }
                    }
                }
            }
        }
    }

    private void recursiveInclude( List<String> list, File directory, boolean directivePrefix, MakeExclude[] excludes ){
        if( directory.isDirectory() && directory.exists() ){
            String path = systemToModel( directory );
            if( path != null ){
                if( !shouldExclude( path, excludes )){
                    if( directivePrefix )
                        list.add( "-I" + path );
                    else
                        list.add( path );
                }
            }

            File[] children = directory.listFiles();
            if( children != null ){
                for( File file : children ){
                    recursiveInclude( list, file, directivePrefix, excludes );
                }
            }
        }
    }
    
    public String systemToModel( File file ){
        ProjectTOS project = getProjectTOS();
        if( project == null )
            return null;
        
        IEnvironment environment = project.getEnvironment();
        if( environment == null )
            return null;
        
        return environment.systemToModel( file );
    }
    
    public File modelToSystem( String path ){
        ProjectTOS project = getProjectTOS();
        if( project == null )
            return null;
        
        IEnvironment environment = project.getEnvironment();
        if( environment == null )
            return null;
        
        return environment.modelToSystem( path );
    }
    
    private boolean shouldExclude( String path, MakeExclude[] excludes ){
        if( excludes != null ){
            for( MakeExclude exclude : excludes ){
                if( exclude.exclude( path )){
                    return true;
                }
            }
        }
        
        return false;
    }

    public String getName() {
        return name;
    }
    
    public String getId(){
    	if( id == null )
    		return "project/" + name;
    	
    	return id;
    }
    
    public MultiMakeExclude getExclude(){
    	ProjectTOS project = getProjectTOS();
    	return new MultiMakeExclude( getExcludes(), project == null ? null : project.getEnvironment() );
    }
    
    public String[] getIncludesDirectives(){
        List<String> result = new ArrayList<String>();
        listIncludes( result, false );
        return result.toArray( new String[ result.size() ] );
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setId( String id ){
		this.id = id;
	}

    @Override
    public String toString() {
    	IProject project = getProject();
    	MakeExtra[] makeExtras = getMakeExtras();
    	
        String out = "Maketarget "+name+" in Project "+project.getName()+ "\n";
        out += "  Target   : "+getTarget()+"\n";
        out += "  loops    : "+loop+ "\n";
        out += "  looptime : "+loopTime+ "\n";

        if ((makeExtras != null)&&(makeExtras.length >0)) {
            out += "  Extras :" + "\n";

            for (int i = 0; i < makeExtras.length; i++) {
                out += "    "+ makeExtras[i].getName() ;

                if (makeExtras[i].hasParameter()) {
                    out += "  "+makeExtras[i].getParameterName() + "  value= "+makeExtras[i].getParameterValue();
                }
                out += "\n";
            }
        }
        return out;
    }
    
    public void setLoopTime(double d) {
        loopTime = d;
    }


    public void setLoop(boolean b) {
        loop = b;
    }


    public boolean getLoop() {
        return loop;
    }


    public double getLoopTime() {
        return loopTime;
    }
    
    public IStatus ready(){
    	IFile component = getComponentFile();
    	
        if( component == null ){
            return new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, "No main-component specified" );
        }
        
        if( !component.exists() ){
        	return new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, "Main-component file does not exist: '" + component.getName() + "'" );
        }
        
        if( getLoop() ){
            return new Status( IStatus.INFO, TinyOSPlugin.PLUGIN_ID, "Target will be executed again after '" + getLoopTime() + "' seconds" );
        }
        
        return new Status( IStatus.OK, TinyOSPlugin.PLUGIN_ID, null );
    }
}
