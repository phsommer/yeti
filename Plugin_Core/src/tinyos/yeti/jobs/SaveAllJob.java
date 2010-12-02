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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISaveablePart;

/**
 * This job can store a list of {@link ISaveablePart}s.
 * @author Benjamin Sigg
 */
public class SaveAllJob extends Job{
    private Display display;
    private List<ISaveablePart> saveable;
    
    public SaveAllJob( Display display, List<ISaveablePart> saveable ){
        super( "Save Resources" );
        this.saveable = saveable;
        this.display = display;
    }
    
    @Override
    public IStatus run( final IProgressMonitor monitor ){
        monitor.beginTask( "Save", saveable.size() );
        for( final ISaveablePart part : saveable ){
            display.syncExec( new Runnable(){
                public void run(){
                    part.doSave( new SubProgressMonitor( monitor, 1 ) );        
                }
            });
            
            if( monitor.isCanceled() ){
                break;
            }
        }
        
        monitor.done();
        if( monitor.isCanceled() ){
            return Status.CANCEL_STATUS;
        }
        else{
            completed();
            return Status.OK_STATUS;
        }
    }
    
    /**
     * Called when this job completed, the resources are now safely stored.
     */
    protected void completed(){
    	// nothing
    }
}
