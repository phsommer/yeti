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

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.ISingleMarkerResolution;
import tinyos.yeti.ep.parser.INesCAST;

public class SingleMarkerResolution implements ISingleMarkerResolution{
    private IMarkerResolution resolution;
    
    public SingleMarkerResolution( IMarkerResolution resolution ){
        this.resolution = resolution;
    }
    
    public String getDescription(){
        if( resolution instanceof IMarkerResolution2 )
            return ((IMarkerResolution2)resolution).getDescription();
        
        return null;
    }

    public Image getImage(){
        if( resolution instanceof IMarkerResolution2 )
            return ((IMarkerResolution2)resolution).getImage();
        
        return null;
    }

    public String getLabel(){
        return resolution.getLabel();
    }

    public void run( IMarker marker, INesCAST ast, IDocumentMap document, IParseFile file, ProjectTOS project ){
        resolution.run( marker );
    }

}
