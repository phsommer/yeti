package tinyOS.debug.simulation.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.simulation.launch.configuration.ITinyOSDebugSimulationLaunchConstants;
import tinyOS.debug.simulation.manager.ISimulationManager;

public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate 
{
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException 
	{
		// Find simulation manager
		monitor.beginTask("Find simulation manager", 10);
		try
		{
			// Get all extensions of the extension point
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			IExtensionPoint extPoint = reg.getExtensionPoint( "tinyos.yeti.debugger.simulation.simulatorTab" );

			// Find the extension selected by the launch configuration
			String simulationManagerId = configuration.getAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_CURRENT_SIMULATOR, "");
			ISimulationManager simulationManager = null;
			for(IExtension ext : extPoint.getExtensions())
	        {
	        	for(IConfigurationElement element : ext.getConfigurationElements())
	            {
	            	if(element.getName().equals("tab") && element.getAttribute("id").equals(simulationManagerId))
	            	{
	            		// create simulation manager
	                	simulationManager = (ISimulationManager)element.createExecutableExtension("simulation_manager");
	            	}
	            }
	        }
			if(simulationManager == null)
			{
				TinyOSDebugPlugin.getDefault().log("Simulation manager not found. (simulator id: "+simulationManagerId+")");
				return;
			}
			
			// create thread with simulation manager and start it
			simulationManager.setup(configuration, mode, launch);
			Thread simulationManagerThread = new Thread(simulationManager);
			simulationManagerThread.start();
			
		} finally {
			monitor.done();
		}
	}
}
