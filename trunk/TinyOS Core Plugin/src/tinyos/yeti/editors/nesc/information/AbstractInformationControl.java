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
package tinyos.yeti.editors.nesc.information;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Basic implementation of {@link INesCInformationControl}.
 * @author Benjamin Sigg
 *
 */
public abstract class AbstractInformationControl implements INesCInformationControl{
	protected final INesCInformationControlOwner owner;
	
	protected AbstractInformationControl( INesCInformationControlOwner owner ){
		this.owner = owner;
	}
	
	/**
	 * Recursively sets the colors of <code>control</code> to all its children.
	 * @param control the control whose colors should be forwarded
	 */
	protected void setColorAndFont( Control control ){
		// setColorAndFont( control, control.getForeground(), control.getBackground(), JFaceResources.getDialogFont() );
		setColorAndFont( control, control.getForeground(), control.getBackground(), control.getDisplay().getSystemFont() );
	}
	
	/**
	 * Recursively sets colors and fonts to <code>control</code> and
	 * all its children.
	 * @param control some element whose properties are to be set
	 * @param foreground the new foreground color
	 * @param background the new background color
	 * @param font the new font
	 */
	protected void setColorAndFont( Control control, Color foreground, Color background, Font font ) {
		control.setForeground(foreground);
		control.setBackground(background);
		control.setFont(font);

		if (control instanceof Composite) {
			Control[] children= ((Composite) control).getChildren();
			for (int i= 0; i < children.length; i++) {
				setColorAndFont(children[i], foreground, background, font);
			}
		}
	}
	
	public Point computeSizeHint() {
		Point preferedSize= owner.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

		Point constrains= owner.getSizeConstraints();
		if (constrains == null)
			return preferedSize;

		Point constrainedSize= owner.getShell().computeSize(constrains.x, SWT.DEFAULT, true);

		int width= Math.min(preferedSize.x, constrainedSize.x);
		int height= Math.max(preferedSize.y, constrainedSize.y);

		return new Point(width, height);
	}
}
