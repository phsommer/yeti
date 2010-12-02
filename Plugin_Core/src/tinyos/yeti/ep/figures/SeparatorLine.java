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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A figure that represents a horizontal line.
 * @author Benjamin Sigg
 */
public class SeparatorLine extends Figure{
    
    public SeparatorLine(){
        setForegroundColor( ColorConstants.buttonDarker );
        setOpaque( false );
    }
    
    @Override
    protected void paintFigure( Graphics g ) {
        super.paintFigure( g );
        
        g.setForegroundColor( getForegroundColor() );
        Rectangle bounds = getBounds();
        g.drawLine( bounds.x, bounds.y + bounds.height/2, bounds.x + bounds.width, bounds.y + bounds.height/2 );
    }
    
    @Override
    public Dimension getPreferredSize( int hint, int hint2 ) {
        return new Dimension( hint, 1 );
    }
    
    @Override
    public Dimension getMinimumSize( int hint, int hint2 ) {
        return new Dimension( hint, 1 );
    }
}
