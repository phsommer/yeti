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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;

/**
 * A job that can inform observers when it is canceled.
 * @author Benjamin Sigg
 */
public abstract class CancelingJob extends Job implements ICancelingJob, IPublicJob{
    private List<ICancelListener> listeners = new ArrayList<ICancelListener>();

    public CancelingJob( String name ){
        super( name );
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter( Class adapter ){
        if( ICancelingJob.class.equals( adapter ))
            return this;
        
        return super.getAdapter( adapter );
    }
    
    public Job asJob(){
        return this;
    }
    
    @Override
    protected synchronized void canceling(){
        super.canceling();
        
        for( ICancelListener listener : listeners.toArray( new ICancelListener[ listeners.size() ] ))
            listener.cancel( this );
    }
    
    public synchronized void addCancelListener( ICancelListener listener ){
        listeners.add( listener );
    }
    
    public synchronized void removeCancelListener( ICancelListener listener ){
        listeners.remove( listener );
    }
}
