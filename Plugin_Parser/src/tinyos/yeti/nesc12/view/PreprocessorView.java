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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.ViewPart;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ProjectIncludeProvider;
import tinyos.yeti.nesc12.parser.preprocessor.macro.PredefinedMacro;
import tinyos.yeti.preprocessor.Preprocessor;

public class PreprocessorView  extends ViewPart implements IPartListener{
    private MultiPageNesCEditor editor;
    private IWorkbenchPage page;

    private Text text;
    
    public PreprocessorView(){
    }

    @Override
    public void createPartControl( Composite parent ) {
        text = new Text( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY );
    }
    
    protected void setText( String text ){
        if( this.text != null && !this.text.isDisposed() )
            this.text.setText( text );
    }
    
    protected Display getDisplay(){
        if( text == null || text.isDisposed() )
            return null;
        
        return text.getDisplay();
    }
    
    @Override
    public void setFocus() {
        if( text != null )
            text.setFocus();
    }
    
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
        Job job = new Job( "parse" ){
            @Override
            protected IStatus run( IProgressMonitor monitor ){
                monitor.beginTask( "parse", 1000 );
                final StringWriter writer = new StringWriter();
                
                if( document != null ){
                    try{
                        Reader reader = null;

                        if( document != null ){
                            reader = new StringReader( document.get() );
                        }

                        if( reader == null && source != null ){
                            IPath p = source.getLocation();
                            if (p != null) {
                                File f= p.toFile();
                                reader = new FileReader(f);
                            }
                        }

                        if( reader != null ){
                            Preprocessor preprocessor = new Preprocessor();
                            ProjectTOS tos = editor.getNesCEditor().getProjectTOS();
                            if( tos != null ){
                                for( IMacro macro : tos.getModel().getBasicDeclarations().listBasicMacros() ){
                                    preprocessor.addMacro( PredefinedMacro.instance( macro ) );
                                }
                            }
                            IParseFile parseFile = NullParseFile.NULL;
                            
                            if( tos != null ){
                                parseFile = tos.getModel().parseFile( source );
                                preprocessor.setIncludeProvider( new ProjectIncludeProvider( tos ) );
                            }
                            else{
                                tos = TinyOSPlugin.getDefault().getProjectTOS();
                                if( tos != null ){
                                    preprocessor.setIncludeProvider( new ProjectIncludeProvider( tos ) );
                                }
                            }
                            
                            preprocessor.process( new NesC12FileInfo( parseFile ), reader, writer, new SubProgressMonitor( monitor, 1000 ) );
                            reader.close();
                        }
                    }
                    catch( IOException ex ){
                        ex.printStackTrace();
                    }
                }

                if( !monitor.isCanceled() ){
                    Display display = getDisplay();
                    if(  display != null ){
                        display.asyncExec( new Runnable(){
                            public void run(){
                                setText( writer.toString() );     
                            }
                        });
                    }
                }
                
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        
        if( source != null )
            job.setRule( source.getProject() );
        else
            job.setRule( TinyOSPlugin.getDefault().getProjectTOS().getProject() );
        
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
