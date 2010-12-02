// 
// To Test jlex definition: java -jar jlex.jar Scanner.lex
// To Generate java: 
// 
//		
// Processing first section -- user code.
// Processing second section -- JLex declarations.
// Processing third section -- lexical rules.
// Creating NFA machine representation.
// NFA comprised of 936 states.
// Creating DFA transition table.
// Working on DFA states................................
// .....................................................
// .....................................................
// .....................................................
// .....................................................
// ......................
// Minimizing DFA transition table.
// 353 states after removal of redundant states.
// Outputting lexical analyzer code.

package tinyOS.nesc.scanner;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import tinyOS.editors.nesc.language.elements.*;
import tinyOS.nesc.parser.NesCparser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import tinyOS.nesc.parser.yyInput;
import tinyOS.nesc.parser.IParser;

/**
 *	NesC Scanner
 */
%%
%unicode

%public
%class Scanner
%implements yyInput, ITypeNames

%type int
%eofval{
	return YYEOF;
%eofval}



%{
    Object value; 
    Object previous_value;
 	StringBuffer string = new StringBuffer();
    private int token;
    IParser parser = null;
    
    int ifcount = 0;
    
    public ArrayList multiLineCommentTokens = new ArrayList();
    public ArrayList attributeElements = new ArrayList();
    
	AttributeElement attribute;    
    Token multiLineBeginToken;
    
    public void setCallback(IParser n) {
    	this.parser = n;
    }

    /** move on to next token.
        @returns false if positioned beyond tokens.
        @throws IOException on input error.
      */
    public boolean advance () throws java.io.IOException {
	  token=yylex();
	  return token != YYEOF;
    }

    /** classifies current token.
        Should not be called if advance() returned false.
        @returns current %token or single character.
      */
    public int token () {
	  return token;
    }

	public int getPosition() {
		return yychar;
	}


    /** associated with current token.
        Should not be called if advance() returned false.
        @returns value for token().
      */
    public Object value () {
	  return value;
    }

	/* Probably because of its use for interactive line-interpreters
	 * like "dc", original yacc uses a "lazy" lookahead, that is to say, it
	 * does not fetch a lookahead when the only action is the default
	 * reduction. But our scanner-feedback must keep the lookahead in
	 * sync. This routine sees to it that the lookahead has been
	 * fetched.
	 *
	 * yychar is the yacc lookahead token. It is -1 when
	 * yacc is being "lazy". yylex() is allowed to return -1 (or any
	 * negative int) to indicate EOF, but yacc uses 0 to indicate EOF.
	 */
	public void lex_sync() {
		if(yychar == -1) {
			try {
				yychar = yylex();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (yychar < 0) {
			  yychar = 0;
			}
	    }
	}
	
	public Object previousValue() {
		return previous_value;
	}
	
	public int identifier_or_typedef_name() {
	    /* Return the type of the token,
		 * IDENTIFIER, TYPEDEF_NAME, or ENUM_CONSTANT
	 	 */
	 	if(parser.get_idents_only()) {
	 		previous_value = value;
	 		value = makeToken(NesCparser.IDENTIFIER);
		 	return NesCparser.IDENTIFIER;
		} else {		
	 		previous_value = value;
			value = makeToken(NesCparser.TYPEDEF_NAME);
	  		return parser.type_of_name(yytext());
		}
 	}

%}
%init{
	// Constructor

%init}
%line
%char
%state MULTILINECOMMENT, MACRODEF, MACROELSE, ATTRIBUTE, ATTRIBUTEBEGIN, ATTRIBUTEFUNCTION, STRING
%notunix
%16bit
%{
	private Token makeToken(int value) {
		return new Token(value, yytext(), yyline, yychar, yychar+ yytext().length());
    }
%}
O 			   = [0-7]
D			   = [:digit:]
L			   = [a-zA-Z_]
H			   = [a-fA-F0-9]
E			   = [Ee][+-]?{D}+
FS			   = (f|F|l|L)
IS			   = ((u|U|l|L)*)		// Achtung: {IS}? nicht erlaubt

InputCharacter 		= [^\r\n]
LineTerminator 		= \r|\n|\r\n
WhiteSpace     		= {LineTerminator} | [\t\f] 
EndOfLineComment    = "//"{InputCharacter}* {LineTerminator}

%unicode
%%
<YYINITIAL> "#else"					{
									 //System.out.println("-------- macro/else");
									 yybegin(MACROELSE);	  
									}
<YYINITIAL> "#"					    {
									 yybegin(MACRODEF);
									}
<YYINITIAL> {EndOfLineComment}      { 

									}			
				
<YYINITIAL> "/*"					{ 
									  multiLineBeginToken = makeToken(0);
									  yybegin(MULTILINECOMMENT);
								  	} 

<YYINITIAL>  \"					    {
									  string.setLength(0); yybegin(STRING);
								  	} 
								  	
<YYINITIAL> 	"as"				{ 
									  value = makeToken(NesCparser.AS); 
									  return NesCparser.AS; 
									}
<YYINITIAL> 	"call"				{ 
									  value = makeToken(NesCparser.CALL); 
									  return NesCparser.CALL; 
									}
<YYINITIAL> 	"command"			{ 
									  value = makeToken(NesCparser.COMMAND); 
									  return NesCparser.COMMAND;
									}
<YYINITIAL> 	"components"		{ 
									  value = makeToken(NesCparser.COMPONENTS); 
									  return NesCparser.COMPONENTS; 
									}
<YYINITIAL> 	"configuration"		{ 
									  value = makeToken(NesCparser.CONFIGURATION); 
									  return NesCparser.CONFIGURATION; 
									}
<YYINITIAL> 	"event"				{ 
									  value = makeToken(NesCparser.EVENT); 
									  return NesCparser.EVENT; 
									}
<YYINITIAL> 	"implementation" 	{
									  value = makeToken(NesCparser.IMPLEMENTATION); 
									  return NesCparser.IMPLEMENTATION; 
									}
<YYINITIAL> 	"interface"			{ 
									  value = makeToken(NesCparser.INTERFACE); 
									  return NesCparser.INTERFACE; 
									}
<YYINITIAL> 	"module"			{ 
									   value = makeToken(NesCparser.MODULE); 
									   return NesCparser.MODULE; 
									}
<YYINITIAL> 	"post"				{  
									   value = makeToken(NesCparser.POST); 
									   return NesCparser.POST; 
									}
<YYINITIAL> 	"provides"			{ 
									   value = makeToken(NesCparser.PROVIDES); 
									   return NesCparser.PROVIDES; 
									}
<YYINITIAL> 	"signal"			{ 
									   value = makeToken(NesCparser.SIGNAL); 
									   return NesCparser.SIGNAL; 
									}
<YYINITIAL> 	"task"				{  
									   value = makeToken(NesCparser.TASK); 
									   return NesCparser.TASK; 
									}
<YYINITIAL> 	"uses"				{  
									   value = makeToken(NesCparser.USES); 
									   return NesCparser.USES; 
									}
<YYINITIAL> 	"includes"			{ 
									   value = makeToken(NesCparser.INCLUDES); 
									   return NesCparser.INCLUDES; 
									}
<YYINITIAL> 	"atomic"			{ 
									    value = makeToken(NesCparser.ATOMIC); 
									    return NesCparser.ATOMIC; 
									 }
<YYINITIAL> 	"async"				{ 
									    value = makeToken(NesCparser.ASYNC); 
									    return NesCparser.ASYNC; 
									 }
<YYINITIAL> 	"norace"			{ 
									    value = makeToken(NesCparser.NORACE); 
									    return NesCparser.NORACE; 
									 }
<YYINITIAL> 	"inline"			{ 
									    value = makeToken(NesCparser.INLINE); 
									    return NesCparser.INLINE; 
									 }
<YYINITIAL> 	"<-"				{ 
									   value = makeToken(NesCparser.LEFTARROW); 
									   return NesCparser.LEFTARROW; 
									}
<YYINITIAL> 	"auto"				{ 
									  value = makeToken(NesCparser.AUTO);
									  return(NesCparser.AUTO); 
									}
<YYINITIAL> 	"break"				{
									  value = makeToken(NesCparser.BREAK);
									   return(NesCparser.BREAK); 
									 }
<YYINITIAL> 	"case"				{ 
									  value = makeToken(NesCparser.CASE);
									  return(NesCparser.CASE); 
									}
<YYINITIAL> 	"char"				{ 
									  value = makeToken(NesCparser.CHAR);
									  return(NesCparser.CHAR); 
									}
<YYINITIAL> 	"const"				{
									  value = makeToken(NesCparser.CONST);
									   return(NesCparser.CONST); 
									 }
<YYINITIAL> 	"continue"		    { 
       								  value = makeToken(NesCparser.CONTINUE);									  
									  return(NesCparser.CONTINUE); 
									}
<YYINITIAL> 	"default"			{
									  value = makeToken(NesCparser.DEFAULT);
									  return(NesCparser.DEFAULT); 
									}
<YYINITIAL> 	"do"				{ 
									  value = makeToken(NesCparser.DO);
									  return(NesCparser.DO); 
									}
<YYINITIAL> 	"double"			{
									  value = makeToken(NesCparser.DOUBLE);
									   return(NesCparser.DOUBLE); 
									 }
<YYINITIAL> 	"else"				{ 
									  value = makeToken(NesCparser.ELSE);
									  return(NesCparser.ELSE); 
									}
<YYINITIAL> 	"enum"				{ 
									  value = makeToken(NesCparser.ENUM);
									  return(NesCparser.ENUM); 
									}
<YYINITIAL> 	"extern"			{
									  value = makeToken(NesCparser.EXTERN);
									   return(NesCparser.EXTERN); 
									}
<YYINITIAL> 	"float"				{
									  value = makeToken(NesCparser.FLOAT);
									   return(NesCparser.FLOAT); 
									 }
<YYINITIAL> 	"for"				{ 
									  value = makeToken(NesCparser.FOR);
									  return(NesCparser.FOR); 
									}
<YYINITIAL> 	"goto"				{ 
									  value = makeToken(NesCparser.GOTO);
									  return(NesCparser.GOTO); 
									}
<YYINITIAL> 	"if"				{ 
									  value = makeToken(NesCparser.IF);
									  return(NesCparser.IF); 
									}
<YYINITIAL> 	"int"				{ 
									  value = makeToken(NesCparser.INT);
									  return(NesCparser.INT); 
									}
<YYINITIAL> 	"long"				{ 
									  value = makeToken(NesCparser.LONG);
									  return(NesCparser.LONG); 
									}
<YYINITIAL> 	"register"		    { 
									  value = makeToken(NesCparser.REGISTER);									  
									  return(NesCparser.REGISTER);
									}
<YYINITIAL> 	"return"			{
									  value = makeToken(NesCparser.RETURN);
  								      return(NesCparser.RETURN); 
									}
<YYINITIAL> 	"short"				{
									  value = makeToken(NesCparser.SHORT);
									   return(NesCparser.SHORT); 
									}
<YYINITIAL> 	"signed"			{
									  value = makeToken(NesCparser.SIGNED);
									   return(NesCparser.SIGNED); 
									}
<YYINITIAL> 	"sizeof"			{
									  value = makeToken(NesCparser.SIZEOF);
									   return(NesCparser.SIZEOF); 
									}
<YYINITIAL> 	"static"			{
									  value = makeToken(NesCparser.STATIC);
									   return(NesCparser.STATIC); 
									}
<YYINITIAL> 	"struct"			{
									  value = makeToken(NesCparser.STRUCT);
									   return(NesCparser.STRUCT); 
									}
<YYINITIAL> 	"switch"			{
									  value = makeToken(NesCparser.SWITCH);
									   return(NesCparser.SWITCH); 
									}
<YYINITIAL> 	"typedef"			{
									  value = makeToken(NesCparser.TYPEDEF);
									  return(NesCparser.TYPEDEF); 
									}
<YYINITIAL> 	"union"				{
									  value = makeToken(NesCparser.UNION);
									  return(NesCparser.UNION); 
									}
<YYINITIAL> 	"unsigned"			{ 
									  value = makeToken(NesCparser.UNSIGNED);									  
									  return(NesCparser.UNSIGNED); 
									}
<YYINITIAL> 	"void"				{ 
									  value = makeToken(NesCparser.VOID);
									  return(NesCparser.VOID); 
									}
<YYINITIAL> 	"volatile"		    { value = makeToken(NesCparser.VOLATILE);									  
									  return(NesCparser.VOLATILE); 
									}
<YYINITIAL> 	"while"				{
									  value = makeToken(NesCparser.WHILE);
									  return(NesCparser.WHILE); 
									} 	
<YYINITIAL>		"offsetof"			{
									  value = makeToken(NesCparser.OFFSETOF);
									  return(NesCparser.OFFSETOF);
									}
<YYINITIAL>		"__attribute__"		{
 									  attribute = new AttributeElement(makeToken(NesCparser.ATTRIBUTE));
									  yybegin(ATTRIBUTEBEGIN);
									  //return(NesCparser.ATTRIBUTE); 
									}									

<YYINITIAL> 	{L}({L}|{D})*		{ 
									  return(identifier_or_typedef_name());
									}

<YYINITIAL>		0[xX]{H}+{IS}? 		{ 
									  value = makeToken(NesCparser.INTEGER_CONSTANT); 
									  return(NesCparser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		0[xX]{H}+{IS}? 		{ 
									  value = makeToken(NesCparser.INTEGER_CONSTANT); 
									  return(NesCparser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		0{O}+{IS}? 			{ 
									  value = makeToken(NesCparser.INTEGER_CONSTANT); 
									  return(NesCparser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		0{O}+{IS}? 			{
									  value = makeToken(NesCparser.INTEGER_CONSTANT); 
									  return(NesCparser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		{D}+{IS}? 			{ 
									  value = makeToken(NesCparser.INTEGER_CONSTANT); 
									  return(NesCparser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		{D}+{IS}? 			{ 
									  value = makeToken(NesCparser.INTEGER_CONSTANT); 
									  return(NesCparser.INTEGER_CONSTANT); 
									}

<YYINITIAL>		'(\\.|[^\\'])+' 	{ 
									  value = makeToken(NesCparser.CHARACTER_CONSTANT); 
									  return(NesCparser.CHARACTER_CONSTANT); 
									}
									
<YYINITIAL>		{D}+{E}{FS}? 		{ 
									 value = makeToken(NesCparser.FLOATING_CONSTANT); 
									  return(NesCparser.FLOATING_CONSTANT); 
									}
<YYINITIAL>		{D}*"."{D}+({E})?{FS}? { 
										  value = makeToken(NesCparser.FLOATING_CONSTANT); 
										  return(NesCparser.FLOATING_CONSTANT); 
									   }
<YYINITIAL>		{D}+"."{D}*({E})?{FS}? { 
										 value = makeToken(NesCparser.FLOATING_CONSTANT); 
										 return(NesCparser.FLOATING_CONSTANT); 
									   }
	
<YYINITIAL> 	"..."			{ value = makeToken(NesCparser.ELLIPSIS); return(NesCparser.ELLIPSIS); }
<YYINITIAL> 	">>="			{ value = makeToken(NesCparser.RIGHT_ASSIGN); return(NesCparser.RIGHT_ASSIGN); }
<YYINITIAL> 	"<<="			{ value = makeToken(NesCparser.LEFT_ASSIGN); return(NesCparser.LEFT_ASSIGN); }
<YYINITIAL> 	"+="			{ value = makeToken(NesCparser.ADD_ASSIGN); return(NesCparser.ADD_ASSIGN); }
<YYINITIAL> 	"-="			{ value = makeToken(NesCparser.SUB_ASSIGN); return(NesCparser.SUB_ASSIGN); }
<YYINITIAL> 	"*="			{ value = makeToken(NesCparser.MUL_ASSIGN); return(NesCparser.MUL_ASSIGN); }
<YYINITIAL> 	"/="			{ value = makeToken(NesCparser.DIV_ASSIGN); return(NesCparser.DIV_ASSIGN); }
<YYINITIAL> 	"%="			{ value = makeToken(NesCparser.MOD_ASSIGN); return(NesCparser.MOD_ASSIGN); }
<YYINITIAL> 	"&="			{ value = makeToken(NesCparser.AND_ASSIGN); return(NesCparser.AND_ASSIGN); }
<YYINITIAL> 	"^="			{ value = makeToken(NesCparser.XOR_ASSIGN); return(NesCparser.XOR_ASSIGN); }
<YYINITIAL> 	"|="			{ value = makeToken(NesCparser.OR_ASSIGN); return(NesCparser.OR_ASSIGN); }
<YYINITIAL> 	">>"			{ value = makeToken(NesCparser.RIGHT_OP); return(NesCparser.RIGHT_OP); }
<YYINITIAL> 	"<<"			{ value = makeToken(NesCparser.LEFT_OP); return(NesCparser.LEFT_OP); }
<YYINITIAL> 	"++"			{ value = makeToken(NesCparser.INC_OP); return(NesCparser.INC_OP); }
<YYINITIAL> 	"--"			{ value = makeToken(NesCparser.DEC_OP); return(NesCparser.DEC_OP); }
<YYINITIAL> 	"->"			{ value = makeToken(NesCparser.PTR_OP); return(NesCparser.PTR_OP); }
<YYINITIAL> 	"&&"			{ value = makeToken(NesCparser.AND_OP); return(NesCparser.AND_OP); }
<YYINITIAL> 	"||"			{ value = makeToken(NesCparser.OR_OP); return(NesCparser.OR_OP); }
<YYINITIAL> 	"<="			{ value = makeToken(NesCparser.LE_OP); return(NesCparser.LE_OP); }
<YYINITIAL>		">="			{ value = makeToken(NesCparser.GE_OP); return(NesCparser.GE_OP); }
<YYINITIAL>		"=="			{ value = makeToken(NesCparser.EQ_OP); return(NesCparser.EQ_OP); }
<YYINITIAL>		"!="			{ value = makeToken(NesCparser.NE_OP); return(NesCparser.NE_OP); }
<YYINITIAL>		"}"				{ value = makeToken(125); return(125); }
<YYINITIAL>		"{"				{ value = makeToken(123); return(123); }
<YYINITIAL>		";"				{ value = makeToken(';'); return(';'); }
<YYINITIAL>		","				{ value = makeToken(',');  return(','); }
<YYINITIAL>		":"				{ value = makeToken(':'); return(':'); }
<YYINITIAL>		"="				{ value = makeToken('='); return('='); }
<YYINITIAL>		"("				{ value = makeToken('('); return('('); }
<YYINITIAL>		")"				{ value = makeToken(')'); return(')'); }
<YYINITIAL>		("["|"<:")		{ value = makeToken('['); return('['); }
<YYINITIAL>		("]"|":>")		{ value = makeToken(']'); return(']'); }
<YYINITIAL>		"."				{ value = makeToken('.'); return('.'); }
<YYINITIAL>		"&"				{ value = makeToken('&'); return('&'); }
<YYINITIAL>		"!"				{ value = makeToken('!'); return('!'); }
<YYINITIAL>		"~"				{ value = makeToken('~'); return('~'); }
<YYINITIAL>		"-"				{ value = makeToken('-'); return('-'); }
<YYINITIAL>		"+"				{ value = makeToken('+'); return('+'); }
<YYINITIAL>		"*"				{ value = makeToken('*'); return('*'); }
<YYINITIAL>		"/"				{ value = makeToken('/'); return('/'); }
<YYINITIAL>		"%"				{ value = makeToken('%'); return('%'); }
<YYINITIAL>		"<"				{ value = makeToken('<'); return('<'); }
<YYINITIAL>		">"				{ value = makeToken('>'); return('>'); }
<YYINITIAL>		"^"				{ value = makeToken('^'); return('^'); }
<YYINITIAL>		"|"				{ value = makeToken('|'); return('|'); }
<YYINITIAL>		"?"				{ value = makeToken('?'); return('?'); }

	
<YYINITIAL>	 " "|{WhiteSpace} 	{ /* ignore */ }
<YYINITIAL>  \t				{ /* ignore */ }

<YYINITIAL>	 . 				{ return(identifier_or_typedef_name()); }
	
<STRING> {
  \"                             { yybegin(YYINITIAL); 
									value = makeToken(NesCparser.STRING);
  									return(NesCparser.STRING);
								 }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
  {LineTerminator}				 {
  									// String was not closed...
  									yybegin(YYINITIAL); 
									value = makeToken(NesCparser.ERRORSTRING);
  									return(NesCparser.ERRORSTRING);
  								 }
}


<MULTILINECOMMENT>	"*/"					{ 
											  
											  multiLineBeginToken.end = makeToken(0).end;
											  multiLineCommentTokens.add(multiLineBeginToken);
											  yybegin(YYINITIAL);
											} 
<MULTILINECOMMENT>	.|\n|\r					{  }
<MACRODEF>	([^\n\\]*)$						{ yybegin(YYINITIAL);}
<MACRODEF>	([^\n]*)[\040]*$				{ /* ignore */ }
<MACROELSE> "#endif"						{ yybegin(YYINITIAL); 
											//System.out.println("---- end macro/else");
											}
<MACROELSE> .|\n|\r							{ /*empty*/ }

<ATTRIBUTEBEGIN>	"(("					{   
											  attribute.setOpenBrackets();
//											  Token(value, yytext(), yyline, yychar, yychar+ yytext().length());
											  yybegin(ATTRIBUTE); 	
											}
<ATTRIBUTEBEGIN>	 " "|{WhiteSpace} 		{ /* ignore */ }
<ATTRIBUTEBEGIN>	 .						{ /* error go back */
											  yybegin(YYINITIAL);
											}
<ATTRIBUTE> ","								{ attribute.addSpacer();	}
<ATTRIBUTE> {L}({L}|{D})*					{ /* attribute word  */
											  attribute.addWord(makeToken(NesCparser.IDENTIFIER));	
											}		  
<ATTRIBUTE> {L}({L}|{D})*"("				{ /* attribute function */ 
											  attribute.addFunction(yytext().substring(0,yytext().length()-1), 
											  						yyline, 
											  						yychar-1, 
											  						yychar+yytext().length()-1);	
											  yybegin(ATTRIBUTEFUNCTION);
											}
<ATTRIBUTE> "))"							{ 
											  //finished attribute declaration
											  attribute.setClosingBrackets();
											  attributeElements.add(attribute);
											  yybegin(YYINITIAL);
											}										
<ATTRIBUTE>	 " "|{WhiteSpace} 				{ /* ignore */ }
<ATTRIBUTE>	.								{ /* error */
											  attribute.setError();
											  yybegin(YYINITIAL);
											}


<ATTRIBUTEFUNCTION>	{L}({L}|{D})*			{ attribute.addFunctionArgument(makeToken(NesCparser.IDENTIFIER));	}
<ATTRIBUTEFUNCTION> ","						{ attribute.addFunctionArgumentDelimiter(); }
<ATTRIBUTEFUNCTION> ")"						{ 	
											  attribute.finishFunction();
											  yybegin(ATTRIBUTE); 
											}
<ATTRIBUTEFUNCTION>	 " "|{WhiteSpace} 		{ /* ignore */ }
<ATTRIBUTEFUNCTION>	 .						{ /* error */ 
											  attribute.setError();
											  yybegin(ATTRIBUTE);  
											}
