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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.text.BlockFlow;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.editors.nesc.NesCDocPresenter;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.INesCDocComment;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.utility.Icon;
import tinyos.yeti.views.cgraph.Hover;
import tinyos.yeti.views.cgraph.HoverManager;

public class CommentHover extends Hover{
	private Figure tooltip;
	
	public CommentHover( HoverManager manager, IASTModelNode node ){
		this( manager, node, null, null );
	}
	
	public CommentHover( HoverManager manager, IASTModelNode node, Icon nodeIcon, String nodeLabel ){
		super( manager );
		String comment = null;
		
		if( node != null ){
			node = findDocumentation( manager, node );
			
			if( nodeIcon == null )
				nodeIcon = new Icon( node );
		
			if( nodeLabel == null )
				nodeLabel = node.getLabel();
			
			INesCDocComment doc = node.getDocumentation();
			if( doc != null ){
				comment = doc.getComment();
			}
		}
		
		Image icon = null;
		if( nodeIcon != null )
			icon = nodeIcon.getImage( true, true );
		
		Label label = null;
		if( icon != null || nodeLabel != null ){
			label = new Label();
			label.setIcon( icon );
			label.setText( nodeLabel );
			label.setFont( manager.getFactory().getFontTooltipBold() );
			if( comment == null ){
				label.setBorder( new MarginBorder( 2 ) );
			}
		}
		if( comment == null ){
			tooltip = label;
		}
		else{
			NesCDocPresenter presenter = new NesCDocPresenter();
			TextPresentation presentation = new TextPresentation();
			
			comment = presenter.updatePresentation( comment, presentation );
		
			tooltip = new Figure();
			tooltip.setLayoutManager( new GridLayout() );
			if( label != null ){
				tooltip.add( label, new GridData( SWT.BEGINNING, SWT.BEGINNING, false, false ) );
			}
			
			FlowPage page = new FlowPage();
			tooltip.add( page, new GridData( SWT.FILL, SWT.FILL, true, true ) );
			page.setBorder( new MarginBorder( 8, 0, 0, 0 ) );
			
			BlockFlow block = new BlockFlow();
			page.add( block );
			
			List<Position> bolds = boldRanges( presentation );
			
			int offset = 0;
			for( Position bold : bolds ){
				if( offset < bold.offset ){
					int length = bold.offset - offset;
					TextFlow text = new TextFlow();
					block.add( text );
					text.setText( comment.substring( offset, offset+length ) );
					text.setFont( manager.getFactory().getFontTooltip() );
				}
				if( bold.length > 0 ){
					TextFlow text = new TextFlow();
					block.add( text );
					text.setText( comment.substring( bold.offset, bold.offset + bold.length ) );
					text.setFont( manager.getFactory().getFontTooltipBold() );
				}
				offset = bold.offset + bold.length;
			}
			
			if( offset < comment.length()-1 ){
				int length = comment.length()-offset;
				TextFlow text = new TextFlow();
				block.add( text );
				text.setText( comment.substring( offset, offset+length ) );
				text.setFont( manager.getFactory().getFontTooltip() );
			}
		}
	}
	
	/**
	 * Searches an {@link IASTModelNode} which has a documentation and his
	 * related to <code>node</code>.
	 * @param manager to gain more information about the file
	 * @param node the node whose documentation is searched
	 * @return a node which should have documentation, if nothing found: <code>node</code>
	 */
	private IASTModelNode findDocumentation( HoverManager manager, IASTModelNode node ){
		INesCDocComment comment = node.getDocumentation();
		if( comment != null )
			return node;
		
		INesCInspector inspector = manager.getFactory().getInspector();
		if( inspector == null )
			return node;
		
		INesCNode nesc = inspector.getNode( node );
		if( nesc == null )
			return node;
		
		INesCNode[] references = nesc.getReferences( INesCNode.DOCUMENTATION_REFERENCE, inspector );
		if( references == null )
			return node;
		
		List<IASTModelNode> nodes = new ArrayList<IASTModelNode>();
		for( INesCNode reference : references ){
			IASTModelNode referenced = reference.asNode();
			if( referenced != null ){
				nodes.add( referenced );
				comment = referenced.getDocumentation();
				if( comment != null )
					return referenced;
			}
		}
		
		for( IASTModelNode referenced : nodes ){
			IASTModelNode check = findDocumentation( manager, referenced );
			if( check.getDocumentation() != null )
				return check;
		}
		
		return node;
	}
	
	@SuppressWarnings("unchecked")
	private List<Position> boldRanges( TextPresentation presentation ){
		List<Position> result = new ArrayList<Position>();
		
		Iterator<StyleRange> iter = presentation.getAllStyleRangeIterator();
		while( iter.hasNext() ){
			StyleRange range = iter.next();
			if( (range.fontStyle | SWT.BOLD) != 0 ){
				result.add( new Position( range.start, range.length ) );
			}
		}
		
		Collections.sort( result, new Comparator<Position>(){
			public int compare( Position o1, Position o2 ){
				if( o1.offset < o2.offset )
					return -1;
				if( o1.offset > o2.offset )
					return 1;
				return 0;
			}
		});
		
		return result;
	}

	@Override
	public IFigure getFigure(){
		return tooltip;
	}
}


