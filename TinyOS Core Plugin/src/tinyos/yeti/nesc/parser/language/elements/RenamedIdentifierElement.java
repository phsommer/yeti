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

public class RenamedIdentifierElement extends IdentifierElement {

	protected String renamedTo = null;
	
	public RenamedIdentifierElement(Token token) {
		super(token);
	}

	public RenamedIdentifierElement(String string, Token t1, Token t2) {
		super(string,t1,t2);
	}

	public RenamedIdentifierElement(Element e) {
		super(e);
	}

	/**
	 * Method declared on IWorkbenchAdapter
	 * overrides Element.getLabel
	 */
	public String getLabel(Object o) {
		if (renamedTo != null) {
			if (renamedTo.equals(name)) {
				return name;
			} else {
				return renamedTo + " ("+name+")";
			}
		} else {
			return name;
		}
	}
	
	public void setRenamedTo(String s) {
		this.renamedTo = s;
	}
	public String getRenamed() {
		if (renamedTo != null) {
			return renamedTo;
		} else {
			return name;
		}
	}


}
