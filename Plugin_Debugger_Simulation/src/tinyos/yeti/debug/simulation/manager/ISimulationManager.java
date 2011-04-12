package tinyos.yeti.debug.simulation.manager;

import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;

import tinyos.yeti.debug.simulation.manager.cooja.Mote;

/**
 * Defines the interface for simulation managers. Simulation managers
 * are responsible for establishing a connection to the simulation and
 * manage the simulation during the debug session.
 * @author: Richard Huber
 */
public interface ISimulationManager extends Runnable, IDebugEventSetListener
{
	
	public static final int SIM_STATE_RUNNING = 0;
	public static final int SIM_STATE_STOPPED = 1;
	public static final int SIM_SINGLE_STEPPING = 2;
	
	/**
	 * Setting up the configuration parameter for the simulation
	 * @param configuration The launch configuration of the initial launch
	 * @param mode Mode of the launch
	 * @param launch The launch to which the debugged motes should be attached to
	 */
	public void setup(ILaunchConfiguration configuration, String mode, ILaunch launch);
		
	public Mote[] getMotes();
	
	public Mote getMote(IDebugTarget target);

	public void resumeSimulation();
	
	public void stepMote(Mote mote);
	
	public void terminate();

	public boolean isBusy();
	
	public int getSimulationState(); 
}
