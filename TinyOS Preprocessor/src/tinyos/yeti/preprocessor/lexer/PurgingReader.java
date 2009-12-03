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
package tinyos.yeti.preprocessor.lexer;

import java.io.IOException;
import java.io.Reader;

import tinyos.yeti.preprocessor.CommentCallback;
import tinyos.yeti.preprocessor.FileInfo;

/**
 * A purging reader does not read comments and continued lines. This reader
 * does recognize Strings and treats them well. This reader also knows how many
 * characters are read from the underlying reader, so it is possible to find
 * out from where a character comes.
 * @author Benjamin Sigg
 */ 

public class PurgingReader extends Reader{
    public interface ReadBase{
        public void increment( int count );
        public int begin();
        public int end();
    }
    
    private Reader base;
    private FileInfo file;
    private State state;
    
    private final char[] NEWLINES = { '\r', '\n', '\u2028', '\u2029', '\u000B', '\u000C', '\u0085' };
    
    /** a list of characters that are already read, but which are not purged */
    private StringBuilder lookahead = new StringBuilder();
    /** the index of the next character out of {@link #lookahead} that will be returned in {@link #read()}*/
    private int lookaheadBegin = 0;
    
    private boolean inString = false;
    
    /** number of characters read from {@link #base} */
    private ReadBase readBase = new ReadBase(){
        private int value = 0;

        public void increment( int count ) {
            value += count;
        }

        public int end() {
            return value;
        }

        public int begin() {
            return value;
        }
    };
    /** number of characters returned by {@link #read()} */
    private int readPurged = 0;
    
    /** tells which character had which original place */
    private int[] indices = new int[100];
    private int readOffset = 0;
    private int indicesOffset = 0;
    private int indicesLength = 0;
    
    /** Stores for each suppressed newline its location */
    private int[] newlines = new int[100];
    private int newlineLength = 0;
    
    private int newlineCacheReadPurged = 0;
    private int newlineCacheCount = 0;
    
    /** instead of a newline, a EOF is returned */
    private boolean newlineAsEOF = false;
    /** whether the last EOF was in reality a replaced newline */
    private boolean lastEOFwasNewline = false;
    /** whether this reader is the top level reader */
    private boolean topLevel = false;
    
    /**
     * Creates a new reader.
     * @param base the source from which this reader gets its characters.
     * @param file information about the file that is read, might be <code>null</code>
     * @param state information about the state of the preprocessor, should not be <code>null</code>
     */
    public PurgingReader( Reader base, FileInfo file, State state ){
        if( base == null )
            throw new IllegalArgumentException( "Base must not be null" );
        
        this.base = base;
        this.file = file;
        this.state = state;
    }
    
    public void setState( State state ){
		this.state = state;
	}
    
    public void setTopLevel( boolean topLevel ){
		this.topLevel = topLevel;
	}
    
    /**
     * Sets how to report newlines. If <code>true</code>, then instead
     * of a newline, an "end of file" marker is returned on {@link #read()}.
     * @param newlineAsEOF <code>true</code> if newline should be replaced
     * by end of file, <code>false</code> otherwise
     */
    public void setNewlineAsEOF( boolean newlineAsEOF ) {
        this.newlineAsEOF = newlineAsEOF;
    }
    
    public boolean wasLastEOFnewline() {
        return lastEOFwasNewline;
    }
    
    /**
     * Gets the index of the character that will be read from the base reader,
     * and that will be returned in the next call to {@link #read()}. 
     * @return the index of the latest character that was read
     */
    public ReadBase getReadBase() {
        return readBase;
    }
    
    public void setReadBase( ReadBase readBase ) {
        this.readBase = readBase;
    }
    
    /**
     * Gets the number of characters which were {@link #read()} from this
     * reader.
     * @return the number of characters
     */
    public int getReadPurged() {
        return readPurged;
    }
    
    /**
     * Tells from where a character was read, looking from the left side.
     * Once this method was called with an argument of value <code>x</code>,
     * it can't be called again with any argument that is less than <code>x</code>.
     * @param readPurged the index of the character in the purged stream
     * @return the index of the character in the base stream
     */
    public int popReadBaseBegin( int readPurged ){
        return popReadBase( readPurged, false );
    }
    
    /**
     * Tells from where a character was read, looking from the right side.
     * Once this method was called with an argument of value <code>x</code>, 
     * it can't be called again with any argument that is less than <code>x-1</code>.
     * @param readPurged the index of the character in the purged stream
     * @return the index of the character in the base stream
     */
    public int popReadBaseEnd( int readPurged ){
        return popReadBase( readPurged, true );
    }
    
    private int popReadBase( int readPurged, boolean lookahead ){
        int index = 2*readPurged-readOffset+indicesOffset;
        if( lookahead )
            index++;
        
        while( index >= indices.length )
            index -= indices.length;
        
        int use = (2*readPurged - readOffset);
        
        if( use < 0 )
            throw new IllegalArgumentException( "Position already recycled: " + readPurged );
        
        indicesOffset += use;
        while( indicesOffset >= indices.length )
            indicesOffset -= indices.length;
        indicesLength -= use;
        readOffset = 2*readPurged;
        
        return indices[ index ];
    }
    
    private void pushReadBase( int readPurged, int readBaseBegin, int readBaseEnd ){
        if( 2*readPurged - readOffset + 1 >= indices.length ){
            int[] temp = new int[ indices.length*2 + 1 ];
            
            int first = Math.min( indices.length-indicesOffset, indicesLength );
            int second = indicesLength-first;
            
            if( first > 0 ){
                System.arraycopy( indices, indicesOffset, temp, 0, first );
            }
            if( second > 0 ){
                System.arraycopy( indices, 0, temp, first, second );
            }
            
            indices = temp;
            indicesOffset = 0;
        }
        
        int index = 2*readPurged-readOffset+indicesOffset;
        if( index >= indices.length )
            index -= indices.length;
        
        indices[ index ] = readBaseBegin;
        index++;
        if( index >= indices.length )
            index -= indices.length;
        indices[ index ] = readBaseEnd;
        
        indicesLength += 2;
    }
    
    /**
     * Gets the number of newlines over which this reader jumped since
     * its start.
     * @param readPurged the current location
     * @return the number of dismissed newlines
     */
    public int getNewlineJumps( int readPurged ) {
        if( newlineCacheReadPurged > readPurged ){
            newlineCacheReadPurged = 0;
            newlineCacheCount = 0;
        }
        
        while( newlineCacheReadPurged < readPurged && newlineCacheCount < newlineLength ){
            newlineCacheReadPurged = newlines[ newlineCacheCount ];
            if( newlineCacheReadPurged < readPurged )
                newlineCacheCount++;
        }
        
        return newlineCacheCount;
    }
    
    private void addNewlineJump( int count ){
        for( int i = 0; i < count; i++ )
            addNewlineJump();
    }
    
    private void addNewlineJump(){
        if( newlineLength >= newlines.length ){
            int[] temp = new int[ newlineLength*2+1 ];
            System.arraycopy( newlines, 0, temp, 0, newlines.length );
            newlines = temp;
        }
        newlines[ newlineLength++ ] = readPurged;
    }
    
    @Override
    public int read( char[] cbuf, int off, int len ) throws IOException {
        int count = 0;
        
        while( len > 0 ){
            int next = read();
            if( next == -1 ){
                return count == 0 ? -1 : count;
            }
            
            cbuf[ off + count ] = (char)next;
            count++;
            len--;
        }
        
        return count;
    }
    
    @Override
    public int read() throws IOException {
        int readBaseBegin = readBase.begin();
        
        int next = next();
        
        if( newlineAsEOF && isNewline( (char)next )){
            next = -1;
            lastEOFwasNewline = true;
        }
        else if( next == -1 ){
            lastEOFwasNewline = false;
        }
        
        pushReadBase( readPurged, readBaseBegin, readBase.end() );
        readPurged++;
        
        return next;
    }
    
    private int next() throws IOException{
        while( true ){
            if( lookaheadBegin < lookahead.length() ){
                // there are characters from the last run, process them before reading new characters
                readBase.increment( 1 );
                return lookahead.charAt( lookaheadBegin++ );
            }
            else{
                boolean specialStringSign = false;
                
                int next = base.read();
                if( next == -1 )
                    return next;

                char c = (char)next;
                if( inString ){
                    if( c == '\\' ){
                        // the next sign is marked. It is either nothing special, a newline to ignore or a " to be part of the string
                        specialStringSign = true;
                    }
                    else if( c == '"' ){
                        // end of string
                        inString = false;
                    }
                    
                    if( !specialStringSign ){
                        // there are surely no special signs in this string, stop the operation
                        readBase.increment( 1 );
                        return c;
                    }
                }

                if( !specialStringSign ){
                    if( c == '"' ){
                        // begin of string
                        inString = true;
                        readBase.increment( 1 );
                        return c;
                    }

                    if( c == '/' ){
                        // could be multi or single line comment
                        restartLookahead();
                        if( !lookahead() ){
                            // end of file reached
                            readBase.increment( 1 );
                            return c;
                        }
                        else if( lookahead.charAt( 0 ) == '/' ){
                            // single line comment
                            restartLookahead();
                            skipSingleLineComment();
                            readBase.increment( 1 );
                            continue;
                        }
                        else if( lookahead.charAt( 0 ) == '*' ){
                            // multi line comment
                            skipMultiLineComment();
                            addNewlineJump( countNewlinesInLookahead() );
                            restartLookahead();

                            lookahead.append( ' ' );
                            readBase.increment( 1 );

                            continue;
                        }

                        // a valid character was found
                        readBase.increment( 1 );
                        return c;
                    }
                }
                
                if( c == '\\' ){
                    // could be a continued line
                    restartLookahead();
                    boolean eof = false;
                    do{
                        eof = !lookahead();
                    }
                    while( !eof && isWhitespace( lookahead.charAt( lookahead.length()-1 )  ));
                    
                    if( !eof ){
                        char newest = lookahead.charAt( lookahead.length()-1 );
                        if( isNewline( newest )){
                            if( newest == '\r' ){
                                if( lookahead() ){
                                    newest = lookahead.charAt( lookahead.length()-1 );
                                    if( newest == '\n' ){
                                        // clear this line
                                        readBase.increment( lookahead.length()+1 ); 
                                        restartLookahead();
                                        addNewlineJump();
                                        //newlineJump++;
                                        continue;
                                    }
                                    else{
                                        // clear this line but remember the last character
                                        readBase.increment( lookahead.length() );
                                        restartLookahead();
                                        lookahead.append( newest );
                                        continue;
                                    }
                                }
                                else{
                                    // clear this line
                                    readBase.increment( lookahead.length()+1 );
                                    restartLookahead();
                                    continue;                                    
                                }
                            }
                            else{
                                // clear this line
                                readBase.increment( lookahead.length()+1 );
                                restartLookahead();
                                addNewlineJump();
                                // newlineJump++;
                                continue;
                            }
                        }
                    }
                }
                
                // no rule was triggered, so c can be returned
                readBase.increment( 1 );
                return c;
            }
        }
    }
    
    private void restartLookahead(){
        lookaheadBegin = 0;
        lookahead.setLength( 0 );
    }
    
    private boolean lookahead() throws IOException{
        int next = base.read();
        if( next == -1 )
            return false;
        
        lookahead.append( (char)next );
        return true;
    }
    
    private int countNewlinesInLookahead(){
        return PreprocessorLexer.countNewlines( lookahead );
    }
    
    private boolean isWhitespace( char c ){
        return Character.isWhitespace( c ) && !isNewline( c );
    }
    
    private boolean isNewline( char c ){
        for( char x : NEWLINES ){
            if( x == c )
                return true;
        }
        return false;
    }
    
    private void skipSingleLineComment() throws IOException{
    	CommentCallback comments = state == null ? null : state.getCommentObserver();
    	
    	int offsetInInput = readBase.begin();
    	StringBuilder comment = comments == null ? null : new StringBuilder( "//" );
    	
        int current = 0;
        
        do{
            current = base.read();
            readBase.increment( 1 );
            if( isNewline( (char)current )){
                restartLookahead();
                lookahead.append( (char)current );
                break;
            }
            if( comment != null ){
            	comment.append( (char)current );
            }
        }while( current != -1 );
        
        if( comments != null ){
        	comments.singleLineComment( offsetInInput, file, comment.toString(), topLevel );
        }
    }
    
    private void skipMultiLineComment() throws IOException{
    	CommentCallback comments = state == null ? null : state.getCommentObserver();
    	
        int offsetInInput = readBase.begin();
        
    	StringBuilder comment = comments == null ? null : new StringBuilder( "/*" );
        
        int current = 0;
        int next = 0;
        do{
            current = next;
            next = base.read();
            readBase.increment( 1 );
            
            if( comment != null ){
            	comment.append( (char)next );
            }
            
            if( isNewline( (char)next ))
                lookahead.append( (char)next );
            
            if( current == '*' && next == '/' ){
                break;
            }
        }while( next != -1 );
        
        if( comments != null ){
        	comments.multiLineComment( offsetInInput, file, comment.toString(), topLevel );
        }
    }
    
    @Override
    public void close() throws IOException {
        if( newlineAsEOF && !wasLastEOFnewline() )
            base.close();
    }
}
