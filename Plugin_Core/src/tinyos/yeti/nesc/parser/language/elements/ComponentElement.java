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

import org.eclipse.jface.resource.ImageDescriptor;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.nesc.parser.Declaration;

public class ComponentElement extends RenamedIdentifierElement {

	Declaration declarations[] = null;
	

	public ComponentElement(Element e) {
		super(e);
		image = null;
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		if (image != null) return image;
		if (getRenamed() != null) {
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMPONENT_RENAMED);
		} else {
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMPONENT);
		}
	}

    @Override
	public Declaration[] getDeclarations() {
		return declarations;
	}

    @Override
	public void setDeclarations(Declaration delcarations[]) {
		this.declarations = delcarations;
	}
	
}
