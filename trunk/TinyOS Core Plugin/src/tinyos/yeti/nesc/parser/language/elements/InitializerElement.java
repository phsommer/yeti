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

import java.util.ArrayList;

import tinyos.yeti.nesc.scanner.Token;

public class InitializerElement extends Element {

	/**
	 * assignment_expression
	 * @param expression
	 */
	public InitializerElement(AssignmentExpression expression) {
		super(expression);
	}

	/**
	 * '{' initializer_list '}'	
	 * @param token
	 * @param list
	 * @param token2
	 */
	public InitializerElement(Token token, ArrayList list, Token token2) {
		super(token,token2);
	}

	/** '{' initializer_list ',' '}' */
	public InitializerElement(Token token, ArrayList list, Token token2, Token token3) {
		super(token,token3);
	}




}
