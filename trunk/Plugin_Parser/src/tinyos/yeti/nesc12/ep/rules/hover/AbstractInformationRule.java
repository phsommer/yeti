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
package tinyos.yeti.nesc12.ep.rules.hover;

import tinyos.yeti.nesc12.ep.NesC12DocComment;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.rules.DocumentRegionInformation;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.preprocessor.comment.NesCDocComment;

public abstract class AbstractInformationRule implements IHoverInformationRule{
	/**
	 * Searches documentation for <code>field</code>.
	 * @param field the identifier of some field
	 * @param region additional information about the document
	 * @return the documentation or <code>null</code>
	 */
	protected String getFieldDocumentation( Identifier field, DocumentRegionInformation region ){
		ASTNode node = field;
		
		while( node != null ){
    		if( node instanceof FunctionDefinition ){
    			FunctionDefinition definition = (FunctionDefinition)node;
    			FieldModelNode fieldNode = definition.resolveNode();
    			if( fieldNode != null ){
    				Name[] arguments = fieldNode.getArgumentNames();
    				if( arguments != null ){
    					for( Name name : arguments ){
    						if( name != null ){
    							if( name.toIdentifier().equals( field.getName() )){
    								NesCDocComment comment = getFunctionDocumentation( definition, region );
    								if( comment == null )
    									return null;
    								return comment.getAnalysis().getParameterDescription( field.getName() );
    							}
    						}
    					}
    				}
    			}
    		}
    		
    		NesCDocComment[] comments = node.getComments();
    		if( comments != null ){
    			for( int i = comments.length-1; i >= 0; i-- ){
    				String documentation = comments[i].getAnalysis().getParameterDescription( field.getName() );
    				if( documentation != null )
    					return documentation;
    			}
    		}
    		
    		node = node.getParent();
    	}
		
		return null;
	}
	
    /**
     * Searches the documentation of the function in which <code>node</code>
     * lies.
     * @param node some node or a function definition
     * @param information  additional information about the region
     * @return the documentation of the function or <code>null</code>
     */
    protected NesCDocComment getFunctionDocumentation( FunctionDefinition node, DocumentRegionInformation information ){
    	NesCDocComment[] comments = node.getComments();
        if( comments != null && comments.length > 0 )
        	return comments[ comments.length-1 ];
        
        Name definitionName = node.resolveName();
        if( definitionName == null )
        	return null;
 
        String[] segments = definitionName.segments();
        if( segments.length != 2 )
        	return null;
        
        NesCInterfaceReference reference = node.resolveInterface();
        if( reference == null )
        	return null;
        
        NesCInterface interfaze = reference.getRawReference();
        if( interfaze == null )
        	return null;
        
        Field base = interfaze.getField( segments[1] );
        if( base == null )
        	return null;
        
        FieldModelNode baseNode = base.asNode();
        if( baseNode == null )
        	return null;
        
        NesC12DocComment nesComment = baseNode.getDocumentation();
        return new NesCDocComment( 0, null, nesComment.getComment(), true );
    }
}
