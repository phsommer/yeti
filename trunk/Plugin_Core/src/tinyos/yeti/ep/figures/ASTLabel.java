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

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;

import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.utility.Icon;
import tinyos.yeti.views.cgraph.HoverManager;

/**
 * A {@link Label} implementing {@link IASTFigure} but not doing anything
 * in its additional methods
 * @author Benjamin Sigg
 *
 */
public class ASTLabel extends DragableFigure implements IASTFigure, IRepresentation{
    private IASTModelPath path;
	
	private Label titleLabel;
    
    public ASTLabel( HoverManager hover, IASTModelNode node ){
    	titleLabel = new Label();

        BorderLayout layout = new BorderLayout();
        layout.setHorizontalSpacing( 5 );
        layout.setVerticalSpacing( 0 );

        setLayoutManager(layout);
        setBackgroundColor(ColorConstants.white);
        setOpaque(true);

        titleLabel.setTextAlignment( PositionConstants.RIGHT );
        titleLabel.setLabelAlignment( PositionConstants.LEFT );
        titleLabel.setIconTextGap( 5 );

        add(titleLabel, BorderLayout.CENTER);
        
        if( node != null ){
        	CommentHover tooltip = new CommentHover( hover, node );
        	tooltip.connectTo( titleLabel );
        	tooltip.connectTo( this );
        	
        	IFileRegion region = node.getRegion();
        	if( region != null ){
        		OpenButton open = new OpenButton( region );
        		add( open, BorderLayout.RIGHT );
        	}
        }
    	
        setHighlighted( Highlight.NONE, null );
    }
    
    public void setText( String text ){
    	titleLabel.setText( text );
    }
    
    public void setIcon( Icon icon ){
    	titleLabel.setIcon( icon == null ? null : icon.getImage( true ) );
    }
    
    @Override
	public void collapseAST() {
        // ignore
    }

    @Override
	public void expandAST( int depth, IExpandCallback callback ) {
    	if( callback != null )
    		callback.expanded( this );
    }

    @Override
	public void layoutAST() {
        // ignore
    }
    
    public void setPath( IASTModelPath path ) {
        this.path = path;
    }
    
    @Override
	public IASTModelPath[] getPaths() {
        if( path == null )
            return null;
        return new IASTModelPath[]{ path };
    }

    @Override
	public void setHighlighted( Highlight highlighted, IASTModelPath path ){
        setForegroundColor( ASTFigureDefaults.color( highlighted ) );
    }
}
