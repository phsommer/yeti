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

import tinyos.yeti.ep.parser.IASTFigure;

/**
 * A callback that forwards its call to another callback but only after the
 * client permits it.
 * @author Benjamin Sigg
 */
public class BarrierExpandCallback implements IExpandCallback{
    private volatile boolean open = false;

    private volatile IASTFigure figure;
    private volatile boolean expanded;
    
    private IExpandCallback callback;
    
    public BarrierExpandCallback( IExpandCallback callback ){
        this.callback = callback;
    }
    
    /**
     * Opens this barrier.
     */
    public synchronized void open(){
        open = true;
        trySend();
    }
    
    public synchronized void expanded( IASTFigure figure ){
        this.figure = figure;
        expanded = true;
        trySend();
    }
    
    public synchronized void canceled( IASTFigure figure ){
        this.figure = figure;
        expanded = false;
        trySend();
    }
    
    private synchronized void trySend(){
        if( open && figure != null ){
            if( expanded )
                callback.expanded( figure );
            else
                callback.canceled( figure );
        }
    }
}
