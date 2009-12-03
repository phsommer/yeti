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
package tinyos.yeti.editors;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.jobs.ProgressMonitorCheckingClose;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.IMultiReader;
import tinyos.yeti.nesc.StringMultiReader;
import tinyos.yeti.preferences.PreferenceConstants;

public class NesCConcilingStrategy implements IReconcilingStrategy{
    private NesCEditor editor;
    private IDocument document;
    private Job reconcileJob;

    private boolean alive = false;

    public NesCConcilingStrategy( NesCEditor editor ){
        this.editor = editor;
    }

    /**
     * Sets whether this strategy will reconcile input.
     * @param alive <code>true</code> if this strategy is active, <code>false</code>
     * if this strategy should cancel any requests
     */
    public void setAlive( boolean alive ){
        this.alive = alive;
    }

    public void setDocument( IDocument document ) {
        this.document = document;
    }

    public void reconcile( IRegion partition ) {
        if( alive )
            reconcile( false );
    }

    public void reconcile( DirtyRegion dirtyRegion, IRegion subRegion ) {
        if( alive )
            reconcile( false );
    }

    private boolean initialized(){
        ProjectTOS project = editor.getProjectTOS();
        if( project == null )
            return true;

        if( project.getModel().isInitialized() ){
        	return true;
        }
        else{
        	if( project.isInitializeable() ){
        		project.initialize();
        	}
        	return false;
        }
    }
    
    public void reconcile( boolean direct ){
        if( !initialized() )
            return;
        
        final IParseFile parseFile = editor.getParseFile();
        if( parseFile == null )
            return;

        final IProject project = editor.getProject();

        if( project == null ){
            reconcileExtern( null );
        }
        else{
            synchronized( this ){
                if( reconcileJob != null ){
                    reconcileJob.cancel();
                    reconcileJob = null;
                }
            }
            reconcileJob = new CancelingJob( "Reconcile '" + parseFile.getName() + "'" ){
                @Override
                public IStatus run( IProgressMonitor monitor ){
                    return reconcile( monitor, this );
                }
            };
            reconcileJob.setRule( project );
            reconcileJob.setPriority( Job.DECORATE );
            if( !direct ){
            	int delay = TinyOSPlugin.getDefault().getPreferenceStore().getInt( PreferenceConstants.OUTLINE_UPDATE_DELAY );
                reconcileJob.schedule( delay );
            }
            else
                reconcileJob.schedule();
        }
    }

    public IStatus reconcile( IProgressMonitor monitor ){
        return reconcile( monitor, null );
    }

    private IStatus reconcile( IProgressMonitor monitor, Job job ){
        monitor = new ProgressMonitorCheckingClose( monitor );
        
        IParseFile parseFile = editor.getParseFile();
        if( parseFile == null ){
            monitor.done();
            return Status.OK_STATUS;
        }

        IProject project = editor.getProject();
        if( project == null ){
            reconcileExtern( monitor );
            if( monitor.isCanceled() )
                return Status.CANCEL_STATUS;
            return Status.OK_STATUS;
        }

        monitor.beginTask( "Reconcile", 10000 );

        try{
            ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
            IMultiReader reader = new StringMultiReader( document.get() );
            monitor.subTask( "Setup parser" );

            IResource resource = editor.getResource();
            
            INesCParser parser = tos.newParser( parseFile, reader, new SubProgressMonitor( monitor, 5000 ) );
            if( monitor.isCanceled() ){
                return Status.CANCEL_STATUS;
            }

            parser.setCreateMessages( resource != null && resource.exists() );
            monitor.subTask( "Parse" );
            reconcile( parser, reader, new SubProgressMonitor( monitor, 5000 ) );
            if( monitor.isCanceled() ){
                return Status.CANCEL_STATUS;
            }

            if( resource != null && resource.exists() ){
                NesCProblemMarker.synchronizeMessages( resource, parseFile, parser.getMessages() );
            }
            else{
                editor.showMessages( parser.getMessages() );
            }
        }
        catch( MissingNatureException ex ){
        	// silent
        }
        catch( IOException ex ){
            TinyOSPlugin.warning( ex );
        }
        finally{
            monitor.done();
            synchronized( NesCConcilingStrategy.this ){
                if( reconcileJob == job ){
                    reconcileJob = null;
                }
            }
        }

        monitor.done();
        return Status.OK_STATUS;
    }

    private void reconcileExtern( IProgressMonitor monitor ){
        INesCParserFactory factory = TinyOSPlugin.getDefault().getParserFactory();
        INesCParser parser = factory.createParser( null );
        reconcile( parser, null, monitor );
    }

    private void reconcile( INesCParser parser, IMultiReader reader, IProgressMonitor monitor ){
        parser.setCreateMessages( true );
        parser.setFollowIncludes( true );

        try{
            editor.setupParser( parser );

            if( reader == null )
                reader = new StringMultiReader( document.get() );

            boolean result = parser.parse( reader, monitor );
            if( monitor.isCanceled() )
                return;

            editor.closeParser( result, parser );
        }
        catch( IOException ex ){
            TinyOSPlugin.warning( ex );
        }
    }
}
