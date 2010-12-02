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

					// line 25 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"


  package tinyos.yeti.nesc.parser;

  import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import tinyos.yeti.nesc.parser.language.SemanticError;
import tinyos.yeti.nesc.parser.language.elements.ArgumentExpressionList;
import tinyos.yeti.nesc.parser.language.elements.AssignmentExpression;
import tinyos.yeti.nesc.parser.language.elements.AssignmentOperator;
import tinyos.yeti.nesc.parser.language.elements.ComponentElement;
import tinyos.yeti.nesc.parser.language.elements.ComponentListElement;
import tinyos.yeti.nesc.parser.language.elements.CompoundElement;
import tinyos.yeti.nesc.parser.language.elements.ConfigurationElement;
import tinyos.yeti.nesc.parser.language.elements.ConfigurationImplElement;
import tinyos.yeti.nesc.parser.language.elements.ConnectionElement;
import tinyos.yeti.nesc.parser.language.elements.ConnectionListElement;
import tinyos.yeti.nesc.parser.language.elements.DeclarationElement;
import tinyos.yeti.nesc.parser.language.elements.DelimiterElement;
import tinyos.yeti.nesc.parser.language.elements.DirectDeclaratorElement;
import tinyos.yeti.nesc.parser.language.elements.Element;
import tinyos.yeti.nesc.parser.language.elements.EndpointElement;
import tinyos.yeti.nesc.parser.language.elements.Expression;
import tinyos.yeti.nesc.parser.language.elements.FunctionElement;
import tinyos.yeti.nesc.parser.language.elements.ISpecificationElement;
import tinyos.yeti.nesc.parser.language.elements.IdentifierElement;
import tinyos.yeti.nesc.parser.language.elements.ImplementationElement;
import tinyos.yeti.nesc.parser.language.elements.IncludesElement;
import tinyos.yeti.nesc.parser.language.elements.IncludesListElement;
import tinyos.yeti.nesc.parser.language.elements.InitDeclaratorElement;
import tinyos.yeti.nesc.parser.language.elements.InitializerElement;
import tinyos.yeti.nesc.parser.language.elements.InterfaceElement;
import tinyos.yeti.nesc.parser.language.elements.JumpStatementElement;
import tinyos.yeti.nesc.parser.language.elements.KeywordElement;
import tinyos.yeti.nesc.parser.language.elements.ModuleElement;
import tinyos.yeti.nesc.parser.language.elements.ParameterDeclarationElement;
import tinyos.yeti.nesc.parser.language.elements.PointerElement;
import tinyos.yeti.nesc.parser.language.elements.PostFixExpression;
import tinyos.yeti.nesc.parser.language.elements.PrimaryExpression;
import tinyos.yeti.nesc.parser.language.elements.RenamedIdentifierElement;
import tinyos.yeti.nesc.parser.language.elements.RootElement;
import tinyos.yeti.nesc.parser.language.elements.SelectionStatementElement;
import tinyos.yeti.nesc.parser.language.elements.SpecificationElement;
import tinyos.yeti.nesc.parser.language.elements.SpecificationListElement;
import tinyos.yeti.nesc.parser.language.elements.StatementElement;
import tinyos.yeti.nesc.parser.language.elements.StorageClassSpecifierElement;
import tinyos.yeti.nesc.parser.language.elements.TypeQualifier;
import tinyos.yeti.nesc.parser.language.elements.TypeSpecifierElement;
import tinyos.yeti.nesc.parser.language.elements.UnaryExpression;
import tinyos.yeti.nesc.scanner.Scanner;
import tinyos.yeti.nesc.scanner.Token;

  /**
   * !! 
   * Don't edit this file directly. Instead edit the Jay-File
   * NesCparser.jay and generate the java file using:
   * jay -v NesCparser.jay <skeleton > NesCparser.java
   * !! 
   */

  /** start with<br>
	no argument to suppress debugging<br>
	0, 1, 2, or 3 to animate trapping input, output, or both<br>
	other to trace
  */
  public class NesCparser implements IParser {
	private List elementList = new LinkedList();
	private Scanner scanner = null;
	public NameSpace ns = null;
	private IResource resource = null;
	
	// to prevent cycles
	private LinkedList fileHistory;
	
	private ArrayList<SemanticError> warnings = new ArrayList<SemanticError>();
	
	public void setScanner(Scanner s) {
		this.scanner = s;
	}
	
	public ArrayList<SemanticError> getWarnings() {
		return warnings;
	}
	
	public NesCparser(String[] allTypes, IResource resource, LinkedList fileHistory) {
		this.resource = resource;
		this.fileHistory = fileHistory;
		ns = new NameSpace(allTypes,null);
		warnings = new ArrayList<SemanticError>();
	}
	public NesCparser(IResource resource, LinkedList fileHistory) {
		this.resource = resource;
		this.fileHistory = fileHistory;
		ns = new NameSpace(resource.getLocation().toFile());
		warnings = new ArrayList<SemanticError>();
	}
	
	public LinkedList getFileHistory() {
		return fileHistory;
	}
	
	private void printTypes() {
		System.out.println("-----TYPES-------");
		for (int i = 0; i < ns.getDeclarations().length; i++) {
			//if (typeDefs[i].decl_type == NesCparser.TYPEDEF_NAME) {
				System.out.print("\""+ns.getDeclarations()[i].name+"\",");
				System.out.println(" " +ns.getDeclarations()[i].type);
			//}
		}
		System.out.println("------------------------");
	}
	
       
    public boolean get_idents_only() {
		return ns.get_idents_only();
    }
    
    public int type_of_name(String text) {
    	return ns.type_of_name(text);
   	}
   	
  	
   	boolean debug = false;
   	   
   	boolean followIncludes = true;
   	
   	public void setFollowIncludes(boolean value) {
   		this.followIncludes = value;
   	}
   	
					// line 132 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"



					// line 111 "-"
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
  public static final int AS = 319;
  public static final int CALL = 320;
  public static final int COMPONENTS = 321;
  public static final int CONFIGURATION = 322;
  public static final int EVENT = 323;
  public static final int INTERFACE = 324;
  public static final int POST = 325;
  public static final int PROVIDES = 326;
  public static final int SIGNAL = 327;
  public static final int TASK = 328;
  public static final int USES = 329;
  public static final int INCLUDES = 330;
  public static final int ATOMIC = 331;
  public static final int ASYNC = 332;
  public static final int NORACE = 333;
  public static final int INLINE = 334;
  public static final int LEFTARROW = 335;
  public static final int OFFSETOF = 336;
  public static final int COMMENT = 337;
  public static final int DOCCOMMENT = 338;
  public static final int ENDOFLINECOMMENT = 339;
  public static final int MODULE = 340;
  public static final int IMPLEMENTATION = 341;
  public static final int COMMAND = 342;
  public static final int ERRORSTRING = 343;
  public static final int THEN = 344;
  public static final int ELSE = 345;
  public static final int yyErrorCode = 256;


	 private ParserError parserError = null;



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

  protected static final int yyFinal = 5;

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
	  this.parserError = pe;
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
		if (parserError != null) {
            		parserError.token = yyToken;
            		parserError.state = yyState;
            		parserError.expected = yyExpecting(yyState);
   					parserError.offset = yyLex.getPosition();
       				parserError.length = 1;
  					

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
					// line 297 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
    								  yyVal = new RootElement();	 
    								}
  break;
case 2:
					// line 300 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
									  yyVal = new RootElement(((Element)yyVals[-1+yyTop]),((Element)yyVals[0+yyTop]));
									  ((Element)yyVal).addChildElement(((Element)yyVals[-1+yyTop]));
									  ((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
									}
  break;
case 3:
					// line 305 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
									  yyVal = new RootElement(((Element)yyVals[-1+yyTop]),((Element)yyVals[0+yyTop])); 
									  ((Element)yyVal).addChildElement(((Element)yyVals[-1+yyTop]));
									  ((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
									}
  break;
case 4:
					// line 310 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
									  yyVal = new RootElement(((Element)yyVals[-1+yyTop]),((Element)yyVals[0+yyTop])); 
									  ((Element)yyVal).addChildElement(((Element)yyVals[-1+yyTop]));
									  ((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
									}
  break;
case 5:
					// line 315 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
									  yyVal = new RootElement(((Element)yyVals[0+yyTop]));
									  ((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
									}
  break;
case 6:
					// line 319 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
									  yyVal = new RootElement(((Element)yyVals[0+yyTop])); 
									  ((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
									}
  break;
case 7:
					// line 323 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
									  yyVal = new RootElement(((Element)yyVals[0+yyTop])); 
									  ((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
									}
  break;
case 8:
					// line 330 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
										yyVal = new IncludesListElement(((ArrayList)yyVals[0+yyTop]));
									    ((Element)yyVal).addChilds(((ArrayList)yyVals[0+yyTop]));										
									}
  break;
case 9:
					// line 335 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
										/* adding the includes-array to the resulting includes_list*/
										((Element)yyVals[-1+yyTop]).addChilds(((ArrayList)yyVals[0+yyTop]));
									}
  break;
case 10:
					// line 344 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
					ArrayList al = new ArrayList();
					for (int i = 0; i < ((ArrayList)yyVals[-2+yyTop]).size(); i++) {
						Element e = (Element)((ArrayList)yyVals[-2+yyTop]).get(i);
						IncludesElement f = new IncludesElement(e,((Token)yyVals[-4+yyTop]),((Token)yyVals[0+yyTop]));

						/* parse includes and add the typenames to the scope*/
						if (followIncludes && (!fileHistory.contains(f.getName()+".h"))) {
						  fileHistory.add(f.getName()+".h");
						  f = (IncludesElement) HeaderFileUtil.resolve(resource, f, ns.getDeclarations(), fileHistory);
						  al.add(f);
						  ns.setTypeDefs(f.getDeclarations());
						  if (debug) printTypes();
						}
						/* --------------------------------------						*/
					}
					yyVal = al;
				}
  break;
case 11:
					// line 366 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	
				yyVal = new InterfaceElement(((Token)yyVals[-3+yyTop]).text, ((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop]));
				((Element)yyVal).addChilds(((ArrayList)yyVals[-1+yyTop]));
			}
  break;
case 12:
					// line 374 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
	 		  yyVal = new ModuleElement(((Token)yyVals[-2+yyTop]).text,((Token)yyVals[-3+yyTop]),((Element)yyVals[0+yyTop]));
			  ((Element)yyVal).addChildElement(((Element)yyVals[-1+yyTop]));
 			  ((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop]));	  	 
			  
			}
  break;
case 13:
					// line 384 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			  yyVal = new ImplementationElement(((Token)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop]));
			 /* $<Element>$.addChilds($3);*/
			}
  break;
case 14:
					// line 389 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			  yyVal = new ImplementationElement(((Token)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop]));
			  ((Element)yyVal).addChilds(((ArrayList)yyVals[-1+yyTop]));
			}
  break;
case 15:
					// line 397 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			 yyVal = new ConfigurationElement(((Token)yyVals[-2+yyTop]).text, ((Token)yyVals[-3+yyTop]), ((Element)yyVals[0+yyTop]));
			 ((Element)yyVal).addChildElement(((Element)yyVals[-1+yyTop]));
			 ((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop])); 
		  }
  break;
case 16:
					// line 406 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			  yyVal = new ConfigurationImplElement(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop]));
			  ((Element)yyVal).addChildElement(((Element)yyVals[-2+yyTop])); 
			  ((Element)yyVal).addChildElement(((Element)yyVals[-1+yyTop])); 																
			}
  break;
case 17:
					// line 412 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			  yyVal = new ConfigurationImplElement(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop]));
			  ((Element)yyVal).addChildElement(((Element)yyVals[-1+yyTop])); 
			}
  break;
case 18:
					// line 420 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			  yyVal = new ComponentListElement(((ArrayList)yyVals[0+yyTop]));											  
			}
  break;
case 19:
					// line 423 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
										  ((Element)yyVal).addChilds(((ArrayList)yyVals[0+yyTop]));
										}
  break;
case 20:
					// line 430 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new ArrayList();
			  for (int i = 0; i < ((ArrayList)yyVals[-2+yyTop]).size();i++) {
				  RenamedIdentifierElement e = (RenamedIdentifierElement)((ArrayList)yyVals[-2+yyTop]).get(i);
				  ComponentElement e2 = new ComponentElement((Element)e);
				  e2.setRenamedTo(e.getRenamed());
			  
				  /* parse includes and add the typenames to the scope*/
				  if (followIncludes && (!fileHistory.contains(e2.getName()+".nc"))) {
				  	  fileHistory.add(e2.getName()+".nc");
	 				  if (debug) System.out.println("  TO PARSE -> "+e2.getName());
					  e2 = (ComponentElement) HeaderFileUtil.resolve(resource, e2, ns.getDeclarations(), fileHistory);
					  ns.setTypeDefs(e2.getDeclarations());
					  if (debug) printTypes(); 
				  }
				  /* --------------------------------------	*/
				 
				  ((ArrayList)yyVal).add(e2);
			  } 
			}
  break;
case 21:
					// line 452 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
												  yyVal = new ArrayList(); 
												  ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
												}
  break;
case 22:
					// line 456 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
												  ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
												}
  break;
case 23:
					// line 463 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new RenamedIdentifierElement(((Token)yyVals[0+yyTop]));
			}
  break;
case 24:
					// line 467 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new RenamedIdentifierElement(((Token)yyVals[-2+yyTop]).text,((Token)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop]));
			  ((RenamedIdentifierElement)yyVal).setRenamedTo(((Token)yyVals[0+yyTop]).text);
   		    }
  break;
case 25:
					// line 476 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
				yyVal = new ConnectionListElement(((Element)yyVals[0+yyTop]));
			  	((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 26:
					// line 481 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			    ((Element)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 27:
					// line 488 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
				yyVal = new ConnectionElement(
					((EndpointElement)yyVals[-3+yyTop]).getName() + " = " + ((EndpointElement)yyVals[-1+yyTop]).getName(),
					((EndpointElement)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop])
				);
				((ConnectionElement)yyVal).setLeft(((EndpointElement)yyVals[-3+yyTop]));
				((ConnectionElement)yyVal).setRigth(((EndpointElement)yyVals[-1+yyTop]));
				((ConnectionElement)yyVal).setOperator("=");
		   }
  break;
case 28:
					// line 498 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			    yyVal = new ConnectionElement(
					((EndpointElement)yyVals[-3+yyTop]).getName() + " -> " + ((EndpointElement)yyVals[-1+yyTop]).getName(),
					((EndpointElement)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop])
				);
				((ConnectionElement)yyVal).setLeft(((EndpointElement)yyVals[-3+yyTop]));
				((ConnectionElement)yyVal).setRigth(((EndpointElement)yyVals[-1+yyTop]));
				((ConnectionElement)yyVal).setOperator("->");
			}
  break;
case 29:
					// line 508 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			    yyVal = new ConnectionElement(
				 	((EndpointElement)yyVals[-3+yyTop]).getName() + " <- " + ((EndpointElement)yyVals[-1+yyTop]).getName(),
					((EndpointElement)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop])
				);
				((ConnectionElement)yyVal).setLeft(((EndpointElement)yyVals[-3+yyTop]));
				((ConnectionElement)yyVal).setRigth(((EndpointElement)yyVals[-1+yyTop]));
				((ConnectionElement)yyVal).setOperator("<-");
			}
  break;
case 30:
					// line 522 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	  
					yyVal = new EndpointElement(((Token)yyVals[0+yyTop]));
					((EndpointElement)yyVal).setComponentOrExternalSpecificationName(((Token)yyVals[0+yyTop]).text);
				}
  break;
case 31:
					// line 527 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
				  	yyVal = new EndpointElement(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop]));
				  /* $<EndpointElement>$.setText($1.text+"["+ $3.getLabel(null) + "]");*/
				  ((EndpointElement)yyVal).setText(((Token)yyVals[-3+yyTop]).text+"[]");
					((EndpointElement)yyVal).setComponentOrExternalSpecificationName(((Token)yyVals[-3+yyTop]).text);
				}
  break;
case 32:
					// line 534 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	  
					yyVal = new EndpointElement(((Token)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop])); 
					((EndpointElement)yyVal).setText(((Token)yyVals[-2+yyTop]).text+"."+((Token)yyVals[0+yyTop]).text);
					((EndpointElement)yyVal).setSpecificationElementName(((Token)yyVals[0+yyTop]).text);
					((EndpointElement)yyVal).setComponentElementName(((Token)yyVals[-2+yyTop]).text);
				}
  break;
case 33:
					// line 542 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
					yyVal = new EndpointElement(((Token)yyVals[-5+yyTop]),((Token)yyVals[0+yyTop])); 
					/*$<EndpointElement>$.setText($1.text+"."+$3.text+"["+ $5.getLabel(null) + "]");					*/
					((EndpointElement)yyVal).setText(((Token)yyVals[-5+yyTop]).text+"."+((Token)yyVals[-3+yyTop]).text+"[]");
					((EndpointElement)yyVal).setSpecificationElementName(((Token)yyVals[-3+yyTop]).text);
					((EndpointElement)yyVal).setComponentElementName(((Token)yyVals[-5+yyTop]).text);
				}
  break;
case 34:
					// line 554 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
				yyVal = new SpecificationListElement(((Token)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop]));
				((Element)yyVal).addChilds(((ArrayList)yyVals[-1+yyTop]));
			}
  break;
case 35:
					// line 559 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
				yyVal = new SpecificationListElement(((Token)yyVals[-1+yyTop]),((Token)yyVals[0+yyTop]));
			}
  break;
case 36:
					// line 566 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = ((ArrayList)yyVals[0+yyTop]);
			}
  break;
case 37:
					// line 570 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  for (int i = 0; i < ((ArrayList)yyVals[0+yyTop]).size(); i++) {
				  ((ArrayList)yyVal).add(((ArrayList)yyVals[0+yyTop]).get(i));											  
			  }
			}
  break;
case 38:
					// line 579 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
				yyVal = new ArrayList();
				for (int i = 0; i < ((ArrayList)yyVals[0+yyTop]).size(); i++) {
 				   ISpecificationElement t = (ISpecificationElement)((ArrayList)yyVals[0+yyTop]).get(i);
 				   t.setUses();
					((ArrayList)yyVal).add(t);
				}
			}
  break;
case 39:
					// line 588 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
				yyVal = new ArrayList();
				for (int i = 0; i < ((ArrayList)yyVals[0+yyTop]).size(); i++) {
 				   ISpecificationElement t = (ISpecificationElement)((ArrayList)yyVals[0+yyTop]).get(i);
 				   t.setProvides();
					((ArrayList)yyVal).add(t);
				}
			}
  break;
case 40:
					// line 600 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			  yyVal = new ArrayList();
			  ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
			}
  break;
case 41:
					// line 605 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
				yyVal = ((ArrayList)yyVals[-1+yyTop]);
			}
  break;
case 42:
					// line 612 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
				yyVal = new ArrayList(); 
				((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
			}
  break;
case 43:
					// line 617 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
				 ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
			}
  break;
case 44:
					// line 624 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = ((Element)yyVals[0+yyTop]);
			}
  break;
case 45:
					// line 629 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  SpecificationElement se = new SpecificationElement(((RenamedIdentifierElement)yyVals[-2+yyTop]));


			  /* parse interface and add the typenames to the scope*/
			  if (followIncludes && (!fileHistory.contains(se.getName()+".nc"))) {
			  	  fileHistory.add(se.getName()+".nc");
		     	  if (debug) System.out.println("  TO PARSE -> "+se.getName());
				  se = (SpecificationElement) HeaderFileUtil.resolve(resource, se, ns.getDeclarations(), fileHistory);
				  ns.setTypeDefs(se.getDeclarations());
				  if (debug) printTypes();
			  }
			  /* --------------------------------------	*/
			  
			  yyVal = se;
			}
  break;
case 46:
					// line 646 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  SpecificationElement se = new SpecificationElement(((RenamedIdentifierElement)yyVals[-5+yyTop]));
			 
			  /* parse interface and add the typenames to the scope*/
			  if (followIncludes && (!fileHistory.contains(se.getName()+".nc"))) {
			      fileHistory.add(se.getName()+".nc");
		     	  if (debug) System.out.println("  TO PARSE -> "+se.getName());
				  se = (SpecificationElement) HeaderFileUtil.resolve(resource, se, ns.getDeclarations(), fileHistory);
				   ns.setTypeDefs(se.getDeclarations());
				   if (debug) printTypes();
			  }
			  /* --------------------------------------						*/
			  						
			  yyVal = se;
			}
  break;
case 47:
					// line 666 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 48:
					// line 670 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 49:
					// line 671 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 50:
					// line 672 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 51:
					// line 687 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
				scanner.lex_sync(); 
				ns.ntd(); 
			}
  break;
case 52:
					// line 696 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
				scanner.lex_sync(); 
				ns.td();
			}
  break;
case 53:
					// line 708 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
					ns.scope_push(); 
					ns.td(); 
				 }
  break;
case 54:
					// line 714 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
					ns.scope_pop(); 
				 }
  break;
case 55:
					// line 722 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
					ns.struct_push(); 
					ns.td(); 
				  }
  break;
case 56:
					// line 728 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
					ns.struct_pop(); 
				  }
  break;
case 57:
					// line 734 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			ns.new_declaration(Declaration.DECLARATION_TYPE_NAME_SPACE_DECL); 
		}
  break;
case 58:
					// line 740 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
				ns.new_declaration(Declaration.DECLARATION_TYPE_NAME_SPACE_DECL); 
			  }
  break;
case 59:
					// line 747 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
					ns.set_typedef(); 
				}
  break;
case 60:
					// line 753 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
					ns.direct_declarator(); 
				 }
  break;
case 61:
					// line 759 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { ns.pointer_declarator(); }
  break;
case 62:
					// line 774 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[-1+yyTop]); }
  break;
case 63:
					// line 776 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); 
				ns.declarator_id(((Token)((Token)yyVals[0+yyTop])).text); }
  break;
case 64:
					// line 779 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 65:
					// line 793 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
							  yyVal = new ArrayList();
							  ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
							}
  break;
case 66:
					// line 798 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
							  ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
							}
  break;
case 67:
					// line 805 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
							  yyVal = ((Element)yyVals[0+yyTop]);
							}
  break;
case 68:
					// line 809 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
							  yyVal = null;
							}
  break;
case 69:
					// line 813 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
							  yyVal = null;
							}
  break;
case 70:
					// line 820 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			yyVal = new DeclarationElement(((ArrayList)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop]));
 			Iterator i = ((ArrayList)yyVals[-2+yyTop]).iterator();
 			while(i.hasNext()) {
 				((DeclarationElement)yyVal).addSpecifiers((Element)i.next());
 			}
 		}
  break;
case 71:
					// line 828 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			yyVal = new DeclarationElement(((ArrayList)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop]));
 			Iterator i = ((ArrayList)yyVals[-3+yyTop]).iterator();
 			while(i.hasNext()) {
 				((DeclarationElement)yyVal).addSpecifiers((Element)i.next());
 			}
 			/*todo : init_declarator_list*/
 			
 			i = ((ArrayList)yyVals[-2+yyTop]).iterator();
 			while(i.hasNext()) {
 				((DeclarationElement)yyVal).addInitDeclarator((InitDeclaratorElement)i.next());
 			}
 			
 			
		}
  break;
case 72:
					// line 844 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 73:
					// line 848 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			yyVal = new DeclarationElement(((ArrayList)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop]));
 			Iterator i = ((ArrayList)yyVals[-2+yyTop]).iterator();
 			while(i.hasNext()) {
 				((DeclarationElement)yyVal).addSpecifiers((Element)i.next());
 			}
 		}
  break;
case 74:
					// line 856 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			yyVal = new DeclarationElement(((ArrayList)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop]));
 			Iterator i = ((ArrayList)yyVals[-3+yyTop]).iterator();
 			while(i.hasNext()) {
 				((DeclarationElement)yyVal).addSpecifiers((Element)i.next());
 			}
	 		/*todo : init_declarator_list*/
 			i = ((ArrayList)yyVals[-2+yyTop]).iterator();
 			while(i.hasNext()) {
 				((DeclarationElement)yyVal).addInitDeclarator((InitDeclaratorElement)i.next());
 			}
 		}
  break;
case 76:
					// line 876 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new ArrayList();
 		  ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
 		}
  break;
case 77:
					// line 880 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[-1+yyTop]);
 		  ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
 		}
  break;
case 78:
					// line 887 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			yyVal = new ArrayList();
			((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
		}
  break;
case 79:
					// line 892 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
		  	yyVal = ((ArrayList)yyVals[0+yyTop]);
		  	((ArrayList)yyVal).add(0,((Element)yyVals[-1+yyTop]));
		}
  break;
case 80:
					// line 897 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
		   yyVal = new ArrayList();
		   ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
		}
  break;
case 81:
					// line 902 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
		   yyVal = ((ArrayList)yyVals[0+yyTop]);
		   ((ArrayList)yyVal).add(0,((Element)yyVals[-1+yyTop]));
		}
  break;
case 82:
					// line 907 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
		   yyVal = new ArrayList();
		   ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
		}
  break;
case 83:
					// line 912 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
		   yyVal = ((ArrayList)yyVals[0+yyTop]);
		   ((ArrayList)yyVal).add(0,((Element)yyVals[-1+yyTop]));
		}
  break;
case 84:
					// line 921 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[0+yyTop]);
 		  ((ArrayList)yyVal).add(new KeywordElement(((Token)yyVals[-1+yyTop])));
 		 }
  break;
case 85:
					// line 925 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[0+yyTop]) ;}
  break;
case 86:
					// line 927 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 		 	yyVal = ((ArrayList)yyVals[-1+yyTop]);
 		 	Iterator i = ((ArrayList)yyVals[0+yyTop]).iterator();
 		 	while(i.hasNext()) {
 		 		((ArrayList)yyVals[-1+yyTop]).add((Element)i.next());
 		 	}
 		 }
  break;
case 87:
					// line 935 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
  		   yyVal = ((ArrayList)yyVals[0+yyTop]);
 		 	Iterator i = ((ArrayList)yyVals[-1+yyTop]).iterator();
 		 	while(i.hasNext()) {
 		 		((ArrayList)yyVals[0+yyTop]).add((Element)i.next());
 		 	}
  		}
  break;
case 88:
					// line 945 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new ArrayList(); 
 											  ((ArrayList)yyVal).add(new StorageClassSpecifierElement(((Token)yyVals[0+yyTop]))); 
 											}
  break;
case 89:
					// line 948 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[-1+yyTop]);
   											  ((ArrayList)yyVal).add(new StorageClassSpecifierElement(((Token)yyVals[0+yyTop]))); 
 											}
  break;
case 90:
					// line 951 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[0+yyTop]);
 											  ((ArrayList)yyVal).add(new StorageClassSpecifierElement(((Token)yyVals[-1+yyTop]))); 
 											}
  break;
case 91:
					// line 954 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new ArrayList(); 
 											  ((ArrayList)yyVal).add(new StorageClassSpecifierElement(((Token)yyVals[0+yyTop]))); 
 											}
  break;
case 92:
					// line 957 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[-1+yyTop]);
   											  ((ArrayList)yyVal).add(new StorageClassSpecifierElement(((Token)yyVals[0+yyTop]))); 
 											}
  break;
case 93:
					// line 960 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[0+yyTop]); 
   											  ((ArrayList)yyVal).add(new StorageClassSpecifierElement(((Token)yyVals[-1+yyTop]))); 
 											}
  break;
case 94:
					// line 963 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new ArrayList(); 
 											  ((ArrayList)yyVal).add(new StorageClassSpecifierElement(((Token)yyVals[0+yyTop]))); 
 											}
  break;
case 95:
					// line 966 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[-1+yyTop]);
   											  ((ArrayList)yyVal).add(new StorageClassSpecifierElement(((Token)yyVals[0+yyTop]))); 
 											}
  break;
case 96:
					// line 969 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[0+yyTop]); 
   											  ((ArrayList)yyVal).add(new StorageClassSpecifierElement(((Token)yyVals[-1+yyTop]))); 
 											}
  break;
case 97:
					// line 975 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new ArrayList(); ((ArrayList)yyVal).add(new KeywordElement(((Token)yyVals[0+yyTop]))); }
  break;
case 98:
					// line 976 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new ArrayList(); ((ArrayList)yyVal).add(new KeywordElement(((Token)yyVals[0+yyTop]))); }
  break;
case 99:
					// line 977 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = yyVal = new ArrayList(); 
 											  ((ArrayList)yyVal).add(new KeywordElement(((Token)yyVals[-1+yyTop]))); 
 											  ((ArrayList)yyVal).add(new KeywordElement(((Token)yyVals[0+yyTop]))); 
 											}
  break;
case 100:
					// line 981 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new ArrayList(); 
 											  ((ArrayList)yyVal).add(new KeywordElement(((Token)yyVals[-1+yyTop]))); 
 										      ((ArrayList)yyVal).add(new KeywordElement(((Token)yyVals[0+yyTop]))); 
 											}
  break;
case 101:
					// line 988 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { ns.set_token(((Token)yyVals[0+yyTop]));
 											  yyVal = new StorageClassSpecifierElement(((Token)yyVals[0+yyTop])); }
  break;
case 102:
					// line 990 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StorageClassSpecifierElement(((Token)yyVals[0+yyTop])); }
  break;
case 103:
					// line 991 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StorageClassSpecifierElement(((Token)yyVals[0+yyTop])); }
  break;
case 104:
					// line 992 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StorageClassSpecifierElement(((Token)yyVals[0+yyTop])); }
  break;
case 105:
					// line 993 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StorageClassSpecifierElement(((Token)yyVals[0+yyTop])); }
  break;
case 106:
					// line 999 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StorageClassSpecifierElement(((Token)yyVals[0+yyTop])); }
  break;
case 107:
					// line 1000 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StorageClassSpecifierElement(((Token)yyVals[0+yyTop])); }
  break;
case 108:
					// line 1013 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	
 			yyVal = new FunctionElement(((Element)yyVals[-1+yyTop]),((Element)yyVals[0+yyTop]));	
  			((FunctionElement)yyVal).setDeclarator(((DirectDeclaratorElement)yyVals[-1+yyTop]));
  			((FunctionElement)yyVal).setCompoundStatement(((Element)yyVals[0+yyTop]));
 		}
  break;
case 109:
					// line 1019 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	
 			yyVal = new FunctionElement("_todo 2",((Element)yyVals[-2+yyTop]),((Element)yyVals[0+yyTop]));	
 			((FunctionElement)yyVal).setDeclarator(((DirectDeclaratorElement)yyVals[-2+yyTop]));
 			/* todo decl list*/
 			((FunctionElement)yyVal).setCompoundStatement(((Element)yyVals[0+yyTop]));
 		}
  break;
case 110:
					// line 1026 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	
  			yyVal = new FunctionElement(((ArrayList)yyVals[-3+yyTop]),((Element)yyVals[0+yyTop]));	
  			((FunctionElement)yyVal).setDeclarationSpecifiers(((ArrayList)yyVals[-3+yyTop]));
  			((FunctionElement)yyVal).setDeclarator(((DirectDeclaratorElement)yyVals[-2+yyTop]));
			((FunctionElement)yyVal).setCompoundStatement(((Element)yyVals[0+yyTop]));	
  		}
  break;
case 111:
					// line 1033 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	
  			yyVal = new FunctionElement(((ArrayList)yyVals[-4+yyTop]),((Element)yyVals[0+yyTop]));	
  			((FunctionElement)yyVal).setDeclarationSpecifiers(((ArrayList)yyVals[-4+yyTop]));
  			((FunctionElement)yyVal).setDeclarator(((DirectDeclaratorElement)yyVals[-3+yyTop]));
  			/* setdeclaration_list //todo*/
			((FunctionElement)yyVal).setCompoundStatement(((Element)yyVals[0+yyTop]));
  		}
  break;
case 112:
					// line 1041 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	yyVal = ((Element)yyVals[0+yyTop]);	}
  break;
case 113:
					// line 1048 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
 		    yyVal = new FunctionElement(((ArrayList)yyVals[-3+yyTop]),((Element)yyVals[0+yyTop]));	
  			((FunctionElement)yyVal).setDeclarationSpecifiers(((ArrayList)yyVals[-3+yyTop]));
  			((FunctionElement)yyVal).setDeclarator(((DirectDeclaratorElement)yyVals[-2+yyTop]));
			((FunctionElement)yyVal).setCompoundStatement(((Element)yyVals[0+yyTop]));
		  }
  break;
case 114:
					// line 1055 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
 		    yyVal = new FunctionElement(((ArrayList)yyVals[-4+yyTop]),((Element)yyVals[0+yyTop]));	
  			((FunctionElement)yyVal).setDeclarationSpecifiers(((ArrayList)yyVals[-4+yyTop]));
  			((FunctionElement)yyVal).setDeclarator(((DirectDeclaratorElement)yyVals[-3+yyTop]));
  			/* declaration list //todo*/
			((FunctionElement)yyVal).setCompoundStatement(((Element)yyVals[0+yyTop]));
		  }
  break;
case 115:
					// line 1068 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new TypeSpecifierElement(((Token)yyVals[0+yyTop])); }
  break;
case 116:
					// line 1069 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new TypeSpecifierElement(((Token)yyVals[0+yyTop])); }
  break;
case 117:
					// line 1073 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 118:
					// line 1074 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 119:
					// line 1075 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 120:
					// line 1076 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 121:
					// line 1077 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 122:
					// line 1078 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 123:
					// line 1079 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 124:
					// line 1080 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 125:
					// line 1084 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 126:
					// line 1085 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 127:
					// line 1086 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 128:
					// line 1087 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 129:
					// line 1091 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new TypeQualifier(((Token)yyVals[0+yyTop])); }
  break;
case 130:
					// line 1092 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new TypeQualifier(((Token)yyVals[0+yyTop])); }
  break;
case 131:
					// line 1097 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[-5+yyTop]); }
  break;
case 132:
					// line 1099 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[-5+yyTop]); }
  break;
case 133:
					// line 1101 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 134:
					// line 1105 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 135:
					// line 1106 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 138:
					// line 1116 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	
 			yyVal = new ArrayList(); 
 			((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
 		}
  break;
case 139:
					// line 1121 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	
 			yyVal = ((ArrayList)yyVals[-2+yyTop]);
 			((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
 		}
  break;
case 140:
					// line 1163 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			yyVal = new InitDeclaratorElement(((DirectDeclaratorElement)yyVals[-1+yyTop])); 
 		}
  break;
case 141:
					// line 1167 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
 			yyVal = new InitDeclaratorElement(((DirectDeclaratorElement)yyVals[-5+yyTop]), ((InitializerElement)yyVals[-1+yyTop])); 
		}
  break;
case 142:
					// line 1171 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			yyVal = new InitDeclaratorElement(((PointerElement)yyVals[-2+yyTop]), ((DirectDeclaratorElement)yyVals[-1+yyTop]));  		
 		}
  break;
case 143:
					// line 1175 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			yyVal = new InitDeclaratorElement(((PointerElement)yyVals[-6+yyTop]),((DirectDeclaratorElement)yyVals[-5+yyTop]),((InitializerElement)yyVals[-1+yyTop]));
		}
  break;
case 144:
					// line 1181 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { ns.new_declaration(NesCparser.STRUCT); }
  break;
case 145:
					// line 1182 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 					yyVal = null; 				
 				}
  break;
case 146:
					// line 1189 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 147:
					// line 1190 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; 
 					SemanticError e = new SemanticError("Unamed struct/union fields are not valid in Sun ANSI/ISO C",((Token)yyVals[-1+yyTop])); 
 					e.severity = IMarker.SEVERITY_WARNING;
 					warnings.add(e);
 				}
  break;
case 148:
					// line 1198 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 149:
					// line 1199 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 150:
					// line 1200 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 151:
					// line 1201 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 152:
					// line 1205 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 153:
					// line 1206 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 154:
					// line 1210 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 155:
					// line 1211 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 156:
					// line 1212 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 157:
					// line 1217 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[-3+yyTop]); }
  break;
case 158:
					// line 1219 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[-4+yyTop]); 
  	  SemanticError e = new SemanticError("Trailing commas in enumerations are nonstandard",((Token)yyVals[-1+yyTop])); 
 	  e.severity = IMarker.SEVERITY_WARNING;
 	  warnings.add(e);
 	}
  break;
case 159:
					// line 1225 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[-3+yyTop]); }
  break;
case 160:
					// line 1227 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[-4+yyTop]); 
  	  SemanticError e = new SemanticError("Trailing commas in enumerations are nonstandard",((Token)yyVals[-1+yyTop])); 
 	  e.severity = IMarker.SEVERITY_WARNING;
 	  warnings.add(e);
 	}
  break;
case 161:
					// line 1233 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 162:
					// line 1237 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 163:
					// line 1238 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 164:
					// line 1242 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 165:
					// line 1243 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 166:
					// line 1248 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((DirectDeclaratorElement)yyVals[-1+yyTop]); }
  break;
case 167:
					// line 1250 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((DirectDeclaratorElement)yyVals[-1+yyTop]); 
 		  ((DirectDeclaratorElement)yyVal).setPointer(((PointerElement)yyVals[-2+yyTop]));
 		}
  break;
case 168:
					// line 1257 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 	  ns.declarator_id(((Token)((Token)yyVals[0+yyTop])).text);  /* can introduce names into name-space  	*/
 	  yyVal = new DirectDeclaratorElement("",((Token)yyVals[0+yyTop]));
 	  ((DirectDeclaratorElement)yyVal).setIdentifier(((Token)yyVals[0+yyTop]));
 	}
  break;
case 169:
					// line 1263 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[-1+yyTop]); }
  break;
case 170:
					// line 1265 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((DirectDeclaratorElement)yyVals[-2+yyTop]); }
  break;
case 171:
					// line 1267 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 		yyVal = ((DirectDeclaratorElement)yyVals[-3+yyTop]); 
		((DirectDeclaratorElement)yyVal).setConstantExpression(((Element)yyVals[-1+yyTop]));
 	}
  break;
case 172:
					// line 1272 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
	 	yyVal = ((DirectDeclaratorElement)yyVals[-5+yyTop]); 
	 	((DirectDeclaratorElement)yyVal).setParameterTypeList(((ArrayList)yyVals[-2+yyTop])); 
	 }
  break;
case 173:
					// line 1277 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((DirectDeclaratorElement)yyVals[-4+yyTop]); }
  break;
case 174:
					// line 1279 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			yyVal = ((DirectDeclaratorElement)yyVals[-5+yyTop]); 
 			((DirectDeclaratorElement)yyVal).setIdentifierList(((ArrayList)yyVals[-2+yyTop]));
 		}
  break;
case 175:
					// line 1284 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			yyVal = new DirectDeclaratorElement("",((Token)yyVals[-7+yyTop]),((Token)yyVals[-5+yyTop]));
			((DirectDeclaratorElement)yyVal).setIdentifier(((Token)yyVals[-7+yyTop]),((Token)yyVals[-5+yyTop])); 
 			((DirectDeclaratorElement)yyVal).setIdentifierList(((ArrayList)yyVals[-2+yyTop]));
 		}
  break;
case 176:
					// line 1290 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
	 	yyVal = new DirectDeclaratorElement("",((Token)yyVals[-7+yyTop]),((Token)yyVals[-5+yyTop]));
		((DirectDeclaratorElement)yyVal).setIdentifier(((Token)yyVals[-7+yyTop]),((Token)yyVals[-5+yyTop])); 
	 	((DirectDeclaratorElement)yyVal).setParameterTypeList(((ArrayList)yyVals[-2+yyTop])); 
	 }
  break;
case 177:
					// line 1297 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
	 		yyVal = new DirectDeclaratorElement("",((Token)yyVals[-5+yyTop]),((Token)yyVals[-3+yyTop]));
			((DirectDeclaratorElement)yyVal).setIdentifier(((Token)yyVals[-5+yyTop]),((Token)yyVals[-3+yyTop]));
 		}
  break;
case 178:
					// line 1302 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			yyVal = new DirectDeclaratorElement("",((Token)yyVals[-11+yyTop]),((Token)yyVals[-9+yyTop]));
			((DirectDeclaratorElement)yyVal).setIdentifier(((Token)yyVals[-11+yyTop]),((Token)yyVals[-9+yyTop]));
 			((DirectDeclaratorElement)yyVal).setParameters(((ArrayList)yyVals[-6+yyTop]));
 			((DirectDeclaratorElement)yyVal).setParameterTypeList(((ArrayList)yyVals[-2+yyTop])); 
 		}
  break;
case 179:
					// line 1309 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			yyVal = new DirectDeclaratorElement("",((Token)yyVals[-10+yyTop]),((Token)yyVals[-8+yyTop]));
			((DirectDeclaratorElement)yyVal).setIdentifier(((Token)yyVals[-10+yyTop]),((Token)yyVals[-8+yyTop])); 
 			((DirectDeclaratorElement)yyVal).setParameters(((ArrayList)yyVals[-5+yyTop]));
 		}
  break;
case 180:
					// line 1333 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 													  yyVal = new PointerElement(((Token)yyVals[0+yyTop])); 
 													}
  break;
case 181:
					// line 1336 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 													 yyVal = new PointerElement(((Token)yyVals[-1+yyTop]),((ArrayList)yyVals[0+yyTop])); 
 													}
  break;
case 182:
					// line 1339 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 													 yyVal = new PointerElement(((Token)yyVals[-1+yyTop]),((PointerElement)yyVals[0+yyTop])); 
 													}
  break;
case 183:
					// line 1342 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 													 yyVal = new PointerElement(((Token)yyVals[-2+yyTop]),((PointerElement)yyVals[0+yyTop])); 
 													 ((PointerElement)yyVal).setTypeQualifierList(((ArrayList)yyVals[-1+yyTop]));
 													}
  break;
case 184:
					// line 1349 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
													 yyVal = new ArrayList(); 
										 			 ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
													}
  break;
case 185:
					// line 1353 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 													 ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop])); 
 													}
  break;
case 186:
					// line 1359 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[0+yyTop]); }
  break;
case 187:
					// line 1360 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((ArrayList)yyVals[-2+yyTop]); }
  break;
case 188:
					// line 1365 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			yyVal = new ArrayList(); 
			((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
		}
  break;
case 189:
					// line 1370 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
		}
  break;
case 190:
					// line 1377 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 				yyVal = new ParameterDeclarationElement("",((ArrayList)yyVals[-2+yyTop]),((Element)yyVals[-1+yyTop]));
 				((ParameterDeclarationElement)yyVal).setDeclarationSpecifiers(((ArrayList)yyVals[-2+yyTop]));
 				((ParameterDeclarationElement)yyVal).setDeclarator(((DirectDeclaratorElement)yyVals[-1+yyTop]));
 				
 			}
  break;
case 191:
					// line 1384 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
 				yyVal = new ParameterDeclarationElement("",((ArrayList)yyVals[-1+yyTop]));
 				((ParameterDeclarationElement)yyVal).setDeclarationSpecifiers(((ArrayList)yyVals[-1+yyTop]));
 			
 			}
  break;
case 192:
					// line 1390 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
	 			yyVal = new ParameterDeclarationElement("",((ArrayList)yyVals[-2+yyTop]),((Element)yyVals[-1+yyTop]));
 				((ParameterDeclarationElement)yyVal).setDeclarationSpecifiers(((ArrayList)yyVals[-2+yyTop]));
 				/*$<ParameterDeclaration>$.setAbstractDeclarator($3); // todo*/
 			}
  break;
case 193:
					// line 1398 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
											yyVal = new ArrayList(); 
											((ArrayList)yyVal).add(
												new IdentifierElement(((Token)yyVals[0+yyTop]))
											);
									 	}
  break;
case 194:
					// line 1404 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {  
											((ArrayList)yyVal).add(
												new IdentifierElement(((Token)yyVals[0+yyTop]))
											);
										}
  break;
case 195:
					// line 1412 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new InitializerElement(((AssignmentExpression)yyVals[0+yyTop])); }
  break;
case 196:
					// line 1413 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new InitializerElement(((Token)yyVals[-2+yyTop]),((ArrayList)yyVals[-1+yyTop]),((Token)yyVals[0+yyTop])); 
 										}
  break;
case 197:
					// line 1415 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
										  yyVal = new InitializerElement(((Token)yyVals[-3+yyTop]),((ArrayList)yyVals[-2+yyTop]),((Token)yyVals[-1+yyTop]),((Token)yyVals[0+yyTop]));
 										}
  break;
case 198:
					// line 1421 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 										  yyVal = new ArrayList(); 
										  ((ArrayList)yyVal).add(((InitializerElement)yyVals[0+yyTop]));
 										}
  break;
case 199:
					// line 1425 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { ((ArrayList)yyVal).add(((InitializerElement)yyVals[0+yyTop]));}
  break;
case 200:
					// line 1429 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 201:
					// line 1430 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 202:
					// line 1434 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 203:
					// line 1435 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 204:
					// line 1436 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 205:
					// line 1440 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 206:
					// line 1441 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 207:
					// line 1442 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 208:
					// line 1443 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 209:
					// line 1444 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 210:
					// line 1445 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 211:
					// line 1446 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 212:
					// line 1447 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 213:
					// line 1448 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 214:
					// line 1452 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 215:
					// line 1453 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 216:
					// line 1454 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 217:
					// line 1455 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 218:
					// line 1456 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 219:
					// line 1457 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 220:
					// line 1458 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 221:
					// line 1462 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StatementElement(((Token)yyVals[-2+yyTop]),((Element)yyVals[0+yyTop])); }
  break;
case 222:
					// line 1463 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { /* statement can be null -> 
 																		case BLA :
 																		case BLA2: return TRUE; */
 															  yyVal = new StatementElement(((Token)yyVals[-3+yyTop]),((Element)yyVals[0+yyTop])); }
  break;
case 223:
					// line 1467 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StatementElement(((Token)yyVals[-2+yyTop]),((Element)yyVals[0+yyTop])); }
  break;
case 224:
					// line 1471 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StatementElement(((Token)yyVals[0+yyTop])); }
  break;
case 225:
					// line 1472 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new StatementElement(((Element)yyVals[-1+yyTop]),((Token)yyVals[0+yyTop])); }
  break;
case 226:
					// line 1477 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new CompoundElement(((Token)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop])); }
  break;
case 227:
					// line 1479 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
  			yyVal = new CompoundElement(((Token)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop]));
  			((Element)yyVal).addChilds(((ArrayList)yyVals[-2+yyTop]));
  		}
  break;
case 228:
					// line 1484 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new CompoundElement(((Token)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop])); 
/*  		  $<Element>$.addChilds($3);*/
  		  ((CompoundElement)yyVal).setDeclarations(((ArrayList)yyVals[-2+yyTop]));
  		}
  break;
case 229:
					// line 1489 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new CompoundElement(((Token)yyVals[-4+yyTop]),((Token)yyVals[0+yyTop])); 
/*  		  $<Element>$.addChilds($3);*/
  		  ((CompoundElement)yyVal).setDeclarations(((ArrayList)yyVals[-3+yyTop]));
  		  ((Element)yyVal).addChilds(((ArrayList)yyVals[-2+yyTop]));
  		}
  break;
case 230:
					// line 1497 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 										  yyVal = new ArrayList(); 
 										  ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop]));
 										}
  break;
case 231:
					// line 1501 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { ((ArrayList)yyVal).add(((Element)yyVals[0+yyTop])); }
  break;
case 232:
					// line 1505 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new SelectionStatementElement(((Token)yyVals[-4+yyTop]),((Element)yyVals[0+yyTop])); 
 															  ((SelectionStatementElement)yyVal).setExpression(((Expression)yyVals[-2+yyTop]));
 															  ((SelectionStatementElement)yyVal).setStatement(((StatementElement)yyVals[0+yyTop]));
 															 }
  break;
case 233:
					// line 1509 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new SelectionStatementElement(((Token)yyVals[-6+yyTop]),((Element)yyVals[0+yyTop])); 
 															  ((SelectionStatementElement)yyVal).setExpression(((Expression)yyVals[-4+yyTop]));
 															  ((SelectionStatementElement)yyVal).setStatement(((StatementElement)yyVals[-2+yyTop]));
 															  ((SelectionStatementElement)yyVal).setElseStatement(((StatementElement)yyVals[0+yyTop]));
 															}
  break;
case 234:
					// line 1514 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 235:
					// line 1518 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 236:
					// line 1519 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 237:
					// line 1520 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 238:
					// line 1521 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 239:
					// line 1525 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 240:
					// line 1526 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 241:
					// line 1527 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = null; }
  break;
case 242:
					// line 1528 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new JumpStatementElement(((Token)yyVals[-1+yyTop]),((Token)yyVals[0+yyTop])); 
 											  ((JumpStatementElement)yyVal).setReturn();
 											}
  break;
case 243:
					// line 1531 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new JumpStatementElement(((Token)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop])); 
 											  ((JumpStatementElement)yyVal).setReturn();
 											  ((JumpStatementElement)yyVal).setReturnExpr();
 											}
  break;
case 244:
					// line 1538 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((AssignmentExpression)yyVals[0+yyTop]); }
  break;
case 245:
					// line 1539 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((AssignmentExpression)yyVals[0+yyTop]); }
  break;
case 246:
					// line 1545 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	yyVal = new AssignmentExpression(((Element)yyVals[0+yyTop]));	 }
  break;
case 247:
					// line 1547 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			yyVal = new AssignmentExpression("",((Element)yyVals[-2+yyTop]),((AssignmentExpression)yyVals[0+yyTop]));
 			((Element)yyVal).addChildElement(((Element)yyVals[-2+yyTop]));
 			((Element)yyVal).addChildElement(((Element)yyVals[-1+yyTop]));
 			((Element)yyVal).addChildElement(((AssignmentExpression)yyVals[0+yyTop])); 			
 	    }
  break;
case 248:
					// line 1556 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 249:
					// line 1557 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 250:
					// line 1558 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 251:
					// line 1559 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 252:
					// line 1560 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 253:
					// line 1561 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 254:
					// line 1562 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 255:
					// line 1563 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 256:
					// line 1564 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 257:
					// line 1565 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 258:
					// line 1566 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new AssignmentOperator(((Token)yyVals[0+yyTop])); }
  break;
case 259:
					// line 1571 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 260:
					// line 1573 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[-4+yyTop]); }
  break;
case 261:
					// line 1578 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 262:
					// line 1584 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 263:
					// line 1586 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {  	
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
 		 			
		}
  break;
case 264:
					// line 1596 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 265:
					// line 1598 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));	 
		}
  break;
case 266:
					// line 1607 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 267:
					// line 1609 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 268:
					// line 1618 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 269:
					// line 1620 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 270:
					// line 1629 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 271:
					// line 1631 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 272:
					// line 1640 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 273:
					// line 1642 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 274:
					// line 1648 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 275:
					// line 1657 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 276:
					// line 1659 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 277:
					// line 1665 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 278:
					// line 1671 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 279:
					// line 1677 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 280:
					// line 1686 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 281:
					// line 1688 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 282:
					// line 1694 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 283:
					// line 1703 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 284:
					// line 1705 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 285:
					// line 1711 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 286:
					// line 1720 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 287:
					// line 1722 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	yyVal = new UnaryExpression("",((Token)yyVals[-5+yyTop]),((Token)yyVals[0+yyTop])); }
  break;
case 288:
					// line 1724 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {	yyVal = new UnaryExpression("",((Token)yyVals[-5+yyTop]),((Token)yyVals[0+yyTop])); }
  break;
case 289:
					// line 1726 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 290:
					// line 1732 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 291:
					// line 1738 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
 			  yyVal = ((Element)yyVals[-2+yyTop]);
 			  ((Expression)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
 			  ((Expression)yyVal).addChildElement(((Element)yyVals[0+yyTop]));
			}
  break;
case 292:
					// line 1747 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 293:
					// line 1749 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Element)yyVals[0+yyTop]); }
  break;
case 294:
					// line 1754 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new UnaryExpression(((Element)yyVals[0+yyTop])); }
  break;
case 295:
					// line 1756 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {   yyVal = ((Element)yyVals[0+yyTop]);
 			((UnaryExpression)yyVals[0+yyTop]).setIncOp(); }
  break;
case 296:
					// line 1759 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 	yyVal = ((Element)yyVals[0+yyTop]);
 			((UnaryExpression)yyVals[0+yyTop]).setDecOp(); }
  break;
case 297:
					// line 1762 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 	yyVal = new UnaryExpression(((Element)yyVals[0+yyTop])); 
 			/*$<UnaryExpression>$.setUnaryOperator($1);*/ }
  break;
case 298:
					// line 1765 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 	yyVal = ((Element)yyVals[0+yyTop]);
 			((UnaryExpression)yyVals[0+yyTop]).setSizeOf(); }
  break;
case 299:
					// line 1768 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new UnaryExpression("",((Token)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop])); }
  break;
case 300:
					// line 1771 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new Integer('&'); }
  break;
case 301:
					// line 1772 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new Integer('*'); }
  break;
case 302:
					// line 1773 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new Integer('+'); }
  break;
case 303:
					// line 1774 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new Integer('-'); }
  break;
case 304:
					// line 1775 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new Integer('~'); }
  break;
case 305:
					// line 1776 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new Integer('!'); }
  break;
case 306:
					// line 1794 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression(((PrimaryExpression)yyVals[0+yyTop]));
			}
  break;
case 307:
					// line 1798 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression("case11",((Element)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop]));
			}
  break;
case 308:
					// line 1802 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression(((PrimaryExpression)yyVals[-4+yyTop]), ((Element)yyVals[-6+yyTop]),((Token)yyVals[0+yyTop]));
			  /*todo*/
			}
  break;
case 309:
					// line 1807 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression(((PrimaryExpression)yyVals[-3+yyTop]), ((Element)yyVals[-5+yyTop]),((Token)yyVals[0+yyTop]));
			  /*todo*/
			}
  break;
case 310:
					// line 1813 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression("case115",((Element)yyVals[-6+yyTop]),((Token)yyVals[0+yyTop]));
			}
  break;
case 311:
					// line 1819 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  /*$$ = new PostFixExpression("case55",$1,$4);*/
			  yyVal = new PostFixExpression((ArgumentExpressionList)((Element)yyVals[-1+yyTop]),((Element)yyVals[-3+yyTop]),((Token)yyVals[0+yyTop]));
			  /*todo ..*/
			}
  break;
case 312:
					// line 1826 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression("case56",((Element)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop]));
			}
  break;
case 313:
					// line 1830 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression("case5",((Element)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop]));
			}
  break;
case 314:
					// line 1834 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression("case4",((Element)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop]));
			}
  break;
case 315:
					// line 1838 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression("case3",((Element)yyVals[-1+yyTop]),((Token)yyVals[0+yyTop]));
			}
  break;
case 316:
					// line 1842 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
			  yyVal = new PostFixExpression("case2",((Element)yyVals[-1+yyTop]),((Token)yyVals[0+yyTop]));
			}
  break;
case 317:
					// line 1849 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new PrimaryExpression(((Token)yyVals[0+yyTop])); }
  break;
case 318:
					// line 1851 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new PrimaryExpression(((Token)yyVals[-2+yyTop])); }
  break;
case 319:
					// line 1853 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new PrimaryExpression(((Token)yyVals[0+yyTop])); }
  break;
case 320:
					// line 1855 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = new PrimaryExpression("\""+((Token)yyVals[0+yyTop]).text+"\"",((Token)yyVals[0+yyTop])); }
  break;
case 321:
					// line 1857 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
  	 			yyVal = new PrimaryExpression("("+((Element)yyVals[-1+yyTop]).getName()+")",((Token)yyVals[-2+yyTop]),((Token)yyVals[0+yyTop])); 
  	 		}
  break;
case 322:
					// line 1864 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { 
			yyVal = new ArgumentExpressionList(((AssignmentExpression)yyVals[0+yyTop]));
		}
  break;
case 323:
					// line 1868 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  {
  			((Element)yyVal).addChildElement(new DelimiterElement(((Token)yyVals[-1+yyTop])));
			((Element)yyVal).addChildElement(((AssignmentExpression)yyVals[0+yyTop]));
  		}
  break;
case 324:
					// line 1875 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 325:
					// line 1876 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 326:
					// line 1877 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
case 327:
					// line 1878 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"
  { yyVal = ((Token)yyVals[0+yyTop]); }
  break;
					// line 2346 "-"
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
          0,    0,    0,    0,    0,    0,    0,   29,   29,   81,
         33,   39,   68,   68,   11,   13,   13,    9,    9,   79,
          8,    8,   45,   45,   12,   12,   14,   14,   14,   20,
         20,   20,   20,   49,   49,   87,   87,   88,   88,   84,
         84,   85,   85,   48,   48,   48,    5,    6,    6,    6,
         97,   98,   99,  100,  101,  102,  103,   96,  104,  105,
        106,   71,   71,   71,   86,   86,   27,   27,   27,   16,
         16,   16,   17,   17,  107,   90,   90,   91,   91,   91,
         91,   91,   91,   89,   89,   89,   89,   78,   78,   78,
         78,   78,   78,   78,   78,   78,   77,   77,   77,   77,
         65,   65,   65,   65,   65,   65,   65,   28,   28,   28,
         28,   28,   60,   60,   64,   64,   69,   69,   69,   69,
         69,   69,   69,   69,   70,   70,   70,   70,   58,   58,
         73,   73,   73,   75,   75,   52,   52,   92,   92,   31,
         31,   31,   31,  108,   53,   54,   54,   50,   50,   50,
         50,   55,   55,   56,   56,   56,   74,   74,   74,   74,
         74,   21,   21,   22,   22,   67,   67,   19,   19,   19,
         19,   19,   19,   19,   19,   19,   19,   19,   19,   41,
         41,   41,   41,   93,   93,   83,   83,   82,   82,   66,
         66,   66,   80,   80,   32,   32,   32,   94,   94,   57,
         57,    1,    1,    1,   18,   18,   18,   18,   18,   18,
         18,   18,   18,   51,   51,   51,   51,   51,   51,   51,
         36,   36,   36,   26,   26,   10,   10,   10,   10,   76,
         76,   46,   46,   46,   34,   34,   34,   34,   35,   35,
         35,   35,   35,   25,   25,   61,   61,    4,    4,    4,
          4,    4,    4,    4,    4,    4,    4,    4,   63,   63,
         15,   38,   38,   37,   37,   30,   30,   24,   24,    3,
          3,   23,   23,   23,   44,   44,   44,   44,   44,   47,
         47,   47,    2,    2,    2,   40,   40,   40,   40,   40,
         40,    7,    7,   59,   59,   59,   59,   59,   59,   95,
         95,   95,   95,   95,   95,   42,   42,   42,   42,   42,
         42,   42,   42,   42,   42,   42,   43,   43,   43,   43,
         43,   62,   62,   72,   72,   72,   72,
    };
  } /* End of class YyLhsClass */

  protected static final class YyLenClass {

    public static final short yyLen [] = {           2,
          0,    2,    2,    2,    1,    1,    1,    1,    2,    5,
          5,    4,    3,    4,    4,    5,    4,    1,    2,    5,
          1,    3,    1,    3,    1,    2,    4,    4,    4,    1,
          4,    3,    6,    3,    2,    1,    2,    2,    2,    1,
          3,    1,    2,    1,    5,    8,    2,    1,    1,    1,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    3,    1,    1,    1,    2,    2,    2,    2,    3,
          4,    1,    3,    4,    2,    1,    2,    1,    2,    1,
          2,    1,    2,    2,    1,    2,    2,    1,    2,    2,
          1,    2,    2,    1,    2,    2,    1,    1,    2,    2,
          2,    1,    1,    1,    1,    1,    1,    2,    3,    4,
          5,    1,    4,    5,    2,    1,    1,    1,    1,    1,
          1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
          6,    7,    2,    1,    1,    1,    2,    1,    3,    2,
          6,    3,    7,    0,    3,    3,    2,    1,    2,    1,
          2,    1,    3,    1,    2,    3,    4,    5,    5,    6,
          2,    1,    3,    1,    3,    2,    3,    1,    3,    3,
          4,    6,    5,    6,    8,    8,    6,   12,   11,    1,
          2,    2,    3,    1,    2,    1,    3,    1,    3,    4,
          3,    4,    1,    3,    1,    3,    4,    1,    3,    2,
          3,    1,    1,    2,    3,    2,    3,    3,    4,    2,
          3,    3,    4,    1,    1,    1,    1,    1,    1,    1,
          3,    4,    3,    1,    2,    4,    5,    5,    6,    1,
          2,    5,    7,    5,    5,    7,    6,    7,    3,    2,
          2,    2,    3,    1,    3,    1,    3,    1,    1,    1,
          1,    1,    1,    1,    1,    1,    1,    1,    1,    5,
          1,    1,    3,    1,    3,    1,    3,    1,    3,    1,
          3,    1,    3,    3,    1,    3,    3,    3,    3,    1,
          3,    3,    1,    3,    3,    1,    6,    6,    3,    3,
          3,    1,    4,    1,    2,    2,    2,    2,    4,    1,
          1,    1,    1,    1,    1,    1,    4,    7,    6,    7,
          4,    3,    3,    3,    2,    2,    1,    3,    1,    1,
          3,    1,    3,    1,    1,    1,    1,
    };
  } /* End class YyLenClass */

  protected static final class YyDefRedClass {

    public static final short yyDefRed [] = {            0,
          0,    0,   51,    0,    0,    7,    0,    5,    6,    8,
         64,   63,    0,    0,    0,    0,    0,    4,    2,    3,
          9,    0,    0,   52,    0,  193,    0,    0,    0,    0,
         35,    0,   36,    0,   15,   62,  102,  103,  104,  105,
        125,  126,  127,  128,  129,  130,    0,    0,    0,    0,
        106,  107,    0,   76,   72,    0,    0,    0,  116,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,   12,
         51,    0,   44,   40,   39,    0,   38,   34,   37,    0,
        100,   84,    0,   93,   96,   99,   90,   83,   81,   79,
         92,   95,   89,   86,    0,    0,    0,    0,  138,    0,
          0,    0,   11,   77,   87,    0,    0,  122,  118,  119,
        120,  121,  117,  134,  135,    0,  115,  123,  124,    0,
        101,  194,   10,    0,    0,   42,    0,   51,    0,    0,
         25,    0,    0,   18,    0,    0,    0,    0,  182,  184,
          0,    0,    0,    0,    0,    0,    0,   73,    0,   70,
          0,    0,    0,    0,   13,   65,    0,    0,    0,   52,
         41,   43,    0,    0,   19,   17,   26,    0,    0,    0,
          0,    0,    0,  166,    0,  169,  183,  185,  324,  325,
        326,  327,  320,    0,    0,    0,    0,   48,   50,   49,
          0,  170,    0,  300,  301,  302,  303,  304,  305,    0,
          0,   51,  286,    0,    0,    0,    0,    0,    0,    0,
          0,  306,    0,    0,  292,  261,  319,    0,    0,    0,
          0,  139,   74,   71,    0,    0,  162,    0,    0,  144,
         14,   66,   68,    0,   67,    0,  112,    0,    0,    0,
          0,   69,    0,    0,    0,   21,   16,    0,    0,    0,
          0,  322,    0,  246,    0,    0,    0,  167,    0,  298,
          0,  295,  296,    0,    0,    0,   52,    0,    0,  244,
          0,    0,    0,    0,    0,  171,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,  315,  316,    0,
          0,    0,    0,    0,    0,    0,    0,    0,  297,   54,
        188,    0,    0,    0,    0,    0,    0,    0,  157,    0,
          0,  144,    0,  136,    0,    0,    0,  108,    0,    0,
         52,   52,   75,   24,   45,   58,    0,    0,   28,   29,
         27,  249,  250,  251,  252,  253,  254,  255,  256,  257,
        258,  248,    0,   31,    0,    0,   58,    0,    0,  318,
          0,    0,    0,  321,    0,    0,  151,  149,    0,    0,
          0,   52,    0,    0,    0,    0,    0,    0,    0,  290,
        291,  289,  314,  312,    0,    0,  313,    0,    0,    0,
          0,    0,    0,  173,   54,    0,   54,    0,    0,   51,
        195,    0,  165,  158,  163,  159,    0,    0,  137,    0,
          0,    0,  109,    0,    0,    0,    0,   22,   20,  247,
        323,    0,    0,  177,    0,    0,  299,    0,    0,  245,
          0,    0,  201,    0,    0,  293,    0,    0,  311,  307,
        174,  187,  189,  172,    0,   52,    0,   52,  191,  198,
          0,  141,   51,  160,    0,  131,   52,    0,  145,    0,
        152,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,  224,  220,  215,    0,
        216,  218,  219,  214,  217,  230,    0,    0,    0,    0,
          0,  113,    0,  110,    0,    0,   33,   53,   54,   54,
          0,    0,  210,    0,    0,  206,    0,    0,    0,    0,
          0,    0,  260,  192,  190,  196,    0,  143,  132,  147,
        155,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,  240,  241,  242,    0,   47,  225,    0,
        231,    0,    0,    0,    0,  226,  114,  111,   46,    0,
        175,  176,  287,  288,  205,  211,  207,  212,    0,  208,
          0,  309,    0,    0,  197,  199,  153,  146,  156,    0,
        223,    0,    0,    0,    0,    0,  239,  243,  221,  227,
          0,  228,    0,  213,  209,  308,  310,  222,    0,    0,
          0,    0,    0,  229,   54,    0,    0,  234,  235,    0,
          0,    0,  179,   54,    0,    0,  237,    0,  178,  233,
        236,  238,
    };
  } /* End of class YyDefRedClass */

  protected static final class YyDgotoClass {

    public static final short yyDgoto [] = {             5,
        494,  200,  201,  343,  468,  202,  203,  245,  129,  469,
          6,  130,   35,  131,  204,   54,   55,  424,  136,  132,
        226,  227,  205,  206,  470,  471,  156,  235,    7,  207,
         99,  390,    8,  472,  473,  474,  208,  209,    9,  210,
        100,  211,  212,  213,  160,  475,  214,   74,   23,  267,
        476,  313,  314,  449,  450,  451,  268,   56,  251,  237,
        270,  253,  254,   57,   58,  301,  138,   70,  117,   59,
        477,  217,  118,  119,  120,  478,   60,   61,  134,   27,
         10,  303,  495,   75,  127,  157,   32,   33,   62,   63,
         64,  101,  141,  441,  218,  305,   14,   36,  320,  384,
        154,  400,  158,   66,  144,  221,  242,  315,
    };
  } /* End of class YyDgotoClass */

  protected static final class YySindexClass {

    public static final short yySindex [] = {          332,
       -154, -154,    0, -154,    0,    0,  332,    0,    0,    0,
          0,    0,   34, -165,   60, -132,   34,    0,    0,    0,
          0,  -93, -121,    0, 3430,    0,  274,  -63, 3177, 3177,
          0,  -92,    0,  213,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0, 1452, -217, -217,   77,
          0,    0, -217,    0,    0, 1221, 1221, 1221,    0,  142,
       1221,   51, 3305,  139, 1289,  100,   90,  353,  292,    0,
          0, 3368,    0,    0,    0,  234,    0,    0,    0,  -49,
          0,    0,   87,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,  402,   51,  -19,  340,    0,  -26,
        425,  426,    0,    0,    0,  425,  440,    0,    0,    0,
          0,    0,    0,    0,    0,  -72,    0,    0,    0, -154,
          0,    0,    0,  401,  243,    0, 3226,    0,  -49, -105,
          0,  -24,   71,    0,  276,  340,  -26,  538,    0,    0,
        -19,  689,  490,    0,  340,   51,  549,    0,  551,    0,
        295,  496,    0,  501,    0,    0,  502,  264,  316,    0,
          0,    0,  243,  -55,    0,    0,    0, -154, -154, -154,
       1187, -154,    0,    0,  340,    0,    0,    0,    0,    0,
          0,    0,    0, 2406, 2449, 2449,  607,    0,    0,    0,
        615,    0,  569,    0,    0,    0,    0,    0,    0,  335,
        619,    0,    0,  575,  166,  581,  564,  417,  -35,  627,
        106,    0,  107,  187,    0,    0,    0, 2491,  -28,  631,
          0,    0,    0,    0,  640,   40,    0,  295,  587,    0,
          0,    0,    0,  340,    0,  -26,    0, 3430,   51,  139,
        255,    0,  407,  242,  682,    0,    0,  669,  679,  680,
        363,    0,   98,    0,  639,  651,  713,    0,  569,    0,
       1187,    0,    0,  438,  157,  474,    0,  716,  441,    0,
        441, 1187, 1187, 1187,  -31,    0, 1187, 1187, 1187, 1187,
       1187, 1187, 1187, 2491, 2491, 2491, -154,    0,    0,  814,
       1187, -154, 1187, 1187, 1187, 1187, 1187, 1187,    0,    0,
          0,  492,  714,  718, 1221,  833,  699, 1187,    0, -101,
         53,    0,    0,    0,  441,    0,  340,    0, 3430,  638,
          0,    0,    0,    0,    0,    0,  243,  703,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0, 1187,    0, 1187, 1187,    0,  -23,  722,    0,
        731,  733, 1187,    0,  231, 2491,    0,    0,  627,  627,
        166,    0,  107,  107,  619,  581,  564,  417,  200,    0,
          0,    0,    0,    0,  505,  181,    0,  187,  187,  187,
        187,  335,  335,    0,    0,  476,    0,  -11,  833,    0,
          0,  833,    0,    0,    0,    0,  -53,    0,    0,  654,
         17,    0,    0,  371, 3430, 3430,  687,    0,    0,    0,
          0,  235,  691,    0,  506,  745,    0, -154,  -26,    0,
        464,  848,    0,   94,  104,    0,  135, 1187,    0,    0,
          0,    0,    0,    0,   70,    0,  -25,    0,    0,    0,
         69,    0,    0,    0,  662,    0,    0, 1187,    0,  744,
          0,  732,    0, 1187,  635,  751,  752,  753,  489,  766,
       -154,  749,  754,  920,  607,  489,    0,    0,    0,  261,
          0,    0,    0,    0,    0,    0,  759,  489,  371, 1743,
        685,    0, 3430,    0, 3430,  760,    0,    0,    0,    0,
        770,   67,    0,  771,  777,    0,  728,  781,  933,   94,
        952, 1187,    0,    0,    0,    0,  583,    0,    0,    0,
          0,   79,  764, 1187,  768,  489, 1187, 1187, 1187,  775,
        514, 1079,  778,    0,    0,    0,  385,    0,    0,  489,
          0,  711,  489,  715,    0,    0,    0,    0,    0,  798,
          0,    0,    0,    0,    0,    0,    0,    0,  809,    0,
        746,    0,  531,  244,    0,    0,    0,    0,    0,  489,
          0,  560,  632,  656,  811, 1079,    0,    0,    0,    0,
        735,    0,  812,    0,    0,    0,    0,    0,  489,  489,
        489, 1187, 1102,    0,    0,  817,  517,    0,    0,  670,
        489,  671,    0,    0,  489,  808,    0,  489,    0,    0,
          0,    0,
    };
  } /* End of class YySindexClass */

  protected static final class YyRindexClass {

    public static final short yyRindex [] = {          874,
        592,  592,    0,  592,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0, 1558,    0,  820,    0, 1558, 1558,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0, 2443, 2168, 2219, 1696,
          0,    0, 2270,    0,    0, 1858, 1918, 1991,    0,    0,
       1089,  820, 1558,  820,    0,    0,    0,    0,    0,    0,
          0, 1558,    0,    0,    0,    0,    0,    0,    0,  592,
          0,    0, 1774,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0, 1312,    0,   48,  529,    0,    0,
        820,    0,    0,    0,    0,  820,    0,    0,    0,    0,
          0,    0,    0,    0,    0,  592,    0,    0,    0,  -76,
          0,    0,    0, 1638,    0,    0, 1558,    0,  592,  592,
          0,    0,   22,    0,    0,  449,    0,    0,    0,    0,
         89,    0,    0,  534,  530,    0,    0,    0,    0,    0,
          0, 1372, 1243,    0,    0,    0, 1638, 1558,  433,    0,
          0,    0,    0,  592,    0,    0,    0,  592,  592,  592,
          0,  592,  207,    0,  559,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0, 2568,    0,    0,    0,
          0,    0, 1834,    0,    0,    0,    0,    0,    0, 2207,
       2054,    0,    0,    0, 1729, 1434, 1584,  152,  420, 2892,
       2761,    0, 1850, 3129,    0,    0,    0,    0, 3488,    0,
        561,    0,    0,    0,   91,    0,    0,    0,    0,    0,
          0,    0,    0, 1430,    0,    0,    0, 1197,  820,  820,
          0,    0,    0,    0,  820,    0,    0,    0,    0,    0,
       2884,    0,    0,    0,   92,    0,    0,    0, 1834,    0,
          0,    0,    0,    0,  592,    0,    0,    0,  964,    0,
       1025,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,  592,    0,    0,    0,
          0,  592,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,   23,    0, 1558,    0,    0,    0,    0,    0,
          0,    0, 2298,    0, 1834, 2050, 1508,    0, 1197,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0, 3488,    0,    0,
          0,    0,    0,    0,  839,    0,    0,    0, 2929, 3014,
       2047,    0, 3291, 3314, 3316, 3370, 1964,  160,    0,    0,
          0,    0,    0,    0,    0,    0,    0, 3173, 3180, 3217,
       3268, 3022, 3051,    0,    0, 3488,    0,  220,    0,    0,
          0,    0,    0,    0,    0,    0,    0, 2298,    0,    0,
          0, 2108,    0,  747, 1197, 1197,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,  592,    0,    0,
       3488,    0,    0,  442,  841,    0,    0,    0,    0,    0,
          0,    0,    0,    0, 3488,    0,  475,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,  820,
          0,  399, 2595,    0, 2443,    0,    0,    0,  -71,    0,
        592,    0,    0,    0, 2636,  -71,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,  -82,  747,    0,
          0,    0, 1197,    0, 1197,    0,    0,    0,    0,    0,
          0,  843,    0,    0,    0,    0,    0, 3488,    0,  484,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,  -71,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,  -71,
          0,    0,  -82,    0, 1578,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,  -71,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0, 3488,    0,    0,    0,    0,    0,  -71,  -71,
        -71,    0,    0,    0,    0,    0,  458,    0,    0,    0,
        -71,    0,    0,    0,  -71,    0,    0,  -71,    0,    0,
          0,    0,
    };
  } /* End of class YyRindexClass */

  protected static final class YyGindexClass {

    public static final short yyGindex [] = {            0,
        -73,  212,  605,    0,    0,    0,  188,    0,    0, -151,
        878,  763,    0,  -16,  -12,  -62,   73,   10,  -52,  576,
        667, -224,  623,  618, -188, -263,  742,    0,    0,  621,
        758, -266,  893,    0,    0,    0,  624,    0,  900,  267,
        -80,    0,  634,  308, -125,    0,  355,  133,  894,  180,
       3118,  600, -271,    0,    0,  403,  655,  252,   66,    0,
         36, -192, -131,  197,    0,  527, -139,    0,    0,    0,
          2,    0,  652,    0,    0,  437,  305,   -2,  789, -156,
        913,    0, -198,  891,    0,    0,    0,  896,  774,   -6,
        -22,  -18,    0,    0,    0,    0,   -3,    3,  -96,  815,
        773,  535,    0,    0,  -91, -119,    0,    0,
    };
  } /* End of class YyGindexClass */

  protected static final class YyTableClass {

    public static final short yyTable [] = {            16,
        104,  143,   13,   15,  266,   17,   76,   76,  261,   98,
        216,   98,  300,   96,  435,  137,  139,  414,  238,  166,
        304,   65,   97,  394,   82,   65,   65,  283,  435,   68,
         97,   31,   78,   88,   89,   90,  170,  246,   94,  143,
         53,  399,   54,   65,  174,  106,   55,  145,  143,   76,
        151,   53,   65,   65,   65,  258,   96,   65,   97,   65,
        177,  105,  302,  186,  102,  422,  107,  125,   65,  247,
        266,  444,  266,  105,  448,  447,  257,  236,  143,  422,
         30,  133,   30,  310,  175,  395,  318,  180,  180,   83,
         96,  180,   97,   98,  369,  233,  397,  375,  376,  321,
        322,   73,   73,  147,   76,  234,   11,  544,  149,  435,
        493,   97,  507,  167,   50,  186,  172,  152,   96,   24,
         97,  153,  440,   65,  163,  443,  399,  407,  181,  181,
        133,  133,  181,  498,  164,  240,  448,  143,  180,  241,
        180,  345,  316,  421,   73,  290,  220,  167,  413,  416,
         32,  292,   32,  412,   65,   11,   22,  142,  236,  236,
        422,  171,  244,   12,  309,  133,  295,  403,  296,  133,
        133,  133,  395,  255,  501,  256,  216,  396,   96,  181,
         97,  181,   25,  317,  499,   26,  234,  234,   11,   65,
        344,  415,  262,  506,  422,  262,  291,  402,  275,   73,
        263,  408,   51,  263,  126,   11,  252,  215,   51,  262,
        262,   11,   12,   51,  262,  164,  225,  263,  263,   34,
        143,  106,  263,  307,  345,  502,  179,  180,  181,  182,
        183,  319,   29,   29,   65,   30,   30,  105,  282,  168,
        556,  102,  107,  353,  262,   12,   53,  328,  438,  260,
        262,  263,  263,  482,  484,   65,  104,  428,  566,  162,
         52,  452,   12,   52,  225,   65,  351,   65,   12,  355,
        421,  128,   97,  430,  425,  527,  262,   69,  345,   45,
         46,  423,  388,  215,  263,   30,  187,  345,  373,   26,
        216,   95,   95,  377,   26,  393,  503,   52,  146,  549,
        325,   65,  583,   96,  353,   97,   95,  437,  553,  554,
        169,   65,   52,  323,  436,   65,  216,   67,  220,  529,
        137,  422,  216,  405,  406,  252,  252,  487,  562,  563,
        564,  537,  326,  538,   95,   80,  577,  215,  215,  215,
        425,  391,  215,  215,  215,  215,  215,  215,  140,  215,
        215,  215,   84,   85,  437,   32,   30,   87,  215,  215,
        215,  215,  215,  215,  427,  180,  492,  216,   95,  287,
        288,  289,  452,  215,  586,  293,  294,  272,  410,  273,
        411,  252,  216,   86,  175,  121,  442,   95,  420,  271,
        439,  540,  178,  590,  592,  143,   95,  479,  483,  485,
        480,   65,   65,  199,  307,  299,  181,  122,  194,  497,
        193,  123,  195,  196,  124,  197,  104,   11,   81,  491,
        104,  215,  104,  342,  391,  262,   32,  391,  353,  467,
        142,  137,   82,  263,  500,  511,  277,  278,  504,  508,
        505,  515,  154,  568,  269,   83,  500,  135,  357,  510,
        358,   65,  513,  297,  298,  271,   95,  154,  114,  115,
        259,   48,  523,  259,   91,  271,   49,  271,  146,   92,
         50,  370,  371,  372,   12,  480,   23,  259,  259,   65,
         53,   65,  203,   93,  148,  203,  551,  215,   53,   60,
        232,   23,   60,  215,  401,  232,  198,  232,  150,  232,
        232,  559,  232,  421,  493,   97,   60,   60,  382,  383,
        269,  271,  259,  215,  354,  202,  232,  353,  202,  215,
        269,  199,  269,   23,  204,  155,  194,  204,  193,  219,
        195,  196,  385,  197,  203,   67,  252,  252,  359,  360,
         83,   60,  391,  426,  259,  429,  489,  467,  345,   67,
         37,   38,   39,   40,  422,   41,   48,   42,   43,   44,
        159,   49,   45,   46,  215,   50,  269,  202,   53,   53,
         47,  576,   60,   61,  345,   53,  204,  140,  176,  215,
        232,   95,  232,  232,  363,  364,   48,   60,   61,   60,
         61,   49,  140,  173,   52,   50,   51,   52,   53,   61,
        579,  199,   61,  353,  142,   53,  194,  223,  193,  224,
        195,  196,  225,  197,  198,  199,   61,   61,  228,  142,
        194,   52,  193,  230,  195,  196,  231,  197,  179,  180,
        181,  453,  183,  184,  243,  185,  186,  332,  333,  334,
        335,  336,  337,  338,  339,  340,  341,  378,  379,  380,
        381,   61,  264,    1,  265,    2,  274,   37,   38,   39,
         40,    3,   41,  285,   42,   43,   44,  276,  286,   45,
         46,    4,  580,  284,  279,  353,  454,  455,  456,  457,
        458,  459,  460,  461,  462,  463,  464,  280,  465,  281,
        188,  306,  516,   48,  198,  189,  581,  190,   49,  353,
        308,  466,   50,   51,   52,  389,  191,  555,  198,  312,
        596,  598,   53,  353,  353,  232,  232,  232,  232,  232,
        232,  199,  232,  232,  324,  327,  194,  329,  193,  346,
        195,  196,   41,  197,   42,   43,   44,  330,  331,   45,
         46,  347,  232,  248,  249,  250,  179,  180,  181,  453,
        183,  184,  348,  185,  186,  350,  356,  386,  387,  392,
        404,  409,  417,  232,  232,  232,  232,  232,  232,  232,
        232,  232,  232,  232,  418,  232,  419,  232,  446,  486,
        432,  192,  232,  488,  232,  490,  509,  512,  232,  514,
        517,  518,  519,  232,  454,  520,  456,  457,  458,  459,
        460,  461,  462,  463,  464,  522,  465,  524,  188,  536,
        543,  545,  525,  189,  198,  190,  530,  546,  539,  466,
        547,  548,  558,  565,  191,  560,  179,  180,  181,  182,
        183,  184,  516,  185,  186,  570,  567,  573,  575,  572,
        179,  180,  181,  182,  183,  184,  199,  185,  186,  574,
        582,  194,  585,  193,  374,  195,  196,  594,  197,  584,
         41,  595,   42,   43,   44,  199,  601,   45,   46,   53,
        194,   54,  193,    1,  195,  196,   51,  197,   52,  200,
        199,  202,   53,  365,   18,  194,  187,  193,  188,  195,
        196,  164,  197,  189,  311,  190,  361,  366,  232,   19,
        187,  367,  188,  222,  191,  368,   20,  189,  362,  190,
         28,  398,  433,  349,  557,  533,  352,  165,  191,   21,
         77,   37,   38,   39,   40,  229,   41,   79,   42,   43,
         44,  239,  445,   45,   46,    0,    0,    0,    0,  198,
        496,    0,    0,    0,    0,    0,  179,  180,  181,  182,
        183,  184,  199,  185,  186,  389,    0,  194,  198,  193,
          0,  195,  196,    0,  197,  199,   81,   51,   52,    0,
        194,    0,  193,  198,  195,  196,    0,  197,  526,    0,
          0,    0,    0,    0,  199,    0,    0,    0,    0,  194,
          0,  193,  552,  195,  196,    0,  197,    0,    0,    0,
          0,    0,    0,  150,  150,  150,  187,    0,  188,    0,
          0,    0,    0,  189,    0,  190,    0,    0,    0,    0,
          0,  150,  150,    0,  191,  550,    0,    0,    0,    0,
          0,   51,   59,    0,    0,    0,    0,   51,    0,   51,
          0,    0,    0,   51,   51,  198,    0,   51,   51,   51,
         51,    0,    0,    0,  150,    0,    0,    0,  198,    0,
          0,    0,    0,    0,  148,  148,  148,    0,    0,    0,
          0,  179,  180,  181,  182,  183,  184,  198,  185,  186,
          0,    0,  148,  148,    0,    0,    0,    0,    0,    0,
        179,  180,  181,  182,  183,  184,    0,  185,  186,    0,
          0,    0,    0,    0,    0,  179,  180,  181,  182,  183,
        184,  199,  185,  186,    0,  148,  194,    0,  193,    0,
        195,  196,    0,  197,    0,    0,    0,    0,   85,    0,
         85,  187,    0,  188,  199,    0,    0,  467,  189,  194,
        190,  193,  591,  195,  196,    0,  197,   85,    0,  191,
        187,    0,  188,    0,    0,    0,    0,  189,    0,  190,
          0,    0,    0,    0,    0,  187,    0,  188,  191,    0,
          0,    0,  189,    0,  190,    0,    0,  179,  180,  181,
        182,  183,  184,  191,  185,  186,    0,    0,    0,    0,
        179,  180,  181,  182,  183,  184,    0,  185,  186,  431,
          0,  434,    0,    0,  198,    0,    0,    0,    0,  179,
        180,  181,  182,  183,  184,    0,  185,  186,  481,  199,
          0,    0,    0,    0,  194,    0,  193,  198,  195,  196,
          0,  197,    0,    0,    0,    0,    0,  187,    0,  188,
          0,    0,    0,    0,  189,    0,  190,    0,   51,    0,
        187,    0,  188,    0,   51,  191,   51,  189,    0,  190,
         51,   51,    0,    0,   51,   51,   51,   51,  191,  187,
          0,  188,    0,    0,    0,    0,  189,    0,  190,    0,
          0,  150,  133,  133,  133,    0,  133,  191,    0,    0,
          0,    0,  532,  534,    0,    0,    0,    0,    0,    0,
        133,  133,    0,  541,  542,    0,    0,    0,    0,   51,
          0,    0,  198,    0,    0,   51,    0,   51,    0,   53,
          0,   51,   51,    0,    0,   51,   51,   51,   51,    0,
          0,    0,    0,  133,    0,  133,  179,  180,  181,  182,
        183,  184,  148,  185,  186,    0,    0,  571,    0,    0,
          0,  168,  168,    0,    0,  168,    0,    0,    0,  179,
        180,  181,  182,  183,  184,   55,  185,  186,    0,  168,
        168,    0,  168,   51,   59,    0,    0,    0,    0,   51,
          0,   51,    0,    0,    0,   51,   51,    0,    0,   51,
         51,   51,   51,    0,    0,    0,  187,    0,  188,  593,
          0,    0,  168,  189,  168,  190,   85,    0,  599,    0,
          0,  161,  161,  161,  191,  161,    0,    0,    0,  187,
          0,  188,    0,    0,    0,    0,  189,    0,  190,  161,
        161,    0,    0,    0,  168,    0,    0,  191,    0,    0,
          0,    0,    0,    0,  179,  180,  181,  182,  183,  184,
          0,  185,  186,    0,    0,    0,    0,    0,    0,    0,
          0,    0,  161,    0,  161,    0,    0,    0,    0,   53,
          0,    0,    0,   60,  266,    0,    0,  266,    0,    0,
          0,   51,   59,    0,    0,    0,    0,   51,   60,   51,
         60,  266,  266,   51,   51,    0,  266,   51,   51,   51,
         51,    0,    0,    0,  187,    0,  188,   37,   38,   39,
         40,  189,   41,  190,   42,   43,   44,    0,    0,   45,
         46,    0,  191,    0,    0,    0,  266,  133,  133,  133,
        133,  133,  133,  133,  133,  133,  133,  133,  133,  133,
        133,  133,  133,  133,  133,  133,  133,   53,    0,  133,
          0,   61,   60,   51,   52,    0,    0,  266,  266,    0,
        133,    0,    0,    0,    0,  133,   61,    0,   61,    0,
        133,    0,    0,  108,  133,  133,  133,    0,    0,  109,
          0,  110,    0,    0,  133,  111,  112,    0,    0,  113,
        114,  115,  116,    0,    0,    0,  168,  168,  168,  168,
        168,  168,  168,  168,  168,  168,  168,  168,  168,  168,
        168,  168,  168,  168,  168,  168,    0,  122,  168,  122,
          0,    0,    0,    0,  264,    0,    0,  264,    0,    0,
         61,    0,    0,    0,  168,   52,  122,    0,    0,  168,
          0,  264,  264,  168,  168,  168,  264,    0,    0,    0,
          0,    0,    0,  168,    0,    0,  161,  161,  161,  161,
        161,  161,  161,  161,  161,  161,  161,  161,  161,  161,
        161,  161,  161,  161,  161,  161,  264,   57,  161,   57,
          0,    0,    0,    0,    0,    0,    0,    0,    0,  161,
          0,    0,    0,    0,  161,    0,    0,    0,    0,  161,
          0,    0,    0,  161,  161,  161,  266,  266,  264,    0,
          0,    0,    0,  161,   60,   60,   60,   60,   60,   60,
         60,   60,   60,   60,   60,   60,   60,   60,   60,   60,
         60,   60,   60,   60,    0,   97,   60,   97,   37,   38,
         39,   40,    0,   41,    0,   42,   43,   44,    0,    0,
         45,   46,   60,    0,   97,    0,    0,   60,    0,    0,
          0,   60,   60,   60,    0,    0,  270,    0,    0,  270,
          0,   60,  270,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,   81,   51,   52,  270,  270,    0,    0,
          0,  270,   61,   61,   61,   61,   61,   61,   61,   61,
         61,   61,   61,   61,   61,   61,   61,   61,   61,   61,
         61,   61,    0,   98,   61,   98,    0,    0,    0,    0,
          0,  270,  270,    0,    0,    0,    0,    0,    0,    0,
         61,    0,   98,    0,    0,   61,    0,    0,    0,   61,
         61,   61,   51,   59,    0,    0,    0,    0,   51,   61,
         51,    0,  270,  270,   51,   51,  264,  264,   51,   51,
         51,   51,  122,  122,  122,  122,  122,  122,  122,  122,
        122,  122,  122,  122,  122,  122,  122,  122,  122,  122,
        122,  122,    0,    0,  122,    0,    0,  272,    0,    0,
        272,    0,    0,  272,    0,  122,    0,   82,   82,   82,
        122,   82,    0,    0,    0,  122,    0,  272,  272,  122,
        122,  122,  272,    0,    0,    0,   82,    0,    0,  122,
          0,    0,   57,   57,   57,   57,   57,   57,   57,   57,
         57,   57,   57,   57,   57,   57,   57,   57,   57,   57,
         57,   57,  272,  272,   57,    0,    0,    0,   82,    0,
         82,    0,    0,    0,    0,   57,    0,   80,   80,   80,
         57,   80,    0,    0,    0,   57,    0,    0,    0,   57,
         57,   57,    0,  272,  272,    0,   80,    0,    0,   57,
         97,   97,   97,   97,   97,   97,   97,   97,   97,   97,
         97,   97,   97,   97,   97,   97,   97,   97,   97,   97,
          0,  270,  270,    0,  265,    0,    0,  265,   80,    0,
         80,    0,    0,   97,    0,    0,    0,    0,   97,    0,
          0,  265,  265,   97,    0,    0,  265,  535,   97,   97,
         78,   78,   78,  109,   78,  110,    0,   97,    0,  111,
        112,    0,    0,  113,  114,  115,  116,    0,    0,   78,
          0,    0,    0,    0,    0,    0,  265,    0,   98,   98,
         98,   98,   98,   98,   98,   98,   98,   98,   98,   98,
         98,   98,   98,   98,   98,   98,   98,   98,    0,    0,
          0,   78,    0,   78,  271,    0,    0,  271,  265,    0,
        271,   98,    0,  140,  268,    0,   98,  268,    0,    0,
          0,   98,    0,    0,  271,  271,   98,   98,  140,  271,
         52,  268,  268,    0,    0,   98,  268,    0,   51,    0,
        272,  272,  272,  272,   51,    0,   51,    0,    0,    0,
         51,   51,    0,    0,   51,   51,   51,   51,    0,  271,
        271,    0,   51,   59,    0,    0,  268,  268,   51,    0,
         51,  142,    0,    0,   51,   51,    0,    0,   51,   51,
         51,   51,    0,    0,   82,    0,  142,    0,   52,    0,
        271,  271,  166,    0,    0,   82,    0,  268,  268,    0,
         82,    0,    0,    0,    0,   82,    0,    0,    0,   82,
          0,    0,    0,    0,    0,    0,    0,    0,    0,   82,
          0,    0,   51,   59,    0,    0,    0,   91,   51,   91,
         51,    0,    0,    0,   51,   51,    0,    0,   51,   51,
         51,   51,    0,    0,   80,    0,   91,    0,    0,    0,
        167,    0,    0,    0,    0,   80,  265,  265,    0,    0,
         80,    0,    0,    0,  280,   80,    0,  280,    0,   80,
        280,    0,    0,    0,    0,    0,    0,    0,   94,   80,
         94,    0,    0,    0,  280,  280,  280,    0,  280,  280,
          0,    0,    0,    0,    0,   51,   59,   94,    0,    0,
          0,   51,    0,   51,    0,    0,    0,   51,   51,    0,
          0,   51,   51,   51,   51,    0,    0,   78,    0,  280,
        280,    0,    0,    0,    0,    0,    0,    0,   78,   88,
          0,   88,    0,   78,    0,    0,    0,    0,   78,  271,
        271,    0,   78,    0,    0,    0,  268,  268,   88,    0,
        280,  280,   78,    0,  166,  166,  166,  166,  166,  166,
        166,  166,  166,  166,  166,  166,  166,  166,  166,  166,
        166,  166,  166,  166,    0,    0,  166,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,  166,    0,    0,    0,    0,  166,    0,    0,
          0,  166,  166,  166,    0,    0,    0,    0,    0,    0,
          0,  166,  167,  167,  167,  167,  167,  167,  167,  167,
        167,  167,  167,  167,  167,  167,  167,  167,  167,  167,
        167,  167,    0,    0,  167,    0,    0,    0,    0,    0,
          0,    0,   56,    0,    0,    0,    0,    0,    0,    0,
        167,    0,    0,    0,    0,  167,    0,    0,  199,  167,
        167,  167,    0,  194,    0,  259,    0,  195,  196,  167,
        197,    0,   91,   91,   91,   91,   91,   91,   91,   91,
         91,   91,   91,   91,   91,   91,   91,   91,   91,   91,
         91,   91,    0,  280,  280,  280,  280,  280,  280,  280,
        280,  199,    0,    0,    0,   91,  194,    0,  261,    0,
        195,  196,    0,  197,    0,    0,    0,    0,    0,    0,
         91,   91,    0,   94,   94,   94,   94,   94,   94,   94,
         94,   94,   94,   94,   94,   94,   94,   94,   94,   94,
         94,   94,   94,  199,    0,    0,    0,    0,  194,    0,
        193,  198,  195,  196,    0,  197,   94,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,   94,   94,    0,   88,   88,   88,   88,   88,   88,
         88,   88,   88,   88,   88,   88,   88,   88,   88,   88,
         88,   88,   88,   88,  198,    0,    0,    0,    0,    0,
          0,    0,  144,    0,    0,    0,    0,   88,  144,  144,
        144,  144,  144,  144,  144,  144,  144,  144,  144,  144,
        144,  144,   88,   88,  317,  317,    0,  317,  317,  317,
        317,  317,  317,    0,  317,    0,  198,    0,    0,    0,
          0,    0,    0,    0,    0,  317,  317,  317,  317,  317,
        317,  327,  327,    0,  327,    0,  327,  327,  327,  327,
        327,  327,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,   64,  327,  327,  327,  327,  327,  317,    0,
        317,  317,    0,  179,  180,  181,  182,  183,  184,    0,
        185,  186,  317,  317,    0,  317,    0,  317,  317,  317,
        317,    0,  317,    0,    0,  327,    0,    0,  327,    0,
          0,  317,  317,   63,  317,  317,  317,  317,  317,    0,
          0,    0,    0,    0,    0,    0,  179,  180,  181,  182,
        183,  184,    0,  185,  186,    0,    0,    0,  327,    0,
          0,    0,    0,  187,    0,  188,  317,   51,   59,  317,
        189,    0,  190,   51,    0,   51,    0,    0,    0,   51,
         51,    0,    0,   51,   51,   51,   51,    0,  179,  180,
        181,  182,  183,  184,    0,  185,  186,    0,    0,  317,
          0,    0,    0,    0,    0,   98,  187,    0,  188,    0,
         98,    0,    0,  189,    0,  190,    0,    0,    0,    0,
          0,    0,    0,    0,   98,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,  294,  294,    0,
          0,  294,  294,  294,  294,  294,    0,  294,  187,    0,
        188,    0,    0,    0,    0,  189,    0,  190,  294,  294,
        294,  294,  294,  294,    0,    0,    0,    0,    0,    0,
          0,  317,  317,  317,  317,  317,  317,  317,  317,  317,
        317,  317,  317,  317,  317,  317,  317,  317,  317,  317,
        317,  317,    0,  294,  294,    0,    0,    0,  327,  327,
        327,  327,  327,  327,  327,  327,  327,  327,  327,  327,
        327,  327,  327,  327,  327,  327,  327,  327,  327,    0,
          0,    0,    0,    0,  294,  294,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,  317,
        317,  317,  317,  317,  317,  317,  317,  317,  317,  317,
        317,  317,  317,  317,  317,  317,  317,  317,  317,  317,
        292,  292,    0,    0,  292,  292,  292,  292,  292,  283,
        292,    0,  283,    0,  283,  283,  283,    0,    0,    0,
          0,  292,  292,  292,    0,  292,  292,    0,    0,  283,
        283,  283,    0,  283,  283,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,  284,    0,    0,  284,
          0,  284,  284,  284,    0,    0,  292,  292,    0,    0,
          0,    0,    0,    0,  283,  283,  284,  284,  284,    0,
        284,  284,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,  292,  292,    0,
          0,    0,    0,    0,    0,  283,  283,    0,    0,    0,
          0,  284,  284,    0,    0,    0,    0,  294,  294,  294,
        294,  294,  294,  294,  294,  294,  294,  294,  294,  294,
        294,  294,  294,  294,  294,    0,    0,    0,    0,    0,
          0,  285,  284,  284,  285,    0,  285,  285,  285,  281,
          0,    0,  281,    0,    0,  281,    0,    0,    0,    0,
          0,  285,  285,  285,    0,  285,  285,    0,    0,  281,
        281,  281,    0,  281,  281,    0,    0,    0,  282,    0,
          0,  282,    0,    0,  282,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,  285,  285,  282,  282,
        282,    0,  282,  282,  281,  281,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,  285,  285,    0,
          0,    0,    0,  282,  282,  281,  281,    0,    0,    0,
        292,  292,  292,  292,  292,  292,  292,  292,  283,  283,
        283,  283,  283,  283,  283,  283,  275,    0,    0,  275,
          0,    0,  275,    0,  282,  282,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,  275,  275,  275,    0,
        275,  275,    0,    0,    0,  284,  284,  284,  284,  284,
        284,  284,  284,    0,    0,    0,    0,    0,    0,    0,
        278,    0,    0,  278,    0,    0,  278,  279,    0,    0,
        279,  275,  275,  279,    0,    0,    0,    0,    0,    0,
        278,  278,  278,    0,  278,  278,    0,  279,  279,  279,
          0,  279,  279,    0,    0,    0,    0,    0,    0,    0,
          0,    0,  275,  275,  276,    0,    0,  276,    0,    0,
        276,    0,    0,    0,    0,  278,  278,    0,    0,    0,
          0,    0,  279,  279,  276,  276,  276,    0,  276,  276,
        285,  285,  285,  285,  285,  285,  285,  285,  281,  281,
        281,  281,  281,  281,  281,  281,  278,  278,    0,   72,
          0,    0,    0,  279,  279,  277,    0,    0,  277,  276,
        276,  277,    0,    0,    0,    0,    0,  282,  282,  282,
        282,  282,  282,  282,  282,  277,  277,  277,  273,  277,
        277,  273,    0,    0,  273,    0,    0,    0,    0,    0,
        276,  276,    0,    0,    0,    0,    0,    0,  273,  273,
        161,  274,    0,  273,  274,    0,  269,  274,    0,  269,
        277,  277,    0,    0,    0,    0,    0,    0,    0,    0,
          0,  274,  274,  269,  269,    0,  274,    0,  269,    0,
          0,    0,    0,  273,  273,    0,    0,    0,    0,    0,
          0,  277,  277,    0,    0,    0,    0,  275,  275,  275,
        275,  275,  275,    0,    0,    0,  274,  274,  269,  269,
        267,    0,    0,  267,  273,  273,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,  267,  267,  103,
          0,    0,  267,    0,    0,    0,    0,  274,  274,  269,
        269,  278,  278,  278,  278,  278,  278,    0,  279,  279,
        279,  279,  279,  279,    0,    0,    0,    0,    0,    0,
          0,    0,  267,   37,   38,   39,   40,    0,   41,    0,
         42,   43,   44,    0,    0,   45,   46,    0,    0,    0,
          0,    0,    0,   47,    0,  276,  276,  276,  276,  276,
        276,    0,    0,  267,  267,    0,    0,    0,    0,   48,
         71,    0,    0,    0,   49,    0,    0,    0,   50,   51,
         52,    0,   37,   38,   39,   40,    0,   41,   53,   42,
         43,   44,    0,    0,   45,   46,    0,    0,    0,    0,
          0,    0,   47,    0,    0,    0,  277,  277,  277,  277,
        277,  277,    0,    0,    0,    0,    0,    0,   48,   71,
          0,    0,    0,   49,    0,    0,    0,   50,   51,   52,
          0,  273,  273,  273,  273,    0,    0,   53,    0,    0,
          0,    0,    0,    0,    0,    0,  521,    0,    0,    0,
          0,    0,    0,  528,  274,  274,  274,  274,  269,  269,
          0,   37,   38,   39,   40,  531,   41,    0,   42,   43,
         44,    0,    0,   45,   46,    0,    0,    0,    0,    0,
          0,   47,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,   48,    0,    0,
          0,    0,   49,  561,    0,    0,   50,   51,   52,    0,
          0,    0,  267,  267,    0,    0,   53,  569,    0,    0,
        531,    0,    0,    0,   37,   38,   39,   40,    0,   41,
          0,   42,   43,   44,    0,    0,   45,   46,    0,    0,
          0,    0,    0,    0,   47,    0,    0,  578,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
         48,   71,    0,    0,    0,   49,  587,  588,  589,   50,
         51,   52,    0,    0,    0,    0,    0,    0,  597,   53,
          0,    0,  600,    0,    0,  602,   37,   38,   39,   40,
          0,   41,    0,   42,   43,   44,    0,    0,   45,   46,
          0,    0,    0,    0,    0,    0,   47,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,   48,    0,    0,    0,    0,   49,    0,    0,
          0,   50,   51,   52,    0,    0,    0,    0,    0,    0,
          0,   53,   58,   58,   58,   58,   58,   58,   58,   58,
         58,   58,   58,   58,   58,   58,   58,   58,   58,   58,
         58,   58,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
          0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
         58,   58,
    };
  } /* End of class YyTableClass */

  protected static final class YyCheckClass {

    public static final short yyCheck [] = {             3,
         63,   98,    1,    2,  193,    4,   29,   30,   40,   62,
        142,   64,   41,   40,   40,   96,   97,   41,  158,  125,
        219,   25,   42,  125,   47,   29,   30,   63,   40,   27,
         42,  125,  125,   56,   57,   58,   61,  163,   61,  136,
        123,  313,  125,   47,  136,   64,  123,  100,  145,   72,
        123,  123,   56,   57,   58,  175,   40,   61,   42,   63,
        141,   64,  219,   41,   62,   91,   64,   71,   72,  125,
        259,  125,  261,   76,   58,   59,  173,  158,  175,   91,
         59,   80,   61,   44,  137,  310,  238,   40,   41,  307,
         40,   44,   42,  146,  283,  158,   44,  290,  291,  239,
        240,   29,   30,  101,  127,  158,  261,   41,  106,   40,
         41,   42,   44,  130,  332,   93,   46,  116,   40,  285,
         42,  120,  389,  127,  128,  392,  398,  326,   40,   41,
        129,  130,   44,   40,   44,  158,   58,  234,   91,  158,
         93,   44,  234,   40,   72,   40,  144,  164,  347,  348,
         59,   46,   61,  346,  158,  261,  123,   91,  239,  240,
         91,   91,  160,  318,  125,  164,   60,  319,   62,  168,
        169,  170,  397,  172,   40,  173,  308,  125,   40,   91,
         42,   93,  123,  236,   91,  318,  239,  240,  261,  193,
         93,  348,   41,  125,   91,   44,   91,  317,  202,  127,
         41,  327,  285,   44,   72,  261,  171,  142,  285,   58,
         59,  261,  318,  285,   63,  125,  318,   58,   59,  341,
        317,  240,   63,  221,   44,   91,  258,  259,  260,  261,
        262,  238,  326,  326,  238,  329,  329,  240,  274,  264,
        507,  239,  240,   44,   93,  318,   40,  245,  388,  184,
        185,  186,   93,  405,  406,  259,  319,   58,  522,  127,
         41,  401,  318,   44,  318,  269,  265,  271,  318,  267,
         40,  321,   42,   93,  355,  464,  125,  341,   44,  299,
        300,  355,  305,  218,  125,  264,  318,   44,  287,  318,
        422,  318,  318,  292,  318,  308,  428,   91,   44,  498,
         59,  305,  566,   40,   44,   42,  318,  388,  501,  502,
        335,  315,   93,   59,  388,  319,  448,   44,  316,   59,
        401,   91,  454,  321,  322,  290,  291,   93,  517,  518,
        519,  483,   91,  485,  318,  123,   93,  272,  273,  274,
        421,  306,  277,  278,  279,  280,  281,  282,   97,  284,
        285,  286,   48,   49,  435,  264,  335,   53,  293,  294,
        295,  296,  297,  298,  362,  318,  419,  499,  318,  264,
        265,  266,  512,  308,  573,  269,  270,   43,  343,   45,
        345,  346,  514,  307,  437,  286,  390,  318,  353,  193,
        388,  488,  141,  582,  583,  492,  318,  404,  405,  406,
        404,  405,  406,   33,  402,  218,  318,  318,   38,  422,
         40,   59,   42,   43,  123,   45,  479,  261,  332,  418,
        483,  356,  485,   61,  389,  274,  335,  392,   44,   59,
         91,  512,  455,  274,  425,  448,  271,  272,  436,  443,
        438,  454,   44,   59,  193,  307,  437,   46,  269,  447,
        271,  455,  450,  267,  268,  259,  318,   59,  302,  303,
         41,  323,  461,   44,  323,  269,  328,  271,   44,  328,
        332,  284,  285,  286,  318,  479,   44,   58,   59,  483,
        342,  485,   41,  342,   59,   44,  499,  422,   40,   41,
         33,   59,   44,  428,  315,   38,  126,   40,   59,   42,
         43,  514,   45,   40,   41,   42,   58,   59,  297,  298,
        259,  315,   93,  448,   41,   41,   59,   44,   44,  454,
        269,   33,  271,   91,   41,  125,   38,   44,   40,   40,
         42,   43,   41,   45,   93,   44,  501,  502,  272,  273,
        307,   93,  507,  356,  125,   41,   41,   59,   44,   44,
        287,  288,  289,  290,   91,  292,  323,  294,  295,  296,
        318,  328,  299,  300,  499,  332,  315,   93,   40,   40,
        307,   41,   44,   44,   44,  342,   93,   44,   41,  514,
        123,  318,  125,  126,  277,  278,  323,   59,   59,   61,
         61,  328,   59,  318,   61,  332,  333,  334,   40,   41,
         41,   33,   44,   44,   44,  342,   38,   59,   40,   59,
         42,   43,  318,   45,  126,   33,   58,   59,  123,   59,
         38,   61,   40,  123,   42,   43,  125,   45,  258,  259,
        260,  261,  262,  263,  319,  265,  266,  275,  276,  277,
        278,  279,  280,  281,  282,  283,  284,  293,  294,  295,
        296,   93,   46,  322,   40,  324,   38,  287,  288,  289,
        290,  330,  292,   37,  294,  295,  296,   93,   42,  299,
        300,  340,   41,   47,   94,   44,  306,  307,  308,  309,
        310,  311,  312,  313,  314,  315,  316,  124,  318,  273,
        320,   61,   58,  323,  126,  325,   41,  327,  328,   44,
         61,  331,  332,  333,  334,  123,  336,  125,  126,  123,
         41,   41,  342,   44,   44,  258,  259,  260,  261,  262,
        263,   33,  265,  266,  318,   44,   38,   59,   40,   91,
         42,   43,  292,   45,  294,  295,  296,   59,   59,  299,
        300,   91,  285,  168,  169,  170,  258,  259,  260,  261,
        262,  263,   40,  265,  266,  318,   41,   44,   41,   61,
        123,   59,   41,  306,  307,  308,  309,  310,  311,  312,
        313,  314,  315,  316,   44,  318,   44,  320,  125,   93,
        305,   93,  325,   93,  327,   41,  125,   44,  331,   58,
         40,   40,   40,  336,  306,  307,  308,  309,  310,  311,
        312,  313,  314,  315,  316,   40,  318,   59,  320,  125,
         41,   41,   59,  325,  126,  327,   58,   41,   59,  331,
         93,   41,   59,  310,  336,   58,  258,  259,  260,  261,
        262,  263,   58,  265,  266,  125,   59,   40,   93,  125,
        258,  259,  260,  261,  262,  263,   33,  265,  266,   41,
         40,   38,   41,   40,   41,   42,   43,   41,   45,  125,
        292,  345,  294,  295,  296,   33,   59,  299,  300,  123,
         38,  125,   40,    0,   42,   43,  285,   45,   59,   41,
         33,   41,   40,  279,    7,   38,  318,   40,  320,   42,
         43,  129,   45,  325,  228,  327,  274,  280,  157,    7,
        318,  281,  320,  146,  336,  282,    7,  325,  275,  327,
         17,  312,  386,  259,  512,  479,  265,  129,  336,    7,
         30,  287,  288,  289,  290,  153,  292,   32,  294,  295,
        296,  158,  398,  299,  300,   -1,   -1,   -1,   -1,  126,
         93,   -1,   -1,   -1,   -1,   -1,  258,  259,  260,  261,
        262,  263,   33,  265,  266,  123,   -1,   38,  126,   40,
         -1,   42,   43,   -1,   45,   33,  332,  333,  334,   -1,
         38,   -1,   40,  126,   42,   43,   -1,   45,   59,   -1,
         -1,   -1,   -1,   -1,   33,   -1,   -1,   -1,   -1,   38,
         -1,   40,   41,   42,   43,   -1,   45,   -1,   -1,   -1,
         -1,   -1,   -1,   40,   41,   42,  318,   -1,  320,   -1,
         -1,   -1,   -1,  325,   -1,  327,   -1,   -1,   -1,   -1,
         -1,   58,   59,   -1,  336,   93,   -1,   -1,   -1,   -1,
         -1,  285,  286,   -1,   -1,   -1,   -1,  291,   -1,  293,
         -1,   -1,   -1,  297,  298,  126,   -1,  301,  302,  303,
        304,   -1,   -1,   -1,   91,   -1,   -1,   -1,  126,   -1,
         -1,   -1,   -1,   -1,   40,   41,   42,   -1,   -1,   -1,
         -1,  258,  259,  260,  261,  262,  263,  126,  265,  266,
         -1,   -1,   58,   59,   -1,   -1,   -1,   -1,   -1,   -1,
        258,  259,  260,  261,  262,  263,   -1,  265,  266,   -1,
         -1,   -1,   -1,   -1,   -1,  258,  259,  260,  261,  262,
        263,   33,  265,  266,   -1,   91,   38,   -1,   40,   -1,
         42,   43,   -1,   45,   -1,   -1,   -1,   -1,   40,   -1,
         42,  318,   -1,  320,   33,   -1,   -1,   59,  325,   38,
        327,   40,   41,   42,   43,   -1,   45,   59,   -1,  336,
        318,   -1,  320,   -1,   -1,   -1,   -1,  325,   -1,  327,
         -1,   -1,   -1,   -1,   -1,  318,   -1,  320,  336,   -1,
         -1,   -1,  325,   -1,  327,   -1,   -1,  258,  259,  260,
        261,  262,  263,  336,  265,  266,   -1,   -1,   -1,   -1,
        258,  259,  260,  261,  262,  263,   -1,  265,  266,  385,
         -1,  387,   -1,   -1,  126,   -1,   -1,   -1,   -1,  258,
        259,  260,  261,  262,  263,   -1,  265,  266,  404,   33,
         -1,   -1,   -1,   -1,   38,   -1,   40,  126,   42,   43,
         -1,   45,   -1,   -1,   -1,   -1,   -1,  318,   -1,  320,
         -1,   -1,   -1,   -1,  325,   -1,  327,   -1,  285,   -1,
        318,   -1,  320,   -1,  291,  336,  293,  325,   -1,  327,
        297,  298,   -1,   -1,  301,  302,  303,  304,  336,  318,
         -1,  320,   -1,   -1,   -1,   -1,  325,   -1,  327,   -1,
         -1,  318,   40,   41,   42,   -1,   44,  336,   -1,   -1,
         -1,   -1,  478,  479,   -1,   -1,   -1,   -1,   -1,   -1,
         58,   59,   -1,  489,  490,   -1,   -1,   -1,   -1,  285,
         -1,   -1,  126,   -1,   -1,  291,   -1,  293,   -1,  123,
         -1,  297,  298,   -1,   -1,  301,  302,  303,  304,   -1,
         -1,   -1,   -1,   91,   -1,   93,  258,  259,  260,  261,
        262,  263,  318,  265,  266,   -1,   -1,  533,   -1,   -1,
         -1,   40,   41,   -1,   -1,   44,   -1,   -1,   -1,  258,
        259,  260,  261,  262,  263,  123,  265,  266,   -1,   58,
         59,   -1,   61,  285,  286,   -1,   -1,   -1,   -1,  291,
         -1,  293,   -1,   -1,   -1,  297,  298,   -1,   -1,  301,
        302,  303,  304,   -1,   -1,   -1,  318,   -1,  320,  585,
         -1,   -1,   91,  325,   93,  327,  318,   -1,  594,   -1,
         -1,   40,   41,   42,  336,   44,   -1,   -1,   -1,  318,
         -1,  320,   -1,   -1,   -1,   -1,  325,   -1,  327,   58,
         59,   -1,   -1,   -1,  123,   -1,   -1,  336,   -1,   -1,
         -1,   -1,   -1,   -1,  258,  259,  260,  261,  262,  263,
         -1,  265,  266,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   91,   -1,   93,   -1,   -1,   -1,   -1,   40,
         -1,   -1,   -1,   44,   41,   -1,   -1,   44,   -1,   -1,
         -1,  285,  286,   -1,   -1,   -1,   -1,  291,   59,  293,
         61,   58,   59,  297,  298,   -1,   63,  301,  302,  303,
        304,   -1,   -1,   -1,  318,   -1,  320,  287,  288,  289,
        290,  325,  292,  327,  294,  295,  296,   -1,   -1,  299,
        300,   -1,  336,   -1,   -1,   -1,   93,  285,  286,  287,
        288,  289,  290,  291,  292,  293,  294,  295,  296,  297,
        298,  299,  300,  301,  302,  303,  304,   40,   -1,  307,
         -1,   44,  123,  333,  334,   -1,   -1,  124,  125,   -1,
        318,   -1,   -1,   -1,   -1,  323,   59,   -1,   61,   -1,
        328,   -1,   -1,  285,  332,  333,  334,   -1,   -1,  291,
         -1,  293,   -1,   -1,  342,  297,  298,   -1,   -1,  301,
        302,  303,  304,   -1,   -1,   -1,  285,  286,  287,  288,
        289,  290,  291,  292,  293,  294,  295,  296,  297,  298,
        299,  300,  301,  302,  303,  304,   -1,   40,  307,   42,
         -1,   -1,   -1,   -1,   41,   -1,   -1,   44,   -1,   -1,
        123,   -1,   -1,   -1,  323,   58,   59,   -1,   -1,  328,
         -1,   58,   59,  332,  333,  334,   63,   -1,   -1,   -1,
         -1,   -1,   -1,  342,   -1,   -1,  285,  286,  287,  288,
        289,  290,  291,  292,  293,  294,  295,  296,  297,  298,
        299,  300,  301,  302,  303,  304,   93,   40,  307,   42,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  318,
         -1,   -1,   -1,   -1,  323,   -1,   -1,   -1,   -1,  328,
         -1,   -1,   -1,  332,  333,  334,  273,  274,  125,   -1,
         -1,   -1,   -1,  342,  285,  286,  287,  288,  289,  290,
        291,  292,  293,  294,  295,  296,  297,  298,  299,  300,
        301,  302,  303,  304,   -1,   40,  307,   42,  287,  288,
        289,  290,   -1,  292,   -1,  294,  295,  296,   -1,   -1,
        299,  300,  323,   -1,   59,   -1,   -1,  328,   -1,   -1,
         -1,  332,  333,  334,   -1,   -1,   38,   -1,   -1,   41,
         -1,  342,   44,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,  332,  333,  334,   58,   59,   -1,   -1,
         -1,   63,  285,  286,  287,  288,  289,  290,  291,  292,
        293,  294,  295,  296,  297,  298,  299,  300,  301,  302,
        303,  304,   -1,   40,  307,   42,   -1,   -1,   -1,   -1,
         -1,   93,   94,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
        323,   -1,   59,   -1,   -1,  328,   -1,   -1,   -1,  332,
        333,  334,  285,  286,   -1,   -1,   -1,   -1,  291,  342,
        293,   -1,  124,  125,  297,  298,  273,  274,  301,  302,
        303,  304,  285,  286,  287,  288,  289,  290,  291,  292,
        293,  294,  295,  296,  297,  298,  299,  300,  301,  302,
        303,  304,   -1,   -1,  307,   -1,   -1,   38,   -1,   -1,
         41,   -1,   -1,   44,   -1,  318,   -1,   40,   41,   42,
        323,   44,   -1,   -1,   -1,  328,   -1,   58,   59,  332,
        333,  334,   63,   -1,   -1,   -1,   59,   -1,   -1,  342,
         -1,   -1,  285,  286,  287,  288,  289,  290,  291,  292,
        293,  294,  295,  296,  297,  298,  299,  300,  301,  302,
        303,  304,   93,   94,  307,   -1,   -1,   -1,   91,   -1,
         93,   -1,   -1,   -1,   -1,  318,   -1,   40,   41,   42,
        323,   44,   -1,   -1,   -1,  328,   -1,   -1,   -1,  332,
        333,  334,   -1,  124,  125,   -1,   59,   -1,   -1,  342,
        285,  286,  287,  288,  289,  290,  291,  292,  293,  294,
        295,  296,  297,  298,  299,  300,  301,  302,  303,  304,
         -1,  273,  274,   -1,   41,   -1,   -1,   44,   91,   -1,
         93,   -1,   -1,  318,   -1,   -1,   -1,   -1,  323,   -1,
         -1,   58,   59,  328,   -1,   -1,   63,  285,  333,  334,
         40,   41,   42,  291,   44,  293,   -1,  342,   -1,  297,
        298,   -1,   -1,  301,  302,  303,  304,   -1,   -1,   59,
         -1,   -1,   -1,   -1,   -1,   -1,   93,   -1,  285,  286,
        287,  288,  289,  290,  291,  292,  293,  294,  295,  296,
        297,  298,  299,  300,  301,  302,  303,  304,   -1,   -1,
         -1,   91,   -1,   93,   38,   -1,   -1,   41,  125,   -1,
         44,  318,   -1,   44,   41,   -1,  323,   44,   -1,   -1,
         -1,  328,   -1,   -1,   58,   59,  333,  334,   59,   63,
         61,   58,   59,   -1,   -1,  342,   63,   -1,  285,   -1,
        271,  272,  273,  274,  291,   -1,  293,   -1,   -1,   -1,
        297,  298,   -1,   -1,  301,  302,  303,  304,   -1,   93,
         94,   -1,  285,  286,   -1,   -1,   93,   94,  291,   -1,
        293,   44,   -1,   -1,  297,  298,   -1,   -1,  301,  302,
        303,  304,   -1,   -1,  307,   -1,   59,   -1,   61,   -1,
        124,  125,  123,   -1,   -1,  318,   -1,  124,  125,   -1,
        323,   -1,   -1,   -1,   -1,  328,   -1,   -1,   -1,  332,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  342,
         -1,   -1,  285,  286,   -1,   -1,   -1,   40,  291,   42,
        293,   -1,   -1,   -1,  297,  298,   -1,   -1,  301,  302,
        303,  304,   -1,   -1,  307,   -1,   59,   -1,   -1,   -1,
        123,   -1,   -1,   -1,   -1,  318,  273,  274,   -1,   -1,
        323,   -1,   -1,   -1,   38,  328,   -1,   41,   -1,  332,
         44,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   40,  342,
         42,   -1,   -1,   -1,   58,   59,   60,   -1,   62,   63,
         -1,   -1,   -1,   -1,   -1,  285,  286,   59,   -1,   -1,
         -1,  291,   -1,  293,   -1,   -1,   -1,  297,  298,   -1,
         -1,  301,  302,  303,  304,   -1,   -1,  307,   -1,   93,
         94,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  318,   40,
         -1,   42,   -1,  323,   -1,   -1,   -1,   -1,  328,  273,
        274,   -1,  332,   -1,   -1,   -1,  273,  274,   59,   -1,
        124,  125,  342,   -1,  285,  286,  287,  288,  289,  290,
        291,  292,  293,  294,  295,  296,  297,  298,  299,  300,
        301,  302,  303,  304,   -1,   -1,  307,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,  323,   -1,   -1,   -1,   -1,  328,   -1,   -1,
         -1,  332,  333,  334,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,  342,  285,  286,  287,  288,  289,  290,  291,  292,
        293,  294,  295,  296,  297,  298,  299,  300,  301,  302,
        303,  304,   -1,   -1,  307,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,  125,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
        323,   -1,   -1,   -1,   -1,  328,   -1,   -1,   33,  332,
        333,  334,   -1,   38,   -1,   40,   -1,   42,   43,  342,
         45,   -1,  285,  286,  287,  288,  289,  290,  291,  292,
        293,  294,  295,  296,  297,  298,  299,  300,  301,  302,
        303,  304,   -1,  267,  268,  269,  270,  271,  272,  273,
        274,   33,   -1,   -1,   -1,  318,   38,   -1,   40,   -1,
         42,   43,   -1,   45,   -1,   -1,   -1,   -1,   -1,   -1,
        333,  334,   -1,  285,  286,  287,  288,  289,  290,  291,
        292,  293,  294,  295,  296,  297,  298,  299,  300,  301,
        302,  303,  304,   33,   -1,   -1,   -1,   -1,   38,   -1,
         40,  126,   42,   43,   -1,   45,  318,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,  333,  334,   -1,  285,  286,  287,  288,  289,  290,
        291,  292,  293,  294,  295,  296,  297,  298,  299,  300,
        301,  302,  303,  304,  126,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,  285,   -1,   -1,   -1,   -1,  318,  291,  292,
        293,  294,  295,  296,  297,  298,  299,  300,  301,  302,
        303,  304,  333,  334,   37,   38,   -1,   40,   41,   42,
         43,   44,   45,   -1,   47,   -1,  126,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   58,   59,   60,   61,   62,
         63,   37,   38,   -1,   40,   -1,   42,   43,   44,   45,
         46,   47,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   58,   59,   60,   61,   62,   63,   91,   -1,
         93,   94,   -1,  258,  259,  260,  261,  262,  263,   -1,
        265,  266,   37,   38,   -1,   40,   -1,   42,   43,   44,
         45,   -1,   47,   -1,   -1,   91,   -1,   -1,   94,   -1,
         -1,  124,  125,   58,   59,   60,   61,   62,   63,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,  258,  259,  260,  261,
        262,  263,   -1,  265,  266,   -1,   -1,   -1,  124,   -1,
         -1,   -1,   -1,  318,   -1,  320,   91,  285,  286,   94,
        325,   -1,  327,  291,   -1,  293,   -1,   -1,   -1,  297,
        298,   -1,   -1,  301,  302,  303,  304,   -1,  258,  259,
        260,  261,  262,  263,   -1,  265,  266,   -1,   -1,  124,
         -1,   -1,   -1,   -1,   -1,  323,  318,   -1,  320,   -1,
        328,   -1,   -1,  325,   -1,  327,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,  342,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   37,   38,   -1,
         -1,   41,   42,   43,   44,   45,   -1,   47,  318,   -1,
        320,   -1,   -1,   -1,   -1,  325,   -1,  327,   58,   59,
         60,   61,   62,   63,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,  264,  265,  266,  267,  268,  269,  270,  271,  272,
        273,  274,  275,  276,  277,  278,  279,  280,  281,  282,
        283,  284,   -1,   93,   94,   -1,   -1,   -1,  264,  265,
        266,  267,  268,  269,  270,  271,  272,  273,  274,  275,
        276,  277,  278,  279,  280,  281,  282,  283,  284,   -1,
         -1,   -1,   -1,   -1,  124,  125,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  264,
        265,  266,  267,  268,  269,  270,  271,  272,  273,  274,
        275,  276,  277,  278,  279,  280,  281,  282,  283,  284,
         37,   38,   -1,   -1,   41,   42,   43,   44,   45,   38,
         47,   -1,   41,   -1,   43,   44,   45,   -1,   -1,   -1,
         -1,   58,   59,   60,   -1,   62,   63,   -1,   -1,   58,
         59,   60,   -1,   62,   63,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   38,   -1,   -1,   41,
         -1,   43,   44,   45,   -1,   -1,   93,   94,   -1,   -1,
         -1,   -1,   -1,   -1,   93,   94,   58,   59,   60,   -1,
         62,   63,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,  124,  125,   -1,
         -1,   -1,   -1,   -1,   -1,  124,  125,   -1,   -1,   -1,
         -1,   93,   94,   -1,   -1,   -1,   -1,  267,  268,  269,
        270,  271,  272,  273,  274,  275,  276,  277,  278,  279,
        280,  281,  282,  283,  284,   -1,   -1,   -1,   -1,   -1,
         -1,   38,  124,  125,   41,   -1,   43,   44,   45,   38,
         -1,   -1,   41,   -1,   -1,   44,   -1,   -1,   -1,   -1,
         -1,   58,   59,   60,   -1,   62,   63,   -1,   -1,   58,
         59,   60,   -1,   62,   63,   -1,   -1,   -1,   38,   -1,
         -1,   41,   -1,   -1,   44,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   93,   94,   58,   59,
         60,   -1,   62,   63,   93,   94,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,  124,  125,   -1,
         -1,   -1,   -1,   93,   94,  124,  125,   -1,   -1,   -1,
        267,  268,  269,  270,  271,  272,  273,  274,  267,  268,
        269,  270,  271,  272,  273,  274,   38,   -1,   -1,   41,
         -1,   -1,   44,   -1,  124,  125,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   58,   59,   60,   -1,
         62,   63,   -1,   -1,   -1,  267,  268,  269,  270,  271,
        272,  273,  274,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         38,   -1,   -1,   41,   -1,   -1,   44,   38,   -1,   -1,
         41,   93,   94,   44,   -1,   -1,   -1,   -1,   -1,   -1,
         58,   59,   60,   -1,   62,   63,   -1,   58,   59,   60,
         -1,   62,   63,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,  124,  125,   38,   -1,   -1,   41,   -1,   -1,
         44,   -1,   -1,   -1,   -1,   93,   94,   -1,   -1,   -1,
         -1,   -1,   93,   94,   58,   59,   60,   -1,   62,   63,
        267,  268,  269,  270,  271,  272,  273,  274,  267,  268,
        269,  270,  271,  272,  273,  274,  124,  125,   -1,  123,
         -1,   -1,   -1,  124,  125,   38,   -1,   -1,   41,   93,
         94,   44,   -1,   -1,   -1,   -1,   -1,  267,  268,  269,
        270,  271,  272,  273,  274,   58,   59,   60,   38,   62,
         63,   41,   -1,   -1,   44,   -1,   -1,   -1,   -1,   -1,
        124,  125,   -1,   -1,   -1,   -1,   -1,   -1,   58,   59,
        125,   38,   -1,   63,   41,   -1,   41,   44,   -1,   44,
         93,   94,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   58,   59,   58,   59,   -1,   63,   -1,   63,   -1,
         -1,   -1,   -1,   93,   94,   -1,   -1,   -1,   -1,   -1,
         -1,  124,  125,   -1,   -1,   -1,   -1,  269,  270,  271,
        272,  273,  274,   -1,   -1,   -1,   93,   94,   93,   94,
         41,   -1,   -1,   44,  124,  125,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   58,   59,  125,
         -1,   -1,   63,   -1,   -1,   -1,   -1,  124,  125,  124,
        125,  269,  270,  271,  272,  273,  274,   -1,  269,  270,
        271,  272,  273,  274,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   93,  287,  288,  289,  290,   -1,  292,   -1,
        294,  295,  296,   -1,   -1,  299,  300,   -1,   -1,   -1,
         -1,   -1,   -1,  307,   -1,  269,  270,  271,  272,  273,
        274,   -1,   -1,  124,  125,   -1,   -1,   -1,   -1,  323,
        324,   -1,   -1,   -1,  328,   -1,   -1,   -1,  332,  333,
        334,   -1,  287,  288,  289,  290,   -1,  292,  342,  294,
        295,  296,   -1,   -1,  299,  300,   -1,   -1,   -1,   -1,
         -1,   -1,  307,   -1,   -1,   -1,  269,  270,  271,  272,
        273,  274,   -1,   -1,   -1,   -1,   -1,   -1,  323,  324,
         -1,   -1,   -1,  328,   -1,   -1,   -1,  332,  333,  334,
         -1,  271,  272,  273,  274,   -1,   -1,  342,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,  459,   -1,   -1,   -1,
         -1,   -1,   -1,  466,  271,  272,  273,  274,  273,  274,
         -1,  287,  288,  289,  290,  478,  292,   -1,  294,  295,
        296,   -1,   -1,  299,  300,   -1,   -1,   -1,   -1,   -1,
         -1,  307,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,  323,   -1,   -1,
         -1,   -1,  328,  516,   -1,   -1,  332,  333,  334,   -1,
         -1,   -1,  273,  274,   -1,   -1,  342,  530,   -1,   -1,
        533,   -1,   -1,   -1,  287,  288,  289,  290,   -1,  292,
         -1,  294,  295,  296,   -1,   -1,  299,  300,   -1,   -1,
         -1,   -1,   -1,   -1,  307,   -1,   -1,  560,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
        323,  324,   -1,   -1,   -1,  328,  579,  580,  581,  332,
        333,  334,   -1,   -1,   -1,   -1,   -1,   -1,  591,  342,
         -1,   -1,  595,   -1,   -1,  598,  287,  288,  289,  290,
         -1,  292,   -1,  294,  295,  296,   -1,   -1,  299,  300,
         -1,   -1,   -1,   -1,   -1,   -1,  307,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,  323,   -1,   -1,   -1,   -1,  328,   -1,   -1,
         -1,  332,  333,  334,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,  342,  285,  286,  287,  288,  289,  290,  291,  292,
        293,  294,  295,  296,  297,  298,  299,  300,  301,  302,
        303,  304,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
         -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
        333,  334,
    };
  } /* End of class YyCheckClass */


  protected static final class YyRuleClass {

    public static final String yyRule [] = {
    "$accept : nesc_file",
    "nesc_file :",
    "nesc_file : includes_list interface",
    "nesc_file : includes_list module",
    "nesc_file : includes_list configuration",
    "nesc_file : interface",
    "nesc_file : module",
    "nesc_file : configuration",
    "includes_list : includes",
    "includes_list : includes_list includes",
    "includes : INCLUDES NS_ntd identifier_list NS_td ';'",
    "interface : INTERFACE identifier '{' declaration_list '}'",
    "module : MODULE identifier specification module_implementation",
    "module_implementation : IMPLEMENTATION '{' '}'",
    "module_implementation : IMPLEMENTATION '{' translation_unit '}'",
    "configuration : CONFIGURATION identifier specification configuration_implementation",
    "configuration_implementation : IMPLEMENTATION '{' component_list connection_list '}'",
    "configuration_implementation : IMPLEMENTATION '{' connection_list '}'",
    "component_list : components",
    "component_list : component_list components",
    "components : COMPONENTS NS_ntd component_line NS_td ';'",
    "component_line : renamed_identifier",
    "component_line : component_line ',' renamed_identifier",
    "renamed_identifier : IDENTIFIER",
    "renamed_identifier : IDENTIFIER AS IDENTIFIER",
    "connection_list : connection",
    "connection_list : connection_list connection",
    "connection : endpoint '=' endpoint ';'",
    "connection : endpoint PTR_OP endpoint ';'",
    "connection : endpoint LEFTARROW endpoint ';'",
    "endpoint : identifier",
    "endpoint : identifier '[' argument_expression_list ']'",
    "endpoint : identifier '.' identifier",
    "endpoint : identifier '.' identifier '[' argument_expression_list ']'",
    "specification : '{' uses_provides_list '}'",
    "specification : '{' '}'",
    "uses_provides_list : uses_provides",
    "uses_provides_list : uses_provides_list uses_provides",
    "uses_provides : USES specification_element_list",
    "uses_provides : PROVIDES specification_element_list",
    "specification_element_list : specification_element",
    "specification_element_list : '{' specification_elements '}'",
    "specification_elements : specification_element",
    "specification_elements : specification_elements specification_element",
    "specification_element : nesc_declaration",
    "specification_element : INTERFACE NS_ntd renamed_identifier NS_td ';'",
    "specification_element : INTERFACE NS_ntd renamed_identifier NS_td '[' parameter_type_list ']' ';'",
    "atomic_statement : ATOMIC statement",
    "call_kind : CALL",
    "call_kind : SIGNAL",
    "call_kind : POST",
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
    "external_declaration : NS_id function_definition",
    "external_declaration : NS_id declaration",
    "external_declaration : NS_id untyped_declaration",
    "declaration : declaration_specifiers NS_td ';'",
    "declaration : declaration_specifiers init_declarator_list NS_td ';'",
    "declaration : nesc_declaration",
    "nesc_declaration : nesc_declaration_specifiers NS_td ';'",
    "nesc_declaration : nesc_declaration_specifiers init_declarator_list NS_td ';'",
    "untyped_declaration : init_declarator_list ';'",
    "declaration_list : declaration",
    "declaration_list : declaration_list declaration",
    "declaration_specifiers : storage_class_specifier",
    "declaration_specifiers : storage_class_specifier declaration_specifiers",
    "declaration_specifiers : type_specifier",
    "declaration_specifiers : type_specifier declaration_specifiers",
    "declaration_specifiers : type_qualifier",
    "declaration_specifiers : type_qualifier declaration_specifiers",
    "nesc_declaration_specifiers : DEFAULT declaration_specifiers",
    "nesc_declaration_specifiers : nesc_storage_class_specifier",
    "nesc_declaration_specifiers : nesc_storage_class_specifier declaration_specifiers",
    "nesc_declaration_specifiers : declaration_specifiers nesc_storage_class_specifier",
    "nesc_storage_class_specifier : COMMAND",
    "nesc_storage_class_specifier : nesc_storage_class_adjective COMMAND",
    "nesc_storage_class_specifier : COMMAND nesc_storage_class_adjective",
    "nesc_storage_class_specifier : EVENT",
    "nesc_storage_class_specifier : nesc_storage_class_adjective EVENT",
    "nesc_storage_class_specifier : EVENT nesc_storage_class_adjective",
    "nesc_storage_class_specifier : TASK",
    "nesc_storage_class_specifier : nesc_storage_class_adjective TASK",
    "nesc_storage_class_specifier : TASK nesc_storage_class_adjective",
    "nesc_storage_class_adjective : ASYNC",
    "nesc_storage_class_adjective : DEFAULT",
    "nesc_storage_class_adjective : ASYNC DEFAULT",
    "nesc_storage_class_adjective : DEFAULT ASYNC",
    "storage_class_specifier : NS_is_typedef TYPEDEF",
    "storage_class_specifier : EXTERN",
    "storage_class_specifier : STATIC",
    "storage_class_specifier : AUTO",
    "storage_class_specifier : REGISTER",
    "storage_class_specifier : NORACE",
    "storage_class_specifier : INLINE",
    "function_definition : declarator compound_statement",
    "function_definition : declarator declaration_list compound_statement",
    "function_definition : declaration_specifiers declarator NS_td compound_statement",
    "function_definition : declaration_specifiers declarator NS_td declaration_list compound_statement",
    "function_definition : nesc_function_definition",
    "nesc_function_definition : nesc_declaration_specifiers declarator NS_td compound_statement",
    "nesc_function_definition : nesc_declaration_specifiers declarator NS_td declaration_list compound_statement",
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
    "init_declarator_list : init_declarator",
    "init_declarator_list : init_declarator_list ',' init_declarator",
    "init_declarator : direct_declarator NS_direct_decl",
    "init_declarator : direct_declarator NS_direct_decl NS_td '=' initializer NS_ntd",
    "init_declarator : pointer direct_declarator NS_ptr_decl",
    "init_declarator : pointer direct_declarator NS_ptr_decl NS_td '=' initializer NS_ntd",
    "$$1 :",
    "struct_declaration : $$1 specifier_qualifier_list struct_declaration_1",
    "struct_declaration_1 : struct_declarator_list NS_td ';'",
    "struct_declaration_1 : ';' NS_td",
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
    "direct_declarator : IDENTIFIER '.' IDENTIFIER NS_scope_enter '(' identifier_list ')' NS_scope_leave",
    "direct_declarator : IDENTIFIER '.' IDENTIFIER NS_scope_enter '(' parameter_type_list ')' NS_scope_leave",
    "direct_declarator : IDENTIFIER '.' IDENTIFIER NS_scope_enter '(' ')'",
    "direct_declarator : IDENTIFIER '.' IDENTIFIER NS_td '[' parameter_type_list ']' NS_scope_enter '(' parameter_type_list ')' NS_scope_leave",
    "direct_declarator : IDENTIFIER '.' IDENTIFIER NS_td '[' parameter_type_list ']' NS_scope_enter '(' ')' NS_scope_leave",
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
    "statement : atomic_statement",
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
    "multiplicative_expression : OFFSETOF '(' identifier ',' identifier ')'",
    "multiplicative_expression : OFFSETOF '(' struct_or_union_specifier ',' direct_declarator ')'",
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
    "postfix_expression : call_kind NS_ntd primary_expression NS_td '(' argument_expression_list ')'",
    "postfix_expression : call_kind NS_ntd primary_expression NS_td '(' ')'",
    "postfix_expression : call_kind NS_ntd primary_expression NS_td '[' argument_expression_list ']'",
    "postfix_expression : postfix_expression '(' argument_expression_list ')'",
    "postfix_expression : postfix_expression '(' ')'",
    "postfix_expression : postfix_expression '.' identifier",
    "postfix_expression : postfix_expression PTR_OP identifier",
    "postfix_expression : postfix_expression INC_OP",
    "postfix_expression : postfix_expression DEC_OP",
    "primary_expression : IDENTIFIER",
    "primary_expression : IDENTIFIER '.' IDENTIFIER",
    "primary_expression : constant",
    "primary_expression : STRING",
    "primary_expression : '(' expression ')'",
    "argument_expression_list : assignment_expression",
    "argument_expression_list : argument_expression_list ',' assignment_expression",
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
    null,null,null,null,null,null,null,"'!'",null,null,null,"'%'","'&'",
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
    "CONTINUE","BREAK","RETURN","ATTRIBUTE","IDENTIFIER","AS","CALL",
    "COMPONENTS","CONFIGURATION","EVENT","INTERFACE","POST","PROVIDES",
    "SIGNAL","TASK","USES","INCLUDES","ATOMIC","ASYNC","NORACE","INLINE",
    "LEFTARROW","OFFSETOF","COMMENT","DOCCOMMENT","ENDOFLINECOMMENT",
    "MODULE","IMPLEMENTATION","COMMAND","ERRORSTRING","THEN","ELSE",
    };
  } /* End of class YyNameClass */


					// line 1880 "/cygdrive/d/backup/dcg/workspace2/TinyOSNewGraph/src/tinyOS/nesc/parser/NesCParser.jay"

      // -----------------------------------------------------------
// epilog
// -----------------------------------------------------------
}
					// line 3846 "-"
