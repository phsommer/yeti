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

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.nesc.parser.Declaration;
import tinyos.yeti.nesc.scanner.Token;

public class IncludesElement extends Element  {

	File reference = null;
	Declaration delcarations[] = null;
	
	Token includes;
	Token semikolon;
	Element elem;
	
	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		if (includes != null) {
			if (includes.updatePosition(region)) includes = null;
		}
		if (semikolon != null) {
			if (semikolon.updatePosition(region)) semikolon = null;
		}
		if (elem != null) elem.updatePosition(region);
	}
	
	public IncludesElement(Element e, Token token, Token token2) {
		super(e);
		elem = e;
		includes = token;  offset=includes.offset;
		semikolon = token2;	length=semikolon.offset-includes.offset+semikolon.length();
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_INCLUDE);
	}
	
	public Position getPositionForOutline() {
		return elem.getPositionForOutline();
	}
	
	public void setFile(File f) {
		this.reference = f;
	}
	
    @Override
	public void setDeclarations(Declaration declarations[]) {
		this.delcarations = declarations;
	}
	
	public File getFile() {
		return reference;
	}

    @Override
	public Declaration[] getDeclarations() {
		return delcarations;
	}
	

}
