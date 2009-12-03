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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A figure which can be dragged using the mouse.
 * 
 * @author Roland Schuler
 * @author Benjamin Sigg
 */
public class DragableFigure extends ASTFigure{
    private DragListener listener;
    
    public DragableFigure(){
        this( false );
    }

    public DragableFigure( boolean dragable ){
        setDragable( dragable );
    }

    public void setDragable( boolean dragable ){
        if( dragable ){
            if( listener == null ){
                listener = new DragListener();
                addMouseListener( listener );
                addMouseMotionListener( listener );
            }
        }
        else{
            if( listener != null ){
                removeMouseListener( listener );
                removeMouseMotionListener( listener );
                listener = null;
            }
        }
    }
    
    public boolean isDragable(){
    	return listener != null;
    }
    
    public DragListener getListener(){
		return listener;
	}
    
    public class DragListener extends MouseMotionListener.Stub implements MouseListener{
        private Dimension offset = new Dimension();

        @Override
        public void mouseDragged( MouseEvent event ){
            Rectangle rect = getBounds().getCopy();

            if( getParent() instanceof Figure ){
                rect.x = Math.max( 0, event.x - offset.width - ((Figure)getParent()).getLocation().x );
                rect.y = Math.max( 0, event.y - offset.height - ((Figure)getParent()).getLocation().y );

                getParent().setConstraint( DragableFigure.this,
                        new Rectangle( rect.x, rect.y, -1, -1 ) );
                getParent().revalidate();
            }
        }

        public void mousePressed( MouseEvent event ){
            event.consume();
            offset.width = event.x - getLocation().x;
            offset.height = event.y - getLocation().y;
        }

        public void mouseReleased( MouseEvent event ){
        	offset.width = 0;
            offset.height = 0;
        }

        public void mouseDoubleClicked( MouseEvent event ){
        	offset.width = 0;
            offset.height = 0;
        }
    }
}
