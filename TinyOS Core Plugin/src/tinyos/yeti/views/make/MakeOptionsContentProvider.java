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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.IMakeTargetListener;
import tinyos.yeti.make.MakeTargetEvent;
import tinyos.yeti.make.MakeTargetManager;


public class MakeOptionsContentProvider implements ITreeContentProvider, IMakeTargetListener {
    private StructuredViewer viewer;

    public Object[] getChildren(Object obj) {
        //System.out.println(obj.getClass());
        if( obj instanceof MakeTargetManager ){	
            return ((MakeTargetManager)obj).getTargetBuilderProjects();
        }
        else if ( obj instanceof IProject ){
            return TinyOSPlugin.getDefault().getTargetManager().getTargets( (IProject)obj );
        }
        return new Object[]{};
    }

    public Object getParent(Object obj) {
        if( obj instanceof IMakeTarget ){
            return ((IMakeTarget)obj).getProject();
        }
        else if( obj instanceof IFolder ){
            return null;
        }
        else if( obj instanceof IContainer ){
            return ((IContainer)obj).getParent();
        }
        return null;
    }

    public boolean hasChildren(Object obj) {
        return getChildren( obj ).length > 0;
    }

    /**
     * getElements is called to obtain the tree viewer's root elements
     */
    public Object[] getElements(Object obj) {
        return getChildren(obj);
    }

    public void dispose() {
        if (viewer != null) {
            TinyOSPlugin.getDefault().getTargetManager().removeListener(this);
        }
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        if (this.viewer == null) {
            TinyOSPlugin.getDefault().getTargetManager().addListener(this);
        }
        this.viewer = (StructuredViewer) viewer;
        
        if( oldInput instanceof MakeTargetManager ){
            ((MakeTargetManager)oldInput).removeListener( this );
        }
        if( newInput instanceof MakeTargetManager ){
            ((MakeTargetManager)newInput).addListener( this );
        }
        
        /*
        IWorkspace oldWorkspace = null;
        IWorkspace newWorkspace = null;
        if (oldInput instanceof IWorkspace) {
            oldWorkspace = (IWorkspace) oldInput;
        } else if (oldInput instanceof IContainer) {
            oldWorkspace = ((IContainer) oldInput).getWorkspace();
        }
        if (newInput instanceof IWorkspace) {
            newWorkspace = (IWorkspace) newInput;
        } else if (newInput instanceof IContainer) {
            newWorkspace = ((IContainer) newInput).getWorkspace();
        }
        
        if (oldWorkspace != newWorkspace) {
            if (oldWorkspace != null) {
                oldWorkspace.removeResourceChangeListener(this);
            }
            if (newWorkspace != null) {
                newWorkspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
            }
        }
        */
    }

    public void targetChanged(final MakeTargetEvent event) {
        final Control ctrl = viewer.getControl();
        if (ctrl != null && !ctrl.isDisposed()) {
        	ctrl.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (ctrl != null && !ctrl.isDisposed()) {
                        syncTargetChange( event );
                    }
                }
            });
        }
    }
    
    protected void syncTargetChange( MakeTargetEvent event ){
    	switch( event.getType() ){
            case MakeTargetEvent.PROJECT_ADDED:
            case MakeTargetEvent.PROJECT_REMOVED:
            case MakeTargetEvent.PROJECT_REFRESH:
            	viewer.refresh();
                break;
            case MakeTargetEvent.TARGET_ADD:
            case MakeTargetEvent.TARGET_CHANGED:
            case MakeTargetEvent.TARGET_REMOVED:
            case MakeTargetEvent.SELECTED_TARGET_CHANGED:
                viewer.refresh(event.getProject());
                break;
    	}
    }
}