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

import tinyos.yeti.TinyOSPlugin;

/**
 * A monitor that checks whether the workspace is still alive. If not
 * then this monitor reports that it is canceled.
 * @author Benjamin Sigg
 */
public class ProgressMonitorCheckingClose implements IProgressMonitor{
    private IProgressMonitor monitor;
    
    public ProgressMonitorCheckingClose( IProgressMonitor monitor ){
        this.monitor = monitor;
    }
    
    public void beginTask( String name, int totalWork ){
        monitor.beginTask( name, totalWork );
    }

    public void done(){
        monitor.done();
    }

    public void internalWorked( double work ){
        monitor.internalWorked( work );
    }

    public boolean isCanceled(){
        return monitor.isCanceled() || TinyOSPlugin.isClosing();
    }

    public void setCanceled( boolean value ){
        monitor.setCanceled( value );
    }

    public void setTaskName( String name ){
        monitor.setTaskName( name );
    }

    public void subTask( String name ){
        monitor.subTask( name );
    }

    public void worked( int work ){
        monitor.worked( work );
    }
}
