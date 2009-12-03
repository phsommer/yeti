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
package tinyos.yeti.wizards.content;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.nature.MissingNatureException;

/**
 * A tree showing only header files
 * @author Benjamin Sigg
 */
public class HeaderFileTree extends ResourceTree{
    public HeaderFileTree( Composite parent, int style ){
        super( parent, style );
    }

    @Override
    protected IContainer[] getRoots( IProject project ){
    	if( project == null )
    		return new IContainer[]{};
    	
        try{
			return TinyOSPlugin.getDefault().getProjectTOS( project ).getSourceContainers();
		}
		catch( MissingNatureException e ){
			return new IContainer[]{};
		}
    }

    @Override
    protected boolean visible( IResource resource ){
        if( !super.visible( resource ))
            return false;
        
        return resource instanceof IContainer || "h".equals( resource.getFileExtension() );
    }
}