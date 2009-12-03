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

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.format.INesCFormattingStrategyFactory;
import tinyos.yeti.editors.nesc.NesCAutoIdentStrategy;
import tinyos.yeti.editors.nesc.NesCHover;
import tinyos.yeti.editors.nesc.doc.NesCDocAutoIdentStrategy;
import tinyos.yeti.editors.nesc.doc.NesCDocCompletionProcessor;
import tinyos.yeti.editors.quickfixer.NesCQuickAssistProcessor;
import tinyos.yeti.ep.INesCPresentationReconcilerFactory;
import tinyos.yeti.ep.NesCPresentationReconcilerDefaults;

public class NesCSourceViewerConfiguration extends
        TextSourceViewerConfiguration implements INesCPartitions{

    /** The double click strategy */
    // private NesCDoubleClickSelector fNesCDoubleClickSelector = null;
    /** The document partitioning. */
    private String fDocumentPartitioning = null;

    private ContentAssistant contentAssistant;
    
    private NesCCompletionProcessor nescCompletionProcessor;
    
    private QuickAssistAssistant quickAssistant;
    
    private NesCQuickAssistProcessor quickAssistantProcessor;
 
    private NesCEditor editor;

    private NesCHover textHover;
    
    public NesCSourceViewerConfiguration( String fDocumentPartitioning, NesCEditor editor ){
        this.editor = editor;
        this.fDocumentPartitioning = fDocumentPartitioning;
        fPreferenceStore = new PreferenceStore();
        AbstractDecoratedTextEditorPreferenceConstants.initializeDefaultValues( fPreferenceStore );        
    }

    public QuickAssistAssistant getQuickAssistAssistant(){
        return quickAssistant;
    }
    
    @Override
    public IQuickAssistAssistant getQuickAssistAssistant( ISourceViewer sourceViewer ){
        // TODO does not work
        // return super.getQuickAssistAssistant( sourceViewer );

        if( quickAssistant == null ){

            quickAssistant = new QuickAssistAssistant();
            quickAssistantProcessor = new NesCQuickAssistProcessor( editor );
            quickAssistant.setQuickAssistProcessor( quickAssistantProcessor );
            // assistant.setInformationControlCreator(getQuickAssistAssistantInformationControlCreator());

            quickAssistant.setProposalSelectorBackground( sourceViewer.getTextWidget().getBackground() );
            quickAssistant.setProposalSelectorForeground( sourceViewer.getTextWidget().getForeground() );
        }
        
        return quickAssistant;
    }
    
    public NesCQuickAssistProcessor getQuickAssistantProcessor(){
		return quickAssistantProcessor;
	}

    public NesCSourceViewerConfiguration( String fDocumentPartitioning ){
        this( fDocumentPartitioning, null );
    }
    
    @SuppressWarnings( "unchecked" )
    @Override
    protected Map getHyperlinkDetectorTargets( ISourceViewer sourceViewer ){
        Map map = super.getHyperlinkDetectorTargets( sourceViewer );
        map.put( "tinyos.ui.hyperlink.nesc", new IAdaptable(){
            public Object getAdapter( Class adapter ){
                if( NesCEditor.class.equals( adapter ) )
                    return editor;

                return null;
            }
        } );
        return map;
    }

    /*
     * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
     */
    @Override
    public String[] getConfiguredContentTypes( ISourceViewer sourceViewer ){
        return INesCPartitions.PARTITION_TYPES;
    }

    @Override
    public String getConfiguredDocumentPartitioning( ISourceViewer sourceViewer ){
        if( fDocumentPartitioning != null )
            return fDocumentPartitioning;
        return super.getConfiguredDocumentPartitioning( sourceViewer );
    }
    
    @Override
    public IContentAssistant getContentAssistant( ISourceViewer sourceViewer ){
        contentAssistant = new UpdatingContentAssistant( editor );
        contentAssistant.setDocumentPartitioning( getConfiguredDocumentPartitioning( sourceViewer ) );
        contentAssistant.enableAutoActivation( true );
        contentAssistant.setAutoActivationDelay( 200 );
        contentAssistant.setProposalPopupOrientation( IContentAssistant.PROPOSAL_OVERLAY );

        nescCompletionProcessor = new NesCCompletionProcessor( editor );
        IContentAssistProcessor pr_doc = new NesCDocCompletionProcessor();

        contentAssistant.setContentAssistProcessor( pr_doc, NESC_DOC );
        contentAssistant.setContentAssistProcessor( new NullCompletionProcessor(), PREPROCESSOR_DIRECTIVE );
        contentAssistant.setContentAssistProcessor( new NullCompletionProcessor(), NESC_SINGLE_LINE_COMMENT );
        contentAssistant.setContentAssistProcessor( new NullCompletionProcessor(), MULTI_LINE_COMMENT );
        contentAssistant.setContentAssistProcessor( nescCompletionProcessor, IDocument.DEFAULT_CONTENT_TYPE );
        // ca.setContentAssistProcessor(pr,NESC_PARTITIONING);
        contentAssistant.setInformationControlCreator( getInformationControlCreator( sourceViewer ) );

        return contentAssistant;
    }

    public ContentAssistant getContentAssistant(){
        return contentAssistant;
    }
    
    public NesCCompletionProcessor getNescCompletionProcessor(){
		return nescCompletionProcessor;
	}
    
    @Override
    public ITextHover getTextHover( ISourceViewer sourceViewer, String contentType ){
    	if( contentType.equals( INesCPartitions.MULTI_LINE_COMMENT ) || 
    		contentType.equals( INesCPartitions.NESC_SINGLE_LINE_COMMENT ) ||
    		contentType.equals( INesCPartitions.NESC_DOC ) ||
    		contentType.equals( INesCPartitions.NESC_STRING ) ||
    		contentType.equals( INesCPartitions.DEFAULT )){
    		
    		if( textHover == null )
    			textHover = new NesCHover( editor, this );
    		
    		return textHover;
    	}
    	
    	return super.getTextHover( sourceViewer, contentType );
    }
    
    @Override
    public boolean isShownInText( Annotation annotation ){
    	return super.isShownInText( annotation );
    }
    
    /*
     * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer,
     *      String)
     */
    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(
            ISourceViewer sourceViewer, String contentType ){
        if( INesCPartitions.NESC_DOC.equals( contentType )
                || INesCPartitions.MULTI_LINE_COMMENT.equals( contentType )
                || INesCPartitions.NESC_SINGLE_LINE_COMMENT
                        .equals( contentType ) )
            return new DefaultTextDoubleClickStrategy();
        // else if (IJavaPartitions.JAVA_STRING.equals(contentType) ||
        // IJavaPartitions.JAVA_CHARACTER.equals(contentType))
        // return new
        // JavaStringDoubleClickSelector(getConfiguredDocumentPartitioning(sourceViewer));
        return new NesCDoubleClickSelector();
    }

    @Override
    public IPresentationReconciler getPresentationReconciler( ISourceViewer sourceViewer ){
        
        INesCPresentationReconcilerFactory factory = TinyOSPlugin.getDefault().getPresentationReconcilerFactory();
        
        return factory.create( 
        		sourceViewer, 
        		new NesCPresentationReconcilerDefaults( 
        				editor,
        				getConfiguredDocumentPartitioning( sourceViewer )));
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies( ISourceViewer sourceViewer, String contentType ){
        // String partitioning= getConfiguredDocumentPartitioning(sourceViewer);
        if( INesCPartitions.NESC_DOC.equals( contentType )
                || INesCPartitions.MULTI_LINE_COMMENT.equals( contentType ) ){
            return new IAutoEditStrategy[]{ new NesCDocAutoIdentStrategy() };
        }
        // return new IAutoEditStrategy[] {new
        // NesCDocAutoIdentStrategy(partitioning)};
        return new IAutoEditStrategy[]{ new NesCAutoIdentStrategy( editor ) };
    }

    @Override
    public MonoReconciler getReconciler( ISourceViewer sourceViewer ){
        final NesCConcilingStrategy strategy = new NesCConcilingStrategy( editor );
        MonoReconciler reconciler = new MonoReconciler( strategy, false ){
            @Override
            public void install( ITextViewer textViewer ){
                strategy.setAlive( true );
                super.install( textViewer );
            }
            
            @Override
            public void uninstall(){
                strategy.setAlive( false );
                super.uninstall();
            }
        };
        reconciler.setIsIncrementalReconciler( true );
        reconciler.setProgressMonitor( new NullProgressMonitor() );
        reconciler.setDelay( 200 );

        return reconciler;
    }

    @Override
    public String[] getDefaultPrefixes( ISourceViewer sourceViewer,
            String contentType ){
        if( contentType.equals( IDocument.DEFAULT_CONTENT_TYPE ) ){
            return new String[]{ "//" };
        }else if( contentType.equals( INesCPartitions.NESC_DOC ) ){
            return new String[]{ "//" };
        }else if( contentType.equals( INesCPartitions.MULTI_LINE_COMMENT ) ){
            return new String[]{ "//" };
        }else if( contentType.equals( INesCPartitions.NESC_SINGLE_LINE_COMMENT ) ){
            return new String[]{ "//" };
        }else
            return null;
    }

    @Override
    public IContentFormatter getContentFormatter( ISourceViewer viewer ){
    	TinyOSPlugin plugin = TinyOSPlugin.getDefault();
    	if( plugin != null ){
    		INesCFormattingStrategyFactory[] factories = plugin.getFormattingFactories();
    		for( INesCFormattingStrategyFactory factory : factories ){
    			if( factory.isFormatter() ){
    				return factory.createFormatter( viewer, editor );
    			}
    		}
    	}
    	return null;
    }

    public IContentFormatter getIndentFormatter( ISourceViewer viewer ){
    	TinyOSPlugin plugin = TinyOSPlugin.getDefault();
    	if( plugin != null ){
    		INesCFormattingStrategyFactory[] factories = plugin.getFormattingFactories();
    		for( INesCFormattingStrategyFactory factory : factories ){
    			if( factory.isIndenter() ){
    				return factory.createIndenter( viewer, editor );
    			}
    		}
    	}
    	return null;
    }
    
    public String getDefaultLineDelimiter(){
        return TextUtilities.getDefaultLineDelimiter( editor.getDocument() );
    }
}
