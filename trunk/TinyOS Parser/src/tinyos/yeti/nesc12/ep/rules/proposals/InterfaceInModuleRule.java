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
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.NesCModule;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;

public class InterfaceInModuleRule implements IProposalRule{
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            IDocumentMap document = collector.getLocation().getDocument();
            int offset = collector.getOffset();
            
            if( RuleUtility.hasBefore( offset-1, document, "." ) )
                return;

            int blockBegin = RuleUtility.blockBegin( offset, document );
            if( blockBegin < 0 )
                return;
            
            String wordImplementation = RuleUtility.reverseWord( blockBegin-1, document );
            if( !"implementation".equals( wordImplementation ))
                return;
            
            INesC12Location location = ast.getOffsetInput( offset );
            
            ASTNode node = RuleUtility.nodeAt( location, ast.getRoot() );
            
            while( node != null ){
                if( node instanceof Module ){
                    break;
                }
                node = node.getParent();
            }
            
            if( node == null )
                return;
            
            String wordInterfaceName = RuleUtility.reverseWord( offset, document );
            if( wordInterfaceName == null )
                wordInterfaceName = "";
            
            Module module = (Module)node;
            NesCModule nesCModule = module.resolveNode().resolve( ast.getBindingResolver() );
            if( nesCModule == null )
                return;
            
            for( int i = 0, n = nesCModule.getProvidesCount(); i<n; i++ ){
                NesCInterfaceReference reference = nesCModule.getProvides( i );
                if( reference != null && reference.getName().toIdentifier().startsWith( wordInterfaceName )){
                	collector.add( ProposalUtility.createProposal( reference.toDeclaration(), collector.getLocation(), ast ) );
                }
            }
            
            for( int i = 0, n = nesCModule.getUsesCount(); i<n; i++ ){
                NesCInterfaceReference reference = nesCModule.getUses( i );
                if( reference != null && reference.getName().toIdentifier().startsWith( wordInterfaceName )){
                	collector.add( ProposalUtility.createProposal( reference.toDeclaration(), collector.getLocation(), ast ) );
                }
            }
        }
        catch( BadLocationException ex ){
            // ignore
        }
    }
}
