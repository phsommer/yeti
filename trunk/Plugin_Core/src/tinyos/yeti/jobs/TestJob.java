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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.TinyOSConsole;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.ITest;

/**
 * A job that calls all the {@link ITest}s that are available.
 * @author Benjamin Sigg
 */
public class TestJob extends Job{
    public TestJob(){
        super( "Test Environments" );
    }

    @Override
    protected IStatus run( IProgressMonitor monitor ) {
        Collection<IEnvironment> environments = TinyOSPlugin.getDefault().getEnvironments().getEnvironments();
        Map<IEnvironment, ITest[]> tests = new HashMap<IEnvironment, ITest[]>();
        int count = 0;

        for( IEnvironment environment : environments ){
            ITest[] test = environment.getTests();
            if( test != null ){
                tests.put( environment, test );
                count += test.length;
            }
        }

        monitor.beginTask( "Testing", count );
        TinyOSConsole console = TinyOSPlugin.getDefault().getConsole();
        Job reveal = new UIJob( "Reveal console"){
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor){
                monitor.beginTask( "Reveal", IProgressMonitor.UNKNOWN );
                TinyOSPlugin.getDefault().revealConsole();
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        reveal.setPriority( Job.INTERACTIVE );
        reveal.setSystem( true );
        reveal.schedule();

        console.setProject( null );

        int okCount = 0;
        int warningCount = 0;
        int errorCount = 0;

        for( Map.Entry<IEnvironment, ITest[]> next : tests.entrySet() ){
            if( monitor.isCanceled() )
                break;

            console.info().println( "Environment: " + next.getKey().getEnvironmentName() );
            console.info().println( "" );

            for( ITest test : next.getValue() ){
                if( monitor.isCanceled() )
                    break;

                console.info().println( "Test: " + test.getName() );
                console.info().println( "Description: " + test.getDescription() );

                IStatus result;
                try{
                    result = test.run( console.out(), console.err(), new SubProgressMonitor( monitor, 1 ) );
                }
                catch( Throwable t ){
                    result = new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, t.getMessage(), t );
                }

                switch( result.getSeverity() ){
                    case IStatus.ERROR:
                        errorCount++;
                        console.info().println( "Status: error" );
                        break;
                    case IStatus.WARNING:
                        warningCount++;
                        console.info().println( "Status: warning" );
                        break;
                    default:
                        okCount++;
                    console.info().println( "Status: ok" );
                    break;
                }

                if( result.getMessage() != null )
                    console.info().println( "Message: " + result.getMessage() );

                if( result.getException() != null ){
                    console.info().println( "Exception: " );
                    result.getException().printStackTrace( console.info().print() );
                }

                console.info().println( "" );

                try {
                    console.out().flush();
                    console.err().flush();
                    console.info().flush();

                    // give the system time to process the streams
                    Thread.sleep( 100 );
                } 
                catch( IOException e ){
                    TinyOSPlugin.warning( e );
                }
                catch( InterruptedException ex ){
                    Thread.currentThread().interrupt();
                }
            }
        }

        console.info().println( "Done" );
        console.info().println( "Errors: " + errorCount );
        console.info().println( "Warnings: " + warningCount );
        console.info().println( "" );

        monitor.done();
        return Status.OK_STATUS;
    }
}
