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
package tinyos.yeti.ep.figures;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import tinyos.yeti.Debug;
import tinyos.yeti.ep.parser.IASTFigure;

/**
 * An {@link IExpandCallback} that waits for a set of {@link IASTFigure}s
 * to be expanded or canceled and then forwards the call to another callback.
 * @author Benjamin Sigg
 *
 */
public class ConvergingExpandCallback implements IExpandCallback{
    private Set<IASTFigure> awaiting;
    private IExpandCallback callback;
    private IASTFigure figure;
    private boolean cancel = false;

    /**
     * Creates a new callback.
     * @param figure the argument for <code>callback</code>
     * @param awaiting the set of {@link IASTFigure}s which should be monitored
     * @param callback the callback to call when the last figure was triggered.
     */
    public ConvergingExpandCallback( IASTFigure figure, Collection<IASTFigure> awaiting, IExpandCallback callback ){
        this.figure = figure;
        this.callback = callback;
        if( awaiting.size() == 0 )
            expanded();
        else
            this.awaiting = new HashSet<IASTFigure>( awaiting );
    }

    public void expanded(IASTFigure figure) {
        awaiting.remove( figure );
        Debug.info( "Remaining: " + awaiting.size() );
        if( awaiting.size() == 0 )
            close();
    }

    public void canceled(IASTFigure figure) {
        awaiting.remove( figure );
        cancel = true;
        if( awaiting.size() == 0 )
            close();
    }

    protected void close(){
        if( cancel )
            canceled();
        else
            expanded();
    }

    protected void expanded(){
        if( callback != null )
            callback.expanded( figure );
    }

    protected void canceled(){
        if( callback != null )
            callback.canceled( figure );
    }
}
