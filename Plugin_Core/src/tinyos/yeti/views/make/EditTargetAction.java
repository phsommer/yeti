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
package tinyos.yeti.views.make;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.dialog.MakeTargetDialog;
import tinyos.yeti.make.targets.MakeTargetSkeleton;
import tinyos.yeti.nature.MissingNatureException;

public class EditTargetAction extends SelectionListenerAction {

    Shell shell;
    IResource resource;

    public EditTargetAction(Shell shell) {
        super("Edit Target");
        this.shell = shell;

        setToolTipText("");
        //MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_EDIT); //$NON-NLS-1$
    }

    @Override
    public void run() {
        if (canEdit()) {
            Runnable longJob = new Runnable() {

                public void run() {
                	Object element = getStructuredSelection().getFirstElement();
                	if( element instanceof MakeTarget ){
                		MakeTargetDialog<MakeTarget> dialog = new MakeTargetDialog.StandardMakeTargetDialog( shell, (MakeTarget)element );
                		dialog.openMakeTargetDialog();
                	}
                	else if( element instanceof IProject ){
                		try{
	                		ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( (IProject)element );
	                		if( tos != null ){
	                			MakeTargetDialog<MakeTargetSkeleton> dialog = 
	                				new MakeTargetDialog.ProjectDefaultsMakeTargetDialog( shell, tos );
	                			dialog.openMakeTargetDialog();
	                		}
                		}
                		catch( MissingNatureException ex ){
                			TinyOSCore.inform( "edit make-option", ex );
                		}
                	}
                };			
            };

            BusyIndicator.showWhile(TinyOSPlugin.getStandardDisplay(), longJob);
        }
    }

    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
        return super.updateSelection(selection) && canEdit();
    }

    private boolean canEdit() {
    	IStructuredSelection selection = getStructuredSelection();
    	
    	if( selection.size() != 1 )
    		return false;
    	
    	Object element = selection.getFirstElement();
    	// return element instanceof MakeTarget || element instanceof IProject;
    	return element instanceof MakeTarget;
    }
}
