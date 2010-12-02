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

import tinyos.yeti.preprocessor.FileInfo;

import java_cup.runtime.Symbol;

/**
 * A lexer reading some file, can be modified to temporary read symbols
 * from another source.
 * @author Benjamin Sigg
 */
public class PreprocessorLexer extends Lexer implements PreprocessorScanner{
    private BaseStream stream = new BaseStream();
    private PurgingReader reader;
    
    private int newlineJump = 0;
    private int characterCount = 0;
    
    public PreprocessorLexer( State states, FileInfo file, PurgingReader reader ){
        super( reader );
        setReader( reader );
        this.reader = reader;
        setFile( file );
        setLine( 1 );
        setStates( states );
    }
    
    public void setStates( State states ){
        this.states = states;
        stream.setStates( states );
    }
    
    public Symbol next() throws IOException {
        return next_token();
    }
    
    public PreprocessorLexer getBase() {
        return this;
    }
    
    public int getCharacterCount() {
        return characterCount;
    }
    
    private void count( Symbol symbol ){
    	/* this method is called for *all* elements of the input file */
    	
        int line = getLine();
        
        PreprocessorToken token = (PreprocessorToken)symbol.value;
        
        int nextNewlineJump = reader.getNewlineJumps( tokenbegin() );
        line += nextNewlineJump - newlineJump;
        newlineJump = nextNewlineJump;
        
        String text = token.getText();
        if( text != null ){
            characterCount += text.length();
            
            if( symbol.sym == Symbols.NEWLINE.sym() ){
                // \r|\n|\r\n|\u2028|\u2029|\u000B|\u000C|\u0085
                line += countNewlines( text );
            }
        }
        setLine( line );
    }
    
    public static int countNewlines( CharSequence text ){
        int line = 0;
        
        int i = 0;
        int n = text.length();
        while( i < n ){
            char c = text.charAt( i );
            switch( c ){
                case '\n':
                case '\u2028':
                case '\u2029':
                case '\u000B':
                case '\u000C':
                case '\u0085':
                    line++;
                    break;
                case '\r':
                    if( i+1 < n ){
                        if( text.charAt( i+1 ) == '\n' ){
                            i++;
                        }
                    }
                    line++;
                    break;
            }
            i++;
        }
        
        return line;
    }
    
    public Stream asStream(){
        Stream stream = new Stream(){
            @Override
            protected Symbol next() throws IOException {
                return PreprocessorLexer.this.next();
            }
            @Override
            public void disable() throws IOException {
            	// ignore
            }
            @Override
            public void enable() throws IOException {
            	// ignore
            }
            @Override
            public void popped() throws IOException {
                // ignore
            }
            @Override
            public void pushed() throws IOException {
                // ignore
            }
        };
        stream.setStates( states );
        return stream;
    }
    
    
    
    @Override
    public Symbol next_token() throws IOException {
    	/*try{
    		Stream stream = this.stream;
    		do{
    			stream = this.stream;
    			if( stream != null ){
    				Symbol next = stream.read();
    				if( next == null ){
    					if( stream == this.stream ){
    						stream.popped();
    						this.stream = null;
    						break;
    					}
    				}
    				else{
    					if( this.stream == null ){
    						stream.repushed();
    						this.stream = stream;
    					}

    					count( next );
    					return next;
    				}
    			}
    		}while( stream != this.stream );

    		Symbol next = super.next_token();
    		count( next );
    	}
    	finally{
    		
    	}
        return next;*/
    	
    	return stream.read();
    }
    
    private Symbol nextLexerToken() throws IOException{
    	Symbol next = super.next_token();
    	return next;
    }
    
    public void push( Stream stream ) throws IOException{
    	this.stream.push( stream );
    }
    
    private class BaseStream extends Stream{
    	@Override
    	public Symbol read() throws IOException {
    		Symbol next = super.read();
    		count( next );
    		return next;
    	}
    	
		@Override
		protected Symbol next() throws IOException {
			return nextLexerToken();
		}

		@Override
		public void disable() throws IOException {
			// ignore
		}
		
		@Override
		public void enable() throws IOException {
			// ignore
		}
		
		@Override
		public void popped() throws IOException {
			// ignore			
		}

		@Override
		public void pushed() throws IOException {
			// ignore
		}
    }
}
