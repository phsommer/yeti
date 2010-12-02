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

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.nesc.scanner.Token;

public class ConnectionElement extends Element {

	int operator = -1;
	String op = null;
	EndpointElement left = null;
	EndpointElement rigth = null;
	
	boolean wiresFunctions = false;
	
	public void setWiresFunctionEndpoints(boolean wiresFunctions) {
		this.wiresFunctions = wiresFunctions;
	}
	
	public boolean wiresFunctionEndpoints() {
		return wiresFunctions;
	}
	
	/**
	 * "="
	 */
	static final public int EQUATE_WIRES = 0;
	
	/**
	 * "<-"
	 */
	static final public int LINK_WIRES_INVERSE = 1;
	
	/**
	 * "->"
	 */
	static final public int LINK_WIRES = 2;
	
	public EndpointElement getLeft() {
		return left;
	}
	
	public EndpointElement getRight() {
		return rigth;
	}
	
	public int getOperator() {
		return operator;
	}
	
	public ConnectionElement(String string, Element e, Token t) {
		super(string,e,t);
		image = null;
	}

	public void setOperator(String op)  {
		this.op = op;
		if (op.equals("=")) {
			operator = 0;
			image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_EQUATE_WIRES);
		} else if (op.equals("<-")) {
			operator = 1;
			image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_LINK_WIRES_INVERSE);
		} else if (op.equals("->")) {
			operator = 2;
			image =  NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_LINK_WIRES);
		}
	}

	public void setLeft(EndpointElement element) {
		this.left = element;	
	}

	public void setRigth(EndpointElement element) {
		this.rigth = element;
	}
	
	@Override
	public String getLabel(Object o) {
		return left.getLabel(null)+ " "+ op + " " +rigth.getLabel(null);
	}

	public String getOperatorString() {
		return op;
	}
	
}
