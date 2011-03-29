package tinyos.yeti.debug.simulation.manager.cooja;

import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import tinyos.yeti.debug.CDTAbstractionLayer.CDTLaunchConfigConst;
import tinyos.yeti.debug.CDTAbstractionLayer.CDTLaunchConfigurationDelegate;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.debug.simulation.TinyOSDebugSimulationPlugin;
import tinyos.yeti.debug.simulation.events.SimulationEvent;
import tinyos.yeti.debug.simulation.launch.configuration.ITinyOSDebugSimulationLaunchConstants;
import tinyos.yeti.debug.simulation.manager.ISimulationManager;

public class Mote 
{
	
	private int id, port;
	private IDebugTarget target;
	private String gdbBinary, gdbInit, firmware, mode;
	private ILaunchConfiguration originalConfiguration;
	private ILaunch launch;
	private IProject project;
	private ISimulationManager manager;
	private Job connectJob;
	
	public enum moteState { DISCONNECTED, CONNECTING, INITIALIZING, CONNECTED };
	private moteState state = moteState.DISCONNECTED;
	
	private boolean inBreakpoint = false;
	private String platform;
	
	
	public Mote(int id, ISimulationManager manager, ILaunch launch, ILaunchConfiguration originalLaunchConfiguration)
	{
		this.id = id;
		this.manager = manager;
		this.launch = launch;
		this.mode = "debug";
		this.originalConfiguration = originalLaunchConfiguration;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public moteState getState()
	{
		return state;
	}
	
	public ISimulationManager getSimulationManager()
	{
		return manager;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void setPort(int port)
	{
		this.port = port;
	}
	
	public IDebugTarget getTarget()
	{
		return target;
	}
	
	public void setTarget(IDebugTarget target)
	{
		this.target = target;
	}
	
	public String getGdbBinary()
	{
		return gdbBinary;
	}
	
	public void setGdbBinary(String gdbBinary)
	{
		this.gdbBinary = gdbBinary;
	}
	
	public String getGdbInit()
	{
		return gdbInit;
	}
	
	public void setGdbInit(String gdbInit)
	{
		this.gdbInit = gdbInit;
	}
	
	public String getFirmware()
	{
		return firmware;
	}
	
	public void setFirmware(String firmware)
	{
		this.firmware = firmware;
		this.project = findProject(firmware);
	}
	
	public String getPlatform()
	{
	    return platform;	
	}
	
	public void setPlatform(String platform)
	{
		this.platform = platform;
	}
	
	public void resume()
	{
		if(target != null)
		{
			try {
				target.resume();
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setInBreakpoint(false);
		}
	}
	
	public IProject getProject()
	{
		return project;
	}

	public String toString()
	{
		return "Mote: { id: "+this.id+ ", port: "+this.port+" }";
	}
	
	public boolean canDisconnect()
	{
		return (target != null) ? target.canTerminate() : false;
	}
	
	public void disconnect()
	{
		try {
			target.terminate();
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		state = moteState.DISCONNECTED;
	}
	
	public void terminate()
	{
		if(target != null)
		{
			try {
				target.terminate();
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		fireSimulationEvent(this, SimulationEvent.TERMINATE);
	}
	
	public void setConnected()
	{
		if(state == moteState.CONNECTING || state == moteState.INITIALIZING)
		{
			state = moteState.CONNECTED;
			fireSimulationEvent(this, SimulationEvent.CHANGE);
		}
	}
	
	public boolean canConnect()
	{
		if(state == moteState.CONNECTED
				|| project == null
				|| originalConfiguration == null
				|| gdbBinary == null
				|| gdbInit == null
				|| port == 0)
			return false;
		return true;
	}
	
	public boolean isBusy()
	{
		switch(state)
		{
			case CONNECTING:
			case INITIALIZING: 
				return true;
			default: return false;
		}
	}
	
	public boolean isInBreakpoint()
	{
		return inBreakpoint;
	}
	
	public void setInBreakpoint(boolean inBreakpoint)
	{
		this.inBreakpoint = inBreakpoint;
	}
	

	public void connect() 
	{
		if(!canConnect())
			return;
		
		try {
			setCNature(project);
		} catch (CoreException e) {
			TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to set C nature of project.", e.getMessage());
			return;
		}
		
		state = moteState.CONNECTING;
		
		connectJob = new Job("Connect Mote") {
			@Override
			protected IStatus run(IProgressMonitor monitor) 
			{
				monitor.beginTask("Connecting mote", 10);
				
				synchronized (originalConfiguration) 
				{
					// Create adapted version of the launch configuration
					ILaunchConfiguration realConfig = createRealLaunchConfiguration();
					if(realConfig == null)
					{
						TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to connect mote", "Unable to setup launch configuration");
						return Status.CANCEL_STATUS;
					}
					monitor.worked(1);
					
					// Delegate the launch to the CDT abstraction layer of the TinyOS debug plugin
					ILaunchConfigurationDelegate cdtDelegate = new CDTLaunchConfigurationDelegate();
					try {
						cdtDelegate.launch(realConfig, mode, launch, new SubProgressMonitor(monitor, 7));
					} catch (CoreException e) {
						TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to launch debugger", e.getMessage());
						return Status.CANCEL_STATUS;
					}
					
				}
				
				// Extract the newest debug target
				IDebugTarget[] targets = launch.getDebugTargets();
				target = targets[targets.length-1];
				
				
				
				return Status.OK_STATUS;
			}
		};
		connectJob.schedule();
		
		Job waitingJob = new Job("Waiting for Mote Connecting to be finished")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor) 
			{
				while(connectJob.getResult() == null)
				{	
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(connectJob.getResult() == Status.OK_STATUS)
				{
					if(state == moteState.CONNECTING)
						state = moteState.INITIALIZING;
					fireSimulationEvent(this, SimulationEvent.CHANGE);
				}
				else
				{
					state = moteState.DISCONNECTED;
					fireSimulationEvent(this, SimulationEvent.CHANGE);
				}
				return Status.OK_STATUS;
			}
		};
		waitingJob.schedule();
	}
	
	
	private ILaunchConfiguration createRealLaunchConfiguration()
	{
		// Extract host form original launch configuration (host for gdb is the same as host for the simulator)
		String host;
		try {
			host = originalConfiguration.getAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_HOST, "");
		} catch (CoreException e) {
			TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to connect mote", e.getMessage());
			return null;
		}
		if(host.equals(""))
			return null;
		
		ILaunchConfigurationWorkingCopy workingCopy = null;
		try {
			workingCopy = originalConfiguration.getWorkingCopy();
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUG_NAME, gdbBinary);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_AUTO_SOLIB, false);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_COMMAND_FACTORY, CDTLaunchConfigConst.CDT_STANDARD_COMMAND_FACTORY);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, false);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS, false);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_ID, "org.eclipse.cdt.debug.mi.core.GDBServerCDebugger");
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_PROTOCOL, CDTLaunchConfigConst.CDT_STANDARD_PROTOCOL);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_START_MODE, CDTLaunchConfigConst.DEBUGGER_MODE_RUN);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_STOP_AT_MAIN, false);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, CDTLaunchConfigConst.CDT_STANDARD_MAIN_SYMBOL);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_DEBUGGER_VERBOSE_MODE, false);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_GDB_INIT, gdbInit);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_HOST, host);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_PORT, Integer.toString(port));
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_PROGRAM_NAME, firmware);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_PROJECT_NAME, project.getName());
		workingCopy.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
		workingCopy.setAttribute(CDTLaunchConfigConst.ATTR_USE_TERMINAL, false);
				
		try {
			workingCopy.doSave();
		} catch (CoreException e) {
			TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to setup launch configuration", e.getMessage());
			return null;
		}
		return workingCopy;
	}
	
	private IProject findProject(String firmware)
	{
		IProject resultProject = null;
		IProject[] tinyOSProjects = TinyOSPlugin.getDefault().getProjects().getProjects();
		for(IProject project : tinyOSProjects)
		{
			if(project.getLocation().isPrefixOf(new Path(firmware)))
				resultProject = project;
		}
		return resultProject;
	}
	
	private void setCNature(IProject project) throws CoreException
	{
		if(project.hasNature(CDTLaunchConfigConst.CDT_PROJECT_NATURE))
			return;
		String[] natureIds = project.getDescription().getNatureIds();
		String[] newNatures = new String[natureIds.length+1];
		System.arraycopy(natureIds, 0, newNatures, 0, natureIds.length);
		newNatures[natureIds.length] = CDTLaunchConfigConst.CDT_PROJECT_NATURE;
		project.getDescription().setNatureIds(newNatures);
	}
	
	private void fireSimulationEvent(Object source, int type)
	{
		SimulationEvent event = new SimulationEvent(source, type);
		TinyOSDebugSimulationPlugin.getDefault().fireSimulationEvent(event);
	}
}






