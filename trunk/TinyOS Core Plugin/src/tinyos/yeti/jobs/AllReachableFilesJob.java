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
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IEnvironment.SearchFlag;
import tinyos.yeti.make.MakeTarget;

/**
 * Secure listing up of all reachable files of a project
 * @author Benjamin Sigg
 */
public class AllReachableFilesJob extends CancelingJob implements IPublicJob{
    private String[] extensions;
    private MakeTarget target;
    private ProjectTOS project;
    private File[] files;
    private Set<SearchFlag> flags;

    public AllReachableFilesJob( ProjectTOS project, MakeTarget target, String... extensions ){
    	this( project, target, null, extensions );
    }
    
    public AllReachableFilesJob( ProjectTOS project, MakeTarget target, Set<SearchFlag> flags, String... extensions ){
        super( "all reachable files (" + Arrays.toString( extensions ) + "): " + project.getProject().getName() );
        this.project = project;
        this.target = target;
        this.extensions = extensions;
        this.flags = flags;
        
        if( this.flags == null ){
        	this.flags = Collections.emptySet();
        }

        setRule( project.getProject() );
        setSystem( true );
    }

    @Override
    public IStatus run( IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        IEnvironment environment = project.getEnvironment();
        if( environment != null && target != null ){
        	files = environment.getAllReachableFiles(
                    target.getProject(), 
                    target.getIncludes(),
                    target.getExcludes(),
                    target.getSensorBoards(),
                    target.getTarget(), 
                    target.isNostdinc(),
                    extensions,
                    flags,
                    monitor );
        }

        if( files == null )
            files = new File[]{};
        
        monitor.done();
        if( monitor.isCanceled() )
            return Status.CANCEL_STATUS;
        return Status.OK_STATUS;
    }

    public File[] getFiles(){
        return files;
    }
}
