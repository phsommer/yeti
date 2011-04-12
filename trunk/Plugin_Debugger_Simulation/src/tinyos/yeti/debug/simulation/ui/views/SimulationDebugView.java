package tinyos.yeti.debug.simulation.ui.views;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.debug.simulation.TinyOSDebugSimulationPlugin;
import tinyos.yeti.debug.simulation.events.ISimulationEventListener;
import tinyos.yeti.debug.simulation.events.SimulationEvent;
import tinyos.yeti.debug.simulation.manager.ISimulationManager;
import tinyos.yeti.debug.simulation.manager.cooja.Mote;
import tinyos.yeti.debug.simulation.ui.actions.ConnectMoteAction;
import tinyos.yeti.debug.simulation.ui.actions.DisconnectMoteAction;
import tinyos.yeti.debug.simulation.ui.actions.ResumeSimulationAction;
import tinyos.yeti.debug.simulation.ui.actions.StepIntoAction;
import tinyos.yeti.debug.simulation.ui.actions.StepOverAction;
import tinyos.yeti.debug.simulation.ui.actions.StepReturnAction;
import tinyos.yeti.debug.simulation.ui.actions.TerminateManagerAction;
import tinyos.yeti.editors.NesCIcons;



public class SimulationDebugView extends AbstractDebugView
{
	private TreeViewer viewer;
	private TreeRoot root;
	
	private static final String DISCONNECT_MANAGER_ACTION_ID = "terminate_manager_action";
	private static final String CONNECT_MOTE_ACTION_ID = "connect_mote_action";
	private static final String DISCONNECT_MOTE_ACTION_ID = "terminate_mote_action";
	private static final String RESUME_SIMULATION_ACTION_ID = "resume_simulation_action";
	private static final String STEP_INTO_ACTION_ID = "step_into_action";
	private static final String STEP_OVER_ACTION_ID = "step_over_action";
	private static final String STEP_RETURN_ACTION_ID = "step_return_action";
	
	@Override
	protected Viewer createViewer(Composite parent) 
	{
		TreeViewer viewer = new TreeViewer(parent);
		viewer.setContentProvider(new DebugContentProvider());
		viewer.setLabelProvider(new DebugLabelProvider());
		viewer.setSorter(new DebugSorter());
		viewer.addSelectionChangedListener(new DebugSelectionListener());
		root = new TreeRoot();
		viewer.setInput(root);
		//DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
		this.viewer = viewer;
		
		return viewer;
	}

	@Override
	protected void createActions() 
	{
		// TODO: set icons
		// addItemAction.setImageDescriptor(getImageDescriptor("add.gif"));
		
		System.out.println("createActions()");
		ConnectMoteAction connectAction = new ConnectMoteAction(viewer);
		setAction(CONNECT_MOTE_ACTION_ID, connectAction);
		
		DisconnectMoteAction disconnectAction = new DisconnectMoteAction(viewer);
		setAction(DISCONNECT_MOTE_ACTION_ID, disconnectAction);
		
		ResumeSimulationAction resumeAction = new ResumeSimulationAction(viewer);
		setAction(RESUME_SIMULATION_ACTION_ID, resumeAction);
		
		TerminateManagerAction terminateManagerAction = new TerminateManagerAction(viewer);
		setAction(DISCONNECT_MANAGER_ACTION_ID, terminateManagerAction);
		
		StepIntoAction stepIntoAction = new StepIntoAction(viewer);
		setAction(STEP_INTO_ACTION_ID, stepIntoAction);
		
		StepOverAction stepOverAction = new StepOverAction(viewer);
		setAction(STEP_OVER_ACTION_ID, stepOverAction);
		
		StepReturnAction stepReturnAction = new StepReturnAction(viewer);
		setAction(STEP_RETURN_ACTION_ID, stepReturnAction);
	}

	@Override
	protected String getHelpContextId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fillContextMenu(IMenuManager menu) 
	{
		menu.add(getAction(CONNECT_MOTE_ACTION_ID));
		menu.add(getAction(DISCONNECT_MOTE_ACTION_ID));
		menu.add(new Separator());
		menu.add(getAction(RESUME_SIMULATION_ACTION_ID));
		menu.add(getAction(STEP_INTO_ACTION_ID));
		menu.add(getAction(STEP_OVER_ACTION_ID));
		menu.add(getAction(STEP_RETURN_ACTION_ID));
		menu.add(new Separator());
		menu.add(getAction(DISCONNECT_MANAGER_ACTION_ID));
	}

	@Override
	protected void configureToolBar(IToolBarManager tbm) 
	{
		System.out.println("configureToolBar()");
		tbm.add(getAction(RESUME_SIMULATION_ACTION_ID));
	}
	
	private class DebugContentProvider implements ITreeContentProvider, ISimulationEventListener
	{
		public DebugContentProvider()
		{
			TinyOSDebugSimulationPlugin.getDefault().addSimulationEventListener(this);
		}
		
		@Override
		public void dispose() {
			TinyOSDebugSimulationPlugin.getDefault().removeSimulationEventListener(this);			
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {  }

		@Override
		public Object[] getElements(Object inputElement) 
		{
			if(inputElement instanceof TreeRoot)
				return ((TreeRoot)inputElement).getElements();
			
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) 
		{
			if(parentElement instanceof ISimulationManager)
			{
				return ((ISimulationManager)parentElement).getMotes();
			}
				
			
			if(parentElement instanceof Mote)
			{
				try {
					IThread[] threads = ((Mote)parentElement).getTarget().getThreads();
					if(threads.length > 0)
						return threads[0].getStackFrames();
				} catch (DebugException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) 
		{
			if(element instanceof ISimulationManager)
				return root;
			if(element instanceof Mote)
				return ((Mote)element).getSimulationManager();
			return null;
		}

		@Override
		public boolean hasChildren(Object element) 
		{
			if(element instanceof TreeRoot)
				return (((TreeRoot)element).getElements().length > 0) ? true : false;
			
			if(element instanceof ISimulationManager)
				return (((ISimulationManager)element).getMotes().length > 0) ? true : false;
			
			if(element instanceof Mote)
			{
				Mote mote = (Mote)element;
				if(mote.getState() == Mote.moteState.DISCONNECTED)
					return false;
				if(mote.getTarget() == null)
					return false;
				if(mote.getSimulationManager().getSimulationState() == ISimulationManager.SIM_STATE_RUNNING)
					return false;
			
				try {
					return mote.getTarget().hasThreads();
				} catch (DebugException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return false;
		}

		@Override
		public void handleSimulationEvent(SimulationEvent event)
		{
			System.out.println("New Simulation event: type = " + event.getType() + ", source = " + event.getSource());
			UIJob uiJob = new UIJob("Update Simulation Debug View") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					
					TreeItem root = null;
					try {
						root = viewer.getTree().getItem(0);
					} catch (Exception e) {}
					
					if (root!=null) viewer.getTree().setSelection(root);
					
					viewer.refresh();
					
					int cursorType = (isBusy()) ? SWT.CURSOR_WAIT : SWT.CURSOR_ARROW;
					viewer.getControl().setCursor(new Cursor(Display.getCurrent(), cursorType));

					return Status.OK_STATUS;
				}
			};
			uiJob.schedule();
		}
		
		private boolean isBusy()
		{
			Iterator<ISimulationManager> managerIterator = TinyOSDebugSimulationPlugin.getDefault().getSimulationManagers().iterator();
			while(managerIterator.hasNext())
			{
				ISimulationManager manager = managerIterator.next();
				if(manager.isBusy())
					return true;
				for(Mote mote : manager.getMotes())
				{
					if(mote.isBusy())
						return true;
				}
			}
			return false;
		}
	}
	
	
	private class DebugLabelProvider implements ILabelProvider
	{

		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Image getImage(Object element) 
		{
			if(element instanceof ISimulationManager)
				return NesCIcons.icons().get(NesCIcons.ICON_NESC);
			if(element instanceof Mote)
			{
				if(((Mote)element).getState() == Mote.moteState.CONNECTED)
				{
					switch(((Mote)element).getSimulationManager().getSimulationState())
					{
						case ISimulationManager.SIM_STATE_RUNNING:
							return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
						case ISimulationManager.SIM_STATE_STOPPED:
						{
							if(((Mote)element).isInBreakpoint()) {
								
								// TODO: set focus on mote and expand subtree
								return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
							} else
								return NesCIcons.icons().get(NesCIcons.ICON_TMOTE);
						}
					}
				}
				else
					return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED);
			}
			if(element instanceof IStackFrame)
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_STACKFRAME);
			
			return null;
		}

		@Override
		public String getText(Object element) 
		{
			if(element instanceof ISimulationManager)
			{
				if(((ISimulationManager)element).getSimulationState() == ISimulationManager.SIM_STATE_RUNNING)
					return "COOJA Simulation (Running)";
				return "COOJA Simulation (Paused)";
			}
				
			if(element instanceof Mote)
			{
				Mote mote = (Mote)element;
				String projectName = (mote.getProject() != null) ? mote.getProject().getName() : "Unknown Project";
				return mote.getPlatform() + " " + Integer.toString(mote.getId()) + " (" + projectName + ")";
			}

			if(element instanceof IStackFrame)
			{
				NesCStackFrameParser parser = new NesCStackFrameParser((IStackFrame)element);
				if(parser.isNesCStackFrame())
					return parser.getEventName();
				try {
					return ((IStackFrame)element).getName();
				} catch (DebugException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return "Unknown";
		}
		
	}
	
	private class DebugSorter extends ViewerSorter
	{
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) 
		{
			if(e1 instanceof IStackFrame && e2 instanceof IStackFrame)
			{
				IStackFrame[] frames;
				try {
					frames = ((IStackFrame)e1).getThread().getStackFrames();
				} catch (DebugException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0;
				}
				if(indexOf(e1, frames) > indexOf(e2, frames))
					return 1;
				if(indexOf(e1, frames) == indexOf(e2, frames))
					return 0;
				if(indexOf(e1, frames) < indexOf(e2, frames))
					return -1;
			}
			if(e1 instanceof Mote && e2 instanceof Mote)
			{
				int id1 = ((Mote)e1).getId();
				int id2 = ((Mote)e2).getId();
				if(id1 > id2)
					return 1;
				if(id1 == id2)
					return 0;
				if(id1 < id2)
					return -1;
			}
			return super.compare(viewer, e1, e2);
		}
		
		private int indexOf(Object o, Object[] array)
		{
			int i = 0;
			while(i < array.length)
			{
				if(o == array[i])
					break;
				i++;
			}
			if(i == array.length)
				return -1;
			return i;
		}
	}
	
	private class DebugSelectionListener implements ISelectionChangedListener, IDebugContextProvider, IDebugEventSetListener
	{
		
		private List<IDebugContextListener> listener;
		private ISelection activeContext;
		
		public DebugSelectionListener()
		{
			listener = new LinkedList<IDebugContextListener>();
			activeContext = null;
			DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextProvider(this);
		}
		
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) 
		{
			if(event.getSelection().isEmpty())
				return;
			
			if(!(event.getSelection() instanceof IStructuredSelection))
				return;
			
			IStructuredSelection sel = (IStructuredSelection)event.getSelection();
			activeContext = sel;
			
			updateActions();
			
			Iterator<IDebugContextListener> iterator = listener.iterator();
			while(iterator.hasNext())
			{
				DebugContextEvent contextEvent = new DebugContextEvent(this, activeContext, DebugContextEvent.ACTIVATED);
				iterator.next().debugContextChanged(contextEvent);
			}
		}

		@Override
		public IWorkbenchPart getPart() 
		{
			return getSite().getPart();
		}

		@Override
		public void addDebugContextListener(IDebugContextListener listener) 
		{
			this.listener.add(listener);
		}

		@Override
		public void removeDebugContextListener(IDebugContextListener listener) 
		{
			this.listener.remove(listener);
		}

		@Override
		public ISelection getActiveContext()
		{
			return activeContext;
		}


		@Override
		public void handleDebugEvents(DebugEvent[] events) 
		{
						
		}
		
		private void updateActions()
		{
			// Disconnect Simulation Action
			boolean enableDisconnectSimulationAction = !TinyOSDebugSimulationPlugin.getDefault().getSimulationManagers().isEmpty();
			getAction(DISCONNECT_MANAGER_ACTION_ID).setEnabled(enableDisconnectSimulationAction);
			
			// Connect/Disconnect Mote Action
			boolean enableConnectMoteAction = false;
			boolean enableDisconnectMoteAction = false;
			if(activeContext instanceof IStructuredSelection)
			{
				Object element = ((IStructuredSelection)activeContext).getFirstElement();
				if(element instanceof Mote)
				{
					if(((Mote)element).getSimulationManager().getSimulationState() == ISimulationManager.SIM_STATE_STOPPED)
					{
						enableConnectMoteAction = ((Mote)element).canConnect();
						enableDisconnectMoteAction = ((Mote)element).canDisconnect();
					}
				}
			}
			getAction(CONNECT_MOTE_ACTION_ID).setEnabled(enableConnectMoteAction);
			getAction(DISCONNECT_MOTE_ACTION_ID).setEnabled(enableDisconnectMoteAction);
			
			// Resume Action
			boolean enableResumeSimulationAction = false;
			ISimulationManager manager = ResumeSimulationAction.getManager(activeContext);
			try
			{
				if(manager.getSimulationState() == ISimulationManager.SIM_STATE_STOPPED)
				{
					enableResumeSimulationAction = true;
					for(Mote mote : manager.getMotes())
					{
						for(IThread thread : mote.getTarget().getThreads())
						{
							if(!thread.canResume())
								enableResumeSimulationAction = false;
						}
					}
				}
			} catch (DebugException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {	}
			getAction(RESUME_SIMULATION_ACTION_ID).setEnabled(enableResumeSimulationAction);
			
			// Step Action
			boolean enableStepIntoAction = false;
			boolean enableStepOverAction = false;
			boolean enableStepReturnAction = false;
			
			if(activeContext instanceof IStructuredSelection)
			{
				Object element = ((IStructuredSelection)activeContext).getFirstElement();
				System.out.println("element is: " + element);
				if(element instanceof Mote)
				{
					if(((Mote)element).getTarget() != null)
					{
						enableStepIntoAction = true;
						enableStepOverAction = true;
						enableStepReturnAction = true;
						try 
						{
							for(IThread thread : ((Mote)element).getTarget().getThreads())
							{
								if(!thread.canStepInto())
									enableStepIntoAction = false;
								if(!thread.canStepOver())
									enableStepOverAction = false;
								if (!thread.canStepReturn())
									enableStepReturnAction = false;
							}
						} catch (DebugException e) { e.printStackTrace(); }
					}
				} else if (element instanceof IStackFrame) {
										
					if(((IStackFrame)element).canStepInto())
						enableStepIntoAction = true;
					if(((IStackFrame)element).canStepOver())
						enableStepOverAction = true;
					if (((IStackFrame)element).canStepReturn())
						enableStepReturnAction = true;
					
				}
			}
			getAction(STEP_INTO_ACTION_ID).setEnabled(enableStepIntoAction && enableResumeSimulationAction);
			getAction(STEP_OVER_ACTION_ID).setEnabled(enableStepOverAction && enableResumeSimulationAction);
			getAction(STEP_RETURN_ACTION_ID).setEnabled(enableStepReturnAction && enableResumeSimulationAction);
		}
	}
	
	private class TreeRoot
	{
		private LinkedList<ISimulationManager> managers;
		
		public TreeRoot()
		{
			managers = TinyOSDebugSimulationPlugin.getDefault().getSimulationManagers();
		}
		
		public ISimulationManager[] getElements()
		{
			return managers.toArray(new ISimulationManager[0]);
		}
	}
}
