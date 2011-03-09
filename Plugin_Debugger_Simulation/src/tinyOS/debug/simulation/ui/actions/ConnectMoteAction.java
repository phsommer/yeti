package tinyOS.debug.simulation.ui.actions;


import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

import tinyOS.debug.simulation.manager.cooja.Mote;

public class ConnectMoteAction extends Action 
{
	private Viewer viewer;
	
	public ConnectMoteAction(Viewer viewer) 
	{
		super("Connect Mote", DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT));
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
				if(mote.canConnect())
				{
					viewer.getControl().setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT));
					mote.connect();
				}
			}
		}
		viewer.setSelection(null);
	}
}
