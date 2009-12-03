package tinyos.yeti.nesc12.lexer;

import tinyos.yeti.nesc12.parser.sym;
import java_cup.runtime.Symbol;
import tinyos.yeti.nesc12.parser.ScopeStack;
import tinyos.yeti.preprocessor.output.Insights;


 

%%
%class Lexer
%unicode
%cup
%public
%abstract

%{
	private ScopeStack scopes;
	
	private Symbol[] cache = new Symbol[4];
	private int cacheIndex = 0;
	
	private int tokenBegin = 0;
	
	
	public void setScopeStack( ScopeStack scopes ){
		this.scopes = scopes;
	}

	public ScopeStack getScopeStack(){
		return scopes;
	}
	
	private void reportError(){
		scopes.getParser().reportError( "Unknown token: '" + yytext() + "'", Insights.unknownSyntaxError(), tokenBegin, tokenBegin + yylength() );
		tokenBegin += yylength();
	}
	
	private Symbol token( int type ){
		return token( type, tokenBegin, tokenBegin + yylength(), yytext() );
	}
	
	private Symbol token( int type, String text ){
		return token( type, tokenBegin, tokenBegin + yylength(), text );
	}
	
	private Symbol token( int type, int left, int right, String text ){
		Symbol result = new Symbol( type, left, right, new Token( text, left, right, scopes.getLevel() ));
		cache[ cacheIndex++ ] = result;
		cacheIndex %= cache.length;
		tokenBegin = right;
		return result;
	}
	
	private Symbol follow( int type ){
		Symbol current = token( type );
		sendLater( current );
		return current;
	}
	
	protected abstract void sendLater( Symbol symbol );
	
	public Symbol previous( Symbol current ){
		for( int i = 0; i < cache.length; i++ ){
			if( cache[i] == current ){
				i -= 1;
				if( i < 0 )
					i += cache.length;
					
				if( i == cacheIndex )
					return null;
					
				return cache[i];
			}
		}
		
		return null;
	}
	
	public Symbol next( Token current ){
		for( int i = 0; i < cache.length; i++ ){
			if( cache[i].value == current ){
				i += 1;
				if( i >= cache.length )
					i -= cache.length;
					
				if( i == cacheIndex )
					return null;
					
				return cache[i];
			}
		}
		
		return null;
	}
%}

nondigit			=	[a-zA-Z_$]
digit				=	[0-9]
digit_sequence		=	({digit}+)

nonzero_digit 		=	[1-9]

hexadecimal_digit	=	[0-9a-fA-F]
hexadecimal_digit_sequence 		=	({hexadecimal_digit}+)
hex_quad			=	({hexadecimal_digit}{4})
hexadecimal_prefix	=	("0" ("x" | "X"))
hexadecimal_constant =	({hexadecimal_prefix} {hexadecimal_digit}+)

octal_digit			=	[0-7]
octal_constant		=	("0" {octal_digit}*)

decimal_constant	=	({nonzero_digit} {digit}*)


universal_character_name
					=	(("\\u" | "\\U") {hex_quad})


identifier			=	({nondigit} ({nondigit} | {digit} | {universal_character_name})*)


unsigned_suffix		=	("u" | "U")
long_suffix			=	("l" | "L")
long_long_suffix	=	("ll" | "LL")
integer_suffix		=	(({unsigned_suffix} ({long_suffix} | {long_long_suffix})?) | (({long_suffix} | {long_long_suffix}) {unsigned_suffix}? ))
integer_constant	= 	(({decimal_constant} | {octal_constant} | {hexadecimal_constant}) {integer_suffix}?)

sign				=	("+" | "-")
floating_suffix		= 	("f" | "F" | "l" | "L")
exponent_part		=	(("e" | "E") {sign}? {digit_sequence})
binary_exponent_part			=	(("p" | "P") {sign}? {digit_sequence})
fractional_constant = 	(({digit_sequence} "." {digit_sequence}?) | ("." {digit_sequence}))
hexadecimal_fractional_constant	=	(({hexadecimal_digit_sequence} "." {hexadecimal_digit_sequence}?) | ("." {hexadecimal_digit_sequence}))
hexadecimal_floating_constant	=	({hexadecimal_prefix} ({hexadecimal_fractional_constant}|{hexadecimal_digit_sequence}) {binary_exponent_part} {floating_suffix}?)
decimal_floating_constant		=	((({fractional_constant} {exponent_part}?) | ({digit_sequence} {exponent_part})) {floating_suffix}?)
floating_constant	=	({decimal_floating_constant} | {hexadecimal_floating_constant})

simple_escape_sequence 			= ("\\" ("'" | "\\" | "\"" | "?" | [abfnrtv] ))
octal_escape_sequence			= ("\\" {octal_digit}{1,3})
hexadecimal_escape_sequence		= ("\\x" {hexadecimal_digit}+)
escape_sequence		= 	({simple_escape_sequence} | {octal_escape_sequence} | {hexadecimal_escape_sequence} | {universal_character_name})
newline				= 	(\r|\n|\r\n|\u2028|\u2029|\u000B|\u000C|\u0085)
c_char				=	({escape_sequence} | (!((. (.|{newline})+)|( ("'"|"\\"|{newline}) (.|{newline})* ))))
c_char_sequence		= 	({c_char}+)
character_constant	= 	(("L")? "'" {c_char_sequence} "'")


s_char				=	({escape_sequence} | (!((. (.|{newline})+)|( ("\""|"\\"|{newline}) (.|{newline})* ))))
s_char_sequence		=	({s_char}+)
string_literal		=	(("L")? "\"" {s_char_sequence} "\"")


whitespace			= 	[ \t\f]

%%

<YYINITIAL>{

	"["			{ return token( sym.P_RECT_OPEN ); }
	"]"			{ return token( sym.P_RECT_CLOSE ); }
	"("			{ return follow( sym.P_ROUND_OPEN ); }
	")"			{ return follow( sym.P_ROUND_CLOSE ); }
	"{"			{ return follow( sym.P_CURLY_OPEN ); }
	"}"			{ return follow( sym.P_CURLY_CLOSE ); }
	"."			{ return token( sym.P_POINT ); }
	"->"		{ return token( sym.P_RIGHT_ARROW ); }
	"++"		{ return token( sym.P_INCREMENT ); }
	"--"		{ return token( sym.P_DECREMENT ); }
	"&"			{ return token( sym.P_AMP ); }
	"*"			{ return token( sym.P_STAR ); }
	"+"			{ return token( sym.P_PLUS ); }
	"-"			{ return token( sym.P_MINUS ); }
	"~"			{ return token( sym.P_TILDE ); }
	"!"			{ return token( sym.P_EXCLAMATION ); }
	"/"			{ return token( sym.P_SLASH ); }
	"%"			{ return token( sym.P_PERCENT ); }
	"<<"		{ return token( sym.P_SHIFT_LEFT ); }
	">>"		{ return token( sym.P_SHIFT_RIGHT ); }
	"<"			{ return follow( sym.P_SMALLER ); }
	">"			{ return follow( sym.P_GREATER ); }
	"<="		{ return token( sym.P_SMALLER_EQ ); }
	">="		{ return token( sym.P_GREATER_EQ ); }
	"=="		{ return token( sym.P_EQ ); }
	"!="		{ return token( sym.P_NOT_EQ ); }
	"^"			{ return token( sym.P_CARET ); }
	"|"			{ return token( sym.P_LINE ); }
	"&&"		{ return token( sym.P_AND ); }
	"||"		{ return token( sym.P_OR ); }
	"?"			{ return token( sym.P_QUESTION ); }
	":"			{ return token( sym.P_COLON ); }
	";"			{ return follow( sym.P_SEMICOLON ); }
	"..."		{ return token( sym.P_ELLIPSIS ); }
	"="			{ return token( sym.P_ASSIGN ); }
	"*="		{ return token( sym.P_MUL_ASSIGN ); }
	"/="		{ return token( sym.P_DIV_ASSIGN ); }
	"%="		{ return token( sym.P_MOD_ASSIGN ); }
	"+="		{ return token( sym.P_ADD_ASSIGN ); }
	"-="		{ return token( sym.P_SUB_ASSIGN ); }
	"<<="		{ return token( sym.P_SHIFT_LEFT_ASSIGN ); }
	">>="		{ return token( sym.P_SHIFT_RIGHT_ASSIGN ); }
	"&="		{ return token( sym.P_AND_ASSIGN ); }
	"^="		{ return token( sym.P_XOR_ASSIGN ); }
	"|="		{ return token( sym.P_OR_ASSIGN ); }
	","			{ return token( sym.P_COMMA ); }


	"sizeof"		{ return token( sym.K_SIZEOF ); }
	"typedef"		{ return token( sym.K_TYPEDEF ); }
	"extern"		{ return token( sym.K_EXTERN ); }
	"static"		{ return token( sym.K_STATIC ); }
	"auto"			{ return token( sym.K_AUTO ); }
	"register"		{ return token( sym.K_REGISTER ); }
	"void"			{ return token( sym.K_VOID ); }
	"char"			{ return token( sym.K_CHAR ); }
	"short"			{ return token( sym.K_SHORT ); }
	"int"			{ return token( sym.K_INT ); }
	"long"			{ return token( sym.K_LONG ); }
	"float"			{ return token( sym.K_FLOAT ); }
	"double"		{ return token( sym.K_DOUBLE ); }
	"signed"		{ return token( sym.K_SIGNED ); }
	"unsigned"		{ return token( sym.K_UNSIGNED ); }
	"_BOOL"			{ return token( sym.K__BOOL ); }
	"_Complex"		{ return token( sym.K__COMPLEX ); }
	"struct"		{ return token( sym.K_STRUCT ); }
	"union"			{ return token( sym.K_UNION ); }
	"enum"			{ return token( sym.K_ENUM ); }
	"const"			{ return token( sym.K_CONST ); }
	"__const__"		{ return token( sym.K_CONST ); }
	"restrict"		{ return token( sym.K_RESTRICT ); }
	"__restrict__"	{ return token( sym.K_RESTRICT ); }
	"volatile"		{ return token( sym.K_VOLATILE ); }
	"__volatile__"	{ return token( sym.K_VOLATILE ); }
	"inline"		{ return token( sym.K_INLINE ); }
	"__inline__"	{ return token( sym.K_INLINE ); }
	"static"		{ return token( sym.K_STATIC ); }
	"case"			{ return token( sym.K_CASE ); }
	"switch"		{ return token( sym.K_SWITCH ); }
	"default"		{ return token( sym.K_DEFAULT ); }
	"if"			{ return token( sym.K_IF ); }
	"else"			{ return token( sym.K_ELSE ); }
	"while"			{ return token( sym.K_WHILE ); }
	"do"			{ return token( sym.K_DO ); }
	"for"			{ return token( sym.K_FOR ); }
	"goto"			{ return token( sym.K_GOTO ); }
	"continue"		{ return token( sym.K_CONTINUE ); }
	"break"			{ return token( sym.K_BREAK ); }
	"return"		{ return token( sym.K_RETURN ); }
	

	"@"				{ return token( sym.NP_AT ); }
	"<-"			{ return token( sym.NP_LEFT_ARROW ); }

	"interface"		{ return token( sym.NK_INTERFACE ); }
	"module"		{ return token( sym.NK_MODULE ); }
	"component"		{ return token( sym.NK_COMPONENT ); }
	"configuration"	{ return token( sym.NK_CONFIGURATION ); }
	"implementation"	{ return token( sym.NK_IMPLEMENTATION ); }
	"generic"		{ return token( sym.NK_GENERIC ); }
	"includes"		{ return token( sym.NK_INCLUDES ); }
	"asm"			{ return token( sym.K_ASM ); }
	"__asm__"		{ return token( sym.K_ASM ); }
	"provides"		{ return token( sym.NK_PROVIDES ); }
	"uses"			{ return token( sym.NK_USES ); }
	"as"			{ return token( sym.NK_AS ); }
	"extension"		{ return token( sym.K_EXTENSION ); }
	"__extension__"	{ return token( sym.K_EXTENSION ); }
	
	"nx_struct"		{ return token( sym.NK_NX_STRUCT ); }
	"nx_union"		{ return token( sym.NK_NX_UNION ); }
	"components"	{ return token( sym.NK_COMPONENTS ); }
	"new"			{ return token( sym.NK_NEW ); }
	"command"		{ return token( sym.NK_COMMAND ); }
	"event"			{ return token( sym.NK_EVENT ); }
	"async"			{ return token( sym.NK_ASYNC ); }
	"norace"		{ return token( sym.NK_NORACE ); }
	"atomic"		{ return token( sym.NK_ATOMIC ); }
	"call"			{ return token( sym.NK_CALL ); }
	"signal"		{ return token( sym.NK_SIGNAL ); }
	"task"			{ return token( sym.NK_TASK ); }
	"post"			{ return token( sym.NK_POST ); }
	

	{identifier}		{
							String text = yytext();
							if( scopes.isTypedef( text )){
								return token( sym.TYPEDEF, text );
							}
							else if( scopes.isEnum( text )){
								return token( sym.ENUMERATION_CONSTANT ); 
							}
							else{
								return token( sym.IDENTIFIER, text );
							}
						}
	
	{integer_constant} 		{ return token( sym.INTEGER_CONSTANT ); }
	{floating_constant}		{ return token( sym.FLOATING_CONSTANT ); }
	{character_constant}	{ return token( sym.CHARACTER_CONSTANT ); }
	
	{string_literal} 	{ return token( sym.STRING ); }

	{newline}		{ tokenBegin += yylength(); }
	{whitespace}	{ tokenBegin += yylength(); }
	

	.				{ reportError(); }
}

