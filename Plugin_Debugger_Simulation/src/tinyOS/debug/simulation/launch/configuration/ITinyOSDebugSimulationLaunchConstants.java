package tinyOS.debug.simulation.launch.configuration;

/**
 * Defines common TinyOS debug simulation launch configuration constants.
 * @author Richard Huber
 *
 */
public interface ITinyOSDebugSimulationLaunchConstants 
{
	/**
	 * Prefix for all launch constants
	 */
	public static final String TINYOS_DBG_LAUNCH_SIMULATION_ID = "tinyOS.debug.simulation.launch";

	/**
	 * This is the launch type id.
	 */
	public static final String ID_LAUNCH_TINYOS_DEBUG_SIMULATION = "tinyOS.debug.launch.simulation.configurationType";
	
	/**
	 * Launch configuration attribute key. The value indicates the currently configured simulator.
	 */
	public static final String ATTR_CURRENT_SIMULATOR = TINYOS_DBG_LAUNCH_SIMULATION_ID + ".currentSimulator";
	
	/**
	 * Launch configuration attribute key. The value indicates the host where the simulator waits for the connection.
	 */
	public static final String ATTR_SIMULATOR_HOST = TINYOS_DBG_LAUNCH_SIMULATION_ID + ".simulatorHost";
	
	/**
	 * Launch configuration attribute key. The value indicates the port where the simulator waits for the connection.
	 */
	public static final String ATTR_SIMULATOR_PORT = TINYOS_DBG_LAUNCH_SIMULATION_ID + ".simulatorPort";
}
