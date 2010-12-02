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
package tinyos.yeti.preprocessor.lexer.streams;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java_cup.runtime.Symbol;
import tinyos.yeti.preprocessor.PreprocessorReader;
import tinyos.yeti.preprocessor.lexer.*;
import tinyos.yeti.preprocessor.lexer.Macro.VarArg;
import tinyos.yeti.preprocessor.output.Insights;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.elements.Token;
import tinyos.yeti.preprocessor.parser.stream.ElementStream;

/**
 * A stream writing the tokens sequence of a {@link Macro}, this stream
 * can put in the parameters the {@link Macro} had.
 * @author Benjamin Sigg
 */
public class MacroStream extends Stream{
	private Macro macro;
	private PreprocessorElement identifier;

	private String[] parameters;
	private List<List<Symbol>> arguments;

	private ElementStream stream;
	/** a buffer used for reading the concat operator and its arguments */
	private LinkedList<PreprocessorElement> streamLookaheadBuffer = new LinkedList<PreprocessorElement>();

	private int line;

	private boolean inString = false;

	/**
	 * Creates a new stream.
	 * @param states the state of the preprocessor
	 * @param identifier the element that gets replaced by this stream
	 * @param macro the macro that is to be put in
	 */
	public MacroStream( State states, PreprocessorElement identifier, Macro macro ) throws IOException{
		setStates( states );
		this.identifier = identifier;
		this.macro = macro;

		parameters = macro.getParameters();

		line = identifier.getToken().getLine();

		if( parameters != null )
			arguments = readArguments( parameters.length, macro.getVarArg() );
		else if( macro.getVarArg() != VarArg.NO )
			arguments = readArguments( 0, macro.getVarArg() );
	}

	public PreprocessorElement getIdentifier() {
		return identifier;
	}
	
	public Macro getMacro(){
		return macro;
	}

	@Override
	protected Symbol next() throws IOException{
		boolean sharp = false;
		List<PreprocessorToken> buffer = null;

		while( !streamLookaheadBuffer.isEmpty() || stream.hasNext() ){
			PreprocessorElement next = streamLookaheadBuffer.isEmpty() ? stream.next() : streamLookaheadBuffer.removeFirst();
			if( next.getToken() == null )
				continue;

			if( inString ){
				sharp = false;
				if( next.getToken().getKind() == Symbols.QUOTE )
					inString = false;
			}
			else{
				if( next.getToken().getKind() != Symbols.WHITESPACE && next.getToken().getKind() != Symbols.NEWLINE ){
					boolean concat = tryReadConcat();
					if( concat ){
						concat( next );
						continue;
					}
				}

				switch( next.getToken().getKind() ){
					case SHARP:
						if( buffer == null ){
							buffer = new ArrayList<PreprocessorToken>();
							buffer.add( next.getToken() );
							sharp = true;
						}
						else{
							push( new TokenStream( buffer ));
							return null;
						}

						break;
					case WHITESPACE:
					case NEWLINE:
						if( buffer != null )
							buffer.add( next.getToken() );

						break;
					case IDENTIFIER:
					case K_DEFINE:
					case K_UNDEF:
					case K_INCLUDE:
					case K_IF:
					case K_IFDEF:
					case K_IFNDEF:
					case K_LINE:
					case K_ELIF:
					case K_ENDIF:
					case K_ERROR:
					case K_PRAGMA:
					case K_DEFINED:
						String name = next.getToken().getText();
						Stream arguments = getArguments( name, sharp );

						if( arguments != null ){                            
							if( sharp ){
								push( new StringStream( new TrimStream( arguments )));
							}
							else{
								push( new TrimStream( new ReplacingSymbolStream( arguments )));
							}
							return null;

						}

						Macro macro = states.getMacro( name );
						if( macro != null && !macro.isInProgress() ){
							push( new TrimStream( new MacroStream( states, next, macro ) ));
							return null;
						}
						break;
					case QUOTE:
						inString = true;
					default:
						sharp = false;
				}
			}

			if( !sharp ){
				if( buffer == null )
					return states.symbol( next.getToken() );
				else{
					push( new TokenStream( buffer ));
					return null;
				}
			}
		}

		if( buffer != null )
			push( new TokenStream( buffer ));

		return null;
	}

	public Stream getArguments( String name, boolean stringify ){
		if( arguments == null )
			return null;

		int index = 0;

		if( name.equals( "__VA_ARGS__" ) && macro.getVarArg() == VarArg.YES_UNNAMED ){
			List<Stream> streams = new ArrayList<Stream>();
			int i = 0;
			if( parameters != null )
				i = Math.max( 0, parameters.length-1 );

			for( int n = arguments.size(); i<n; i++ ){
				streams.add( new TrimStream( new SymbolStream( arguments.get( i ), true )) );
			}
			return new ArgumentStream( streams );
		}

		if( parameters != null ){
			for( String id : parameters ){
				if( id.equals( name )){
					if( parameters[ parameters.length-1 ] == id ){
						// last id, might be special
						if( macro.getVarArg() == VarArg.YES_NAMED ){
							List<Stream> streams = new ArrayList<Stream>();
							for( int i = parameters.length-1, n = arguments.size(); i<n; i++ ){
								if( stringify )
									streams.add( new SymbolStream( arguments.get( i ), true ));
								else
									streams.add( new TrimStream( new SymbolStream( arguments.get( i ), true )) );
							}
							return new ArgumentStream( streams );
						}
					}

					return new SymbolStream( arguments.get( index ), true );
				}
				index++;
			}
		}
		return null;
	}

	/**
	 * Tries to read something of the form "   ##   xy" from the stream
	 * into the lookahead buffer.
	 * @return <code>true</code> if a concat operation was read.
	 */
	private boolean tryReadConcat(){
		int phase = 0;
		for( PreprocessorElement next : streamLookaheadBuffer ){
			phase = tryReadConcatPhase( next, phase );
			if( phase == -1 )
				return false;
			if( phase == 3 )
				return true;
		}

		while( stream.hasNext() ){
			PreprocessorElement next = stream.next();
			streamLookaheadBuffer.addLast( next );
			phase = tryReadConcatPhase( next, phase );
			if( phase == -1 )
				return false;
			if( phase == 3 )
				return true;
		}

		return false;
	}

	private void concat( PreprocessorElement first ) throws IOException{
		PreprocessorElement last = streamLookaheadBuffer.getLast();
		streamLookaheadBuffer.clear();

		StringBuilder builder = new StringBuilder();
		ArrayReadBase base = new ArrayReadBase();

		concatAppend( first, builder, base );
		concatAppend( last, builder, base );

		PurgingReader reader = new PurgingReader( new StringReader( builder.toString() ), null, getStates() );
		reader.setReadBase( base );
		PreprocessorLexer lexer = new PreprocessorLexer( states, first.getToken().getFile(), reader );
		lexer.setNoAutoNewlineAtEnd( true );
		lexer.setLine( line );

		Symbol next;
		while( (next=lexer.next()).sym != Symbols.EOF.sym() ){
			streamLookaheadBuffer.add( new Token( (PreprocessorToken)next.value ) );
		}
	}

	private void concatAppend( PreprocessorElement element, StringBuilder builder, ArrayReadBase base ) throws IOException{
		Stream args = getArguments( element.getToken().getText(), false );
		if( args != null ){
			Stream stream = new TrimStream( args );
			Symbol next;
			stream.setStates( states );
			stream.pushed();
			while( (next=stream.read()) != null ){
				builder.append( ((PreprocessorToken)next.value).getText() );
				base.add( (PreprocessorToken)next.value );
			}
			stream.popped();
		}
		else{
			builder.append( element.getToken().getText() );
			base.add( element.getToken() );
		}
	}

	private int tryReadConcatPhase( PreprocessorElement next, int phase ){
		if( next.getToken() != null ){
			switch( next.getToken().getKind() ){
				case CONCAT:
					if( phase == 0 )
						return 1;
					else
						return -1;

				case WHITESPACE:
					if( phase == 1 )
						return 2;
					return phase;

				default:
					if( phase == 1 || phase == 2 )
						return 3;
					else
						return -1;
			}
		}

		return phase;
	}

	/**
	 * Tries to read <code>count</code> arguments from the stream. The arguments
	 * must be put into brackets, and can be separated by commas. Empty arguments
	 * are possible.
	 * @param count the number of expected arguments
	 * @param vararg which kind of varargs are present
	 * @return a list containing exactly <code>count</code> arguments or
	 * <code>null</code> if another number than <code>count</code> arguments
	 * was found (in that case, the symbols are pushed back onto the stream)
	 */
	private List<List<Symbol>> readArguments( int count, VarArg vararg ) throws IOException{
		List<List<Symbol>> arguments = new ArrayList<List<Symbol>>();
		List<Symbol> currentArgument = null;

		List<Symbol> history = new LinkedList<Symbol>();
		boolean valid = false;

		int openCount = 0;

		boolean run = true;
		boolean inString = false;
		boolean wasOpen = false;

		while( run ){
			Symbol next = states.getBase().next();

			PreprocessorToken token = (PreprocessorToken)next.value;

			switch( token.getKind() ){
				case OPEN:
					if( !inString )
						openCount++;
					if( !inString && openCount == 1 ){
						currentArgument = new LinkedList<Symbol>();
						arguments.add( currentArgument );
						wasOpen = true;
					}
					else if( openCount > 0 ){
						currentArgument.add( next );
					}
					break;
				case CLOSE:
					if( !inString )
						openCount--;
					if( !inString && openCount <= 0 ){
						run = false;
						valid = (openCount == 0);
						line = token.getLine();
						break;
					}
					else if( openCount > 0 ){
						currentArgument.add( next );
					}
					break;
				case COMMA:
					if( !inString && openCount == 1 ){
						currentArgument = new LinkedList<Symbol>();
						arguments.add( currentArgument );
					}
					else if( openCount > 0 ){
						currentArgument.add( next );
					}
					break;
				case EOF:
					run = false;
					break;
				case NEWLINE:
					inString = false;
				case WHITESPACE:
					if( openCount > 0 ){
						currentArgument.add( next );
					}
					break;
				case QUOTE:
					inString = !inString;
				default:
					if( openCount > 0 ){
						currentArgument.add( next );
					}
					else{
						run = false;
						valid = false;
					}
			}

			history.add( next );
		}

		if( valid ){
			if( arguments.size() == 1 ){
				List<Symbol> first = arguments.get( 0 );
				boolean whitespaces = true;
				for( Symbol symbol : first ){
					if( symbol.sym != Symbols.WHITESPACE.sym() ){
						whitespaces = false;
					}
				}
				if( whitespaces ){
					arguments.remove( 0 );
				}
			}

			switch( vararg ){
				case NO:
					valid = arguments.size() == count;
					break;
				case YES_UNNAMED:
					valid = arguments.size() >= count;
					break;
				case YES_NAMED:
					valid = arguments.size() >= count-1;
					break;
			}
		}

		if( valid ){
			// ok
			return arguments;
		}
		else{
			switch( vararg ){
				case NO:
					valid = arguments.size() == count;
					if( !valid ){
						states.reportError( "Wrong nr. of arguments for macro, expected " +
								count + " arguments, but found " + ( wasOpen ? arguments.size() : "none" ),
								Insights.macroWrongNumberOfArguments( macro.getName(), arguments.size(), count, false ),
								identifier );
					}
					break;
				case YES_UNNAMED:
					valid = arguments.size() >= count;
					if( !valid ){
						states.reportError( "Wrong nr. of arguments for macro, expected " +
								count + " or more arguments, but found " + ( wasOpen ? arguments.size() : "none" ), 
								Insights.macroWrongNumberOfArguments( macro.getName(), arguments.size(), count, true ),
								identifier );
					}
					break;
				case YES_NAMED:
					valid = arguments.size() >= count-1;
					if( !valid ){
						states.reportError( "Wrong nr. of arguments for macro, expected " +
								Math.max( 0, count-1) + " or more arguments, but found " + ( wasOpen ? arguments.size() : "none" ),
								Insights.macroWrongNumberOfArguments( macro.getName(), arguments.size(), Math.max( 0, count-1 ), true ),
								identifier );
					}
					break;
			}

			states.getBase().push( new SymbolStream( history, false ) );
			return null;
		}
	}

	@Override
	public void pushed(){
		if( macro.requireInformNext() ){
			String[] args;
			if( arguments == null )
				args = new String[]{};
			else{
				args = new String[ arguments.size() ];
				int index = 0;
				for( List<Symbol> argument : arguments ){
					args[ index++ ] = toString( argument );
				}
			}
			macro.informNext( args );
		}

		stream = new ElementStream( macro.getTokenSequence( identifier ), true );
		macro.setInProgress( true );
		if( states != null ){
			states.changeMacroNesting( this );
		}
	}
	
	@Override
	public void disable() throws IOException {
		macro.setInProgress( false );
		if( states != null )
			states.changeMacroNesting( null );
	}
	
	@Override
	public void enable() throws IOException {
		macro.setInProgress( true );
		if( states != null )
			states.changeMacroNesting( this );
	}
	
	@Override
	public void popped(){
		// nothing to do
	}
	
	private String toString( List<Symbol> argument ){
		StringBuilder builder = new StringBuilder();
		for( Symbol symbol : argument ){
			if( symbol.value instanceof PreprocessorElement ){
				try{
					PreprocessorReader reader = new PreprocessorReader( (PreprocessorElement)symbol.value , null, 0, 0 );
					int next;
					while( (next = reader.read()) != -1 ){
						builder.append( (char)next );
					}
				}catch ( IOException e ){
					// it is very doubtful that we land here, so ignore the error
					e.printStackTrace();
				}
			}
			else if( symbol.value instanceof PreprocessorToken ){
				builder.append( ((PreprocessorToken)symbol.value).getText() );
			}
		}
		return builder.toString();
	}



	private static class ArrayReadBase implements PurgingReader.ReadBase{
		private List<PreprocessorToken> tokens = new ArrayList<PreprocessorToken>();
		private int index;

		public ArrayReadBase(){
		}

		public void add( PreprocessorToken token ){
			tokens.add( token );
		}

		public void increment( int count ) {
			index += count;
		}

		public int end() {
			int index = this.index-1;
			if( index < 0 )
				return -1;

			for( PreprocessorToken token : tokens ){
				int[] end = token.getEnd();
				if( end == null ){
					if( index < token.getText().length() )
						return -1;

					else
						index -= token.getText().length();
				}
				else{
					if( index < end.length )
						return end[ index ];
					else
						index -= end.length;
				}
			}

			return -1;
		}

		public int begin() {
			int index = this.index;

			for( PreprocessorToken token : tokens ){
				int[] begin = token.getBegin();
				if( begin == null ){
					if( index < token.getText().length() )
						return -1;

					else
						index -= token.getText().length();
				}
				else{
					if( index < begin.length )
						return begin[ index ];
					else
						index -= begin.length;
				}
			}

			return -1;
		}
	}
}
