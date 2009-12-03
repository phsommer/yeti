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

import tinyos.yeti.nesc.scanner.Token;

public class UnaryExpression extends Expression {
	
	boolean inc_op = false;
	boolean dec_op = false;
	boolean size_of = false;

	public UnaryExpression(Element element) {
		super(element);
	}

	public UnaryExpression(String string, Token token, Token token2) {
		super(string, token, token2);
	}

	public void setIncOp() {
		inc_op = true;
	}
	
	public void setDecOp() {
		dec_op = true;
	}

	public void setSizeOf() {
		size_of = true;
	}
}

