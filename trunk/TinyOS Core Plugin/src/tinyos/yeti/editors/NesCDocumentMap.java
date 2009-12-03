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
package tinyos.yeti.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;

import tinyos.yeti.editors.nesc.NesCPartitionScanner2;

public class NesCDocumentMap implements IDocumentMap{
    private IDocument document;
    
    private byte[] partitioning;
    
    public NesCDocumentMap( IDocument document ){
        this.document = document;
        
        int length = document.getLength();
        partitioning = new byte[ length ];
        
        NesCPartitionScanner2 scanner = new NesCPartitionScanner2();
        scanner.setRange( document, 0, length );
        
        while( true ){
            IToken token = scanner.nextToken();
            if( token.isEOF() )
                break;
            
            byte type = 0;
            Object data = token.getData();
            if( data == null )
                type = TYPE_DEFAULT;
            else if( INesCPartitions.DEFAULT.equals( data ) || INesCPartitions.PREPROCESSOR_DIRECTIVE.equals( data ))
                type = TYPE_DEFAULT;
            else if( INesCPartitions.MULTI_LINE_COMMENT.equals( data ))
                type = TYPE_MULTI_LINE_COMMENT;
            else if( INesCPartitions.NESC_DOC.equals( data ))
                type = TYPE_NESC_DOC;
            else if( INesCPartitions.NESC_SINGLE_LINE_COMMENT.equals( data ))
                type = TYPE_SINGLE_LINE_COMMENT;
            else if( INesCPartitions.NESC_STRING.equals( data ))
                type = TYPE_STRING;
            
            int tokenOffset = scanner.getTokenOffset();
            int tokenLength = scanner.getTokenLength();
            
            for( int i = 0; i < tokenLength; i++ )
                partitioning[ tokenOffset+i ] = type;
        }
        
        scanner.disconnect();
    }
    
    public IDocument getDocument(){
        return document;
    }
    
    public char getChar( int offset ) throws BadLocationException{
        return document.getChar( offset );
    }
    
    public String get( int offset, int length ) throws BadLocationException{
	    return document.get( offset, length );
    }
    
    public boolean isType( int offset, int flag ) throws BadLocationException{
        if( partitioning.length == 0 )
            return (flag & TYPE_DEFAULT) == TYPE_DEFAULT;
        
        if( offset >= partitioning.length )
            offset = partitioning.length-1;
        
        if( offset < 0 )
            offset = 0;
        
        int type = partitioning[offset];
        return (type & flag) == type;
        /*
        return ( (flag & TYPE_DEFAULT) == TYPE_DEFAULT && (type == null || INesCPartitions.DEFAULT.equals( type ))) ||
                ( (flag & TYPE_MULTI_LINE_COMMENT) == TYPE_MULTI_LINE_COMMENT && INesCPartitions.MULTI_LINE_COMMENT.equals( type )) ||
                ( (flag & TYPE_NESC_DOC) == TYPE_NESC_DOC && INesCPartitions.NESC_DOC.equals( type )) ||
                ( (flag & TYPE_SINGLE_LINE_COMMENT) == TYPE_SINGLE_LINE_COMMENT && INesCPartitions.NESC_SINGLE_LINE_COMMENT.equals( type )) ||
                ( (flag & TYPE_STRING) == TYPE_STRING && INesCPartitions.NESC_STRING.equals( type ));
        */      
    }
    
    public boolean isDefault( int offset ) throws BadLocationException{
        return isType( offset, TYPE_DEFAULT );
    }
    
    public boolean isNesCDoc( int offset ) throws BadLocationException{
        return isType( offset, TYPE_NESC_DOC );
    }
    
    public boolean isMultiLineComment( int offset ) throws BadLocationException{
        return isType( offset, TYPE_MULTI_LINE_COMMENT );
    }
    
    public boolean isSingleLineComment( int offset ) throws BadLocationException{
        return isType( offset, TYPE_SINGLE_LINE_COMMENT );
    }
    
    public boolean isString( int offset ) throws BadLocationException{
        return isType( offset, TYPE_STRING );
    }
    
    public boolean isSource( int offset ) throws BadLocationException{
        return isType( offset, TYPE_SOURCE );
    }
}
