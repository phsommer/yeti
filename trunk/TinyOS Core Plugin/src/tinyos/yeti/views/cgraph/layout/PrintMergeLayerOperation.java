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
package tinyos.yeti.views.cgraph.layout;

import java.util.Iterator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.PrintOperation;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

/**
 * Class to fix the problem, that if more than one connection layer exists
 * only the first was resiszed according to the printer data..
 * 
 * Merges all layers if the source is a layered pane 
 * 
 */
public class PrintMergeLayerOperation extends PrintOperation {

	/**
	 * The default print mode. Prints at 100% scale and tiles horizontally and/or vertically, 
	 * if necessary.
	 */
	public static final int TILE = 1;
	/**
	 * A print mode that scales the printer graphics so that the entire printed image fits on 
	 * one page.
	 */
	public static final int FIT_PAGE = 2;
	/**
	 * A print mode that scales the printer graphics so that the width of the printed image 
	 * fits on one page and tiles vertically, if necessary.
	 */
	public static final int FIT_WIDTH = 3;
	/**
	 * A print mode that scales the printer graphics so that the height of the printed image 
	 * fits on one page and tiles horizontally, if necessary.
	 */
	public static final int FIT_HEIGHT = 4;

	private IFigure printSource;
	private Color oldBGColor;
	private int printMode = TILE;

	/**
	 * Constructor for PrintFigureOperation.
	 * <p>
	 * Note: Descendants must call setPrintSource(IFigure) to set the IFigure that is to be 
	 * printed.
	 * @see org.eclipse.draw2d.PrintOperation#PrintOperation(Printer)
	 */
	protected PrintMergeLayerOperation(Printer p) {
		super(p);
	}

	/**
	 * Constructor for PrintFigureOperation.
	 * 
	 * @param p Printer to print on
	 * @param srcFigure Figure to print
	 */
	public PrintMergeLayerOperation(Printer p, IFigure srcFigure) {
		super(p);
		setPrintSource(srcFigure);
	}

	/**
	 * @return SWT.RIGHT_TO_LEFT if the print source is mirrored; SWT.LEFT_TO_RIGHT otherwise
	 * @see org.eclipse.draw2d.PrintOperation#getGraphicsOrientation()
	 */
	int getGraphicsOrientation() {
		return getPrintSource().isMirrored() ? SWT.RIGHT_TO_LEFT : SWT.LEFT_TO_RIGHT;
	}

	/**
	 * Returns the current print mode.  The print mode is one of: {@link #FIT_HEIGHT},
	 * {@link #FIT_PAGE}, or {@link #FIT_WIDTH}.
	 * @return the print mode
	 */
	protected int getPrintMode() {
		return printMode;
	}

	/**
	 * Returns the printSource.
	 * 
	 * @return IFigure The source IFigure
	 */
	protected IFigure getPrintSource() {
		return printSource;
	}

	/**
	 * @see org.eclipse.draw2d.PrintOperation#preparePrintSource()
	 */
	protected void preparePrintSource() {
		oldBGColor = getPrintSource().getLocalBackgroundColor();
		getPrintSource().setBackgroundColor(ColorConstants.white);
	}

	/**
	 * Prints the pages based on the current print mode.
	 * @see org.eclipse.draw2d.PrintOperation#printPages()
	 */
	protected void printPages() {
		Graphics graphics = getFreshPrinterGraphics();
		IFigure figure = getPrintSource();
		setupPrinterGraphicsFor(graphics, figure);
		Rectangle bounds = figure.getBounds();
		int x = bounds.x, y = bounds.y;
		Rectangle clipRect = new Rectangle();
		while (y < bounds.y + bounds.height) {
			while (x < bounds.x + bounds.width) {
				graphics.pushState();
				getPrinter().startPage();
				graphics.translate(-x, -y);
				graphics.getClip(clipRect);
				clipRect.setLocation(x, y);
				graphics.clipRect(clipRect);
				figure.paint(graphics);
				getPrinter().endPage();
				graphics.popState();
				x += clipRect.width;
			}
			x = bounds.x;
			y += clipRect.height;
		}
	}

	/**
	 * @see org.eclipse.draw2d.PrintOperation#restorePrintSource()
	 */
	protected void restorePrintSource() {
		getPrintSource().setBackgroundColor(oldBGColor);
		oldBGColor = null;
	}

	/**
	 * Sets the print mode.  Possible values are {@link #TILE}, {@link #FIT_HEIGHT}, 
	 * {@link #FIT_WIDTH} and {@link #FIT_PAGE}.
	 * @param mode the print mode
	 */
	public void setPrintMode(int mode) {
		printMode = mode;
	}

	/**
	 * Sets the printSource.
	 * @param printSource The printSource to set
	 */
	protected void setPrintSource(IFigure printSource) {
		if (printSource instanceof LayeredPane) {
			LayeredPane lp = (LayeredPane)printSource;
			Iterator iter = lp.getChildren().iterator();
			if (iter.hasNext()) {
				IFigure first = (IFigure) iter.next(); 
				while(iter.hasNext()) {
					IFigure temp = (IFigure) iter.next();
					first.getChildren().addAll(temp.getChildren());
				}
				this.printSource = first;
				return;
			}
		}
		this.printSource = printSource;
		
	}

	/**
	 * Sets up Graphics object for the given IFigure.
	 * @param graphics The Graphics to setup
	 * @param figure The IFigure used to setup graphics
	 */
	protected void setupPrinterGraphicsFor(Graphics graphics, IFigure figure) {
		double dpiScale = (double)getPrinter().getDPI().x / Display.getCurrent().getDPI().x;
		
		Rectangle printRegion = getPrintRegion();
		// put the print region in display coordinates
		printRegion.width /= dpiScale;
		printRegion.height /= dpiScale;
		
		Rectangle bounds = figure.getBounds();
		double xScale = (double)printRegion.width / bounds.width;
		double yScale = (double)printRegion.height / bounds.height;
		switch (getPrintMode()) {
			case FIT_PAGE:
				graphics.scale(Math.min(xScale, yScale) * dpiScale);
				break;
			case FIT_WIDTH:
				graphics.scale(xScale * dpiScale);
				break;
			case FIT_HEIGHT:
				graphics.scale(yScale * dpiScale);
				break;
			default:
				graphics.scale(dpiScale);
		}
		graphics.setForegroundColor(figure.getForegroundColor());
		graphics.setBackgroundColor(figure.getBackgroundColor());
		graphics.setFont(figure.getFont());
	}
}
