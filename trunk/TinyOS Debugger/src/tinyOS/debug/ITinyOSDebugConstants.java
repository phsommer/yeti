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
package tinyOS.debug;

public interface ITinyOSDebugConstants {
	public static final String PLUGIN_ID = TinyOSDebugPlugin.getUniqueIdentifier();
	
	public static final String PREFIX = PLUGIN_ID + ".";
	
	// action ids
	public static final String ACTION_TOGGLE_BREAKPOINT = PREFIX + "toggleBreakpoint"; //$NON-NLS-1$
	
	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 150;
	
	// TODO: Move to a more sensible location
	final String BREAKPOINT_SOURCE_HANDLE_ATTRIBUTE="TinyOS.debug.breakpoints.attributes.sourcehandle";

}
