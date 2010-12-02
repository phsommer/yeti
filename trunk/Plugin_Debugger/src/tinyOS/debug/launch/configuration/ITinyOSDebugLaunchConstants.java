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

/**
 * Defines comman TinyOS debug launch configuration constants.
 * @author Silvan Nellen
 *
 */
public interface ITinyOSDebugLaunchConstants {
	/**
	 * Prefix for all launch constants
	 */
	public static final String TINYOS_DBG_LAUNCH_ID = "tinyOS.debug.launch"; //$NON-NLS-1$

	/**
	 * This is the launch type id.
	 */
	public static final String ID_LAUNCH_TINYOS_DEBUG = "tinyOS.debug.launch.configurationType"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute key. The value is a command that launches a 
	 * Proxy (such as avarice) between GDB and a tinyOS debug target.
	 */
	public static final String ATTR_GDB_PROXY_COMMAND = TINYOS_DBG_LAUNCH_ID + ".gdbProxyCommand"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute key. The launch will be delayed by the given value (in milliseconds) in order
	 * to wait for the GDB proxy program to start up.
	 */
	public static final String ATTR_GDB_PROXY_STARTUP_DELAY = TINYOS_DBG_LAUNCH_ID + ".gdbProxyStartupDelay"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute key. The value indicates the currently configured gdb proxy.
	 */
	public static final String ATTR_CURRENT_GDB_PROXY = TINYOS_DBG_LAUNCH_ID + ".currentGdbProxy"; //$NON-NLS-1$
	
	/**
	 * Which launch configuration to run before the deubg session starts.
	 */
	public static final String ATTR_CURRENT_LAUNCH_PRERUN = TINYOS_DBG_LAUNCH_ID + ".currentLaunchPrerun";
}
