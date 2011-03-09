package tinyOS.debug.simulation.ui.actions;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import tinyOS.debug.simulation.manager.cooja.Mote;

public class DisconnectMoteAction extends Action 
{
	private Viewer viewer;
	
	public DisconnectMoteAction(Viewer viewer) 
	{
		super("Disconnect Mote", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED));
		this.viewer = viewer;
	}

	public void run()
	{
		ISelection selection = viewer.getSelection();
		
		if(selection.isEmpty())
			return;
		
		if(selection instanceof IStructuredSelection)
		{
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			if(structuredSelection.getFirstElement() instanceof Mote)
			{
				Mote mote = (Mote)structuredSelection.getFirstElement();
				if(mote.canDisconnect())
					mote.disconnect();
			}
		}
		viewer.setSelection(null);
	}
}
