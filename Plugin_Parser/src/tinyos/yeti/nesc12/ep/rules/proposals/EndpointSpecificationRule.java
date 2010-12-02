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

import java.util.List;

import org.eclipse.jface.text.BadLocationException;

import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.NesCComponent;
import tinyos.yeti.nesc12.parser.ast.elements.NesCComponentReference;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;

public class EndpointSpecificationRule implements IProposalRule{
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            int offset = collector.getOffset();
            int begin = RuleUtility.begin( offset-1, collector.getLocation().getDocument(), "." ) - 1;
            if( begin < 0 )
                return;
            
            String name = null;
            int inputLocation = -1;
            
            ASTNode node = RuleUtility.nodeAtOrBefore( ast.getOffsetInput( begin ), ast.getRoot() );
            if( node != null ){
                int limit = RuleUtility.lastIndexOf( 0, offset-1, collector.getLocation().getDocument(), ";", "->", "<-", "=", "{" );
                if( limit >= 0 ){
                    INesC12Location limitLocation = ast.getOffsetInput( limit );
                
                    if( node.getRange().getLeft() < limitLocation.getPreprocessedOffset() ){
                        inputLocation = limit+1;
                        node = null;
                    }
                }
            }
            
            while( node != null ){
                if( node instanceof ParameterizedIdentifier ){
                    break;
                }
                node = node.getParent();
            }
            if( node != null ){
             	Identifier identifier = ((ParameterizedIdentifier)node).getIdentifier();
            	if( identifier != null ){
            	    name = identifier.getName();
            	    if( inputLocation < 0 )
            	        inputLocation = ast.getOffsetAtEnd( node ).getInputfileOffset();
            	}
            }
            
            if( name == null ){
            	name = RuleUtility.reverseWord( begin, collector.getLocation().getDocument() );
            	if( inputLocation < 0 && name != null )
            	    inputLocation = begin - name.length() - 1;
            }
            
            if( name == null ){
            	return;
            }
            
            List<ComponentReferenceModelConnection> references =
                ast.getRanges().getComponentReferences( inputLocation );
            
            for( ComponentReferenceModelConnection reference : references ){
                if( reference.getName().equals( name )){
                    NesCComponentReference componentReference = reference.resolve( ast.getBindingResolver() );
                    if( componentReference != null ){
                        NesCComponent component = componentReference.getParameterizedComponent();
                        if( component != null ){
                            for( int i = 0, n = component.getUsesProvidesCount(); i<n; i++ ){
                                Binding binding = component.getUsesProvides( i );
                                if( binding instanceof Field ){
                                    collector.add( ProposalUtility.createProposal( (Field)binding, collector.getLocation(), false, ast ) );
                                }
                                if( binding instanceof NesCInterfaceReference ){
                                    collector.add( ProposalUtility.createProposal( ((NesCInterfaceReference)binding).getModel(), collector.getLocation() ) );
                                }
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
