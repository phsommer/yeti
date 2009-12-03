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

public class SpecificationElement extends RenamedIdentifierElement implements ISpecificationElement {
	
	private boolean provides = false;
	private boolean uses = false;
	
	private boolean hasParameters = false;
	
	Declaration[] declarations;
	
	public SpecificationElement(RenamedIdentifierElement e) {
		super((Element)e);
		this.setRenamedTo(e.getRenamed());
		image = null;
	}
	
	@Override
	public String getRenamed() {
		return super.getRenamed();
	}
	
	public void setProvides() {
		provides = true;
	}
	
	public void setUses() {
		uses = true;
	}
	
	public boolean isProvides() {
		return provides;
	}
	
	public void setHasParameters() {
		this.hasParameters = true;
	}
		
	public ImageDescriptor getImageDescriptor(Object object)	{
		if (image != null) return image;
		if (provides) {
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_PROVIDES_INTERFACE);
		} else  {
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_USES_INTERFACE);
		}
	}

    @Override
	public Declaration[] getDeclarations() {
		return declarations;

	}

    @Override
	public void setDeclarations(Declaration declarations[]) {
		this.declarations = declarations;
	}

}
