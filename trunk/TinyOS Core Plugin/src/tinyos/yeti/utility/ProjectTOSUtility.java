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
package tinyos.yeti.utility;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.EnvironmentManager;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.MakeExclude;
import tinyos.yeti.make.ProjectTargets;
import tinyos.yeti.make.targets.MakeTargetSkeleton;
import tinyos.yeti.nature.MissingNatureException;

/**
 * Helpful methods to create new projects.
 * @author Benjamin Sigg
 */
public final class ProjectTOSUtility{
	private ProjectTOSUtility(){
		// nothing
	}

	/**
	 * Gets the contents of the makefile-stub-file "MakefileStub.txt".
	 * @return the makefile
	 */
	private static String getMakefileStub(){
		return getTextDocument( "MakefileStub.txt" );
	}

	private static String getTextDocument( String name ){
		try{
			BufferedInputStream in = new BufferedInputStream( ProjectTOSUtility.class.getResourceAsStream( name ) );
			InputStreamReader reader = new InputStreamReader( in, "UTF-8" );

			StringBuilder builder = new StringBuilder();
			int read;
			while( (read = reader.read() ) != -1 ){
				builder.append( (char)read );
			}

			reader.close();
			return builder.toString();
		}
		catch( IOException ex ){
			throw new RuntimeException( ex );
		}
	}

	/**
	 * Creates a new empty project with the given name.
	 * @param name the name of the project
	 * @param monitor to report progress
	 * @return the new project
	 */
	public static ProjectTOS createEmptyProject( String name, IProgressMonitor monitor ){
		IWorkspace workspace = TinyOSPlugin.getWorkspace();
		IProject handle = workspace.getRoot().getProject( name );

		return createEmptyProject( handle, null, monitor );
	}

	/**
	 * Transforms a non existing project into an empty, non-initialized
	 * TinyOS-project. 
	 * @param projectHandle the handle for the new project
	 * @param customPath the path for the new project, can be <code>null</code>
	 * @param monitor for interaction
	 * @return the new project or <code>null</code> if the operation was canceled
	 */
	public static ProjectTOS createEmptyProject( IProject projectHandle, IPath customPath, IProgressMonitor monitor ){
		try{
			if( monitor == null )
				monitor = new NullProgressMonitor();

			monitor.beginTask( "Setup Project", 30 );

			IWorkspace workspace = TinyOSPlugin.getWorkspace();

			IProjectDescription description = workspace.newProjectDescription( projectHandle.getName() );
			IPath path = Platform.getLocation();
			if( customPath != null && !path.equals( customPath ) ){
				path = customPath;
				description.setLocation(path);
			}

			if( !projectHandle.exists() ){
				projectHandle.create(
						description,
						new SubProgressMonitor( monitor, 10 ));
			}

			if( !projectHandle.isOpen() ){
				projectHandle.open(new SubProgressMonitor( monitor, 10 ));
			}


			IProjectDescription projectDescription = projectHandle.getDescription();
			String[] natures = projectDescription.getNatureIds();
			String[] newNatures;
			newNatures = new String[ natures.length + 1 ];
			System.arraycopy( natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = TinyOSCore.NATURE_ID;

			projectDescription.setNatureIds( newNatures );
			projectHandle.setDescription(projectDescription, null);

			addBuilder( projectHandle,"tinyos.yeti.core.projectbuilder" );
			// addBuilder( projectHandle,"tinyos.yeti.core.makefilebuilder" );

			monitor.done();
			ProjectTOS project = TinyOSPlugin.getDefault().getProjectTOS( projectHandle, false );

			for( IFolder folder : project.getSourceContainers() ){
				FileUtility.createFolder( folder, new SubProgressMonitor( monitor, 0 ) );
			}

			return project;
		}
		catch( MissingNatureException ex ){
			TinyOSCore.inform( "create empty project", ex );
			monitor.done();
			return null;
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex );
			monitor.done();
			return null;
		}
	}

	/**
	 * Copies the contents of <code>directory</code> into the source folder of
	 * <code>project</code>. Returns the contents of the makefile if any is found.
	 * @param project the project to write into
	 * @param directory the directory to copy
	 * @param monitor to be informed about progress
	 * @return the contents of the makefile (if any), or <code>null</code>
	 * @throws CoreException if the files cannot be copied
	 */
	public static byte[] copyToProject( ProjectTOS project, File directory, IProgressMonitor monitor ) throws CoreException{
		class MakefileObserver implements FileUtility.TreeCopyListener{
			public byte[] makefile;

			public void copyFile( File source, IFile destination, byte[] content ){
				if( source.getName().equals( "Makefile" )){
					makefile = content;
				}
			}
			public void enterDirectory( File sourceDirectory, IFolder destinationDirectory ){
				// ignore
			}
			public void leaveDirectory( File sourceDirectory, IFolder destinationDirectory ){
				// ignore   
			}
		};

		MakefileObserver observer = new MakefileObserver();

		IFolder[] folders = project.getSourceContainers();
		if( folders.length != 1 )
			throw new CoreException( new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, "Project must have exactly one source folder" ));
		
		FileUtility.copyTree( directory, folders[0], observer, monitor );

		return observer.makefile;
	}

	/**
	 * Copies all the given files to the source folder of <code>project</code>.
	 * @param project the project to write into
	 * @param source the files to copy
	 * @param monitor to interact
	 * @return the bytes of the makefile (if any was found)
	 */
	public static byte[] copyToProject( ProjectTOS project, File[] source, IProgressMonitor monitor ) throws IOException, CoreException{
		if( monitor == null )
			monitor = new NullProgressMonitor();

		monitor.beginTask( "Copy files", source.length );

		IFolder[] folders = project.getSourceContainers();
		if( folders.length != 1 )
			throw new CoreException( new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, "Project must have exactly one source folder" ));
		
		IFolder sourceFolder = folders[0];

		byte[] makefile = null;

		for( int i = 0; i < source.length; i++ ){
			if( source[i].isFile() ){
				IFile file;
				boolean isMakefile = source[i].getName().equals( "Makefile" );

				//if( isMakefile ){
					//    file = sourceFolder.getFile( "."+source[i].getName()+"_orig" );
				//}
				//else{
				file = sourceFolder.getFile( source[i].getName() );
				//}

				byte[] bytes = FileUtility.getBytesFromFile( source[i] );

				if( isMakefile ){
					makefile = bytes;
				}

				InputStream in = new ByteArrayInputStream( bytes );
				file.create( in, IResource.NONE, new SubProgressMonitor( monitor, 1 ) ); 
				in.close();
			}
			else{
				monitor.worked( 1 );
			}

			if( monitor.isCanceled() ){
				monitor.done();
				return null;
			}
		}

		monitor.done();
		return makefile;
	}

	/**
	 * Tries to find the name of the application specified in <code>makefile</code>.
	 * @param makefile some MAKEFILE
	 * @return the name of the COMPONENT or <code>null</code>
	 */
	public static String getApplicationFromMakefile( String makefile ){
		int index = makefile.indexOf( "COMPONENT" );
		if( index == -1 )
			return null;

		index = makefile.indexOf( "=", index + "COMPONENT".length() );
		if( index == -1 )
			return null;

		// the next word should be the value of COMPONENT
		index++;
		int length = makefile.length();
		while( index < length && Character.isWhitespace( makefile.charAt( index ) )){
			index++;
		}

		int begin = index;
		while( index < length && !Character.isWhitespace( makefile.charAt( index ) )){
			index++;
		}

		if( begin == index )
			return null;

		return makefile.substring( begin, index );
	}

	/**
	 * Creates a new makefile from another makefile.
	 * @param project the project for which to create the file
	 * @param original the contents of the original file
	 * @param monitor for interaction
	 */
	public static void createMakefile( ProjectTOS project, String original, IProgressMonitor monitor ) throws CoreException {
		/*String replacement = Messages.getString("TinyOSNewWizard.newFileMakeContent");
        String preface = Messages.getString("TinyOSNewWizard.newFileMakeIntro");
        createFile( 
                project.getSourceContainer().getFile( "Makefile" ),
                preface+original.replace("include ../Makerules", replacement ),
                monitor );*/

		createMakefile( project, monitor );
	}

	/**
	 * Creates the standard makefile for <code>project</code>.
	 * @param project some project
	 * @param monitor for user interaction
	 * @throws CoreException if the makefile can't be created
	 */
	public static void createMakefile( ProjectTOS project, IProgressMonitor monitor ) throws CoreException {
		//String m1 = Messages.getString( "TinyOSNewWizard.newFileMakeIntro" );
		//String m2 = Messages.getString( "TinyOSNewWizard.newFileMakeContent" );

		FileUtility.createFile( project.getMakefile(), getMakefileStub(), monitor );
	}

	/**
	 * Creates the make-options file.
	 * @param project the project for which to create the file
	 * @param target the target platform
	 * @param application the name of the application, can be <code>null</code>
	 * @param monitor for user interaction
	 * @throws CoreException if the file can't be created
	 */
	public static void createDefaultMakeTargetSkeleton( ProjectTOS project, String target, String application, IProgressMonitor monitor ) throws CoreException {
		FileUtility.createFile(
				project.getProject().getFile( TinyOSCore.MAKEOPTIONS_FILE_NAME ), 
				getMakeOptionContent( project, target, application ),
				monitor );
	}

	private static String getMakeOptionContent( ProjectTOS project, String target, String application ) {
		MakeTargetSkeleton skeleton = new MakeTargetSkeleton( null );

		if( application != null ){
			IContainer source = project.getLegacySourceContainer();
			IFile file = source.getFile( new Path( application + ".nc" ) );
			skeleton.setCustomComponentFile( file );
		}
		
		skeleton.setCustomTarget( target );
		skeleton.setCustomExcludes( MakeExclude.DEFAULT_EXCLUDES );

		return ProjectTargets.convert( skeleton );
	}

	/**
	 * Stores the environment for <code>project</code>.
	 * @param project the project for which to store <code>environment</code>
	 * @param environment the environment of <code>project</code>
	 * @throws CoreException if the environment cannot be set
	 */
	public static void createEnvironmentEntry( ProjectTOS project, IEnvironment environment ) throws CoreException {
		EnvironmentManager.getDefault().setEnvironment( project.getProject(), environment );
	}

	/**
	 * Makes the default setup steps for a new project: create a makefile, set
	 * environment and target.
	 * @param project the project to set up
	 * @param environment the environment of the project
	 * @param platform the target
	 * @param applicationName the name of the main component, can be <code>null</code>
	 * @param monitor to report progress
	 * @throws CoreException if some resource is not available
	 */
	public static void doDefaultSetup( ProjectTOS project, IEnvironment environment, IPlatform platform, String applicationName, IProgressMonitor monitor ) throws CoreException{
		if( monitor == null )
			monitor = new NullProgressMonitor();

		monitor.beginTask( "Setup project", 300 );

		createMakefile( project, new SubProgressMonitor( monitor, 100 ) );
		if( monitor.isCanceled() ){
			monitor.done();
			return;
		}

		createEnvironmentEntry( project, environment );
		monitor.worked( 100 );
		if( monitor.isCanceled() ){
			monitor.done();
			return;
		}

		createDefaultMakeTargetSkeleton( project, platform.getName(), applicationName, new SubProgressMonitor( monitor, 100 ) );

		monitor.done();
	}

	/**
	 * Ensures that <code>project</code> has the builder with the name <code>id</code>.
	 * @param project the project to update
	 * @param id the builder that must exist
	 * @throws CoreException if <code>project</code> can't be accessed
	 */
	private static void addBuilder( IProject project, String id ) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		for( int i = 0; i < commands.length; ++i )
			if( commands[i].getBuilderName().equals( id ) )
				return;
		//add builder to project
		ICommand command = desc.newCommand();
		command.setBuilderName( id );
		ICommand[] nc = new ICommand[ commands.length + 1 ];
		// Add it before other builders.
		System.arraycopy( commands, 0, nc, 1, commands.length );
		nc[0] = command;
		desc.setBuildSpec( nc );
		project.setDescription( desc, null );
	}

	public static void replaceBuilder( IProjectDescription desc, String oldId, String newId ){
		ICommand[] commands = desc.getBuildSpec();
		for( int i = 0; i < commands.length; ++i ){
			if( commands[i].getBuilderName().equals( oldId ) ){
				ICommand command = desc.newCommand();
				command.setBuilderName( newId );
				commands[i] = command;		
			}
		}
		desc.setBuildSpec( commands );
	}
	
	public static void deleteBuilder( IProjectDescription desc, String oldId ){
		ICommand[] commands = desc.getBuildSpec();
		List<ICommand> result = new ArrayList<ICommand>();
		
		for( int i = 0; i < commands.length; ++i ){
			if( !commands[i].getBuilderName().equals( oldId ) ){
				result.add( commands[i] );		
			}
		}
		desc.setBuildSpec( result.toArray( new ICommand[ result.size() ] ) );
	}
	
	/**
	 * Checks and asynchronously updates the nature of <code>project</code>
	 * if necessary. This was necessary because the name of the nature changed
	 * when the plugins id was changed.
	 * @param project the project which may still be using an old nature
	 */
    public static void updateNature( final IProject project ){
    	try{
	    	if( project.hasNature( TinyOSCore.OLD_NATURE_ID ) ){
	    		Job job = new Job( "Correct Nature" ){
	    			@Override
	    			protected IStatus run( IProgressMonitor monitor ){
	    		    	try{
	    		    		monitor.beginTask( "Update Natures", 10 );
	    		    		
	    		    		IProjectDescription description = project.getDescription();
	    			    	String[] natures = description.getNatureIds();
	    			    	String[] copy = new String[ natures.length ];
	    			    	System.arraycopy( natures, 0, copy, 0, natures.length );
	    			    	
	    			    	for( int i = 0; i < copy.length; i++ ){
	    			    		if( TinyOSCore.OLD_NATURE_ID.equals( copy[i] )){
	    			    			copy[i] = TinyOSCore.NATURE_ID;
	    			    		}
	    			    	}
	    			    	
	    			    	description.setNatureIds( copy );

	    			    	ProjectTOSUtility.replaceBuilder( description, "TinyOS.projectbuilder", "tinyos.yeti.core.projectbuilder" );
	    			    	ProjectTOSUtility.deleteBuilder( description, "TinyOS.makefilebuilder" );
	    			    	
	    			    	project.setDescription( description, new SubProgressMonitor( monitor, 10 ) );
	    			    	monitor.done();
	    			    	return Status.OK_STATUS;
	    		    	}
	    		    	catch( CoreException ex ){
	    		    		TinyOSPlugin.log( ex );
	    		    		return new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, ex.getMessage() );
	    		    	}
	    			}
	    		};
	    		job.setRule( ResourcesPlugin.getWorkspace().getRoot() );
	    		job.setPriority( Job.INTERACTIVE );
	    		job.setSystem( true );
	    		job.schedule();
	    	}
    	}
    	catch( CoreException ex ){
    		TinyOSPlugin.log( ex );
    	}
    }
}
