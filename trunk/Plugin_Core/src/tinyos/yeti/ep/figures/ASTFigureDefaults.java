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
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.graphics.Color;

import tinyos.yeti.ep.figures.IRepresentation.Highlight;

/**
 * Default values useful for painting figures.
 * @author Benjamin Sigg
 */
public abstract class ASTFigureDefaults{
    public static final Color darkred = new Color( null, 125, 0, 0 );
    
    private ASTFigureDefaults(){
        // ignore
    }
    
    public static LineBorder border( Highlight highlight ){
        Color color = color( highlight );
        
        switch( highlight ){
            case NONE:
            case ON_PATH:
                return new LineBorder( color, 1 );
            case ALTERNATIVE:
            case SELECTED:
                return new LineBorder( color, 2 );
        }
        
        throw new IllegalArgumentException( "highlight null" );
    }
    
    public static Color color( Highlight highlight ){
        switch( highlight ){
            case NONE:
                return ColorConstants.black;
            case SELECTED:
                return ColorConstants.red;
            case ALTERNATIVE:
                return ColorConstants.blue;
            case ON_PATH:
                return darkred;
        }
        
        throw new IllegalArgumentException( "highlight null" );
    }
}
