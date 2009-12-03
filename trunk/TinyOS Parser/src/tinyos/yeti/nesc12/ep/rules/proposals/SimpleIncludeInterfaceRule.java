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

import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;

/**
 * Rules that identifies the state "uses/provides interface x".
 * @author Benjamin Sigg
 */
public class SimpleIncludeInterfaceRule implements IProposalRule{
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            int offset = collector.getOffset();
            IDocumentMap document = collector.getLocation().getDocument();
            
            boolean hasInterface = RuleUtility.hasBefore( offset-1, document, "interface" );
            if( hasInterface ){
                offset = RuleUtility.lastIndexOf( 0, offset, document, "interface" );
            }
            
            boolean trigger = RuleUtility.hasBefore( offset-1, document, "uses" ) ||
                RuleUtility.hasBefore( offset-1, document, "provides" );

            if( trigger ){
                listInterfaces( ast, collector, !hasInterface );
            }
        }
        catch( BadLocationException ex ){
            // no proposals at this location
        }
    }

    protected void listInterfaces( NesC12AST ast, CompletionProposalCollector collector, boolean insertInterface ){
        DeclarationResolver resolver = ast.getResolver();
        if( resolver == null )
            return;

        String prefix = null;
        if( insertInterface )
            prefix = "interface ";

        IDeclaration[] all = resolver.resolveAll( null, Kind.INTERFACE );
        if( all != null ){
            for( IDeclaration declaration : all ){
                collector.add( ProposalUtility.createProposal( declaration, collector.getLocation(), prefix, null, ast ) );
            }
        }
    }
}
