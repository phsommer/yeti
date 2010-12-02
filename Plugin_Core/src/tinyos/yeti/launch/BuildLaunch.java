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
package tinyos.yeti.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.jobs.InvokeMakeJob;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.MakeTarget;

/**
 * {@link ILaunchConfigurationDelegate} that will call 'ncc'.
 * @author Benjamin Sigg
 */
public class BuildLaunch implements ILaunchConfigurationDelegate{
	public void launch( ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor ) throws CoreException{
		MakeTarget target = LaunchConverter.read( configuration );
		if( target.getProject() == null ){
			throw new CoreException( new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, "No project selected, can't build" ));
		}
		
		if( !target.getProject().hasNature( TinyOSCore.NATURE_ID )){
			throw new CoreException( new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, "Project '" + target.getProject().getName() + "' is not a TinyOS project" ));
		}

		build( target, monitor );
	}
	
	public void buildAsync( final IMakeTarget target ){
		Job job = new Job( "Build" ){
			@Override
			protected IStatus run( IProgressMonitor monitor ){
				build( target, monitor );
				if( monitor.isCanceled() )
					return Status.CANCEL_STATUS;
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	public void build( IMakeTarget target, IProgressMonitor monitor ){
		build( target, true, monitor );
	}
	
	public void build( IMakeTarget target, boolean allowVeto, IProgressMonitor monitor ){
		monitor.beginTask( "Build '" + target.getProject().getName() + "'", 1000 );

		if( allowVeto && !LaunchManager.getDefault().launch( new SubProgressMonitor( monitor, 500 ) ) ){
			monitor.done();
			return;
		}
		
		if( monitor.isCanceled() ){
			return;
		}
		
		InvokeMakeJob job = new InvokeMakeJob( target );
		job.run( new SubProgressMonitor( monitor, 500 ) );
	}
}
