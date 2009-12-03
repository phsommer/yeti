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
import tinyos.yeti.nesc12.ep.rules.DocumentRegionInformation;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.EnumConstant;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.utility.Icon;

public class EnumConstantHover implements IHoverInformationRule{
	public IHoverInformation getInformation( NesC12AST ast, DocumentRegionInformation region ){
		ASTNode node = region.getNode();
		if( !(node instanceof Identifier ))
			return null;
		
		Identifier id = (Identifier)node;
		if( !(node.getParent() instanceof EnumConstant ))
			return null;
		
		EnumConstant constant = (EnumConstant)node.getParent();
		
		String name = id.getName();
		Value value = constant.resolveValue();
		
		if( value != null ){
			name = name + " : " + value.toLabel();
		}
		
		return new HoverInformation( new Icon( TagSet.get( NesC12ASTModel.ENUMERATION_CONSTANT ), null ), name, null, null );
	}
}
