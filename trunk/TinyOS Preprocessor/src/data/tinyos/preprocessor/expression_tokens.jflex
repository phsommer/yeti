package tinyos.yeti.preprocessor.expression;

import tinyos.yeti.preprocessor.parser.*;
import java_cup.runtime.*;

/**
 * This class reads a single expression and searches tokens in the expression.
 */
%%

%class Lexer
%unicode
%cup

%{
 	private Symbol symbol( int type ){
		return symbol( type, null );
	}
	private Symbol symbol( int type, String value) {
		return new Symbol( type, value );
	}
%}

identifier			= ([:letter:]|_)+([:letter:]|[:digit:]|_)*
integer				= ( ([0-9]*) | ("0x" [0-9a-fA-F]* )) [uUlL]* 
finteger			= 0 | ([1-9][0-9]*)
float				= ("." {finteger}) | ({finteger} ("." {finteger}?)?) [fFdD]
character			= "'" ( ("\\"? .) | "\\" [0-7]{1,3} | ("\\" "x" [0-9a-fA-F]{1,2}) ) "'"

whitespace			= \t|\f|" "
newline				= \r|\n|\r\n|\u2028|\u2029|\u000B|\u000C|\u0085

%%

<YYINITIAL>{
	"+"			{ return symbol( sym.PLUS ); }
	"-"			{ return symbol( sym.MINUS ); }
	"*"			{ return symbol( sym.TIMES ); }
	"/"			{ return symbol( sym.DIVISION ); }
	"|"			{ return symbol( sym.BIT_OR ); }
	"||"		{ return symbol( sym.OR ); }
	"&"			{ return symbol( sym.BIT_AND ); }
	"&&"		{ return symbol( sym.AND ); }
	"!"			{ return symbol( sym.NOT ); }
	"^"			{ return symbol( sym.XOR ); }
	"~"			{ return symbol( sym.BIT_REVERSE ); }
	"=="		{ return symbol( sym.EQ ); }
	"!="		{ return symbol( sym.NEQ ); }
	">"			{ return symbol( sym.GREATER ); }
	">="		{ return symbol( sym.GREATER_EQ ); }
	"<"			{ return symbol( sym.LESS ); }
	"<="		{ return symbol( sym.LESS_EQ ); }
	"("			{ return symbol( sym.OPEN ); }
	")"			{ return symbol( sym.CLOSE ); }
	">>"		{ return symbol( sym.SHIFT_RIGHT ); } 
	"<<"		{ return symbol( sym.SHIFT_LEFT ); }
	"?"			{ return symbol( sym.QUESTION ); }
	":"			{ return symbol( sym.COLON ); }
	"defined"	{ return symbol( sym.DEFINED ); }
	{integer}	{ return symbol( sym.INTEGER, yytext() ); }
	{identifier} { return symbol( sym.IDENTIFIER, yytext() ); }
	{character} { return symbol( sym.CHARACTER, yytext() ); }
	
	{whitespace} { }
	{newline}	{ }
	
	{float}		{ return symbol( sym.ERROR, "Floating point number not allowed in condition: " + yytext() ); }
	"'" ~"'" 	{ return symbol( sym.ERROR, "Illegal character: " + yytext() ); }
	"\"" ~"\""  { return symbol( sym.ERROR, "No strings permitted in condition: " + yytext() ); }
	.			{ return symbol( sym.ERROR, "Unexpected sign in condition: " + yytext() ); }
}