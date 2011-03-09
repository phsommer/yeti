package tinyos.yeti.debug.simulation.launch.configuration;

import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Defines the interface for the configuration of simulators (such as cooja).
 * @author Richard Huber
 *
 */
public interface ISimulatorConfigurationTab extends ILaunchConfigurationTab 
{
	/**
	 * Get the id of this configuration. Must be unique within the plugin.
	 * @return
	 */
	public abstract String getID();

	/**
	 * Get the configured host as String
	 * @return
	 */
	public abstract String getHost();
	
	/**
	 * Get the configured port
	 * @return
	 */
	public abstract int getPort();
	
	/**
	 * Gets the current error message of this tab
	 * @return the error message
	 */
	public abstract String getErrorCondition();

	/**
	 * Tells whether there are unsaved elements on this tab
	 * @return <code>true</code> if there are unsaved elements
	 */
	public abstract boolean isDirty();
}
