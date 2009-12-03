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
package tinyos.yeti.ep;

import java.io.File;
import java.io.OutputStream;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.MakeExclude;
import tinyos.yeti.make.MakeInclude;

public interface IEnvironment {
	public static enum SearchFlag{
		EXCLUDE_PROJECT
	}

    public String getEnvironmentID();

    public String getEnvironmentName();
    
    public String getEnvironmentDescription();

    /**
     * Gets a set of tests which can determine whether this environment
     * is setup correctly.
     * @return the tests, can be <code>null</code>
     */
    public ITest[] getTests();
    
    /**
     * Adds a new listener to this environment. A listener can be added
     * more than once.
     * @param listener the new listener
     */
    public void addEnvironmentListener( IEnvironmentListener listener );
    
    /**
     * Removes the first occurrence of the listener <code>listener</code>
     * from this environment.
     * @param listener the listener to remove
     */
    public void removeEnvironmentListener( IEnvironmentListener listener );
    
    /**
     * If the environment is not yet started up, then <code>run</code> is
     * stored in a list and {@link Runnable#run()} is called later from an
     * unspecified thread.<br>
     * If the environment is already started up, then {@link Runnable#run()}
     * is called either from this or from an unspecified thread
     * @param run the element to call after startup
     */
    public void runAfterStartup( Runnable run );
    
    /**
     * Gets an integer telling how important this environment is. As higher
     * the integer, as more important the environment. The most important
     * environment will be set as default environment for all new projects.
     * @return the importance, a number greater or equal to 0
     */
    public int getEnvironmentImportance();
    
    @Deprecated
    public String getSuitablePath(File file);
    
    /**
     * Given a real file, this method returns the path as it would be used
     * when calling a tool of the tinyos tool chain.
     * @param file some file
     * @return the path of the file, or <code>null</code> if <code>file</code>
     * cannot be resolved
     */
    public String systemToModel( File file );
    
    /**
     * Given a path as it is used by the tinyos tool chain, this method
     * returns the real file the path points to.
     * @param path some path
     * @return the real file or <code>null</code> if <code>path</code>
     * cannot be resolved
     */
    public File modelToSystem( String path );

    /** 
     * Lists all platforms the environment can support. This method should
     * always return the same objects. 
     * @return the platforms
     */
    public IPlatform[] getPlatforms();

    public String[] getExampleApplicationNames();

    public File getExampleAppDirectory( String appName );

    /**
     * Searches for a file that is accessible from project <code>p</code>
     * @param fileName the name of the file, this name has a form like it could
     * be found in an include directive. There is no need for this method to
     * search subdirectories since base-directory + filename should point
     * into the right subdirectory
     * @param p the project for which the file is searched
     * @param directives additional includes for the project <code>p</code>
     * @param excludes the set of directories which must not be searched
     * @param boards the sensor board(s) which are used by the project
     * @param platform the target platform for which the project will be compiled
     * @param nostdinc no standard include, if set then only current directory,
     * directories and 'NESCPATH' environment variable directories are searched 
     * @param systemFile whether a system file is requested, if so then
     * the files that are written by the user should be ignored
     * @param monitor to inform about progress or to cancel the operation
     * @return the file that matches <code>filename</code> or <code>null</code> 
     * if not found
     */
    public File locate(
            String fileName, 
            IProject p,  
            MakeInclude[] directives, 
            MakeExclude[] excludes,
            ISensorBoard[] boards, 
            String platform, 
            boolean nostdinc,
            boolean systemFile,
            IProgressMonitor monitor );

    public File[] getAllReachableFiles(
            IProject project, 
            MakeInclude[] directives, 
            MakeExclude[] excludes,
            ISensorBoard[] boards, 
            String platformName, 
            boolean nostinct, 
            String[] fileExtensions,
            Set<SearchFlag> flags,
            IProgressMonitor monitor );

    /**
     * Gets a set of files that are included into any file that gets parsed.
     * @return the set of always present files
     */
    public File[] getStandardInclusionFiles();
    
    /**
     * Calls the ncc compiler.
     * @param info stream to write informations which are provided by the plugin
     * @param out the stream to write message in
     * @param error the stream to write error message in
     * @param directory the directory in which ncc should run
     * @param project the project that gets compiler
     * @param target how to compile the project
     * @param progress to inform about progress
     */
    public void executeMake(
            OutputStream info,
            OutputStream out,
            OutputStream error,
            File directory,
            ProjectTOS project,
            IMakeTarget target, 
            IProgressMonitor progress);
}

