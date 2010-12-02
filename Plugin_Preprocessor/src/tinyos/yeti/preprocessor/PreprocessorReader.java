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
package tinyos.yeti.preprocessor;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.lexer.Symbols;
import tinyos.yeti.preprocessor.output.FlaggedAreaRecognizer;
import tinyos.yeti.preprocessor.output.RangeDescriptonBuilder;
import tinyos.yeti.preprocessor.output.TokenList;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.stream.ElementStream;

/**
 * A {@link Reader} for the output of the {@link Preprocessor}. This 
 * reader can tell for almost all characters it reads, from which file and
 * from which range it was read. The exact location of the characters is
 * not available.
 * @author Benjamin Sigg
 */
public class PreprocessorReader extends Reader{
    private ElementStream stream;
    private PreprocessorToken current;
    private int currentIndex;
    private int offset;

    /** the maximal number of characters this reader will read */
    private int size;
    
    /** 
     * A list that contains all the tokens that were read from stream until now,
     * the first and the last token of this list are from the base file and valid
     * after this reader is finished. 
     */
    private List<PreprocessorToken> tokens;

    /**
     * Optimized list of tokens used to search tokens faster than with an
     * ordinary list.
     */
    private TokenList tokenList;

    private FileInfo baseFileName;
    private int baseFileLength;
    private int baseFileLines;

    /**
     * Creates a new reader
     * @param source the contents of the file <code>filename</code>
     * @param filename the file that was read. A value of <code>null</code> indicates
     * that the location information of this reader will not be needed
     * @param length the number of characters that were in the file <code>filename</code>
     * @param lines the number of lines that were in the file <code>filename</code>
     */
    public PreprocessorReader( PreprocessorElement source, FileInfo filename, int length, int lines ){
        stream = new ElementStream( source, true );

        baseFileName = filename;
        baseFileLength = length;
        baseFileLines = lines;

        if( filename != null ){
            tokens = new ArrayList<PreprocessorToken>();
            tokens.add( new PreprocessorToken( Symbols.WHITESPACE, filename, 1, new int[]{0}, new int[]{0}, "", null ) );
            
            readTokens();
            if( tokens.size() > 1 ){
                current = tokens.get( 1 );
                currentIndex = 1;
            }
            if( current.getKind() == Symbols.EOF )
                current = null;
        }
        else{
            advanceToValidToken();
        }
    }

    private TokenList tokens(){
        if( tokenList == null ){
            tokenList = new TokenList( tokens );
        }
        return tokenList;
    }

    public FileInfo getBaseFileName() {
        return baseFileName;
    }

    public int getBaseFileLength() {
        return baseFileLength;
    }

    public int getBaseFileLines() {
        return baseFileLines;
    }
    
    /**
     * Gets the number of characters this reader has read. If still in reading
     * then this value might be invalid. 
     * @return the size of the output file
     */
    public int getFileLength(){
        return size;
    }

    /**
     * Given a location in the original file, this method tells where the location
     * landed in the output of the preprocessor. If there is more than one answer,
     * just the first one that is found will be returned.
     * @param inputOffset an offset in the input file
     * @return an offset in the preprocessed file or -1 if not found (for example
     * because the input was in a comment)
     */
    public int getPreprocessedOffset( int inputOffset ){
        int offset = 0;
        
        for( PreprocessorToken token : tokens ){
            int[] begin = token.getBegin();
            int[] end = token.getEnd();
            
            String text = token.getText();
            
            if( token.getFile() == baseFileName ){
                if( end != null ){
                    for( int i = 0, n = end.length; i<n; i++ ){
                        if( end[i] == inputOffset ){
                            return offset + i + 1;
                        }
                    }
                }

                if( begin != null ){
                    for( int i = 0, n = end.length; i<n; i++ ){
                        if( begin[i] == inputOffset ){
                            return offset + i;
                        }
                    }
                }
            }
            
            if( text != null )
                offset += text.length();
        }
        
        return -1;
    }
    
    /**
     * Finds the range in which the element <code>location</code> lies. The
     * elements output location does not need to be known.
     * @param location some element
     * @param inclusion if <code>true</code>, then the this method will follow
     * the path(s) of the location to find out where the location was included
     * @return a new range whose left and right output location are set to -1
     */
    public RangeDescription range( PreprocessorElement location, boolean inclusion ) {
        ElementStream stream = new ElementStream( location, true );
        return RangeDescriptonBuilder.build( stream, inclusion );
    }

    /**
     * Creates a description of the range between <code>left</code> and <code>right</code>.
     * If the given range is outside the input file, than the begin or end of the
     * file will be returned.
     * @param left the index of the first character in the range
     * @param right the index of the first character outside the range
     * @param inclusion whether paths that tell that the range was included
     * or part of a macro should be resolved as well
     * @return a description which character within the range came from which
     * file and location
     */
    public RangeDescription range( int left, int right, boolean inclusion ){
        TokenList tokens = tokens();
        return RangeDescriptonBuilder.build( left, right, tokens, inclusion );
    }
    
    /**
     * Given the left and the right side of a range in the input file,
     * this method returns the corresponding {@link RangeDescription}.
     * @param inputLeft the left side of the range
     * @param inputRight the right side of the range
     * @return
     */
    public RangeDescription inputRange( int inputLeft, int inputRight ){
    	RangeDescription range = new RangeDescription( -1, -1 );
    	RangeDescription.Range fine = range.addFine( inputLeft, inputRight, -1, 0, getBaseFileName(), null );
    	RangeDescription.Range root = range.addRough( inputLeft, inputRight, -1, 0, getBaseFileName(), null, new RangeDescription.Range[]{ fine } );
    	range.addRoot( root );
    	return range;
    }
    
    /**
     * Searches the nearest location in the original input file to a given
     * location in the output file.
     *  
     * @param location some location in the output file
     * @param roundUp if there are two possible results, and <code>roundUp</code> is
     * set, then take the higher result
     * @return the nearest location in the base input file
     */
    public int inputLocation( int location, boolean roundUp ){
        if( location < 0 )
            return 0;
        
        TokenList tokens = tokens();
        int index = tokens.getIndexOf( location );

        PreprocessorToken near;
        if( roundUp )
            near = tokens.getNearestRight( index );
        else
            near = tokens.getNearestLeft( index );

        PreprocessorToken token = tokens.getToken( index );

        if( near.hasLocation() ){
            if( near == token ){
                int offset = location - tokens.getOffset( index );
                int[] begin = tokens.getToken( index ).getBegin();
                if( offset < begin.length )
                    return begin[ offset ];
                else
                    return tokens.getToken( index ).getEndLocation();
            }

            if( roundUp )
                return near.getBeginLocation();
            else
                return near.getEndLocation();
        }
        else{
            return 0;
        }
    }

    @Override
    public int read( char[] cbuf, int off, int len ) throws IOException {
        if( len == 0 )
            return 0;

        int next = read();
        if( next == -1 )
            return -1;

        int count = 1;
        cbuf[ off ] = (char)next;
        off++;

        while( count < len ){
            next = read();
            if( next == -1 )
                return count;

            count++;
            cbuf[ off++ ] = (char)next;
        }

        return count;
    }

    @Override
    public int read() throws IOException {
        if( current == null )
            return -1;

        String text = current.getText();
        int next = text.charAt( offset );

        offset++;
        if( offset >= text.length() ){
            offset = 0;
            
            if( tokens == null ){
                advanceToValidToken();
            }
            else{
                currentIndex++;
                if( currentIndex < tokens.size() ){
                    current = tokens.get( currentIndex );
                    if( current.getKind() == Symbols.EOF )
                        current = null;
                }
                else
                    current = null;
            }
        }

        return next;
    }

    private void readTokens(){
        while( advanceToValidToken() );
    }
    
    private boolean advanceToValidToken(){
        PreprocessorToken current;
        
        while( stream.hasNext() ){
            PreprocessorElement element = stream.next();
            current = element.getToken();
            if( current != null ){
                String text = current.getText();
                if( text != null && text.length() > 0 ){
                    size += text.length();
                    if( tokens != null ){
                        tokens.add( current );
                    }
                    return true;
                }
            }
        }

        current = null;
        if( tokens != null ){
            // add eof token
            int length = Math.max( 0, baseFileLength-1 );
            tokens.add( new PreprocessorToken( Symbols.EOF, baseFileName, baseFileLines, new int[]{ length }, new int[]{ length }, "", null ));
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        // nothing to do
    }

    public FlaggedAreaRecognizer queryAreaIncluded(){
        return new FlaggedAreaRecognizer( tokens.listIterator(), true, false );
    }
}
