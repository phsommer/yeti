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

import org.eclipse.draw2d.IFigure;

import tinyos.yeti.ep.figures.IExpandCallback;

/**
 * An {@link IFigure} with additional capabilities.
 * @author Benjamin Sigg
 */
public interface IASTFigure extends IFigure{
	
    /**
     * Tries to expand the contents of this figure (optional).
     * @param depth how deep the ast should be expanded, 0 means that only
     * this figure should be expanded, 1 means that the children should be expanded
     * as well, and so on...
     * @param callback must be called when this method finishes its work. If this
     * method uses another thread, then the callback can be used from that other
     * thread. Might be <code>null</code>
     */
    public void expandAST( int depth, IExpandCallback callback );
    
    /**
     * Tries to collapse the contents of this figure (optional).
     */
    public void collapseAST();
    
    /**
     * Tries to layout its children anew such that they look good (optional).
     */
    public void layoutAST();
}
