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
package tinyos.yeti.editors.nesc.util;

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;


/**
 * Manages SWT color objects for the given color keys and
 * given <code>RGB</code> objects. Until the <code>dispose</code> 
 * method is called, the same color object is returned for
 * equal keys and equal <code>RGB</code> values.
 * <p>
 * In order to provide backward compatibility for clients of <code>IColorManager</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jdt.ui.text.IColorManagerExtension} since version 2.0 introducing 
 * 		the ability to bind and un-bind colors.</li>
 * </ul>
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see org.eclipse.jdt.ui.text.IColorManagerExtension
 * @see org.eclipse.jdt.ui.text.IJavaColorConstants
 */
public interface IColorManager extends ISharedTextColors {
	
	/**
	 * Returns a color object for the given key. The color objects 
	 * are remembered internally; the same color object is returned 
	 * for equal keys.
	 *
	 * @param key the color key
	 * @return the color object for the given key
	 */
	Color getColor(String key);
}
