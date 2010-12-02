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
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.ISingleMarkerResolution;
import tinyos.yeti.ep.parser.INesCAST;

public class SingleQuickfixerResolution extends QuickfixerResolution{
    private ISingleMarkerResolution resolution;
    private IMarker marker;
    
    public SingleQuickfixerResolution( IMarker marker, ISingleMarkerResolution resolution, IResource resource, ProjectTOS project, IParseFile file ){
        super( resource, project, file );
        this.resolution = resolution;
        this.marker = marker;
    }

    @Override
    protected void run( IDocumentMap document, INesCAST ast,
            IParseFile file, ProjectTOS project ){
        resolution.run( marker, ast, document, file, project );
    }

    public String getLabel(){
        return resolution.getLabel();
    }

    public String getDescription(){
        return resolution.getDescription();
    }

    public Image getImage(){
        return resolution.getImage();
    }
    
}
