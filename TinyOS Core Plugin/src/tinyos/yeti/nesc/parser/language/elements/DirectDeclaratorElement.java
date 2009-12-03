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
import java.util.Iterator;

import org.eclipse.jface.text.reconciler.DirtyRegion;

import tinyos.yeti.nesc.scanner.Token;


public class DirectDeclaratorElement extends Element{

	String identifier = null;
	ArrayList<ParameterDeclarationElement> parameterTypeList = null;
	ArrayList identifierList = null;
	ArrayList parameters = null;
	Element constantExpression = null;
	
	PointerElement pointer = null;
	
	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		if (pointer != null) pointer.updatePosition(region);
		if (constantExpression!=null) constantExpression.updatePosition(region);
		Iterator iter;
		if (parameterTypeList != null) {
		iter = parameterTypeList.iterator();
		while(iter.hasNext()) {
			((Element) iter.next()).updatePosition(region);
		}
		}
		if (identifierList!=null) {
		iter = identifierList.iterator();
		while(iter.hasNext()) {
			((Element) iter.next()).updatePosition(region);
		}
		}
		if (parameters != null) {
		iter = parameters.iterator();
		while(iter.hasNext()) {
			((Element) iter.next()).updatePosition(region);
		}}
	}
	
	
	/*  pointer direct_declarator */ 
	public void setPointer(PointerElement element) {
		this.pointer = element;
	}
	
	public int getParameterCount() {
		if (parameterTypeList == null) return 0;
		return parameterTypeList.size();
	}
	
	public ParameterDeclarationElement[] getParameters() {
		if (parameterTypeList!= null)
		return (ParameterDeclarationElement[]) parameterTypeList.toArray(new ParameterDeclarationElement[parameterTypeList.size()]);
		else return null;
	}
	
	private String getParameterTypeNames() {
		String t = "";
		if (parameterTypeList != null) {
			Iterator iter = parameterTypeList.iterator();
			while(iter.hasNext()){
				ParameterDeclarationElement e = (ParameterDeclarationElement) iter.next();
				Iterator iter2  = e.declarationSpecifiers.iterator();
				while(iter2.hasNext()) {
					Element e2 = (Element) iter2.next();
					if (e2 instanceof TypeSpecifierElement) {
						t+= e2.name;
					}
				}
				if (iter.hasNext()) {
					 t+= ",";	
				}
			}
		}
		return t;
	}
	

	public DirectDeclaratorElement(String string, Token token) {
		super(string,token);
	}

	public DirectDeclaratorElement(String string, Token token, Token token2) {
		super(string, token, token2);
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getLabel(Object o) {
		String t = "";
		if (identifier != null) {
			t += identifier;
		}
		t += "(" + getParameterTypeNames() +")";
		return t;
	}
	
	public void setConstantExpression(Element e) {
		this.constantExpression = e;
	}
	
	public void setIdentifier(Token i) {
		this.identifier = i.text;
	}
	
	public void setIdentifier(Token i, Token j) {
		this.identifier = i.text + "." +j.text;
	}
	
	public void setParameterTypeList(ArrayList<ParameterDeclarationElement> l) {
		this.parameterTypeList = l;
	}

	public void setIdentifierList(ArrayList l) {
		this.identifierList = l;
	}
	
	public void setParameters(ArrayList l) {
		this.parameters = l;
	}
	

}
