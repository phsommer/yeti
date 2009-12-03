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

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;

/**
 * A proposal rule works on the {@link NesC12AST} to make {@link ICompletionProposal}s. 
 * @author Benjamin Sigg
 *
 */
public interface IProposalRule {
    /**
     * Collects new proposals and writes them into <code>collector</code>
     * @param ast the abstract syntax tree to work on
     * @param collector information about what the user is doing
     */
    public void propose( NesC12AST ast, CompletionProposalCollector collector );
}
