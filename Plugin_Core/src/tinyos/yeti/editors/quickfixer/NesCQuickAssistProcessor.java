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
package tinyos.yeti.editors.quickfixer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

import tinyos.yeti.editors.NesCEditor;

/**
 * This {@link IQuickAssistProcessor} is designed to work only with a {@link NesCEditor}.
 * It uses the {@link NesCEditor#getCurrentAssistMarker()} and {@link NesCEditor#getCurrentMarkerResolutions()}
 * methods to generate new completion proposals.
 * @author Benjamin Sigg
 *
 */
public class NesCQuickAssistProcessor implements IQuickAssistProcessor{
    private NesCEditor editor;
    
    public NesCQuickAssistProcessor( NesCEditor editor ){
        this.editor = editor;
    }

    public boolean canAssist( IQuickAssistInvocationContext invocationContext ){
        return false;
    }

    public boolean canFix(Annotation annotation) {
        return annotation instanceof MarkerAnnotation;
    }

    public ICompletionProposal[] computeQuickAssistProposals( IQuickAssistInvocationContext invocationContext) {
        IMarker marker = editor.getCurrentAssistMarker();
        if( marker == null ){
            return new ICompletionProposal[]{};
        }
        
        IMarkerResolution[] resolutions = editor.getCurrentMarkerResolutions();
        ICompletionProposal[] additional = computeAdditionalProposals( marker );
        
        if( resolutions == null && additional == null ){
            return new ICompletionProposal[]{};
        }
        else if( resolutions == null ){
            return additional;
        }
        
        int size = resolutions.length;
        if( additional != null )
            size += additional.length;
        
        ICompletionProposal[] result = new ICompletionProposal[ size ];
        
        for( int i = 0, n = resolutions.length; i<n; i++ ){
            result[i] = new MarkerCompletion( marker, resolutions[i] );
        }
        
        if( additional != null )
            System.arraycopy( additional, 0, result, resolutions.length, additional.length );
        
        return result;
    }

    private ICompletionProposal[] computeAdditionalProposals( IMarker marker ){
        
        try{
            ContentAssistant assistant = editor.getEditorContentAssistant();
            
            int end = MarkerUtilities.getCharEnd( marker );
            
            IDocument document= editor.getDocument();
            String type = TextUtilities.getContentType( document, editor.getEditorDocumentPartitioning(), end, true );
            
            IContentAssistProcessor processor = assistant.getContentAssistProcessor( type );
            if( processor == null )
                return null;
            
            return processor.computeCompletionProposals( editor.getEditorSourceViewer(), end );
        }
        catch ( BadLocationException e ){
            return null;
        }
    }
    
    public String getErrorMessage() {
        return null;
    }
}
