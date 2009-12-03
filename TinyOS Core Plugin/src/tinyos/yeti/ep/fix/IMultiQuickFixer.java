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

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IMessage;

/**
 * A multi quick fixer is used to generate fixes for several markers at once.
 * This quick fixer is used when clicking onto a marker on the left side of the
 * editor area. Ordinary {@link ISingleQuickFixer}s will be called as well.
 * @author Benjamin Sigg
 */
public interface IMultiQuickFixer{
    /**
     * Called when a new set of resolutions is needed.
     * @param markers the markers which contain the contents of the 
     * maps {@link IMessage#getQuickfixInfos()}
     * @param parseFile the key of the file in which the marker is
     * @param project the project in which the file is, might be <code>null</code>
     * @return a set of resolutions or <code>null</code>
     */
    public IMultiMarkerResolution[] getResolutions( IMarker[] markers, IParseFile parseFile, ProjectTOS project );
}
