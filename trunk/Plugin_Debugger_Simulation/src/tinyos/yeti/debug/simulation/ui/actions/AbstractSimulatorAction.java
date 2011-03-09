package tinyos.yeti.debug.simulation.ui.actions;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import tinyos.yeti.debug.simulation.TinyOSDebugSimulationPlugin;
import tinyos.yeti.debug.simulation.manager.ISimulationManager;
import tinyos.yeti.debug.simulation.manager.cooja.Mote;

public abstract class AbstractSimulatorAction extends Action 
{
	
	protected Viewer viewer;
	
	public AbstractSimulatorAction(String string, ImageDescriptor imageDescriptor) 
	{
		super(string, imageDescriptor);
	}

	protected void setViewer(Viewer viewer)
	{
		this.viewer = viewer;
	}
	
	protected Object getSelectedObject()
	{
		if(viewer == null)
			return null;
		
		if(viewer.getSelection() instanceof IStructuredSelection)
		{
			return ((IStructuredSelection)viewer.getSelection()).getFirstElement();
		}
		
		return null;
	}
	
	
	public static ISimulationManager getManager(Object object)
	{
		if(object == null)
			return null;
		
		if(object instanceof IStructuredSelection)
			object = ((IStructuredSelection)object).getFirstElement();
		
		if(object instanceof ISimulationManager)
			return (ISimulationManager)object;
		
		if(object instanceof Mote)
			return ((Mote)object).getSimulationManager();
		
		if(object instanceof IStackFrame)
		{
			LinkedList<ISimulationManager> managers = TinyOSDebugSimulationPlugin.getDefault().getSimulationManagers();
			Iterator<ISimulationManager> iterator = managers.iterator();
			while(iterator.hasNext())
			{
				Mote[] motes = iterator.next().getMotes();
				for(Mote mote : motes)
				{
					try {
						for(IThread thread : mote.getTarget().getThreads())
						{
							for(IStackFrame frame : thread.getStackFrames())
							{
								if(frame == object)
									return mote.getSimulationManager();
							}
						}
					} catch (DebugException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						continue;
					}
				}
			}
			return null;
		}
		
		return null;
	}
}
