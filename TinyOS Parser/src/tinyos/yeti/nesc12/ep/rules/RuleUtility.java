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
package tinyos.yeti.nesc12.ep.rules;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.editors.nesc.NesCAutoIdentStrategy;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.rules.hyperlink.IHyperlinkRule;
import tinyos.yeti.nesc12.ep.rules.proposals.IProposalRule;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.preprocessor.RangeDescription;

/**
 * Various helper methods to work with {@link IProposalRule}s, 
 * {@link IHyperlinkRule}s other rules.
 * @author Benjamin Sigg
 */
public final class RuleUtility{
    private RuleUtility(){
        // ignore
    }

    public static boolean startsWithIgnoreCase( String string, String prefix ){
        if( prefix.length() > string.length() )
            return false;

        if( prefix.length() == 0 )
            return true;

        return string.substring( 0, prefix.length() ).equalsIgnoreCase( prefix );
    }

    /**
     * Starting at node <code>leaf</code>, this method goes up the tree
     * until a node whose ranges include <code>location</code>. If <code>location</code>
     * is on the border of the range, it is considered not to be in the range.
     * The last node which does not have <code>location</code> in its range is
     * returned.
     * @param location the location to look for
     * @param leaf the starting point, <code>null</code> is valid and will
     * yield a result of <code>null</code>
     * @return the node with the biggest range, not including <code>location</code>
     * but parent of <code>leaf</code> or equal to <code>leaf</code>. <code>null</code>
     * if no satisfactory node was found
     */
    public static ASTNode highestWithout( INesC12Location location, ASTNode leaf ){
        if( leaf == null )
            return null;

        int offset = location.getPreprocessedOffset();
        if( offset < 0 )
            return null;

        Range range = leaf.getRange();
        if( range.getLeft() < offset && offset < range.getRight() ){
            return null;
        }

        ASTNode parent = highestWithout( location, leaf.getParent() );
        if( parent == null )
            return leaf;
        else
            return parent;
    }

    /**
     * Searches the leaf at <code>location</code>
     * @param location where to search for the node
     * @param root the root of the tree to search in, can be <code>null</code> 
     * which will yield a result of <code>null</code>
     * @return the node at <code>location</code> or <code>null</code>
     */
    public static ASTNode nodeAt( INesC12Location location, ASTNode root ){
        if( root == null )
            return null;

        int offset = location.getPreprocessedOffset();
        if( offset < 0 )
            return null;
        
        Range range = root.getRange();
        if( range.getLeft() <= offset && offset < range.getRight() ){
            for( int i = 0, n = root.getChildrenCount(); i<n; i++ ){
                ASTNode check = root.getChild( i );
                check = nodeAt( location, check );
                if( check != null )
                    return check;
            }
            return root;
        }

        return null;
    }

    /**
     * Searches for an {@link Expression} that is a child of <code>root</code>
     * and that is directly before <code>location</code>. 
     * @param location the location to look at
     * @param root the root of the ast (or a subtree)
     * @return the expression or <code>null</code>
     */
    public static Expression expressionsBefore( INesC12Location location, ASTNode root ){
    	ASTNode node = nodeAtOrBefore( location, root );
    	if( node == null )
    		return null;
    	
    	int offset = location.getPreprocessedOffset();
        if( offset < 0 )
            offset = Integer.MAX_VALUE;
        
        while( !(node instanceof Expression )){
    		node = node.getParent();
    		if( node == null )
    			return null;
    		
    		Range range = node.getRange();
    		if( range.getRight() > offset )
    			return null;
    	}
        
        Range range = node.getRange();
        if( range.getRight() > offset )
			return null;
        
        return (Expression)node;
    }
    
    /**
     * 
     * Searches a leaf that either contains <code>location</code> or
     * is directly before <code>location</code>.
     * @param location the location to look at
     * @param root the root of the ast
     * @return the node or <code>root</code>
     */
    public static ASTNode nodeAtOrBefore( INesC12Location location, ASTNode root ){
        if( root == null )
            return null;

        int offset = location.getPreprocessedOffset();
        if( offset < 0 )
            offset = Integer.MAX_VALUE;

        // sweep one: child that contains location
        for( int i = 0, n = root.getChildrenCount(); i<n; i++ ){
            ASTNode child = root.getChild( i );
            Range range = child == null ? null : child.getRange();
            if( range != null ){
                if( range.getLeft() <= offset && offset <= range.getRight() ){
                    ASTNode result = nodeAtOrBefore( location, child );
                    if( result != null )
                        return result;
                }
            }
        }

        // sweep two: child directly before location
        ASTNode nearest = null;
        int dist = Integer.MAX_VALUE;

        for( int i = 0, n = root.getChildrenCount(); i<n; i++ ){
            ASTNode child = root.getChild( i );
            Range range = child == null ? null : child.getRange();
            if( range != null ){
                if( range.getRight() < offset ){
                    int nextDist = offset - range.getRight();
                    if( nextDist < dist ){
                        dist = nextDist;
                        nearest = child;
                    }
                }
            }
        }

        if( nearest != null ){
            ASTNode result = nodeAtOrBefore( location, nearest );
            if( result != null )
                return result;
        }

        return root;
    }

    /**
     * Starting at <code>location</code>, this method reads backwards
     * in <code>document</code> until a non whitespace character or the beginning
     * of the document is found.
     * @param location the location to start reading backwards
     * @param document the document to read in
     * @return the first non whitespace character or -1 if non was found
     */
    public static int reverseWhitespace( INesC12Location location, IDocumentMap document ) throws BadLocationException{
        int offset = location.getInputfileOffset();

        while( offset >= 0 ){
            if( document.isSource( offset )){
                char c = document.getChar( offset );
                if( !Character.isWhitespace( c ))
                    break;
            }

            offset--;
        }

        return offset;
    }

    /**
     * Starting at <code>location</code>, this method reads backwards
     * in <code>document</code> until a non whitespace character or the beginning
     * of the document is found.
     * @param location the location to start reading backwards
     * @param document the document to read in
     * @return the first non whitespace character or -1 if non was found
     */
    public static int reverseWhitespace( int location, IDocumentMap document ) throws BadLocationException{
        while( location >= 0 ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                if( !Character.isWhitespace( c ))
                    break;
            }

            location--;
        }

        return location;
    }

    public static int reverseWhitespace( int location, String document ){
        while( location >= 0 ){
            char c = document.charAt( location );
            if( !Character.isWhitespace( c ))
                break;

            location--;
        }

        return location;
    }

    /**
     * Tells whether the character <code>location</code> or before is a whitespace
     * @param location the character where to start the query
     * @param document the document in which to search
     * @return <code>true</code> if <code>location</code> is a whitespace
     * @throws BadLocationException if <code>location</code> is invalid
     */
    public static boolean previousWhitespace( int location, IDocumentMap document ) throws BadLocationException{
        while( location >= 0 ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                return Character.isWhitespace( c );
            }
        }

        return true;
    }

    /**
     * Reads the first part of the word in which <code>location</code> is.
     * @param location the last character to check
     * @param document the document to search in
     * @return the prefix or <code>null</code>
     * @throws BadLocationException if <code>location</code> is not valid
     */
    public static String prefix( int location, IDocument document ) throws BadLocationException{
        StringBuilder builder = new StringBuilder();
        while( location >= 0 ){
            char c = document.getChar( location );
            if( !Character.isJavaIdentifierPart( c ))
                break;

            builder.append( c );

            location--;
        }

        if( builder.length() == 0 )
            return null;

        builder.reverse();
        return builder.toString();
    }

    /**
     * Reads the first part of the word in which <code>location</code> is.
     * @param location the last character to check
     * @param document the document to search in
     * @return the prefix or <code>null</code>
     * @throws BadLocationException if <code>location</code> is not valid
     */
    public static String prefix( int location, IDocumentMap document ) throws BadLocationException{
        StringBuilder builder = new StringBuilder();
        while( location >= 0 ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                if( !Character.isJavaIdentifierPart( c ))
                    break;

                builder.append( c );
            }
            location--;
        }

        if( builder.length() == 0 )
            return null;

        builder.reverse();
        return builder.toString();
    }
    
    public static int prefixBegin( int location, IDocumentMap document ) throws BadLocationException{
        while( location >= 0 ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                if( !Character.isJavaIdentifierPart( c ))
                    return location+1;
            }
            location--;
        }

        return location;
    }
    
    /**
     * Reads the last part of the word in which <code>location</code> is.
     * @param location the first character to check
     * @param document the document to search in
     * @return the prefix or <code>null</code>
     * @throws BadLocationException if <code>location</code> is not valid
     */
    public static String postfix( int location, IDocumentMap document ) throws BadLocationException{
        StringBuilder builder = new StringBuilder();
        int length = document.getDocument().getLength();
        
        while( location < length ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                if( !Character.isJavaIdentifierPart( c ))
                    break;

                builder.append( c );
            }
            location++;
        }

        if( builder.length() == 0 )
            return null;

        return builder.toString();
    }
    
    public static int postfixEnd( int location, IDocumentMap document ) throws BadLocationException{
        int length = document.getDocument().getLength();
        
        while( location < length ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                if( !Character.isJavaIdentifierPart( c ))
                    return location-1;
            }
            location++;
        }
        
        return location-1;
    }
    
    
    /**
     * Tries to find out which word contains <code>location</code>
     * and returns that word.
     * @param location the location of a character in the document
     * @param document a document to search in
     * @return the word at <code>location</code> or <code>null</code> if
     * nothing was found
     * @throws BadLocationException if <code>location</code> is not valid
     */
    public static String wordAt( int location, IDocumentMap document ) throws BadLocationException{
	    String prefix = prefix( location-1, document );
	    String postfix = postfix( location, document );
	    
	    if( prefix == null && postfix == null )
	    	return null;
	    
	    if( postfix == null )
	    	return prefix;
	    
	    if( prefix == null )
	    	return postfix;
	    
	    return prefix + postfix;
    }
    
    public static IFileRegion regionOfWordAt( IParseFile file, int location, IDocumentMap document ) throws BadLocationException{
    	int begin = prefixBegin( location-1, document );
    	int end = postfixEnd( location, document )+1;
    	
    	return new FileRegion( file, begin, end-begin, document.getDocument().getLineOfOffset( begin ));
    }

    /**
     * Tries to read the word that ends at or before <code>location</code>.
     * @param location the last possible character of the word
     * @param document the document to search in
     * @return the word that was read or <code>null</code>
     * @throws BadLocationException if <code>location</code> is illegal
     */
    public static String reverseWord( int location, IDocumentMap document ) throws BadLocationException{
        location = reverseWhitespace( location, document );
        return prefix( location, document );
    }

    /**
     * Tries to find out where the word before <code>location</code> begins.
     * @param location where to start the search
     * @param document the document to search in
     * @return the location of the first character of the word before <code>location</code>
     * @throws BadLocationException if <code>location</code> is not valid
     */
    public static int reverseWordBegin( int location, IDocumentMap document ) throws BadLocationException {
        location = reverseWhitespace( location, document );
        int result = -1;

        while( location >= 0 ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                if( !Character.isJavaIdentifierPart( c ))
                    break;
                else
                    result = location;
            }
            location--;
        }

        return result;
    }

    /**
     * Reads backwards until a whitespace is found. Returns the offset of the
     * last non-whitespace character that was read. This methods assumes that
     * <code>location</code> is a non whitespace character
     * @param location the current offset
     * @param document the string to scan
     * @return the location of the last non-whitespace character before the
     * previous whitespace
     * @throws BadLocationException if <code>location</code> is not in the document
     */
    public static int reverseNonWhitespace( int location, IDocumentMap document ) throws BadLocationException{
        while( location >= 0 ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                if( Character.isWhitespace( c ))
                    break;
            }
            location--;
        }

        return location+1;
    }

    /**
     * Reads backwards until a whitespace is found. Returns the offset of the
     * last non-whitespace character that was read. This methods assumes that
     * <code>location</code> is a non whitespace character
     * @param location the current offset
     * @param document the string to scan
     * @return the location of the last non-whitespace character before the
     * previous whitespace
     */
    public static int reverseNonWhitespace( int location, String document ){
        while( location >= 0 ){
            char c = document.charAt( location );
            if( Character.isWhitespace( c ))
                break;

            location--;
        }

        return location+1;
    }

    /**
     * Looks whether <code>search</code> is before <code>location</code> in
     * <code>document</code> jumping over any whitespaces.
     * @param location some location in the document
     * @param document the document to search in 
     * @param search the text to look out for
     * @return <code>true</code> if <code>search</code> was found, <code>false</code>
     * otherwise
     * @throws BadLocationException if <code>location</code> is out of boundaries
     */
    public static boolean hasBefore( int location, IDocumentMap document, String search ) throws BadLocationException {
        location = reverseWhitespace( location, document );

        int length = search.length();
        LimitedCharBuffer buffer = new LimitedCharBuffer( length );

        while( location > length && length > 0 ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                if( Character.isWhitespace( c )){
                    return false;
                }
                else{
                    length--;
                    buffer.head( c );
                }
            }
            location--;
        }

        return buffer.equals( search );
    }

    /**
     * Begins a backward search at <code>offset</code>. Jumps over strings
     * and blocks, but stops if a semicolon is discovered or the end of the
     * current block.
     * @param offset the start of the search, may be the character after the last
     * character of <code>search</code>.
     * @param document the document to search
     * @param search the string to search
     * @return <code>true</code> if <code>search</code> was found, <code>false</code> otherwise
     * @throws BadLocationException if <code>offset</code> is illegal
     */
    public static boolean hasBeforeWithinStatement( int offset, IDocumentMap document, String search ) throws BadLocationException {
        LimitedCharBuffer buffer = new LimitedCharBuffer( search.length() );

        while( offset >= 0 ){
            if( document.isDefault( offset )){
                char c = document.getChar( offset );
                if( c == '}' ){
                    buffer.clear();
                    offset = blockBegin( offset-1, document );
                }
                else if( c == '{' || c == ';' ){
                    return false;
                }
                else{
                    buffer.head( c );
                    if( buffer.equals( search ))
                        return true;
                    offset--;
                }
            }
            else{
                offset--;
            }
        }

        return false;
    }

    /**
     * Looks whether <code>search</code> is before <code>location</code> in
     * <code>document</code> jumping over any whitespaces.
     * @param location some location in the document
     * @param document the document to search in 
     * @param search the text to look out for
     * @return the index of the first character of <code>search</code> or -1
     * @throws BadLocationException if <code>location</code> is out of boundaries
     */
    public static int begin( int location, IDocumentMap document, String search ) throws BadLocationException{
        location = reverseWhitespace( location, document );

        int length = search.length();
        LimitedCharBuffer buffer = new LimitedCharBuffer( length );

        while( location > length && length > 0 ){
            if( document.isSource( location )){
                char c = document.getChar( location );
                if( Character.isWhitespace( c )){
                    return -1;
                }
                else{
                    length--;
                    buffer.head( c );
                }
            }
            location--;
        }

        if( buffer.equals( search ) )
            return location + 1;

        return -1;
    }
    
    /**
     * Searches the string <code>search</code> in the range described by
     * <code>offset</code> and <code>length</code> in <code>document</code>
     * and returns that last occurrence of the string.
     * @param offset start of the search
     * @param length how many characters in the search range are
     * @param document the document to search in
     * @param search the string to search
     * @return the last occurrence of <code>search</code> or -1 if not found
     * @throws BadLocationException if <code>offset</code> or <code>length</code> is not legal
     */
    public static int lastIndexOf( int offset, int length, IDocumentMap document, String search ) throws BadLocationException{
        LimitedCharBuffer buffer = new LimitedCharBuffer( search.length() );
        for( int i = offset + length-1; i >= offset; i-- ){
            if( document.isSource( i )){
                buffer.head( document.getChar( i ) );
                if( buffer.equals( search ))
                    return i;
            }
        }

        return -1;
    }

    /**
     * Searches the strings <code>search</code> in the range described by
     * <code>offset</code> and <code>length</code> in <code>document</code>
     * and returns that last occurrence of one of the strings.
     * @param offset start of the search
     * @param length how many characters in the search range are
     * @param document the document to search in
     * @param searchs the strings to search
     * @return the last occurrence of <code>search</code> or -1 if not found
     * @throws BadLocationException if <code>offset</code> or <code>length</code> is not legal
     */
    public static int lastIndexOf( int offset, int length, IDocumentMap document, String... search ) throws BadLocationException{
        int max = 0;
        for( String string : search )
            max = Math.max( max, string.length() );
        
        LimitedCharBuffer buffer = new LimitedCharBuffer( max );
        for( int i = offset + length-1; i >= offset; i-- ){
            if( document.isSource( i )){
                buffer.head( document.getChar( i ) );
                for( String check : search ){
                    if( buffer.startsWith( check ))
                        return i;
                }
            }
        }

        return -1;
    }
    
    /**
     * Tries to find the begin of the next block.
     * @param offset where to start the search
     * @param document the document to search in
     * @return the index of the character '{'
     * @throws BadLocationException if <code>offset</code> is not legal
     */
    public static int nextBlock( int offset, IDocumentMap document ) throws BadLocationException{
        return forwardSearch( offset, document, '{' );
    }

    /**
     * Starting at <code>offset</code>, this method searches for the character sequence
     * <code>search</code>.
     * @param offset where to start the search
     * @param document the document in which to search
     * @param search the sequence to search
     * @return the index of <code>search</code>
     * @throws BadLocationException if <code>offset</code> is not valid
     */
    public static int forwardSearch( int offset, IDocumentMap document, char search ) throws BadLocationException{
        for( int i = offset, n = document.getDocument().getLength(); i<n; i++ ){
            if( document.isSource( i )){
                char c = document.getChar( i );
                if( c == search )
                    return i;
            }
        }

        return -1;
    }


    /**
     * Starting at <code>offset</code>, this method searches for the character sequence
     * <code>search</code>.
     * @param offset where to start the search
     * @param document the document in which to search
     * @param search the sequence to search
     * @param first whether to return the first or the last character of <code>search</code>
     * @return the index of the first or the last character of <code>search</code>, or -1
     * if <code>search</code> was not found
     * @throws BadLocationException if <code>offset</code> is not valid
     */
    public static int forwardSearch( int offset, IDocumentMap document, String search, boolean first ) throws BadLocationException{
        int length = search.length();

        LimitedCharBuffer buffer = new LimitedCharBuffer( length );
        LimitedIntBuffer indices = new LimitedIntBuffer( length );

        for( int i = offset, n = document.getDocument().getLength(); i<n; i++ ){
            if( document.isSource( i )){
                char c = document.getChar( i );

                buffer.tail( c );
                indices.tail( i );

                if( buffer.equals( search )){
                    if( first )
                        return indices.get( 0 );
                    else
                        return i;
                }
            }
        }

        return -1;
    }

    /**
     * Tries to find out where the block begins where <code>offset</code> is in.
     * A block is something like "{...}" where ... might contain other blocks.
     * @param offset some location, may be directly on a '{'.
     * @param document the document to search in
     * @return the location of the open bracket sign or -1 if non was found
     * @throws BadLocationException if <code>offset</code> is illegal
     */
    public static int blockBegin( int offset, IDocumentMap document ) throws BadLocationException{
        int level = 0;
        int result = -1;

        while( offset >= 0 && level >= 0 ){
            if( document.isDefault( offset )){
                char c = document.getChar( offset );
                if( c == '{' ){
                    level--;
                    result = offset;
                }
                else if( c == '}' )
                    level++;
            }

            offset--;
        }

        if( level > 0 )
            return -1;

        return result;
    }

    /**
     * Starting at <code>offset</code>, this methods tries to find the end
     * of the current block.
     * @param offset the first character to consider
     * @param document the document to search in
     * @return the location of the last character in this block or -1
     * @throws BadLocationException if <code>offset</code> is not valid
     */
    public static int blockEnd( int offset, IDocumentMap document ) throws BadLocationException{
        int level = 0;
        int length = document.getDocument().getLength();

        while( offset < length ){
            if( document.isDefault( offset )){
                char c = document.getChar( offset );
                if( c == '{' ){
                    level++;
                }
                else if( c == '}' ){
                    level--;
                    if( level < 0 )
                        return offset;
                }
            }
            offset++;
        }

        return -1;
    }

    /**
     * Gets the tabulator whitespace that will be inserted whenever the user
     * clicks the tabulator-key.
     * @return the inserted whitespaces
     */
    public static String getTab(){
        return NesCAutoIdentStrategy.getTab();
    }

    /**
     * Analyzes the line of <code>offset</code> and returns all whitespaces
     * that make up the beginning of the line.
     * @param offset some location in <code>document</code>
     * @param document the document to analyze
     * @return the begin of the line in which <code>offset</code> is
     * @throws BadLocationException if <code>offset</code> is not legal
     */
    public static String whitespaceLineBegin( int offset, IDocumentMap document ) throws BadLocationException{
        IDocument real = document.getDocument();
        int line = real.getLineOfOffset( offset );
        IRegion lineInfo = real.getLineInformation( line );

        int begin = lineInfo.getOffset();
        int length = 0;
        while( length < lineInfo.getLength() ){
            char c = real.getChar( begin+length );
            if( Character.isWhitespace( c )){
                length++;
            }
            else{
                break;
            }
        }

        return real.get( begin, length );
    }
    
    /**
     * Tries to guess where the element that is described in the roots of
     * <code>range</code> comes from.
     * @param range some range, can be <code>null</code>
     * @return an educated guess where the origin of the described element
     * might be, can be <code>null</code>
     */
    public static FileRegion source( RangeDescription range ){
    	if( range == null )
    		return null;
    
    	RangeDescription.Range result = range.getSource();
    	if( result == null )
    		return null;
    	
    	if( ((NesC12FileInfo)result.file()).getParseFile() == NullParseFile.NULL )
    		return null;
    	
    	return new FileRegion( result );
    }
}
