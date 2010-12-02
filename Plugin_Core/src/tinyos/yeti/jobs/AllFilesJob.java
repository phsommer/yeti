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
package tinyos.yeti.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.IEnvironment.SearchFlag;
import tinyos.yeti.make.MakeTarget;

/**
 * A job that can collect all files that are accessible by a project.
 * @author besigg
 */
public class AllFilesJob extends CancelingJob implements IPublicJob{
	private boolean includeProject;
	private ProjectTOS project;
	private List<IParseFile> files = new ArrayList<IParseFile>();
	
	public AllFilesJob( ProjectTOS project, boolean includeProject, String name ){
		super( name );
		this.includeProject = includeProject;
		this.project = project;
	}
	
	public List<IParseFile> getFiles(){
		return files;
	}
	
	@Override
	public IStatus run( IProgressMonitor monitor ){
        monitor.beginTask( "Parse All", 7 );
        SubProgressMonitor runMonitor = new SubProgressMonitor( monitor, 3 );
        runMonitor.beginTask( "Parse All", 3 );
        
        MakeTargetJob meJob = new MakeTargetJob( project );
        meJob.setPriority( getPriority() );
        project.getModel().runJob( meJob, runMonitor );
        MakeTarget me = meJob.getTarget();

        if( me == null ){
            cancel();
            monitor.done();
            return Status.CANCEL_STATUS;
        }
        
        Set<SearchFlag> flags = new HashSet<SearchFlag>();
        if( !includeProject ){
        	flags.add( SearchFlag.EXCLUDE_PROJECT );
        }

        AllReachableFilesJob reach = new AllReachableFilesJob( project, me, flags, "nc", "h" );
        reach.setPriority( getPriority() );
        project.getModel().runJob( reach, runMonitor );
        
        if( monitor.isCanceled() ){
            monitor.done();
            return Status.CANCEL_STATUS;
        }

        File[] fnc = reach.getFiles();
        
        if( fnc != null ){
        	for( File file : fnc ){
        		IParseFile pFile = project.getModel().parseFile( file );
        		if( pFile != null ){
        			files.add( pFile );
        		}
        	}
        }
        
        if( monitor.isCanceled() )
        	return Status.CANCEL_STATUS;
        return Status.OK_STATUS;
    }
}
