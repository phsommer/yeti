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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * A gdb proxy which starts no command.
 * @author Silvan Nellen
 *
 */
public class NoGdbProxyTab  extends AbstractTinyOSDebuggerTab implements IGdbProxyConfigurationTab  {

	protected Label description;
	
	@Override
	public String getCommand() {
		return "";
	}

	@Override
	public String getID() {
		return ITinyOSDebugLaunchConstants.TINYOS_DBG_LAUNCH_ID+".noGdbProxy";
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		
		description = new Label(parent, SWT.NONE);
		description.setText("Don't use a GDB proxy."); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		description.setLayoutData(gd);
	}

	@Override
	public String getName() {
		return "None";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		// Nothing to do
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// nothing to do
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// nothing to do
	}

}
