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
package tinyos.yeti.ep.parser;

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * A {@link IASTFigureContent} describes the contents of an element in the
 * graph-view. An {@link IASTFigureContent} is always created by an
 * {@link IASTModelNode}.
 * @author Benjamin Sigg
 */
public interface IASTFigureContent {
    /**
     * Creates a new figure that describes this content.
     * @param factory a factory that can be used to create additional figures
     * @param monitor to inform the user about the state or to cancel the operation
     * @return the new figure
     */
    public IASTFigure createContent( IASTFigureFactory factory, IProgressMonitor monitor );
}
