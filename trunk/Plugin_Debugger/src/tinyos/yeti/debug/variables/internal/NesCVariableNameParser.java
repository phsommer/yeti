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
package tinyos.yeti.debug.variables.internal;

import tinyos.yeti.debug.variables.INesCSeparatorProvider;
import tinyos.yeti.debug.variables.INesCVariableNameParser;
import tinyos.yeti.ep.IPlatform;

public class NesCVariableNameParser implements INesCVariableNameParser {

	public NesCVariableNameParser(INesCSeparatorProvider sp) {
		super();
		if(sp != null) {
			String sep = sp.getSeparator();
			if(sep != null){
				m_separator = sep;
			}
		}
	}

	@Override
	public String getSeparator() {
		return m_separator;
	}

	@Override
	public String getVariableName(String varName) {
		if(varName != null) {
			int varIndex = varName.lastIndexOf(getSeparator());
			if(varIndex > 0 ) {
				return varName.substring(varIndex+getSeparator().length());
			} else {
				return varName;
			}
		}
		return null;
	}

	@Override
	public String getModuleName(String varName) {
		if(varName != null) {
			String separator = getSeparator();
			int varIndex = varName.lastIndexOf(separator);
			if(varIndex > 0) {
				String modName = varName.substring(0, varIndex);
				int instIndex = modName.lastIndexOf(getSeparator());
				if(instIndex > 0) {
					return modName.substring(0,instIndex) + " (" +modName.substring(instIndex+separator.length()) + ")";
				} else {
					return modName; 	
				} 
			} else {
				return varName;
			}
		}
		return null;
	}

	@Override
	public boolean isNesCVariable(String varName) {
		if(varName != null) {
			// TODO: More elaborate checks.
			if(varName.indexOf(getSeparator()) == 0) { 
				// ignore variables starting with a separator
				return false;
			}
			return varName.contains(getSeparator());
		}
		return false;
	}

	private String m_separator = IPlatform.DEFAULT_NESC_SEPARATOR;
}
