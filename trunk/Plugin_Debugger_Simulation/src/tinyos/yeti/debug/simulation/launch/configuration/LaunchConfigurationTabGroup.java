package tinyos.yeti.debug.simulation.launch.configuration;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;



public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup{

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new SimulatorTab(),
				//new MainConfigurationTab(),
				//new GdbProxyTab(),
				//new DebuggerConfigurationTab(),
				new SourceLookupTab(),
				new CommonTab()
			};
			setTabs(tabs);
	}

	
}
