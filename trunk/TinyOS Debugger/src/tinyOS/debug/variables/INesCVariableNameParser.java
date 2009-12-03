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


public interface INesCVariableNameParser {

	/**
	 * Get the separator this parser expects to find in the names of nested c variables
	 * @return separator
	 */
	public String getSeparator();
	
	/**
	 * Extract the module name from the name of the C variable.
	 * @param varName
	 * @return The module name, null if varName == null
	 */
	public String getModuleName(String varName);
	
	/**
	 * Extract the variable name from the name of the C variable.
	 * @param varName
	 * @return The variable name, null if varName == null
	 */
	public String getVariableName(String varName);
	
	/**
	 * Returns true if the name of the variable indicates that it might be the name of a nested c variable.
	 * @param varName
	 * @return true if the name of the variable indicates that it might be the name of a nested c variable. false if it is not a nested c variable or varName == null
	 */
	public boolean isNesCVariable(String varName);
	
}
