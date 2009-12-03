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

import java.util.List;

import org.eclipse.jface.text.BadLocationException;

import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.ep.parser.HoverInformation;
import tinyos.yeti.ep.parser.IHoverInformation;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.rules.DocumentRegionInformation;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.EnumerationConstant;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.meta.GenericRangedCollection;
import tinyos.yeti.utility.Icon;

public class LocalFieldHover extends AbstractInformationRule implements IHoverInformationRule{
	public IHoverInformation getInformation( NesC12AST ast, DocumentRegionInformation region ){
	    try{
            if( ast.getRanges() == null )
                return null;
            
            // ensure not conflicting with an access rule, no . or -> in front
            IDocumentMap document = region.getLocation().getDocument();
            
            int offset = region.getOffset().getInputfileOffset();
            
            if( RuleUtility.hasBefore( offset, document, "." ))
                return null;
            
            if( RuleUtility.hasBefore( offset, document, "->" ))
                return null;
            
            INesC12Location location = region.getOffset();
            ASTNode node = region.getNode();
            
            if( !(node instanceof Identifier) )
                return null;
            
            ASTNode parent = node.getParent();
            
            boolean identifierExpression = parent instanceof IdentifierExpression;
            boolean enumerationConstant = parent instanceof EnumerationConstant;
            
            if( identifierExpression || enumerationConstant ){
                String name = ((Identifier)node).getName();
                
                GenericRangedCollection ranges = ast.getRanges();
                
                String content = getFieldDocumentation( (Identifier)node, region );
                List<Field> fields = ranges.getFields( location.getInputfileOffset() );
                
                for( Field field : fields ){
                    if( field.getName() != null && field.getName().toIdentifier().equals( name )){
                    	if( enumerationConstant ){
                    		String title;
                    		FieldModelNode fieldNode = field.asNode();
                    		if( fieldNode != null )
                    			title = fieldNode.getLabel();
                    		else{
                    			title = field.getName().toIdentifier();
                    		}
                    		
                    		TagSet tags = TagSet.get( NesC12ASTModel.ENUMERATION_CONSTANT );
                    		return new HoverInformation( new Icon( tags, field.getAttributes() ), title, content, null );
                    	}
                    	else if( identifierExpression ){
	                    	Type type = field.getType();
	                    	if( type != null ){
	                    		String title = type.toLabel( field.getName().toIdentifier(), Type.Label.SMALL );
	                    	 	TagSet tags = FieldModelNode.getFieldTags( field.getModifiers(), type, false );
	                    	 	
	                    	 	return new HoverInformation( new Icon( tags, field.getAttributes() ), title, content, null );
	                    	}
                    	}
                    }
                }
            }
        }
        catch( BadLocationException ex ){
            // ignore
        }
        return null;
	}
}
