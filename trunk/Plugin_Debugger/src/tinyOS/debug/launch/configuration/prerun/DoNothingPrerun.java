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
package tinyOS.debug.launch.configuration.prerun;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyOS.debug.launch.configuration.ILaunchPrerun;
import tinyOS.debug.launch.configuration.ILaunchPrerunTab;
import tinyOS.debug.launch.configuration.ILaunchPrerunTabHandle;
import tinyos.yeti.make.IMakeTarget;

public class DoNothingPrerun implements ILaunchPrerun, ILaunchPrerunTab{
	@Override
	public void setHandle( ILaunchPrerunTabHandle handle ){
		// ignore
	}
	
	@Override
	public void setProject( IProject project ){
		// ignore	
	}
	
	@Override
	public String getErrorMessage(){
		return null;
	}
	
	@Override
	public IMakeTarget getMakeTarget( ILaunchConfiguration configuration, IProject project ){
		return null;
	}
	
	@Override
	public void apply( ILaunchConfigurationWorkingCopy configuration ){
		// ignore
	}

	@Override
	public Control getControl( Composite parent ){
		return new Composite( parent, SWT.NONE );
	}

	@Override
	public String getName(){
		return "do nothing";
	}

	@Override
	public void read( ILaunchConfiguration configuration ){
		// ignore	
	}
}
