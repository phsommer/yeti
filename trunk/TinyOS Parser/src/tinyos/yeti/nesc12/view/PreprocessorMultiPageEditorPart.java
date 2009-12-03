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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.INesCPartitions;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.NesCDocumentPartitioner;
import tinyos.yeti.editors.outline.NesCOutlinePage;
import tinyos.yeti.ep.INesCMultiPageEditorPart;
import tinyos.yeti.ep.INesCPresentationReconcilerFactory;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.NesCPresentationReconcilerDefaults;
import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.model.BasicDeclarationSet;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ProjectIncludeProvider;
import tinyos.yeti.nesc12.parser.preprocessor.macro.PredefinedMacro;
import tinyos.yeti.preprocessor.Preprocessor;

public class PreprocessorMultiPageEditorPart implements INesCMultiPageEditorPart {
    private Composite content;
    private StackLayout contentLayout;
    
    // private Text sourceViewer;
    
    private Composite waiting;
    private NesCEditor editor;
    
    private Composite viewerControl;
    private ISourceViewer viewer;
    private FastPartitioner partitionScanner;
    
    public Control createControl( Composite parent, NesCEditor editor ) {
        this.editor = editor;
        
        content = new Composite( parent, SWT.NONE );
        contentLayout = new StackLayout();
        content.setLayout( contentLayout );
        
        // sourceViewer = new Text( content, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY );
                
        viewerControl = new Composite( content, SWT.NONE );
        viewerControl.setLayout( new FillLayout( ));
        viewer = new SourceViewer(viewerControl, null, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.READ_ONLY );
        viewer.configure( new Configuration() );
        
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
        return "Preprocessor";
    }

    public void setOutlinePage( NesCOutlinePage outline ) {
        // ignore
    }

    public void setSelected( boolean selected ) {
        contentLayout.topControl = waiting;
        content.layout();
        
        if( !selected ){
            if( !viewerControl.isDisposed() ){
            	setSource( "" );
            }
        }
        else{
            IDocument document = editor.getDocument();
            IEditorInput input = editor.getEditorInput();
            IResource source = null;
            if( input instanceof IFileEditorInput ){
                source = ((IFileEditorInput)input).getFile();
            }

            preprocess( document, source );
        }
    }
    
    private void setSource( String source ){
    	Document document = new Document( source );
    	
    	if( partitionScanner == null ){
    		partitionScanner = new NesCDocumentPartitioner(  );
    	}
    	else{
    		partitionScanner.disconnect();
    	}
    	
    	document.setDocumentPartitioner( INesCPartitions.NESC_PARTITIONING, partitionScanner );
    	partitionScanner.connect( document );
    			
    	viewer.setDocument( document );
    	viewer.invalidateTextPresentation();
    	viewer.getTextWidget().redraw();
    }
    
    public void dispose(){
    	if( partitionScanner != null ){
    		partitionScanner.disconnect();
    	}
    }
    
    private void preprocess( final IDocument document, final IResource source ){
        if( document == null )
            return;

        Job job = new CancelingJob( "Populate preprocess view" ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                monitor.beginTask( "Preprocess", 1000 );
                final StringWriter writer = new StringWriter();

                try{
                    Reader reader = null;

                    if( document != null ){
                        reader = new StringReader( document.get() );
                    }

                    if( reader == null && source != null ){
                        IPath p = source.getLocation();
                        if (p != null) {
                            File f = p.toFile();
                            reader = new FileReader(f);
                        }
                    }

                    if( reader != null ){
                        Preprocessor preprocessor = new Preprocessor();
                        ProjectTOS tos = editor.getProjectTOS();
                        if( tos == null )
                            tos = TinyOSPlugin.getDefault().getProjectTOS();
                            
                        if( tos != null ){
                        	BasicDeclarationSet set = tos.getModel().getBasicDeclarations();
                        	
                        	for( IMacro macro : set.listBasicMacros() ){
                                preprocessor.addMacro( PredefinedMacro.instance( macro ) );
                            }
                        	
                        	if( !set.isGlobalInclusionFile( editor.getParseFile(), new SubProgressMonitor( monitor, 100 ) )){
                        		for( IMacro macro : tos.getModel().getBasicDeclarations().listGlobalMacros() ){
                        			preprocessor.addMacro( PredefinedMacro.instance( macro ) );
                        		}
                        	}
                        }
                        
                        IParseFile parseFile = NullParseFile.NULL;

                        if( tos != null ){
                            if( source != null ){
                                parseFile = tos.getModel().parseFile( source );
                            }
                            preprocessor.setIncludeProvider( new ProjectIncludeProvider( tos ) );
                        }

                        preprocessor.process( new NesC12FileInfo( parseFile ), reader, writer, new SubProgressMonitor( monitor, 900 ) );
                        reader.close();
                    }
                }
                catch( IOException ex ){
                    ex.printStackTrace();
                }

                if( !monitor.isCanceled() ){
                    UIJob putJob = new UIJob( "Transmit data to UI" ){
                        @Override
                        public IStatus runInUIThread( IProgressMonitor monitor ) {
                            if( !viewerControl.isDisposed() ){
                            	setSource( trim( writer.getBuffer() ) );

                                contentLayout.topControl = viewerControl;
                                content.layout();
                            }
                            return Status.OK_STATUS;
                        }
                    };
                    putJob.setSystem( true );
                    putJob.setPriority( Job.INTERACTIVE );
                    putJob.schedule();
                }

                monitor.done();
                if( monitor.isCanceled() )
                    return Status.CANCEL_STATUS;
                
                return Status.OK_STATUS;
            }
        };

        if( source != null ){
            job.setRule( source.getProject() );
        }
        else{
        	ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS();
        	if( tos != null ){
        		job.setRule( tos.getProject() );
        	}
        }

        job.setPriority( Job.INTERACTIVE );
        job.schedule();
    }
    
    private String trim( StringBuffer buffer ){
        StringWriter result = new StringWriter();
        PrintWriter writer = new PrintWriter( result );
        Scanner scanner = new Scanner( buffer.toString() );
        
        int empty = 1;
        
        while( scanner.hasNextLine() ){
            String line = scanner.nextLine();
            if( line.trim().length() == 0 ){
                if( empty++ < 1 ){
                    writer.println();
                }
            }
            else{
                writer.append( line );
                writer.println();
                empty = 0;
            }
        }
        
        return result.toString();
    }
    
    private class Configuration extends TextSourceViewerConfiguration{
    	@Override
    	public String getConfiguredDocumentPartitioning( ISourceViewer sourceViewer) {
    		return INesCPartitions.NESC_PARTITIONING;
    	}
    	
    	@Override
    	public IPresentationReconciler getPresentationReconciler( ISourceViewer sourceViewer) {
            INesCPresentationReconcilerFactory factory = TinyOSPlugin.getDefault().getPresentationReconcilerFactory();
            
            return factory.create( 
            		sourceViewer, 
            		new NesCPresentationReconcilerDefaults( 
            				null,
            				getConfiguredDocumentPartitioning( sourceViewer )));
    	}
    }
}
