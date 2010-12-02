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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.jface.viewers.IStructuredSelection;

public final class ResourceUtil{
    private ResourceUtil(){
        // ignore
    }
    
    @SuppressWarnings("unchecked")
    public static IProject getProject( IStructuredSelection selection ){
        Iterator<Object> iterator = selection.iterator();
        while( iterator.hasNext() ){
            Object next = iterator.next();
            if( next instanceof IResource )
                return ((IResource)next).getProject();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static IContainer getContainer( IStructuredSelection selection ){
        Iterator<Object> iterator = selection.iterator();
        while( iterator.hasNext() ){
            Object next = iterator.next();
            if( next instanceof IResource ){
                return getContainer( (IResource)next );
            }
        }
        return null;
    }
    
    public static IContainer getContainer( IResource selection ){
        if( selection instanceof IContainer )
            return (IContainer)selection;
        
        return getContainer( selection.getParent() );
    }
    
    public static IProject[] open( IProject[] projects ){
        List<IProject> result = new ArrayList<IProject>();
        for( IProject project : projects ){
            if( project.isAccessible() )
                result.add( project );
        }
        return result.toArray( new IProject[ result.size() ] );
    }

    /**
     * Gets all the resources that are accessible and not hidden.
     * @param resources a set of resources
     * @return the filtered set of resources
     */
    public static IResource[] resources( IResource[] resources ){
        List<IResource> list = new ArrayList<IResource>();
        for( IResource resource : resources ){
            if( resource.isAccessible() ){
                ResourceAttributes attributes = resource.getResourceAttributes();
                boolean hidden = false;

                if( attributes != null )
                    hidden = attributes.isHidden();

                if( !hidden )
                    list.add( resource );
            }
        }

        return list.toArray( new IResource[ list.size() ] ); 
    }
    
    public static IContainer[] containers( IResource[] resources ){
        List<IContainer> list = new ArrayList<IContainer>();
        for( IResource resource : resources ){
            if( resource instanceof IContainer ){
                IContainer container = (IContainer)resource;
                if( container.isAccessible() ){
                    ResourceAttributes attributes = container.getResourceAttributes();
                    boolean hidden = false;
                    
                    if( attributes != null )
                        hidden = attributes.isHidden();
                    
                    if( !hidden )
                        list.add( (IContainer)resource );
                }
            }
        }
         
        return list.toArray( new IContainer[ list.size() ] );
    }
}
