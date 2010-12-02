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
package tinyos.yeti.editors.markerresolutions;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import tinyos.yeti.TinyOSPlugin;

public class NesCMarkerUtilities{
    private static final String MESSAGE_KEY = TinyOSPlugin.PLUGIN_ID + ".message.key";

    public static void setMessageKey( Map<String,Object> map, String key ){
        map.put( MESSAGE_KEY, key );
    }
    
    public static void setMessageKey( IMarker marker, String key ){
        try{
            marker.setAttribute( MESSAGE_KEY, key );
        }
        catch ( CoreException e ){
            TinyOSPlugin.getDefault().getLog().log( e.getStatus() );
        }
    }
    
    public static String getMessageKey( IMarker marker ){
        try{
            return (String)marker.getAttribute( MESSAGE_KEY );
        }
        catch ( CoreException e ){
            TinyOSPlugin.getDefault().getLog().log( e.getStatus() );
        }
        return null;
    }
}
