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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionListenerAction;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.jobs.InvokeMakeJob;
import tinyos.yeti.launch.LaunchManager;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public class BuildTargetAction extends SelectionListenerAction {
    public BuildTargetAction(){
        super("Execute Make");
    
        setToolTipText("Execute Make");
        setDisabledImageDescriptor(NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_MAKE_BUILD_DISABLED));
        setImageDescriptor(NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_MAKE_BUILD));
    }

    @Override
    public void run() {
        if( !canBuild() )
            return;
        
        final List<IMakeTarget> selection = getSelectedElements();
        Job run = new Job( "Build" ){
        	@Override
        	protected IStatus run( IProgressMonitor monitor ){
        		monitor.beginTask( "Build", selection.size()*2 );
        		if( LaunchManager.getDefault().launch( new SubProgressMonitor( monitor, selection.size() ) )){
        			for( IMakeTarget target : selection ){
        				scheduleMake( target );
        				monitor.worked( 1 );
        			}
        			monitor.done();
        			return Status.OK_STATUS;
        		}
        		else{
        			monitor.done();
        			return Status.CANCEL_STATUS;
        		}
        	}
        };
        run.setPriority( Job.INTERACTIVE );
        run.setSystem( true );
        run.schedule();
    }
    
    private void scheduleMake( IMakeTarget target ){
        Job targetJob = new InvokeMakeJob( target );
        targetJob.setPriority( Job.INTERACTIVE );
        targetJob.schedule();
    }
    
    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
        return super.updateSelection(selection) && canBuild();
    }

    private boolean canBuild() {
        List<?> elements = getSelectedElements();
        if (elements.size() > 0) {
            Iterator<?> iterator = elements.iterator();
            while (iterator.hasNext()) {
            	Object next = iterator.next();
            	
                if( !(next instanceof IMakeTarget) && !(next instanceof IProject) ){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private List<IMakeTarget> getSelectedElements() {
    	List<?> list = getStructuredSelection().toList();
    	List<IMakeTarget> result = new ArrayList<IMakeTarget>();
    	
    	for( Object next : list ){
    		if( next instanceof IMakeTarget ){
    			result.add( (IMakeTarget)next );
    		}
    		else if( next instanceof IProject ){
    		 	MakeTargetSkeleton skeleton = TinyOSPlugin.getDefault().getTargetManager().getDefaults( (IProject)next );
    		 	result.add( skeleton.toMakeTarget() );
    		}
    	}
    	
    	return result;
    }
}
