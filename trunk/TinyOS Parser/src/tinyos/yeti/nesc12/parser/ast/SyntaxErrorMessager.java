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
package tinyos.yeti.nesc12.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java_cup.runtime.Symbol;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.RawParser;
import tinyos.yeti.nesc12.parser.sym;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.error.BaseErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.error.TokenErrorASTNode;

/**
 * This class is used to create new {@link ErrorASTNode}s for the parser.
 * @author Benjamin Sigg
 */
public class SyntaxErrorMessager {
    private RawParser parser;
    
    public SyntaxErrorMessager( RawParser parser ){
        this.parser = parser;
    }
    
    /**
     * Creates a new {@link ASTModelNode} which represents an error.
     * @param rule the rule which found the error, should be human readable
     * @param expected the symbol which was expected
     * @param found the symbol which was found
     * @param peek the symbol that was atop of the stack
     * @return an error
     */
    public ErrorASTNode error( String rule, String expected, Symbol found, Symbol peek ){
        return error( rule, expected, found, peek, -1, -1 );
    }

    /**
     * Creates a new {@link ASTModelNode} which represents an error.
     * @param rule the rule which found the error, should be human readable
     * @param expected the symbol which was expected
     * @param found the symbol which was found
     * @param peek the symbol that was atop of the stack
     * @param left first character of the error
     * @param right last character of the error
     * @return an error
     */
    public ErrorASTNode error( String rule, String expected, Symbol found, Symbol peek, int left, int right ){
        Token token = null;
        if( found.value instanceof Token )
            token = (Token)found.value;
        
        if( left == -1 && right == -1 ){
            left = found.left;
            right = found.right;
        }
        
        if(( left == -1 || right == -1 ) && (found.sym == sym.EOF || (peek != null && peek.sym == sym.EOF))){
            left = Integer.MAX_VALUE-1;
            right = Integer.MAX_VALUE;
        }
        
        StringBuilder message = new StringBuilder();
        boolean messageBegan = false;
        
        if( rule != null ){
            message.append( rule );
            message.append( ": " );
        }
        if( expected != null ){
            message.append( "expected " );
            message.append( prefix( expected ) );
            messageBegan = true;
        }
        
        int state = peek.parse_state;
        
        int[] suggestions = listImportantFollowers( state );
        if( suggestions != null && suggestions.length >  0 ){
            if( messageBegan ) {
                message.append( ", " );
            }
            messageBegan = true;
            message.append( "common missing tokens at this place: " );
            for( int i = 0, n = suggestions.length; i<n; i++ ){
                if( i > 0 )
                    message.append( ", " );
                
                message.append( '\'' );
                message.append( signs( suggestions[i] ) );
                message.append( '\'' );
            }
        }
        
        short[] tab = parser.action_table()[ state ];
        
        boolean first = true;
        
        if( tab != null ){
            for( int i = 0, n = tab.length; i<n; i += 2 ){
                int tag = tab[i];
                if( tag != 0 ){
                    String sign = signs( tag );
                    if( sign != null ){
                        if( first ){
                            first = false;
                            if( messageBegan ){
                                message.append( ", " );
                            }
                            message.append( "to complete expression, insert one of: " );
                            messageBegan = true;
                        }
                        else{
                            message.append( ", " );
                        }
                        message.append( '\'' );
                        message.append( sign );
                        message.append( '\'' );
                    }
                }
            }
        }
        
        if( token == null ){
            return new BaseErrorASTNode( message.toString(), new SimpleRange( left, right ) );
        }
        else{
            TokenErrorASTNode node = new TokenErrorASTNode( message.toString(), token );
            node.setRange( new SimpleRange( left, right ) );
            return node;
        }
    }
    
    /**
     * Creates a list of the most important symbols that are valid input
     * for the parser if the parser is in state <code>state</code>.
     * @param state a state of the parser
     * @return values of {@link sym} or <code>null</code> if no symbols were found
     */
    public int[] listImportantFollowers( int state ){
        short[] tab = parser.action_table()[ state ];
        if( tab == null )
            return null;

        List<Integer> list = new ArrayList<Integer>();
        
        for( int i = 0, n = tab.length; i<n; i += 2 ){
            int tag = tab[i];
            if( tag != 0 ){
                if( commonMissingToken( tag ) >= 0 ){
                    list.add( tag );
                }
            }
        }
        
        if( list.isEmpty() )
            return null;
        
        Collections.sort( list, new Comparator<Integer>(){
            public int compare( Integer a, Integer b ) {
                int importanceA = commonMissingToken( a );
                int importanceB = commonMissingToken( b );
                
                if( importanceA == importanceB )
                    return 0;
                
                if( importanceA < importanceB )
                    return -1;
                else
                    return 1;
            }
        });
        
        int[] result = new int[ list.size() ];
        for( int i = 0, n = result.length; i<n; i++ )
            result[i] = list.get( i );
        
        return result;
    }
    
    /**
     * Tells whether <code>state</code> could be followed by the symbol <code>sym</code>.
     * @param state a state of the parser
     * @param sym a possible symbol of the parser
     * @return <code>true</code> if in state <code>state</code> <code>sym</code>
     * would be a valid input, <code>false</code> otherwise
     */
    public boolean couldBeFollowedBy( int state, int sym ){
        short[] tab = parser.action_table()[state];
        if( tab == null )
            return false;
        
        for( int i = 0, n = tab.length; i<n; i+=2 ){
            if( tab[i] == sym ){
                return tab[i] != 0;
            }
        }
        
        return false;
    }
    
    /**
     * Creates a new {@link ASTModelNode} which represents a missing element.
     * @param rule the rule in which the element is missing
     * @param expected what was expected
     * @param left the index of the first character in the hole
     * @param right the index of the last character in the hole
     * @return the node
     */
    public ErrorASTNode missing( String rule, String expected, int left, int right ){
        if( left >= right )
            right = left+1;
        
        Range range = new SimpleRange( left, right );
        StringBuilder builder = new StringBuilder();
        if( rule != null ){
            builder.append( rule );
            builder.append( ": " );
        }
        
        if( expected != null ){
            builder.append( "missing " );
            builder.append( prefix( expected ) );
        }
        else{
            builder.append( "missing argument" );
        }
        
        return new BaseErrorASTNode( builder.toString(), range );
    }

    public static String prefix( String text ){
        if( text == null || text.length() == 0 )
            return null;
        
        switch( text.charAt( 0 )){
            case 'a':
            case 'A':
            case 'e':
            case 'E':
            case 'i':
            case 'I':
            case 'o':
            case 'O':
            case 'u':
            case 'U':
                return "an '" + text + "'";
        }
        
        return "a '" + text + "'";
    }
    
    /**
     * Returns the name of the group of objects <code>symbol</code> is in.
     * @param symbol a value of {@link sym}
     * @return the name of the group or <code>null</code>
     */
    public static String name( int symbol ){
        switch( symbol ){
            case sym.K_BREAK: return "flow control keyword";
            case sym.K_STATIC: return "storage class keyword";
            case sym.P_PLUS: return "arithmetic operator";
            case sym.IDENTIFIER: return "identifier";
            case sym.ENUMERATION_CONSTANT: return "enumeration constant";
            case sym.NK_INTERFACE: return "nesc keyword";
            case sym.NK_NX_UNION: return "nesc keyword";
            case sym.K_VOLATILE: return "storage class keyword";
            case sym.K_SIGNED: return "type specifier keyword";
            case sym.P_SHIFT_LEFT: return "arithmetic operator";
            case sym.K_GOTO: return "flow control keyword";
            case sym.FLOATING_CONSTANT: return "floating constant";
            case sym.P_STAR: return "arithmetic operator";
            case sym.P_POINT: return null;
            case sym.P_CURLY_CLOSE: return null;
            case sym.P_ADD_ASSIGN: return "assignment operator";
            case sym.NK_GENERIC: return "nesc keyword";
            case sym.K_DEFAULT: return null;
            case sym.NK_MODULE: return "nesc keyword";
            case sym.P_MUL_ASSIGN: return "assignment operator";
            case sym.K_DOUBLE: return "type specifier keyword";
            case sym.P_LINE: return "bit operator";
            case sym.K_SIZEOF: return "arithmetic keyword";
            case sym.INTEGER_CONSTANT: return "integer constant";
            case sym.K_LONG: return "type specifier keyword";
            case sym.P_SHIFT_LEFT_ASSIGN: return "assignment operator";
            case sym.P_NOT_EQ: return "logical operator";
            case sym.P_MINUS: return "arithmetic operator";
            case sym.K_CHAR: return "type specifier keyword";
            case sym.NK_SIGNAL: return "nesc keyword";
            case sym.P_EXCLAMATION: return "logical operator";
            case sym.P_QUESTION: return "conditional operator";
            case sym.NK_AS: return "nesc keyword";
            case sym.CHARACTER_CONSTANT: return "character constant";
            case sym.NK_CONFIGURATION: return "nesc keyword";
            case sym.K_STRUCT: return "type specifier keyword";
            case sym.K_VOID: return "type specifier keyword";
            case sym.K_ELSE: return "flow control keyword";
            case sym.NK_IMPLEMENTATION: return "nesc keyword";
            case sym.P_AND: return "logical operator";
            case sym.P_COLON: return null;
            case sym.K_REGISTER: return "storage class keyword";
            case sym.P_GREATER: return "logical operator";
            case sym.NK_ATOMIC: return "nesc keyword";
            case sym.NK_NEW: return "nesc keyword";
            case sym.K_INLINE: return null;
            case sym.K__COMPLEX: return "type specifier keyword";
            case sym.P_CARET: return "bit operator";
            case sym.P_SEMICOLON: return "semicolon";
            case sym.P_PERCENT: return "arithmetic operator";
            case sym.NP_AT: return "nesc attribute specifier";
            case sym.K_SHORT: return "type specifier keyword";
            case sym.K_ASM: return null;
            case sym.P_SHIFT_RIGHT: return "bit operator";
            case sym.P_AMP: return "bit operator";
            case sym.P_ROUND_OPEN: return null;
            case sym.P_RECT_CLOSE: return null;
            case sym.NK_COMPONENT: return "nesc keyword";
            case sym.NK_NX_STRUCT: return "nesc type specifier keyword";
            case sym.K_TYPEDEF: return "storage class keyword";
            case sym.P_DIV_ASSIGN: return "assignment operator";
            case sym.P_EQ: return "logical operator";
            case sym.K_CONST: return "storage class keyword";
            case sym.K_EXTERN: return "storage class keyword";
            case sym.P_RIGHT_ARROW: return null;
            case sym.K_DO: return "flow control keyword";
            case sym.K__BOOL: return "type specifier keyword";
            case sym.P_DECREMENT: return "arithmetic operator";
            case sym.NK_POST: return "nesc keyword";
            case sym.NK_COMMAND: return "nesc keyword";
            case sym.P_SHIFT_RIGHT_ASSIGN: return "assignment operator";
            case sym.P_MOD_ASSIGN: return "assignment operator";
            case sym.P_CURLY_OPEN: return null;
            case sym.K_SWITCH: return "flow control keyword";
            case sym.K_EXTENSION: return "extension keyword";
            case sym.P_RECT_OPEN: return null;
            case sym.K_RETURN: return "flow control keyword";
            case sym.K_CASE: return "flow control keyword";
            case sym.EOF: return "end of file";
            case sym.P_OR_ASSIGN: return "assignment operator";
            case sym.P_COMMA: return "comma";
            case sym.P_SMALLER: return "logical operator";
            case sym.P_SUB_ASSIGN: return "assignment operator";
            case sym.NK_COMPONENTS: return "nesc keyword";
            case sym.K_AUTO: return "storage class keyword";
            case sym.K_FLOAT: return "type specifier keyword";
            case sym.P_ELLIPSIS: return null;
            case sym.P_SMALLER_EQ: return "logical operator";
            case sym.K_IF: return "flow control keyword";
            case sym.P_AND_ASSIGN: return "assignment operator";
            case sym.P_ASSIGN: return "assignment operator";
            case sym.K_RESTRICT: return "storage class operator";
            case sym.P_TILDE: return "bit operator";
            case sym.NK_EVENT: return "nesc keyword";
            case sym.NK_TASK: return "nesc keyword";
            case sym.NK_CALL: return "nesc keyword";
            case sym.NK_USES: return "nesc keyword";
            case sym.P_OR: return "logical operator";
            case sym.K_FOR: return "flow control keyword";
            case sym.P_ROUND_CLOSE: return null;
            case sym.STRING: return "string";
            case sym.P_GREATER_EQ: return "logical operator";
            case sym.K_WHILE: return "flow control keyword";
            case sym.K_UNION: return "type specifier keyword";
            case sym.K_ENUM: return "type specifier keyword";
            case sym.K_INT: return "type specifier keyword";
            case sym.NK_ASYNC: return "nesc keyword";
            case sym.NK_NORACE: return "nesc keyword";
            case sym.TYPEDEF: return "typedef name";
            case sym.NP_LEFT_ARROW: return null;
            case sym.P_INCREMENT: return "arithmetic operator";
            case sym.P_XOR_ASSIGN: return "assignment operator";
            case sym.K_CONTINUE: return "flow control keyword";
            case sym.P_SLASH: return "arithmetic operator";
            case sym.NK_PROVIDES: return "nesc keyword";
            case sym.K_UNSIGNED: return "type specifier keyword";
        }
        
        return null;
    }
    
    /**
     * Tells whether <code>symbol</code> is often missing or not. As lower
     * as the result is, as more important the symbol is. Values below 0
     * are invalid.
     * @param symbol some value of {@link sym}
     * @return the importance of <code>symbol</code> where small numbers mean
     * that <code>symbol</code> is more important, but any number below 0
     * is invalid.
     */
    public static int commonMissingToken( int symbol ){
        switch( symbol ){
            case sym.P_SEMICOLON:
                return 0;
            case sym.P_CURLY_CLOSE:
            case sym.P_ROUND_CLOSE:
            case sym.P_RECT_CLOSE:
                return 1;
            case sym.P_CURLY_OPEN:
            case sym.P_ROUND_OPEN:
            case sym.P_RECT_OPEN:
                return 2;
        }
        
        return -1;
    }
    
    /**
     * Gets the token, or something that describes the token, that would instruct
     * the lexer to create <code>symbol</code>.
     * @param symbol some value of {@link sym}
     * @return the token <code>symbol</code> represents
     */
    public static String signs( int symbol ){
        switch( symbol ){
            case sym.K_BREAK: return "break";
            case sym.K_STATIC: return "static";
            case sym.P_PLUS: return "+";
            case sym.IDENTIFIER: return "identifier";
            case sym.ENUMERATION_CONSTANT: return "enumeration constant";
            case sym.NK_INTERFACE: return "interface";
            case sym.NK_NX_UNION: return "nx_union";
            case sym.K_VOLATILE: return "volatile";
            case sym.K_SIGNED: return "signed";
            case sym.P_SHIFT_LEFT: return "<<";
            case sym.K_GOTO: return "goto";
            case sym.FLOATING_CONSTANT: return "floating constant";
            case sym.P_STAR: return "*";
            case sym.P_POINT: return ".";
            case sym.P_CURLY_CLOSE: return "}";
            case sym.P_ADD_ASSIGN: return "+=";
            case sym.NK_GENERIC: return "generic";
            case sym.K_DEFAULT: return "default";
            case sym.NK_MODULE: return "module";
            case sym.P_MUL_ASSIGN: return "*=";
            case sym.K_DOUBLE: return "double";
            case sym.P_LINE: return "|";
            case sym.K_SIZEOF: return "sizeof";
            case sym.INTEGER_CONSTANT: return "integer constant";
            case sym.K_LONG: return "long";
            case sym.P_SHIFT_LEFT_ASSIGN: return "<<=";
            case sym.P_NOT_EQ: return "!=";
            case sym.P_MINUS: return "-";
            case sym.K_CHAR: return "char";
            case sym.NK_SIGNAL: return "signal";
            case sym.P_EXCLAMATION: return "!";
            case sym.P_QUESTION: return "?";
            case sym.NK_AS: return "as";
            case sym.CHARACTER_CONSTANT: return "character constant";
            case sym.NK_CONFIGURATION: return "configuration";
            case sym.K_STRUCT: return "struct";
            case sym.K_VOID: return "void";
            case sym.K_ELSE: return "else";
            case sym.NK_IMPLEMENTATION: return "implementation";
            case sym.P_AND: return "&&";
            case sym.P_COLON: return ":";
            case sym.K_REGISTER: return "register";
            case sym.P_GREATER: return ">";
            case sym.NK_ATOMIC: return "atomic";
            case sym.NK_NEW: return "new";
            case sym.K_INLINE: return "inline";
            case sym.K__COMPLEX: return "_Complex";
            case sym.P_CARET: return "^";
            case sym.P_SEMICOLON: return ";";
            case sym.P_PERCENT: return "%";
            case sym.NP_AT: return "@";
            case sym.K_SHORT: return "short";
            case sym.K_ASM: return "asm";
            case sym.P_SHIFT_RIGHT: return ">>";
            case sym.P_AMP: return "&";
            case sym.P_ROUND_OPEN: return "(";
            case sym.P_RECT_CLOSE: return "]";
            case sym.NK_COMPONENT: return "component";
            case sym.NK_NX_STRUCT: return "nx_struct";
            case sym.K_TYPEDEF: return "typedef";
            case sym.P_DIV_ASSIGN: return "/=";
            case sym.P_EQ: return "==";
            case sym.K_CONST: return "const";
            case sym.K_EXTERN: return "extern";
            case sym.P_RIGHT_ARROW: return "->";
            case sym.K_DO: return "do";
            case sym.K__BOOL: return "_BOOL";
            case sym.P_DECREMENT: return "--";
            case sym.NK_POST: return "post";
            case sym.NK_COMMAND: return "command";
            case sym.P_SHIFT_RIGHT_ASSIGN: return ">>=";
            case sym.P_MOD_ASSIGN: return "%=";
            case sym.P_CURLY_OPEN: return "{";
            case sym.K_SWITCH: return "switch";
            case sym.K_EXTENSION: return "extension";
            case sym.P_RECT_OPEN: return "[";
            case sym.K_RETURN: return "return";
            case sym.K_CASE: return "case";
            case sym.EOF: return "end of file";
            case sym.P_OR_ASSIGN: return "|=";
            case sym.P_COMMA: return ",";
            case sym.P_SMALLER: return "<";
            case sym.P_SUB_ASSIGN: return "-=";
            case sym.NK_COMPONENTS: return "components";
            case sym.K_AUTO: return "auto";
            case sym.K_FLOAT: return "float";
            case sym.P_ELLIPSIS: return "...";
            case sym.P_SMALLER_EQ: return "<=";
            case sym.K_IF: return "if";
            case sym.P_AND_ASSIGN: return "&=";
            case sym.P_ASSIGN: return "=";
            case sym.K_RESTRICT: return "restrict";
            case sym.P_TILDE: return "~";
            case sym.NK_EVENT: return "event";
            case sym.NK_TASK: return "task";
            case sym.NK_CALL: return "call";
            case sym.NK_USES: return "uses";
            case sym.P_OR: return "||";
            case sym.K_FOR: return "for";
            case sym.P_ROUND_CLOSE: return ")";
            case sym.STRING: return "string";
            case sym.P_GREATER_EQ: return ">=";
            case sym.K_WHILE: return "while";
            case sym.K_UNION: return "union";
            case sym.K_ENUM: return "enum";
            case sym.K_INT: return "int";
            case sym.NK_ASYNC: return "async";
            case sym.NK_NORACE: return "norace";
            case sym.TYPEDEF: return "typedef name";
            case sym.NP_LEFT_ARROW: return "<-";
            case sym.P_INCREMENT: return "++";
            case sym.P_XOR_ASSIGN: return "^=";
            case sym.K_CONTINUE: return "continue";
            case sym.P_SLASH: return "/";
            case sym.NK_PROVIDES: return "provides";
            case sym.K_UNSIGNED: return "unsigned";
        }
        
        return null;
    }
}
