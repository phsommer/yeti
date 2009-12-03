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
package tinyos.yeti.nesc12.ep.rules.quickfix;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.nesc12.ep.NesC12AST;

public class QuickfixInformation{
    private NesC12AST ast;
    private IDocumentMap document;
    private IParseFile file;
    private ProjectTOS project;
    
    public QuickfixInformation( NesC12AST ast, IDocumentMap document, IParseFile file, ProjectTOS project ){
        this.ast = ast;
        this.document = document;
        this.file = file;
        this.project = project;
    }
    
    public void replace( final int offset, final int length, final String text ){
        Job job = new UIJob( "Replace" ){
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ) {
                monitor.beginTask( "Replace", 1 );
                try {
                    document.getDocument().replace( offset, length, text );
                }
                catch( BadLocationException e ) {
                    e.printStackTrace();
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setSystem( true );
        job.setPriority( Job.INTERACTIVE );
        job.schedule();
    }
    
    public NesC12AST getAst(){
        return ast;
    }
    
    public IDocumentMap getDocument(){
        return document;
    }
    
    public IParseFile getFile(){
        return file;
    }
    
    public ProjectTOS getProject(){
        return project;
    }
}
