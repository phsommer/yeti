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
import tinyos.yeti.ep.parser.IHoverInformation;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.ep.rules.DocumentRegionInformation;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeUtility;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypedefType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.utility.Icon;

public class FieldDeclarationHover extends AbstractInformationRule implements IHoverInformationRule{
	public IHoverInformation getInformation( NesC12AST ast, DocumentRegionInformation region ){
		ASTNode node = region.getNode();
		if( !(node instanceof Identifier ))
			return null;
		
		Identifier id = (Identifier)node;
		if( !(id.getParent() instanceof DeclaratorName) ){
			return null;
		}
		
		DeclaratorName declarator = (DeclaratorName)id.getParent();
		Type type = declarator.resolveType();
		if( type == null )
			return null;
		
		
		Modifiers modifiers = declarator.resolveModifiers();
		String name = id.getName();
		ModelAttribute[] attributes = declarator.resolveModelAttributes();
		
		String content = getFieldDocumentation( id, region );
		
		if( modifiers != null && modifiers.isTypedef() ){
			type = new TypedefType( name, type );
			TagSet tags = TagSet.get( NesC12ASTModel.TYPEDEF, NesC12ASTModel.TYPE );
			return new HoverInformation( new Icon( tags, attributes ), TypeUtility.toAstNodeLabel( type ), null, null );
		}
		else{
			TagSet tags = FieldModelNode.getFieldTags( modifiers, type, false );
			return new HoverInformation( new Icon( tags, attributes ), type.toLabel( name, Type.Label.SMALL ), content, null );
		}
	}
}
