package tinyOS.debug.simulation.ui.actions;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.Viewer;

import tinyOS.debug.simulation.manager.ISimulationManager;

public class ResumeSimulationAction extends AbstractSimulatorAction
{
	public ResumeSimulationAction(Viewer viewer) 
	{
		super("Resume Simulation", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_LCL_DISCONNECT));
		setViewer(viewer);
	}
	
	public void run()
	{
		ISimulationManager manager = getManager(getSelectedObject());
		manager.resumeSimulation();
		viewer.setSelection(null);
	}
	
}
