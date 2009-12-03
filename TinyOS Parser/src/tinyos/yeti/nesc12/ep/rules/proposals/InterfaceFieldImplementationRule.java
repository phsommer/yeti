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
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.NesCModule;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;

/**
 * Searches for the occurrence of the trigger "<interface name>." and can replace
 * that token by a skeleton for a command or an event.
 * @author Benjamin Sigg
 */
public class InterfaceFieldImplementationRule implements IProposalRule{
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            IDocumentMap document = collector.getLocation().getDocument();
            int offset = collector.getOffset();

            offset = RuleUtility.begin( offset-1, document, "." );
            if( offset < 0 )
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
            
            String wordInterfaceName = RuleUtility.reverseWord( offset-1, document );
            if( wordInterfaceName == null )
                return;
            
            int wordInterfaceNameBegin = RuleUtility.reverseWordBegin( offset-1, document );
            String whitespaces = RuleUtility.whitespaceLineBegin( wordInterfaceNameBegin, document );
            
            Module module = (Module)node;
            NesCModule nesCModule = module.resolveNode().resolve( ast.getBindingResolver() );
            if( nesCModule == null )
                return;
            
            for( int i = 0, n = nesCModule.getProvidesCount(); i<n; i++ ){
                NesCInterfaceReference reference = nesCModule.getProvides( i );
                if( reference != null && reference.getName().toIdentifier().equals( wordInterfaceName )){
                    NesCInterface interfaze = reference.getParameterizedReference();
                    if( interfaze != null ){
                        for( int j = 0, m = interfaze.getFieldCount(); j<m; j++ ){
                            Field field = interfaze.getField( j );
                            if( field != null && field.getModifiers() != null && field.getModifiers().isCommand() ){
                                collector.add( ProposalUtility.createProposal( reference, field, wordInterfaceNameBegin, whitespaces, collector.getLocation() ) );
                            }
                        }
                    }
                }
            }
            
            for( int i = 0, n = nesCModule.getUsesCount(); i<n; i++ ){
                NesCInterfaceReference reference = nesCModule.getUses( i );
                if( reference != null && reference.getName().toIdentifier().equals( wordInterfaceName )){
                    NesCInterface interfaze = reference.getParameterizedReference();
                    if( interfaze != null ){
                        for( int j = 0, m = interfaze.getFieldCount(); j<m; j++ ){
                            Field field = interfaze.getField( j );
                            if( field != null && field.getModifiers() != null && field.getModifiers().isEvent() ){
                                collector.add( ProposalUtility.createProposal( reference, field, wordInterfaceNameBegin, whitespaces, collector.getLocation() ) );
                            }
                        }
                    }
                }
            }
        }
        catch( BadLocationException ex ){
            // ignore
        }
    }
}
