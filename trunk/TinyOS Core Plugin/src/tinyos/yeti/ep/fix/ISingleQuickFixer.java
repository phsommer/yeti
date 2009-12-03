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
import org.eclipse.ui.IMarkerResolution;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IMessage;

/**
 * An {@link ISingleQuickFixer} is used to create new {@link IMarkerResolution}s. The
 * resolutions are created for markers which are the result of an {@link IMessage}.
 * @author Benjamin Sigg
 */
public interface ISingleQuickFixer {
    /**
     * Called when a new set of resolutions is needed.
     * @param marker the marker which contains the contents of the 
     * map {@link IMessage#getQuickfixInfos()}
     * @param parseFile the key of the file in which the marker is
     * @param project the project in which the file is, might be <code>null</code>
     * @return a set of resolutions or <code>null</code>
     */
    public ISingleMarkerResolution[] getResolutions( IMarker marker, IParseFile parseFile, ProjectTOS project );
}
