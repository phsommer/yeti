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

import tinyos.yeti.ep.parser.HoverInformation;
import tinyos.yeti.ep.parser.IASTModelAttribute;
import tinyos.yeti.ep.parser.IHoverInformation;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.rules.DocumentRegionInformation;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.AttributeDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.EnumDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.utility.Icon;

public class TypeDeclarationHover extends AbstractInformationRule implements IHoverInformationRule{
	public IHoverInformation getInformation( NesC12AST ast, DocumentRegionInformation region ){
		ASTNode node = region.getNode();
		if( !(node instanceof Identifier ))
			return null;
		
		Identifier id = (Identifier)node;
		ASTNode parent = id.getParent();
		
		Type type = null;
		String text = null;
		IASTModelAttribute[] attributes = null;
		
		if( parent instanceof DataObjectDeclaration ){
			DataObjectDeclaration decl = (DataObjectDeclaration)parent;
			
			type = decl.resolveType();
			text = type.toLabel( null, Type.Label.SMALL );
			attributes = decl.resolveModelAttributes();
		}
		else if( parent instanceof EnumDeclaration ){
			EnumDeclaration decl = (EnumDeclaration)parent;
			
			type = decl.resolveType();
			text = type.toLabel( null, Type.Label.SMALL );
			attributes = decl.resolveAttributes();
		}
		else if( parent instanceof AttributeDeclaration ){
			AttributeDeclaration decl = (AttributeDeclaration)parent;
			
			type = decl.resolveType();
			text = id.getName();
			attributes = decl.resolveModelAttributes();
		}
		
		if( type != null ){
			String content = getFieldDocumentation( id, region );
		    return new HoverInformation( new Icon( getTags( type ), attributes ), text, content, null );
		}
		
		return null;
	}
	
    public static TagSet getTags( Type type ){
    	TagSet tags = new TagSet();
    	
    	DataObjectType data = type.asDataObjectType();
    	if( data != null ){
    		tags.add( Tag.DATA_OBJECT );
    		if( data != null ){
    			if( data.isStruct() ){
    				tags.add( Tag.STRUCT );
    			}
    			else if( data.isUnion() ){
    				tags.add( Tag.UNION );
    			}
    			else if( data.isAttribute() ){
    				tags.add( Tag.ATTRIBUTE );
    			}
    		}
    	}
    	else if( type.asEnumType() != null ){
    		tags.add( NesC12ASTModel.ENUMERATION );
    	}
    	
        return tags;
    }
}
