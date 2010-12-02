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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.IProjectMakeTargets;


public class MakeLabelProvider extends LabelProvider implements ITableLabelProvider {
    private WorkbenchLabelProvider fLableProvider = new WorkbenchLabelProvider();

    public MakeLabelProvider() {
        // nothing to do
    }
    
    /*
    public MakeLabelProvider(IPath removePrefix) {
        pathPrefix = removePrefix;
    }*/
    
    /**
     * @see ILabelProvider#getImage(Object)
     */
    @Override
    public Image getImage(Object obj) {
        Image image = null;
        if (obj instanceof IMakeTarget) {
            if( ((IMakeTarget)obj).getTargets().getSelectedTarget() == obj ){
                return NesCIcons.icons().get(NesCIcons.ICON_MAKE_TARGET_DEFAULT);	
            }
            else {
                return NesCIcons.icons().get(NesCIcons.ICON_MAKE_TARGET);
            }

        } else if (obj instanceof IProject) {
            return fLableProvider.getImage(obj);
        }
        return image;
    }

    /**
     * @see ILabelProvider#getText(Object)
     */
    @Override
    public String getText(Object obj) {
    	if( obj instanceof IProject ){
    		IProjectMakeTargets targets = TinyOSPlugin.getDefault().getTargetManager().getProjectTargets( (IProject)obj );
    		if( targets.getSelectedTarget() == targets.getDefaults() ){
    			return fLableProvider.getText( obj ) + " (build)";
    		}
    		else{
    			return fLableProvider.getText( obj );
    		}
    	}
    	
        if (obj instanceof IMakeTarget) {
            if( ((IMakeTarget)obj).getTargets().getSelectedTarget() == obj ){
                return ((IMakeTarget) obj).getName() + " (build)";
            } 
            else {
                return ((IMakeTarget) obj).getName();
            }
        } else if (obj instanceof IProject) {
            return fLableProvider.getText(obj);
        }
        return "";
    }

    @Override
    public void dispose() {
        super.dispose();
        fLableProvider.dispose();
    }

    public Image getColumnImage(Object obj, int columnIndex) {
        return columnIndex == 0 ? getImage(obj) : null;
    }

    public String getColumnText(Object obj, int columnIndex) {
        switch (columnIndex) {
            case 0 :
                return getText(obj);
                /*case 1 :
				if (obj instanceof IMakeTarget) {
					if (pathPrefix != null) {
						IPath targetPath = ((IMakeTarget) obj).getProject().getProjectRelativePath();
						if (pathPrefix.isPrefixOf(targetPath)) {
							targetPath = targetPath.removeFirstSegments(pathPrefix.segmentCount());
						}
						if (targetPath.segmentCount() > 0) {
							return targetPath.toString();
						}
					}
				}*/
        }
        return null;
    }
}

