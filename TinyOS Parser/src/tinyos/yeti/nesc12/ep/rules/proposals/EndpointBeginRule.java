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

import tinyos.yeti.ep.parser.INesCCompletionProposal;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Endpoint;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;

public class EndpointBeginRule implements IProposalRule{
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            INesC12Location location = ast.getOffsetInput( collector.getOffset() );

            if( RuleUtility.hasBefore( location.getInputfileOffset()-1, collector.getLocation().getDocument(), "." ))
                return;

            if( RuleUtility.hasBefore( location.getInputfileOffset()-1, collector.getLocation().getDocument(), "components" ))
                return;

            boolean nothing = true;
            loop:for( int i = 0; i < 2 && nothing; i++ ){
                ASTNode node;
                if( i == 0 )
                    node = RuleUtility.nodeAt( location, ast.getRoot() );
                else
                    node = RuleUtility.nodeAtOrBefore( location, ast.getRoot() );

                if( node == null )
                    continue;

                INesC12Location nodeLocation = ast.getOffsetAtEnd( node );

                String componentName = null;

                while( node != null && componentName == null ){
                    if( node instanceof Identifier ){
                        // should be part of an endpoint
                        ASTNode nodeParameterizedIdentifier = node.getParent();
                        if( !(nodeParameterizedIdentifier instanceof ParameterizedIdentifier))
                            continue loop;

                        ParameterizedIdentifier parameterizedIdentifier = (ParameterizedIdentifier)nodeParameterizedIdentifier;
                        if( parameterizedIdentifier.getIdentifier() != node )
                            continue loop;

                        ASTNode nodeEndpoint = parameterizedIdentifier.getParent();
                        if( !(nodeEndpoint instanceof Endpoint ))
                            continue loop;

                        Endpoint endpoint = (Endpoint)nodeEndpoint;
                        if( endpoint.getComponent() != parameterizedIdentifier )
                            continue loop;

                        componentName = ((Identifier)node).getName();
                    }
                    else if( node instanceof ConfigurationDeclarationList ){
                        componentName = "";
                    }

                    if( componentName == null ){
                        node = node.getParent();
                    }
                }

                if( componentName == null )
                    continue;

                List<ComponentReferenceModelConnection> references =
                    ast.getRanges().getComponentReferences( 
                            Math.min( location.getInputfileOffset(), nodeLocation.getInputfileOffset() ) );

                for( ComponentReferenceModelConnection reference : references ){
                    INesCCompletionProposal proposal = ProposalUtility.createProposal( reference, collector.getLocation() );
                    if( proposal != null ){
                        nothing = false;
                        collector.add( proposal );
                    }
                }

                List<InterfaceReferenceModelConnection> interfaces = 
                    ast.getRanges().getInterfaceReferences( 
                            Math.min( location.getInputfileOffset(), nodeLocation.getInputfileOffset() ) );
                for( InterfaceReferenceModelConnection interfaze : interfaces ){
                    INesCCompletionProposal proposal = ProposalUtility.createProposal( interfaze, collector.getLocation() );
                    if( proposal != null ){
                        nothing = false;
                        collector.add( proposal );
                    }
                }
                
                List<Field> fields = ast.getRanges().getFields( Math.min( location.getInputfileOffset(), nodeLocation.getInputfileOffset() ) );
                for( Field field : fields ){
                    FieldModelNode modelNode = field.asNode();
                    if( modelNode != null ){
                        if( modelNode.getTags().contains( Tag.USES ) || modelNode.getTags().contains( Tag.PROVIDES )){
                            INesCCompletionProposal proposal = ProposalUtility.createProposal( field, collector.getLocation(), false, ast );
                            if( proposal != null ){
                                nothing = false;
                                collector.add( proposal );
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
