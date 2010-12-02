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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.IProjectMakeTargets;
import tinyos.yeti.make.targets.IMakeTargetMorpheable;

public class SetDefaultTargetAction extends SelectionListenerAction {
    public SetDefaultTargetAction(Shell shell) {
        super("Set As Build"); 

        setToolTipText("");
        //MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_DELETE); //$NON-NLS-1$
    }

    @Override
    public void run() {
        if( !canSetDefault() )
        	return;
        
        Object m = getStructuredSelection().getFirstElement();
        if( m instanceof IMakeTargetMorpheable ){
        	((IMakeTargetMorpheable)m).getTargets().setSelectedTarget( (IMakeTargetMorpheable)m );
        }
        else if( m instanceof IProject ){
           	IProjectMakeTargets targets = TinyOSPlugin.getDefault().getTargetManager().getProjectTargets( (IProject)m );
           	targets.setSelectedTarget( targets.getDefaults() );
        }
    }

    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
        return super.updateSelection(selection) && canSetDefault();
    }

    private boolean canSetDefault() {
    	IStructuredSelection selection = getStructuredSelection();
    	if( selection.size() != 1 )
    		return false;
    	
        Object m = selection.getFirstElement();
                
	    if( m instanceof IMakeTargetMorpheable ){
	    	return ((IMakeTargetMorpheable)m).getTargets().getSelectedTarget() != m;
	    }
	    if( m instanceof IProject ){
	    	IProjectMakeTargets targets = TinyOSPlugin.getDefault().getTargetManager().getProjectTargets( (IProject)m );
	    	return targets.getDefaults() != targets.getSelectedTarget();
	    }
	    return false;
    }

}
