package tinyOS.debug.simulation.manager.cooja;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import tinyOS.debug.simulation.TinyOSDebugSimulationPlugin;
import tinyOS.debug.simulation.events.ISimulationEventListener;
import tinyOS.debug.simulation.events.SimulationEvent;
import tinyOS.debug.simulation.launch.configuration.ITinyOSDebugSimulationLaunchConstants;
import tinyOS.debug.simulation.manager.ISimulationManager;

public class CoojaSimulationManager implements ISimulationManager, ISimulationEventListener
{	
	/**
	 * The timeout assigned to the network socket im ms. After this time, a SocketTimeoutException
	 * is thrown.
	 */
	private static final int SOCKET_TIMEOUT = 100;
	
	/**
	 * Number of attempts to read from the socket before assuming the connection to be timed out.
	 */
	private static final int SOCKET_TIMEOUT_NUMBER_OF_ATTEMPTS = 100;
	
	private ILaunchConfiguration originalConfiguration;
	@SuppressWarnings("unused")
	private String mode = "debug";
	private ILaunch launch;
	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	private int simulationState = ISimulationManager.SIM_STATE_STOPPED;
	
	private Map<Integer, Mote> motes = new HashMap<Integer, Mote>();
	
	private enum SimulationManagerState {
		CONNECTING_TO_COOJA,
		CONNECTED,
		TERMINATED
	}
	private SimulationManagerState managerState;
	
	public CoojaSimulationManager() 
	{
		
	}
	
	@Override
	public void setup(ILaunchConfiguration configuration, String mode, ILaunch launch) 
	{
		this.originalConfiguration = configuration;
		this.mode = mode;
		this.launch = launch;
	}

	@Override
	public void run() 
	{		
		managerState = SimulationManagerState.CONNECTING_TO_COOJA;
		TinyOSDebugSimulationPlugin.getDefault().addSimulationEventListener(this);
		
		fireSimulationEvent(this, SimulationEvent.CREATE);
		
		// Establish connection to Cooja
		boolean connectionEstablished = connectToCooja();
		if(!connectionEstablished)
		{
			terminate();
			return;
		}
		
		while(managerState != SimulationManagerState.TERMINATED)
		{
			CoojaCommand command = null;
			try {
				command = receiveOneCommand();
			} catch (Exception e) {
				if(!(e instanceof SocketTimeoutException))
				{
					DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
					break;
				}
			}
			if(command != null)
				command.processCommand();
		}
		terminate();
		TinyOSDebugSimulationPlugin.getDefault().removeSimulationEventListener(this);
		fireSimulationEvent(this, SimulationEvent.TERMINATE);
	}
	
	
	private boolean connectToCooja()
	{
		Job job = new Job("Connect to Cooja") 
		{
		    protected IStatus run(IProgressMonitor monitor) 
		    {
		    	monitor.beginTask("Connecting to Cooja", 5);
		    	try
		    	{
			    	// create socket with host/port pair from the configuration
			    	try {
						String host = originalConfiguration.getAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_HOST, "");
						int port = originalConfiguration.getAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_PORT, 0);
						socket = new Socket( host, port );
						socket.setSoTimeout(SOCKET_TIMEOUT);
					} catch (Exception e) {
						TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to establish connection to Cooja", e.getMessage());
						return Status.CANCEL_STATUS;
					}
					monitor.worked(1);
					
					// register input- and output streams
					boolean streamsRegistered = registerStreams(new SubProgressMonitor(monitor, 1));
					if(!streamsRegistered)
						return Status.CANCEL_STATUS;
					
					new HelloCommand().sendCommand();
					monitor.worked(1);
					
					// Waiting for HelloAck
					boolean helloAckReceived = waitForHelloAck(new SubProgressMonitor(monitor, 1));
					if(!helloAckReceived)
						return Status.CANCEL_STATUS;
					managerState = SimulationManagerState.CONNECTED;
					monitor.worked(1);
					
					new GetMoteIDsCommand().sendCommand();
					monitor.worked(1);
					
					fireDebugEvent(DebugEvent.CREATE);
					
				} finally {
					monitor.done();
		    	}
		        return Status.OK_STATUS;
		    }
		};
		job.schedule();
		while(job.getResult() == null)
		{
			try { Thread.sleep(10); }
			catch (InterruptedException e) { }
		}
		return job.getResult().isOK();
	}
	
	private boolean registerStreams(IProgressMonitor monitor)
	{
		monitor.beginTask("Register Streams", SOCKET_TIMEOUT_NUMBER_OF_ATTEMPTS);
		for(int i=0; i<SOCKET_TIMEOUT_NUMBER_OF_ATTEMPTS; i++)
		{
			if(monitor.isCanceled())
				return false;
			try {
				inputStream = socket.getInputStream();
				objectInputStream = new ObjectInputStream(inputStream);
				outputStream = socket.getOutputStream();
				objectOutputStream = new ObjectOutputStream( outputStream );
			} catch (IOException ex) {
				// also invoked when timeout is reached
				monitor.worked(1);
				continue;
			}
			monitor.done();
			return true;
		}
		TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to connect to Cooja", "Timeout while registering streams");
		monitor.done();
		return false;
	}
	
	private boolean waitForHelloAck(IProgressMonitor monitor)
	{
		monitor.beginTask("waiting for HelloAckCommand", SOCKET_TIMEOUT_NUMBER_OF_ATTEMPTS);
		CoojaCommand command = null;
		
		for(int i=0; command == null || !(command instanceof HelloAckCommand); i++)
		{
			if(i>SOCKET_TIMEOUT_NUMBER_OF_ATTEMPTS)
			{
				TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to connect to Cooja", "Connection timed out. Cooja did not reply on hello packet");
				monitor.done();
				return false;
			}
			try {
				command = receiveOneCommand();
			} catch (Exception e) {
				if (!(e instanceof SocketTimeoutException))
				{
					TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to connect to Cooja", "Unknown error while waiting for 'helloAck' package: "+e.getMessage());
					monitor.done();
					return false;
				}
			}
			monitor.worked(1);
			
			if(monitor.isCanceled())
				return false;
		}
		monitor.done();
		return true;
	}
	
	private class CoojaCommand
	{
		protected Map<String, String> command;
		@SuppressWarnings("unused")
		public String commandName = "Default command name";
		
		public CoojaCommand()
		{
			command = new HashMap<String,String>();
		}
		
		protected void setCommandName(String name)
		{
			command.put("commandName", name);
		}
		
		@SuppressWarnings("unchecked")
		public void setArguments(Map<?,?> arguments)
		{
			command.putAll((Map<String,String>)arguments);
		}
		
		public void addArgument(String key, String value)
		{
			command.put(key, value);
		}
		
		public Map<String, String> getCommand()
		{
			return command;
		}
		
		public void processCommand()
		{
			// do nothing
		}
		
		public void sendCommand()
		{
			if(objectOutputStream != null)
			{
				try {
					objectOutputStream.writeObject(command);
					objectOutputStream.flush();
				} catch (IOException e) {
					TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to send command to Cooja", e.getMessage());
				}
				
			}
		}
	}
	
	private class HelloCommand extends CoojaCommand
	{
		public static final String commandName = "hello";
		
		public HelloCommand()
		{
			setCommandName(commandName);
		}
	}
	
	private class HelloAckCommand extends CoojaCommand
	{
		public static final String commandName = "helloAck";
		
		public HelloAckCommand()
		{
			setCommandName(commandName);
		}
	}
	
	private class DisconnectCommand extends CoojaCommand
	{
		public static final String commandName = "disconnect";
		
		public DisconnectCommand()
		{
			setCommandName(commandName);
		}
	}
	
	private class GetMoteIDsCommand extends CoojaCommand
	{
		public static final String commandName = "getMoteIds";
		
		public GetMoteIDsCommand()
		{
			setCommandName(commandName);
		}
	}
	
	private class MoteIDsCommand extends CoojaCommand
	{
		public static final String commandName = "moteIDs";
		
		public MoteIDsCommand(Map<?,?> arguments)
		{
			setCommandName(commandName);
			setArguments(arguments);
		}
		
		public void processCommand()
		{
			// parse command argument 'ids' to list of integers
			ArrayList<Integer> newIDs = new ArrayList<Integer>();
			String[] idStrings = getCommand().get("ids").split(",");
			for(String idString : idStrings)
			{
				newIDs.add(Integer.parseInt(idString));
			}
			
			
			// Check if current Motes have to be deleted
			Iterator<Integer> iterator = motes.keySet().iterator();
			LinkedList<Integer> motesToDelete = new LinkedList<Integer>();
			while(iterator.hasNext())
			{
				int id = iterator.next();
				if(!(newIDs.contains(id)))
				{
					motes.get(id).disconnect();
					motesToDelete.add(id);
				}
			}
			iterator = motesToDelete.iterator();
			while(iterator.hasNext())
			{
				Mote removedMote = motes.remove(iterator.next());
				fireSimulationEvent(removedMote, SimulationEvent.TERMINATE);
				fireSimulationEvent(this, SimulationEvent.CHANGE);
			}
						
			// Check if new motes have to be inserted
			iterator = newIDs.iterator();
			while(iterator.hasNext())
			{
				int id = iterator.next();
				if(!(motes.containsKey(id)))
				{
					Mote newMote = new Mote(id, CoojaSimulationManager.this, launch, originalConfiguration);
					motes.put(id, newMote);
					new GetMoteConfiguration(id).sendCommand();
					fireSimulationEvent(newMote, SimulationEvent.CREATE);
					fireSimulationEvent(this, SimulationEvent.CHANGE);
				}
			}		
		}
	}
	
	private class GetMoteConfiguration extends CoojaCommand
	{
		public static final String commandName = "getMoteConfiguration";
		
		public GetMoteConfiguration(int id)
		{
			setCommandName(commandName);
			addArgument("id", Integer.toString(id));
		}
	}
	
	private class MoteConfigurationCommand extends CoojaCommand
	{
		public static final String commandName = "moteConfiguration";
		
		public MoteConfigurationCommand(Map<?,?> arguments)
		{
			setCommandName(commandName);
			setArguments(arguments);
		}
		
		public void processCommand()
		{
			int id = Integer.parseInt(getCommand().get("id"));
			Mote mote = motes.get(id);
			mote.setPort(Integer.parseInt(getCommand().get("port")));
			mote.setGdbBinary(getCommand().get("gdbBinary"));
			mote.setGdbInit(getCommand().get("gdbInit"));
			mote.setFirmware(getCommand().get("firmware"));
			fireSimulationEvent(mote, SimulationEvent.CHANGE);
		}
	}
	
	private class MoteConnectedCommand extends CoojaCommand
	{
		public static final String commandName = "moteConnected";
		
		public MoteConnectedCommand(Map<?,?> arguments)
		{
			setCommandName(commandName);
			setArguments(arguments);
		}
		
		public void processCommand()
		{
			int id = Integer.parseInt(getCommand().get("id"));
			motes.get(id).setConnected();
		}
	}
	
	private class ResumeSimulationCommand extends CoojaCommand
	{
		public static final String commandName = "resumeSimulation";
		
		public ResumeSimulationCommand()
		{
			setCommandName(commandName);
		}
	}
	
	private class BreakpointReceivedCommand extends CoojaCommand
	{
		public static final String commandName = "breakpointReceived";
		
		public BreakpointReceivedCommand(Map<?,?> arguments)
		{
			setCommandName(commandName);
			setArguments(arguments);
		}
		
		public void processCommand()
		{
			int id = Integer.parseInt(getCommand().get("id"));
			motes.get(id).setInBreakpoint(true);
			simulationState = SIM_STATE_STOPPED;
			fireSimulationEvent(this, SimulationEvent.CHANGE);
		}
	}

	private CoojaCommand receiveOneCommand() throws Exception
	{
		Map<?,?> receivedCommand = null;
		Object receivedObject = objectInputStream.readObject();
		if(receivedObject instanceof Map<?,?>)
			receivedCommand = (Map<?,?>)receivedObject;
		
		if(receivedCommand.get("commandName").equals(HelloCommand.commandName))
			return new HelloCommand();
		
		else if(receivedCommand.get("commandName").equals(HelloAckCommand.commandName))
			return new HelloAckCommand();
		
		else if(receivedCommand.get("commandName").equals(MoteIDsCommand.commandName))
			return new MoteIDsCommand(receivedCommand);
		
		else if(receivedCommand.get("commandName").equals(MoteConfigurationCommand.commandName))
			return new MoteConfigurationCommand(receivedCommand);
		
		else if(receivedCommand.get("commandName").equals(MoteConnectedCommand.commandName))
			return new MoteConnectedCommand(receivedCommand);
		
		else if(receivedCommand.get("commandName").equals(BreakpointReceivedCommand.commandName))
			return new BreakpointReceivedCommand(receivedCommand);
		
		TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Received unknown command form Cooja", (String)receivedCommand.get("commandName"));
		return null;
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) 
	{
		
	}
	
	public void fireDebugEvent(int kind)
	{
		DebugEvent event = new DebugEvent(this, kind);
		DebugEvent[] eventSet = new DebugEvent[1];
		eventSet[0] = event;
		DebugPlugin.getDefault().fireDebugEventSet(eventSet);
	}

	//@Override
	public ILaunch getLaunch() 
	{
		return launch;
	}

	@Override
	public Mote[] getMotes() 
	{
		return motes.values().toArray(new Mote[0]);
	}
	
	
	@Override
	public void resumeSimulation()
	{
		Job job = new Job("Wait for all motes to be resumed")
		{	
			@Override
			protected IStatus run(IProgressMonitor monitor) 
			{
				boolean allResumed = false;
				while(!allResumed)
				{
					// resume motes
					Iterator<Mote> iterator = motes.values().iterator();
					while(iterator.hasNext())
						iterator.next().resume();
					
					allResumed = true;
					iterator = motes.values().iterator();
					while(iterator.hasNext())
					{
						Mote mote = iterator.next();
						if(mote.getTarget() != null)
						{
							if(mote.getTarget().isSuspended())
							{
								allResumed = false;
							}
						}
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				simulationState = ISimulationManager.SIM_STATE_RUNNING;
				fireSimulationEvent(this, SimulationEvent.RESUME);
				new ResumeSimulationCommand().sendCommand();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}

	@Override
	public void terminate() 
	{
		// Terminate all motes
		Iterator<Mote> moteIterator = motes.values().iterator();
		while(moteIterator.hasNext())
		{
			moteIterator.next().terminate();
		}
		
		// Say coodbye to cooja
		new DisconnectCommand().sendCommand();
		
		// terminate manager
		try {
			launch.terminate();
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		managerState = SimulationManagerState.TERMINATED;
	}

	@Override
	public void handleSimulationEvent(SimulationEvent event) 
	{
		
	}
	
	private void fireSimulationEvent(Object source, int type)
	{
		SimulationEvent event = new SimulationEvent(source, type);
		TinyOSDebugSimulationPlugin.getDefault().fireSimulationEvent(event);
	}

	@Override
	public boolean isBusy() 
	{
		if(managerState == SimulationManagerState.CONNECTING_TO_COOJA)
		{
			return true;
		}
		return false;
	}

	@Override
	public int getSimulationState() 
	{
		return simulationState;
	}
}

