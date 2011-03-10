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
package tinyos.yeti.debug.launch.configuration;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;

/**
 * Abstract base class for TinyOS Debugger tabs. Provides a mechanism to store multiple error messages and
 * a mechanism for detecting if the tab is initializing.
 * @author Silvan Nellen
 *
 */
public abstract class AbstractTinyOSDebuggerTab extends AbstractLaunchConfigurationTab {

	/**
	 * Holds the error conditions (error messages) for this tab.
	 */
	Set<String> errorConditions = new HashSet<String>();

	/**
	 * Indicates whether this tab is initializing
	 */
	private boolean isInitializing;
	
	protected boolean isInitializing() {
		return isInitializing ;
	}

	protected void setInitializing(boolean i) {
		isInitializing = i;
	}
	
	protected void updateLaunchConfigurationDialog() {
		if(!isInitializing()) {
			super.updateLaunchConfigurationDialog();
		}
	}
	
	/**
	 * Adds the given error message to the set of error messages.
	 * @param errorCondition
	 */
	protected void setErrorCondition(String errorCondition) {
		errorConditions.add(errorCondition);
	}
	
	/**
	 * Removes the given error message form the set of error messages.
	 * @param errorCondition
	 */
	protected void removeErrorCondition(String errorCondition) {
		errorConditions.remove(errorCondition);
	}

	/**
	 * Get one of the error messages (Error messages are returned in no particular order).
	 * @return
	 */
	public String getErrorCondition() {
		String errorCond = null;
		if(errorConditions.size() > 0) {
			errorCond = "["+getName()+"]: "+errorConditions.iterator().next();
		}
		return errorCond;
	}
	
	/**
	 * Returns one of the error messages set using setErrorCondition
	 */
	@Override
	public String getErrorMessage() {
		return getErrorCondition();
	}
	
	public boolean isDirty() {
		return super.isDirty();
	}
}
