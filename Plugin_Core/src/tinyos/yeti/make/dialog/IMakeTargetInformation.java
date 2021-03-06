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
package tinyos.yeti.make.dialog;

import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.IMakeTarget;

/**
 * Contains additional information for the {@link IMakeTarget} that gets
 * modified by a {@link MakeTargetDialog} and its pages.
 * @author Benjamin Sigg
 */
public interface IMakeTargetInformation {
	/**
	 * Gets a list of all available platforms.
	 * @return the list of platforms
	 */
	public IPlatform[] getPlatforms();
	
	/**
	 * Gets the platform which is currently selected.
	 * @return the selected platform or <code>null</code>
	 */
	public IPlatform getSelectedPlatform();
	
	/**
	 * Gets the environment in which the settings have to be valid.
	 * @return the environment
	 */
	public IEnvironment getEnvironment();
}
