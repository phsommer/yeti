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
package tinyOS.debug.variables;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IVariable;

/**
 * Interface for classes that are able to extract and add variables to a debug target.
 */
public interface IVariableManager {

	/**
	 * Get all registered variables from a target using the adaptable interface.
	 * @param adaptable The object to attempt to read the variables from.
	 * @return The extracted variables or null if extraction was not possible
	 */
	public IVariable[] getRegisteredVariables(IAdaptable adaptable);
	
	/**
	 * Get descriptors of all registered variables from a target using the adaptable interface.
	 * @param adaptable The object to attempt to read the variables from.
	 * @return The extracted variable descriptors or null if extraction was not possible
	 */
	public IVariableDescriptor[] getRegisteredVariableDescriptors(IAdaptable adaptable);
	
	/**
	 * Get descriptors of all available variables (that is all static variables defined in the binary of the target) from a target using the adaptable interface.
	 * @param adaptable The object to attempt to read the variables from.
	 * @return The extracted variable descriptors or null if extraction was not possible
	 */
	public IVariableDescriptor[] getAvailableVariableDescriptors(IAdaptable adaptable);
	
	/**
	 * Add the variables described by the IVariableDescriptor[] to the adaptable interface (if possible)
	 * @param vars The descriptions of the variables to add.
	 * @param adaptable The object to attempt to add the variables to.
	 * @return true if registration succeeded, false otherwise.
	 */
	public boolean registerVariables(IVariableDescriptor[] vars, IAdaptable adaptable);
}
