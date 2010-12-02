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
package tinyos.yeti.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Point;

import tinyos.yeti.editors.NesCEditor;

/**
 * A generic template is an easy way to insert complex structures into
 * a document. A template may contain these options:
 * <ul>
 *      <li>${var} inserts a variable named var with text <code>var</code></li>
 *      <li>${var,text} inserts a variable named var with text <code>text</code></li>
 *      <li>Arguments can be written in quotes to allow special signs like a comma</li>
 *      <li>$x gets replaced by x, x will not be seen as special sign</li>
 *      <li>${cursor} marks the future cursor position</li>
 * </ul>
 * @author Benjamin Sigg
 */
public class GenericTemplate {
    private List<Piece> pieces;
    private String template;
    
    /**
     * Creates a new template
     * @param template the content of this template
     */
    public GenericTemplate( String template ){
    	this.template = template;
    }
    
    /**
     * Applies this template to <code>editor</code> on behalf of <code>command</code>. This
     * method will effectively replace whatever <code>command</code> would have
     * done.
     * @param editor the editor into which this template will be inserted
     * @param command the command which gets replaced by this template
     */
    public void apply( NesCEditor editor, DocumentCommand command ){
    	apply( editor.getEditorSourceViewer(), command.offset );
	 	Point selection = getSelection( editor.getDocument(), command.offset );
	 	editor.selectAndReveal( selection );
	 	
	 	command.offset = selection.x;
	 	command.length = 0;
	 	command.text = null;
	 	command.caretOffset = selection.x + selection.y;
	 	command.shiftsCaret = false;
    }
    
    /**
     * Applies this template to <code>editor</code>.
     * @param editor the editor into which this template will be inserted
     * @param offset the location where the template will be inserted
     */
    public void apply( NesCEditor editor, int offset ){
		apply( editor.getEditorSourceViewer(), offset );
	 	Point selection = getSelection( editor.getDocument(), offset );
	 	editor.selectAndReveal( selection );
    }

    /**
     * Inserts this template into <code>viewer</code>.
     * @param viewer a viewer of a document into which the template is inserted
     * @param offset where to insert the template
     */
    public void apply( ITextViewer viewer, int offset ){
    	apply( viewer, offset, offset );
    }
    
    /**
     * Inserts this template into <code>viewer</code>.
     * @param viewer a viewer of a document into which the template is inserted
     * @param offset where to insert the template
     * @param triggerOffset the current location of the cursor, must be
     * greater or equal than <code>offset</code>
     */
    public void apply( ITextViewer viewer, int offset, int triggerOffset ){
        try{
            IDocument document = viewer.getDocument();
            
            ensurePieces();
            LinkedModeModel model = new LinkedModeModel();
            Map<String, LinkedPositionGroup> groups = new HashMap<String, LinkedPositionGroup>();

            StringBuilder text = new StringBuilder();

            int replacementOffset = offset;
            int rank = 0;

            for( Piece piece : pieces ){
                if( piece.text ){
                    text.append( piece.arguments[0] );
                    offset += piece.arguments[0].length();
                }
                else{
                    String var = piece.arguments[0];
                    if( !"cursor".equals( var )){
                        String name = var;
                        if( piece.arguments.length > 1 )
                            name = piece.arguments[1];

                        LinkedPositionGroup group = groups.get( var );
                        if( group == null ){
                            group = new LinkedPositionGroup();
                            groups.put( var, group );
                        }

                        LinkedPosition position = new LinkedPosition( document, offset, name.length(), rank++ );
                        group.addPosition( position );

                        text.append( name );
                        offset += name.length();
                    }
                }
            }

            for( LinkedPositionGroup group : groups.values() ){
                model.addGroup( group );
            }

            document.replace( replacementOffset, triggerOffset - replacementOffset, text.toString() );

            if( groups.size() > 0 ){
                model.forceInstall();

                LinkedModeUI ui= new LinkedModeUI( model, viewer );
                ui.setExitPosition( viewer, replacementOffset + text.length(), 0, Integer.MAX_VALUE );
                ui.enter();
            }
        }
        catch( BadLocationException ex ){
            ex.printStackTrace();
        }
    }
    
    /**
     * The region that should be selected after this template has been
     * applied.
     * @param document the document into which this template was inserted 
     * @param offset where this template was inserted
     * @return the selection
     */
    public Point getSelection( IDocument document, int offset ){
        ensurePieces();
        
        int selectionOffset = offset;
        
        // search for {cursor}
        for( Piece piece : pieces ){
            if( piece.text ){
                selectionOffset += piece.arguments[0].length();
            }
            else{
                if( "cursor".equals( piece.arguments[0] )){
                    return new Point( selectionOffset, 0 );
                }

                if( piece.arguments.length > 1 )
                    selectionOffset += piece.arguments[1].length();
                else
                    selectionOffset += piece.arguments[0].length();
            }
        }

        // nothing, now just select the first one
        selectionOffset = offset;
        
        for( Piece piece : pieces ){
            if( piece.text ){
                selectionOffset += piece.arguments[0].length();
            }
            else{
                int length;
                
                if( piece.arguments.length > 1 )
                    length = piece.arguments[1].length();
                else
                    length = piece.arguments[0].length();
                
                return new Point( selectionOffset, length );
            }
        }
        
        return new Point( selectionOffset, 0 );
    }
    
    private void ensurePieces(){
        if( pieces == null ){
            pieces = parse( template );
        }
    }

    private List<Piece> parse( String template ){
        StringBuilder builder = new StringBuilder();
        List<Piece> result = new ArrayList<Piece>();

        boolean readDollar = false;
        boolean readText = true;
        boolean quotet = false;

        for( int i = 0, n = template.length(); i<n; i++ ){
            char c = template.charAt( i );
            
            if( c == '$' ){
                if( readDollar ){
                    readDollar = false;
                    builder.append( c );
                    continue;
                }
                else{
                    readDollar = true;
                    continue;
                }
            }
            
            if( c == '"' && !readDollar ){
                quotet = !quotet;
            }
            if( !quotet ){
                if( c == '{' && readText && readDollar ){
                    // begin block
                    if( builder.length() > 0 ){
                        result.add( new Piece( true, builder.toString() ) );
                        builder.setLength( 0 );
                    }

                    readDollar = false;
                    readText = false;
                }

                else if( c == '}' && !readText && !readDollar ){
                    result.add( new Piece( false, split( builder.toString() ) ));
                    builder.setLength( 0 );
    
                    readText = true;
                }
                else{
                    builder.append( c );
                    readDollar = false;
                }
            }
            else{
                builder.append( c );
                readDollar = false;
            }
        }

        if( readText && builder.length() > 0 ){
            result.add( new Piece( true, builder.toString() ) );
        }

        return result;
    }
    
    private String[] split( String text ){
        List<String> list = new ArrayList<String>();
        int offset = 0;
        int length = 0;

        boolean quoted = false;
        boolean skip = false;

        for( int i = 0, n = text.length(); i<n; i++ ){
            char c = text.charAt( i );
            if( quoted ){
                if( c == '"' ){
                    quoted = false;
                    skip = true;
                }
                else{
                    length++;
                }
            }
            else{
                if( c == '"' ){
                    quoted = true;
                    offset = i+1;
                    length = 0;
                }
                else if( c == ',' ){
                    list.add( text.substring( offset, offset+length ) );
                    skip = false;
                    offset = i+1;
                    length = 0;
                }
                else if( !skip ){
                    length++;
                }
            }
        }

        list.add( text.substring( offset, offset+length ) );
        return list.toArray( new String[ list.size() ] );
    }
    
    

    @Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pieces == null) ? 0 : pieces.hashCode());
		result = prime * result
				+ ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override
	public boolean equals( Object obj ){
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		GenericTemplate other = (GenericTemplate)obj;
		if( pieces == null ){
			if( other.pieces != null )
				return false;
		}
		else if( !pieces.equals( other.pieces ) )
			return false;
		if( template == null ){
			if( other.template != null )
				return false;
		}
		else if( !template.equals( other.template ) )
			return false;
		return true;
	}



	private static class Piece{
        public final boolean text;
        public final String[] arguments;

        public Piece( boolean text, String... arguments ){
            this.text = text;
            this.arguments = arguments;
        }

        @Override
        public String toString(){
            if( text ){
                return Arrays.toString( arguments );
            }
            else{
                return "${" + Arrays.toString( arguments ) + "}";
            }
        }

		@Override
		public int hashCode(){
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode( arguments );
			result = prime * result + (text ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals( Object obj ){
			if( this == obj )
				return true;
			if( obj == null )
				return false;
			if( getClass() != obj.getClass() )
				return false;
			Piece other = (Piece)obj;
			if( !Arrays.equals( arguments, other.arguments ) )
				return false;
			if( text != other.text )
				return false;
			return true;
		}
    }
}
