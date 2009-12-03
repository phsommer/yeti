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
package tinyos.yeti.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.MultiMakeExclude;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.preferences.PreferenceConstants;

public class TinyOSProjectBuilder2 extends IncrementalProjectBuilder {
    public TinyOSProjectBuilder2() {
        super();
    }

    @Override
    protected void clean( IProgressMonitor monitor ){
        Debug.enter();
        try{
	        ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( getProject() );
	
	        if( tos.getModel().secureThread() ){
	            boolean full = TinyOSPlugin.getDefault().getPreferenceStore().getBoolean( PreferenceConstants.CLEAN_FULL );
	            
	            tos.deleteCache( full, monitor );
	            
	            try{
	                tos.acceptSourceFiles( new CleanVisitor() );
	            }
	            catch ( CoreException e ){
	                TinyOSPlugin.warning( e.getStatus() );
	            }
	            
	            if( full ){
	                tos.initialize();
	            }
	        }
	        else{
	            CancelingJob job = new CancelingJob( "clean" ){
	                @Override
	                public IStatus run( IProgressMonitor monitor ){
	                    clean( monitor );
	                    return Status.OK_STATUS;
	                }
	            };
	
	            job.setSystem( true );
	            job.setPriority( Job.SHORT );
	            tos.getModel().runJob( job, monitor );
	        }
        }
        catch( MissingNatureException ex ){
        	TinyOSPlugin.log( ex );
        }
        Debug.leave();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build( final int kind, final Map args, IProgressMonitor monitor ) {
    	try{
	        if( monitor == null )
	            monitor = new NullProgressMonitor();
	
	        if( kind == IncrementalProjectBuilder.FULL_BUILD ){
	            fullBuild(monitor);
	        }
	        else {
	            IResourceDelta delta = getDelta(getProject());
	            if (delta == null) {
	                fullBuild(monitor);
	            }
	            else {
	                incrementalBuild(monitor,delta);
	            }
	        }
    	}
    	catch( MissingNatureException ex ){
    		TinyOSPlugin.log( ex );
    	}
        return null;
    }

    protected void fullBuild( IProgressMonitor monitor ) throws MissingNatureException{
        ProjectTOS project = TinyOSPlugin.getDefault().getProjectTOS( getProject() );
        project.getBuilder().doFullBuild();
    }

    private void incrementalBuild( IProgressMonitor monitor, IResourceDelta delta ) throws MissingNatureException{
        final DeltaResourcCollector collector = new DeltaResourcCollector();
        try {
            delta.accept( collector );
        }
        catch (CoreException e) {
            e.printStackTrace();
        }

        ProjectTOS project = getProjectTOS();
        incrementalBuild( collector.changed, collector.removed, project, monitor );
    }

    public ProjectTOS getProjectTOS() throws MissingNatureException{
        return TinyOSPlugin.getDefault().getProjectTOS( getProject() );
    }

    public void incrementalBuild( List<IResource> changed, List<IResource> removed, ProjectTOS project, IProgressMonitor monitor ){
        if( !changed.isEmpty() || !removed.isEmpty() ){
            project.getBuilder().doIncrementalBuild( changed, removed );
        }
    }

    private static class CleanVisitor implements IResourceVisitor {
        public boolean visit(IResource res) {
            try {
                res.deleteMarkers(IMarker.PROBLEM,true,IResource.DEPTH_INFINITE);
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    private class DeltaResourcCollector implements IResourceDeltaVisitor {
        public List<IResource> changed = new ArrayList<IResource>();
        public List<IResource> removed = new ArrayList<IResource>();

        private MakeTarget target;
        private MultiMakeExclude exclude;

        public DeltaResourcCollector() throws MissingNatureException{
        	target = getProjectTOS().getMakeTarget();
        	exclude = target == null ? null : target.getExclude();
        }
        
        public boolean visit( IResourceDelta delta ) throws CoreException {
            String extension = delta.getResource().getFileExtension();
            if( "nc".equals( extension ) || "h".equals( extension )){

                if( target != null && exclude != null ){
                    IResource parent = delta.getResource().getParent();
                    if( parent != null ){
                        IPath location = parent.getLocation();
                        if( location != null ){
                            if( exclude.shouldExclude( location.toFile() )){
                                return true;
                            }
                        }
                    }
                }

                switch( delta.getKind() ){
                    case IResourceDelta.ADDED:
                    case IResourceDelta.ADDED_PHANTOM:
                    case IResourceDelta.CHANGED:
                    case IResourceDelta.REPLACED:
                        changed.add( delta.getResource() );
                        break;
                    case IResourceDelta.REMOVED:
                    case IResourceDelta.REMOVED_PHANTOM:
                        removed.add( delta.getResource() );
                        break;
                }
            }
            return true;
        }
    }
}