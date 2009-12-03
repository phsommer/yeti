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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.Debug;
import tinyos.yeti.ep.parser.INesCParser;

/**
 * A content assistant which updates its content when the {@link NesCEditor}
 * reconciles.
 * @author Benjamin Sigg
 */
public class UpdatingContentAssistant extends ContentAssistant {
    private NesCEditor editor;
    private boolean showing = false;
    
    private INesCEditorParserClient parserClient = new INesCEditorParserClient(){
        public void setupParser( NesCEditor editor, INesCParser parser ){
            // ignore
        }
        
        public void closeParser( NesCEditor editor, boolean successful, INesCParser parser ){
            Job update = new UIJob( "Update Content Assistant" ){
                @Override
                public IStatus runInUIThread( IProgressMonitor monitor ){
                    monitor.beginTask( "Update", IProgressMonitor.UNKNOWN );
                    Debug.info( showing );
                    
                    if( showing ){
                    	NesCCompletionProcessor processor = UpdatingContentAssistant.this.editor.getConfiguration().getNescCompletionProcessor();
                    	
                    	processor.setReuseProposals();
                        showPossibleCompletions();
                    }
                    
                    monitor.done();
                    return Status.OK_STATUS;
                }
            };
            
            update.setSystem( true );
            update.setPriority( Job.INTERACTIVE );
            update.schedule();
        }    
    };
    
    public ICompletionListener completion = new ICompletionListener(){
        public void assistSessionEnded( ContentAssistEvent event ){
            showing = false;
        }

        public void assistSessionStarted( ContentAssistEvent event ){
            showing = true;
        }

        public void selectionChanged( ICompletionProposal proposal, boolean smartToggle ){
            // ignore
        }
    };
    
    public UpdatingContentAssistant( NesCEditor editor ){
        this.editor = editor;
        setRepeatedInvocationMode( true );
        editor.addParserClient( parserClient );
        
        addCompletionListener( completion );
    }
    
    public NesCEditor getEditor(){
        return editor;
    }   
}
