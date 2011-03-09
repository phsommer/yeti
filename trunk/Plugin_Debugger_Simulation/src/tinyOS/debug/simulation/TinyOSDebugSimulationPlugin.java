/**
 * 
 */
package tinyOS.debug.simulation;


import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

import tinyOS.debug.ITinyOSDebugConstants;
import tinyOS.debug.simulation.events.ISimulationEventListener;
import tinyOS.debug.simulation.events.SimulationEvent;
import tinyOS.debug.simulation.manager.ISimulationManager;


/**
 * The plugin class for TinyOS debug simulation core.
 * @author rihuber
 *
 */
public class TinyOSDebugSimulationPlugin extends AbstractUIPlugin implements ISimulationEventListener, IDebugEventSetListener {
	
    // The singleton instance.
    private static TinyOSDebugSimulationPlugin plugin;
    
    private LinkedList<ISimulationManager> managers;
    
    private LinkedList<ISimulationEventListener> simulationEventListener;
    
	/**
	 * The plug-in identifier (value <code>"tinyOS.debug.simulation"</code>).
	 */
	public static final String PLUGIN_ID = "tinyOS.debug.simulation";
	
    /**
     * The constructor.
     */
    public TinyOSDebugSimulationPlugin() {
        plugin = this;
        managers = new LinkedList<ISimulationManager>();
        simulationEventListener = new LinkedList<ISimulationEventListener>();
        addSimulationEventListener(this);
    }
    
    /**
     * Returns the shared instance.
     */
    public static TinyOSDebugSimulationPlugin getDefault() {
        return plugin;
    }
    
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}
	
	/**
	 * Get this plugin's preference store
	 */
    @Override
    public IPreferenceStore getPreferenceStore(){
        return super.getPreferenceStore();
    }
	
    public void log( String msg ){
        if( msg == null ){
            log( "null", null );
        }else{
            log( msg, null );
        }
    }

    public void log( String msg, Exception e ){
    	getLog().log( new Status( IStatus.ERROR, PLUGIN_ID, Status.OK, msg, e ) );
    }
    
    public static Display getStandardDisplay(){
        Display display;
        display = Display.getCurrent();
        if( display == null )
            display = Display.getDefault();
        return display;
    }
    
	/**
	 * Utility method with conventions
	 */
	public void errorDialog(Shell shell, String title, String message, Throwable t) {
		IStatus status;
		if (t instanceof CoreException) {
			status= ((CoreException)t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message= null;
			}
		} else {
			status= new Status(IStatus.ERROR, getUniqueIdentifier(), ITinyOSDebugConstants.INTERNAL_ERROR, "Error within TinyOS Debug UI: ", t); //$NON-NLS-1$
			log(status.toString());
		}
		ErrorDialog.openError(shell, title, message, status);
	}
    
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start( BundleContext context ) throws Exception 
    {
		super.start( context );
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@SuppressWarnings("deprecation")
	@Override
    public void stop( BundleContext context ) throws Exception {
		savePluginPreferences();
		super.stop( context );
	}
	
	public void dialog(int severity, String message, String reason)
	{
		final IStatus status = new Status(severity, PLUGIN_ID, reason);
		final String finalMessage = message;
		String title;
		switch(severity)
		{
			case IStatus.ERROR: title = "Error"; break;
			case IStatus.WARNING: title = "Warning"; break;
			case IStatus.INFO: title = "Info"; break;
			default: title = "";
		}
		final String finalTitle = title + " in TinyOS Debug Simulation Plugin";
		
		UIJob job = new UIJob(finalTitle) 
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) 
			{
				ErrorDialog.openError(Display.getDefault().getActiveShell(), finalTitle, finalMessage, status);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	public void addSimulationManager(ISimulationManager manager)
	{
		managers.add(manager);
		DebugPlugin.getDefault().addDebugEventListener(manager);
	}
	
	public void removeSimulationManager(ISimulationManager manager)
	{
		managers.remove(manager);
		DebugPlugin.getDefault().removeDebugEventListener(manager);
	}
	
	public LinkedList<ISimulationManager> getSimulationManagers()
	{
		return managers;
	}
	
	public void fireSimulationEvent(SimulationEvent event)
	{
		Iterator<ISimulationEventListener> iterator = simulationEventListener.iterator();
		while(iterator.hasNext())
		{
			ISimulationEventListener listener = iterator.next();
			if(listener != null)
				listener.handleSimulationEvent(event);
		}
	}
	
	public void addSimulationEventListener(ISimulationEventListener listener)
	{
		simulationEventListener.add(listener);
	}
	
	public void removeSimulationEventListener(ISimulationEventListener listener)
	{
		simulationEventListener.remove(listener);
	}

	@Override
	public void handleSimulationEvent(SimulationEvent event)
	{
		if(event.getSource() instanceof ISimulationManager)
		{
			if(event.getType() == SimulationEvent.CREATE)
			{
				if(!managers.contains(event.getSource()))
				{
					managers.add((ISimulationManager)event.getSource());
					SimulationEvent e = new SimulationEvent(this, SimulationEvent.CHANGE);
					fireSimulationEvent(e);
				}
			}
			else if(event.getType() == SimulationEvent.TERMINATE)
			{
				managers.remove(event.getSource());
				SimulationEvent e = new SimulationEvent(this, SimulationEvent.CHANGE);
				fireSimulationEvent(e);
			}
		}
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events)
	{
		for(DebugEvent event : events)
		{
			if(event.getKind() == DebugEvent.BREAKPOINT)
				System.out.println("DebugPlugin Breakpoint event received");
		}
		SimulationEvent event = new SimulationEvent(this, SimulationEvent.DEBUG_EVENT);
		fireSimulationEvent(event);
	}

}
