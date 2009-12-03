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
// created by jay 0.8 (c) 1998 Axel.Schreiner@informatik.uni-osnabrueck.de

					// line 21 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"


  package tinyos.yeti.nesc.parser;

  import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;

import tinyos.yeti.nesc.scanner.ITokenInfo;
import tinyos.yeti.nesc.scanner.Scanner;
import tinyos.yeti.nesc.scanner.Token;

  /**
   * !! 
   * Don't edit this file directly. Instead edit the Jay-File
   * HeaderFileParser.jay and generate the java file using:
   * ay -v HeaderFileParser.jay <skeleton > HeaderFileParser.java
   * !! 
   */

  /** start with<br>
	no argument to suppress debugging<br>
	0, 1, 2, or 3 to animate trapping input, output, or both<br>
	other to trace
  */
  public class HeaderFileParser implements IParser {
	private List elementList = new LinkedList();
	private Scanner scanner = null;
	private NameSpace ns = null;
	private IResource resourceOrigin = null;
	
	/** to prevent cycles */
	private LinkedList fileHistory = null;
	
	/** the current file */
	File file = null;
	
	public void setScanner(Scanner s) {
		this.scanner = s;
	}
    
    public NameSpace getNameSpace() {
		return ns;
    }
    
    public IResource getOrigin() {
    	return resourceOrigin;
    }
    public void setOrigin(IResource r) {
    	this.resourceOrigin = r;
    }
    
    public boolean get_idents_only() {
		return ns.get_idents_only();
    }
    
    public int type_of_name(String text) {
    	return ns.type_of_name(text);
   	}
   	
   	private HeaderFileParser(LinkedList fileHistory) {
   		this.fileHistory = fileHistory;
   		ns = new NameSpace(file);
   	}
   	
   	public HeaderFileParser(File f, String types[], LinkedList fileHistory) {
   		this.fileHistory = fileHistory;
   		this.file = f;
   		ns = new NameSpace(types, file);
   		if( f != null )
   		    this.fileHistory.add(f.getName());
   	}
   	
   	public HeaderFileParser(IResource resource, File f, LinkedList fileHistory) {
		this.fileHistory = fileHistory;
   		this.file = f;
   		ns = new NameSpace(file);
   		this.resourceOrigin = resource;
   		if( f != null )
   		    this.fileHistory.add(f.getName());
   	}

	public HeaderFileParser(File f, LinkedList fileHistory) {
		this.fileHistory = fileHistory;
	    this.file = f;
	    ns = new NameSpace(file);
	    this.fileHistory.add(f.getName());
	}
   	
	boolean debug = true;

	boolean followIncludes = true;
	
	public void setFollowIncludes(boolean t) {
		followIncludes = t;
	}

   	   
					// line 130 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"



					// line 113 "-"
// %token constants

  public static final int BAD_TOKEN = 257;
  public static final int INTEGER_CONSTANT = 258;
  public static final int CHARACTER_CONSTANT = 259;
  public static final int FLOATING_CONSTANT = 260;
  public static final int ENUMERATION_CONSTANT = 261;
  public static final int STRING = 262;
  public static final int SIZEOF = 263;
  public static final int PTR_OP = 264;
  public static final int INC_OP = 265;
  public static final int DEC_OP = 266;
  public static final int LEFT_OP = 267;
  public static final int RIGHT_OP = 268;
  public static final int LE_OP = 269;
  public static final int GE_OP = 270;
  public static final int EQ_OP = 271;
  public static final int NE_OP = 272;
  public static final int AND_OP = 273;
  public static final int OR_OP = 274;
  public static final int MUL_ASSIGN = 275;
  public static final int DIV_ASSIGN = 276;
  public static final int MOD_ASSIGN = 277;
  public static final int ADD_ASSIGN = 278;
  public static final int SUB_ASSIGN = 279;
  public static final int LEFT_ASSIGN = 280;
  public static final int RIGHT_ASSIGN = 281;
  public static final int AND_ASSIGN = 282;
  public static final int XOR_ASSIGN = 283;
  public static final int OR_ASSIGN = 284;
  public static final int TYPEDEF_NAME = 285;
  public static final int TYPEDEF = 286;
  public static final int EXTERN = 287;
  public static final int STATIC = 288;
  public static final int AUTO = 289;
  public static final int REGISTER = 290;
  public static final int CHAR = 291;
  public static final int SHORT = 292;
  public static final int INT = 293;
  public static final int LONG = 294;
  public static final int SIGNED = 295;
  public static final int UNSIGNED = 296;
  public static final int FLOAT = 297;
  public static final int DOUBLE = 298;
  public static final int CONST = 299;
  public static final int VOLATILE = 300;
  public static final int VOID = 301;
  public static final int STRUCT = 302;
  public static final int UNION = 303;
  public static final int ENUM = 304;
  public static final int ELLIPSIS = 305;
  public static final int CASE = 306;
  public static final int DEFAULT = 307;
  public static final int IF = 308;
  public static final int SWITCH = 309;
  public static final int WHILE = 310;
  public static final int DO = 311;
  public static final int FOR = 312;
  public static final int GOTO = 313;
  public static final int CONTINUE = 314;
  public static final int BREAK = 315;
  public static final int RETURN = 316;
  public static final int ATTRIBUTE = 317;
  public static final int IDENTIFIER = 318;
  public static final int PATH = 319;
  public static final int HEADERFILE = 320;
  public static final int INCLUDE = 321;
  public static final int THEN = 322;
  public static final int ELSE = 323;
  public static final int yyErrorCode = 256;


	 private ParserError pe = null;



  /** simplified error message.
      @see <a href="#yyerror(java.lang.String, java.lang.String[])">yyerror</a>
    */
  public void yyerror (String message) {
    yyerror(message, null);
  }

  /** (syntax) error message.
      Can be overwritten to control message format.
      @param message text to be displayed.
      @param expected vector of acceptable tokens, if available.
    */
  public void yyerror (String message, String[] expected) {
    if (expected != null && expected.length > 0) {
      System.err.print(message+", expecting");
      for (int n = 0; n < expected.length; ++ n)
        System.err.print(" "+expected[n]);
      System.err.println();
    } else
      System.err.println(message);
  }

  /** debugging support, requires the package jay.yydebug.
      Set to null to suppress debugging messages.
    */
  protected jay.yydebug.yyDebug yydebug;

  protected static final int yyFinal = 3;

  /** index-checked interface to yyName[].
      @param token single character or %token value.
      @return token name or [illegal] or [unknown].
    */
  public static final String yyname (int token) {
    if (token < 0 || token > YyNameClass.yyName.length) return "[illegal]";
    String name;
    if ((name = YyNameClass.yyName[token]) != null) return name;
    return "[unknown]";
  }

  /** computes list of expected tokens on error by tracing the tables.
      @param state for which to compute the list.
      @return list of token names.
    */
  protected String[] yyExpecting (int state) {
    int token, n, len = 0;
    boolean[] ok = new boolean[YyNameClass.yyName.length];

    if ((n = YySindexClass.yySindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           token < YyNameClass.yyName.length && n+token < YyTableClass.yyTable.length; ++ token)
        if (YyCheckClass.yyCheck[n+token] == token && !ok[token] && YyNameClass.yyName[token] != null) {
          ++ len;
          ok[token] = true;
        }
    if ((n = YyRindexClass.yyRindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           token < YyNameClass.yyName.length && n+token < YyTableClass.yyTable.length; ++ token)
        if (YyCheckClass.yyCheck[n+token] == token && !ok[token] && YyNameClass.yyName[token] != null) {
          ++ len;
          ok[token] = true;
        }

    String result[] = new String[len];
    for (n = token = 0; n < len;  ++ token)
      if (ok[token]) result[n++] = YyNameClass.yyName[token];
    return result;
  }

  /** the generated parser, with debugging messages.
      Maintains a state and a value stack, currently with fixed maximum size.
      @param yyLex scanner.
      @param yydebug debug message writer implementing yyDebug, or null.
      @return result of the last reduction, if any.
      @throws yyException on irrecoverable parse error.
    */
  public Object yyparse (yyInput yyLex, Object yydebug)
				throws java.io.IOException, yyException {
    this.yydebug = (jay.yydebug.yyDebug)yydebug;
    return yyparse(yyLex);
  }

  public Object yyparse (yyInput yyLex, ParserError pe)
	throws java.io.IOException, yyException {
	  this.pe = pe;
	  return yyparse(yyLex);
  }



  /** initial size and increment of the state/value stack [default 256].
      This is not final so that it can be overwritten outside of invocations
      of yyparse().
    */
  protected int yyMax;

  /** executed at the beginning of a reduce action.
      Used as $$ = yyDefault($1), prior to the user-specified action, if any.
      Can be overwritten to provide deep copy, etc.
      @param first value for $1, or null.
      @return first.
    */
  protected Object yyDefault (Object first) {
    return first;
  }

  /** the generated parser.
      Maintains a state and a value stack, currently with fixed maximum size.
      @param yyLex scanner.
      @return result of the last reduction, if any.
      @throws yyException on irrecoverable parse error.
    */
  public Object yyparse (yyInput yyLex)
				throws java.io.IOException, yyException {
    if (yyMax <= 0) yyMax = 256;			// initial size
    int yyState = 0, yyStates[] = new int[yyMax];	// state stack
    Object yyVal = null, yyVals[] = new Object[yyMax];	// value stack
    int yyToken = -1;					// current input
    int yyErrorFlag = 0;				// #tks to shift

    yyLoop: for (int yyTop = 0;; ++ yyTop) {
      if (yyTop >= yyStates.length) {			// dynamically increase
        int[] i = new int[yyStates.length+yyMax];
        System.arraycopy(yyStates, 0, i, 0, yyStates.length);
        yyStates = i;
        Object[] o = new Object[yyVals.length+yyMax];
        System.arraycopy(yyVals, 0, o, 0, yyVals.length);
        yyVals = o;
      }
      yyStates[yyTop] = yyState;
      yyVals[yyTop] = yyVal;
      if (yydebug != null) yydebug.push(yyState, yyVal);

      yyDiscarded: for (;;) {	// discarding a token does not change stack
        int yyN;
        if ((yyN = YyDefRedClass.yyDefRed[yyState]) == 0) {	// else [default] reduce (yyN)
          if (yyToken < 0) {
            yyToken = yyLex.advance() ? yyLex.token() : 0;
            if (yydebug != null)
              yydebug.lex(yyState, yyToken, yyname(yyToken), yyLex.value());
          }
          if ((yyN = YySindexClass.yySindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < YyTableClass.yyTable.length && YyCheckClass.yyCheck[yyN] == yyToken) {
            if (yydebug != null)
              yydebug.shift(yyState, YyTableClass.yyTable[yyN], yyErrorFlag-1);
            yyState = YyTableClass.yyTable[yyN];		// shift to yyN
            yyVal = yyLex.value();
            yyToken = -1;
            if (yyErrorFlag > 0) -- yyErrorFlag;
            continue yyLoop;
          }
          if ((yyN = YyRindexClass.yyRindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < YyTableClass.yyTable.length && YyCheckClass.yyCheck[yyN] == yyToken)
            yyN = YyTableClass.yyTable[yyN];			// reduce (yyN)
          else
            switch (yyErrorFlag) {
  
			case 0:
			if (pe != null) {
           			pe.offset = -1;
            		pe.token = yyToken;
            		pe.state = yyState;
            		pe.expected = yyExpecting(yyState);
            		if (yyVal instanceof ITokenInfo) {
    					ITokenInfo tokeninfo =(ITokenInfo)yyVal;
    					pe.line = tokeninfo.getLine();
    					pe.offset = tokeninfo.getOffset();
    					pe.length = tokeninfo.getLength();				
    				}
          		    return yyVal;
           	} else {
            		yyerror("syntax error");
              if (yydebug != null) yydebug.error("syntax error");
            }
            case 1: case 2:
              yyErrorFlag = 3;
              do {
                if ((yyN = YySindexClass.yySindex[yyStates[yyTop]]) != 0
                    && (yyN += yyErrorCode) >= 0 && yyN < YyTableClass.yyTable.length
                    && YyCheckClass.yyCheck[yyN] == yyErrorCode) {
                  if (yydebug != null)
                    yydebug.shift(yyStates[yyTop], YyTableClass.yyTable[yyN], 3);
                  yyState = YyTableClass.yyTable[yyN];
                  yyVal = yyLex.value();
                  continue yyLoop;
                }
                if (yydebug != null) yydebug.pop(yyStates[yyTop]);
              } while (-- yyTop >= 0);
              if (yydebug != null) yydebug.reject();
              throw new yyException("irrecoverable syntax error");
  
            case 3:
              if (yyToken == 0) {
                if (yydebug != null) yydebug.reject();
                throw new yyException("irrecoverable syntax error at end-of-file");
              }
              if (yydebug != null)
                yydebug.discard(yyState, yyToken, yyname(yyToken),
  							yyLex.value());
              yyToken = -1;
              continue yyDiscarded;		// leave stack alone
            }
        }
        int yyV = yyTop + 1-YyLenClass.yyLen[yyN];
        if (yydebug != null)
          yydebug.reduce(yyState, yyStates[yyV-1], yyN, YyRuleClass.yyRule[yyN], YyLenClass.yyLen[yyN]);
        yyVal = yyDefault(yyV > yyTop ? null : yyVals[yyV]);
        switch (yyN) {
case 1:
					// line 264 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  {   
				/*scanner.lex_sync(); */
				ns.ntd(); 
			}
  break;
case 2:
					// line 273 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { 
				/*scanner.lex_sync(); */
				ns.td();
			}
  break;
case 3:
					// line 286 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  {  ns.scope_push(); ns.td();  }
  break;
case 4:
					// line 290 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { ns.scope_pop();  }
  break;
case 5:
					// line 297 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { ns.struct_push();  ns.td();  }
  break;
case 6:
					// line 301 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { ns.struct_pop();  }
  break;
case 7:
					// line 306 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { 
			ns.new_declaration(Declaration.DECLARATION_TYPE_NAME_SPACE_DECL); 
		}
  break;
case 8:
					// line 313 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { 
				ns.new_declaration(Declaration.DECLARATION_TYPE_NAME_SPACE_DECL); 
			  }
  break;
case 9:
					// line 321 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { ns.set_typedef(); }
  break;
case 10:
					// line 326 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { ns.direct_declarator(); }
  break;
case 11:
					// line 331 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { ns.pointer_declarator(); }
  break;
case 12:
					// line 335 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-1+yyTop]); }
  break;
case 13:
					// line 336 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); ns.declarator_id(((Token)((Token)yyVals[0+yyTop])).text); }
  break;
case 14:
					// line 337 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 15:
					// line 342 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 16:
					// line 343 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 17:
					// line 344 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 18:
					// line 345 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 19:
					// line 346 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { System.out.println("ERROR:Current FILE - "+file.getAbsolutePath()); }
  break;
case 20:
					// line 350 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { 
 												  if (followIncludes) {
 												  /* parse included file and add typedefs and
 												     enumeration constants to current parse */
 												     System.out.println("Current File -> "+file.getName() + "  ("+file.getAbsolutePath()+")");
 												  if (fileHistory.contains(((Token)yyVals[-1+yyTop]).text) == false) {
 												  	fileHistory.add(((Token)yyVals[-1+yyTop]).text);
 												  	System.out.println("  TO PARSE -> "+((Token)yyVals[-1+yyTop]).text);
	 												Declaration[] dd = HeaderFileUtil.resolveCTypes(((Token)yyVals[-1+yyTop]).text, this, false, fileHistory);
	 												ns.setTypeDefs(dd);
	 												System.out.println("Current File -> "+file.getName() + "  ("+file.getAbsolutePath()+")");
	 											  }
	 											  }
 	
 												}
  break;
case 21:
					// line 365 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  {  	
 												  if (followIncludes) {
 												     System.out.println("Current File -> "+file.getName() + "  ("+file.getAbsolutePath()+")");
												  if (fileHistory.contains(((Token)yyVals[-1+yyTop]).text) == false) {
 												  	fileHistory.add(((Token)yyVals[-1+yyTop]).text);
 												  	System.out.println("  TO PARSE -> "+((Token)yyVals[-1+yyTop]).text); 												  	
 											  	    Declaration[] dd = HeaderFileUtil.resolveCTypes(((Token)yyVals[-1+yyTop]).text, this, true, fileHistory); 
   	 												ns.setTypeDefs(dd);
   	 												System.out.println("Current File -> "+file.getName() + "  ("+file.getAbsolutePath()+")");
 											  	  }
 											  	  }
 											  	}
  break;
case 22:
					// line 377 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { 
 												  if(followIncludes) {
 												     System.out.println("Current File -> "+file.getName() + "  ("+file.getAbsolutePath()+")");
												  if (fileHistory.contains(((Token)yyVals[-2+yyTop]).text+((Token)yyVals[-1+yyTop]).text) == false) {
 												  	fileHistory.add(((Token)yyVals[-2+yyTop]).text+((Token)yyVals[-1+yyTop]).text);
 												  	System.out.println("  TO PARSE -> "+((Token)yyVals[-2+yyTop]).text+((Token)yyVals[-1+yyTop]).text);
 												  	Declaration[] dd = HeaderFileUtil.resolveCTypes(((Token)yyVals[-2+yyTop]).text,((Token)yyVals[-1+yyTop]).text, this, false, fileHistory);  
   	 												ns.setTypeDefs(dd);
   	 												System.out.println("Current File -> "+file.getName() + "  ("+file.getAbsolutePath()+")");
 												  }
 												  }
 												}
  break;
case 23:
					// line 389 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { 
 												  if (followIncludes) {
 												     System.out.println("Current File -> "+file.getName() + "  ("+file.getAbsolutePath()+")");
												  if (fileHistory.contains(((Token)yyVals[-2+yyTop]).text+((Token)yyVals[-1+yyTop]).text) == false) {
 												  	fileHistory.add(((Token)yyVals[-2+yyTop]).text+((Token)yyVals[-1+yyTop]).text);
 												  	System.out.println("  TO PARSE -> "+((Token)yyVals[-2+yyTop]).text+((Token)yyVals[-1+yyTop]).text);
 												    Declaration[] dd = HeaderFileUtil.resolveCTypes(((Token)yyVals[-2+yyTop]).text,((Token)yyVals[-1+yyTop]).text, this, true, fileHistory);   		
 												    ns.setTypeDefs(dd);
 												    System.out.println("Current File -> "+file.getName() + "  ("+file.getAbsolutePath()+")");
 												  }
 												  }
 												}
  break;
case 24:
					// line 404 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 25:
					// line 405 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 26:
					// line 406 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 27:
					// line 411 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 28:
					// line 412 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 29:
					// line 417 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 30:
					// line 421 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 31:
					// line 422 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 32:
					// line 426 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 33:
					// line 427 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 34:
					// line 428 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 35:
					// line 429 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 36:
					// line 430 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 37:
					// line 431 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 38:
					// line 435 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 39:
					// line 436 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 40:
					// line 437 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 41:
					// line 438 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 42:
					// line 439 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 43:
					// line 444 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 44:
					// line 445 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 45:
					// line 446 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 46:
					// line 447 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 47:
					// line 455 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 48:
					// line 456 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 49:
					// line 460 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 50:
					// line 461 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 51:
					// line 462 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 52:
					// line 463 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 53:
					// line 464 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 54:
					// line 465 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 55:
					// line 466 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 56:
					// line 467 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 57:
					// line 471 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 58:
					// line 472 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 59:
					// line 473 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 60:
					// line 474 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 61:
					// line 478 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 62:
					// line 479 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 63:
					// line 483 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 64:
					// line 484 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 65:
					// line 485 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 66:
					// line 489 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 67:
					// line 490 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 68:
					// line 494 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 69:
					// line 495 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 70:
					// line 496 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* if something goes wrong, catch it.. -> struct still can be recognized */ }
  break;
case 71:
					// line 500 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 72:
					// line 501 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 73:
					// line 506 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 74:
					// line 507 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 75:
					// line 508 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 76:
					// line 509 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 77:
					// line 514 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { ns.new_declaration(HeaderFileParser.STRUCT); }
  break;
case 78:
					// line 514 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 79:
					// line 518 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 80:
					// line 519 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 81:
					// line 520 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 82:
					// line 521 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 83:
					// line 525 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 84:
					// line 526 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 85:
					// line 530 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 86:
					// line 531 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 87:
					// line 532 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 88:
					// line 536 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 89:
					// line 537 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 90:
					// line 538 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 91:
					// line 539 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 92:
					// line 540 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 93:
					// line 545 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 94:
					// line 546 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 95:
					// line 550 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { ns.putDefineConstant(((Token)yyVals[0+yyTop])); }
  break;
case 96:
					// line 551 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { ns.putDefineConstant(((Token)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop]).text);
 											}
  break;
case 97:
					// line 556 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 98:
					// line 557 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 99:
					// line 561 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  {  yyVal = ((Token)yyVals[0+yyTop]); 
 	 						 			   ns.declarator_id(((Token)((Token)yyVals[0+yyTop])).text);  /* can introduce names into name-space  	*/
 	  									}
  break;
case 100:
					// line 564 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 101:
					// line 565 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 102:
					// line 566 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 103:
					// line 567 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 104:
					// line 568 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 105:
					// line 569 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 106:
					// line 573 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 107:
					// line 574 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 108:
					// line 575 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 109:
					// line 576 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 110:
					// line 580 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 111:
					// line 581 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 112:
					// line 585 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 113:
					// line 586 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 114:
					// line 590 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 115:
					// line 591 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 116:
					// line 596 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 117:
					// line 597 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 118:
					// line 598 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 119:
					// line 602 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 120:
					// line 603 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 121:
					// line 607 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 122:
					// line 608 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 123:
					// line 609 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 124:
					// line 613 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 125:
					// line 614 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 126:
					// line 618 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 127:
					// line 619 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 128:
					// line 623 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 129:
					// line 624 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 130:
					// line 625 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 131:
					// line 629 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 132:
					// line 630 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 133:
					// line 631 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 134:
					// line 632 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 135:
					// line 633 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 136:
					// line 634 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 137:
					// line 635 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 138:
					// line 636 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 139:
					// line 637 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 140:
					// line 641 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 141:
					// line 642 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 142:
					// line 643 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 143:
					// line 644 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 144:
					// line 645 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 145:
					// line 646 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 146:
					// line 650 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 147:
					// line 651 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 148:
					// line 652 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 149:
					// line 656 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 150:
					// line 657 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 151:
					// line 661 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 152:
					// line 662 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 153:
					// line 663 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 154:
					// line 664 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 155:
					// line 668 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 156:
					// line 669 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 157:
					// line 673 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 158:
					// line 674 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 159:
					// line 675 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 160:
					// line 679 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 161:
					// line 680 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 162:
					// line 681 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 163:
					// line 682 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 164:
					// line 686 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 165:
					// line 687 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 166:
					// line 688 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 167:
					// line 689 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 168:
					// line 690 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 169:
					// line 694 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 170:
					// line 695 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 171:
					// line 700 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 172:
					// line 701 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); 
 																  ((Token)yyVal).text +=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text;
 																}
  break;
case 173:
					// line 707 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 174:
					// line 708 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 175:
					// line 709 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 176:
					// line 710 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 177:
					// line 711 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 178:
					// line 712 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 179:
					// line 713 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 180:
					// line 714 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 181:
					// line 715 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 182:
					// line 716 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 183:
					// line 717 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 184:
					// line 721 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 185:
					// line 722 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-4+yyTop]); ((Token)yyVal).text+="?"+((Token)yyVals[-2+yyTop]).text+":"+((Token)yyVals[0+yyTop]).text; }
  break;
case 186:
					// line 726 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 187:
					// line 731 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 188:
					// line 732 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 189:
					// line 736 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 190:
					// line 737 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 191:
					// line 741 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 192:
					// line 742 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 193:
					// line 746 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 194:
					// line 747 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 195:
					// line 751 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 196:
					// line 752 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 197:
					// line 756 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 198:
					// line 757 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 199:
					// line 758 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 200:
					// line 762 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 201:
					// line 763 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 202:
					// line 764 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 203:
					// line 765 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 204:
					// line 766 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 205:
					// line 770 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 206:
					// line 771 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 207:
					// line 772 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-1+yyTop]).text+((Token)yyVals[0+yyTop]).text; }
  break;
case 208:
					// line 776 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 209:
					// line 777 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+="+"+((Token)yyVals[0+yyTop]).text; }
  break;
case 210:
					// line 778 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+="-"+((Token)yyVals[0+yyTop]).text; }
  break;
case 211:
					// line 782 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 212:
					// line 783 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+="*"+((Token)yyVals[0+yyTop]).text; }
  break;
case 213:
					// line 784 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+="/"+((Token)yyVals[0+yyTop]).text;  }
  break;
case 214:
					// line 785 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); ((Token)yyVal).text+="%"+((Token)yyVals[0+yyTop]).text;  }
  break;
case 215:
					// line 789 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 216:
					// line 790 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 217:
					// line 794 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 218:
					// line 795 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-1+yyTop]); ((Token)yyVal).text+=((Token)yyVals[0+yyTop]).text; }
  break;
case 219:
					// line 796 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-1+yyTop]); ((Token)yyVal).text+=((Token)yyVals[0+yyTop]).text; }
  break;
case 220:
					// line 797 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-1+yyTop]); ((Token)yyVal).text+=((Token)yyVals[0+yyTop]).text; }
  break;
case 221:
					// line 798 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-1+yyTop]); ((Token)yyVal).text+=((Token)yyVals[0+yyTop]).text; }
  break;
case 222:
					// line 799 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-3+yyTop]); ((Token)yyVal).text+=((Token)yyVals[-2+yyTop]).text; }
  break;
case 223:
					// line 803 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 224:
					// line 804 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 225:
					// line 805 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 226:
					// line 806 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 227:
					// line 807 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 228:
					// line 808 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 229:
					// line 813 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 230:
					// line 814 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-3+yyTop]); }
  break;
case 231:
					// line 815 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-3+yyTop]); }
  break;
case 232:
					// line 816 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-3+yyTop]); }
  break;
case 233:
					// line 817 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); }
  break;
case 234:
					// line 818 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); }
  break;
case 235:
					// line 819 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); }
  break;
case 236:
					// line 820 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-1+yyTop]); }
  break;
case 237:
					// line 821 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-1+yyTop]); }
  break;
case 238:
					// line 825 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 239:
					// line 826 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 240:
					// line 827 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 241:
					// line 828 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[-2+yyTop]); 
 												  ((Token)yyVal).text += ((Token)yyVals[-1+yyTop]).text + ")";
 												}
  break;
case 242:
					// line 835 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 243:
					// line 836 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  {			}
  break;
case 244:
					// line 837 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 245:
					// line 838 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { /* ignore */ }
  break;
case 246:
					// line 842 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 247:
					// line 843 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 248:
					// line 844 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 249:
					// line 845 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
					// line 1458 "-"
        }
        yyTop -= YyLenClass.yyLen[yyN];
        yyState = yyStates[yyTop];
        int yyM = YyLhsClass.yyLhs[yyN];
        if (yyState == 0 && yyM == 0) {
          if (yydebug != null) yydebug.shift(0, yyFinal);
          yyState = yyFinal;
          if (yyToken < 0) {
            yyToken = yyLex.advance() ? yyLex.token() : 0;
            if (yydebug != null)
               yydebug.lex(yyState, yyToken,yyname(yyToken), yyLex.value());
          }
          if (yyToken == 0) {
            if (yydebug != null) yydebug.accept(yyVal);
            return yyVal;
          }
          continue yyLoop;
        }
        if ((yyN = YyGindexClass.yyGindex[yyM]) != 0 && (yyN += yyState) >= 0
            && yyN < YyTableClass.yyTable.length && YyCheckClass.yyCheck[yyN] == yyState)
          yyState = YyTableClass.yyTable[yyN];
        else
          yyState = YyDgotoClass.yyDgoto[yyM];
        if (yydebug != null) yydebug.shift(yyStates[yyTop], yyState);
	 continue yyLoop;
      }
    }
  }

  protected static final class YyLhsClass {

    public static final short yyLhs [] = {              -1,
         69,   70,   71,   72,   73,   74,   75,   76,   77,   78,
         79,    1,    1,    1,    0,    0,    0,    0,    0,   67,
         67,   67,   67,   18,   18,   18,    9,    9,   35,   65,
         65,   66,   66,   66,   66,   66,   66,   51,   51,   51,
         51,   51,   19,   19,   19,   19,   50,   50,   54,   54,
         54,   54,   54,   54,   54,   54,   55,   55,   55,   55,
         45,   45,   57,   57,   57,   59,   59,   39,   39,   39,
         60,   60,   21,   21,   21,   21,   80,   40,   37,   37,
         37,   37,   41,   41,   42,   42,   42,   58,   58,   58,
         58,   58,   12,   12,   13,   13,   53,   53,   11,   11,
         11,   11,   11,   11,   11,   30,   30,   30,   30,   44,
         44,   64,   64,   63,   63,   52,   52,   52,   62,   62,
         23,   23,   23,   22,   22,   43,   43,    2,    2,    2,
         10,   10,   10,   10,   10,   10,   10,   10,   10,   38,
         38,   38,   38,   38,   38,   26,   26,   26,   17,   17,
          7,    7,    7,    7,   61,   61,   34,   34,   34,   24,
         24,   24,   24,   25,   25,   25,   25,   25,   16,   16,
         47,   47,    5,    5,    5,    5,    5,    5,    5,    5,
          5,    5,    5,   49,   49,    8,   28,   28,   27,   27,
         20,   20,   15,   15,    4,    4,   14,   14,   14,   33,
         33,   33,   33,   33,   36,   36,   36,    3,    3,    3,
         29,   29,   29,   29,    6,    6,   46,   46,   46,   46,
         46,   46,   68,   68,   68,   68,   68,   68,   31,   31,
         31,   31,   31,   31,   31,   31,   31,   32,   32,   32,
         32,   48,   48,   48,   48,   56,   56,   56,   56,
    };
  } /* End of class YyLhsClass */

  protected static final class YyLenClass {

    public static final short yyLen [] = {           2,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    3,    1,    1,    1,    2,    1,    2,    1,    4,
          4,    5,    5,    2,    2,    2,    3,    4,    2,    1,
          2,    1,    2,    1,    2,    1,    2,    2,    1,    1,
          1,    1,    2,    3,    4,    5,    2,    1,    1,    1,
          1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
          1,    1,    6,    7,    2,    1,    1,    1,    2,    1,
          1,    3,    2,    6,    3,    7,    0,    5,    1,    2,
          1,    2,    1,    3,    1,    2,    3,    4,    5,    5,
          6,    2,    1,    3,    1,    3,    2,    3,    1,    3,
          3,    4,    6,    5,    6,    1,    2,    2,    3,    1,
          2,    1,    3,    1,    3,    4,    3,    4,    1,    3,
          1,    3,    4,    1,    3,    2,    3,    1,    1,    2,
          3,    2,    3,    3,    4,    2,    3,    3,    4,    1,
          1,    1,    1,    1,    1,    3,    4,    3,    1,    2,
          4,    5,    5,    6,    1,    2,    5,    7,    5,    5,
          7,    6,    7,    3,    2,    2,    2,    3,    1,    3,
          1,    3,    1,    1,    1,    1,    1,    1,    1,    1,
          1,    1,    1,    1,    5,    1,    1,    3,    1,    3,
          1,    3,    1,    3,    1,    3,    1,    3,    3,    1,
          3,    3,    3,    3,    1,    3,    3,    1,    3,    3,
          1,    3,    3,    3,    1,    4,    1,    2,    2,    2,
          2,    4,    1,    1,    1,    1,    1,    1,    1,    4,
          4,    4,    3,    3,    3,    2,    2,    1,    1,    1,
          3,    1,    2,    3,    4,    1,    1,    1,    1,
    };
  } /* End class YyLenClass */

  protected static final class YyDefRedClass {

    public static final short yyDefRed [] = {            0,
         19,    0,    0,   15,   17,    0,    0,    0,   16,   18,
         39,   40,   41,   42,   57,   58,   59,   60,   61,   62,
         99,    0,    0,   25,    0,   24,   71,    0,   26,    0,
          0,    0,    0,   48,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,  108,    0,  110,    0,    0,
          0,    0,   37,   35,   33,   43,   30,    0,    0,    0,
          0,   29,    2,    0,    0,   54,   50,   51,   52,   53,
         49,   66,   67,    0,   47,   55,   56,    0,   38,    0,
         20,    0,   21,   97,    0,  100,  109,  111,  246,  247,
        248,  249,  240,    0,    0,    0,  238,  101,    0,  223,
        224,  225,  226,  227,  228,    0,    0,  211,    0,    0,
          0,    0,    0,    0,    0,    0,  229,    0,    0,  215,
        186,  239,    0,    0,    0,    0,   44,   31,    0,    0,
          0,   72,    0,    0,   27,   14,   13,    0,    0,    0,
          0,    0,   22,   23,   98,    0,  221,    0,  218,  219,
          0,    2,    0,    0,    0,  169,  171,    0,    0,    0,
          0,  102,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,  236,  237,    0,    0,    0,    0,    0,
          0,    0,    0,    0,  220,  119,    4,  114,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,  149,
          0,  141,    0,  142,  144,  145,  140,  143,  155,    0,
          0,    0,    0,   45,    0,   28,    0,    0,   93,    0,
          2,    0,    0,    0,    0,  241,    0,    0,   82,  174,
        175,  176,  177,  178,  179,  180,  181,  182,  183,  173,
          0,   80,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,  213,  214,  212,  235,    0,  233,  242,    0,
          0,    0,  234,    0,    0,    0,    0,    0,    0,  104,
          0,    4,    0,    4,    0,    0,    1,  121,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,  165,  166,
        167,    0,    0,  150,  156,    0,    0,    0,    0,  151,
         46,    0,   88,    0,    0,   12,    0,   70,    0,   68,
          0,  222,  170,    0,    0,  127,    0,    0,  216,  172,
          0,  232,    0,  231,  243,  230,  120,  105,  113,  115,
        103,    0,    2,    0,    2,  117,    0,  124,   74,    1,
          0,  148,    0,    0,    0,    0,    0,  164,  168,  146,
        152,    0,  153,   96,   89,   94,   90,    0,    0,   69,
          0,    0,  136,    0,    0,  132,    0,    0,    0,    0,
        185,  244,    0,  118,  116,  122,    0,   76,  147,    0,
          0,    0,    0,    0,  154,   91,    0,   63,    0,    0,
         83,    0,  131,  137,  133,  138,    0,  134,    0,  245,
        123,  125,    0,  159,  160,    0,    0,    0,   64,   86,
          0,    0,    0,  139,  135,    0,    0,  162,    0,   84,
         78,   87,  158,  161,  163,
    };
  } /* End of class YyDefRedClass */

  protected static final class YyDgotoClass {

    public static final short yyDgoto [] = {             3,
        211,  374,  106,  107,  251,  108,  212,  109,   57,  327,
         43,  228,  229,  110,  111,  213,  214,    4,   26,  112,
         27,  347,  287,  215,  216,  217,  113,  114,  115,   44,
        116,  117,  118,  218,   29,  119,  152,  219,  319,  320,
        400,  401,  153,   47,   30,  155,  156,  270,  157,   31,
         32,  188,   45,   75,   34,  122,   76,   77,   78,   64,
        220,  189,  190,  375,   58,   59,    5,  123,  140,   65,
         60,  223,  142,  371,    6,  192,   38,   51,  126,  321,
    };
  } /* End of class YyDgotoClass */

  protected static final class YySindexClass {

    public static final short yySindex [] = {         -213,
          0,  145, -247,    0,    0,  981, -233, -117,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,  -31,  -19,    0,  -13,    0,    0,  -27,    0,  805,
        805,  805,  805,    0,  231,  -31,  443, -204,   -8,  211,
         29,  254,  -13,  -27,  263,    0,  -19,    0,  510,  268,
          0,  -13,    0,    0,    0,    0,    0,  805,  -31,  230,
        -31,    0,    0,  316,  319,    0,    0,    0,    0,    0,
          0,    0,    0, -107,    0,    0,    0, -157,    0,  335,
          0,  351,    0,    0,  -13,    0,    0,    0,    0,    0,
          0,    0,    0,  653,  666,  666,    0,    0,  305,    0,
          0,    0,    0,    0,    0,  177,  368,    0,  315,  -62,
        317,  296,  140,  -18,  356,   30,    0,  -10,   62,    0,
          0,    0,  677,  -26,  361,    0,    0,    0,  -13,  -27,
        129,    0,  805,  373,    0,    0,    0,  108,  339,  199,
          0,  363,    0,    0,    0,  305,    0,  677,    0,    0,
        333,    0,  455,  343,  780,    0,    0,  343,  677,  677,
        677,    0,  677,  677,  677,  677,  677,  677,  677,  677,
        677,  677, -157,    0,    0,  229,  367, -157,  677,  677,
        677,  677,  677,  677,    0,    0,    0,    0,  342,  453,
        470,  805,  536,  463,    0,  -13,    0,  677,  471,  490,
        494,  496,  207,  511, -157,  486,  499,  549,    0,    0,
        502,    0,  257,    0,    0,    0,    0,    0,    0,  207,
        129, 1000,  437,    0,  805,    0,  512,   40,    0,  108,
          0,  449,  324,  534,  677,    0,  117,  677,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
        677,    0,  356,  356,  -62,  -10,  -10,  368,  317,  296,
        140,   49,    0,    0,    0,    0,  542,    0,    0,  358,
       -157,  133,    0,   62,   62,   62,   62,  177,  177,    0,
        266,    0,  280,    0,  -36,  536,    0,    0,  536,    0,
        528,  207,  677,  677,  677,  278,  612,  537,    0,    0,
          0,  267,  207,    0,    0,  473,  207,  477,    0,    0,
          0,  677,    0, -101,   59,    0,  324,    0,    0,    0,
        343,    0,    0,  281,  574,    0,  130,  147,    0,    0,
        677,    0,  367,    0,    0,    0,    0,    0,    0,    0,
          0,  -20,    0,  -32,    0,    0,   81,    0,    0,    0,
        207,    0,  360,  386,  415,  566,  612,    0,    0,    0,
          0,  484,    0,    0,    0,    0,    0,  -90,    0,    0,
        485,    2,    0,  570,  572,    0,  522,  581,  623,  130,
          0,    0, -157,    0,    0,    0,  495,    0,    0,  207,
        207,  207,  677,  639,    0,    0,  506,    0,  677,  580,
          0,  576,    0,    0,    0,    0,  599,    0,  548,    0,
          0,    0,  325,    0,    0,  430,  207,  498,    0,    0,
          2,  587,  677,    0,    0,  207,  588,    0,  207,    0,
          0,    0,    0,    0,    0,
    };
  } /* End of class YySindexClass */

  protected static final class YyRindexClass {

    public static final short yyRindex [] = {          867,
          0,    0,  867,    0,    0,  713,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,  -12,    0, 1134,    0,    0,    0,    0,  689,
        905,  961, 1202,    0,    0,  590,    0,    0,    0,    0,
          0,    0,  323,    0,    0,    0,   50,    0,    0,    0,
       1182, 1160,    0,    0,    0,    0,    0, 1202,  590,    0,
          0,    0,    0,  590,    0,    0,    0,    0,    0,    0,
          0,    0,    0,  366,    0,    0,    0,  -89,    0,    0,
          0,    0,    0,    0,  420,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0, 2019,    0,
          0,    0,    0,    0,    0, 1654,   51,    0,    0, 2140,
       1976,  160,  321,  468, 1584, 1544,    0, 2035, 1920,    0,
          0,    0,    0, 1574,    0, 1223,    0,    0,  138,    0,
       1109,    0, 1202,    0,    0,    0,    0,    0,  747,    0,
        785,    0,    0,    0,    0, 2019,    0,    0,    0,    0,
          0,    0,    0,  927, 1572,    0,    0, 1086,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,  366,    0,    0,    0,    0,  366,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,  617,
          0,  713,    0,    0,  122,  300, 1269,    0,    0,    0,
          0,    0,  -69,    0,  366,    0,    0,    0, 1517,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,  -56,
       1109,    0,    0,    0, 1202,    0,  111,    0,    0,    0,
          0,    0, 1602,    0,    0,    0,  619,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0, 1612, 1641, 2163, 2058, 2081, 1292, 1999, 1088,
        723,    0,    0,    0,    0,    0,    0,    0,    0,    0,
        366,    0,    0, 1943, 1966, 1989, 2012, 1680, 1897,    0,
          0,    0, 1574,    0,  500,    0,    0,    0,    0,  275,
          0,  -69,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,  -69,    0,    0,    0,  -56,    0,  821,    0,
          0,    0,    0,    0,    0,    0, 1602,    0, 1459,    0,
       2019,    0,    0, 1574,    0,    0,  505,  632,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0, 1574,    0,  513,    0,    0,    0,    0,    0,    0,
        -69,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0, 1459,    0,
          0,    0,    0,    0,    0,    0,    0, 1574,    0,  515,
          0,    0,  366,    0,    0,    0,    0,    0,    0,  -69,
        -69,  -69,    0,    0,    0,    0,    0,    0,    0,  590,
          0,  298,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,  192,    0,    0,    0,  -69,    0,    0,    0,
          0,    0,    0,    0,    0,  -69,    0,    0,  -69,    0,
          0,    0,    0,    0,    0,
    };
  } /* End of class YyRindexClass */

  protected static final class YyGindexClass {

    public static final short yyGindex [] = {            0,
        -25,   17,  186,  488,    0,   23,  110,  265,   82, -137,
         -3,  444, -156,  517,  521,  -80, -171,  673,    0,  516,
        628,    0, -187,    0,    0,    0,  524,    0,  289,    4,
          0,    0,  312,    0,    0,  301,  -97, 2071,  377, -127,
          0,  269,  551,    0,  -16,  -48, 1911,  525,  -47,  -73,
          0,  418,   -6,    0,    0,    0,    0,    0,  -76,  697,
        491,    0,    0, -110,  206,   66,  702,    0,    6,  -46,
        104,   89,  573,  338,    0,    0,    0,   34,  -23,    0,
    };
  } /* End of class YyGindexClass */

  protected static final class YyTableClass {

    public static final short yyTable [] = {            33,
        120,  121,   25,  342,  125,   23,   48,  342,   22,   28,
         23,   37,   22,  191,  187,  138,  133,  134,  151,  342,
        373,   23,   23,  365,   52,  158,   46,  106,  106,   63,
         88,  106,   25,    5,  396,   37,   37,   37,   37,   28,
         85,   22,    1,   23,  169,  147,  149,  150,  139,  181,
         87,  182,  141,    3,  325,  129,  239,  129,  325,  399,
        252,  145,  130,   37,  130,  151,    3,  151,    4,  176,
        325,   36,  158,    2,  120,  178,   84,   49,  106,  194,
        158,   79,  154,  314,  158,   39,   40,   24,  262,  107,
        107,  193,  235,  107,  193,   53,   54,   55,  348,  271,
        271,  350,  368,  136,   37,  237,  331,    2,  193,  193,
        120,  120,  120,  193,  120,  120,  120,  120,  120,  120,
        177,  120,  120,  120,  387,  357,  196,  302,   50,  154,
        120,  120,  120,  120,  120,  120,  222,  154,   37,  128,
        107,  154,   56,  193,  193,  185,   50,  266,  125,  120,
        121,   37,  273,  136,   95,   50,  324,  366,   23,   37,
        137,  105,  195,   37,  313,   73,  100,  127,   99,  378,
        101,  102,  290,  103,  193,  193,  333,    3,    8,  298,
         73,   10,    2,  367,  316,  394,  324,  210,   50,  120,
        380,  370,  263,  264,  265,    1,   10,   37,   10,  412,
        189,   41,   42,  189,    7,  386,  380,  325,  163,  164,
        137,  366,  353,  354,  355,    1,  227,  189,  189,  159,
        379,  160,  189,  372,  157,  336,  222,  227,    1,  157,
         37,  157,   50,  157,  157,   95,  157,  325,  346,  105,
        328,  370,  224,  194,  100,  335,   99,  158,  101,  102,
        157,  103,  189,  326,  104,  168,  383,  285,  179,  180,
        329,  105,  316,  120,  121,  210,  100,  407,   99,  268,
        101,  102,   81,  103,   61,  280,  120,  121,  345,   19,
         20,   21,  120,  381,  189,   21,   21,   83,  344,   62,
         21,  186,  349,  173,  174,  175,  384,   21,  385,   50,
        235,  343,  128,   86,  154,  106,  128,  124,  306,  308,
        235,   80,  416,  418,  157,  304,  157,  157,   75,   21,
        324,  373,   23,  193,  193,  359,   37,  328,  183,  184,
        120,  121,  104,   75,  311,    2,  221,  105,  225,    3,
         85,   85,  100,   11,   99,  344,  101,  102,   82,  103,
        120,  121,  131,  422,  104,  388,   85,  410,   11,   61,
         11,  187,    3,   10,  187,  402,   10,  107,  278,  279,
        338,  325,  341,  236,  120,  121,  235,  135,  187,  187,
         10,   10,  282,  187,  144,  281,   89,   90,   91,  197,
         93,   94,  171,   95,   96,  362,  143,  172,  334,  105,
        390,  333,  170,  235,  100,  161,   99,  162,  101,  102,
        165,  103,  167,  187,  402,   11,   12,   13,   14,  166,
         15,  193,   16,   17,   18,  227,  391,   19,   20,  235,
        104,  226,  189,  189,  198,  199,  200,  201,  202,  203,
        204,  205,  206,  207,  208,  187,  209,  253,  254,  157,
        157,  157,  157,  157,  157,  392,  157,  157,  235,    3,
         11,  230,  291,   11,   89,   90,   91,  197,   93,   94,
        427,   95,   96,  235,  256,  257,  157,   11,   11,  274,
        275,  276,  277,  231,  267,  233,   89,   90,   91,   92,
         93,   94,  104,   95,   96,  238,  283,  157,  157,  157,
        157,  157,  157,  157,  157,  157,  157,  157,  184,  157,
        284,  184,  198,  199,  200,  201,  202,  203,  204,  205,
        206,  207,  208,  289,  209,  184,  184,  105,  292,  293,
         72,   73,  100,  294,   99,  295,  101,  102,  429,  103,
          2,  235,  105,    2,  299,  129,   97,  100,  129,   99,
        297,  101,  102,  128,  103,  130,  128,  300,  130,  303,
        184,  310,   89,   90,   91,   92,   93,   94,  105,   95,
         96,  317,  312,  100,  322,   99,  364,  101,  102,  318,
        103,  105,  332,  337,  339,  351,  100,  356,   99,  377,
        101,  102,  184,  103,  187,  358,   15,  361,   16,   17,
         18,  363,   98,   19,   20,  393,  105,  301,  395,  398,
        403,  100,  404,   99,  405,  101,  102,  286,  103,  411,
        104,  406,   97,  421,   89,   90,   91,   92,   93,   94,
        419,   95,   96,  423,   15,  104,   16,   17,   18,  424,
        425,   19,   20,  409,  105,  431,  434,  426,    2,  100,
          1,   99,  258,  101,  102,  105,  103,  112,  286,  126,
        100,  104,   99,  420,  101,  102,  376,  103,   72,   73,
        210,  105,  128,  315,  104,    9,  100,  255,   99,  417,
        101,  102,  260,  103,   97,  105,  259,  432,  132,  430,
        100,  261,  146,  369,  101,  102,  234,  103,  105,  104,
        340,  272,   35,  100,   10,  148,  397,  101,  102,  105,
        103,  307,    0,  232,  100,  408,   99,    0,  101,  102,
          0,  103,    0,    0,    0,    0,    0,   66,   36,   36,
         36,    0,   36,   67,    0,   68,    0,  104,    0,   69,
         70,    0,    0,   71,   72,   73,   74,   36,  104,    0,
          0,    0,   89,   90,   91,   92,   93,   94,    0,   95,
         96,    0,    0,  188,  104,    0,  188,   89,   90,   91,
         92,   93,   94,    0,   95,   96,    0,    0,  104,   36,
        188,  188,    0,    0,    0,  188,   92,   92,   92,    0,
         92,  104,    0,   89,   90,   91,   92,   93,   94,    0,
         95,   96,  104,    0,   92,   92,   89,   90,   91,   92,
         93,   94,   97,   95,   96,  188,    0,    0,    0,    0,
          0,    0,    0,    0,   65,   65,   65,   97,   65,    0,
          0,   89,   90,   91,   92,   93,   94,   92,   95,   96,
        250,    0,   65,   65,    0,    0,    0,  188,    0,    0,
          0,    0,    0,   97,    0,    0,    0,    0,    0,    0,
         54,    0,   54,    0,    0,    0,   97,    0,    0,   89,
         90,   91,   92,   93,   94,   65,   95,   96,    2,   54,
         89,   90,   91,   92,   93,   94,    0,   95,   96,    0,
          0,   97,    0,    0,    0,    0,   89,   90,   91,   92,
         93,   94,    0,   95,   96,    0,    7,    5,    7,    0,
         89,   90,   91,   92,   93,   94,    0,   95,   96,    0,
          0,    0,    0,   89,   90,   91,   92,   93,   94,   97,
         95,   96,    0,    0,   89,   90,   91,   92,   93,   94,
         97,   95,   96,    0,   34,   34,   34,    0,   34,    0,
          0,    0,    0,    0,    0,    0,   97,    0,    0,    0,
          0,    0,    0,   34,    0,    0,   81,   81,   81,    0,
         97,    0,    0,    1,    9,    0,    0,    0,    0,    1,
          0,    1,    0,   97,   81,    1,    1,    0,    0,    1,
          1,    1,    1,    0,   97,   34,  188,    1,    9,    0,
         32,   32,   32,    1,   32,    1,   36,    0,    0,    1,
          1,    0,    0,    1,    1,    1,    1,   81,    0,   32,
         22,    0,   23,    0,    0,    0,    0,    0,    0,    0,
          0,   92,   92,   92,   92,   92,   92,   92,   92,   92,
         92,   92,   92,   92,   92,   92,   92,   92,   92,   92,
         92,   32,    0,    0,  240,  241,  242,  243,  244,  245,
        246,  247,  248,  249,   92,    0,    0,    0,    0,   65,
         65,   65,   65,   65,   65,   65,   65,   65,   65,   65,
         65,   65,   65,   65,   65,   65,   65,   65,   65,    0,
          0,   11,   12,   13,   14,    0,   15,    0,   16,   17,
         18,    0,   65,   19,   20,   54,   54,   54,   54,   54,
         54,   54,   54,   54,   54,   54,   54,   54,   54,   54,
         54,   54,   54,   54,   54,   79,   79,   79,  190,    0,
          0,  190,    0,    0,    0,    0,    0,    0,   54,    0,
          0,    0,    0,   79,    0,  190,  190,    0,    0,    0,
        190,    7,    7,    7,    7,    7,    7,    7,    7,    7,
          7,    7,    7,    7,    7,    7,    7,    7,    7,    7,
          7,    0,    0,    3,    0,    0,   79,   10,    0,    0,
        190,    0,    0,    0,    7,    0,    0,    0,    0,    1,
          9,    0,   10,    0,   10,    1,    0,    1,    0,    3,
          0,    1,    1,   11,    0,    1,    1,    1,    1,    0,
          0,    1,  190,    0,    0,    0,    0,    1,   11,    1,
         11,    0,   34,    1,    1,   73,    0,    1,    1,    1,
          1,    3,    0,    4,    0,    0,    0,    0,    0,    0,
         73,    0,    2,    0,   81,    1,    9,    0,    0,    0,
          0,    1,    0,    1,    0,    0,   10,    1,    1,    0,
          0,    1,    1,    1,    1,    0,   75,   11,   12,   13,
         14,    0,   15,    0,   16,   17,   18,    0,   32,   19,
         20,   75,   11,    2,  309,    0,    0,    0,    0,    0,
         67,    0,   68,    0,    0,    0,   69,   70,   21,    0,
         71,   72,   73,   74,   97,  249,  249,    0,  249,    0,
        249,  249,  249,  249,  249,  249,    0,    0,    0,    0,
          0,    0,    0,    0,    3,    0,   14,  249,  249,  249,
        249,  249,  194,    0,    0,  194,    0,    0,    0,    0,
          0,    0,    0,    0,    0,   98,    0,    0,    0,  194,
        194,    0,    0,    0,  194,    0,    0,    0,    0,  249,
        190,  190,  249,    0,    0,    0,    0,    0,    0,    0,
          1,    0,    0,    0,    0,    0,    1,    0,    1,    0,
          0,    0,    1,    1,  194,  194,    1,    1,    1,    1,
          0,    0,  249,    1,    9,    0,    0,    0,    0,    1,
          0,    1,    0,   79,    0,    1,    1,    0,    0,    1,
          1,    1,    1,    0,    0,  194,  194,    0,   10,   10,
         10,   10,   10,   10,   10,   10,   10,   10,   10,   10,
         10,   10,   10,   10,   10,   10,   10,   10,    0,    0,
          0,    0,    0,    0,   11,   11,   11,   11,   11,   11,
         11,   11,   11,   11,   11,   11,   11,   11,   11,   11,
         11,   11,   11,   11,    0,    0,   97,   97,   97,   97,
         97,   97,   97,   97,   97,   97,   97,   97,   97,   97,
         97,   97,   97,   97,   97,   97,    1,    9,    0,    0,
          0,    0,    1,    0,    1,    0,    0,    0,    1,    1,
          0,    0,    1,    1,    1,    1,    0,   98,   98,   98,
         98,   98,   98,   98,   98,   98,   98,   98,   98,   98,
         98,   98,   98,   98,   98,   98,   98,    0,    0,    0,
          0,    0,  249,  249,  249,  249,  249,  249,  249,  249,
        249,  249,  249,  249,  249,  249,  249,  249,  249,  249,
        249,  249,  249,  238,  238,    0,  238,    0,  238,  238,
        238,  238,  238,  238,  194,  194,    0,    0,    0,    0,
          0,    0,    0,    0,   13,  238,  238,  238,  238,  238,
        217,  217,    0,    6,  217,  217,  217,  217,  217,    0,
        217,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,  217,  217,  217,  217,  217,  217,  238,  215,  215,
        238,    0,  215,  215,  215,  215,  215,    0,  215,    0,
          0,  208,    0,    0,  208,    0,  208,  208,  208,  215,
        215,  215,    0,  215,  215,    0,  217,  217,    0,    0,
        238,  208,  208,  208,    0,  208,  208,    0,    0,  209,
          0,    0,  209,    0,  209,  209,  209,    0,    0,    0,
          0,    0,    0,    0,  215,  215,    0,  217,  217,  209,
        209,  209,    0,  209,  209,    0,  208,  208,  210,    0,
          0,  210,    0,  210,  210,  210,    0,    0,    0,    0,
          0,  205,    0,    0,  205,  215,  215,  205,  210,  210,
        210,    0,  210,  210,  209,  209,    0,  208,  208,    0,
          0,  205,  205,  205,    0,  205,  205,  206,    0,    0,
        206,    0,    0,  206,    0,    0,    0,    0,    0,    0,
          0,    0,    0,  210,  210,  209,  209,  206,  206,  206,
          0,  206,  206,   77,    0,    0,  205,  205,    0,   77,
         77,   77,   77,   77,   77,   77,   77,   77,   77,   77,
         77,   77,   77,    0,  210,  210,    0,    0,    0,    0,
          0,    0,  206,  206,    0,    0,    0,  205,  205,    0,
        238,  238,  238,  238,  238,  238,  238,  238,  238,  238,
        238,  238,  238,  238,  238,  238,  238,  238,  238,  238,
        238,    0,    0,  206,  206,    0,    0,    0,    0,    0,
        217,  217,  217,  217,  217,  217,  217,  217,  217,  217,
        217,  217,  217,  217,  217,  217,  217,  217,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,  215,  215,
        215,  215,  215,  215,  215,  215,    0,    0,    0,    0,
        208,  208,  208,  208,  208,  208,  208,  208,    8,    8,
          8,    8,    8,    8,    8,    8,    8,    8,    8,    8,
          8,    8,    8,    8,    8,    8,    8,    8,  209,  209,
        209,  209,  209,  209,  209,  209,   77,    0,    0,    0,
          0,    0,   77,   77,   77,   77,   77,   77,   77,   77,
         77,   77,   77,   77,   77,   77,    0,  210,  210,  210,
        210,  210,  210,  210,  210,    0,    0,    0,    0,    0,
        205,  205,  205,  205,  205,  205,  205,  205,    0,    0,
          0,    0,    0,    0,  207,    0,    0,  207,    0,    0,
        207,    0,    0,    0,    0,    0,  206,  206,  206,  206,
        206,  206,  206,  206,  207,  207,  207,  200,  207,  207,
        200,    0,    0,  200,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,  200,  200,  200,
        203,  200,  200,  203,    0,    0,  203,    0,    0,  207,
        207,    0,    0,    0,    0,    0,    0,    0,    0,    0,
        203,  203,  203,  204,  203,  203,  204,    0,    0,  204,
          0,    0,  200,  200,    0,    0,  191,    0,    0,  191,
        207,  207,    0,  204,  204,  204,  201,  204,  204,  201,
          0,    0,  201,  191,  191,  203,  203,    0,  191,  192,
          0,    0,  192,  200,  200,    0,  201,  201,  201,  202,
        201,  201,  202,    0,    0,  202,  192,  192,  204,  204,
          0,  192,    0,    0,    0,    0,  203,  203,  191,  202,
        202,  202,  197,  202,  202,  197,    0,    0,  197,    0,
          0,  201,  201,    0,    0,    0,  269,  269,    0,  204,
        204,  192,  197,  197,    0,  198,    0,  197,  198,  191,
        191,  198,    0,  288,  202,  202,    0,    0,    0,    0,
          0,    0,  201,  201,    0,  198,  198,    0,  199,    0,
        198,  199,  192,  192,  199,    0,    0,  197,  197,    0,
          0,    0,    0,    0,    0,  202,  202,    0,  199,  199,
          0,    0,    0,  199,    0,  323,    0,    0,    0,    0,
        198,  198,    0,    0,    0,    0,    0,    0,  197,  197,
          0,  330,    0,  207,  207,  207,  207,  207,  207,  207,
        207,    0,    0,  199,  199,    0,    0,  195,    0,    0,
        195,  198,  198,  195,    0,    0,    0,    0,  200,  200,
        200,  200,  200,  200,    0,    0,  288,  195,  195,  288,
        196,    0,  195,  196,  199,  199,  196,    0,    0,    0,
          0,  203,  203,  203,  203,  203,  203,    0,    0,    0,
        196,  196,    0,    0,    0,  196,    0,    0,    0,    0,
          0,    0,  195,  195,  204,  204,  204,  204,  204,  204,
          0,    0,    0,  382,    0,    0,    0,    0,  191,  191,
          0,    0,    0,    0,    0,  196,  196,  201,  201,  201,
        201,  201,  201,  195,  195,    0,    0,    0,    0,    0,
          0,  192,  192,  296,    0,    0,    0,    0,    0,    0,
        202,  202,  202,  202,  202,  202,  196,  196,    0,    0,
        305,    0,    0,    0,    0,    0,    0,  288,    0,    0,
          0,    0,    0,    1,    0,  197,  197,  197,  197,    1,
          0,    1,    0,    0,    0,    1,    1,    0,    0,    1,
          1,    1,    1,    0,    0,    0,    0,    0,  198,  198,
        198,  198,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,  199,  199,  199,  199,    0,    0,    0,    0,    0,
          0,    0,  352,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,  360,    0,    0,    0,  305,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,  195,  195,    0,    0,    0,    0,    0,    0,
          0,  389,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,  196,  196,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
        413,  414,  415,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,  428,    0,    0,
          0,    0,    0,    0,    0,    0,  433,    0,    0,  435,
    };
  } /* End of class YyTableClass */

  protected static final class YyCheckClass {

    public static final short yyCheck [] = {             6,
         49,   49,    6,   40,   51,   42,   23,   40,   40,    6,
         42,    6,   40,  124,   41,  123,   63,   64,   99,   40,
         41,   42,   42,  125,   28,   99,   23,   40,   41,   36,
         47,   44,   36,  123,  125,   30,   31,   32,   33,   36,
         44,   40,  256,   42,   63,   94,   95,   96,   74,   60,
         47,   62,   78,  123,   91,   59,  154,   61,   91,   58,
        158,   85,   59,   58,   61,  146,  123,  148,  125,   40,
         91,    6,  146,  321,  123,   46,   43,   91,   91,  126,
        154,  286,   99,   44,  158,  319,  320,    6,  169,   40,
         41,   41,   44,   44,   44,   30,   31,   32,  286,  176,
        177,  289,   44,  261,   99,  152,   58,  321,   58,   59,
        159,  160,  161,   63,  163,  164,  165,  166,  167,  168,
         91,  170,  171,  172,   44,  297,  130,  208,   25,  146,
        179,  180,  181,  182,  183,  184,  131,  154,  133,   58,
         91,  158,   33,   93,   94,  123,   43,  173,  195,  198,
        198,  146,  178,  261,   44,   52,   40,  314,   42,  154,
        318,   33,  129,  158,  125,   44,   38,   58,   40,   40,
         42,   43,  196,   45,  124,  125,   44,   40,   34,  205,
         59,   44,   61,  125,  231,  357,   40,   59,   85,  238,
        328,  319,  170,  171,  172,  285,   59,  192,   61,  387,
         41,  319,  320,   44,   60,  125,  344,   91,  271,  272,
        318,  368,  293,  294,  295,  285,  318,   58,   59,   43,
         91,   45,   63,  321,   33,   93,  221,  318,  285,   38,
        225,   40,  129,   42,   43,  125,   45,   91,  285,   33,
        237,  369,  133,  290,   38,  271,   40,  321,   42,   43,
         59,   45,   93,  237,  126,  274,  333,  192,  269,  270,
        238,   33,  309,  312,  312,   59,   38,  378,   40,   41,
         42,   43,   62,   45,   44,  187,  325,  325,  285,  299,
        300,  318,  331,  331,  125,  318,  318,   34,  285,   59,
        318,  318,  287,  264,  265,  266,  343,  318,  345,  196,
         44,  285,  221,   41,  321,  318,  225,   40,  220,  221,
         44,  320,  393,  394,  123,   59,  125,  126,   44,  318,
         40,   41,   42,  273,  274,   59,  321,  324,  267,  268,
        379,  379,  126,   59,  225,   61,  131,   33,  133,   40,
        344,   44,   38,   44,   40,  342,   42,   43,  320,   45,
        399,  399,  123,  400,  126,  350,   59,  383,   59,   44,
         61,   41,   40,   41,   44,  372,   44,  318,  183,  184,
        282,   91,  284,   41,  423,  423,   44,   59,   58,   59,
         58,   59,   41,   63,   34,   44,  258,  259,  260,  261,
        262,  263,   37,  265,  266,  307,   62,   42,   41,   33,
         41,   44,   47,   44,   38,   38,   40,   93,   42,   43,
         94,   45,  273,   93,  421,  287,  288,  289,  290,  124,
        292,   61,  294,  295,  296,  318,   41,  299,  300,   44,
        126,   59,  273,  274,  306,  307,  308,  309,  310,  311,
        312,  313,  314,  315,  316,  125,  318,  159,  160,  258,
        259,  260,  261,  262,  263,   41,  265,  266,   44,   40,
         41,  123,  198,   44,  258,  259,  260,  261,  262,  263,
         41,  265,  266,   44,  163,  164,  285,   58,   59,  179,
        180,  181,  182,  285,  256,  123,  258,  259,  260,  261,
        262,  263,  126,  265,  266,   41,   44,  306,  307,  308,
        309,  310,  311,  312,  313,  314,  315,  316,   41,  318,
         41,   44,  306,  307,  308,  309,  310,  311,  312,  313,
        314,  315,  316,   61,  318,   58,   59,   33,   58,   40,
        302,  303,   38,   40,   40,   40,   42,   43,   41,   45,
         41,   44,   33,   44,   59,   41,  318,   38,   44,   40,
         40,   42,   43,   41,   45,   41,   44,   59,   44,   58,
         93,  125,  258,  259,  260,  261,  262,  263,   33,  265,
        266,  123,   61,   38,   41,   40,  312,   42,   43,  256,
         45,   33,   41,  318,  305,   58,   38,  310,   40,  325,
         42,   43,  125,   45,  274,   59,  292,  125,  294,  295,
        296,  125,   93,  299,  300,   40,   33,   59,  125,  125,
         41,   38,   41,   40,   93,   42,   43,  123,   45,  125,
        126,   41,  318,   44,  258,  259,  260,  261,  262,  263,
        125,  265,  266,   58,  292,  126,  294,  295,  296,   41,
         93,  299,  300,  379,   33,   59,   59,  323,   59,   38,
        285,   40,  165,   42,   43,   33,   45,   41,  123,   41,
         38,  126,   40,  399,   42,   43,   93,   45,  302,  303,
         59,   33,   41,  230,  126,    3,   38,  161,   40,   41,
         42,   43,  167,   45,  318,   33,  166,  423,   61,  421,
         38,  168,   40,  317,   42,   43,  146,   45,   33,  126,
        283,  177,    6,   38,    3,   40,  369,   42,   43,   33,
         45,  221,   -1,  141,   38,   93,   40,   -1,   42,   43,
         -1,   45,   -1,   -1,   -1,   -1,   -1,  285,   40,   41,
         42,   -1,   44,  291,   -1,  293,   -1,  126,   -1,  297,
        298,   -1,   -1,  301,  302,  303,  304,   59,  126,   -1,
         -1,   -1,  258,  259,  260,  261,  262,  263,   -1,  265,
        266,   -1,   -1,   41,  126,   -1,   44,  258,  259,  260,
        261,  262,  263,   -1,  265,  266,   -1,   -1,  126,   91,
         58,   59,   -1,   -1,   -1,   63,   40,   41,   42,   -1,
         44,  126,   -1,  258,  259,  260,  261,  262,  263,   -1,
        265,  266,  126,   -1,   58,   59,  258,  259,  260,  261,
        262,  263,  318,  265,  266,   93,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   40,   41,   42,  318,   44,   -1,
         -1,  258,  259,  260,  261,  262,  263,   91,  265,  266,
         61,   -1,   58,   59,   -1,   -1,   -1,  125,   -1,   -1,
         -1,   -1,   -1,  318,   -1,   -1,   -1,   -1,   -1,   -1,
         40,   -1,   42,   -1,   -1,   -1,  318,   -1,   -1,  258,
        259,  260,  261,  262,  263,   91,  265,  266,   58,   59,
        258,  259,  260,  261,  262,  263,   -1,  265,  266,   -1,
         -1,  318,   -1,   -1,   -1,   -1,  258,  259,  260,  261,
        262,  263,   -1,  265,  266,   -1,   40,  123,   42,   -1,
        258,  259,  260,  261,  262,  263,   -1,  265,  266,   -1,
         -1,   -1,   -1,  258,  259,  260,  261,  262,  263,  318,
        265,  266,   -1,   -1,  258,  259,  260,  261,  262,  263,
        318,  265,  266,   -1,   40,   41,   42,   -1,   44,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,  318,   -1,   -1,   -1,
         -1,   -1,   -1,   59,   -1,   -1,   40,   41,   42,   -1,
        318,   -1,   -1,  285,  286,   -1,   -1,   -1,   -1,  291,
         -1,  293,   -1,  318,   58,  297,  298,   -1,   -1,  301,
        302,  303,  304,   -1,  318,   91,  274,  285,  286,   -1,
         40,   41,   42,  291,   44,  293,  318,   -1,   -1,  297,
        298,   -1,   -1,  301,  302,  303,  304,   91,   -1,   59,
         40,   -1,   42,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,  285,  286,  287,  288,  289,  290,  291,  292,  293,
        294,  295,  296,  297,  298,  299,  300,  301,  302,  303,
        304,   91,   -1,   -1,  275,  276,  277,  278,  279,  280,
        281,  282,  283,  284,  318,   -1,   -1,   -1,   -1,  285,
        286,  287,  288,  289,  290,  291,  292,  293,  294,  295,
        296,  297,  298,  299,  300,  301,  302,  303,  304,   -1,
         -1,  287,  288,  289,  290,   -1,  292,   -1,  294,  295,
        296,   -1,  318,  299,  300,  285,  286,  287,  288,  289,
        290,  291,  292,  293,  294,  295,  296,  297,  298,  299,
        300,  301,  302,  303,  304,   40,   41,   42,   41,   -1,
         -1,   44,   -1,   -1,   -1,   -1,   -1,   -1,  318,   -1,
         -1,   -1,   -1,   58,   -1,   58,   59,   -1,   -1,   -1,
         63,  285,  286,  287,  288,  289,  290,  291,  292,  293,
        294,  295,  296,  297,  298,  299,  300,  301,  302,  303,
        304,   -1,   -1,   40,   -1,   -1,   91,   44,   -1,   -1,
         93,   -1,   -1,   -1,  318,   -1,   -1,   -1,   -1,  285,
        286,   -1,   59,   -1,   61,  291,   -1,  293,   -1,   40,
         -1,  297,  298,   44,   -1,  301,  302,  303,  304,   -1,
         -1,  285,  125,   -1,   -1,   -1,   -1,  291,   59,  293,
         61,   -1,  318,  297,  298,   44,   -1,  301,  302,  303,
        304,  123,   -1,  125,   -1,   -1,   -1,   -1,   -1,   -1,
         59,   -1,   61,   -1,  318,  285,  286,   -1,   -1,   -1,
         -1,  291,   -1,  293,   -1,   -1,  123,  297,  298,   -1,
         -1,  301,  302,  303,  304,   -1,   44,  287,  288,  289,
        290,   -1,  292,   -1,  294,  295,  296,   -1,  318,  299,
        300,   59,  123,   61,  285,   -1,   -1,   -1,   -1,   -1,
        291,   -1,  293,   -1,   -1,   -1,  297,  298,  318,   -1,
        301,  302,  303,  304,  123,   37,   38,   -1,   40,   -1,
         42,   43,   44,   45,   46,   47,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,  123,   -1,   58,   59,   60,   61,
         62,   63,   41,   -1,   -1,   44,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,  123,   -1,   -1,   -1,   58,
         59,   -1,   -1,   -1,   63,   -1,   -1,   -1,   -1,   91,
        273,  274,   94,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
        285,   -1,   -1,   -1,   -1,   -1,  291,   -1,  293,   -1,
         -1,   -1,  297,  298,   93,   94,  301,  302,  303,  304,
         -1,   -1,  124,  285,  286,   -1,   -1,   -1,   -1,  291,
         -1,  293,   -1,  318,   -1,  297,  298,   -1,   -1,  301,
        302,  303,  304,   -1,   -1,  124,  125,   -1,  285,  286,
        287,  288,  289,  290,  291,  292,  293,  294,  295,  296,
        297,  298,  299,  300,  301,  302,  303,  304,   -1,   -1,
         -1,   -1,   -1,   -1,  285,  286,  287,  288,  289,  290,
        291,  292,  293,  294,  295,  296,  297,  298,  299,  300,
        301,  302,  303,  304,   -1,   -1,  285,  286,  287,  288,
        289,  290,  291,  292,  293,  294,  295,  296,  297,  298,
        299,  300,  301,  302,  303,  304,  285,  286,   -1,   -1,
         -1,   -1,  291,   -1,  293,   -1,   -1,   -1,  297,  298,
         -1,   -1,  301,  302,  303,  304,   -1,  285,  286,  287,
        288,  289,  290,  291,  292,  293,  294,  295,  296,  297,
        298,  299,  300,  301,  302,  303,  304,   -1,   -1,   -1,
         -1,   -1,  264,  265,  266,  267,  268,  269,  270,  271,
        272,  273,  274,  275,  276,  277,  278,  279,  280,  281,
        282,  283,  284,   37,   38,   -1,   40,   -1,   42,   43,
         44,   45,   46,   47,  273,  274,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   58,   59,   60,   61,   62,   63,
         37,   38,   -1,  125,   41,   42,   43,   44,   45,   -1,
         47,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   58,   59,   60,   61,   62,   63,   91,   37,   38,
         94,   -1,   41,   42,   43,   44,   45,   -1,   47,   -1,
         -1,   38,   -1,   -1,   41,   -1,   43,   44,   45,   58,
         59,   60,   -1,   62,   63,   -1,   93,   94,   -1,   -1,
        124,   58,   59,   60,   -1,   62,   63,   -1,   -1,   38,
         -1,   -1,   41,   -1,   43,   44,   45,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   93,   94,   -1,  124,  125,   58,
         59,   60,   -1,   62,   63,   -1,   93,   94,   38,   -1,
         -1,   41,   -1,   43,   44,   45,   -1,   -1,   -1,   -1,
         -1,   38,   -1,   -1,   41,  124,  125,   44,   58,   59,
         60,   -1,   62,   63,   93,   94,   -1,  124,  125,   -1,
         -1,   58,   59,   60,   -1,   62,   63,   38,   -1,   -1,
         41,   -1,   -1,   44,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   93,   94,  124,  125,   58,   59,   60,
         -1,   62,   63,  285,   -1,   -1,   93,   94,   -1,  291,
        292,  293,  294,  295,  296,  297,  298,  299,  300,  301,
        302,  303,  304,   -1,  124,  125,   -1,   -1,   -1,   -1,
         -1,   -1,   93,   94,   -1,   -1,   -1,  124,  125,   -1,
        264,  265,  266,  267,  268,  269,  270,  271,  272,  273,
        274,  275,  276,  277,  278,  279,  280,  281,  282,  283,
        284,   -1,   -1,  124,  125,   -1,   -1,   -1,   -1,   -1,
        267,  268,  269,  270,  271,  272,  273,  274,  275,  276,
        277,  278,  279,  280,  281,  282,  283,  284,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  267,  268,
        269,  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,
        267,  268,  269,  270,  271,  272,  273,  274,  285,  286,
        287,  288,  289,  290,  291,  292,  293,  294,  295,  296,
        297,  298,  299,  300,  301,  302,  303,  304,  267,  268,
        269,  270,  271,  272,  273,  274,  285,   -1,   -1,   -1,
         -1,   -1,  291,  292,  293,  294,  295,  296,  297,  298,
        299,  300,  301,  302,  303,  304,   -1,  267,  268,  269,
        270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,   -1,
        267,  268,  269,  270,  271,  272,  273,  274,   -1,   -1,
         -1,   -1,   -1,   -1,   38,   -1,   -1,   41,   -1,   -1,
         44,   -1,   -1,   -1,   -1,   -1,  267,  268,  269,  270,
        271,  272,  273,  274,   58,   59,   60,   38,   62,   63,
         41,   -1,   -1,   44,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   58,   59,   60,
         38,   62,   63,   41,   -1,   -1,   44,   -1,   -1,   93,
         94,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         58,   59,   60,   38,   62,   63,   41,   -1,   -1,   44,
         -1,   -1,   93,   94,   -1,   -1,   41,   -1,   -1,   44,
        124,  125,   -1,   58,   59,   60,   38,   62,   63,   41,
         -1,   -1,   44,   58,   59,   93,   94,   -1,   63,   41,
         -1,   -1,   44,  124,  125,   -1,   58,   59,   60,   38,
         62,   63,   41,   -1,   -1,   44,   58,   59,   93,   94,
         -1,   63,   -1,   -1,   -1,   -1,  124,  125,   93,   58,
         59,   60,   38,   62,   63,   41,   -1,   -1,   44,   -1,
         -1,   93,   94,   -1,   -1,   -1,  176,  177,   -1,  124,
        125,   93,   58,   59,   -1,   38,   -1,   63,   41,  124,
        125,   44,   -1,  193,   93,   94,   -1,   -1,   -1,   -1,
         -1,   -1,  124,  125,   -1,   58,   59,   -1,   38,   -1,
         63,   41,  124,  125,   44,   -1,   -1,   93,   94,   -1,
         -1,   -1,   -1,   -1,   -1,  124,  125,   -1,   58,   59,
         -1,   -1,   -1,   63,   -1,  235,   -1,   -1,   -1,   -1,
         93,   94,   -1,   -1,   -1,   -1,   -1,   -1,  124,  125,
         -1,  251,   -1,  267,  268,  269,  270,  271,  272,  273,
        274,   -1,   -1,   93,   94,   -1,   -1,   38,   -1,   -1,
         41,  124,  125,   44,   -1,   -1,   -1,   -1,  269,  270,
        271,  272,  273,  274,   -1,   -1,  286,   58,   59,  289,
         38,   -1,   63,   41,  124,  125,   44,   -1,   -1,   -1,
         -1,  269,  270,  271,  272,  273,  274,   -1,   -1,   -1,
         58,   59,   -1,   -1,   -1,   63,   -1,   -1,   -1,   -1,
         -1,   -1,   93,   94,  269,  270,  271,  272,  273,  274,
         -1,   -1,   -1,  333,   -1,   -1,   -1,   -1,  273,  274,
         -1,   -1,   -1,   -1,   -1,   93,   94,  269,  270,  271,
        272,  273,  274,  124,  125,   -1,   -1,   -1,   -1,   -1,
         -1,  273,  274,  203,   -1,   -1,   -1,   -1,   -1,   -1,
        269,  270,  271,  272,  273,  274,  124,  125,   -1,   -1,
        220,   -1,   -1,   -1,   -1,   -1,   -1,  387,   -1,   -1,
         -1,   -1,   -1,  285,   -1,  271,  272,  273,  274,  291,
         -1,  293,   -1,   -1,   -1,  297,  298,   -1,   -1,  301,
        302,  303,  304,   -1,   -1,   -1,   -1,   -1,  271,  272,
        273,  274,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,  271,  272,  273,  274,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,  292,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,  303,   -1,   -1,   -1,  307,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,  273,  274,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,  351,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,  273,  274,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
        390,  391,  392,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,  417,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,  426,   -1,   -1,  429,
    };
  } /* End of class YyCheckClass */


  protected static final class YyRuleClass {

    public static final String yyRule [] = {
    "$accept : translation_unit",
    "NS_ntd :",
    "NS_td :",
    "NS_scope_enter :",
    "NS_scope_leave :",
    "NS_struct_push :",
    "NS_struct_pop :",
    "NS_id :",
    "NS_new_parm :",
    "NS_is_typedef :",
    "NS_direct_decl :",
    "NS_ptr_decl :",
    "identifier : NS_ntd TYPEDEF_NAME NS_td",
    "identifier : IDENTIFIER",
    "identifier : ENUMERATION_CONSTANT",
    "translation_unit : external_declaration",
    "translation_unit : translation_unit external_declaration",
    "translation_unit : preprocessor_statements",
    "translation_unit : translation_unit preprocessor_statements",
    "translation_unit : error",
    "preprocessor_statements : INCLUDE '<' HEADERFILE '>'",
    "preprocessor_statements : INCLUDE '\"' HEADERFILE '\"'",
    "preprocessor_statements : INCLUDE '<' PATH HEADERFILE '>'",
    "preprocessor_statements : INCLUDE '\"' PATH HEADERFILE '\"'",
    "external_declaration : NS_id function_definition",
    "external_declaration : NS_id declaration",
    "external_declaration : NS_id untyped_declaration",
    "declaration : declaration_specifiers NS_td ';'",
    "declaration : declaration_specifiers init_declarator_list NS_td ';'",
    "untyped_declaration : init_declarator_list ';'",
    "declaration_list : declaration",
    "declaration_list : declaration_list declaration",
    "declaration_specifiers : storage_class_specifier",
    "declaration_specifiers : storage_class_specifier declaration_specifiers",
    "declaration_specifiers : type_specifier",
    "declaration_specifiers : type_specifier declaration_specifiers",
    "declaration_specifiers : type_qualifier",
    "declaration_specifiers : type_qualifier declaration_specifiers",
    "storage_class_specifier : NS_is_typedef TYPEDEF",
    "storage_class_specifier : EXTERN",
    "storage_class_specifier : STATIC",
    "storage_class_specifier : AUTO",
    "storage_class_specifier : REGISTER",
    "function_definition : declarator compound_statement",
    "function_definition : declarator declaration_list compound_statement",
    "function_definition : declaration_specifiers declarator NS_td compound_statement",
    "function_definition : declaration_specifiers declarator NS_td declaration_list compound_statement",
    "type_specifier : NS_ntd actual_type_specifier",
    "type_specifier : type_adjective",
    "actual_type_specifier : VOID",
    "actual_type_specifier : CHAR",
    "actual_type_specifier : INT",
    "actual_type_specifier : FLOAT",
    "actual_type_specifier : DOUBLE",
    "actual_type_specifier : TYPEDEF_NAME",
    "actual_type_specifier : struct_or_union_specifier",
    "actual_type_specifier : enum_specifier",
    "type_adjective : SHORT",
    "type_adjective : LONG",
    "type_adjective : SIGNED",
    "type_adjective : UNSIGNED",
    "type_qualifier : CONST",
    "type_qualifier : VOLATILE",
    "struct_or_union_specifier : struct_or_union NS_struct_push '{' struct_declaration_list NS_struct_pop '}'",
    "struct_or_union_specifier : struct_or_union identifier NS_struct_push '{' struct_declaration_list NS_struct_pop '}'",
    "struct_or_union_specifier : struct_or_union identifier",
    "struct_or_union : STRUCT",
    "struct_or_union : UNION",
    "struct_declaration_list : struct_declaration",
    "struct_declaration_list : struct_declaration_list struct_declaration",
    "struct_declaration_list : error",
    "init_declarator_list : init_declarator",
    "init_declarator_list : init_declarator_list ',' init_declarator",
    "init_declarator : direct_declarator NS_direct_decl",
    "init_declarator : direct_declarator NS_direct_decl NS_td '=' initializer NS_ntd",
    "init_declarator : pointer direct_declarator NS_ptr_decl",
    "init_declarator : pointer direct_declarator NS_ptr_decl NS_td '=' initializer NS_ntd",
    "$$1 :",
    "struct_declaration : $$1 specifier_qualifier_list struct_declarator_list NS_td ';'",
    "specifier_qualifier_list : type_specifier",
    "specifier_qualifier_list : type_specifier specifier_qualifier_list",
    "specifier_qualifier_list : type_qualifier",
    "specifier_qualifier_list : type_qualifier specifier_qualifier_list",
    "struct_declarator_list : struct_declarator",
    "struct_declarator_list : struct_declarator_list ',' struct_declarator",
    "struct_declarator : declarator",
    "struct_declarator : ':' constant_expression",
    "struct_declarator : declarator ':' constant_expression",
    "enum_specifier : ENUM '{' enumerator_list '}'",
    "enum_specifier : ENUM '{' enumerator_list ',' '}'",
    "enum_specifier : ENUM identifier '{' enumerator_list '}'",
    "enum_specifier : ENUM identifier '{' enumerator_list ',' '}'",
    "enum_specifier : ENUM identifier",
    "enumerator_list : enumerator",
    "enumerator_list : enumerator_list ',' enumerator",
    "enumerator : IDENTIFIER",
    "enumerator : IDENTIFIER '=' constant_expression",
    "declarator : direct_declarator NS_direct_decl",
    "declarator : pointer direct_declarator NS_ptr_decl",
    "direct_declarator : IDENTIFIER",
    "direct_declarator : '(' declarator ')'",
    "direct_declarator : direct_declarator '[' ']'",
    "direct_declarator : direct_declarator '[' constant_expression ']'",
    "direct_declarator : direct_declarator NS_scope_enter '(' parameter_type_list ')' NS_scope_leave",
    "direct_declarator : direct_declarator NS_scope_enter '(' ')' NS_scope_leave",
    "direct_declarator : direct_declarator NS_scope_enter '(' identifier_list ')' NS_scope_leave",
    "pointer : '*'",
    "pointer : '*' type_qualifier_list",
    "pointer : '*' pointer",
    "pointer : '*' type_qualifier_list pointer",
    "type_qualifier_list : type_qualifier",
    "type_qualifier_list : type_qualifier_list type_qualifier",
    "parameter_type_list : parameter_list",
    "parameter_type_list : parameter_list ',' ELLIPSIS",
    "parameter_list : parameter_declaration",
    "parameter_list : parameter_list ',' parameter_declaration",
    "parameter_declaration : NS_new_parm declaration_specifiers declarator NS_td",
    "parameter_declaration : NS_new_parm declaration_specifiers NS_td",
    "parameter_declaration : NS_new_parm declaration_specifiers abstract_declarator NS_td",
    "identifier_list : IDENTIFIER",
    "identifier_list : identifier_list ',' IDENTIFIER",
    "initializer : assignment_expression",
    "initializer : '{' initializer_list '}'",
    "initializer : '{' initializer_list ',' '}'",
    "initializer_list : initializer",
    "initializer_list : initializer_list ',' initializer",
    "type_name : specifier_qualifier_list NS_td",
    "type_name : specifier_qualifier_list NS_td abstract_declarator",
    "abstract_declarator : pointer",
    "abstract_declarator : direct_abstract_declarator",
    "abstract_declarator : pointer direct_abstract_declarator",
    "direct_abstract_declarator : '(' abstract_declarator ')'",
    "direct_abstract_declarator : '[' ']'",
    "direct_abstract_declarator : '[' constant_expression ']'",
    "direct_abstract_declarator : direct_abstract_declarator '[' ']'",
    "direct_abstract_declarator : direct_abstract_declarator '[' constant_expression ']'",
    "direct_abstract_declarator : '(' ')'",
    "direct_abstract_declarator : '(' parameter_type_list ')'",
    "direct_abstract_declarator : direct_abstract_declarator '(' ')'",
    "direct_abstract_declarator : direct_abstract_declarator '(' parameter_type_list ')'",
    "statement : labeled_statement",
    "statement : compound_statement",
    "statement : expression_statement",
    "statement : selection_statement",
    "statement : iteration_statement",
    "statement : jump_statement",
    "labeled_statement : identifier ':' statement",
    "labeled_statement : CASE constant_expression ':' statement",
    "labeled_statement : DEFAULT ':' statement",
    "expression_statement : ';'",
    "expression_statement : expression ';'",
    "compound_statement : NS_scope_enter '{' NS_scope_leave '}'",
    "compound_statement : NS_scope_enter '{' statement_list NS_scope_leave '}'",
    "compound_statement : NS_scope_enter '{' declaration_list NS_scope_leave '}'",
    "compound_statement : NS_scope_enter '{' declaration_list statement_list NS_scope_leave '}'",
    "statement_list : statement",
    "statement_list : statement_list statement",
    "selection_statement : IF '(' expression ')' statement",
    "selection_statement : IF '(' expression ')' statement ELSE statement",
    "selection_statement : SWITCH '(' expression ')' statement",
    "iteration_statement : WHILE '(' expression ')' statement",
    "iteration_statement : DO statement WHILE '(' expression ')' ';'",
    "iteration_statement : FOR '(' expression_statement expression_statement ')' statement",
    "iteration_statement : FOR '(' expression_statement expression_statement expression ')' statement",
    "jump_statement : GOTO identifier ';'",
    "jump_statement : CONTINUE ';'",
    "jump_statement : BREAK ';'",
    "jump_statement : RETURN ';'",
    "jump_statement : RETURN expression ';'",
    "expression : assignment_expression",
    "expression : expression ',' assignment_expression",
    "assignment_expression : conditional_expression",
    "assignment_expression : unary_expression assignment_operator assignment_expression",
    "assignment_operator : '='",
    "assignment_operator : MUL_ASSIGN",
    "assignment_operator : DIV_ASSIGN",
    "assignment_operator : MOD_ASSIGN",
    "assignment_operator : ADD_ASSIGN",
    "assignment_operator : SUB_ASSIGN",
    "assignment_operator : LEFT_ASSIGN",
    "assignment_operator : RIGHT_ASSIGN",
    "assignment_operator : AND_ASSIGN",
    "assignment_operator : XOR_ASSIGN",
    "assignment_operator : OR_ASSIGN",
    "conditional_expression : logical_or_expression",
    "conditional_expression : logical_or_expression '?' expression ':' conditional_expression",
    "constant_expression : conditional_expression",
    "logical_or_expression : logical_and_expression",
    "logical_or_expression : logical_or_expression OR_OP logical_and_expression",
    "logical_and_expression : inclusive_or_expression",
    "logical_and_expression : logical_and_expression AND_OP inclusive_or_expression",
    "inclusive_or_expression : exclusive_or_expression",
    "inclusive_or_expression : inclusive_or_expression '|' exclusive_or_expression",
    "exclusive_or_expression : and_expression",
    "exclusive_or_expression : exclusive_or_expression '^' and_expression",
    "and_expression : equality_expression",
    "and_expression : and_expression '&' equality_expression",
    "equality_expression : relational_expression",
    "equality_expression : equality_expression EQ_OP relational_expression",
    "equality_expression : equality_expression NE_OP relational_expression",
    "relational_expression : shift_expression",
    "relational_expression : relational_expression '<' shift_expression",
    "relational_expression : relational_expression '>' shift_expression",
    "relational_expression : relational_expression LE_OP shift_expression",
    "relational_expression : relational_expression GE_OP shift_expression",
    "shift_expression : additive_expression",
    "shift_expression : shift_expression LEFT_OP additive_expression",
    "shift_expression : shift_expression RIGHT_OP additive_expression",
    "additive_expression : multiplicative_expression",
    "additive_expression : additive_expression '+' multiplicative_expression",
    "additive_expression : additive_expression '-' multiplicative_expression",
    "multiplicative_expression : cast_expression",
    "multiplicative_expression : multiplicative_expression '*' cast_expression",
    "multiplicative_expression : multiplicative_expression '/' cast_expression",
    "multiplicative_expression : multiplicative_expression '%' cast_expression",
    "cast_expression : unary_expression",
    "cast_expression : '(' type_name ')' cast_expression",
    "unary_expression : postfix_expression",
    "unary_expression : INC_OP unary_expression",
    "unary_expression : DEC_OP unary_expression",
    "unary_expression : unary_operator cast_expression",
    "unary_expression : SIZEOF unary_expression",
    "unary_expression : SIZEOF '(' type_name ')'",
    "unary_operator : '&'",
    "unary_operator : '*'",
    "unary_operator : '+'",
    "unary_operator : '-'",
    "unary_operator : '~'",
    "unary_operator : '!'",
    "postfix_expression : primary_expression",
    "postfix_expression : postfix_expression '[' argument_expression_list ']'",
    "postfix_expression : postfix_expression '(' argument_expression_list ')'",
    "postfix_expression : postfix_expression '(' error ')'",
    "postfix_expression : postfix_expression '(' ')'",
    "postfix_expression : postfix_expression '.' identifier",
    "postfix_expression : postfix_expression PTR_OP identifier",
    "postfix_expression : postfix_expression INC_OP",
    "postfix_expression : postfix_expression DEC_OP",
    "primary_expression : IDENTIFIER",
    "primary_expression : constant",
    "primary_expression : STRING",
    "primary_expression : '(' expression ')'",
    "argument_expression_list : assignment_expression",
    "argument_expression_list : struct_or_union identifier",
    "argument_expression_list : argument_expression_list ',' assignment_expression",
    "argument_expression_list : argument_expression_list ',' struct_or_union identifier",
    "constant : INTEGER_CONSTANT",
    "constant : CHARACTER_CONSTANT",
    "constant : FLOATING_CONSTANT",
    "constant : ENUMERATION_CONSTANT",
    };
  } /* End of class YyRuleClass */

  protected static final class YyNameClass {

    public static final String yyName [] = {    
    "end-of-file",null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,"'!'","'\"'",null,null,"'%'","'&'",
    null,"'('","')'","'*'","'+'","','","'-'","'.'","'/'",null,null,null,
    null,null,null,null,null,null,null,"':'","';'","'<'","'='","'>'",
    "'?'",null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,"'['",null,"']'","'^'",null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,"'{'","'|'","'}'","'~'",null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,"BAD_TOKEN","INTEGER_CONSTANT","CHARACTER_CONSTANT",
    "FLOATING_CONSTANT","ENUMERATION_CONSTANT","STRING","SIZEOF","PTR_OP",
    "INC_OP","DEC_OP","LEFT_OP","RIGHT_OP","LE_OP","GE_OP","EQ_OP",
    "NE_OP","AND_OP","OR_OP","MUL_ASSIGN","DIV_ASSIGN","MOD_ASSIGN",
    "ADD_ASSIGN","SUB_ASSIGN","LEFT_ASSIGN","RIGHT_ASSIGN","AND_ASSIGN",
    "XOR_ASSIGN","OR_ASSIGN","TYPEDEF_NAME","TYPEDEF","EXTERN","STATIC",
    "AUTO","REGISTER","CHAR","SHORT","INT","LONG","SIGNED","UNSIGNED",
    "FLOAT","DOUBLE","CONST","VOLATILE","VOID","STRUCT","UNION","ENUM",
    "ELLIPSIS","CASE","DEFAULT","IF","SWITCH","WHILE","DO","FOR","GOTO",
    "CONTINUE","BREAK","RETURN","ATTRIBUTE","IDENTIFIER","PATH",
    "HEADERFILE","INCLUDE","THEN","ELSE",
    };
  } /* End of class YyNameClass */


					// line 847 "/cygdrive/c/Documents and Settings/dcg/workspace2/TinyOS/src/tinyOS/nesc/parser/HeaderFileParser.jay"

      // -----------------------------------------------------------
// epilog
// -----------------------------------------------------------
}
					// line 2538 "-"
