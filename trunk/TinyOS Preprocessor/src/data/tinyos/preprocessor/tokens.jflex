package tinyos.yeti.preprocessor.lexer;

import tinyos.yeti.preprocessor.parser.*;
import tinyos.yeti.preprocessor.FileInfo;
import java_cup.runtime.*;

/**
 * This class reads a c-source file and searches tokens for the preprocessor.
 */
%%

%class Lexer
%unicode
%cup

%{
	// kind of symbol that is currently read
	Symbols symbol;

	// Buffer used for text
	StringBuilder bufferText = new StringBuilder();

	// source of characters
	private PurgingReader reader;

	// the path of the file that is currently read
	private FileInfo file;

	private int tokenBegin = 0;
	private int tokenLength = 0;

	private Symbol pushedSymbol;

	private boolean eof = false;
	private boolean noNewline;
	private int line;	
	
	protected State states;

	protected int tokenbegin(){
		return tokenBegin;
	}

	public void setNoAutoNewlineAtEnd( boolean noNewline ){
		this.noNewline = noNewline;
	}

	public void setLine( int line ){
		this.line = line;
	}
	
	public int getLine(){
		return line;
	}

	public void setReader( PurgingReader reader ){
		this.reader = reader;
	}
	
	public void setFile( FileInfo file ){
		this.file = file;
	}
	
	public FileInfo getFile() {
        return file;
    }
    
 	private Symbol symbol( Symbols type ){
		return symbol( type, null );
	}
	private Symbol symbol( Symbols type, String value) {
		PreprocessorToken result;
	
		int[] begin;
		int[] end;
	
		int tokenEnd = tokenBegin + tokenLength;
		
		begin = new int[ tokenLength ];
		end = new int[ tokenLength ];
		for( int i = tokenBegin; i < tokenEnd; i++ ){
			begin[ i-tokenBegin ] = reader.popReadBaseBegin( i );
			end[ i-tokenBegin ] = reader.popReadBaseEnd( i );
		}
		
		
		result = new PreprocessorToken( 
			type,
			file, 
			line, 
			begin,
			end,
			value,
			states == null ? null : states.getInclusionPath() );
		
		tokenBegin += tokenLength;
		tokenLength = 0;
		
		return new Symbol( type.sym(), result );
	}
	
	private Symbol token( Symbols type ){
		String full = bufferText.toString();
		bufferText.setLength( 0 );
		if( full.length() > 0 ){
			Symbol result = symbol( Symbols.TEXT, full );
			push( token( type ) ); 
			return result;
		}
	
		updateTokenLength();
		return symbol( type, yytext() );
	}
	
	public void buffer(){
		bufferText.append( yytext() );
		updateTokenLength();
	}
	
	private void updateTokenLength(){
		tokenLength += yylength();
	}
	
	private void push( Symbol symbol ){
		pushedSymbol = symbol;
		yybegin( PUSHED );
	}
%}

%eofval{
	if( pushedSymbol != null ){
		Symbol result = pushedSymbol;
		pushedSymbol = null;
		return result;
	}

	String full = bufferText.toString();
	if( full.length() == 0 ){
		if( eof || noNewline )
			return symbol( Symbols.EOF );
			
		eof = true;
		return symbol( Symbols.NEWLINE );
	}
	else{
		bufferText.setLength( 0 );
		return symbol( Symbols.TEXT, full );
	}
%eofval}

idletter			= ([:letter:]|[$]|_)
identifier			= {idletter}+({idletter}|[:digit:])*
integer				= 0 | ([1-9][0-9]*)

whitespace			= \t|\f|" "
newline				= \r|\n|\r\n|\u2028|\u2029|\u000B|\u000C|\u0085

%state PUSHED

%%

<YYINITIAL>{
	"#"					{ return token( Symbols.SHARP ); }
	"##"				{ return token( Symbols.CONCAT ); }
	"\""				{ return token( Symbols.QUOTE ); }
	"\\\""				{ return token( Symbols.MASKED_QUOTE ); }
	{whitespace}+ 		{ return token( Symbols.WHITESPACE ); }
	{newline}+			{ return token( Symbols.NEWLINE ); }
	{whitespace}*{newline}+
						{ return token( Symbols.NEWLINE ); }
	"("					{ return token( Symbols.OPEN ); }
	","					{ return token( Symbols.COMMA ); }
	")"					{ return token( Symbols.CLOSE ); }
	">"					{ return token( Symbols.GREATER ); }
	"<"					{ return token( Symbols.SMALLER ); }
	"..."				{ return token( Symbols.VARARG ); }
	
	"define"			{ return token( Symbols.K_DEFINE ); }
	[:digit:]+ "define"	{ buffer(); }

	"defined"			{ return token( Symbols.K_DEFINED ); }
	[:digit:]+ "defined" { buffer(); }

	"undef"				{ return token( Symbols.K_UNDEF ); }
	[:digit:]+ "undef"	{ buffer(); }

	"error"				{ return token( Symbols.K_ERROR ); }
	[:digit:]+ "error"	{ buffer(); }
	
	"warning"			{ return token( Symbols.K_WARNING ); }
	[:digit:]+ "error"	{ buffer(); }
	
	"if"				{ return token( Symbols.K_IF ); }
	[:digit:]+ "if"		{ buffer(); }
	
	"else"				{ return token( Symbols.K_ELSE ); }
	[:digit:]+ "else"	{ buffer(); }

	"ifdef"				{ return token( Symbols.K_IFDEF ); }
	[:digit:]+ "ifdef"	{ buffer(); }
	
	"ifndef"			{ return token( Symbols.K_IFNDEF ); }
	[:digit:]+ "ifndef"	{ buffer(); }
	
	"line"				{ return token( Symbols.K_LINE ); }
	[:digit:]+ "line"	{ buffer(); }
	
	"pragma"			{ return token( Symbols.K_PRAGMA ); }
	[:digit:]+ "pragma"	{ buffer(); }

	"elif"				{ return token( Symbols.K_ELIF ); }
	[:digit:]+ "elif"	{ buffer(); }
	
	"endif"				{ return token( Symbols.K_ENDIF ); }
	[:digit:]+ "endif"	{ buffer(); }
	
	"include"			{ return token( Symbols.K_INCLUDE ); }
	[:digit:]+ "include" { buffer(); }
	
	{identifier}		{ return token( Symbols.IDENTIFIER ); }
	[:digit:]+ {identifier}
						{ buffer(); }
	.					{ buffer(); }
}

<PUSHED>{
	.|{newline}			{ 	yypushback( yylength() );
							yybegin( YYINITIAL );
							Symbol result = pushedSymbol;
							pushedSymbol = null;
							return result; }
}



