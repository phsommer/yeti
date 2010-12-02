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

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Toggle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.utility.Icon;
import tinyos.yeti.views.cgraph.HoverManager;

/**
 * A {@link TitleFigure} shows a title, an image an a button. The button 
 * either shows a '+' or a '-'. The button represents the {@link #isExpanded() expanded}
 * state. Clients can add an {@link ActionListener} to this figure to get
 * informed when the expanded state changes. 
 * @author Benjamin Sigg
 */
public class TitleFigure extends DragableFigure implements IRepresentation{
	private static Color titleColor = new Color( null, 200, 200, 255 );
	
    private Label titleLabel;
    private Toggle toggle;

    private boolean expanded = false;
    private boolean toplevel;
    
    public TitleFigure( HoverManager hover, String title, Icon icon, Font font, IASTModelNode node, boolean toplevel ){
        this.toplevel = toplevel;
        
        BorderLayout layout = new BorderLayout();
        layout.setHorizontalSpacing(5);

        setLayoutManager(layout);
        setBackgroundColor( titleColor );
        setOpaque(true);
        setFont( font );

        titleLabel = new Label( title, icon.getImage( true, true ) );
        titleLabel.setTextAlignment(PositionConstants.RIGHT);
        titleLabel.setFont( font );

        add(titleLabel, BorderLayout.CENTER);
        
        Panel buttonPanel = new Panel();
        ToolbarLayout buttonLayout = new ToolbarLayout( true );
        buttonLayout.setSpacing( 2 );
        buttonPanel.setLayoutManager( buttonLayout );
        add( buttonPanel, BorderLayout.RIGHT );
        
        if( !toplevel ){
	        if( expanded ){
	            toggle = new Toggle(new ImageFigure(NesCIcons.icons().get(NesCIcons.ICON_MINUS)));            
	        } 
	        else {
	            toggle = new Toggle(new ImageFigure(NesCIcons.icons().get(NesCIcons.ICON_PLUS)));
	        }
	        toggle.setFont( font );
	        addActionListener(new ActionListener(){
	            public void actionPerformed(ActionEvent event) {
	                setExpanded( !expanded );
	            }
	        });
	
	        buttonPanel.add( toggle );
        }
        
        if( node != null ){
        	CommentHover tooltip = new CommentHover( hover, node );
        	tooltip.connectTo( titleLabel );
        	
        	IFileRegion region = node.getRegion();
        	if( region != null ){
        		OpenButton open = new OpenButton( region );
        		open.setFont( font );
        		buttonPanel.add( open );
        	}
        }
        
        setHighlighted( Highlight.NONE, null );
    }

    public String getTitle(){
        return titleLabel.getText();
    }

    @Override
    public void expandAST( int depth, IExpandCallback callback ) {
        if( !isExpanded() && toggle != null )
            toggle.doClick();

        if( callback != null )
            callback.expanded( this );
    }

    @Override
    public void collapseAST() {
        if( isExpanded() && toggle != null )
            toggle.doClick();
    }

    @Override
    public void setHighlighted( Highlight highlight, IASTModelPath path ){
        Border border = null;

        if( highlight == Highlight.NONE && !toplevel ){
            border = new MarginBorder( 4, 4, 4, 4 );
        }
        else{
            border = ASTFigureDefaults.border( highlight );
        }

        setBorder( border );
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded( boolean expanded ) {
        this.expanded = expanded;
        if( toggle != null ){
	        if( expanded ) {
	            toggle.removeAll();
	            toggle.add(new ImageFigure(NesCIcons.icons().get(NesCIcons.ICON_MINUS)));
	            toggle.setToolTip(new Label("Collapse element"));
	        }
	        else {
	            toggle.removeAll();
	            toggle.add(new ImageFigure(NesCIcons.icons().get(NesCIcons.ICON_PLUS)));
	            toggle.setToolTip(new Label("Expand element"));
	        }
        }
    }

    public void addActionListener( ActionListener listener ){
    	if( toggle != null ){
    		toggle.addActionListener( listener );
    	}
    }

    public void removeActionListener( ActionListener listener ){
    	if( toggle != null ){
    		toggle.removeActionListener( listener );
    	}
    }
}
