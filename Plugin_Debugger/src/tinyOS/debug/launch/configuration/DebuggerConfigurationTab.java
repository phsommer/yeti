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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tinyOS.debug.NesCDebugIcons;
import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.CDTAbstractionLayer.CDTLaunchConfigConst;
/**
 * Configures the CDT debugger.
 * @author Silvan Nellen
 *
 */
public class DebuggerConfigurationTab extends AbstractTinyOSDebuggerTab {

	public class AdvancedDebuggerOptionsDialog extends Dialog {

		private Button fVarBookKeeping;

		private Button fRegBookKeeping;

		/**
		 * Constructor for AdvancedDebuggerOptionsDialog.
		 */
		protected AdvancedDebuggerOptionsDialog(Shell parentShell) {
			super(parentShell);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite)super.createDialogArea(parent);
			Group group = new Group(composite, SWT.NONE);
			group.setText("Automatically track the values of"); //$NON-NLS-1$
			GridLayout layout = new GridLayout();
			group.setLayout(layout);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fVarBookKeeping = new Button(group, SWT.CHECK);
			fVarBookKeeping.setText("Variables"); //$NON-NLS-1$
			fRegBookKeeping = new Button(group, SWT.CHECK);
			fRegBookKeeping.setText("Registers"); //$NON-NLS-1$
			initialize();
			return composite;
		}

		protected void okPressed() {
			saveValues();
			super.okPressed();
		}

		private void initialize() {
			Map<String, Boolean> attr = getAdvancedAttributes();
			Object varBookkeeping = attr.get(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING);
			fVarBookKeeping.setSelection( (varBookkeeping instanceof Boolean) ? !((Boolean)varBookkeeping).booleanValue() : true);
			Object regBookkeeping = attr.get(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING);
			fRegBookKeeping.setSelection( (regBookkeeping instanceof Boolean) ? !((Boolean)regBookkeeping).booleanValue() : true);
		}

		private void saveValues() {
			Map<String, Boolean> attr = getAdvancedAttributes();
			Boolean varBookkeeping = Boolean.valueOf( !fVarBookKeeping.getSelection() );
			attr.put(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, varBookkeeping);
			Boolean regBookkeeping = Boolean.valueOf( !fRegBookKeeping.getSelection() );
			attr.put(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, regBookkeeping);
			if (!isInitializing()) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Advanced Options"); //$NON-NLS-1$
		}
	}

	private void initializeAdvancedAttributes(ILaunchConfiguration config) {
		Map<String, Boolean> attr = getAdvancedAttributes();
		try {
			Boolean varBookkeeping = (config.getAttribute(
					CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false))
					? Boolean.TRUE
							: Boolean.FALSE;
			attr.put(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, varBookkeeping);
		} catch (CoreException e) {
		}
		try {
			Boolean regBookkeeping = (config.getAttribute(
					CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, false))
					? Boolean.TRUE
							: Boolean.FALSE;
			attr.put(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, regBookkeeping);
		} catch (CoreException e) {
		}
	}

	private void applyAdvancedAttributes(ILaunchConfigurationWorkingCopy config) {
		Map<String, Boolean> attr = getAdvancedAttributes();
		Object varBookkeeping = attr.get(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING);
		if (varBookkeeping instanceof Boolean)
			config.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING,
					((Boolean)varBookkeeping).booleanValue());
		Object regBookkeeping = attr.get(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING);
		if (regBookkeeping instanceof Boolean)
			config.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING,
					((Boolean)regBookkeeping).booleanValue());
	}

	private Map<String, Boolean> fAdvancedAttributes = new HashMap<String, Boolean>(5);

	public Map<String, Boolean> getAdvancedAttributes() {
		return fAdvancedAttributes;
	}

	private Composite mainConfigurationHolder;
	private Button stopInMainCheck;
	private Text stopInMainSymbol;
	private Button advancedButton;
	private Text gdbCommandText;
	private Text gdbInitText;
	private Combo commandFactoryCombo;
	private Combo protocolCombo;
	private Button verboseModeButton;
	private Button breakpointsFullPath;
	private Group connectionHolder;
	private Text hostname;
	private Text port;

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createStopAtMain(comp);
		createMainHolder(comp);
		createMain(mainConfigurationHolder);
		createConnectionHolder(comp);
		createConnection(connectionHolder);
	}

	protected void createStopAtMain(Composite parent) {
		Composite optionsComp = new Composite(parent, SWT.NONE);
		int numberOfColumns =  3;
		GridLayout layout = new GridLayout( numberOfColumns, false );
		optionsComp.setLayout( layout );
		optionsComp.setLayoutData( new GridData( GridData.BEGINNING, GridData.CENTER, true, false, 1, 1 ) );
		stopInMainCheck = createCheckButton( optionsComp, "Stop on startup at:" ); //$NON-NLS-1$
		stopInMainCheck.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				stopInMainSymbol.setEnabled(stopInMainCheck.getSelection());
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});
		stopInMainSymbol = new Text(optionsComp, SWT.SINGLE | SWT.BORDER);
		final GridData gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gridData.widthHint = 100;
		stopInMainSymbol.setLayoutData(gridData);
		stopInMainSymbol.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				String error = "Invalid stop symbol";
				if(!stopSymbolIsValid()) {
					setErrorCondition(error);
				} else {
					removeErrorCondition(error);
				}
				if(!isInitializing()) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});
		stopInMainSymbol.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {                       
					public void getName(AccessibleEvent e) {
						e.result = "Stop on startup at:"; //$NON-NLS-1$
					}
				}
		);

		advancedButton = createPushButton(optionsComp, "Advanced...", null); //$NON-NLS-1$
		((GridData)advancedButton.getLayoutData()).horizontalAlignment = GridData.END;
		advancedButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Dialog dialog = new AdvancedDebuggerOptionsDialog(getShell());
				dialog.open();
			}
		});
	}


	private boolean stopSymbolIsValid() {
		if(stopInMainSymbol.getText().length() != 0) {
			return true;
		}
		return false;
	}

	protected void createMainHolder(Composite parent) {
		Group mainGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		mainGroup.setText("Debugger Options"); //$NON-NLS-1$
		mainConfigurationHolder = mainGroup;
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		mainConfigurationHolder.setLayout(tabHolderLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		mainConfigurationHolder.setLayoutData(gd);
	}

	protected void createMain(Composite parent) {
		Composite gdbCommandComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		gdbCommandComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gdbCommandComp.setLayoutData(gd);

		Label label = new Label( gdbCommandComp, SWT.NONE ); //$NON-NLS-1$
		label.setText("GDB debugger:");
		gd = new GridData();
		label.setLayoutData( gd );
		gdbCommandText = new Text( gdbCommandComp, SWT.SINGLE | SWT.BORDER );
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gdbCommandText.setLayoutData(gd);
		gdbCommandText.addModifyListener( new ModifyListener() {

			public void modifyText( ModifyEvent evt ) {
				String error = "Invalid GDB debugger";
				if(!gdbDebuggerIsValid()) {
					setErrorCondition(error);
				} else {
					removeErrorCondition(error);
				}
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		} );

		Button button = createPushButton( gdbCommandComp, "Browse...", null ); //$NON-NLS-1$
		button.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent evt ) {
				handleGDBButtonSelected();
				if(!isInitializing()) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

			private void handleGDBButtonSelected() {
				FileDialog dialog = new FileDialog( getShell(), SWT.NONE );
				dialog.setText( "Browser for GDB Command" ); //$NON-NLS-1$
				String gdbCommand = gdbCommandText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf( File.separator );
				if ( lastSeparatorIndex != -1 ) {
					dialog.setFilterPath( gdbCommand.substring( 0, lastSeparatorIndex ) );
				}
				String res = dialog.open();
				if ( res == null ) {
					return;
				}
				gdbCommandText.setText( res );
			}
		} );

		label = new Label( gdbCommandComp, SWT.NONE); //$NON-NLS-1$
		label.setText("GDB command file:");
		gd = new GridData();
		//		gd.horizontalSpan = 2;
		label.setLayoutData( gd );
		gdbInitText = new Text( gdbCommandComp, SWT.SINGLE | SWT.BORDER );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gdbInitText.setLayoutData( gd );
		gdbInitText.addModifyListener( new ModifyListener() {

			public void modifyText( ModifyEvent evt ) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		} );
		button = createPushButton( gdbCommandComp, "Browse...", null ); //$NON-NLS-1$
		button.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent evt ) {
				handleGDBInitButtonSelected();
				if(!isInitializing()) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

			private void handleGDBInitButtonSelected() {
				FileDialog dialog = new FileDialog( getShell(), SWT.NONE );
				dialog.setText( "Browse for GDB Init file" ); //$NON-NLS-1$
				String gdbCommand = gdbInitText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf( File.separator );
				if ( lastSeparatorIndex != -1 ) {
					dialog.setFilterPath( gdbCommand.substring( 0, lastSeparatorIndex ) );
				}
				String res = dialog.open();
				if ( res == null ) {
					return;
				}
				gdbInitText.setText( res );
			}
		} );

		label = new Label( gdbCommandComp, SWT.WRAP );
		label.setText("Warning: Some commands in this file may interfere with the startup operation of the debugger, for example \"run\"");
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.heightHint = SWT.DEFAULT;
		gd.horizontalSpan = 3;
		label.setLayoutData( gd );

		Composite options = new Composite(gdbCommandComp, SWT.NULL);	
		options.setLayout(new GridLayout(2, true));
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		options.setLayoutData( gd );
		createCommandFactoryCombo(options);
		createProtocolCombo( options );
		options = new Composite(gdbCommandComp, SWT.NULL);	
		options.setLayout(new GridLayout(1, true));
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		options.setLayoutData( gd );
		createVerboseModeButton( options );
		createBreakpointFullPathName(options);
	}

	private boolean gdbDebuggerIsValid() {
		if(this.gdbCommandText.getText().length() != 0)
			return true;
		return false;
	}

	private void createBreakpointFullPathName(Composite parent) {
		breakpointsFullPath = createCheckButton( parent, "Use full file path to set breakpoints" ); //$NON-NLS-1$

		breakpointsFullPath.addSelectionListener( new SelectionListener() {

			public void widgetDefaultSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

			public void widgetSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		} );
	}

	private void createVerboseModeButton(Composite parent) {
		verboseModeButton = createCheckButton( parent, "Verbose console mode" ); //$NON-NLS-1$
		verboseModeButton.addSelectionListener( new SelectionListener() {

			public void widgetDefaultSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

			public void widgetSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		} );
	}

	private void createProtocolCombo(Composite parent) {
		Label label = new Label( parent, SWT.NONE );
		label.setText( "Protocol:" ); //$NON-NLS-1$
		protocolCombo = new Combo( parent, SWT.READ_ONLY | SWT.DROP_DOWN );
		protocolCombo.addSelectionListener( new SelectionListener() {

			public void widgetDefaultSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

			public void widgetSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		} );
	}

	private void loadProtocolComboBox(ILaunchConfiguration configuration, String id) {
		String[] protocolNames = {"mi","mi1","mi2"};

		protocolCombo.removeAll();
		int select = 0;
		for (int i = 0; i < protocolNames.length; i++) {
			protocolCombo.add(protocolNames[i]);
			protocolCombo.setData(Integer.toString(i), protocolNames[i]);
			if (protocolNames[i].equalsIgnoreCase(id)) {
				select = i;
			}
		}

		if (select > 0) {
			protocolCombo.select(select);
			handleFactoryChanged();
		} else {
			protocolCombo.select( select );
		}

	}

	protected void createCommandFactoryCombo( Composite parent ) {
		Label label = new Label( parent, SWT.NONE );
		label.setText( "GDB command set:" ); //$NON-NLS-1$
		commandFactoryCombo = new Combo( parent, SWT.READ_ONLY | SWT.DROP_DOWN );
		commandFactoryCombo.addSelectionListener( new SelectionListener() {

			public void widgetDefaultSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}

			public void widgetSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		} );
	}

	private void loadCommandFactoryComboBox(ILaunchConfiguration configuration, String id) {
		String[] factoryNames = {"Standard"};
		String[] factoryIds = {CDTLaunchConfigConst.CDT_STANDARD_COMMAND_FACTORY};

		commandFactoryCombo.removeAll();
		int select = 0;
		for (int i = 0; i < factoryNames.length; i++) {
			commandFactoryCombo.add(factoryNames[i]);
			commandFactoryCombo.setData(Integer.toString(i), factoryIds[i]);
			if (factoryIds[i].equalsIgnoreCase(id)) {
				select = i;
			}
		}

		if (select > 0) {
			commandFactoryCombo.select(select);
			handleFactoryChanged();
		} else {
			commandFactoryCombo.select( select );
		}

	}

	private void handleFactoryChanged() {
		if ( !isInitializing() ) {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}
	}

	private void createConnectionHolder(Composite parent) {
		Group mainGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		mainGroup.setText("Connection Options"); //$NON-NLS-1$
		connectionHolder = mainGroup;
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		connectionHolder.setLayout(tabHolderLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		connectionHolder.setLayoutData(gd);
	}

	private void createConnection(Composite parent) {
		Composite connectionComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		connectionComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		connectionComp.setLayoutData(gd);

		Label label = new Label( connectionComp, SWT.WRAP );
		label.setText("GDB will try to connect to this host:port to communicate with the target:");
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.heightHint = SWT.DEFAULT;
		gd.horizontalSpan = 2;
		label.setLayoutData( gd );

		label = new Label( connectionComp, SWT.NONE); //$NON-NLS-1$
		label.setText("Host name or IP address:");
		gd = new GridData();
		label.setLayoutData( gd );

		hostname = new Text( connectionComp, SWT.SINGLE | SWT.BORDER );
		gd = new GridData( );
		gd.widthHint = 100;
		hostname.setLayoutData( gd );
		hostname.addModifyListener( new ModifyListener() {

			public void modifyText( ModifyEvent evt ) {
				String error = "Invalid hostname";
				if(!hostIsValid()) {
					setErrorCondition(error);
				} else {
					removeErrorCondition(error);
				}
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		} );

		label = new Label( connectionComp, SWT.NONE); //$NON-NLS-1$
		label.setText("Port:");
		gd = new GridData();
		label.setLayoutData( gd );

		port = new Text( connectionComp, SWT.SINGLE | SWT.BORDER );
		gd = new GridData( );
		gd.widthHint = 50;
		port.setLayoutData( gd );
		port.addModifyListener( new ModifyListener() {

			public void modifyText( ModifyEvent evt ) {
				String error = "Invalid port";
				if(!portIsValid()) {
					setErrorCondition(error);
				} else {
					removeErrorCondition(error);
				}
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		} );

	}

	private boolean portIsValid() {
		try {
			int p = Integer.parseInt( port.getText() );
			return ( p > 0 && p <= 0xFFFF );
		}
		catch( NumberFormatException e ) {
			return false;
		}
	}

	private boolean hostIsValid() {
		if(hostname.getText().length() == 0) {
			return false;
		}
		return true;
	}

	@Override
	public String getName() {
		return "Debugger";
	}

	@Override
	public Image getImage() {
		return NesCDebugIcons.get(NesCDebugIcons.ICON_CDT_DEBBUGER_TAB);
	}

	protected String getCommandFactory() {
		if(commandFactoryCombo != null) {
			int selectedIndex = commandFactoryCombo.getSelectionIndex();
			return (String)commandFactoryCombo.getData(Integer.toString(selectedIndex));
		}
		return null;
	}
	protected String getProtocol() {
		if(protocolCombo != null) {
			int selectedIndex = protocolCombo.getSelectionIndex();
			return (String)protocolCombo.getData(Integer.toString(selectedIndex));
		}
		return null;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if(super.isValid(launchConfig)) {
			if(!portIsValid()) {
				return false;
			}
			if(!hostIsValid()) {
				return false;
			}
			if(!gdbDebuggerIsValid()) {
				return false;
			}
			if(!stopSymbolIsValid()) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);

		try {
			String id = configuration.getAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_COMMAND_FACTORY, CDTLaunchConfigConst.CDT_STANDARD_COMMAND_FACTORY); //$NON-NLS-1$
			loadCommandFactoryComboBox(configuration, id);
			id = configuration.getAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_PROTOCOL, CDTLaunchConfigConst.CDT_STANDARD_PROTOCOL); //$NON-NLS-1$
			loadProtocolComboBox(configuration, id);
			gdbCommandText.setText(configuration.getAttribute( CDTLaunchConfigConst.ATTR_DEBUG_NAME, "avr-gdb" ));
			gdbInitText.setText(configuration.getAttribute( CDTLaunchConfigConst.ATTR_GDB_INIT, ".gdbinit" ));
			verboseModeButton.setSelection(configuration.getAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_VERBOSE_MODE, false ));
			breakpointsFullPath.setSelection(configuration.getAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS, false ));
			stopInMainCheck.setSelection(configuration.getAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_STOP_AT_MAIN, false));
			stopInMainSymbol.setText(configuration.getAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, CDTLaunchConfigConst.CDT_STANDARD_MAIN_SYMBOL));
			configuration.getAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_START_MODE, CDTLaunchConfigConst.DEBUGGER_MODE_RUN);
			hostname.setText(configuration.getAttribute( CDTLaunchConfigConst.ATTR_HOST, "localhost" ));
			port.setText(configuration.getAttribute( CDTLaunchConfigConst.ATTR_PORT, "4242" ));
			initializeAdvancedAttributes(configuration);

		} catch (CoreException e) {
			TinyOSDebugPlugin.getDefault().log("Exception while initializing CDT debugger configuration tab.", e);
		}

		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if(isDirty()) {
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUG_NAME, gdbCommandText.getText().trim() );
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_GDB_INIT, gdbInitText.getText().trim() );
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_COMMAND_FACTORY, getCommandFactory() );
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_PROTOCOL, getProtocol() );
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_VERBOSE_MODE, verboseModeButton.getSelection() );
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS, breakpointsFullPath.getSelection() );
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_STOP_AT_MAIN, stopInMainCheck.getSelection());
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, stopInMainSymbol.getText());
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_START_MODE, CDTLaunchConfigConst.DEBUGGER_MODE_RUN);
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_HOST, hostname.getText().trim() );
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_PORT, port.getText().trim() );
			applyAdvancedAttributes(configuration);

			setDirty(false);
		}
		setAdditionalAttributes(configuration);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUG_NAME, "avr-gdb" );
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_GDB_INIT, ".gdbinit" );
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_COMMAND_FACTORY, CDTLaunchConfigConst.CDT_STANDARD_COMMAND_FACTORY );
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_PROTOCOL, CDTLaunchConfigConst.CDT_STANDARD_PROTOCOL );
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_VERBOSE_MODE, false );
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS, false );
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_STOP_AT_MAIN, false);
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, CDTLaunchConfigConst.CDT_STANDARD_MAIN_SYMBOL);
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_START_MODE, CDTLaunchConfigConst.DEBUGGER_MODE_RUN);
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_HOST, "localhost" );
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_PORT, "4242" );

		setAdditionalAttributes(configuration);
	}

	private void setAdditionalAttributes(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_ID, "org.eclipse.cdt.debug.mi.core.GDBServerCDebugger");
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_REMOTE_TCP ,true);
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_AUTO_SOLIB, false );
	}

}
