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
import tinyos.yeti.debug.CDTAbstractionLayer.CDTLaunchConfigConst;

public class MSP430ProxyTab extends AbstractTinyOSDebuggerTab implements IGdbProxyConfigurationTab{
	public static final String PROXY_CONFIG_ID = ITinyOSDebugLaunchConstants.TINYOS_DBG_LAUNCH_ID + ".msp430Configuration";

	public static final String ATTR_GDB_SERVER_PORT = CDTLaunchConfigConst.ATTR_PORT;
	
	public static final String ATTR_ADAPTER_PORT = PROXY_CONFIG_ID + ".port";
	
	public static final String ATTR_ADAPTER_TARGET = PROXY_CONFIG_ID + ".target";
	
	private Text gdbPort;
	private Text adapterPort;
	private Text adapterTarget;
	
	@Override
	public String getCommand(){
		return "msp430-gdbproxy " +
			"--port=" + gdbPort.getText() + " " +
			adapterTarget.getText() + " " +
			adapterPort.getText();
	}

	@Override
	public String getID(){
		return PROXY_CONFIG_ID;
	}

	@Override
	public void createControl( Composite parent ){
		Composite content = new Composite( parent, SWT.NONE );
		content.setLayout( new GridLayout( 2, false ) );
		content.setLayoutData( new GridData( SWT.LEFT, SWT.TOP, false, false ) );
		
		createAdapterTargetSetting( content );
		createAdapterPortSetting( content );
		createPortSetting( content );
	}

	private void createPortSetting( Composite parent ){
		Label gdbServerPortLabel = new Label( parent, SWT.NONE );
		gdbServerPortLabel.setText("Listen for GDB on port"); //$NON-NLS-1$
		gdbServerPortLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );

		GridData data;
		gdbPort = new Text( parent, SWT.SINGLE | SWT.BORDER );
		gdbPort.setLayoutData( data = new GridData( SWT.LEFT, SWT.CENTER, true, false ));
		data.widthHint = 45;
		gdbPort.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				String error = "Invalid server port";
				if(!gdbServerPortIsValid()) {
					setErrorCondition(error);
				}
				else {
					removeErrorCondition(error);
				}
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});
	}
	
	private void createAdapterPortSetting( Composite parent ){
		Label gdbServerPortLabel = new Label( parent, SWT.NONE );
		gdbServerPortLabel.setText("Port attached to JTAG box"); //$NON-NLS-1$
		gdbServerPortLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );

		GridData data;
		adapterPort = new Text( parent, SWT.SINGLE | SWT.BORDER );
		adapterPort.setLayoutData( data = new GridData( SWT.FILL, SWT.CENTER, true, false ));
		data.widthHint = 250;
		adapterPort.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});
	}
	
	private void createAdapterTargetSetting( Composite parent ){
		Label gdbServerPortLabel = new Label( parent, SWT.NONE );
		gdbServerPortLabel.setText("Target of the JTAG box"); //$NON-NLS-1$
		gdbServerPortLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );

		GridData data;
		adapterTarget = new Text( parent, SWT.SINGLE | SWT.BORDER );
		adapterTarget.setLayoutData( data = new GridData( SWT.FILL, SWT.CENTER, true, false ));
		data.widthHint = 250;
		adapterTarget.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});		
	}

	private boolean gdbServerPortIsValid() {
		try {
			int port = Integer.parseInt( gdbPort.getText() );
			return ( port > 0 && port <= 0xFFFF );
		}
		catch( NumberFormatException e ) {
			return false;
		}
	}
	
	@Override
	public String getName(){
		return "msp430-proxy";
	}

	@Override
	public void initializeFrom( ILaunchConfiguration configuration ){
		try{
			setDirty( false );
			setInitializing( true );
			adapterPort.setText( configuration.getAttribute( ATTR_ADAPTER_PORT, "/dev/ttyUSB0" ) );
			adapterTarget.setText( configuration.getAttribute( ATTR_ADAPTER_TARGET, "msp430" ) );
			gdbPort.setText( configuration.getAttribute( ATTR_GDB_SERVER_PORT, "4242" ) );
		}
		catch( CoreException e ){
			TinyOSDebugPlugin.getDefault().log( "Exception while initializing msp430 proxy tag", e );
		}
		finally{
			setInitializing( false );
		}
	}

	@Override
	public void performApply( ILaunchConfigurationWorkingCopy configuration ){
		if( isDirty() ){
			configuration.setAttribute( ATTR_ADAPTER_PORT, adapterPort.getText() );
			configuration.setAttribute( ATTR_ADAPTER_TARGET, adapterTarget.getText() );
			configuration.setAttribute( ATTR_GDB_SERVER_PORT, gdbPort.getText() );
		}
	}

	@Override
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ){
		configuration.setAttribute( ATTR_ADAPTER_PORT, "/dev/ttyUSB0" );
		configuration.setAttribute( ATTR_ADAPTER_TARGET, "msp430" );
	}
}
