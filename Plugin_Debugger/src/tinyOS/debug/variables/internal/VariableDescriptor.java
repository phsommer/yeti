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
package tinyOS.debug.variables.internal;

import org.eclipse.core.runtime.IPath;

import tinyOS.debug.variables.IVariableDescriptor;

public class VariableDescriptor implements IVariableDescriptor {

	public VariableDescriptor(String name, IPath path) {
		super();
		this.m_name = name;
		this.m_path = path;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public IPath getPath() {
		return m_path;
	}
	
	private String m_name;
	private IPath m_path;

}
