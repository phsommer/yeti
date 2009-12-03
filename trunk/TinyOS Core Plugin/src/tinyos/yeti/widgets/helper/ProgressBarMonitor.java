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
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * An {@link IProgressMonitor} which forwards its progress
 * to a {@link ProgressBar}.
 * @author Benjamin Sigg
 */
public class ProgressBarMonitor implements IProgressMonitor{
    private ProgressBar bar;
    
    private boolean running = false;
    private int totalWork = 1;
    private double work = 0;
    private boolean canceled = false;
    
    public ProgressBarMonitor( ProgressBar bar ){
        this.bar = bar;
    }
    
    private void update(){
        try{
            Display display = bar.getDisplay();
            display.asyncExec( new Runnable(){
                public void run(){
                    if( !bar.isDisposed() ){
                        bar.setEnabled( running );
                        bar.setMaximum( totalWork );
                        bar.setSelection( running ? (int)Math.round( work ) : 0 );
                    }
                }
            });
        }
        catch( SWTException ex ){
            // alright, the bar is disposed, no need to throw an exception...
        }
    }

    public void beginTask( String name, int totalWork ){
        this.totalWork = Math.max( 1, totalWork );
        running = true;
        update();
    }

    public void done(){
        running = false;
        update();
    }

    public void internalWorked( double work ){
        this.work += work;
        update();
    }

    public boolean isCanceled(){
        return canceled;
    }

    public void setCanceled( boolean value ){
        canceled = value;
    }

    public void setTaskName( String name ){
        // ignore
    }

    public void subTask( String name ){
        // ignore
    }

    public void worked( int work ){
        internalWorked( work );
    }
}
