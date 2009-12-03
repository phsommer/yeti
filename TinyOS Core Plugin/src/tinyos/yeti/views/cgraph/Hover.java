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
package tinyos.yeti.views.cgraph;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;

import tinyos.yeti.ep.figures.DragableFigure;

/**
 * A hover contains an {@link IFigure} which is shown if the mouse
 * hovers over a {@link #connectTo(IFigure) connected} component. At any
 * time there can be only one hover visible.
 * @author Benjamin Sigg
 *
 */
public abstract class Hover{
	private HoverManager manager;
	private MouseMotionListener listener = new MouseMotionListener(){
		public void mouseDragged( MouseEvent me ){
			DragableFigure figure = getDragable( me );
			if( figure != null ){
				figure.getListener().mouseDragged( me );
			}
		}

		public void mouseEntered( MouseEvent me ){
		}

		public void mouseExited( MouseEvent me ){
			hide();
		}

		public void mouseHover( MouseEvent me ){
			Object source = me.getSource();
			if( source instanceof IFigure ){
				show( (IFigure)source, me.x, me.y );
				me.consume();
			}
		}

		public void mouseMoved( MouseEvent me ){
		}		
	};
	
	private MouseListener dragListener = new MouseListener(){
		public void mouseDoubleClicked( MouseEvent me ){
			DragableFigure figure = getDragable( me );
			if( figure != null ){
				figure.getListener().mouseDoubleClicked( me );
			}
		}
		public void mousePressed( MouseEvent me ){
			hide();
			DragableFigure figure = getDragable( me );
			if( figure != null ){
				figure.getListener().mousePressed( me );
			}
		}
		public void mouseReleased( MouseEvent me ){
			DragableFigure figure = getDragable( me );
			if( figure != null ){
				figure.getListener().mouseReleased( me );
			}
		}
	};
	
	private List<IFigure> figures = new ArrayList<IFigure>();
	
	public Hover( HoverManager manager ){
		if( manager == null )
			throw new IllegalArgumentException();
		this.manager = manager;
	}
	
	private DragableFigure getDragable( MouseEvent me ){
		if( me.getSource() instanceof IFigure ){
			IFigure figure = (IFigure)me.getSource();
			if( figure instanceof DragableFigure ){
				if( ((DragableFigure)figure).isDragable() ){
					return null;
				}
			}
			figure = figure.getParent();
			while( figure != null ){
				if( figure instanceof DragableFigure && ((DragableFigure)figure).isDragable() ){
					return (DragableFigure)figure;
				}
				figure = figure.getParent();
			}
		}
		return null;
	}
	public void dispose(){
		hide();
		for( IFigure figure : figures ){
			figure.removeMouseMotionListener( listener );
			figure.removeMouseListener( dragListener );
		}
		figures.clear();
	}
	
	public void connectTo( IFigure figure ){
		figures.add( figure );
		figure.addMouseMotionListener( listener );
		figure.addMouseListener( dragListener );
	}
	
	public void hide(){
		manager.hide();
	}
	
	public void show( IFigure figure, int x, int y ){
		manager.show( this, figure, x, y );
	}
	
	public abstract IFigure getFigure();
}
