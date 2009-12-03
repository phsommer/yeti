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

public class InitDeclaratorElement extends Element {
	
	DirectDeclaratorElement directDeclarator;
	PointerElement pointer;
	InitializerElement initializer;
	
	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		if (directDeclarator != null) directDeclarator.updatePosition(region);
		if (pointer != null) pointer.updatePosition(region);
		if (initializer != null) initializer.updatePosition(region);
	}

	public InitDeclaratorElement(DirectDeclaratorElement element) {
		super(element);
		this.directDeclarator = element;
	}

	public InitDeclaratorElement(DirectDeclaratorElement element, InitializerElement element2) {
		super(element,element2);
		this.directDeclarator = element;
		this.initializer = element2;
	}

	public InitDeclaratorElement(PointerElement element, DirectDeclaratorElement element2) {
		super(element,element2);
		this.pointer = element;
		this.directDeclarator = element2;
	}

	public InitDeclaratorElement(PointerElement element, DirectDeclaratorElement element2, InitializerElement element3) {
		super(element,element3);
		this.pointer = element;
		this.directDeclarator = element2;
		this.initializer = element3; 
	}

	public InitDeclaratorElement(Token object) {
		super(object);
	}

	/**
	 */
	public String getLabel(Object o)
	{
		if (directDeclarator!=null) {
			return directDeclarator.getLabel(null);
		}
		return name;
	}


}
