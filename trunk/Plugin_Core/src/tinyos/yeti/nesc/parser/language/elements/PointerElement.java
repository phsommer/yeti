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

import org.eclipse.jface.text.reconciler.DirtyRegion;

import tinyos.yeti.nesc.scanner.Token;

public class PointerElement extends Element {

	// Implicit *
	
	private TypeQualifier[] typeQualifiers;
	private PointerElement pointer;
	
	/**
	 * Token '*'
	 * @param token
	 */
	public PointerElement(Token token) {
		super(token);
	}
	
	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		if (typeQualifiers!=null) {
			for (int i = 0; i < typeQualifiers.length; i++) {
				typeQualifiers[i].updatePosition(region);
			}
		}
		if (pointer!=null) pointer.updatePosition(region);
	}

	/**
	 * Token '*' , ArrayList typequalifiers
	 * @param token
	 * @param list
	 */
	public PointerElement(Token token, ArrayList<TypeQualifier> list) {
		super(token,(Element)list.get(list.size()));
		typeQualifiers = list.toArray(new TypeQualifier[list.size()]);
	}

	
	public PointerElement(Token token, PointerElement element) {
		super(token,element);
	}

	public void setTypeQualifierList(ArrayList<TypeQualifier> list) {
		typeQualifiers = list.toArray(new TypeQualifier[list.size()]);
	}



}
