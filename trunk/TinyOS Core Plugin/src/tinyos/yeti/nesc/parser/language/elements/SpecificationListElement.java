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

import org.eclipse.jface.resource.ImageDescriptor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.nesc.parser.language.SemanticError;
import tinyos.yeti.nesc.scanner.ITokenInfo;

public class SpecificationListElement extends Element {
	
	public SpecificationListElement(ITokenInfo it) {
		super(it);
		image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_SPECIFICATION);
	}

	public SpecificationListElement(ITokenInfo token, ITokenInfo token2) {
		super("Specification",token,token2);
		image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_SPECIFICATION);
	}

	@SuppressWarnings("unchecked")
	public SemanticError[] getSemanticErrors( ProjectTOS project ) {
		ArrayList errors = new ArrayList();
		// interfaces only have function-declarations with
		// command or event storage classes..
		for (int i = 0; i < children.size(); i++) {
			Object o = children.get(i);
			if (o instanceof DeclarationElement) {
				DeclarationElement de = (DeclarationElement) o;
				if ((de.command==false)&&(de.event==false)) {
					
					ITokenInfo it = (ITokenInfo) de.storageClassSpecifierElements.get("task");
					SemanticError se = null;
					if (it != null) {
						se= new SemanticError("Only interfaces, commands and " +
								"events can be defined in specificiation",it);
					} else {
						 se= new SemanticError("Only interfaces, commands and " +
									"events can be defined in specificiation",de);
						
					}

					se.expected= new String[]{"COMMAND","EVENT","INTERFACE"};
					errors.add(se);
				}
			}
		}
		if (errors.size() > 0) {
			return (SemanticError[]) errors.toArray(new SemanticError[errors.size()]);
		} 
		return null;
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return image;
	}

}
