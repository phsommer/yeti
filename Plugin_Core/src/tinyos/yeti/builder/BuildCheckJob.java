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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.jobs.ProgressMonitorCheckingClose;

/**
 * A job that checks each file for its build state and might start building
 * some files. 
 * @author Benjamin Sigg
 */
public class BuildCheckJob extends CancelingJob{
    private ProjectTOS project;
    private int autoTriggered;
    
    public BuildCheckJob( ProjectTOS project, int autoTriggered ){
        super( "Check consistent: '" + project.getProject().getName() + "'" );
        setRule( project.getProject() );
        this.autoTriggered = autoTriggered;
        this.project = project;
    }
    
    @Override
    public IStatus run( IProgressMonitor monitor ){
        monitor = new ProgressMonitorCheckingClose( monitor );
        monitor.beginTask( "Build Check", 2 );
        
        // collect all resources which need to be checked
        final ProjectResourceCollector collector = new ProjectResourceCollector();
        CancelingJob collectorJob = new CancelingJob( "Collect" ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                monitor.beginTask( "Collect", IProgressMonitor.UNKNOWN );
                try{
                    project.acceptSourceFiles( collector );
                }
                catch ( CoreException e ){
                    TinyOSPlugin.warning( e.getStatus() );
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        collectorJob.setPriority( getPriority() );
        collectorJob.setSystem( true );
        project.getModel().runJob( collectorJob, new SubProgressMonitor( monitor, 1 ) );
        
        if( monitor.isCanceled() ){
            monitor.done();
            return Status.CANCEL_STATUS;
        }
        
        // check all resources for consistency
        final List<IResource> build = new ArrayList<IResource>();
        final List<IResource> changed = new ArrayList<IResource>();
        
        IProgressMonitor resourceMonitor = new SubProgressMonitor( monitor, 1 );
        resourceMonitor.beginTask( "Build Check", collector.resources.size() );
        
        for( final IResource resource : collector.resources ){
            CancelingJob checkJob = new CancelingJob( "Check" ){
                @Override
                public IStatus run( IProgressMonitor monitor ){
                    if( project.getModel().checkChange( resource )){
                        changed.add( resource );
                    }
                    else{
                        if( project.getModel().checkBuild( resource, monitor ))
                            build.add( resource );
                    }
                    return Status.OK_STATUS;
                }
            };
            checkJob.setPriority( getPriority() );
            checkJob.setSystem( true );
            project.getModel().runJob( checkJob, new SubProgressMonitor( resourceMonitor, 1 ) );
            if( monitor.isCanceled() ){
                monitor.done();
                return Status.CANCEL_STATUS;
            }
        }
        
        // rebuild the resources which are not consistent
        if( build.size() > 0 || changed.size() > 0 ){
            project.getBuilder().doAutoBuild( build, changed, autoTriggered );
        }
        
        resourceMonitor.done();
        monitor.done();
        return Status.OK_STATUS;
    }
}
