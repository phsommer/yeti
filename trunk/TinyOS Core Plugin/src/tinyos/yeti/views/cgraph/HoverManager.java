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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PopUpHelper;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.ep.parser.IASTFigureFactory;

/**
 * Manages the hovers of one {@link Control} and its {@link IFigure}s, only
 * one hover can be visible at any time. 
 * @author Benjamin Sigg
 */
public class HoverManager{
	private HoverHelper helper;
	private IASTFigureFactory factory;
	
	public HoverManager( IASTFigureFactory factory ){
		this.factory = factory;
	}
	
	public IASTFigureFactory getFactory(){
		return factory;
	}
	
	public void setControl( ComponentGraphView view, FigureCanvas control ){
		if( helper != null )
			throw new IllegalStateException();
		helper = new HoverHelper( view, control );
	}
	
	public void show( Hover hover, IFigure figure, int x, int y ){	
		helper.hide();
		helper.displayToolTipNear( figure, hover.getFigure(), x, y );
	}
	
	public void hide(){
		helper.hide();
	}
	
	private class HoverHelper extends PopUpHelper{
		private IFigure currentTipSource;
		private ComponentGraphView view;
		private FigureCanvas canvas;
		
		public HoverHelper( ComponentGraphView view, FigureCanvas control ){
			super( control, SWT.TOOL | SWT.ON_TOP);
			this.canvas = control;
			this.view = view;
			getShell().setBackground( ColorConstants.tooltipBackground );
			getShell().setForeground( ColorConstants.tooltipForeground );
		}
		
		@Override
		public void hide(){
			super.hide();
			currentTipSource = null;
		}

		/*
		 * Calculates the location where the tooltip will be painted. Returns this as a Point. 
		 * Tooltip will be painted directly below the cursor if possible, otherwise it will be 
		 * painted directly above cursor.
		 */
		private Point computeWindowLocation(IFigure tip, int eventX, int eventY) {
			Rectangle clientArea = control.getDisplay().getClientArea();
			Point preferredLocation = new Point(eventX, eventY + 26);
			
			Dimension tipSize = getLightweightSystem()
				.getRootFigure()
				.getPreferredSize()
				.getExpanded(getShellTrimSize());

			// Adjust location if tip is going to fall outside display
			if (preferredLocation.y + tipSize.height > clientArea.height)  
				preferredLocation.y = eventY - tipSize.height;
			
			if (preferredLocation.x + tipSize.width > clientArea.width)
				preferredLocation.x -= (preferredLocation.x + tipSize.width) - clientArea.width;
			
			return preferredLocation; 
		}

		/**
		 * Sets the LightWeightSystem's contents to the passed tooltip, and displays the tip. The 
		 * tip will be displayed only if the tip source is different than the previously viewed 
		 * tip source. (i.e. The cursor has moved off of the previous tooltip source figure.)
		 * <p>
		 * The tooltip will be painted directly below the cursor if possible, otherwise it will be 
		 * painted directly above cursor.
		 *
		 * @param hoverSource the figure over which the hover event was fired
		 * @param tip the tooltip to be displayed
		 * @param eventX the x coordinate of the hover event
		 * @param eventY the y coordinate of the hover event
		 * @since 2.0
		 */
		public void displayToolTipNear(IFigure hoverSource, IFigure tip, int eventX, int eventY) {
			if (tip != null && hoverSource != currentTipSource) {
				double scale = view.getLayers().getScale();
				
				Point absolute = canvas.toDisplay( (int)(scale*eventX), (int)(scale*eventY) );
				org.eclipse.draw2d.geometry.Point viewport = canvas.getViewport().getViewLocation();
				absolute.x -= viewport.x;
				absolute.y -= viewport.y;
				
				getLightweightSystem().setContents(tip);
				Point displayPoint = computeWindowLocation( tip, absolute.x, absolute.y );
				Dimension shellSize = getLightweightSystem().getRootFigure()
					.getPreferredSize().getExpanded(getShellTrimSize());
				setShellBounds(displayPoint.x, displayPoint.y, shellSize.width, shellSize.height);
				show();
				currentTipSource = hoverSource;
			}
		}
		
		@Override
		protected void hookShellListeners(){
			// nothing	
		}
	}
}
