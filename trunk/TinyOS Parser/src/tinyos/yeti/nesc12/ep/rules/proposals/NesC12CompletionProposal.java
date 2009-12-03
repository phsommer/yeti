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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import tinyos.yeti.ep.parser.INesCCompletionProposal;

public class NesC12CompletionProposal implements INesCCompletionProposal{
    private CompletionProposal proposal;
    private boolean file;
    
    private int replacementOffset;
    private int replacementLength;
    private String replacementString;
    
    public NesC12CompletionProposal( String replacementString,
            int replacementOffset, int replacementLength, int cursorPosition ){
        proposal = new CompletionProposal( replacementString,
                replacementOffset, replacementLength, cursorPosition );
        
        this.replacementOffset = replacementOffset;
        this.replacementLength = replacementLength;
        this.replacementString = replacementString;
    }

    public NesC12CompletionProposal( String replacementString,
            int replacementOffset, int replacementLength, int cursorPosition,
            Image image, String displayString,
            IContextInformation contextInformation,
            String additionalProposalInfo ){
        
        proposal = new CompletionProposal( replacementString,
                replacementOffset, replacementLength, cursorPosition, image,
                displayString, contextInformation, additionalProposalInfo );
        
        this.replacementOffset = replacementOffset;
        this.replacementLength = replacementLength;
        this.replacementString = replacementString;
    }
    
    public boolean inFile(){
        return file;
    }
    
    public void setInFile( boolean file ){
        this.file = file;
    }

    public int getReplacementOffset(){
        return replacementOffset;
    }
    
    public int getReplacementLength(){
        return replacementLength;
    }
    
    public void apply( IDocument document ){
        proposal.apply( document );
    }

    public Point getSelection( IDocument document ){
        return proposal.getSelection( document );
    }

    public IContextInformation getContextInformation(){
        return proposal.getContextInformation();
    }

    public Image getImage(){
        return proposal.getImage();
    }

    public String getDisplayString(){
        return proposal.getDisplayString();
    }

    public String getAdditionalProposalInfo(){
        return proposal.getAdditionalProposalInfo();
    }
    
    @Override
    public int hashCode(){
    	int code = file ? 1 : 0;
    	code = code * 41 + replacementLength;
    	code = code * 41 + replacementOffset;
    	code = code * 41 + replacementString.hashCode();
    	return code;
    }
    
    @Override
    public boolean equals( Object obj ){
	    if( obj == null )
	    	return false;
	    if( !(obj instanceof NesC12CompletionProposal ))
	    	return false;
	    
	    NesC12CompletionProposal other = (NesC12CompletionProposal)obj;
	    if( other.file != file )
	    	return false;
	    if( other.replacementLength != replacementLength )
	    	return false;
	    if( other.replacementOffset != replacementOffset )
	    	return false;
	    if( !other.replacementString.equals( replacementString ))
	    	return false;
	    
	    return true;
    }
}
