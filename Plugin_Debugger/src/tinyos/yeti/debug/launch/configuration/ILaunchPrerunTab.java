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

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Allows to select a {@link ILaunchPrerun} which will be run before
 * the debugging session starts.
 * @author Benjamin Sigg
 */
public interface ILaunchPrerunTab{
	public void setHandle( ILaunchPrerunTabHandle handle );
	
	public void setProject( IProject project );
	
	public Control getControl( Composite parent );
	
	public void read( ILaunchConfiguration configuration );
	
	public void apply( ILaunchConfigurationWorkingCopy configuration );
	
	public String getName();
	
	public String getErrorMessage();
}
