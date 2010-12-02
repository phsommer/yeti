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
package tinyos.yeti.nesc.parser.language.elements;

import org.eclipse.jface.text.reconciler.DirtyRegion;

import tinyos.yeti.nesc.scanner.Token;

public class PostFixExpression extends Expression{

	PrimaryExpression primaryExpression = null;
	
	public PostFixExpression(PrimaryExpression e) {
		super(e);
		this.primaryExpression = e;
	}
	
	public PostFixExpression(String string, Element element, Token token) {
		super(string, element, token);
	}
	public PostFixExpression(PrimaryExpression expression, Element element, Token token) {
		super("",element,token);
		this.primaryExpression = expression;
	}

	public PostFixExpression(ArgumentExpressionList list, Element element, Token token) {
		super("",element,token);
		//System.out.println(list);
	}

	public String getLabel(Object o) {
		return primaryExpression.getLabel(null);
	}
	
	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		if (primaryExpression!=null) primaryExpression.updatePosition(region);
	}
	
}
