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
package tinyos.yeti.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.MarkerUtilities;

import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IProjectChecker;
import tinyos.yeti.ep.IProjectCheckerCallback;
import tinyos.yeti.utility.TinyOSProjectsListener;

public class ProjectChecker{
	private IProjectChecker[] checkers;
	private List<CheckSequence> unfinishedChecks = new ArrayList<CheckSequence>();
	
	public ProjectChecker(){
		loadCheckers();
		TinyOSPlugin.getDefault().getProjects().addListener( 
				new TinyOSProjectsListener(){
					public void projectAdded( IProject project ){
						recheck( project );
					}
					public void projectRemoved( IProject project ){
						// ignore
					}
				});
	}
	
	public void recheck( IProject project ){
		synchronized( unfinishedChecks ){
			for( CheckSequence check : unfinishedChecks ){
				if( check.project.equals( project ) ){
					check.schedule( 150 );
					return;
				}
			}
			
			CheckSequence check = new CheckSequence( project );
			unfinishedChecks.add( check );
			check.schedule();
		}
	}
	
	private void loadCheckers(){
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = reg.getExtensionPoint( TinyOSPlugin.PLUGIN_ID + ".ProjectChecker" );
        List<IProjectChecker> result = new ArrayList<IProjectChecker>();

        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( "checker" ) ){
                    try{
                    	result.add( (IProjectChecker)element.createExecutableExtension( "class" ));
                    }
                    catch ( CoreException e ){
                    	TinyOSPlugin.log( e );
                    }
                }
            }
        }
        
        checkers = result.toArray( new IProjectChecker[ result.size() ] );
        for( IProjectChecker checker : checkers ){
        	checker.connect( this );
        }
	}
	
	private void clearMessages( IProject project ) throws CoreException{
    	project.deleteMarkers( IMarker.PROBLEM, true, IResource.DEPTH_ZERO );
	}
	
	private class CheckSequence{
		private IProject project;
		
		private MessageClear clear;
		private Check check;
		
		public CheckSequence( IProject project ){
			this.project = project;
			
			clear = new MessageClear( this );
			check = new Check( this );
		}
		
		public void schedule(){
			schedule( 0 );
		}
		
		public void schedule( long delay ){
			clear.schedule( delay );
		}
		
		public void scheduleCheck(){
			check.schedule();
		}
	}
	
	private class MessageClear extends UIJob{
		private CheckSequence sequence;
		
		public MessageClear( CheckSequence sequence ){
			super( "Clear messages '" + sequence.project.getName() + "'" );
			this.sequence = sequence;
			setSystem( true );
			setPriority( DECORATE );
		}
		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try{
				IProject project = sequence.project;
				monitor.beginTask( "Clean " + project.getName(), checkers.length );
				
				if( !project.isOpen() || !project.hasNature( TinyOSCore.NATURE_ID )){
					monitor.done();
					return Status.OK_STATUS;
				}
				
				clearMessages( project );
				sequence.scheduleCheck();
				return Status.OK_STATUS;
			}
			catch( CoreException ex ){
				TinyOSPlugin.log( ex );
				return new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, ex.getMessage(), ex );
			}
		}
	}
	
	private class Check extends Job implements IProjectCheckerCallback{
		private CheckSequence sequence;
		
		public Check( CheckSequence sequence ){
			super( "Check " + sequence.project.getName() );
			this.sequence = sequence;
			setPriority( LONG );
			setRule( sequence.project );
		}
		
		@Override
		protected IStatus run( IProgressMonitor monitor ){
			try{
				IProject project = sequence.project;
				monitor.beginTask( "Clean " + project.getName(), checkers.length );
				
				for( IProjectChecker checker : checkers ){
					checker.checkProject( project, new SubProgressMonitor( monitor, 1 ), this );
					if( monitor.isCanceled() ){
						return Status.CANCEL_STATUS;
					}
				}
				
				synchronized( unfinishedChecks ){
					unfinishedChecks.remove( this );
				}
				
				monitor.done();
				return Status.OK_STATUS;
			}
			catch( CoreException ex ){
				monitor.done();
				return new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, ex.getMessage(), ex );
			}
		}
		
		public void reportError( String message ) throws CoreException{
			report( message, IMarker.SEVERITY_ERROR );
		}
		
		public void reportWarning( String message ) throws CoreException{
			report( message, IMarker.SEVERITY_WARNING );
		}
		
		public void reportInfo( String message ) throws CoreException{
			report( message, IMarker.SEVERITY_INFO );
		}
		
		private void report( String message, int severity ) throws CoreException{
	    	Map<String, Object> map = new HashMap<String, Object>();
	        MarkerUtilities.setMessage( map, message );
	        map.put( IMarker.SEVERITY, severity );
	        map.put( IMarker.PRIORITY, IMarker.PRIORITY_HIGH );
	        MarkerUtilities.createMarker( sequence.project, map, IMarker.PROBLEM );	
		}
	}
}
