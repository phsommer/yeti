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
package tinyos.yeti.ep.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.INesCAST;

/**
 * Resolves a marker. Usually that results in the deletion of the marker
 * @author Benjamin Sigg
 */
public interface ISingleMarkerResolution{
    /** 
     * Returns a short label indicating what the resolution will do. 
     * @return a short label for this resolution
     */
    public String getLabel();

    /**
     * Gets a description of what this resolution does.
     * @return the description or <code>null</code>
     */
    public String getDescription();
    
    /**
     * Runs this resolution.
     * @param marker the marker to resolve
     * @param ast the abstract syntax tree on which the changes are performed
     * @param document the document which was parsed to get <code>ast</code>
     * @param file which gets updated
     * @param project the project in which <code>file</code> is
     */
    public void run( IMarker marker, INesCAST ast, IDocumentMap document, IParseFile file, ProjectTOS project );
    
    /**
     * Gets a small icon for this resolution
     * @return the icon, can be <code>null</code>
     */
    public Image getImage();
}
