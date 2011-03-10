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
package tinyos.yeti.debug.views.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;

import tinyos.yeti.debug.NesCDebugIcons;
import tinyos.yeti.debug.TinyOSDebugPlugin;
import tinyos.yeti.debug.views.ChangeValueDialog;
import tinyos.yeti.debug.views.NescVariablesViewer;
import tinyos.yeti.debug.views.NescVariablesViewer.TreeNode;

public class ChangeValueAction  extends SelectionProviderAction {

	class InputValidator implements IInputValidator {
		IVariable m_var;
		public InputValidator(IVariable var) {
			super();
			this.m_var = var;
		}
		/**
		 * Returns an error string if the input is invalid
		 */
		public String isValid(String input) {
			try {
				if (m_var.verifyValue(input)) {
					return null; // null means valid
				}
			} catch (DebugException exception) {
				return "Exception while validating input: "+exception.getMessage(); 
			}
			return "Invalid input: "+input; 
		}
	}

	public ChangeValueAction(NescVariablesViewer viewer) {
		super(viewer.getViewer(), "Change value...");
		setImageDescriptor(NesCDebugIcons.getImageDescriptor(NesCDebugIcons.ICON_CHANGE_VARIABLE));
		setDisabledImageDescriptor(NesCDebugIcons.getImageDescriptor(NesCDebugIcons.ICON_CHANGE_VARIABLE_DISABLED));
		setToolTipText("Change the value of a variable.");
		m_viewer = viewer;
	}

	public void run() {
		IStructuredSelection ss = getStructuredSelection();
		if( ss.iterator().hasNext()) {
			Object obj = ss.iterator().next();
			if(obj instanceof TreeNode) {
				edit((TreeNode)obj);
			}
		}
	}

	private void edit(TreeNode node){
		// If a previous edit is still in progress, don't start another		
		if (m_isEditing) {
			return;
		}
		IVariable var = (IVariable)node.getAdapter(IVariable.class);
		if(var != null) {
			m_isEditing = true;
			doEdit(var);
			m_viewer.getViewer().refresh();
			m_isEditing = false;
		}
	}

	private void doEdit(IVariable var) {
		Shell shell = m_viewer.getViewSite().getShell();
		String name= "";
		String value= "";
		try {
			name= var.getName();
			value= var.getValue().getValueString();
		} catch (DebugException exception) {
			TinyOSDebugPlugin.getDefault().errorDialog(shell, "Error while changing value","Setting the value failed.", exception);	// 
			return;
		}
		ChangeValueDialog inputDialog= new ChangeValueDialog(shell, this.getText(), "Enter the new value for "+name, value, new InputValidator(var));

		inputDialog.open();
		String newValue= inputDialog.getValue();
		if (newValue != null) {
			// null value means cancel was pressed
			try {
				var.setValue(newValue);
				getSelectionProvider().setSelection(new StructuredSelection(var));
			} catch (DebugException de) {
				TinyOSDebugPlugin.getDefault().errorDialog(shell, "Error while changing value","Setting the value failed.", de);	// 
			}
		}

	}

	private boolean m_isEditing = false;
	private NescVariablesViewer m_viewer;
}
