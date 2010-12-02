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

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.nesc.CTemplateCompletionProcessor;
import tinyos.yeti.editors.quickfixer.NesCQuickAssistProcessor;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.ep.parser.INesCCompletionProposal;
import tinyos.yeti.ep.parser.ProposalLocation;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.utility.DocumentUtility;

/**
 * 
 * @author dcg
 *
 */
public class NesCCompletionProcessor implements IContentAssistProcessor {
    private NesCEditor editor;

    private INesCCompletionProposal[] NO_COMPLETIONS = new INesCCompletionProposal[0];
    private final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];

    private boolean reuse = false;
    private Set<INesCCompletionProposal> proposals = new HashSet<INesCCompletionProposal>();
    
    public NesCCompletionProcessor( NesCEditor editor ){
        this.editor = editor;
    }
    
    /**
     * Informs this {@link NesCQuickAssistProcessor} that the completion
     * proposals from the last call are to be reused. Otherwise they are
     * all deleted.
     */
    public void setReuseProposals(){
    	reuse = true;
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
    	if( !reuse ){
    		proposals.clear();
    	}
    	
    	INesCCompletionProposal[] newProposals = computeNewCompletionProposals( viewer, offset );
		if( newProposals == null )
			newProposals = NO_COMPLETIONS;
		
    	for( INesCCompletionProposal proposal : newProposals ){
			proposals.add( proposal );
		}
    	
    	INesCCompletionProposal[] result;
    	if( reuse ){
    		reuse = false;
    	}
    	
    	result = proposals.toArray( new INesCCompletionProposal[ proposals.size() ] );
    	
    	sort( result );
    	
    	return addTemplates( result, viewer, offset );
    }
    
    private INesCCompletionProposal[] computeNewCompletionProposals(ITextViewer viewer, int offset) {
        IDocument document = viewer.getDocument();
        String prefix = DocumentUtility.lastWord(document, offset);

        IProject project = editor.getProject();
        ProjectTOS tos = null;
        if( project != null ){
        	try{
        		tos = TinyOSPlugin.getDefault().getProjectTOS( project );
        	}
        	catch( MissingNatureException ex ){
        		// ignore
        	}
        }

        ProposalLocation location = new ProposalLocation( tos, viewer, new NesCDocumentMap( document ), offset, prefix );

        INesCAST ast = editor.getAST();

        if( ast == null ){
            return TinyOSPlugin.getDefault().getParserFactory().getProposals( location );
        }

        INesCCompletionProposal[] proposals = ast.getProposals( location );
        // model.getCompletionProposal(prefix, indent, offset, document,viewer);
        if( proposals == null )
            return NO_COMPLETIONS;


        return proposals;
    }
    
    private void sort( INesCCompletionProposal[] proposals ){
        Arrays.sort( proposals, new Comparator<INesCCompletionProposal>(){
            private Collator collator = Collator.getInstance();

            public int compare( INesCCompletionProposal a, INesCCompletionProposal b ){
                boolean fileA = a.inFile();
                boolean fileB = b.inFile();
                
                if( fileA && !fileB )
                    return -1;
                
                if( !fileA && fileB )
                    return 1;

                String displayA = a.getDisplayString();
                String displayB = b.getDisplayString();

                if( displayA == null && displayB == null )
                    return 0;

                if( displayA == null )
                    return 1;

                if( displayB == null )
                    return -1;

                return collator.compare( displayA, displayB );
            }
        });
    }
    
    private ICompletionProposal[] addTemplates( ICompletionProposal[] proposals, ITextViewer viewer, int offset ){
        ICompletionProposal[] result = proposals;
        
        CTemplateCompletionProcessor templates = new CTemplateCompletionProcessor();
        ICompletionProposal[] additional = templates.computeCompletionProposals( viewer, offset );
        
        if( additional != null && additional.length > 0 ){
            result = new ICompletionProposal[ proposals.length + additional.length ];
            System.arraycopy( proposals, 0, result, 0, proposals.length );
            System.arraycopy( additional, 0, result, proposals.length, additional.length );
            
        }
        
        return result;
    }

    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return NO_CONTEXTS;
    }

    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[]{'.'};
    }

    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    public String getErrorMessage() {
        return null;
    }

    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }
}
