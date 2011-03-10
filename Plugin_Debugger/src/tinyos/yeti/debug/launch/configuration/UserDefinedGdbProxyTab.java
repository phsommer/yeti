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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.debug.TinyOSDebugPlugin;

/**
 * A gdb proxy which starts a user defined command.
 * @author Silvan Nellen
 *
 */
public class UserDefinedGdbProxyTab  extends AbstractTinyOSDebuggerTab implements IGdbProxyConfigurationTab  {

	/**
	 * Launch configuration attribute key. The value is a user defined command that launches a 
	 * Proxy (such as avarice) between GDB and a tinyOS debug target.
	 */
	public static final String ATTR_GDB_PROXY_COMMAND = ITinyOSDebugLaunchConstants.TINYOS_DBG_LAUNCH_ID + ".userDefinedGdbProxy.gdbProxyCommand"; //$NON-NLS-1$

	// Gdb proxy UI widgets
	protected Label proxyCommandLabel;
	protected Text proxyCommand;

	@Override
	public String getCommand() {
		return proxyCommand.getText();
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);

		proxyCommandLabel = new Label(parent, SWT.NONE);
		proxyCommandLabel.setText("GDB Proxy Command:"); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		proxyCommandLabel.setLayoutData(gd);

		proxyCommand = new Text(parent, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		proxyCommand.setLayoutData(gd);
		proxyCommand.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				String error = "Invalid command";
				if(!proxyCommandIsValid()) {
					setErrorCondition(error);
				} else {
					removeErrorCondition(error);
				}
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});

		createVerticalSpacer(comp, 1);
	}

	protected boolean proxyCommandIsValid() {
		if(proxyCommand.getText().length() == 0) {
			return false;
		}
		return true;
	}

	@Override
	public String getName() {
		return "User defined command";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);

		String proxyCmd = "";
		try {
			proxyCmd = configuration.getAttribute(ATTR_GDB_PROXY_COMMAND, "");
		} catch (CoreException ce) {
			TinyOSDebugPlugin.getDefault().log("Exception while initializing user defined command proxy tab", ce);
		}
		proxyCommand.setText(proxyCmd);

		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if(isDirty()) {
			configuration.setAttribute(ATTR_GDB_PROXY_COMMAND, proxyCommand.getText());
			setDirty(false);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_GDB_PROXY_COMMAND, "");
	}

	@Override
	public String getID() {
		return ITinyOSDebugLaunchConstants.TINYOS_DBG_LAUNCH_ID+".userDefinedGdbProxy";
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if(super.isValid(launchConfig)) {
			if(!proxyCommandIsValid()) {
				return false;
			}
			return true;
		}
		return false;
	}

}
