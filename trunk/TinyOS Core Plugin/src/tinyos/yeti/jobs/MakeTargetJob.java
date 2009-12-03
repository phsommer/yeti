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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.make.MakeTarget;

/**
 * Secure access to the make target
 * @author Benjamin Sigg
 */
public class MakeTargetJob extends CancelingJob implements IPublicJob{
    private MakeTarget target;
    private ProjectTOS project;
    
    public MakeTargetJob( ProjectTOS project ){
        super( "access MakeTarget " + project.getProject().getName() );
        this.project = project;
        
        setRule( project.getProject() );
        setSystem( true );
    }
    
    @Override
    public IStatus run( IProgressMonitor monitor ){
        monitor.beginTask( "access", 1 );
        target = project.getMakeTarget();
        monitor.done();
        return Status.OK_STATUS;
    }
    
    public MakeTarget getTarget(){
        return target;
    }
}
