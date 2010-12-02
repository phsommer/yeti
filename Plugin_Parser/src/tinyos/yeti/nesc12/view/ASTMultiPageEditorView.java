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
package tinyos.yeti.nesc12.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.editors.INesCEditorParserClient;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.outline.NesCOutlinePage;
import tinyos.yeti.ep.INesCMultiPageEditorPart;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class ASTMultiPageEditorView implements INesCMultiPageEditorPart, INesCEditorParserClient{
    private NesCEditor editor;
    
    private Composite content;
    private StackLayout contentLayout;
    private Text astText;
    private Composite waiting;
    
    public Control createControl( Composite parent, NesCEditor editor ) {
        this.editor = editor;
        editor.addParserClient( this );
        
        content = new Composite( parent, SWT.NONE );
        contentLayout = new StackLayout();
        content.setLayout( contentLayout );
        
        astText = new Text( content, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY );

        waiting = new Composite( content, SWT.NONE );
        waiting.setLayout( new GridLayout() );
        
        Label running = new Label( waiting, SWT.NONE );
        running.setText( "No content available - please wait while content is generated" );
        running.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
        
        contentLayout.topControl = waiting;
        content.layout();
        
        return content;
    }

    public String getPartName() {
        return "AST";
    }

    public void setOutlinePage( NesCOutlinePage outline ) {
        // ignore
    }

    public void setSelected( boolean selected ) {
        contentLayout.topControl = waiting;
        content.layout();
        
        if( selected ){
            editor.reconcileAsync();
        }
        else{
            setNode( null );
        }
    }

    public void setupParser( NesCEditor editor, INesCParser parser ) {
        // ignore
    }
    
    public void closeParser( NesCEditor editor, boolean successful, INesCParser parser ) {
        ASTNode root = ((Parser)parser).getRootASTNode();
        setNode( root );
    }
    
    private void setNode( final ASTNode root ){
        UIJob job = new UIJob( "Populate" ){
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ) {
                if( astText != null && !astText.isDisposed() ){
                    astText.setText( root == null ? "parsing error" : root.toString() );
                    
                    contentLayout.topControl = astText;
                    content.layout();
                }
                
                return Status.OK_STATUS;
            }
        };
        job.setSystem( true );
        job.setPriority( Job.INTERACTIVE );
        job.schedule();
    }

    public void dispose(){
    	// ignore
    }
}
