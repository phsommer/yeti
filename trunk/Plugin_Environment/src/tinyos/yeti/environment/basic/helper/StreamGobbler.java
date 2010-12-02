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
package tinyos.yeti.environment.basic.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

/**
 * A {@link StreamGobbler} reads from a given {@link InputStream} and might
 * forward its readings to some {@link OutputStream}.
 *
 * @author Roland Schuler
 * @author Benjamin Sigg
 *
 */
public class StreamGobbler{
    private InputStream input;
    private OutputStream output;
    
    boolean usePrintStream = false;
    
    private Running running = new Running();
    private Thread thread;
    
    private StreamGobbler partner;
    private Leader leader;
    
    public StreamGobbler( InputStream input, OutputStream output ){
        this.input = input;
        this.output = output;
    }
    
    /**
     * Sets the partner of this gobble, this gobbler will not process its
     * {@link InputStream} at the same time when its partner is reading.
     * @param partner the partner
     */
    public void setPartner( StreamGobbler partner ){
		this.partner = partner;
		leader = partner.leader;
		if( leader == null ){
			leader = new Leader();
		}
	}
    
    protected void setInput( InputStream input ){
        this.input = input;
    }
    
    protected void setOutput( OutputStream output ){
        this.output = output;
    }
    
    public synchronized void start(){
        if( thread != null && thread.isInterrupted() )
            thread = null;
        
        if( thread == null ){
            thread = new Thread( running );
            thread.start();
        }
    }
    
    public synchronized void stop(){
        if( thread != null ){
            thread.interrupt();
        }
    }
    
    public void join(){
        Thread thread = this.thread;
        if( thread != null ){
            while( thread.isAlive() ){
                try{
                    thread.join();
                }
                catch ( InterruptedException e ){
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Called every time when a new byte was transmitted. The default implementation
     * does do nothing.
     * @param value the byte that was transmitted or -1
     */
    protected void piped( int value ){
        // nothing
    }
    
    /**
     * Tells whether this gobble is currently processing input from its
     * {@link InputStream}.
     * @return <code>true</code> if this gobbler is neither waiting for input
     * nor stopped.
     */
    public synchronized boolean isReading(){
    	if( thread == null )
    		return false;
    	
    	return running.reading;
    }
    
    /**
     * Used to elect a leader when two gobblers try to process their stream
     * at the same time. The winner processes its stream first, afterwards
     * the looser processes its stream. Leadership can change whenever a 
     * gobbler blocks.
     */
    private static class Leader{
    	private int time;
    	
    	public synchronized int time(){
    		time++;
    		return time;
    	}
    }
    
    private class Running implements Runnable{
    	private volatile boolean reading = true;
    	private volatile int time = 0;
    	
        public void run(){
            try {
            	if( leader != null ){
            		time = leader.time();
            	}
            	
                while( !Thread.interrupted() ){
                	int next = 0;
                	
                	if( input.available() > 0 ){
                		next = input.read();
                	}
                	else{
                		reading = false;
                		next = input.read();
                		if( leader != null ){
                			time = leader.time();
                		}
                		reading = true;
                	}
                	
                	if( partner != null ){
                		while( partner.isReading() && time > partner.running.time ){
                			Thread.yield();
                		}
                	}
                	
                    if( next == -1 ){
                        piped( next );
                        synchronized( StreamGobbler.this ){
                        	thread = null;
                        }
                        return;
                    }
                    
                    if( output != null ){
                        output.write( next );
                    }
                    piped( next );
                }
            }
            catch( InterruptedIOException ex ){
                // ignore
            }
            catch( IOException ioe ){
                ioe.printStackTrace();
            }
        }
    }
}
