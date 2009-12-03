/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyOS.debug.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;

import tinyOS.debug.NesCDebugIcons;
import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.variables.IModuleDescriptor;
import tinyOS.debug.variables.internal.ModuleDescriptor;
import tinyOS.debug.variables.internal.NesCSeparatorFromCDTLaunch;
import tinyOS.debug.variables.internal.NesCVariableNameParser;
import tinyOS.debug.views.actions.ChangeValueAction;


/**
 * This viewer displays all NesC variables grouped by the components they are defined in.
 */

public class NescVariablesViewer extends AbstractDebugView  implements IDebugContextListener {
	public static final String ASSIGNMENT = "=";
	
	private DrillDownAdapter drillDownAdapter;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */

	public class TreeNode implements IAdaptable {
		private String name;
		private TreeParent parent;
		private Object data;

		public TreeNode(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setData(Object obj) {
			this.data = obj;
		}

		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		public String toString() {
			String res = getName();
			if(data instanceof IVariable) {
				IVariable var = (IVariable) data;
				try {
					String value = var.getValue().getValueString();
					res += " " + ASSIGNMENT + " " + value;
				} catch (DebugException e) {
					TinyOSDebugPlugin.getDefault().log("Could not get value from variable", e);
				}
			}
			return res;
		}
		@SuppressWarnings("unchecked")
		public Object getAdapter(Class key) {
			if (key == IVariable.class && data instanceof IVariable) {
				return data;
			}
			if (key == IModuleDescriptor.class && data instanceof IModuleDescriptor) {
				return data;
			}
			return null;
		}
		public boolean containsModule() {
			return (data instanceof IModuleDescriptor);
		}
		public boolean containsVariable() {
			return (data instanceof IVariable);
		}
	}

	class TreeParent extends TreeNode {
		private ArrayList<TreeNode> children;
		public TreeParent(String name) {
			super(name);
			children = new ArrayList<TreeNode>();
		}
		public void addChild(TreeNode child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(TreeNode child) {
			children.remove(child);
			child.setParent(null);
		}
		public TreeNode [] getChildren() {
			return (TreeNode [])children.toArray(new TreeNode[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
		}
		public TreeNode hasChild(String name) {
			for(TreeNode t: children){
				if(t.getName().equals(name))
					return t;
			}
			return null;
		}
	}

	class NescVariableContentProvider implements IStructuredContentProvider, 
	ITreeContentProvider {
		private TreeParent invisibleRoot = new TreeParent("");
		private boolean isDisposed = false;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if(newInput != oldInput){
				populate(newInput);
			}
		}
		public void dispose() {
			isDisposed = true;
		}
		public boolean isDisposed() {
			return isDisposed;
		}
		// Build the tree using the given source.
		public Object[] getElements(Object source) {
			if(source != null){
				populate(source);
				return getChildren(invisibleRoot);
			}
			return getChildren(source);
		}
		// Tree navigation functions
		public Object getParent(Object child) {
			if (child instanceof TreeNode) {
				return ((TreeNode)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
		private void populate(Object o) {
			if(o instanceof IStackFrame) {
				IStackFrame f = (IStackFrame) o;
				NesCVariableNameParser varPars = new NesCVariableNameParser(new NesCSeparatorFromCDTLaunch(f.getDebugTarget().getLaunch()));
				try {
					if(f.hasVariables()) {
						IVariable[] vars = f.getVariables();
						populate(vars,varPars);
					}
				} catch (DebugException e) {
					TinyOSDebugPlugin.getDefault().log("Exception while populating viewer.",e);
				}
			} else {
				clear();
			}
		}
		private void clear() {
			invisibleRoot = new TreeParent("");
		}
		private void populate(IVariable[] variables, NesCVariableNameParser varPars) {
			HashMap<String, HashMap<String, IVariable> > modules = getModuleDescriptors(variables,varPars);
			for(TreeNode t: invisibleRoot.getChildren()){
				if(!modules.containsKey(t.getName()))
					invisibleRoot.removeChild(t);
			}
			for(String desc : modules.keySet()) {
				TreeNode p = invisibleRoot.hasChild(desc);
				if(p == null) {
					p = new TreeParent(desc);
					p.setData(new ModuleDescriptor(desc));
					invisibleRoot.addChild(p);
				}
				if(p instanceof TreeParent) {
					TreeParent parent = (TreeParent) p;
					HashMap<String,IVariable> modVars = modules.get(desc);
					for(TreeNode t: parent.getChildren()){
						if(!modVars.containsKey(t.getName()))
							parent.removeChild(t);
					}
					Collection<IVariable> vars = modules.get(desc).values();
					for(IVariable var:vars) {
						addVariable(var,varPars,parent);
					}
				}
			}
		}
		private void addVariable(IVariable var, NesCVariableNameParser varPars, TreeParent parent){
			try {
				IValue val = var.getValue();
				String cVarName = var.getName();
				String name = varPars.getVariableName(cVarName);
				TreeNode node = parent.hasChild(name);
				if(val.hasVariables()) {
					// var consists of multiple inner variables... (like for example a struct)
					if(node == null) {
						node = new TreeParent(name);
						node.setData(var);
						parent.addChild(node);
					}
					for(IVariable innerVar: val.getVariables()){
						addVariable(innerVar, varPars, (TreeParent)node);
					}
				}
				else {
					if(node == null) {
						node = new TreeParent(name); // Leaf
						node.setData(var);
						parent.addChild(node);
					}
				}

			} catch (DebugException e) {
				TinyOSDebugPlugin.getDefault().log("Exception while adding variable to tree.",e);
			}
		}
		private HashMap<String, HashMap<String, IVariable> > getModuleDescriptors(IVariable[] variables, NesCVariableNameParser varPars){
			HashMap<String, HashMap<String, IVariable> > res = new HashMap<String, HashMap<String, IVariable> >();
			for(IVariable var : variables) {
				try {
					if(varPars.isNesCVariable(var.getName())) {
						String name = var.getName();
						String desc = varPars.getModuleName(name);
						String varName = varPars.getVariableName(name);
						if(desc != null) {
							HashMap<String, IVariable> modVars = res.get(desc);
							if(modVars == null) {
								modVars = new HashMap<String, IVariable>();
								res.put(desc, modVars);
							}
							modVars.put(varName,var);
						}
					}
				} catch (DebugException e) {
					TinyOSDebugPlugin.getDefault().log("Exception while extracting module descriptors.",e);
				}
			}
			return res;
		}
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {		
			if (obj instanceof TreeNode) {
				TreeNode to = (TreeNode)obj;
				IVariable var = (IVariable) to.getAdapter(IVariable.class);
				if(var != null) {
					try {
						if(var.getValue().hasVariables()){
							return NesCDebugIcons.get(NesCDebugIcons.ICON_VAR_AGGR);
						} else {
							return NesCDebugIcons.get(NesCDebugIcons.ICON_VAR_SIMPLE);
						}
					} catch (DebugException e) {
						TinyOSDebugPlugin.getDefault().log("Exception while getting image.",e);
					}
				} else if(to.getAdapter(IModuleDescriptor.class) != null) {
					return NesCDebugIcons.get(NesCDebugIcons.ICON_COMPONENT);
				}
			}
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

	class NameSorter extends ViewerSorter {
		private String sub;
		private int index;
		private boolean indexed;
		
		@Override
		public int compare( Viewer viewer, Object e1, Object e2 ){
			String s1 = e1.toString();
			String s2 = e2.toString();
			
			indexed( s1 );
			if( !indexed )
				return super.compare( viewer, e1, e2 );
			
			int i1 = index;
			s1 = sub;
			
			indexed( s2 );
			if( !indexed )
				return super.compare( viewer, e1, e2 );
			
			int i2 = index;
			s2 = sub;
			
			if( s1.equals( s2 )){
				return i1 - i2;
			}
			else{
				return super.compare( viewer, e1, e2 );
			}
		}
		
		private void indexed( String string ){
			int indexEq = string.indexOf( ASSIGNMENT );
			if( indexEq >= 0 )
				string = string.substring( 0, indexEq );
			
			int beginB = string.indexOf( "[" );
			int endB = string.indexOf( "]" );
			
			if( beginB == -1 || endB == -1 || beginB+1 >= endB ){
				beginB = string.indexOf( "(" );
				endB = string.indexOf( ")" );
			}
			
			if( beginB == -1 || endB == -1 || beginB+1 >= endB ){
				indexed = false;
				return;
			}
			
			String sub = string.substring( beginB+1, endB );
			try{
				index = Integer.valueOf( sub );
				indexed = true;
				this.sub = string.substring( 0, beginB );
			}
			catch( NumberFormatException ex ){
				indexed = false;
			}
		}
	}

	/**
	 * This is a callback that will allow us
	 * to clean up when the viewer is closed.
	 */
	public void dispose()
	{
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).removeDebugContextListener(this);
		super.dispose();
	}

	private void hookContextMenu(TreeViewer viewer) {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				NescVariablesViewer.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(getAction("change_value"));
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void hookDoubleClickAction(TreeViewer viewer) {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				getAction("doubleClickAction").run();
			}
		});
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		ISelection selection = event.getContext();
		contextActivated(selection);
	}

	/**
	 * Updates actions and sets the viewer input when a context is activated.
	 * @param selection
	 */
	protected void contextActivated(ISelection selection) {
		if (!isAvailable() || !isVisible()) {
			return;
		}
		if (selection instanceof IStructuredSelection) {
			Object source = ((IStructuredSelection)selection).getFirstElement();
			TreePath[] paths = ((TreeViewer)getViewer()).getExpandedTreePaths();
			this.getViewer().setInput(source);
			((TreeViewer)getViewer()).setExpandedTreePaths(paths);
		}
	}
	@Override
	protected void configureToolBar(IToolBarManager manager) {
		//manager.add(getAction("action2"));
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	@Override
	protected void createActions() {
		Action changeValueAction = new ChangeValueAction(this);

		setAction("change_value", changeValueAction);	
		setAction("doubleClickAction", changeValueAction);	
	}

	@Override
	protected Viewer createViewer(Composite parent) {
		TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new NescVariableContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());

		hookContextMenu(viewer);
		hookDoubleClickAction(viewer);

		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);

		return viewer;
	}

	@Override
	protected String getHelpContextId() {
		return "TinyOS_Debugger.viewer";
	}

	protected void becomesVisible() {
		super.becomesVisible();
		ISelection selection = DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).getActiveContext();
		contextActivated(selection);
	}

	protected void becomesHidden() {
		super.becomesHidden();
	}
}