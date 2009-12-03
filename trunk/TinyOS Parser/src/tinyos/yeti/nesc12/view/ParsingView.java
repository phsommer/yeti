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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc.IMultiReader;
import tinyos.yeti.nesc.StringMultiReader;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

/**
 * A view that uses the parser to show the contents of the currently
 * selected {@link NesCEditor}.
 * @author Benjamin Sigg
 */
public abstract class ParsingView extends ViewPart implements IPartListener{
    private MultiPageNesCEditor editor;
    private IWorkbenchPage page;
    
    /** whether the parser should resolve the ast or not */
    private boolean resolve;
    
    public ParsingView( boolean resolve ){
        this.resolve = resolve;
    }
    
    /**
     * Sets the current ast.
     * @param root the root of the ast or <code>null</code>
     */
    protected abstract void setAST( ASTNode root );
    
    protected abstract Display getDisplay();
    
    @Override
    protected void setSite( IWorkbenchPartSite site ) {
        super.setSite( site );
        if( page != null )
            page.removePartListener( this );
        
        if( site == null )
            page = null;
        else
            page = site.getPage();
        
        if( page != null )
            page.addPartListener( this );
    }
    
    @Override
    public void dispose() {
        super.dispose();
        
        if( page != null ){
            page.removePartListener( this );
            page = null;
        }
        
        setEditor( null );
    }
    
    private void setEditor( MultiPageNesCEditor editor ){
        if( editor != this.editor ){
            if( editor == null ){
                setModel( null, null );
            }
            else{
                NesCEditor nescEditor = editor.getNesCEditor();
                IDocument document = nescEditor.getDocument();
                IEditorInput input = nescEditor.getEditorInput();
                IResource source = null;
                if( input instanceof IFileEditorInput ){
                     source = ((IFileEditorInput)input).getFile();
                }
                setModel( source, document );
            }
            this.editor = editor;
        }
    }
    
    private void setModel( final IResource source, final IDocument document ){
        ProjectTOS tos = null;
        
        if( source != null ){
        	try{
        		tos = TinyOSPlugin.getDefault().getProjectTOS( source.getProject() );
        	}
        	catch( MissingNatureException ex ){
        		// ignore
        	}
        }
        if( tos == null ){
            tos = TinyOSPlugin.getDefault().getProjectTOS();
        }
        
        final ProjectTOS projectTOS = tos;
        
        Job job = new Job( "parse" ){
            @Override
            protected IStatus run( IProgressMonitor monitor ){
                monitor.beginTask( "parse", 1000 );
                int ticksRemaining = 1000;
                
                ASTNode root = null;
                if( document != null ){
                    try{
                        IMultiReader reader = null;

                        if( document != null ){
                            reader = new StringMultiReader( document.get() );
                        }

                        if( reader == null && source != null ){
                            IPath p = source.getLocation();
                            if (p != null) {
                                File f= p.toFile();
                                reader = new FileMultiReader( f );
                            }
                        }

                        if( reader != null ){
                            Parser parser = null;
                            
                            IParseFile parseFile = NullParseFile.NULL;
                            
                            if( source != null && projectTOS != null )
                                parseFile = projectTOS.getModel().parseFile( source );
                            
                            if( projectTOS != null ){
                                parser = (Parser)projectTOS.getModel().newParser( parseFile, reader, new SubProgressMonitor( monitor, 500 ) );
                                ticksRemaining -= 500;
                            }
                            if( parser == null ){
                                parser = new Parser( source == null ? null : source.getProject() );
                                if( projectTOS != null ){
                                    projectTOS.getModel().getBasicDeclarations().addBasics( parser, parseFile, null );
                                }
                                parser.setParseFile( parseFile );
                            }
                            
                            parser.setCreateMessages( false );
                            parser.setFollowIncludes( true );
                            parser.setResolve( resolve );
                            parser.parse( reader, new SubProgressMonitor( monitor, ticksRemaining ) );
                            root = parser.getRootASTNode();
                        }
                    }
                    catch( IOException ex ){

                    }
                }

                final ASTNode send = root;
                Job job = new UIJob( "Set AST" ){
                    @Override
                    public IStatus runInUIThread( IProgressMonitor monitor ){
                        setAST( send );
                        return Status.OK_STATUS;
                    }
                };
                job.setPriority( getPriority() );
                job.setSystem( true );
                job.schedule();
                
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        
        if( projectTOS != null )
            job.setRule( projectTOS.getProject() );
        
        job.setPriority( Job.DECORATE );
        job.schedule();
    }

    public void partActivated( IWorkbenchPart part ) {
        if (part instanceof MultiPageNesCEditor) {
            setEditor( (MultiPageNesCEditor)part );
        }
    }

    public void partBroughtToTop( IWorkbenchPart part ) {
        if (part instanceof MultiPageNesCEditor) {
            setEditor( (MultiPageNesCEditor)part );
        }            
    }

    public void partClosed( IWorkbenchPart part ) {
        if( part == editor ) {
            setEditor( null );
        }
    }

    public void partDeactivated( IWorkbenchPart part ) {
        /*if (part instanceof MultiPageNesCEditor) {
            setEditor( null );
        }*/
    }

    public void partOpened( IWorkbenchPart part ) {
        if (part instanceof MultiPageNesCEditor) {
            setEditor( (MultiPageNesCEditor)part );
        }
    }

}
