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

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.nesc.scanner.Token;

public class AssignmentOperator extends Element{

	public AssignmentOperator(Token token) {
		super(token);
	}
	/**
	 * Method declared on IWorkbenchAdapter
	 */
	public String getLabel(Object o)
	{
		String res = new String();
		for (int i = 0; i < (children.size()-1); i++) {
			res +=((Element)children.get(i)).getLabel(null)+",";
		}
		if (children.size() >0) {
			res += ((Element)children.get(children.size()-1)).getLabel(null);
		}
		
		return res;
	}
}
