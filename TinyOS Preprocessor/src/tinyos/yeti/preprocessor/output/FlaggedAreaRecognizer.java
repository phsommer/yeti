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
package tinyos.yeti.preprocessor.output;

import java.util.ListIterator;

import tinyos.yeti.preprocessor.lexer.InclusionPath;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;

/**
 * A flagged area recognizer checks the path of a {@link PreprocessorToken}
 * and can tell the client whether a certain area lies completely within the
 * flagged area.
 * @author Benjamin Sigg
 */
public class FlaggedAreaRecognizer {
    private PreprocessorToken token;
    private ListIterator<PreprocessorToken> iterator;

    /** where the current area begins */
    private int begin = 0;
    /** the offset in the current token */
    private int offset = 0;
    
    private boolean include;
    private boolean macro;

    /**
     * Creates a new recognizer. If both <code>include</code> and <code>macro</code> 
     * are <code>true</code>, then only areas that are in an included macro (or a macro
     * included an area) are recognized. At least one of <code>include</code>
     * or <code>macro</code> should be set.
     * @param iterator a list of tokens
     * @param include whether the area is an include area
     * @param macro whether the area is a macro area
     */
    public FlaggedAreaRecognizer( ListIterator<PreprocessorToken> iterator, boolean include, boolean macro ){
        this.iterator = iterator;
        this.include = include;
        this.macro = macro;
        
        if( iterator.hasNext() )
            token = iterator.next();
    }
    
    /**
     * Checks the area between <code>begin</code> and <code>end</code>, tells
     * whether the area is completely within a flagged area or not.
     * @param begin the begin of the range
     * @param end the end of the range
     * @return <code>true</code> if the range is in the flagged area
     */
    public boolean within( int begin, int end ){
        begin( offset );
        return end( end );
    }

    /**
     * Tells this recognizer to be prepared to check the area from
     * {@link #begin(int)} to {@link #end(int)}.
     * @param offset the beginning of the next area to check
     */
    public void begin( int offset ){
        // travel backwards
        if( begin > offset ){
            if( offset < 0 )
                offset = 0;

            begin -= this.offset;
            this.offset = 0;

            while( begin > offset ){
                if( iterator.hasPrevious() ){
                    token = iterator.previous();
                    String text = token.getText();
                    if( text != null ){
                        begin -= text.length();
                    }
                }
                else{
                    begin = 0;
                    break;
                }
            }
        }

        // advance until begin == offset
        while( begin < offset && token != null ){
            String text = token.getText();
            if( text != null ){
                int length = text.length() - this.offset;
                if( begin + length <= offset ){
                    begin += length;
                    this.offset = 0;
                    if( iterator.hasNext() )
                        token = iterator.next();
                    else
                        token = null;
                }
                else{
                    this.offset += offset - begin;
                    begin = offset;
                    break;
                }
            }
            else if( iterator.hasNext() )
                token = iterator.next();
            else
                token = null;
        }
    }

    private boolean check( InclusionPath path ){
        if( path == null )
            return false;
        
        if( include && !path.include() )
            return false;
        
        if( macro && !path.macro() )
            return false;
        
        return true;
    }
    
    /**
     * Checks whether the area between {@link #begin(int)} and <code>offset</code>
     * is completely within a flagged area.
     * @param offset the end of the range
     * @return <code>true</code> if the complete range was within the flagged area
     */
    public boolean end( int offset ){
        if( offset < begin )
            throw new IllegalArgumentException( "can't travel back" );

        if( begin == offset ){
            if( token == null )
                return true;

            return check( token.getPath() );
        }

        while( begin < offset && token != null ){
            if( !check( token.getPath() ) )
                return false;

            String text = token.getText();
            if( text != null ){
                int length = text.length() - this.offset;
                if( begin + length <= offset ){
                    begin += length;
                    this.offset = 0;
                    if( iterator.hasNext() )
                        token = iterator.next();
                    else
                        token = null;
                }
                else{
                    this.offset += offset - begin;
                    begin = offset;
                    break;
                }
            }
            else if( iterator.hasNext() ){
                token = iterator.next();
            }
            else{
                token = null;
            }
        }            

        return true;
    }
}
