package tinyos.yeti.debug.simulation.ui.actions;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.Viewer;

import tinyos.yeti.debug.simulation.manager.ISimulationManager;

public class ResumeSimulationAction extends AbstractSimulatorAction
{
	public ResumeSimulationAction(Viewer viewer) 
	{
		super("Resume Simulation", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_LCL_DISCONNECT));
		setViewer(viewer);
	}
	
	public void run()
	{
		System.out.println("ResumeSimulationAction");
		System.out.println("getSelectedObject(): " + getSelectedObject());
		ISimulationManager manager = getManager(getSelectedObject());
		manager.resumeSimulation();
		System.out.println("manager: " + manager);
		viewer.setSelection(null);
	}
	
}
