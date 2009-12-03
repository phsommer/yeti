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
package tinyos.yeti.environment.basic.path;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.environment.basic.path.steps.AdditionalDirectoriesStep;
import tinyos.yeti.environment.basic.path.steps.ArchitectureStep;
import tinyos.yeti.environment.basic.path.steps.DirectivesStep;
import tinyos.yeti.environment.basic.path.steps.GccIncludeStep;
import tinyos.yeti.environment.basic.path.steps.ICollectStep;
import tinyos.yeti.environment.basic.path.steps.ISearchStep;
import tinyos.yeti.environment.basic.path.steps.InterfacesStep;
import tinyos.yeti.environment.basic.path.steps.LibraryStep;
import tinyos.yeti.environment.basic.path.steps.NesCPathStep;
import tinyos.yeti.environment.basic.path.steps.ProjectStep;
import tinyos.yeti.environment.basic.path.steps.SafeStep;
import tinyos.yeti.environment.basic.path.steps.SensorBoardStep;
import tinyos.yeti.environment.basic.path.steps.SystemStep;
import tinyos.yeti.environment.basic.path.steps.TargetStep;
import tinyos.yeti.environment.basic.path.steps.TypesStep;
import tinyos.yeti.environment.basic.platform.IExtendedPlatform;
import tinyos.yeti.environment.basic.progress.ICancellation;
import tinyos.yeti.environment.basic.progress.NullCancellation;
import tinyos.yeti.make.MakeExclude;

/**
 * This abstract version of a path manager 
 * @author Benjamin Sigg
 */
public abstract class AbstractPathManager implements IPathManager{
    public static final String TREE_TINYOS_1X = "tinyos 1.x";
    public static final String TREE_TINYOS_2X = "tinyos 2.x";
    
    private Map<String,ISearchStep[]> steps = new HashMap<String, ISearchStep[]>();

    private ICollectStep standardStep;


    /**
     * Sets the steps that should be performed when searching a file.
     * @param steps the steps
     */
    public void setSteps( String layout, ISearchStep[] steps ){
        this.steps.put( layout, steps );
    }

    public void addStep( String layout, ISearchStep step ){
        ISearchStep[] steps = this.steps.get( layout );

        if( steps == null ){
            steps = new ISearchStep[]{ step };
        }
        else{
            ISearchStep[] old = steps;
            steps = new ISearchStep[ steps.length+1 ];
            System.arraycopy( old, 0, steps, 0, old.length );
            steps[ old.length ] = step;
        }

        this.steps.put( layout, steps );
    }
    
    /**
     * Gets the current layout of the tree. One of
     * {@link #TREE_TINYOS_1X} or {@link #TREE_TINYOS_2X}
     * @return the current layout
     */
    public abstract String getTreeLayout();
    
    public String getPlatformDirectory(){
        return getPlatformDirectory( null, null );
    }
    
    public String getPlatformDirectory( String tosdir, String treeLayout ){
        if( tosdir == null )
            tosdir = getTosDirectoryPath();
        
        if( treeLayout == null )
            treeLayout = getTreeLayout();
        
        if( TREE_TINYOS_2X.equals( treeLayout )){
            return tosdir + "/platforms";
        }
        if( TREE_TINYOS_1X.equals( treeLayout )){
            return tosdir + "/platform";
        }
        throw new IllegalStateException( "Unknown kind of tree layout: " + treeLayout );
    }

    /**
     * Puts up the default implementations for the steps to perform when
     * searching a file.
     * @param layout for which layout to set the steps
     */
    public void setDefaultSteps( String layout ){
        if( TREE_TINYOS_1X.equals( layout )){
            setSteps(
                    TREE_TINYOS_1X,
                    new ISearchStep[]{
                            // note: no "save step" as in 2.x
                            
                            new ProjectStep(),
                            new DirectivesStep( false ),
                            new SensorBoardStep(),
                            new TargetStep(),
                            new ArchitectureStep(){
                                @Override
                                protected File[] getIncludeDirectories( String processor ){
                                    return AbstractPathManager.this.getArchitectureIncludeDirectories( processor );
                                }
                            },
                            new AdditionalDirectoriesStep(),
                            new InterfacesStep(),
                            new DirectivesStep( true ),
                            new SystemStep(),
                            new LibraryStep(),

                            new NesCPathStep(),

                            new GccIncludeStep(){
                                @Override
                                protected File[] getGccIncludeDirectories(){
                                    return AbstractPathManager.this.getGccIncludeDirectories();
                                }
                            },

                            // hm, this one seems not be specified anywhere, but also
                            // it seems to be a pretty important one
                            new TypesStep()
                    });
        }
        
        if( TREE_TINYOS_2X.equals( layout )){
            setSteps(
                    TREE_TINYOS_2X,
                    new ISearchStep[]{
                            new ProjectStep(),
                            new DirectivesStep( false ),
                            new SensorBoardStep(),
                            new TargetStep(),
                            new ArchitectureStep(){
                                @Override
                                protected File[] getIncludeDirectories( String processor ){
                                    return AbstractPathManager.this.getArchitectureIncludeDirectories( processor );
                                }
                            },
                            new AdditionalDirectoriesStep(),
                            new InterfacesStep(),
                            new SafeStep(),
                            new DirectivesStep( true ),
                            new SystemStep(),
                            // new LibraryStep(),

                            new NesCPathStep(),

                            new GccIncludeStep(){
                                @Override
                                protected File[] getGccIncludeDirectories(){
                                    return AbstractPathManager.this.getGccIncludeDirectories();
                                }
                            },

                            // hm, this one seems not be specified anywhere, but also
                            // it seems to be a pretty important one
                            new TypesStep()
                    });
        }
    }

    /**
     * Searches the directories with included headers when using the given processor.
     * @param processor a processor, for example "avr" or "msp430"
     * @return the directories, can be <code>null</code>
     */
    protected abstract File[] getArchitectureIncludeDirectories( String processor );

    /**
     * Gets the directories in which gcc is supposed to find the very normal
     * system c headers.
     * @return the directories, can be <code>null</code>
     */
    protected abstract File[] getGccIncludeDirectories();

    /*
     *
     *  SEARCH PATH

    ncc performs the following substitutions on the directories specified with the -I option: %T is replaced by the TinyOS directory, %p is replaced by the selected target, %% is replaced by %.

    Except when -nostdinc is specified, the search path for nesC components is as follows, where tosdir is the TinyOS directory requested and target is the selected target:

       1. ./
       2. -I directives (in option order)
       3. %T/sensorboards/boardname, for each -board=boardname option specified (in option order) - except if the sensorboard was found via an explicit -I directive
       4. %T/platform/%p - except if the platform was found via an explicit -I directive
       5. Additional directories requested by the selected target (e.g., %T/platform/avrmote for the mica target)
       6. %T/interfaces
       7. %T/system
       8. %T/lib
       9. `NESCPATH' environment variable directories (note that %T and %p subsitution is not performed on these directories). 

    When -nostdinc is specified, the search path is simply:

       1. ./
       2. -I directives
       3. `NESCPATH' environment variable directories 


     */

    public ISearchStep[] getSteps( String layout ){
        ISearchStep[] steps = this.steps.get( layout );
        if( steps == null ){
            setDefaultSteps( layout );
            steps = this.steps.get( layout );
        }
        return steps;
    }

    public File[] getAllReachableFiles( IPathRequest request, ICancellation cancellation ){
        if( cancellation == null )
            cancellation = new NullCancellation();

        ISearchStep[] steps = getSteps( getTreeLayout() );

        PathSet paths = new PathSet( request, false );
        for( ISearchStep step : steps ){
            if( cancellation.isCanceled() )
                return null;

            Debug.info( "search all files, step: " + step.getName() );
            step.collect( request, paths, cancellation );
        }

        if( cancellation.isCanceled() )
            return null;

        return paths.getFiles();
    }

    public File locate( String fileName, IPathRequest request, ICancellation cancellation ){
        if( cancellation == null )
            cancellation = new NullCancellation();

        ISearchStep[] steps = getSteps( getTreeLayout() );

        PathSet paths = new PathSet( request, true );
        for( ISearchStep step : steps ){
            if( cancellation.isCanceled() )
                return null;

            step.locate( fileName, request, paths, cancellation );
            File file = paths.getLocated();
            if( file != null )
                return file;
        }

        return null;
    }

    /**
     * Creates the search pattern for the standard inclusion files.
     * @return the pattern
     */
    protected abstract ICollectStep createStandardSearch();

    public File[] getStandardInclusionFiles( ICancellation cancellation ){
        if( cancellation == null )
            cancellation = new NullCancellation();

        if( standardStep == null )
            standardStep = createStandardSearch();

        PathRequest request = new PathRequest();
        request.setFileExtensions( new String[]{ "h", "nc" } );
        PathSet paths = new PathSet( request, false );
        standardStep.collect( request, paths, cancellation );
        if( cancellation.isCanceled() )
            return null;
        return paths.getFiles();
    }

    /**
     * Gets the extended platform information of platform <code>name</code>.
     * @param name the name of the platform
     * @return the platform or <code>null</code>
     */
    protected abstract IExtendedPlatform getPlatform( String name );
    
    public IPathReplacer createReplacer( final String platform ){
    	return new IPathReplacer(){
    		public String replace( String string ){
	    		return AbstractPathManager.this.replace( string, platform );
    		}
    	};
    }
    
    public String replace( String string, String platform ){
        String tos = getTosDirectoryPath();
        if( tos == null )
            tos = "%T";

        // TODO also replace: 
        // String[] ENVIRONMENT_TOSDIR = new String[]{ "${TOSDIR}", TOSDIR };

        if( string == null || string.length() < 2 )
            return string;

        // count the number of characters we will need
        int count = 1;
        char current = string.charAt( 0 );

        for( int i = 1, n = string.length(); i<n; i++ ){
            char next = string.charAt( i );
            count++;

            if( current == '%' ){
                switch( next ){
                    case '%':
                        count -= 1;
                        next = 0;
                        break;
                    case 'p':
                        count += platform.length()-1;
                        break;
                    case 'T':
                        count += tos.length()-1;
                        break;
                }
            }

            current = next;
        }

        // replace
        StringBuilder builder = new StringBuilder( count );

        current = string.charAt( 0 );
        for( int i = 1, n = string.length(); i<n; i++ ){
            char next = string.charAt( i );

            if( current == '%' ){
                switch( next ){
                    case '%':
                        builder.append( '%' );
                        next = 0;
                        break;
                    case 'p':
                        builder.append( platform );
                        next = 0;
                        break;
                    case 'T':
                        builder.append( tos );
                        next = 0;
                        break;
                    default:
                        builder.append( current );
                    builder.append( next );
                    next = 0;
                    break;
                }
            }
            else if( current != 0 ){
                builder.append( current );
            }

            current = next;
        }

        if( current != 0 )
            builder.append( current );

        return builder.toString();
    }
    
    public String[] relativeToAbsolute( String path, ProjectTOS project ){
    	File file = modelToSystem( path );
    	if( file.isAbsolute() )
    		return new String[]{ path };
    	
    	IFolder[] source = project.getSourceContainers();
    	
    	String[] result = new String[ source.length ];
    	
    	for( int i = 0; i < source.length; i++ ){
    		IPath location = source[i].getLocation();
    		String locationPath = systemToModel( location.toFile() );
    		result[i] = locationPath + "/" + path;
    	}
    	
    	return result;
    }

    private class PathSet implements IPathSet{
        private List<File> files;
        private File located;
        private IPathRequest request;
        private Set<String> processedDirectories = new HashSet<String>();
        private String[] extensions;

        public PathSet( IPathRequest request, boolean search ){
            this.request = request;

            String[] fileExtensions = request.getFileExtensions();
            if( fileExtensions != null ){
                extensions = new String[ fileExtensions.length ];
                for( int i = 0, n = extensions.length; i<n; i++ )
                    extensions[i] = "." + fileExtensions[i];
            }

            if( !search )
                files = new ArrayList<File>();
        }

        public IPathManager getPathManager() {
            return AbstractPathManager.this;
        }

        public boolean validFileExtension( String name ){
            if( extensions == null )
                return true;

            for( String ext : extensions ){
                if( name.endsWith( ext ))
                    return true;
            }

            return false;
        }

        public void store( File file ){
        	if( file != null ){
                if( files != null ){
                    files.add( file );
                    Debug.info( "search all files, found: " + file.getAbsolutePath() );
                }
                else
                    located = file;
            }
        }

        public boolean isExcluded( File directory ){
            MakeExclude[] excludes = request.getExcludes();
            if( excludes == null )
                return false;

            String path = directory.getAbsolutePath();

            for( MakeExclude exclude : excludes ){
                if( exclude.exclude( path ))
                    return true;
            }

            return false;
        }

        public File[] getFiles(){
            return files.toArray( new File[ files.size() ] );
        }

        public File getLocated(){
            return located;
        }

        public boolean setProcessed( String directory ){
            boolean result = processedDirectories.add( directory );
            if( result && files != null ){
            	Debug.info( "Search all files, visit: " + directory );
            }
            return result;
        }

        public boolean isProcessed( String directory ){
            return processedDirectories.contains( directory );
        }

        public String replace( String string ) {
            String platform = request.getPlatformName();
            if( platform == null )
                platform = "%p";

            return AbstractPathManager.this.replace( string, platform );
        }

        public String[] relativeToAbsolute( String path ){
        	return AbstractPathManager.this.relativeToAbsolute( path, request.getProject() );
        }
        
        public IExtendedPlatform getPlatform( String name ) {
            if( name == null )
                return null;

            return AbstractPathManager.this.getPlatform( name );
        }
    }
}
