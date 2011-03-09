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
package tinyOS.debug.simulation.launch.configuration;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyOS.debug.NesCDebugIcons;
import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.CDTAbstractionLayer.CDTLaunchConfigConst;
import tinyOS.debug.launch.configuration.AbstractTinyOSDebuggerTab;
/**
 * Configures the CDT debugger.
 * @author Silvan Nellen
 *
 */
public class DebuggerConfigurationTab extends AbstractTinyOSDebuggerTab
{

	private Composite mainConfigurationHolder;
	private Text gdbCommandText;
	
	private Group connectionHolder;
	private Text hostname;
	private Text port;

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createMainHolder(comp);
		createMain(mainConfigurationHolder);
		createConnectionHolder(comp);
		createConnection(connectionHolder);
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

		Composite options = new Composite(gdbCommandComp, SWT.NULL);	
		options.setLayout(new GridLayout(2, true));
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		options.setLayoutData( gd );
		options = new Composite(gdbCommandComp, SWT.NULL);	
		options.setLayout(new GridLayout(1, true));
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		options.setLayoutData( gd );
	}

	private boolean gdbDebuggerIsValid() {
		if(this.gdbCommandText.getText().length() != 0)
			return true;
		return false;
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
		label.setText("Eclipse will try to connect to this host:port to communicate with the Cooja plugin:");
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


	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if(!portIsValid()) {
			System.out.println("port is not valid");
			return false;
		}
		if(!hostIsValid()) {
			System.out.println("host is not valid");
			return false;
		}
		if(!gdbDebuggerIsValid()) {
			System.out.println("gdbdebugger is not valid");
			return false;
		}
		return true;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);

		try {
			gdbCommandText.setText(configuration.getAttribute( CDTLaunchConfigConst.ATTR_DEBUG_NAME, "msp430-gdb" ));
			hostname.setText(configuration.getAttribute( CDTLaunchConfigConst.ATTR_HOST, "localhost" ));
			port.setText(configuration.getAttribute( CDTLaunchConfigConst.ATTR_PORT, "4242" ));
			
		} catch (CoreException e) {
			TinyOSDebugPlugin.getDefault().log("Exception while initializing CDT debugger configuration tab.", e);
		}

		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if(isDirty()) {
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUG_NAME, gdbCommandText.getText().trim() );
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_HOST, hostname.getText().trim() );
			configuration.setAttribute( CDTLaunchConfigConst.ATTR_PORT, port.getText().trim() );

			setDirty(false);
		}
		setAdditionalAttributes(configuration);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUG_NAME, "msp430-gdb" );
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_HOST, "localhost" );
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_PORT, "4242" );

		setAdditionalAttributes(configuration);
	}

	private void setAdditionalAttributes(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_DEBUGGER_ID, "tinyOS.debug.simulation.debugger.CoojaDebugger"); // TODO: hier ID des Debuggers hardcoden
		configuration.setAttribute( CDTLaunchConfigConst.ATTR_REMOTE_TCP ,true);
	}

}
