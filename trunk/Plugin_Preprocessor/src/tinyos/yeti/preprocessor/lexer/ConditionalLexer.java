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
import java.util.LinkedList;

import java_cup.runtime.Symbol;

/**
 * A lexer which can filter out all elements that are not of the form
 * #if, #ifdef, #ifndef, #elif or #else.
 * @author Benjamin Sigg
 *
 */
public class ConditionalLexer extends StatedLexer{
    private State states;
    /**
     * A list of symbols that has been read prematurely and has now to be
     * returned through {@link #next()} 
     */
    private LinkedList<Symbol> queue = new LinkedList<Symbol>();
    
    private final int STATE_NORMAL  = 0;
    private final int STATE_NEWLINE = 1;
    private final int STATE_SHARP   = 2;
    private final int STATE_IF      = 3;
    private final int STATE_IFDEF   = 4;
    private final int STATE_IFNDEF  = 5;
    private final int STATE_ELIF    = 6;
    private final int STATE_ELSE    = 7;
    private final int STATE_ENDIF   = 8;
    
    /**
     * Whether macro replacement must be disabled for the next identifier
     * that is read.
     */
    private boolean defined = false;
    
    /**
     * Whether the remainder of the current line has to be read. This
     * field is required to handle macros properly because 
     * {@link #isReadingDefinedIdentifier()} must return the correct value when
     * invoked for a symbol that is not swallowed. Since the {@link MacroLexer}
     * does not replace tokens that are swallowed, it is unimportant what
     * {@link #isReadingDefinedIdentifier()} returns for them.
     */
    private boolean readingToEndline = false;
    
    public ConditionalLexer( PreprocessorScanner original ){
        super( original, 1, 9 );
        
        wire( STATE_NORMAL, Symbols.NEWLINE, STATE_NEWLINE );
        
        wire( STATE_NEWLINE, null, STATE_NORMAL );
        wire( STATE_NEWLINE, Symbols.NEWLINE, STATE_NEWLINE );
        wire( STATE_NEWLINE, Symbols.WHITESPACE, STATE_NEWLINE );
        wire( STATE_NEWLINE, Symbols.SHARP, STATE_SHARP );
        
        wire( STATE_SHARP, null, STATE_NORMAL );
        wire( STATE_SHARP, Symbols.NEWLINE, STATE_NEWLINE );
        wire( STATE_SHARP, Symbols.WHITESPACE, STATE_SHARP );
        wire( STATE_SHARP, Symbols.K_IF, STATE_IF );
        wire( STATE_SHARP, Symbols.K_IFDEF, STATE_IFDEF );
        wire( STATE_SHARP, Symbols.K_IFNDEF, STATE_IFNDEF );
        wire( STATE_SHARP, Symbols.K_ELIF, STATE_ELIF );
        wire( STATE_SHARP, Symbols.K_ELSE, STATE_ELSE );
        wire( STATE_SHARP, Symbols.K_ENDIF, STATE_ENDIF );
        
        wire( STATE_IF, null ,STATE_IF );
        wire( STATE_IF, Symbols.NEWLINE, STATE_NEWLINE );
        
        wire( STATE_IFDEF, null, STATE_IFDEF );
        wire( STATE_IFDEF, Symbols.NEWLINE, STATE_NEWLINE );
        
        wire( STATE_IFNDEF, null, STATE_IFNDEF );
        wire( STATE_IFNDEF, Symbols.NEWLINE, STATE_NEWLINE );
        
        wire( STATE_ELIF, null, STATE_ELIF );
        wire( STATE_ELIF, Symbols.NEWLINE, STATE_NEWLINE );
        
        wire( STATE_ELSE, null, STATE_ELSE );
        wire( STATE_ELSE, Symbols.NEWLINE, STATE_NEWLINE );
        
        wire( STATE_ENDIF, null, STATE_ENDIF );
        wire( STATE_ENDIF, Symbols.NEWLINE, STATE_NEWLINE );
    }
    
    /**
     * Tells whether the next identifier that is read should not be
     * replaced by a macro.
     * @return <code>true</code> if the next read identifier should not 
     * be expanded
     */
    public boolean isReadingDefinedIdentifier(){
        int state = getState();
        switch( state ){
            case STATE_IF:
            case STATE_IFDEF:
            case STATE_IFNDEF:
            case STATE_ELIF:
                return defined;
            default:
                return false;
        }
    }
    
    /**
     * Tells whether this scanner is currently reading a line that
     * started with <code>#elif</code>.
     * @return <code>true</code> if an <code>#elif</code> was detected
     */
    public boolean isReadingElifPart(){
    	int state = getState();
    	return state == STATE_ELIF;
    }
    
    public void setStates( State states ) {
        this.states = states;
    }
    
    /**
     * Reads the next symbol that should not be swallowed. Also analyzes what
     * kind of symbol was read and updates the {@link #isReadingDefinedIdentifier() defined}
     * state. The state is set to <code>true</code> if <code>#ifdef</code>, 
     * <code>#ifndef</code> or <code>define</code> was read, in any other
     * case it is set to <code>false</code>. 
     * @return the next symbol or <code>null</code>
     */
    @Override
    public Symbol next() throws IOException {
        Symbol read = read();
                
        if( read == null )
            return null;

        int state = getState();
        switch( state ){
            case STATE_IF:
            case STATE_ELIF:        
                PreprocessorToken token = (PreprocessorToken)read.value;
                switch( token.getKind() ){
                    case IDENTIFIER:
                        defined = false;
                        break;
                    case K_DEFINED:
                        defined = true;
                        break;
                }
                break;
            case STATE_IFDEF:
            case STATE_IFNDEF:
                defined = true;
                break;
            default:
                defined = false;
                break;
        }

        return read;
    }
    
    /**
     * Reads the next symbol that should not be swallowed. The exact search
     * of the next symbol goes as follows:
     * <ol>
     *  <li>If {@link #queue} is not empty, then its head is popped and returned</li>
     *  <li>If tokens should not be ignored ({@link State#shouldIgnore()}) then the next symbol
     *  is returned without further processing it.</li>
     *  <li>If {@link #readingToEndline} has been set, then the next symbol is returned
     *  without further processing it. If the next symbol is a newline-symbol, then
     *  {@link #readingToEndline} is set to <code>false</code></li>
     *  <li>The next directive is searched. If the directive is a 'if', 'else', 'elif', etc.. then
     *  two things can happen:
     *  <ol>
     *   <li>If the directive is not evaluated the {@link #queue} is cleared and the directive swallowed, the {@link State} is informed
     *   about the directive and can update its internal states.</li>
     *   <li>If the directive is a 'elif' and will be evaluated {@link #readingToEndline} is set to <code>true</code> and
     *   {@link #queue} gets cleared.</li>
     *  </ol>
     *  </li>
     * </ol>
     * The methods loops until one of these rules is satisfied.
     * @return the next symbol that is not swallowed or <code>null</code> in case of 
     * end of file.
     * @throws IOException if the file cannot be read
     */
    private Symbol read() throws IOException{
        while( true ){
            if( !queue.isEmpty() )
                return queue.removeFirst();
            
            if( !states.shouldIgnore() )
                return read_token();
            
            if( readingToEndline ){
                Symbol token = read_token();
                if( token != null && token.sym == Symbols.NEWLINE.sym() ){
                    readingToEndline = false;
                }
                return token;
            }
            
            /*
             *  We are in a block of code that would normally be ignored.
             *  We check for if/elif/elise/ifdef/ifndef/endif and keep track
             *  of the nesting of conditionals.
             *  
             *  In case of if,ifdef,ifndef the nesting increases by one
             *  In case of endif the nesting decreases
             *  In case of else,elif the nesting remains, this case will be handled by the parser
             */
            
            boolean done = true;
            int part = -1;
            
            if( !readToFirstSharp() ){
                done = false;
            }
            if( done ){
                if( readToFirstIfElifElse() ){
                    part = getState();
                }   
                else{
                    done = false;
                }
            }
            
            if( done ){
                boolean fillBuffer = done;
                
                if( part == STATE_ELIF ){
                    // maybe the condition needs to be checked
                    if( states.elifpartWillBeEvaluated() ){
                        fillBuffer = false;
                        readingToEndline = true;
                    }
                }
                
                if( fillBuffer ){
                    readToEndline();
                }
            }
            
            if( !done ){
                Symbol last = queue.size() == 0 ? null : queue.removeLast();
                queue.clear();
                if( last != null && last.sym == Symbols.EOF.sym() ){
                    return last;
                }
                else if( last == null )
                    return null;
            }
            else if( part == STATE_IF ){
                queue.clear();
                states.ifpart( null );
            }
            else if( part == STATE_IFDEF ){
                queue.clear();
                states.ifdefpart( null );
            }
            else if( part == STATE_IFNDEF ){
                queue.clear();
                states.ifndefpart( null );
            }
            else{
                return queue.removeFirst();
            }
        }
    }
    
    /**
     * Reads the next token, updating the current {@link #getState() state}
     * but not processing the token otherwise. This is equivalent to
     * calling <code>super.next()</code>.
     * @return the next token
     * @throws IOException if the token cannot be read
     */
    public Symbol read_token() throws IOException {
        return super.next();
    }
    
    /**
     * Reads symbols until a sharp ('#') or the end of file is found.
     * @return <code>true</code> if a sharp was found.
     * @throws IOException if the file cannot be read
     */
    private boolean readToFirstSharp() throws IOException{
        while( true ){
            Symbol next = readNext();
            if( next == null )
                return false;
            
            if( getState() == STATE_SHARP ){
                queue.addLast( next );
                return true;
            }
        }
    }
    
    /**
     * Under the assumption that a sharp ('#') was already found in the
     * current line, this method reads until a 'if', 'ifdef', 'ifndef', 
     * 'elif' or 'else' is found. Any symbol that is read gets stored in 
     * {@link #queue}.
     * @return <code>true</code> if one of the searched tokens was found,
     * <code>false</code> in any other case
     * @throws IOException
     */
    private boolean readToFirstIfElifElse() throws IOException{
        while( true ){
            Symbol next = readNext();
            if( next == null )
                return false;
            
            queue.addLast( next );
            int state = getState();
            if( state == STATE_IF || state == STATE_IFDEF || state == STATE_IFNDEF || state == STATE_ENDIF || state == STATE_ELIF || state == STATE_ELSE )
                return true;
            
            if( state != STATE_SHARP )
                return false;
        }
    }
    
    /**
     * Reads symbols until the end of the line or the end of the file is
     * reached. All read symbols are stored in the {@link #queue}.  
     * @throws IOException if the file cannot be read
     */
    private void readToEndline() throws IOException{
        while( true ){
            Symbol next = readNext();
            if( next == null )
                return;
            
            queue.addLast( next );
            if( getState() == STATE_NEWLINE )
                return;
        }
    }
    
    /**
     * Reads the next symbol without processing it. In case of an end-of-file
     * symbol <code>null</code> is returned and the symbol added to the
     * {@link #queue}.
     * @return the next symbol or <code>null</code> in case of the end of
     * the file.
     * @throws IOException if the file cannot be read
     */
    private Symbol readNext() throws IOException{
        Symbol next = read_token();
        if( next.sym == Symbols.EOF.sym()){
            if( next != null )
                queue.addLast( next );
            return null; 
        }
        return next;
    }
}
