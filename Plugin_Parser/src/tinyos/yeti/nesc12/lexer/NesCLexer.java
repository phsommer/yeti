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
package tinyos.yeti.nesc12.lexer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;
import tinyos.yeti.nesc12.parser.RawLexer;
import tinyos.yeti.nesc12.parser.ScopeStack;
import tinyos.yeti.nesc12.parser.sym;
import tinyos.yeti.preprocessor.BaseFileInfo;
import tinyos.yeti.preprocessor.Preprocessor;
import tinyos.yeti.preprocessor.PreprocessorReader;

public class NesCLexer extends Lexer implements RawLexer{
    private Symbol follow;
    private Symbol cache;
    
    /**
     * Does all the steps necessary to activate the lexer and lexes <code>code</code>. 
     * @param code the code to lex
     * @return the tokens found in <code>code</code>
     */
    public static List<Symbol> quickLexer( String code ){
        try{
            Preprocessor p = new Preprocessor();
            PreprocessorReader in = p.open( new BaseFileInfo( new File( "file.c" )), new StringReader( code ), null );
            
            ScopeStack scopes = new ScopeStack( null );
            
            NesCLexer lexer = new NesCLexer( in );
            lexer.setScopeStack( scopes );
            
            List<Symbol> result = new ArrayList<Symbol>();
            
            Symbol next = lexer.next_token();
            while( next != null && next.sym != sym.EOF ){
                result.add( next );
                next = lexer.next_token();
            }
            
            return result;
        }
        catch( IOException ex ){
            ex.printStackTrace();
            return new ArrayList<Symbol>();
        }
    }
    
    public NesCLexer( Reader in ) {
        super( in );
    }

    public boolean nextIsEOF() throws IOException{
        if( follow != null )
            return false;
        
        if( cache == null ){
            cache = next_token();
        }
        
        if( cache == null )
            return true;
        
        return cache.sym == sym.EOF;
    }
    
    @Override
    protected void sendLater( Symbol symbol ) {
        follow = new Symbol( sym.S_FOLLOW, symbol.left, symbol.right, symbol.value );
    }
    
    @Override
    public Symbol next_token() throws IOException {
        if( cache != null ){
            Symbol result = cache;
            cache = null;
            return result;
        }
        if( follow != null ){
            Symbol result = follow;
            follow = null;
            return result;
        }
        return super.next_token();
    }
    
    public static String toString( Symbol symbol ){
        return symToText( symbol.sym ) + " [" + ((Token)symbol.value).getText() + "]"; 
    }
    
    public static String symToText( int s ){
        switch( s ){
            case sym.K_BREAK: return "k_break";
            case sym.K_STATIC: return "k_static";
            case sym.P_PLUS: return "p_plus";
            case sym.IDENTIFIER: return "identifier";
            case sym.ENUMERATION_CONSTANT: return "enumeration_constant";
            case sym.NK_INTERFACE: return "nk_interface";
            case sym.NK_NX_UNION: return "nk_nx_union";
            case sym.K_VOLATILE: return "k_volatile";
            case sym.K_SIGNED: return "k_signed";
            case sym.P_SHIFT_LEFT: return "p_shift_left";
            case sym.K_GOTO: return "k_goto";
            case sym.FLOATING_CONSTANT: return "floating_constant";
            case sym.P_STAR: return "p_star";
            case sym.P_POINT: return "p_point";
            case sym.P_CURLY_CLOSE: return "p_curly_close";
            case sym.P_ADD_ASSIGN: return "p_add_assign";
            case sym.NK_GENERIC: return "nk_generic";
            case sym.K_DEFAULT: return "k_default";
            case sym.NK_MODULE: return "nk_module";
            case sym.S_FOLLOW: return "s_follow";
            case sym.P_MUL_ASSIGN: return "p_mul_assign";
            case sym.K_DOUBLE: return "k_double";
            case sym.P_LINE: return "p_line";
            case sym.K_SIZEOF: return "k_sizeof";
            case sym.INTEGER_CONSTANT: return "integer_constant";
            case sym.K_LONG: return "k_long";
            case sym.P_SHIFT_LEFT_ASSIGN: return "p_shift_left_assign";
            case sym.P_NOT_EQ: return "p_not_eq";
            case sym.P_MINUS: return "p_minus";
            case sym.K_CHAR: return "k_char";
            case sym.NK_SIGNAL: return "nk_signal";
            case sym.P_EXCLAMATION: return "p_exclamation";
            case sym.P_QUESTION: return "p_question";
            case sym.NK_AS: return "nk_as";
            case sym.CHARACTER_CONSTANT: return "character_constant";
            case sym.NK_CONFIGURATION: return "nk_configuration";
            case sym.K_STRUCT: return "k_struct";
            case sym.K_VOID: return "k_void";
            case sym.K_ELSE: return "k_else";
            case sym.NK_IMPLEMENTATION: return "nk_implementation";
            case sym.P_AND: return "p_and";
            case sym.P_COLON: return "p_colon";
            case sym.K_REGISTER: return "k_register";
            case sym.P_GREATER: return "p_greater";
            case sym.NK_ATOMIC: return "nk_atomic";
            case sym.NK_NEW: return "nk_new";
            case sym.K_INLINE: return "k_inline";
            case sym.K__COMPLEX: return "k__complex";
            case sym.P_CARET: return "p_caret";
            case sym.P_SEMICOLON: return "p_semicolon";
            case sym.P_PERCENT: return "p_percent";
            case sym.NP_AT: return "np_at";
            case sym.K_SHORT: return "k_short";
            case sym.K_ASM: return "k_asm";
            case sym.P_SHIFT_RIGHT: return "p_shift_right";
            case sym.P_AMP: return "p_amp";
            case sym.P_ROUND_OPEN: return "p_round_open";
            case sym.P_RECT_CLOSE: return "p_rect_close";
            case sym.NK_COMPONENT: return "nk_component";
            case sym.NK_NX_STRUCT: return "nk_nx_struct";
            case sym.K_TYPEDEF: return "k_typedef";
            case sym.P_DIV_ASSIGN: return "p_div_assign";
            case sym.P_EQ: return "p_eq";
            case sym.K_CONST: return "k_const";
            case sym.K_EXTERN: return "k_extern";
            case sym.P_RIGHT_ARROW: return "p_right_arrow";
            case sym.K_DO: return "k_do";
            case sym.K__BOOL: return "k__bool";
            case sym.P_DECREMENT: return "p_decrement";
            case sym.NK_POST: return "nk_post";
            case sym.NK_COMMAND: return "nk_command";
            case sym.P_SHIFT_RIGHT_ASSIGN: return "p_shift_right_assign";
            case sym.P_MOD_ASSIGN: return "p_mod_assign";
            case sym.P_CURLY_OPEN: return "p_curly_open";
            case sym.K_SWITCH: return "k_switch";
            case sym.K_EXTENSION: return "k_extension";
            case sym.P_RECT_OPEN: return "p_rect_open";
            case sym.K_RETURN: return "k_return";
            case sym.K_CASE: return "k_case";
            case sym.EOF: return "eof";
            case sym.P_OR_ASSIGN: return "p_or_assign";
            case sym.P_COMMA: return "p_comma";
            case sym.P_SMALLER: return "p_smaller";
            case sym.P_SUB_ASSIGN: return "p_sub_assign";
            case sym.NK_COMPONENTS: return "nk_components";
            case sym.K_AUTO: return "k_auto";
            case sym.K_FLOAT: return "k_float";
            case sym.error: return "error";
            case sym.P_ELLIPSIS: return "p_ellipsis";
            case sym.P_SMALLER_EQ: return "p_smaller_eq";
            case sym.K_IF: return "k_if";
            case sym.P_AND_ASSIGN: return "p_and_assign";
            case sym.P_ASSIGN: return "p_assign";
            case sym.K_RESTRICT: return "k_restrict";
            case sym.P_TILDE: return "p_tilde";
            case sym.NK_EVENT: return "nk_event";
            case sym.NK_CALL: return "nk_call";
            case sym.NK_USES: return "nk_uses";
            case sym.P_OR: return "p_or";
            case sym.K_FOR: return "k_for";
            case sym.P_ROUND_CLOSE: return "p_round_close";
            case sym.STRING: return "string";
            case sym.P_GREATER_EQ: return "p_greater_eq";
            case sym.K_WHILE: return "k_while";
            case sym.K_UNION: return "k_union";
            case sym.K_ENUM: return "k_enum";
            case sym.K_INT: return "k_int";
            case sym.NK_ASYNC: return "nk_async";
            case sym.TYPEDEF: return "typedef";
            case sym.NP_LEFT_ARROW: return "np_left_arrow";
            case sym.P_INCREMENT: return "p_increment";
            case sym.P_XOR_ASSIGN: return "p_xor_assign";
            case sym.K_CONTINUE: return "k_continue";
            case sym.P_SLASH: return "p_slash";
            case sym.NK_PROVIDES: return "nk_provides";
            case sym.K_UNSIGNED: return "k_unsigned";
            default: return null;
        }
    }
}
