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

import org.eclipse.draw2d.Figure;

import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTModelPath;

/**
 * A {@link Figure} that implements {@link IASTFigure} put whose additional
 * methods from {@link IASTFigure} do nothing.
 * @author Benjamin Sigg
 */
public class ASTFigure extends Figure implements IASTFigure, IRepresentation{
    private IASTModelPath[] paths;
    
    public void collapseAST() {
        // ignore
    }

    public void setPaths( IASTModelPath... paths ){
    	for( IASTModelPath path : paths ){
    		if( path == null )
    			throw new IllegalArgumentException( "paths must not be null" );
    	}
        this.paths = paths;
    }
    
    public void setPaths( Collection<IASTModelPath> paths ){
        if( paths == null )
            this.paths = null;
        else
            this.paths = paths.toArray( new IASTModelPath[ paths.size() ] );
    }
    
    public IASTModelPath[] getPaths(){
        return paths;
    }
    
    public void setHighlighted( Highlight highlighted, IASTModelPath path ){
        // ignore
    }
    
    public void expandAST( int depth, IExpandCallback callback ) {
        if( callback != null )
            callback.expanded( this );
    }

    public void layoutAST() {
        // ignore
    }
}
