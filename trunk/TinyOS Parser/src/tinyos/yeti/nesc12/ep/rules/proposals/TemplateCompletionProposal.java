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
package tinyos.yeti.nesc12.ep.rules.proposals;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import tinyos.yeti.ep.parser.INesCCompletionProposal;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.utility.GenericTemplate;

/**
 * A completion proposal which takes a template as replacement string.
 * @see GenericTemplate
 * @author Benjamin Sigg
 */
public class TemplateCompletionProposal implements INesCCompletionProposal, ICompletionProposal, ICompletionProposalExtension2{
    private int replacementOffset;
    private String prefix;
    private GenericTemplate template;
    
    private String displayString;
    private Image image;
    private String additionalProposalInfo;
    private IContextInformation contextInformation;
    
    private boolean inFile;

    public TemplateCompletionProposal( String prefix, String template, int offset ){
        this.prefix = prefix;
        this.replacementOffset = offset;
        this.template = new GenericTemplate( template );
    }
    
    public TemplateCompletionProposal(
            String prefix,
            String template,
            int offset,
            Image image,
            String displayString,
            IContextInformation contextInformation,
            String additionalProposalInfo ){

        this.replacementOffset = offset;
        this.prefix = prefix.toLowerCase();
        this.template = new GenericTemplate( template );
        this.image = image;
        this.displayString = displayString;
        this.contextInformation = contextInformation;
        this.additionalProposalInfo = additionalProposalInfo;
    }

    public void apply( IDocument document ){
        // never happens
    }
    
    public void setAdditionalProposalInfo( String additionalProposalInfo ){
        this.additionalProposalInfo = additionalProposalInfo;
    }
    
    public String getAdditionalProposalInfo(){
        return additionalProposalInfo;
    }
    
    public void setContextInformation( IContextInformation contextInformation ){
        this.contextInformation = contextInformation;
    }
    
    public IContextInformation getContextInformation(){
        return contextInformation;
    }
    
    public void setDisplayString( String displayString ){
        this.displayString = displayString;
    }
    
    public String getDisplayString(){
        return displayString;
    }
    
    public void setImage( Image image ){
        this.image = image;
    }
    
    public Image getImage(){
        return image;
    }
    
    public void setInFile( boolean inFile ){
        this.inFile = inFile;
    }
    
    public boolean inFile(){
        return inFile;
    }
    
    public void selected( ITextViewer viewer, boolean smartToggle ){
        // ignore
    }

    public void unselected( ITextViewer viewer ){
        // ignore
    }
    
    public int getReplacementOffset(){
        return replacementOffset;
    }

    public boolean validate( IDocument document, int offset, DocumentEvent event ){
        try{
            String prefix = RuleUtility.prefix( offset-1, document );
            if( prefix == null )
                return true;

            return this.prefix.startsWith( prefix.toLowerCase() );
        }
        catch( BadLocationException ex ){
            return false;
        }
    }

    public void apply( ITextViewer viewer, char trigger, int stateMask, int triggerOffset ){
    	template.apply( viewer, getReplacementOffset(), triggerOffset );
    }
    
    public Point getSelection(IDocument document) {
    	return template.getSelection( document, getReplacementOffset() );
    }
}
