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
package tinyos.yeti.editors.quickfixer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.markerresolutions.InsertTextResolution;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.ISingleMarkerResolution;
import tinyos.yeti.ep.fix.ISingleQuickFixer;
import tinyos.yeti.nesc.parser.language.elements.ImplementationElement;
import tinyos.yeti.nesc.parser.language.elements.ModuleElement;

/**
 * The quickfixer for the original plugin. This class is only used as
 * backup when no parser is plugged in.
 */
public class QuickFixer implements ISingleQuickFixer{
    public static final String EXPECTED = TinyOSPlugin.PLUGIN_ID + ".expected";
    public static final String OFFSET = TinyOSPlugin.PLUGIN_ID + ".offset";
    public static final String LENGTH = TinyOSPlugin.PLUGIN_ID + ".length";

    public static final String SEMANTICS =  TinyOSPlugin.PLUGIN_ID + ".SEMANTIC_ID"; 

    public static final String MODULE = TinyOSPlugin.PLUGIN_ID + ".module";
    public final static int MODULE_INTERFACEFUNCTION_NOT_IMPL = 1; 

    public ISingleMarkerResolution[] getResolutions( IMarker marker, IParseFile parseFile, ProjectTOS project ) {
        List<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();
        try{
            String[] expected = (String[])marker.getAttribute( EXPECTED );
            if( expected != null ){
                for( String expect : expected ){
                    if( expect.equals("IMPLEMENTATION")) {
                        int location = ((Number)marker.getAttribute( OFFSET )).intValue() +
                        ((Number)marker.getAttribute( LENGTH )).intValue();

                        resolutions.add(
                                new InsertTextResolution(
                                        "Add Implementation Skeleton",
                                        location,
                                        ImplementationElement.SKELETON));
                    }
                }
            }

            int type = marker.getAttribute( SEMANTICS, -1 );
            switch (type) {
                case MODULE_INTERFACEFUNCTION_NOT_IMPL:
                    ModuleElement m = (ModuleElement)marker.getAttribute( MODULE );
                    if( m!=null ){
                        IMarkerResolution[] ideas = m.getResolution();
                        if( ideas == null )
                            return null;
                        
                        ISingleMarkerResolution[] result = new ISingleMarkerResolution[ ideas.length ];
                        for( int i = 0, n = ideas.length; i<n; i++ )
                            result[i] = new SingleMarkerResolution( ideas[i] );
                        return result;
                    }
            }
        }
        catch( CoreException ex ){
            ex.printStackTrace();
        }
        return resolutions.toArray( new ISingleMarkerResolution[ resolutions.size() ] );
    }
}
