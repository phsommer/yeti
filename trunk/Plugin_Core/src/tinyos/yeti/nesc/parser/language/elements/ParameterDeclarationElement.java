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

public class ParameterDeclarationElement extends Element {
	
	ArrayList declarationSpecifiers = null;
	DirectDeclaratorElement declarator = null;

	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		if (declarationSpecifiers!=null) {
			Iterator iter = declarationSpecifiers.iterator();
			while(iter.hasNext()) {
				((Element) iter.next()).updatePosition(region);
			}
		}
		if (declarator!=null) declarator.updatePosition(region);
	}
	
	public ParameterDeclarationElement(String string, ArrayList list, Element element) {
		super(string, list, element);
	}

	public ParameterDeclarationElement(String string, ArrayList list) {
		super(string, list);
	}

	public void setDeclarationSpecifiers(ArrayList l) {
		this.declarationSpecifiers = l;
	}
	
	public void setDeclarator(DirectDeclaratorElement e){
		this.declarator = e;
	}


}
