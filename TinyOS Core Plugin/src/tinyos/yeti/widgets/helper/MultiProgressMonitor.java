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
package tinyos.yeti.widgets.helper;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An {@link IProgressMonitor} which forwards any call to two other
 * monitors.
 * @author Benjamin Sigg
 */
public class MultiProgressMonitor implements IProgressMonitor{
    private IProgressMonitor alpha;
    private IProgressMonitor beta;
    
    public MultiProgressMonitor( IProgressMonitor alpha, IProgressMonitor beta ){
        this.alpha = alpha;
        this.beta = beta;
    }

    public void beginTask( String name, int totalWork ){
        alpha.beginTask( name, totalWork );
        beta.beginTask( name, totalWork );
    }

    public void done(){
        alpha.done();
        beta.done();
    }

    public void internalWorked( double work ){
        alpha.internalWorked( work );
        beta.internalWorked( work );
    }

    public boolean isCanceled(){
        return alpha.isCanceled() || beta.isCanceled();
    }

    public void setCanceled( boolean value ){
         alpha.setCanceled( value );
         beta.setCanceled( value );
    }

    public void setTaskName( String name ){
        alpha.setTaskName( name );
        beta.setTaskName( name );
    }

    public void subTask( String name ){
         alpha.subTask( name );
         beta.subTask( name );
    }

    public void worked( int work ){
        alpha.worked( work );
        beta.worked( work );
    }
}
