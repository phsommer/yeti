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

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.CDTAbstractionLayer.CDTLaunchConfigConst;

/**
 * Configure avarice as gdb proxy.
 * @author Silvan Nellen
 *
 */
public class AvariceGdbProxyTab extends AbstractTinyOSDebuggerTab implements IGdbProxyConfigurationTab {

	/**
	 * The id of avarice proxy configurations.
	 */
	public static final String PROXY_CONFIG_ID = ITinyOSDebugLaunchConstants.TINYOS_DBG_LAUNCH_ID + ".avariceConfiguration";

	/**
	 * Launch configuration attribute key. The value defines the version of the JTAG device avarice will connect to.
	 */
	public static final String ATTR_JTAG_VERSION = PROXY_CONFIG_ID + ".jtagVersion"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value defines the device avarice will use to connect
	 * to the JTAG device.
	 */
	public static final String ATTR_JTAG_DEVICE = PROXY_CONFIG_ID + ".jtagDevice"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value defines the bit rate the JTAG device will use to communicate with the 
	 * target device.
	 */
	public static final String ATTR_BITRATE = PROXY_CONFIG_ID + ".bitrate"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value defines if avarice should automatically step over interrupts. 
	 */
	public static final String ATTR_IGNORE_INTERRUPTS = PROXY_CONFIG_ID + ".ignoreInterrupts"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value defines if avarice should set the fuse bytes. 
	 */
	public static final String ATTR_FUSE_BYTES_CHECK = PROXY_CONFIG_ID + ".fuseBytesCheck"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value defines the fuse bytes avarice will set if ATTR_FUSE_BYTES_CHECK is true. 
	 */
	public static final String ATTR_FUSE_BYTES = PROXY_CONFIG_ID + ".fuseBytes"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value defines the port on which avarice listens on for gdb connections.
	 * Use the same value as the CDT so that the port is automatically correct.
	 */
	public static final String ATTR_GDB_SERVER_PORT = CDTLaunchConfigConst.ATTR_PORT; //$NON-NLS-1$

	private Composite connectSettingsParent;
	private Composite deviceSettingsParent;

	private Label jtagVersionConfigurationLabel;
	private Combo jtagVersionConfigurationCombo;

	private Label deviceLabel;
	private Text device;

	private Label bitrateLabel;
	private Text bitrate;

	private Button ignoreInterrupts;

	private Label gdbServerPortLabel;
	private Text gdbServerPort;

	private Button fuseBytesCheck;
	private Text fuseBytes;

	/**
	 * Holds the legal bit rates for a mkI JTAG
	 */
	HashSet<Integer> legalMKIBitrates = new HashSet<Integer>();

	public AvariceGdbProxyTab() {
		super();
		legalMKIBitrates.add(1000);legalMKIBitrates.add(500);legalMKIBitrates.add(250);legalMKIBitrates.add(125);
	}

	@Override
	public String getCommand() {
		String ignoreInterrupt = ignoreInterrupts.getSelection() ? " --ignore-intr" : "";
		String writeFuseBytes = fuseBytesCheck.getSelection() ? " --write-fuses "+fuseBytes.getText() : "";
		return "avarice"
		+ " --"+getJtagVersion()
		+ " --jtag "+device.getText()
		+ " --jtag-bitrate "+bitrate.getText()+"KHz"
		+ " :"+gdbServerPort.getText()
		+ ignoreInterrupt
		+ writeFuseBytes;
	}

	@Override
	public String getID() {
		return PROXY_CONFIG_ID;
	}

	@Override
	public void createControl(Composite parent) {
		createLayout(parent);
		createDeviceSettings(deviceSettingsParent);
		createConnectionSettings(connectSettingsParent);
		createPortSetting(parent);
	}

	private void createDeviceSettings(Composite parent) {
		createBitrate(parent);
		createIgnoreInterrupt(parent);
		createFuseBytes(parent);
	}

	private void createIgnoreInterrupt(Composite parent) {
		ignoreInterrupts = createCheckButton(parent, "Automatically step over interrupts. (experimental)"); //$NON-NLS-1$
		ignoreInterrupts.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});	
	}

	private void createBitrate(Composite parent) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comboComp.setLayoutData(gd);

		bitrateLabel = new Label(comboComp, SWT.NONE);
		bitrateLabel.setText("Bitrate [kHz]:"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 1;
		bitrateLabel.setLayoutData(gd);

		bitrate = new Text(comboComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 40;
		bitrate.setLayoutData(gd);
		bitrate.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updatedBitrateIsValid();
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});	
	}

	private void updatedBitrateIsValid() {
		String error = "Invalid bitrate";
		if(!bitrateIsValid()) {
			setErrorCondition(error);
		} else {
			removeErrorCondition(error);
		}
	}

	private boolean bitrateIsValid() {
		String currentBitrate = bitrate.getText();
		currentBitrate = currentBitrate.trim();
		int br = -1;
		try {
			if(currentBitrate.length() == 0) {
				return false;
			}
			br = Integer.parseInt( currentBitrate );
			if(br < 0)
				return false;
		}
		catch( NumberFormatException e ) {
			return false;
		}

		if(getJtagVersion() == "mkI") {			
			if(!legalMKIBitrates.contains(br)) {
				return false;
			}
		} else {
			if(!(br <= 6400 && br >= 22)) {
				return false;
			}
		}
		return true;
	}

	private void createFuseBytes(Composite parent) {
		Composite fuseParent = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		fuseParent.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fuseParent.setLayoutData(gd);

		fuseBytesCheck = createCheckButton(fuseParent, "Write fuse bytes (6 Hex digits):"); //$NON-NLS-1$
		fuseBytesCheck.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updatedFuseByteCheck();
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				updatedFuseByteCheck();
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});	

		fuseBytes = new Text(fuseParent, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 50;
		fuseBytes.setLayoutData(gd);
		fuseBytes.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updateFuseBytesAreValid();
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

		});	

	}
	private void updatedFuseByteCheck() {
		boolean selection = fuseBytesCheck.getSelection();
		fuseBytes.setEnabled(selection);
		if(selection) {
			updateFuseBytesAreValid();
		}
	}

	private void updateFuseBytesAreValid() {
		String error = "Invalid fuse bytes";
		if(!fuseBytesAreValid()) {
			setErrorCondition(error);
		} else {
			removeErrorCondition(error);
		}

	}

	private boolean fuseBytesAreValid() {
		if(!fuseBytesCheck.getSelection()) {
			return true; // Ignore invalid fuse bits if they are not written.
		}
		if(fuseBytes.getText().length() != 6) {
			return false;
		}
		try {
			int fb = Integer.parseInt( fuseBytes.getText(), 16 );
			return ( fb >= 0x100000 && fb <= 0xFFFFFF );
		}
		catch( NumberFormatException e ) {
			return false;
		}
	}

	private void createConnectionSettings(Composite parent) {
		createJtagVersionCombo(parent);
		createDeviceName(parent);
	}

	private void createDeviceName(Composite parent) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comboComp.setLayoutData(gd);

		deviceLabel = new Label(comboComp, SWT.NONE);
		deviceLabel.setText("Port attached to JTAG box:"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 1;
		deviceLabel.setLayoutData(gd);

		device = new Text(comboComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 150;
		device.setLayoutData(gd);
		device.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				String error = "Invalid JTAG device";
				if(!jtagDeviceIsValid()) {
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
	}

	private boolean jtagDeviceIsValid() {
		if(device.getText().length() > 0) {
			return true;
		}
		return false;
	}

	private void createJtagVersionCombo(Composite parent) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comboComp.setLayoutData(gd);

		jtagVersionConfigurationLabel = new Label(comboComp, SWT.NONE);
		jtagVersionConfigurationLabel.setText("Use JTAG Version"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		jtagVersionConfigurationLabel.setLayoutData(gd);

		jtagVersionConfigurationCombo = new Combo(comboComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		jtagVersionConfigurationCombo.setLayoutData(new GridData());
		jtagVersionConfigurationCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updatedBitrateIsValid(); // Changing the version changes the validity of bit rates
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void loadJtagVersions(String current) {
		String[] supportedVersions = {"mkI","mkII"};
		jtagVersionConfigurationCombo.removeAll();
		int select = -1;
		for (int i = 0; i < supportedVersions.length; i++) {
			jtagVersionConfigurationCombo.add(supportedVersions[i]);
			jtagVersionConfigurationCombo.setData(Integer.toString(i), supportedVersions[i]);
			if (supportedVersions[i].equalsIgnoreCase(current)) {
				select = i;
			}
		}

		if (select != -1) {
			jtagVersionConfigurationCombo.select(select);
			if ( !isInitializing() ) {
				updateLaunchConfigurationDialog();
			}
		}
	}

	protected String getJtagVersion() {
		int selectedIndex = jtagVersionConfigurationCombo.getSelectionIndex();
		return (String)jtagVersionConfigurationCombo.getData(Integer.toString(selectedIndex));
	}

	private void createPortSetting(Composite parent) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comboComp.setLayoutData(gd);

		gdbServerPortLabel = new Label(comboComp, SWT.NONE);
		gdbServerPortLabel.setText("Listen for GDB on port"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 1;
		gdbServerPortLabel.setLayoutData(gd);

		gdbServerPort = new Text(comboComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 40;
		gdbServerPort.setLayoutData(gd);
		gdbServerPort.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				String error = "Invalid server port";
				if(!gdbServerPortIsValid()) {
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
	}

	private boolean gdbServerPortIsValid() {
		try {
			int port = Integer.parseInt( gdbServerPort.getText() );
			return ( port > 0 && port <= 0xFFFF );
		}
		catch( NumberFormatException e ) {
			return false;
		}
	}

	private void createLayout(Composite parent) {
		Composite settingsParent = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		settingsParent.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		settingsParent.setLayoutData(gd);

		Group connectSettingsGroup = new Group(settingsParent, SWT.SHADOW_ETCHED_IN);
		connectSettingsGroup.setText("JTAG Connection Settings"); //$NON-NLS-1$
		GridLayout connectSettingsLayout = new GridLayout();
		connectSettingsLayout.marginHeight = 0;
		connectSettingsLayout.marginWidth = 0;
		connectSettingsLayout.numColumns = 1;
		connectSettingsGroup.setLayout(connectSettingsLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		connectSettingsGroup.setLayoutData(gd);

		Group deviceSettingsGroup = new Group(settingsParent, SWT.SHADOW_ETCHED_IN);
		deviceSettingsGroup.setText("Device Settings"); //$NON-NLS-1$
		GridLayout deviceSettingsLayout = new GridLayout();
		deviceSettingsLayout.marginHeight = 0;
		deviceSettingsLayout.marginWidth = 0;
		deviceSettingsLayout.numColumns = 1;
		deviceSettingsGroup.setLayout(deviceSettingsLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		deviceSettingsGroup.setLayoutData(gd);

		connectSettingsParent = connectSettingsGroup;
		deviceSettingsParent = deviceSettingsGroup;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if(super.isValid(launchConfig)) {
			if(!gdbServerPortIsValid()) {
				return false;
			}
			if(!jtagDeviceIsValid()) {
				return false;
			}
			if(!fuseBytesAreValid()) {
				return false;
			}
			if(!bitrateIsValid()) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return "Avarice";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);

		String dev = "";
		String br = "";
		boolean ii = true;
		String gsp = "";
		String jv = "";
		boolean wfb = false;
		String fb = "";
		try {
			dev = configuration.getAttribute(ATTR_JTAG_DEVICE, "/dev/avrjtag");
			br = configuration.getAttribute(ATTR_BITRATE,"1000");
			ii = configuration.getAttribute(ATTR_IGNORE_INTERRUPTS,true);
			gsp = configuration.getAttribute(ATTR_GDB_SERVER_PORT,"4242");
			jv = configuration.getAttribute(ATTR_JTAG_VERSION, "mkI");
			wfb = configuration.getAttribute(ATTR_FUSE_BYTES_CHECK , false);
			fb = configuration.getAttribute(ATTR_FUSE_BYTES, "ff19c2");
			loadJtagVersions(jv);
		} catch (CoreException ce) {
			TinyOSDebugPlugin.getDefault().log("Exception while initializing avarice proxy tab", ce);
		}
		device.setText(dev);
		bitrate.setText(br);
		ignoreInterrupts.setSelection(ii);
		gdbServerPort.setText(gsp);
		fuseBytesCheck.setSelection(wfb);
		updatedFuseByteCheck();
		fuseBytes.setText(fb);

		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if(isDirty()) {
			configuration.setAttribute(ATTR_JTAG_DEVICE, device.getText().trim()); 
			configuration.setAttribute(ATTR_JTAG_VERSION, getJtagVersion());
			configuration.setAttribute(ATTR_BITRATE, bitrate.getText().trim());
			configuration.setAttribute(ATTR_IGNORE_INTERRUPTS , ignoreInterrupts.getSelection());
			configuration.setAttribute(ATTR_GDB_SERVER_PORT, gdbServerPort.getText());
			configuration.setAttribute(ATTR_FUSE_BYTES_CHECK , fuseBytesCheck.getSelection());
			configuration.setAttribute(ATTR_FUSE_BYTES, fuseBytes.getText().trim());
			setDirty(false);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_JTAG_DEVICE, "/dev/avrjtag");
		configuration.setAttribute(ATTR_JTAG_VERSION, "mkI");
		configuration.setAttribute(ATTR_BITRATE, "1000");
		configuration.setAttribute(ATTR_IGNORE_INTERRUPTS , true);
		configuration.setAttribute(ATTR_GDB_SERVER_PORT, "4242");
		configuration.setAttribute(ATTR_FUSE_BYTES_CHECK , false);
		configuration.setAttribute(ATTR_FUSE_BYTES, "ff19c2");
	}
}
