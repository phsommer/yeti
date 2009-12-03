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
package tinyos.yeti.nesc12.ep.rules.hyperlink;

import java.util.List;

import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.ConnectionModelNode;
import tinyos.yeti.nesc12.ep.nodes.EndpointModelConnection;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Connection;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Endpoint;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.preprocessor.RangeDescription;

public class EndpointHyperlink implements IHyperlinkRule{
    public void search( NesC12AST ast, HyperlinkCollector collector ){
        if( ast.getRanges() == null )
            return;
        
        INesC12Location location = ast.getOffsetInput( collector.getLocation().getRegion().getOffset() );
        ASTNode nodeIdentifier = RuleUtility.nodeAt( location, ast.getRoot() );
        if( nodeIdentifier == null )
            return;
        
        if( !(nodeIdentifier instanceof Identifier ))
            return;
        
        ASTNode nodeParameterizedIdentifier = nodeIdentifier.getParent();
        if( !(nodeParameterizedIdentifier instanceof ParameterizedIdentifier ))
            return;
        
        ASTNode nodeEndpoint = nodeParameterizedIdentifier.getParent();
        if( !(nodeEndpoint instanceof Endpoint ))
            return;
        
        Endpoint endpoint = (Endpoint)nodeEndpoint;
        if( endpoint.getComponent() == nodeParameterizedIdentifier ){
            String name = ((Identifier)nodeIdentifier).getName();
            IFileRegion sourceRegion = ast.getRegion( nodeIdentifier );
            
            // try component
            List<ComponentReferenceModelConnection> components =
                ast.getRanges().getComponentReferences( location.getInputfileOffset() );
            
            for( ComponentReferenceModelConnection reference : components ){
                if( name.equals( reference.getName() )){
                    IFileRegion targetRegion = reference.getRegion();
                    if( targetRegion != null ){
                        collector.add( new FileHyperlink( sourceRegion, targetRegion ) );
                    }
                }
            }
            
            // try interface
            List<InterfaceReferenceModelConnection> interfaces =
                ast.getRanges().getInterfaceReferences( location.getInputfileOffset() );
            for( InterfaceReferenceModelConnection reference : interfaces ){
                if( name.equals( reference.getName().toIdentifier() )){
                    IFileRegion targetRegion = reference.getRegion();
                    if( targetRegion != null ){
                        collector.add( new FileHyperlink( sourceRegion, targetRegion ));
                    }
                }
            }
            
            // try functions
            List<Field> fields = ast.getRanges().getFields( location.getInputfileOffset() );
            for( Field field : fields ){
                Name fieldName = field.getName();
                if( fieldName != null ){
                    if( name.equals( fieldName.toIdentifier() )){
                        RangeDescription range = fieldName.getRange();
                        IFileRegion targetRegion = RuleUtility.source( range );
                        if( targetRegion != null ){
                            collector.add( new FileHyperlink( sourceRegion, targetRegion ) );
                        }
                    }
                }
            }
        }
        else if( endpoint.getSpecification() == nodeParameterizedIdentifier ){
            // points to an element of a component
            ASTNode nodeConnection = endpoint.getParent();
            if( nodeConnection instanceof Connection ){
                Connection connection = (Connection)nodeConnection;
                ConnectionModelNode modelNode = connection.resolveConnection();
                EndpointModelConnection endpointConnection = null;
                if( modelNode != null ){
                    if( connection.getLeftEndpoint() == endpoint ){
                        endpointConnection = modelNode.getLeft();
                    }
                    else if( connection.getRightEndpoint() == endpoint ){
                        endpointConnection = modelNode.getRight();
                    }
                }
                if( endpointConnection != null ){
                    ModelConnection referenced = endpointConnection.getReference();
                    if( referenced != null ){
                        IFileRegion sourceRegion = ast.getRegion( nodeIdentifier );
                        IFileRegion targetRegion = referenced.getRegion();
                        if( sourceRegion != null && targetRegion != null ){
                            collector.add( new FileHyperlink( sourceRegion, targetRegion ) );
                        }
                    }
                }
            }
        }
    }
}
