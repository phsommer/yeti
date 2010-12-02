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
package tinyos.yeti.nesc12.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import tinyos.yeti.ep.parser.INesCCompletionProposal;
import tinyos.yeti.ep.parser.ProposalLocation;

/**
 * Used to collect {@link ICompletionProposal}s within an AST. 
 * @author Benjamin Sigg
 */
public class CompletionProposalCollector {
    private ProposalLocation location;
    private List<INesCCompletionProposal> proposals = new ArrayList<INesCCompletionProposal>();
    
    public CompletionProposalCollector( ProposalLocation location ){
        this.location = location;
    }

    /**
     * Gets the offset at the beginning of the selected word.
     * @return the begin of the selected prefix
     */
    public int getOffset(){
        return location.getOffset() - location.getPrefix().length();
    }
    
    /**
     * Gets the location where the user is currently editing a file. 
     * @return the location
     */
    public ProposalLocation getLocation(){
        return location;
    }

    /**
     * Stores an additional proposal.
     * @param proposal the new proposal, <code>null</code> will be ignored
     */
    public void add( INesCCompletionProposal proposal ){
        if( proposal != null ){
            proposals.add( proposal );
        }
    }

    public INesCCompletionProposal[] getProposals(){
        return proposals.toArray( new INesCCompletionProposal[ proposals.size() ]);
    }
}
