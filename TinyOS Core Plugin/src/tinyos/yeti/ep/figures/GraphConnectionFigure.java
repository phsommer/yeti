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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MidpointLocator;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTModelPath;

/**
 * A figure used in the {@link GraphFigure} to draw connections between
 * the children of the graph.
 * @author Benjamin Sigg
 */
public class GraphConnectionFigure extends PolylineConnection implements IASTFigure, IRepresentation{
    private Figure label;

    private List<IASTModelPath> paths;
    private List<Label> labels;

    public GraphConnectionFigure( IFigure source, IFigure target, Font font ){
        setFont( font );
        setLineWidth(1);
        setSourceAnchor( new ChopboxAnchor( source ) );
        setTargetAnchor( new ChopboxAnchor( target ) );

        label = new Figure();
        label.setLayoutManager( new ToolbarLayout( false ) );
        label.setFont( font );

        MidpointLocator targetLocator = new MidpointLocator( this, 0 );
        targetLocator.setRelativePosition(PositionConstants.EAST);
        targetLocator.setGap(5);

        add( label, targetLocator);
    }

    public void setTargetDecoration(){
        if( getTargetDecoration() == null ){
            PolygonDecoration decoration = new PolygonDecoration();
            decoration.setTemplate( PolygonDecoration.TRIANGLE_TIP );
            setTargetDecoration( decoration );
        }
    }

    public void setSourceDecoration(){
        if( getSourceDecoration() == null ){
            PolygonDecoration decoration = new PolygonDecoration();
            decoration.setTemplate( PolygonDecoration.TRIANGLE_TIP );
            setSourceDecoration( decoration );
        }
    }

    public void setDotted( boolean dotted ){
        if( dotted ){
            setLineStyle( Graphics.LINE_CUSTOM );
        }
        else
            setLineStyle( Graphics.LINE_SOLID );
    }

    @Override
    protected void outlineShape( Graphics g ){
        if( getLineStyle() == Graphics.LINE_CUSTOM ){
            g.setLineDash( new int[]{ 10, 10 } );
        }

        super.outlineShape( g );
    }

    public void add( IASTModelPath path, String text ){
        if( paths == null ){
            paths = new ArrayList<IASTModelPath>();
            labels = new ArrayList<Label>();
        }

        paths.add( path );

        Label label = new Label();
        label.setFont( getFont() );
        label.setText( text );
        labels.add( label );
        label.setForegroundColor( ColorConstants.black );

        this.label.add( label );
    }

    public IASTModelPath[] getPaths(){
        if( paths == null )
            return null;
        return paths.toArray( new IASTModelPath[ paths.size() ] );
    }

    public void setHighlighted( Highlight highlighted, IASTModelPath path ){
        int index = paths.indexOf( path );
        if( index >= 0 ){
            Label label = labels.get( index );

            Color color = ASTFigureDefaults.color( highlighted );
            label.setForegroundColor( color );
            setForegroundColor( color );
        }
    }

    public void collapseAST() {
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
