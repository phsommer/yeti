// 
// To Test jlex definition: java -jar jlex.jar HeaderScanner.lex
// cd "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/scanner"
//
// To Generate java: 
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
// 239 states after removal of redundant states.
// Outputting lexical analyzer code.

package tinyOS.nesc.scanner;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import tinyOS.editors.nesc.language.elements.*;
import tinyOS.nesc.parser.HeaderFileParser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import tinyOS.nesc.parser.IParser;
import tinyOS.nesc.parser.yyInput;

/**
 *	NesC Scanner
 */
%%

%public
%class HeaderScanner
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
    int openBrackets = 0;
    
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
    
    public int getPosition() {
		return yychar;
	}

    /** classifies current token.
        Should not be called if advance() returned false.
        @returns current %token or single character.
      */
    public int token () {
	  return token;
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
	 		value = makeToken(HeaderFileParser.IDENTIFIER);
		 	return HeaderFileParser.IDENTIFIER;
		} else {		
	 		previous_value = value;
			value = makeToken(HeaderFileParser.TYPEDEF_NAME);
	  		return parser.type_of_name(yytext());
		}
 	}

	boolean log = false;
	
	public void setLog(boolean value) {
		this.log = value;
	}
	
%}
%init{
	// Constructor

%init}
%line
%char
%state INCLUDE MULTILINECOMMENT DEFINE SKIPMACRO ATTRIBUTEBEGIN ATTRIBUTE ATTRIBUTEFUNCTION
%notunix

%{
	private Token makeToken(int value) {
		return new Token(value, yytext(), yyline, yychar, yychar+ yytext().length());
    }
%}
O 			   = [0-7]
D			   = [0-9]
L			   = [a-zA-Z_]
H			   = [a-fA-F0-9]
E			   = [Ee][+-]?{D}+
FS			   = (f|F|l|L)
IS			   = ((u|U|l|L)*)		// Achtung: {IS}? nicht erlaubt

InputCharacter 		= [^\n]
LineTerminator 		= \r|\n|\r\n
WhiteSpace     		= {LineTerminator} | [\t\f] 
EndOfLineComment    = "//"{InputCharacter}* {LineTerminator}

MACROEND = [^\n\\]*$
MACRO = [^\n]*[\\][\040]*$


%%
<YYINITIAL> {EndOfLineComment}      { /* ignore */	}
<YYINITIAL> "/*"					{ 
									  yybegin(MULTILINECOMMENT);
									  if (log) System.out.println("MULTILINECOMMENT BEGIN");
								  	}
<YYINITIAL>		"\""([^\n\r\"]+)"\""      { 
											  value = makeToken(HeaderFileParser.STRING);
						                      return(HeaderFileParser.STRING);
											}
<YYINITIAL>		"__attribute__"		{
 									  attribute = new AttributeElement(makeToken(HeaderFileParser.ATTRIBUTE));
 									  if (log) System.out.println("ATTRIBUTEBEGIN");
									  yybegin(ATTRIBUTEBEGIN);
									  //return(NesCparser.ATTRIBUTE); 
									}
<YYINITIAL>		"typedef"			{
									  value = makeToken(HeaderFileParser.TYPEDEF);
									  return(HeaderFileParser.TYPEDEF); 
									}
<YYINITIAL>		"enum"				{
									  value = makeToken(HeaderFileParser.ENUM);
									  return(HeaderFileParser.ENUM); 
									}
<YYINITIAL>		"#"((" "|{WhiteSpace})*)"include"			{ 	yybegin(INCLUDE);
										value = makeToken(HeaderFileParser.INCLUDE);
									  	return HeaderFileParser.INCLUDE;
									}
<YYINITIAL>		"#"((" "|{WhiteSpace})*)("undef"|"if"|"ifdef"|"ifndef"|"define"|"endif"|"else"|"error") 	{
										yybegin(SKIPMACRO);
									  if (log) System.out.println("SKIPMACRO BEGIN");
									}


<INCLUDE> "<" 						{ value = makeToken('<'); return('<'); }
<INCLUDE> ">"						{ value = makeToken('>'); return('>'); }
<INCLUDE> "\""						{ value = makeToken('"'); return('"'); }
<INCLUDE> {L}({L}|{D})*".h"			{ value = makeToken(HeaderFileParser.HEADERFILE); return(HeaderFileParser.HEADERFILE); }
<INCLUDE> {L}({L}|{D})*".c"			{ value = makeToken(HeaderFileParser.HEADERFILE); return(HeaderFileParser.HEADERFILE); }
<INCLUDE> {EndOfLineComment}      	{ /* ignore */	}
<INCLUDE> ("/")*(([^"<"\n"/"" "])*)"/"	{ value = makeToken(HeaderFileParser.PATH); return(HeaderFileParser.PATH); }
<INCLUDE> {LineTerminator}			{ yybegin(YYINITIAL); 
									 if (log) System.out.println("YYINITIAL BEGIN");
									}
<INCLUDE> .							{ /* ignore */ }
	
<YYINITIAL> 	"int"				{ 
									  value = makeToken(HeaderFileParser.INT);
									  return(HeaderFileParser.INT); 
								}
<YYINITIAL> 	"long"				{ 
									  value = makeToken(HeaderFileParser.LONG);
									  return(HeaderFileParser.LONG); 
								}
<YYINITIAL> 	"short"			{
									  value = makeToken(HeaderFileParser.SHORT);
									   return(HeaderFileParser.SHORT); 
								}
<YYINITIAL>		"enum"			{
								  value = makeToken(HeaderFileParser.ENUM);
								  return(HeaderFileParser.ENUM); 
								}
<YYINITIAL> 	"signed"			{
									  value = makeToken(HeaderFileParser.SIGNED);
									   return(HeaderFileParser.SIGNED); 
								}
<YYINITIAL> 	"sizeof"			{
									  value = makeToken(HeaderFileParser.SIZEOF);
									   return(HeaderFileParser.SIZEOF); 
									}
<YYINITIAL> 	"unsigned"			{ 
									  value = makeToken(HeaderFileParser.UNSIGNED);									  
									  return(HeaderFileParser.UNSIGNED); 
									}
<YYINITIAL> 	"void"				{ 
									  value = makeToken(HeaderFileParser.VOID);
									  return(HeaderFileParser.VOID); 
									}
<YYINITIAL> 	"struct"			{ 
									  value = makeToken(HeaderFileParser.STRUCT);
									  return(HeaderFileParser.STRUCT); 
									}
<YYINITIAL>		"union"			{ value = makeToken(HeaderFileParser.UNION);
									return(HeaderFileParser.UNION);
								}
<YYINITIAL> 	"for"				{ 
									  value = makeToken(HeaderFileParser.FOR);
									  return(HeaderFileParser.FOR); 
									}
<YYINITIAL> 	"if"				{ 
									  value = makeToken(HeaderFileParser.IF);
									  return(HeaderFileParser.IF); 
									}
<YYINITIAL> 	"return"			{ 
									  value = makeToken(HeaderFileParser.RETURN);
									  return(HeaderFileParser.RETURN); 
									}
<YYINITIAL> 	"extern"			{
									  value = makeToken(HeaderFileParser.EXTERN);
									   return(HeaderFileParser.EXTERN); 
									}
<YYINITIAL> 	{L}({L}|{D})*		{ 
									  return(identifier_or_typedef_name());
									}

<YYINITIAL>		0[xX]{H}+{IS}? 		{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		0[xX]{H}+{IS}? 		{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		0{O}+{IS}? 			{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		0{O}+{IS}? 			{
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		{D}+{IS}? 			{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
<YYINITIAL>		{D}+{IS}? 			{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}

<YYINITIAL>		'(\\.|[^\\'])+' 	{ 
									  value = makeToken(HeaderFileParser.CHARACTER_CONSTANT); 
									  return(HeaderFileParser.CHARACTER_CONSTANT); 
									}
									
<YYINITIAL>		{D}+{E}{FS}? 		{ 
									 value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
									  return(HeaderFileParser.FLOATING_CONSTANT); 
									}
<YYINITIAL>		{D}*"."{D}+({E})?{FS}? { 
										  value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
										  return(HeaderFileParser.FLOATING_CONSTANT); 
									   }
<YYINITIAL>		{D}+"."{D}*({E})?{FS}? { 
										 value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
										 return(HeaderFileParser.FLOATING_CONSTANT); 
									   }
	
<YYINITIAL> 	"..."			{ value = makeToken(HeaderFileParser.ELLIPSIS); return(HeaderFileParser.ELLIPSIS); }
<YYINITIAL> 	">>="			{ value = makeToken(HeaderFileParser.RIGHT_ASSIGN); return(HeaderFileParser.RIGHT_ASSIGN); }
<YYINITIAL> 	"<<="			{ value = makeToken(HeaderFileParser.LEFT_ASSIGN); return(HeaderFileParser.LEFT_ASSIGN); }
<YYINITIAL> 	"+="			{ value = makeToken(HeaderFileParser.ADD_ASSIGN); return(HeaderFileParser.ADD_ASSIGN); }
<YYINITIAL> 	"-="			{ value = makeToken(HeaderFileParser.SUB_ASSIGN); return(HeaderFileParser.SUB_ASSIGN); }
<YYINITIAL> 	"*="			{ value = makeToken(HeaderFileParser.MUL_ASSIGN); return(HeaderFileParser.MUL_ASSIGN); }
<YYINITIAL> 	"/="			{ value = makeToken(HeaderFileParser.DIV_ASSIGN); return(HeaderFileParser.DIV_ASSIGN); }
<YYINITIAL> 	"%="			{ value = makeToken(HeaderFileParser.MOD_ASSIGN); return(HeaderFileParser.MOD_ASSIGN); }
<YYINITIAL> 	"&="			{ value = makeToken(HeaderFileParser.AND_ASSIGN); return(HeaderFileParser.AND_ASSIGN); }
<YYINITIAL> 	"^="			{ value = makeToken(HeaderFileParser.XOR_ASSIGN); return(HeaderFileParser.XOR_ASSIGN); }
<YYINITIAL> 	"|="			{ value = makeToken(HeaderFileParser.OR_ASSIGN); return(HeaderFileParser.OR_ASSIGN); }
<YYINITIAL> 	">>"			{ value = makeToken(HeaderFileParser.RIGHT_OP); return(HeaderFileParser.RIGHT_OP); }
<YYINITIAL> 	"<<"			{ value = makeToken(HeaderFileParser.LEFT_OP); return(HeaderFileParser.LEFT_OP); }
<YYINITIAL> 	"++"			{ value = makeToken(HeaderFileParser.INC_OP); return(HeaderFileParser.INC_OP); }
<YYINITIAL> 	"--"			{ value = makeToken(HeaderFileParser.DEC_OP); return(HeaderFileParser.DEC_OP); }
<YYINITIAL> 	"->"			{ value = makeToken(HeaderFileParser.PTR_OP); return(HeaderFileParser.PTR_OP); }
<YYINITIAL> 	"&&"			{ value = makeToken(HeaderFileParser.AND_OP); return(HeaderFileParser.AND_OP); }
<YYINITIAL> 	"||"			{ value = makeToken(HeaderFileParser.OR_OP); return(HeaderFileParser.OR_OP); }
<YYINITIAL> 	"<="			{ value = makeToken(HeaderFileParser.LE_OP); return(HeaderFileParser.LE_OP); }
<YYINITIAL>		">="			{ value = makeToken(HeaderFileParser.GE_OP); return(HeaderFileParser.GE_OP); }
<YYINITIAL>		"=="			{ value = makeToken(HeaderFileParser.EQ_OP); return(HeaderFileParser.EQ_OP); }
<YYINITIAL>		"!="			{ value = makeToken(HeaderFileParser.NE_OP); return(HeaderFileParser.NE_OP); }
<YYINITIAL>		"}"				{ openBrackets--;
								  value = makeToken(125); return(125); 
								}
<YYINITIAL>		"{"				{ 
								  openBrackets++;
								  value = makeToken(123); return(123); 
								}
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
<YYINITIAL>		";"				{ /* is end of TYPEDEF,ENUM if no open brackets exist.. */ 
								 	if(openBrackets==0) yybegin(YYINITIAL);
								 	if (log) System.out.println("YYINITIAL BEGIN");
									 value = makeToken(';'); 
									 return(';');
								}
<YYINITIAL>	 	" "|{WhiteSpace} 	{ /* ignore */ }
<YYINITIAL>  	\t					{ /* ignore */ }
<YYINITIAL>		.					{ /* ignore */ }

<MULTILINECOMMENT>	"*/"					{
											if (log) System.out.println("YYINITIAL BEGIN");
											 yybegin(YYINITIAL); 
											} 
<MULTILINECOMMENT>	.|\n|\r					{  }

<SKIPMACRO>	([^\n\\]*)$						{ 
											if (log) System.out.println("YYINITIAL BEGIN");
											yybegin(YYINITIAL);
											}
<SKIPMACRO>	([^\n]*)[\040]*$				{ /* ignore */ }
<SKIPMACRO>	.*								{ 
											if (log) System.out.println("YYINITIAL BEGIN");
											yybegin(YYINITIAL); 
											}

<ATTRIBUTEBEGIN>	"(("					{   
											  attribute.setOpenBrackets();
//											  Token(value, yytext(), yyline, yychar, yychar+ yytext().length());
											  yybegin(ATTRIBUTE); 	
											}
<ATTRIBUTEBEGIN>	 " "|{WhiteSpace} 		{ /* ignore */ }
<ATTRIBUTEBEGIN>	 .						{ /* error go back */
											 if (log) System.out.println("YYINITIAL BEGIN");
											  yybegin(YYINITIAL);
											}
<ATTRIBUTE> ","								{ attribute.addSpacer();	}
<ATTRIBUTE> {L}({L}|{D})*					{ /* attribute word  */
											  attribute.addWord(makeToken(HeaderFileParser.IDENTIFIER));	
											}		  
<ATTRIBUTE> {L}({L}|{D})*"("				{ /* attribute function */ 
											  attribute.addFunction(yytext().substring(0,yytext().length()-1), 
											  						yyline, 
											  						yychar-1, 
											  						yychar+yytext().length()-1);	
											  if (log) System.out.println("ATTRIBUTEFUNCTION BEGIN");											  						
											  yybegin(ATTRIBUTEFUNCTION);
											}
<ATTRIBUTE> "))"							{ 
											  //finished attribute declaration
											  attribute.setClosingBrackets();
											  attributeElements.add(attribute);
											  if (log) System.out.println("YYINITIAL BEGIN");	
											  yybegin(YYINITIAL);
											}										
<ATTRIBUTE>	 " "|{WhiteSpace} 				{ /* ignore */ }
<ATTRIBUTE>	.								{ /* error */
											  attribute.setError();
											  if (log) System.out.println("YYINITIAL BEGIN");	
											  yybegin(YYINITIAL);
											}


<ATTRIBUTEFUNCTION>	{L}({L}|{D})*			{ attribute.addFunctionArgument(makeToken(HeaderFileParser.IDENTIFIER));	}
<ATTRIBUTEFUNCTION> ","						{ attribute.addFunctionArgumentDelimiter(); }
<ATTRIBUTEFUNCTION> ")"						{ 	
											  attribute.finishFunction();
											  if (log) System.out.println("ATTRIBUTE BEGIN");	
											  yybegin(ATTRIBUTE); 
											}
<ATTRIBUTEFUNCTION>	 " "|{WhiteSpace} 		{ /* ignore */ }
<ATTRIBUTEFUNCTION>	 .						{ /* error */ 
											  attribute.setError();
											  if (log) System.out.println("ATTRIBUTE BEGIN");	
											  yybegin(ATTRIBUTE);  
											}

