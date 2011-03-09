package tinyos.yeti.debug.simulation.launch.configuration;

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

import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.launch.configuration.AbstractTinyOSDebuggerTab;

public class CoojaConfigurationTab extends AbstractTinyOSDebuggerTab implements ISimulatorConfigurationTab 
{
	/**
	 * The id of cooja simulator configurations.
	 */
	public static final String SIMULATOR_ID = "tinyos.yeti.debugger.simulation.simulatorTab.cooja";
	
	/**
	 * Launch configuration attribute key. The value defines which simulator is used.
	 */
	public static final String ATTR_CURRENT_SIMULATOR = ITinyOSDebugSimulationLaunchConstants.ATTR_CURRENT_SIMULATOR;

	/**
	 * Launch configuration attribute key. The value defines the host on which listens for connections.
	 */
	public static final String ATTR_SIMULATOR_HOST = ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_HOST;
	
	/**
	 * Launch configuration attribute key. The value defines the port on which listens for connections.
	 */
	public static final String ATTR_SIMULATOR_PORT = ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_PORT;

	
	private Label hostLabel;
	private Text host;
	
	private Label portLabel;
	private Text port;

	
	public CoojaConfigurationTab() 
	{
		
	}

	@Override
	public void createControl(Composite parent) 
	{
		createConnectionSetting(parent);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) 
	{
		configuration.setAttribute(ATTR_CURRENT_SIMULATOR, SIMULATOR_ID);
		configuration.setAttribute(ATTR_SIMULATOR_HOST, "localhost");
		configuration.setAttribute(ATTR_SIMULATOR_PORT, 4242);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) 
	{
		setInitializing(true);

		String hostString = "localhost";
		String portString = "4242";
		
		try {
			hostString = configuration.getAttribute(ATTR_SIMULATOR_HOST, "localhost");
			portString = ((Integer)(configuration.getAttribute(ATTR_SIMULATOR_PORT,4242))).toString();
		} catch (CoreException e) {
			TinyOSDebugPlugin.getDefault().log("Exception while initializing cooja simulator tab", e);
		}
		
		host.setText(hostString);
		port.setText(portString);
		
		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) 
	{
		if(isDirty()) 
		{
			configuration.setAttribute(ATTR_SIMULATOR_HOST, host.getText().trim()); 
			int applyPort = 0;
			try{
				applyPort = Integer.parseInt(port.getText().trim());
			} catch (Exception e) { }
			configuration.setAttribute(ATTR_SIMULATOR_PORT, applyPort);
			setDirty(false);
		}
	}

	@Override
	public String getName() 
	{
		return "Cooja";
	}

	@Override
	public String getID() 
	{
		return SIMULATOR_ID;
	}

	@Override
	public String getHost() 
	{
		return host.getText();
	}

	@Override
	public int getPort() 
	{
		int parsedPort = 0;
		try{
			parsedPort = Integer.parseInt(port.getText());
		} catch(Exception e) {}
		return parsedPort;
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig)
	{
		return (hostIsValid(launchConfig) && portIsValid(launchConfig));
	}
	
	private boolean hostIsValid(ILaunchConfiguration launchConfig)
	{
		String configHost = "";
		try {
			configHost = launchConfig.getAttribute(ATTR_SIMULATOR_HOST, "");
		} catch (CoreException e) {
			TinyOSDebugPlugin.getDefault().log("Exception while checking host to be valid", e);
		}
		return !(configHost.equals(""));
	}
	
	private boolean hostIsValid()
	{
		return !(host.getText().equals(""));
	}
	
	private boolean portIsValid(ILaunchConfiguration launchConfig)
	{
		int configPort = 0;
		try {
			configPort = launchConfig.getAttribute(ATTR_SIMULATOR_PORT, 0);
		} catch (CoreException e) {
			TinyOSDebugPlugin.getDefault().log("Exception while checking port to be valid", e);
		}
		return (configPort > 0 && configPort < 65535);
	}
	
	private boolean portIsValid()
	{
		int portInt = 0;
		try{
			portInt = Integer.parseInt(port.getText());
		} catch(Exception e) {
			return false;
		}
		return (portInt > 0 && portInt < 65535);
	}
	
	private void createConnectionSetting(Composite parent)
	{
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comboComp.setLayoutData(gd);

		hostLabel = new Label(comboComp, SWT.NONE);
		hostLabel.setText("Connect to Cooja on host");
		gd = new GridData();
		gd.horizontalSpan = 1;
		hostLabel.setLayoutData(gd);

		host = new Text(comboComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 100;
		host.setLayoutData(gd);
		host.addModifyListener(
			new ModifyListener() 
			{
				public void modifyText(ModifyEvent evt) {
					String error = "Invalid host";
					if(!hostIsValid()) 
					{
						setErrorCondition(error);
					} else {
						removeErrorCondition(error);
					}
					if ( !isInitializing() ) 
					{
						setDirty(true);
						updateLaunchConfigurationDialog();
					}
				}
			});
	
		portLabel = new Label(comboComp, SWT.NONE);
		portLabel.setText("Connect to Cooja on port");
		gd = new GridData();
		gd.horizontalSpan = 1;
		portLabel.setLayoutData(gd);

		port = new Text(comboComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 100;
		port.setLayoutData(gd);
		port.addModifyListener(
			new ModifyListener() 
			{
				public void modifyText(ModifyEvent evt) {
					String error = "Invalid port";
					if(!portIsValid()) 
					{
						setErrorCondition(error);
					} else {
						removeErrorCondition(error);
					}
					if ( !isInitializing() ) 
					{
						setDirty(true);
						updateLaunchConfigurationDialog();
					}
				}
			});
	}
}
