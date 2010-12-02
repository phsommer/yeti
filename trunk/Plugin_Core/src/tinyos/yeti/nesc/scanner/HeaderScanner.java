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
package tinyos.yeti.nesc.scanner;
import java.io.IOException;
import java.util.ArrayList;

import tinyos.yeti.nesc.parser.HeaderFileParser;
import tinyos.yeti.nesc.parser.IParser;
import tinyos.yeti.nesc.parser.yyInput;
import tinyos.yeti.nesc.parser.language.elements.AttributeElement;
/**
 *	NesC Scanner
 */


public class HeaderScanner implements yyInput, ITypeNames {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final char YYEOF = '\uFFFF';

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

	private Token makeToken(int value) {
		return new Token(value, yytext(), yyline, yychar, yychar+ yytext().length());
    }
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yychar;
	private int yyline;
	private int yy_lexical_state;

	public HeaderScanner (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	public HeaderScanner (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private HeaderScanner () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yychar = 0;
		yyline = 0;
		yy_lexical_state = YYINITIAL;

	// Constructor
	}

	private boolean yy_eof_done = false;
	private final int MULTILINECOMMENT = 2;
	private final int ATTRIBUTEFUNCTION = 7;
	private final int ATTRIBUTE = 6;
	private final int ATTRIBUTEBEGIN = 5;
	private final int YYINITIAL = 0;
	private final int SKIPMACRO = 4;
	private final int DEFINE = 3;
	private final int INCLUDE = 1;
	private final int yy_state_dtrans[] = {
		0,
		181,
		185,
		-1,
		90,
		188,
		189,
		190
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private char yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YYEOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YYEOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_start () {
		if ((byte) '\n' == yy_buffer[yy_buffer_start]
			|| (byte) '\r' == yy_buffer[yy_buffer_start]) {
			++yyline;
		}
		++yychar;
		++yy_buffer_start;
	}
	private void yy_pushback () {
		--yy_buffer_end;
	}
	private void yy_mark_start () {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ((byte) '\n' == yy_buffer[i] || (byte) '\r' == yy_buffer[i]) {
				++yyline;
			}
		}
		yychar = yychar
			+ yy_buffer_index - yy_buffer_start;
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
private int [][] unpackFromString(int size1, int size2, String st)
    {
      int colonIndex = -1;
      String lengthString;
      int sequenceLength = 0;
      int sequenceInteger = 0;
      int commaIndex;
      String workString;
      int res[][] = new int[size1][size2];
      for (int i= 0; i < size1; i++)
	for (int j= 0; j < size2; j++)
	  {
	    if (sequenceLength == 0) 
	      {	
		commaIndex = st.indexOf(',');
		if (commaIndex == -1)
		  workString = st;
		else
		  workString = st.substring(0, commaIndex);
		st = st.substring(commaIndex+1);
		colonIndex = workString.indexOf(':');
		if (colonIndex == -1)
		  {
		    res[i][j] = Integer.parseInt(workString);
		  }
		else 
		  {
		    lengthString = workString.substring(colonIndex+1);  
		    sequenceLength = Integer.parseInt(lengthString);
		    workString = workString.substring(0,colonIndex);
		    sequenceInteger = Integer.parseInt(workString);
		    res[i][j] = sequenceInteger;
		    sequenceLength--;
		  }
	      }
	    else 
	      {
		res[i][j] = sequenceInteger;
		sequenceLength--;
	      }
	  }
      return res;
    }
	private int yy_acpt[] = {
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_END,
		YY_END,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_END,
		YY_END,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR
	};
	private int yy_cmap[] = {
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 1, 2, 0, 0, 3, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		4, 5, 6, 7, 0, 8, 9, 10,
		11, 12, 13, 14, 15, 16, 17, 18,
		19, 20, 20, 20, 20, 20, 20, 20,
		21, 21, 22, 23, 24, 25, 26, 27,
		28, 29, 29, 29, 29, 30, 31, 32,
		32, 32, 32, 32, 33, 32, 32, 32,
		32, 32, 32, 32, 32, 34, 32, 32,
		35, 32, 32, 36, 37, 38, 39, 40,
		0, 41, 42, 43, 44, 45, 46, 47,
		48, 49, 50, 50, 51, 52, 53, 54,
		55, 50, 56, 57, 58, 59, 60, 50,
		61, 62, 63, 64, 65, 66, 67, 0
		
	};
	private int yy_rmap[] = {
		0, 1, 1, 1, 2, 3, 4, 1,
		1, 5, 6, 1, 7, 8, 9, 10,
		11, 1, 12, 13, 14, 1, 15, 1,
		1, 16, 1, 17, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1,
		18, 1, 19, 1, 20, 21, 22, 1,
		1, 1, 23, 1, 15, 1, 1, 1,
		24, 1, 1, 25, 26, 1, 1, 15,
		15, 15, 15, 15, 15, 15, 15, 15,
		15, 15, 15, 15, 1, 15, 15, 27,
		1, 27, 28, 1, 27, 29, 27, 27,
		1, 1, 30, 1, 1, 1, 1, 1,
		1, 1, 1, 31, 1, 1, 1, 1,
		1, 1, 32, 33, 34, 35, 36, 37,
		1, 1, 38, 1, 1, 39, 1, 40,
		1, 41, 42, 43, 44, 45, 46, 47,
		48, 49, 50, 50, 51, 52, 53, 54,
		55, 56, 57, 58, 59, 60, 61, 62,
		63, 64, 65, 66, 67, 68, 69, 70,
		71, 72, 73, 74, 75, 76, 77, 78,
		79, 80, 81, 82, 83, 84, 85, 86,
		87, 88, 89, 90, 91, 92, 93, 94,
		95, 90, 96, 97, 98, 99, 27, 100,
		101, 102, 43, 44, 103, 104, 105, 106,
		101, 107, 108, 109, 110, 111, 112, 113,
		114, 115, 116, 117, 118, 119, 120, 121,
		122, 123, 124, 125, 126, 127, 128, 129,
		130, 131, 132, 133, 134, 135, 136, 137,
		138, 139, 140, 141, 142, 143, 144, 145,
		146, 147, 148, 149, 150, 151, 152, 153,
		154, 155 
	};
	private int yy_nxt[][] = unpackFromString(156,68,
"1,2,3,109,3,4,108,131,5,6,137,7,8,9,10,11,12,13,14,15,110:2,16,17,18,19,20,21,1,22:7,23,1,24,25,241,22:4,208,191,22:2,111,22,209,22:4,230,221,234,222,210,22:3,26,27,28,29,-1:93,30,-1:67,31,-1:51,32,-1:15,33,-1:67,34,-1:56,35,-1:10,36,-1:58,37,-1:8,38,39,-1:58,150,-1,40:3,-1:59,41,-1:4,42,-1:6,43,-1:59,44,-1,45:2,110,-1:8,152,-1:2,132:2,154,-1:9,152,-1:5,132,-1:7,132,-1,154,-1:32,24,-1:63,23,-1,46,47,-1:67,48,-1:67,49,50,-1:60,22:3,-1:7,22:7,-1:4,22:24,-1:29,51,-1:67,53,-1:39,54,-1:21,40:3,-1:8,166,112,-1,112,-1:11,166,112,-1:4,112,-1:16,42:2,-1,42:65,-1:19,134:3,-1:8,167,113,-1,113,-1:11,167,113,-1:4,113,-1:33,44,-1,45:2,110,-1:8,152,-1:2,114:2,-1:10,152,-1:5,114,-1:7,114,-1:33,61,-1:67,62,-1:86,171,-1:8,172,-1:28,59:25,-1:7,116,-1:4,116,-1:35,60:3,-1:7,60:3,-1,117:2,-1:6,60:6,-1:4,117,-1:7,117,-1:8,182:2,-1,182,-1,182:13,120,182:5,-1,182:45,-1,182,-1,182:13,85,182:5,-1,182:43,85:2,-1,85:65,90:2,91,123,90:33,122,90:30,-1:11,101,-1:7,99:3,-1:7,99:7,-1:4,99:24,-1:23,106:3,-1:7,106:7,-1:4,106:24,-1:4,107:2,-1:2,107:2,55,107:63,-1:2,107:2,-1,107:61,-1:2,3,-1:82,44,-1,110:3,-1:8,152,-1:2,132:2,-1:10,152,-1:5,132,-1:7,132,-1:27,22:3,-1:7,22:7,-1:4,22:6,52,22:6,138,22:10,-1:37,114:2,-1:16,114,-1:7,114,-1:41,117:2,-1:16,117,-1:7,117,-1:8,182:2,80,182,-1,182:13,120,182:5,-1,182:43,-1:18,89,-1:49,122:2,92,124,122:64,186:2,91,123,186:33,187,186:30,187:2,92,124,187:64,-1:11,95,-1:58,94,-1:77,100,-1:57,97,-1:67,103,-1:67,130:3,-1:39,136,140,-1:3,142,-1:9,144,-1:41,132:2,-1:16,132,-1:7,132,-1:27,22:3,-1:7,22:7,-1:4,22:16,63,22:7,-1:23,134:3,-1:8,166,112,-1,112,-1:11,166,112,-1:4,112,-1:30,135:25,-1:7,113,-1:4,113,-1:61,156,-1:22,146:10,-1,146:26,148,146:30,-1:19,22:3,-1:7,22:7,-1:4,22:18,64,22:5,-1:18,139:25,-1:7,112,-1:4,112,-1:67,158,-1,160,-1:2,162,-1:30,22:3,-1:7,22:7,-1:4,22:12,65,22:11,-1:50,56,-1:6,164,-1:33,22:3,-1:7,22:7,-1:4,22:7,66,22:16,-1:57,193,-1:33,22:3,-1:7,22:7,-1:4,22:4,67,22:19,-1:4,146:10,57,146:26,148,146:30,-1:19,22:3,-1:7,22:7,-1:4,22:18,68,22:5,-1:4,146:2,-1:2,146:64,-1:19,22:3,-1:7,22:7,-1:4,22:13,69,22:10,-1:21,58,-1:69,22:3,-1:7,22:7,-1:4,22:13,70,22:10,-1:18,59:25,-1:48,22:3,-1:7,22:7,-1:4,22:13,71,22:10,-1:23,60:3,-1:7,60:3,-1:9,60:6,-1:40,22:3,-1:7,22:7,-1:4,22:4,72,22:19,-1:50,168,-1:40,22:3,-1:7,22:7,-1:4,22:6,73,22:17,-1:61,169,-1:29,22:3,-1:7,22:7,-1:4,22:18,74,22:5,-1:48,194,-1:42,22:3,-1:7,22:7,-1:4,22:6,75,22:17,-1:60,170,-1:30,22:3,-1:7,22:7,-1:4,22:4,77,22:19,-1:47,173,-1:43,22:3,-1:7,22:7,-1:4,78,22:23,-1:18,139:25,-1:43,135:25,-1:78,174,-1:63,115,-1:76,176,-1:58,175,-1:66,177,-1:74,178,-1:69,169,-1:60,115,-1:77,115,-1:70,179,-1:52,180,-1:68,76,-1:22,79:2,80,119,118,79,81,79:11,82,79:5,83,79,84,79:2,192:7,79:4,192:24,79:4,182:2,-1,182,-1,182:13,120,182:5,-1,182:18,86,182:4,87,182:21,-1,182,-1,182:12,183,120,184:3,182:2,-1,182:4,184:7,182:4,184:24,182:4,88:13,121,88:54,93:2,94,126,94,93:6,125,93:56,96:2,97,128,97,96:7,127,96:2,98,96:13,99:7,96:4,99:24,96:4,102:2,103,129,103,102:7,104,102:2,105,102:13,106:7,102:4,106:24,102:4,-1:19,22:3,-1:7,22:7,-1:4,22:14,133,22:9,-1:48,171,-1:72,175,-1:37,22:3,-1:7,22:7,-1:4,22:19,141,22:4,-1:23,22:3,-1:7,22:7,-1:4,22:13,143,22:10,-1:23,22:3,-1:7,22:7,-1:4,22:9,145,22:14,-1:23,22:3,-1:7,22:7,-1:4,22:16,147,22:7,-1:23,22:3,-1:7,22:7,-1:4,22:14,149,22:9,-1:23,22:3,-1:7,22:7,-1:4,22:16,151,22:7,-1:23,22:3,-1:7,22:7,-1:4,22:16,153,22:7,-1:23,22:3,-1:7,22:7,-1:4,22:5,155,22:18,-1:23,22:3,-1:7,22:7,-1:4,22:14,157,22:9,-1:23,22:3,-1:7,22:7,-1:4,22:3,159,22:20,-1:23,22:3,-1:7,22:7,-1:4,22:5,161,22:18,-1:23,22:3,-1:7,22:7,-1:4,22:5,163,22:18,-1:23,22:3,-1:7,22:7,-1:4,165,22:23,-1:23,22:3,-1:7,22:7,-1:4,22:13,195,22:7,223,22:2,-1:23,22:3,-1:7,22:7,-1:4,22:14,196,22:9,-1:23,22:3,-1:7,22:7,-1:4,22:14,197,22:9,-1:23,22:3,-1:7,22:7,-1:4,22:14,198,22:9,-1:23,22:3,-1:7,22:7,-1:4,22:9,199,22:7,232,22:6,-1:23,22:3,-1:7,22:7,-1:4,22:5,200,22:18,-1:23,22:3,-1:7,22:7,-1:4,22:19,201,22:4,-1:23,22:3,-1:7,22:7,-1:4,22:13,202,22:10,-1:23,22:3,-1:7,22:7,-1:4,22:5,203,22:18,-1:23,22:3,-1:7,22:7,-1:4,22:19,204,22:4,-1:23,22:3,-1:7,22:7,-1:4,22:4,205,22:19,-1:23,22:3,-1:7,22:7,-1:4,22:13,206,22:10,-1:23,22:3,-1:7,22:7,-1:4,22:5,207,22:18,-1:23,22:3,-1:7,22:7,-1:4,22:8,211,225,22:8,226,22:5,-1:23,22:3,-1:7,22:7,-1:4,22:13,212,22:10,-1:23,22:3,-1:7,22:7,-1:4,22:18,213,22:5,-1:23,22:3,-1:7,22:7,-1:4,22:18,214,22:5,-1:23,22:3,-1:7,22:7,-1:4,22:7,215,22:15,216,-1:23,22:3,-1:7,22:7,-1:4,22:16,217,22:7,-1:23,22:3,-1:7,22:7,-1:4,22:5,218,22:18,-1:23,22:3,-1:7,22:7,-1:4,22:7,219,22:16,-1:23,22:3,-1:7,22:7,-1:4,22:18,220,22:5,-1:23,22:3,-1:7,22:7,-1:4,22:5,224,22:18,-1:23,22:3,-1:7,22:7,-1:4,22:15,227,22:8,-1:23,22:3,-1:7,22:7,-1:4,22:9,228,22:14,-1:23,22:3,-1:7,22:7,-1:4,22:19,229,22:4,-1:23,22:3,-1:7,22:7,-1:4,22:22,231,22,-1:23,22:3,-1:7,22:7,-1:4,22:2,233,22:21,-1:23,22:3,-1:7,22:7,-1:4,22:9,235,22:14,-1:23,22:3,-1:7,22:7,-1:4,22:16,236,22:7,-1:23,22:3,-1:7,22:7,-1:4,22:18,237,22:5,-1:23,22:3,-1:7,22:7,-1:4,22:18,238,22:5,-1:23,22:3,-1:7,22:7,-1:4,22,239,22:22,-1:23,22:3,-1:7,22:7,-1:4,240,22:23,-1:4");
	public int yylex ()
		throws java.io.IOException {
		char yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			if (YYEOF != yy_lookahead) {
				yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YYEOF == yy_lookahead && true == yy_initial) {

	return YYEOF;
				}
				else if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_to_mark();
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_pushback();
					}
					if (0 != (YY_START & yy_anchor)) {
						yy_move_start();
					}
					switch (yy_last_accept_state) {
					case 1:
						{ /* ignore */ }
					case -2:
						break;
					case 2:
						{ /* ignore */ }
					case -3:
						break;
					case 3:
						{ /* ignore */ }
					case -4:
						break;
					case 4:
						{ value = makeToken('!'); return('!'); }
					case -5:
						break;
					case 5:
						{ value = makeToken('%'); return('%'); }
					case -6:
						break;
					case 6:
						{ value = makeToken('&'); return('&'); }
					case -7:
						break;
					case 7:
						{ value = makeToken('('); return('('); }
					case -8:
						break;
					case 8:
						{ value = makeToken(')'); return(')'); }
					case -9:
						break;
					case 9:
						{ value = makeToken('*'); return('*'); }
					case -10:
						break;
					case 10:
						{ value = makeToken('+'); return('+'); }
					case -11:
						break;
					case 11:
						{ value = makeToken(',');  return(','); }
					case -12:
						break;
					case 12:
						{ value = makeToken('-'); return('-'); }
					case -13:
						break;
					case 13:
						{ value = makeToken('.'); return('.'); }
					case -14:
						break;
					case 14:
						{ value = makeToken('/'); return('/'); }
					case -15:
						break;
					case 15:
						{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
					case -16:
						break;
					case 16:
						{ value = makeToken(':'); return(':'); }
					case -17:
						break;
					case 17:
						{ /* is end of TYPEDEF,ENUM if no open brackets exist.. */ 
								 	if(openBrackets==0) yybegin(YYINITIAL);
								 	if (log) System.out.println("YYINITIAL BEGIN");
									 value = makeToken(';'); 
									 return(';');
								}
					case -18:
						break;
					case 18:
						{ value = makeToken('<'); return('<'); }
					case -19:
						break;
					case 19:
						{ value = makeToken('='); return('='); }
					case -20:
						break;
					case 20:
						{ value = makeToken('>'); return('>'); }
					case -21:
						break;
					case 21:
						{ value = makeToken('?'); return('?'); }
					case -22:
						break;
					case 22:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -23:
						break;
					case 23:
						{ value = makeToken('['); return('['); }
					case -24:
						break;
					case 24:
						{ value = makeToken(']'); return(']'); }
					case -25:
						break;
					case 25:
						{ value = makeToken('^'); return('^'); }
					case -26:
						break;
					case 26:
						{ 
								  openBrackets++;
								  value = makeToken(123); return(123); 
								}
					case -27:
						break;
					case 27:
						{ value = makeToken('|'); return('|'); }
					case -28:
						break;
					case 28:
						{ openBrackets--;
								  value = makeToken(125); return(125); 
								}
					case -29:
						break;
					case 29:
						{ value = makeToken('~'); return('~'); }
					case -30:
						break;
					case 30:
						{ value = makeToken(HeaderFileParser.NE_OP); return(HeaderFileParser.NE_OP); }
					case -31:
						break;
					case 31:
						{ value = makeToken(HeaderFileParser.MOD_ASSIGN); return(HeaderFileParser.MOD_ASSIGN); }
					case -32:
						break;
					case 32:
						{ value = makeToken(HeaderFileParser.AND_OP); return(HeaderFileParser.AND_OP); }
					case -33:
						break;
					case 33:
						{ value = makeToken(HeaderFileParser.AND_ASSIGN); return(HeaderFileParser.AND_ASSIGN); }
					case -34:
						break;
					case 34:
						{ value = makeToken(HeaderFileParser.MUL_ASSIGN); return(HeaderFileParser.MUL_ASSIGN); }
					case -35:
						break;
					case 35:
						{ value = makeToken(HeaderFileParser.INC_OP); return(HeaderFileParser.INC_OP); }
					case -36:
						break;
					case 36:
						{ value = makeToken(HeaderFileParser.ADD_ASSIGN); return(HeaderFileParser.ADD_ASSIGN); }
					case -37:
						break;
					case 37:
						{ value = makeToken(HeaderFileParser.DEC_OP); return(HeaderFileParser.DEC_OP); }
					case -38:
						break;
					case 38:
						{ value = makeToken(HeaderFileParser.SUB_ASSIGN); return(HeaderFileParser.SUB_ASSIGN); }
					case -39:
						break;
					case 39:
						{ value = makeToken(HeaderFileParser.PTR_OP); return(HeaderFileParser.PTR_OP); }
					case -40:
						break;
					case 40:
						{ 
										  value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
										  return(HeaderFileParser.FLOATING_CONSTANT); 
									   }
					case -41:
						break;
					case 41:
						{ 
									  yybegin(MULTILINECOMMENT);
									  if (log) System.out.println("MULTILINECOMMENT BEGIN");
								  	}
					case -42:
						break;
					case 42:
						{ /* ignore */	}
					case -43:
						break;
					case 43:
						{ value = makeToken(HeaderFileParser.DIV_ASSIGN); return(HeaderFileParser.DIV_ASSIGN); }
					case -44:
						break;
					case 44:
						{ 
										 value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
										 return(HeaderFileParser.FLOATING_CONSTANT); 
									   }
					case -45:
						break;
					case 45:
						{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
					case -46:
						break;
					case 46:
						{ value = makeToken(HeaderFileParser.LEFT_OP); return(HeaderFileParser.LEFT_OP); }
					case -47:
						break;
					case 47:
						{ value = makeToken(HeaderFileParser.LE_OP); return(HeaderFileParser.LE_OP); }
					case -48:
						break;
					case 48:
						{ value = makeToken(HeaderFileParser.EQ_OP); return(HeaderFileParser.EQ_OP); }
					case -49:
						break;
					case 49:
						{ value = makeToken(HeaderFileParser.GE_OP); return(HeaderFileParser.GE_OP); }
					case -50:
						break;
					case 50:
						{ value = makeToken(HeaderFileParser.RIGHT_OP); return(HeaderFileParser.RIGHT_OP); }
					case -51:
						break;
					case 51:
						{ value = makeToken(HeaderFileParser.XOR_ASSIGN); return(HeaderFileParser.XOR_ASSIGN); }
					case -52:
						break;
					case 52:
						{ 
									  value = makeToken(HeaderFileParser.IF);
									  return(HeaderFileParser.IF); 
									}
					case -53:
						break;
					case 53:
						{ value = makeToken(HeaderFileParser.OR_ASSIGN); return(HeaderFileParser.OR_ASSIGN); }
					case -54:
						break;
					case 54:
						{ value = makeToken(HeaderFileParser.OR_OP); return(HeaderFileParser.OR_OP); }
					case -55:
						break;
					case 55:
						{ 
											  value = makeToken(HeaderFileParser.STRING);
						                      return(HeaderFileParser.STRING);
											}
					case -56:
						break;
					case 56:
						{
										yybegin(SKIPMACRO);
									  if (log) System.out.println("SKIPMACRO BEGIN");
									}
					case -57:
						break;
					case 57:
						{ 
									  value = makeToken(HeaderFileParser.CHARACTER_CONSTANT); 
									  return(HeaderFileParser.CHARACTER_CONSTANT); 
									}
					case -58:
						break;
					case 58:
						{ value = makeToken(HeaderFileParser.ELLIPSIS); return(HeaderFileParser.ELLIPSIS); }
					case -59:
						break;
					case 59:
						{ 
									 value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
									  return(HeaderFileParser.FLOATING_CONSTANT); 
									}
					case -60:
						break;
					case 60:
						{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
					case -61:
						break;
					case 61:
						{ value = makeToken(HeaderFileParser.LEFT_ASSIGN); return(HeaderFileParser.LEFT_ASSIGN); }
					case -62:
						break;
					case 62:
						{ value = makeToken(HeaderFileParser.RIGHT_ASSIGN); return(HeaderFileParser.RIGHT_ASSIGN); }
					case -63:
						break;
					case 63:
						{ 
									  value = makeToken(HeaderFileParser.FOR);
									  return(HeaderFileParser.FOR); 
									}
					case -64:
						break;
					case 64:
						{ 
									  value = makeToken(HeaderFileParser.INT);
									  return(HeaderFileParser.INT); 
								}
					case -65:
						break;
					case 65:
						{
									  value = makeToken(HeaderFileParser.ENUM);
									  return(HeaderFileParser.ENUM); 
									}
					case -66:
						break;
					case 66:
						{ 
									  value = makeToken(HeaderFileParser.LONG);
									  return(HeaderFileParser.LONG); 
								}
					case -67:
						break;
					case 67:
						{ 
									  value = makeToken(HeaderFileParser.VOID);
									  return(HeaderFileParser.VOID); 
									}
					case -68:
						break;
					case 68:
						{
									  value = makeToken(HeaderFileParser.SHORT);
									   return(HeaderFileParser.SHORT); 
								}
					case -69:
						break;
					case 69:
						{ value = makeToken(HeaderFileParser.UNION);
									return(HeaderFileParser.UNION);
								}
					case -70:
						break;
					case 70:
						{
									  value = makeToken(HeaderFileParser.EXTERN);
									   return(HeaderFileParser.EXTERN); 
									}
					case -71:
						break;
					case 71:
						{ 
									  value = makeToken(HeaderFileParser.RETURN);
									  return(HeaderFileParser.RETURN); 
									}
					case -72:
						break;
					case 72:
						{
									  value = makeToken(HeaderFileParser.SIGNED);
									   return(HeaderFileParser.SIGNED); 
								}
					case -73:
						break;
					case 73:
						{
									  value = makeToken(HeaderFileParser.SIZEOF);
									   return(HeaderFileParser.SIZEOF); 
									}
					case -74:
						break;
					case 74:
						{ 
									  value = makeToken(HeaderFileParser.STRUCT);
									  return(HeaderFileParser.STRUCT); 
									}
					case -75:
						break;
					case 75:
						{
									  value = makeToken(HeaderFileParser.TYPEDEF);
									  return(HeaderFileParser.TYPEDEF); 
									}
					case -76:
						break;
					case 76:
						{ 	yybegin(INCLUDE);
										value = makeToken(HeaderFileParser.INCLUDE);
									  	return HeaderFileParser.INCLUDE;
									}
					case -77:
						break;
					case 77:
						{ 
									  value = makeToken(HeaderFileParser.UNSIGNED);									  
									  return(HeaderFileParser.UNSIGNED); 
									}
					case -78:
						break;
					case 78:
						{
 									  attribute = new AttributeElement(makeToken(HeaderFileParser.ATTRIBUTE));
 									  if (log) System.out.println("ATTRIBUTEBEGIN");
									  yybegin(ATTRIBUTEBEGIN);
									  //return(NesCparser.ATTRIBUTE); 
									}
					case -79:
						break;
					case 79:
						{ /* ignore */ }
					case -80:
						break;
					case 80:
						{ yybegin(YYINITIAL); 
									 if (log) System.out.println("YYINITIAL BEGIN");
									}
					case -81:
						break;
					case 81:
						{ value = makeToken('"'); return('"'); }
					case -82:
						break;
					case 82:
						{ value = makeToken(HeaderFileParser.PATH); return(HeaderFileParser.PATH); }
					case -83:
						break;
					case 83:
						{ value = makeToken('<'); return('<'); }
					case -84:
						break;
					case 84:
						{ value = makeToken('>'); return('>'); }
					case -85:
						break;
					case 85:
						{ /* ignore */	}
					case -86:
						break;
					case 86:
						{ value = makeToken(HeaderFileParser.HEADERFILE); return(HeaderFileParser.HEADERFILE); }
					case -87:
						break;
					case 87:
						{ value = makeToken(HeaderFileParser.HEADERFILE); return(HeaderFileParser.HEADERFILE); }
					case -88:
						break;
					case 88:
						{  }
					case -89:
						break;
					case 89:
						{
											if (log) System.out.println("YYINITIAL BEGIN");
											 yybegin(YYINITIAL); 
											}
					case -90:
						break;
					case 90:
						{ 
											if (log) System.out.println("YYINITIAL BEGIN");
											yybegin(YYINITIAL); 
											}
					case -91:
						break;
					case 91:
						{ 
											if (log) System.out.println("YYINITIAL BEGIN");
											yybegin(YYINITIAL);
											}
					case -92:
						break;
					case 92:
						{ /* ignore */ }
					case -93:
						break;
					case 93:
						{ /* error go back */
											 if (log) System.out.println("YYINITIAL BEGIN");
											  yybegin(YYINITIAL);
											}
					case -94:
						break;
					case 94:
						{ /* ignore */ }
					case -95:
						break;
					case 95:
						{   
											  attribute.setOpenBrackets();
//											  Token(value, yytext(), yyline, yychar, yychar+ yytext().length());
											  yybegin(ATTRIBUTE); 	
											}
					case -96:
						break;
					case 96:
						{ /* error */
											  attribute.setError();
											  if (log) System.out.println("YYINITIAL BEGIN");	
											  yybegin(YYINITIAL);
											}
					case -97:
						break;
					case 97:
						{ /* ignore */ }
					case -98:
						break;
					case 98:
						{ attribute.addSpacer();	}
					case -99:
						break;
					case 99:
						{ /* attribute word  */
											  attribute.addWord(makeToken(HeaderFileParser.IDENTIFIER));	
											}
					case -100:
						break;
					case 100:
						{ 
											  //finished attribute declaration
											  attribute.setClosingBrackets();
											  attributeElements.add(attribute);
											  if (log) System.out.println("YYINITIAL BEGIN");	
											  yybegin(YYINITIAL);
											}
					case -101:
						break;
					case 101:
						{ /* attribute function */ 
											  attribute.addFunction(yytext().substring(0,yytext().length()-1), 
											  						yyline, 
											  						yychar-1, 
											  						yychar+yytext().length()-1);	
											  if (log) System.out.println("ATTRIBUTEFUNCTION BEGIN");											  						
											  yybegin(ATTRIBUTEFUNCTION);
											}
					case -102:
						break;
					case 102:
						{ /* error */ 
											  attribute.setError();
											  if (log) System.out.println("ATTRIBUTE BEGIN");	
											  yybegin(ATTRIBUTE);  
											}
					case -103:
						break;
					case 103:
						{ /* ignore */ }
					case -104:
						break;
					case 104:
						{ 	
											  attribute.finishFunction();
											  if (log) System.out.println("ATTRIBUTE BEGIN");	
											  yybegin(ATTRIBUTE); 
											}
					case -105:
						break;
					case 105:
						{ attribute.addFunctionArgumentDelimiter(); }
					case -106:
						break;
					case 106:
						{ attribute.addFunctionArgument(makeToken(HeaderFileParser.IDENTIFIER));	}
					case -107:
						break;
					case 108:
						{ /* ignore */ }
					case -108:
						break;
					case 109:
						{ /* ignore */ }
					case -109:
						break;
					case 110:
						{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
					case -110:
						break;
					case 111:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -111:
						break;
					case 112:
						{ 
										  value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
										  return(HeaderFileParser.FLOATING_CONSTANT); 
									   }
					case -112:
						break;
					case 113:
						{ 
										 value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
										 return(HeaderFileParser.FLOATING_CONSTANT); 
									   }
					case -113:
						break;
					case 114:
						{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
					case -114:
						break;
					case 115:
						{
										yybegin(SKIPMACRO);
									  if (log) System.out.println("SKIPMACRO BEGIN");
									}
					case -115:
						break;
					case 116:
						{ 
									 value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
									  return(HeaderFileParser.FLOATING_CONSTANT); 
									}
					case -116:
						break;
					case 117:
						{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
					case -117:
						break;
					case 118:
						{ /* ignore */ }
					case -118:
						break;
					case 119:
						{ yybegin(YYINITIAL); 
									 if (log) System.out.println("YYINITIAL BEGIN");
									}
					case -119:
						break;
					case 120:
						{ value = makeToken(HeaderFileParser.PATH); return(HeaderFileParser.PATH); }
					case -120:
						break;
					case 121:
						{  }
					case -121:
						break;
					case 122:
						{ 
											if (log) System.out.println("YYINITIAL BEGIN");
											yybegin(YYINITIAL); 
											}
					case -122:
						break;
					case 123:
						{ 
											if (log) System.out.println("YYINITIAL BEGIN");
											yybegin(YYINITIAL);
											}
					case -123:
						break;
					case 124:
						{ /* ignore */ }
					case -124:
						break;
					case 125:
						{ /* error go back */
											 if (log) System.out.println("YYINITIAL BEGIN");
											  yybegin(YYINITIAL);
											}
					case -125:
						break;
					case 126:
						{ /* ignore */ }
					case -126:
						break;
					case 127:
						{ /* error */
											  attribute.setError();
											  if (log) System.out.println("YYINITIAL BEGIN");	
											  yybegin(YYINITIAL);
											}
					case -127:
						break;
					case 128:
						{ /* ignore */ }
					case -128:
						break;
					case 129:
						{ /* ignore */ }
					case -129:
						break;
					case 131:
						{ /* ignore */ }
					case -130:
						break;
					case 132:
						{ 
									  value = makeToken(HeaderFileParser.INTEGER_CONSTANT); 
									  return(HeaderFileParser.INTEGER_CONSTANT); 
									}
					case -131:
						break;
					case 133:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -132:
						break;
					case 134:
						{ 
										  value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
										  return(HeaderFileParser.FLOATING_CONSTANT); 
									   }
					case -133:
						break;
					case 135:
						{ 
										 value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
										 return(HeaderFileParser.FLOATING_CONSTANT); 
									   }
					case -134:
						break;
					case 137:
						{ /* ignore */ }
					case -135:
						break;
					case 138:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -136:
						break;
					case 139:
						{ 
										  value = makeToken(HeaderFileParser.FLOATING_CONSTANT); 
										  return(HeaderFileParser.FLOATING_CONSTANT); 
									   }
					case -137:
						break;
					case 141:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -138:
						break;
					case 143:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -139:
						break;
					case 145:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -140:
						break;
					case 147:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -141:
						break;
					case 149:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -142:
						break;
					case 151:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -143:
						break;
					case 153:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -144:
						break;
					case 155:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -145:
						break;
					case 157:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -146:
						break;
					case 159:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -147:
						break;
					case 161:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -148:
						break;
					case 163:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -149:
						break;
					case 165:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -150:
						break;
					case 191:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -151:
						break;
					case 192:
						{ /* ignore */ }
					case -152:
						break;
					case 195:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -153:
						break;
					case 196:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -154:
						break;
					case 197:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -155:
						break;
					case 198:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -156:
						break;
					case 199:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -157:
						break;
					case 200:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -158:
						break;
					case 201:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -159:
						break;
					case 202:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -160:
						break;
					case 203:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -161:
						break;
					case 204:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -162:
						break;
					case 205:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -163:
						break;
					case 206:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -164:
						break;
					case 207:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -165:
						break;
					case 208:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -166:
						break;
					case 209:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -167:
						break;
					case 210:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -168:
						break;
					case 211:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -169:
						break;
					case 212:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -170:
						break;
					case 213:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -171:
						break;
					case 214:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -172:
						break;
					case 215:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -173:
						break;
					case 216:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -174:
						break;
					case 217:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -175:
						break;
					case 218:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -176:
						break;
					case 219:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -177:
						break;
					case 220:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -178:
						break;
					case 221:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -179:
						break;
					case 222:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -180:
						break;
					case 223:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -181:
						break;
					case 224:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -182:
						break;
					case 225:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -183:
						break;
					case 226:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -184:
						break;
					case 227:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -185:
						break;
					case 228:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -186:
						break;
					case 229:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -187:
						break;
					case 230:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -188:
						break;
					case 231:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -189:
						break;
					case 232:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -190:
						break;
					case 233:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -191:
						break;
					case 234:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -192:
						break;
					case 235:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -193:
						break;
					case 236:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -194:
						break;
					case 237:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -195:
						break;
					case 238:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -196:
						break;
					case 239:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -197:
						break;
					case 240:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -198:
						break;
					case 241:
						{ 
									  return(identifier_or_typedef_name());
									}
					case -199:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
					}
				}
			}
		}
	}
}
