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
package tinyos.yeti.environment.basic.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import tinyos.yeti.Debug;

/**
 * An implementation of {@link ICommandExecuter} doing the work which has
 * to be done anyway.
 * @author Benjamin Sigg
 */
public abstract class AbstractCommandExecuter implements ICommandExecuter{

    public <R> R execute( ICommand<R> command ) throws InterruptedException, IOException{
        return execute( command, null, null, null, null );
    }
    
    public <R> R execute( ICommand<R> command, IProgressMonitor monitor,
            OutputStream info, OutputStream out, OutputStream error ) throws InterruptedException,
            IOException{
        
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "execute command", IProgressMonitor.UNKNOWN );
        
        if( !command.setup() ){
            monitor.done();
            return null;
        }
        
        if( info != null ){
            StringBuilder builder = new StringBuilder();
            
            builder.append( "run: " );
            for( String cmd : command.getCommand() ){
                builder.append( " \"" );
                builder.append( cmd );
                builder.append( "\"" );
            }
            builder.append( "\n" );
            
            builder.append( "at " );
            builder.append( DateFormat.getDateTimeInstance().format( new Date() ) );
            builder.append( "\n" );
            
            info.write( builder.toString().getBytes() );
        }

        long nanoStart = System.nanoTime();
        IExecutionResult result = run( command, monitor, info, out, error );
        long nanoEnd = System.nanoTime();
        
        if( Debug.DEBUG ){
            java.lang.System.out.println( result );
            java.lang.System.out.println("outputting: command ");
            String[] cmd = command.getCommand();
            for (int i = 0; i < cmd.length; i++){
                java.lang.System.out.println("i="+i+" value="+cmd[i]);
            }
        }
        
        if( out != null ){
            out.flush();
        }
        
        if( error != null ){
            error.flush();
        }
        
        if( info != null ){
            StringBuilder builder = new StringBuilder();
            builder.append( "finished (exit code: '" );
            builder.append( result.getExitValue() );
            builder.append( "'" );
            
            builder.append( ", took: " );
            long nano = nanoEnd - nanoStart;
            long milli = nano / 1000000;
            long seconds = milli / 1000;
            long mins = seconds / 60;
            long hours = mins / 60;
            
            mins %= 60;
            seconds %= 60;
            milli %= 1000;
            if( hours > 0 ){
                builder.append( hours );
                builder.append( ":" );
            }
            if( mins < 10 )
                builder.append( "0" );
            builder.append( mins );
            builder.append( ":" );
            if( seconds < 10 )
                builder.append( "0" );
            builder.append( seconds );
            builder.append( "." );
            if( milli < 100 )
                builder.append( "0" );
            if( milli < 10 )
                builder.append( "0" );
            builder.append( milli );
            builder.append( ")\n\n" );
            
            info.write( builder.toString().getBytes() );
            info.flush();
        }
        
        R commandResult = command.result( result );
        monitor.done();
        return commandResult;
        
    }
    
    protected abstract IExecutionResult run( ICommand<?> command, IProgressMonitor monitor,
            OutputStream info, OutputStream out, OutputStream error ) throws InterruptedException, IOException;
    
    protected IExecutionResult toResult( String output, String error, int exitValue ){
        return new ExecutionResult( output, error, exitValue );
    }
    
    /**
     * Waits until <code>process</code> ends or until <code>monitor</code>
     * was canceled.
     * @param process the process for which to wait
     * @param monitor the monitor to test for cancellation
     * @return the exit value of <code>process</code>
     */
    protected int waitFor( Process process, IProgressMonitor monitor ){
        ProcessMonitor processMonitor = new ProcessMonitor( Thread.currentThread(), process, monitor );
        processMonitor.setRunning( true );
        int exitValue = 0;
        
        while( true ){
            try{
                exitValue = process.waitFor();
                break;
            }
            catch( InterruptedException ex ){
                if( monitor.isCanceled() ){
                    return -1;
                }
            }
        }
        
        processMonitor.setRunning( false );
        return exitValue;
    }
    
    /**
     * This thread observes an {@link IProgressMonitor} and cancels a
     * {@link Process} if the monitor is canceled.
     * @author Benjamin Sigg
     */
    private static class ProcessMonitor extends Thread{
        private Process process;
        private IProgressMonitor monitor;
        private Thread thread;
        
        private boolean running;
        
        public ProcessMonitor( Thread thread, Process process, IProgressMonitor monitor ){
            this.thread = thread;
            this.process = process;
            this.monitor = monitor;
        }
        
        public void setRunning( boolean running ){
            this.running = running;
            
            if( running ){
                start();
            }
        }
        
        @Override
        public void run(){
            while( running ){
                boolean cancling = false;
                
                if( monitor.isCanceled() ){
                    cancling = true;
                    setRunning( false );
                    process.destroy();
                }
                
                try{
                    Thread.sleep( 250 );
                }
                catch( InterruptedException ex ){
                    // ignore
                }
                
                if( cancling ){
                    thread.interrupt();
                }
            }
        }
    }
    
    private static class ExecutionResult implements IExecutionResult{
        private String output;
        private String error;
        private int exitValue;
        
        public ExecutionResult( String output, String error, int exitValue ){
            this.output = output;
            this.error = error;
            this.exitValue = exitValue;
        }
        
        public String getOutput(){
            return output;
        }
        public String getError(){
            return error;
        }
        public int getExitValue(){
            return exitValue;
        }
        
        @Override
        public String toString(){
            return "exit value= " + exitValue +
                "\noutput= " + output +
                "\nerror= " + error;
        }
    }
}
