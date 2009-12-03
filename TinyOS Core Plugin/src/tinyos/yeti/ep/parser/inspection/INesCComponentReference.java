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
package tinyos.yeti.ep.parser.inspection;

public interface INesCComponentReference extends INesCNode{
	/**
	 * Gets the name of the component this reference points to.
	 * @return the name of the referenced component
	 */
	public String getReferencedComponentName();
	
	/**
	 * Gets the name of the component as it is visible in the component.
	 * @return the renamed component name
	 */
	public String getVisibleComponentName();
	
	/**
	 * Gets the component this reference points to.
	 * @return the component, maybe <code>null</code>
	 */
	public INesCComponent getReference();
	
	/**
	 * Gets the raw form of the component this reference points to.
	 * @return the raw component, maybe <code>null</code>
	 */
	public INesCComponent getRawReference();
}
