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
package tinyos.yeti.ep.parser;

import org.eclipse.jface.text.TextPresentation;

import tinyos.yeti.utility.Icon;

/**
 * Information to be shown in a hover.
 * @author Benjamin Sigg
 */
public interface IHoverInformation{
	/**
	 * The title of this information
	 * @return the title or <code>null</code>
	 */
	public String getTitle();
	
	/**
	 * An icon for this information.
	 * @return the tags for an icon or <code>null</code>
	 */
	public Icon getIcon();
	
	/**
	 * Description of this information
	 * @return description or <code>null</code>
	 */
	public String getContent();
	
	/**
	 * Formatting instructions for {@link #getContent() the content}
	 * @return instructions or <code>null</code>
	 */
	public TextPresentation getContentPresentation();
	
	/**
	 * Gets the text of this hover information in html format.
	 * @return the html or <code>null</code>
	 */
	public String getHTML();
}
