package tinyOS.debug.simulation.ui.actions;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.Viewer;

import tinyOS.debug.simulation.manager.ISimulationManager;

public class TerminateManagerAction extends AbstractSimulatorAction
{
	public TerminateManagerAction(Viewer viewer) 
	{
		super("Disconnect Simulation", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_LCL_DISCONNECT));
		setViewer(viewer);
	}

	public void run()
	{
		ISimulationManager manager = getManager(getSelectedObject());
		
		if(manager != null)
			manager.terminate();
		
		viewer.setSelection(null);
	}
}
