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
package tinyOS.debug.launch.configuration;

import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Defines the interface for the configuration of gdb proxy commands (such as avarice).
 * @author Silvan Nellen
 *
 */
public interface IGdbProxyConfigurationTab extends ILaunchConfigurationTab{
	/**
	 * Get the configured command as string.
	 * @return
	 */
	public abstract String getCommand();
	
	/**
	 * Get the id of this configuration. Must be unique within the plugin.
	 * @return
	 */
	public abstract String getID();

	/**
	 * Gets the current error message of this tab
	 * @return the error message
	 */
	public abstract String getErrorCondition();

	/**
	 * Tells whether there are unsaved elements on this tab
	 * @return <code>true</code> if there are unsaved elements
	 */
	public abstract boolean isDirty();
}
