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
package tinyOS.debug.launch;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.CDTAbstractionLayer.CDTLaunchConfigConst;
import tinyOS.debug.CDTAbstractionLayer.CDTLaunchConfigurationDelegate;
import tinyOS.debug.launch.configuration.ILaunchPrerun;
import tinyOS.debug.launch.configuration.ITinyOSDebugLaunchConstants;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.launch.BuildLaunch;
import tinyos.yeti.launch.LaunchManager;
import tinyos.yeti.make.IMakeTarget;

public class LaunchConfigurationDelegate implements
ILaunchConfigurationDelegate {

	/**
	 * The real delegate. The launch will be delegated to this delegate after the 
	 * the gdb proxy has been started.
	 */
	ILaunchConfigurationDelegate realDelegate;

	public LaunchConfigurationDelegate() {
		super();
		realDelegate = new CDTLaunchConfigurationDelegate();
	}

	private boolean procDone(Process p) {
		try {
			p.exitValue();
			return true;
		}
		catch(IllegalThreadStateException e) {
			return false;
		}
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	protected void abort(String message, String detail, int code) throws CoreException {
		IStatus status;
		MultiStatus multiStatus = new MultiStatus(TinyOSDebugPlugin.getUniqueIdentifier(), code, message, null);
		multiStatus.add(new Status(IStatus.ERROR, TinyOSDebugPlugin.getUniqueIdentifier(), code, detail, null));
		status= multiStatus;
		throw new CoreException(status);
	}

	/**
	 * Extracts the Gdb proxy command from the configuration.
	 * @param configuration
	 * @return The command in a string array suitable for a ProcessBuilder.
	 */
	private String getCommand(ILaunchConfiguration configuration) {
		String command = null;
		try {
			command = configuration.getAttribute(ITinyOSDebugLaunchConstants.ATTR_GDB_PROXY_COMMAND, (String)null);
		} catch (CoreException e) {
			TinyOSPlugin.getDefault().log("Exception while getting command from launch configuration", e);
		}
		return command;
	}
	
	/**
	 * Asks a {@link ILaunchPrerun} to translate the configuration to a {@link IMakeTarget}
	 * and runs this target.
	 * @param configuration
	 * @param launch
	 * @param monitor
	 * @throws CoreException
	 */
	private void prerunCommand( ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor ) throws CoreException{
		String prerunId = configuration.getAttribute( ITinyOSDebugLaunchConstants.ATTR_CURRENT_LAUNCH_PRERUN, (String)null );
		ILaunchPrerun prerun = getPrerun( prerunId );
		if( prerun == null )
			return;
		
		String projectName = configuration.getAttribute(CDTLaunchConfigConst.ATTR_PROJECT_NAME, (String)null);
		if( projectName == null )
			throw new CoreException( new Status( IStatus.ERROR, TinyOSDebugPlugin.PLUGIN_ID, "No project selected" ));
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
		if( !project.isAccessible() )
			throw new CoreException( new Status( IStatus.ERROR, TinyOSDebugPlugin.PLUGIN_ID, "Project '" + projectName + "' not accessible." ));
		
		IMakeTarget target = prerun.getMakeTarget( configuration, project );
		if( target == null )
			return;
		
		BuildLaunch build = new BuildLaunch();
		build.build( target, false, new SubProgressMonitor( monitor, 1 ) );
	}
	
	private ILaunchPrerun getPrerun( String id ) throws CoreException{
		if( id == null )
			return null;
		
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint extPoint = reg.getExtensionPoint( "tinyos.yeti.debugger.launchPrerun" );

        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( "prerun" ) ){
                	if( id.equals( element.getAttribute( "id" ) )){
                		return (ILaunchPrerun)element.createExecutableExtension( "class" );
                	}
                }
            }
        }

        throw new CoreException( new Status( IStatus.ERROR, TinyOSDebugPlugin.PLUGIN_ID, "unknown id for a launch-prerun: '" + id + "'" ) );
	}
	
	/**
	 * Starts the configured GDB proxy.
	 * @param configuration
	 * @param launch
	 * @param monitor
	 * @throws CoreException
	 */
	private void startGDBProxy(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String[] command = null;
		String rawCommand = getCommand(configuration);
		if(rawCommand != null) {
			rawCommand = rawCommand.trim();
			command = rawCommand.split(" ");
		}
		if(command != null && command.length > 0 && command[0].length() > 0) {
			monitor.subTask("Starting "+rawCommand);
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(command);
			Process p = null;
			try {
				p = pb.start();
				DebugPlugin.newProcess(launch, p, rawCommand);
				Thread.sleep(getStartupDelay(configuration));
			} catch (IOException e) {
				TinyOSPlugin.getDefault().log("Exception while starting command: "+rawCommand, e);
				abort("Could not start GDB Proxy!", "Command was: "+rawCommand, 0);
			} catch (InterruptedException e) {
				TinyOSPlugin.getDefault().log("Exception while waiting for command: "+rawCommand, e);
			}
			if(p == null || procDone(p)) {
				abort("GDB proxy died!", "Command was: "+rawCommand+"\n  Check console for more information!", 0);
			}
		}
	}

	private int getStartupDelay(ILaunchConfiguration configuration) {
		int delay = 0;
		try {
			String delayStr = configuration.getAttribute(ITinyOSDebugLaunchConstants.ATTR_GDB_PROXY_STARTUP_DELAY, (String)null);
			delay = Integer.parseInt(delayStr);
		} catch (CoreException e) {
			TinyOSPlugin.getDefault().log("Exception while getting command from launch configuration", e);
		}
		return delay;
	}
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if ( monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		if(monitor.isCanceled()) {
			return;
		}

		monitor.beginTask( "Debugging", 5 );
		
		if( !LaunchManager.getDefault().launch( new SubProgressMonitor( monitor, 1 ) ) ){
			monitor.done();
			return;
		}
		
		setCNature(configuration);
		
		prerunCommand( configuration, launch, new SubProgressMonitor( monitor, 1 ) );
		if( monitor.isCanceled() ){
			monitor.done();
			return;
		}
		
		startGDBProxy( configuration, launch, new SubProgressMonitor( monitor, 1 ) );		
		if(monitor.isCanceled()) {
			monitor.done();
			return;
		}
		
		realDelegate.launch( configuration, mode, launch, new SubProgressMonitor( monitor, 2 ) );
		monitor.done();
	}

	/**
	 * Sets the CDT c nature if the project being launched hasn't already the CDT c nature. Projects
	 * being launched by the CDT must have c nature.
	 * @param configuration
	 */
	private void setCNature(ILaunchConfiguration configuration){
		try {
			String projectName = configuration.getAttribute(CDTLaunchConfigConst.ATTR_PROJECT_NAME, (String)null);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProject project = workspace.getRoot().getProject(projectName);
			if(project != null && !project.hasNature(CDTLaunchConfigConst.CDT_PROJECT_NATURE)) {
				IProjectDescription projectDescription = project.getDescription();
				String[] natures = projectDescription.getNatureIds();
				String[] newNatures;
				newNatures = new String[ natures.length + 1 ];
				System.arraycopy(
						natures,
						0,
						newNatures,
						0,
						natures.length);
				newNatures[natures.length] = CDTLaunchConfigConst.CDT_PROJECT_NATURE;

				projectDescription.setNatureIds( newNatures );
				project.setDescription(projectDescription, null);
			}

		} catch (CoreException e) {
			TinyOSPlugin.getDefault().log("Exception while setting the c nature", e);
		}

	}
}
