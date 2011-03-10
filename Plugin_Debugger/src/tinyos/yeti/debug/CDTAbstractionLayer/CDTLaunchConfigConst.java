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
package tinyos.yeti.debug.CDTAbstractionLayer;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;

/**
 * Redefines CDT launch configuration constants. For more information have look at the documentation 
 * of the original CDT constants.
 * @author Silvan Nellen
 *
 */
public interface CDTLaunchConfigConst {
	
	/**
	 * The CDT c project nature. Projects being launched using the CDT must have this nature.
	 */
	public static final String CDT_PROJECT_NATURE = "org.eclipse.cdt.core.cnature";
	
	/**
	 * The standard command factory of the CDT.
	 */
	public static final String CDT_STANDARD_COMMAND_FACTORY = "org.eclipse.cdt.debug.mi.core.standardCommandFactory"; 
	
	/**
	 * The standard protocol used by the CDT
	 */
	public static final String CDT_STANDARD_PROTOCOL = "mi";
	
	/**
	 * The standard main symbol of CDT 
	 */
	public static final String CDT_STANDARD_MAIN_SYMBOL = "main";
	
	/*
	 * The following constants redefine CDT constants
	 */

	public static final String ATTR_DEBUGGER_STOP_AT_MAIN = ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN;

	public static final String ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL = ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL;

	public static final String ATTR_DEBUGGER_START_MODE = ICDTLaunchConfigurationConstants. ATTR_DEBUGGER_START_MODE;

	public static final String DEBUGGER_MODE_RUN = ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN;

	public static final String ATTR_DEBUGGER_ID = ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID;

	public static final String ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING = ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING;

	public static final String ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING = ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING;

	public static final String ATTR_HOST = IGDBServerMILaunchConfigurationConstants.ATTR_HOST;

	public static final String ATTR_PORT = IGDBServerMILaunchConfigurationConstants.ATTR_PORT;

	public static final String ATTR_REMOTE_TCP = IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP;

	public static final String ATTR_DEBUG_NAME = IMILaunchConfigurationConstants.ATTR_DEBUG_NAME;

	public static final String ATTR_GDB_INIT = IMILaunchConfigurationConstants.ATTR_GDB_INIT;

	public static final String ATTR_DEBUGGER_COMMAND_FACTORY = IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY;

	public static final String ATTR_DEBUGGER_PROTOCOL = IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL;

	public static final String ATTR_DEBUGGER_VERBOSE_MODE = IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE;

	public static final String ATTR_DEBUGGER_FULLPATH_BREAKPOINTS = IMILaunchConfigurationConstants.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS;

	public static final String ATTR_DEBUGGER_AUTO_SOLIB = IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB;
	
	public static final String ATTR_PROJECT_NAME = ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME;

	public static final String ATTR_PROGRAM_NAME = ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME;

	public static final String ATTR_USE_TERMINAL = ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL;
	
}
