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
package tinyos.yeti.nesc12.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import java_cup.runtime.Symbol;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ListASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.error.ListErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.error.WrapperErrorASTNode;

/**
 * The advanced parser overrides the behavior of the standard CUP parser in
 * the following ways:<br>
 * <ul>
 *  <li><b>error recovery</b> instead of just popping elements from the stack
 *  until a rule is found that permits shift with the error-symbol, this parser
 *  first tries to reduce under the error symbol, and starts the default error recovery
 *  only when there is no reduce possible.</li>
 * </ul>
 * @author Benjamin Sigg
 *
 */
public class AdvancedParser extends parser{
    private RawLexer lexer;
    
    /**
     * Creates a new advanced parser.
     * @param parser the wrapper around this parser that also handles the preprocessor.
     * @param scanner the lexer that creates the tokens for this parser
     */
    public AdvancedParser( Parser parser, RawLexer lexer ){
        super( lexer );
        this.lexer = lexer;
        scopes = new ScopeStack( parser );
    }

    
    public ASTNode parseAST() throws Exception{
        Symbol result;
        if( lexer.nextIsEOF() ){
            // an empty file, not much to look at...
            result = new Symbol( 0, new TranslationUnit() );
        }
        else{
            result = parse();
        }
        
        return parseAST( result, stack, remainingErrors() );
    }
    
    /**
     * Method to retrieve as much as possible from the generated Abstract Syntax Tree
     * even if the parser stopped with a heavy error.
     * @param result the result of the parser
     * @param stack the parsers stack
     * @param remaining the remaining errors on the parser
     * @return a node that somehow combines all remaining elements
     */
    public static ASTNode parseAST( Symbol result, Stack<?> stack, List<ErrorASTNode> remaining ){
        ASTNode root;
        if( result != null && result.value instanceof ASTNode ){
            root = (ASTNode)result.value;
        }
        else{
            root = new TranslationUnit();
        }
        
        while( root.getParent() != null )
            root = root.getParent();

        // search for nodes that are still on the stack (but should not be there)
        List<ASTNode> begin = new ArrayList<ASTNode>();
        for( int i = 0, n = stack.size()-1; i < n; i++ ){
            Symbol next = (Symbol)stack.get( i );
            if( next.value instanceof ASTNode ){
                if( next.value != root ){
                    begin.add( (ASTNode)next.value );
                }
            }
        }
        
        if( !begin.isEmpty() ){
            // the stack was not built correct, try put these things together
            begin.add( root );
            Collections.sort( begin, new Comparator<ASTNode>(){
                public int compare( ASTNode a, ASTNode b ){
                    if( a == b )
                        return 0;
                    
                    Range rangeA = a.getRange();
                    Range rangeB = b.getRange();
                    
                    if( rangeA.getLeft() < rangeB.getLeft() )
                        return -1;
                    
                    if( rangeA.getLeft() > rangeB.getLeft() )
                        return 1;
                    
                    if( rangeA.getRight() < rangeB.getRight() )
                        return -1;
                    
                    if( rangeA.getRight() > rangeB.getRight() )
                        return 1;
                    
                    return 0;
                }
            });
            
            root = begin.get( begin.size()-1 );
            for( int i = begin.size()-2; i >= 0; i-- ){
                ASTNode next = begin.get( i );
                root = merge( next, root );
            }
        }
        
        if( !remaining.isEmpty() ){
            if( !(root instanceof ListASTNode )){
                root = new ListErrorASTNode( root );
            }

            ListASTNode<?>  list = (ListASTNode<?>)root;

            for( ErrorASTNode error : remaining )
                list.addError( error );
        }

        return root;
    }
    
    private static ASTNode merge( ASTNode top, ASTNode child ){
        ListASTNode<? extends ASTNode> list = searchList( top );
        if( list == null ){
            list = new ListErrorASTNode();
            list.addError( new WrapperErrorASTNode( parent( top )));
            list.addError( new WrapperErrorASTNode( child ));
            return list;
        }
        else{
            list.addError( new WrapperErrorASTNode( child ) );
            return parent( top );
        }
    }
    
    @SuppressWarnings("unchecked")
    private static ListASTNode<? extends ASTNode> searchList( ASTNode top ){
        if( top instanceof ListASTNode )
            return (ListASTNode<? extends ASTNode>)top;
        
        ASTNode parent = top.getParent();
        if( parent == null )
            return null;
        
        return searchList( parent );
    }
    
    private static ASTNode parent( ASTNode node ){
        while( node.getParent() != null )
            node = node.getParent();
        
        return node;
    }
    
    /**
     * Modified error recovery: tries to do a reduce if no shift out can be
     * found.
     * @param debug
     * @return <code>true</code> if error recovery was successful
     * @throws Exception
     */
    @Override
    protected boolean error_recovery( boolean debug ) throws Exception {
        /* the current action code */
        int act;

        /* the Symbol/stack element returned by a reduce */
        Symbol lhs_sym = null;

        /* information about production being reduced with */
        short handle_size, lhs_sym_num;

        for( _done_parsing = false; !_done_parsing; ) {
            act = get_action( ( (Symbol)stack.peek() ).parse_state, error_sym() );

            /* decode the action -- > 0 encodes shift */
            if( act > 0 ) {
                // stop
                break;
            }
            /* if its less than zero, then it encodes a reduce action */
            else if( act < 0 ) {
                /* perform the action for the reduce */
                lhs_sym = do_action( ( -act ) - 1, this, stack, tos );

                /* look up information about the production */
                lhs_sym_num = production_tab[( -act ) - 1][0];
                handle_size = production_tab[( -act ) - 1][1];

                if( debug ) 
                    debug_reduce( ( -act ) - 1, lhs_sym_num, handle_size );

                /* pop the handle off the stack */
                for( int i = 0; i < handle_size; i++ ) {
                    stack.pop();
                    tos--;
                }

                /* look up the state to go to from the one popped back to */
                act = get_reduce( ( (Symbol)stack.peek() ).parse_state,
                        lhs_sym_num );
                if( debug )
                    debug_message( "# Reduce rule: top state "
                        + ( (Symbol)stack.peek() ).parse_state + ", lhs sym "
                        + lhs_sym_num + " -> state " + act );

                /* shift to that state */
                lhs_sym.parse_state = act;

                // not accessible from here, just forget it. Let's just assume
                // our lexer does what he should do...
                //lhs_sym.used_by_parser = true;
                
                stack.push( lhs_sym );
                tos++;
                
                if( debug )
                    debug_message( "# Goto state #" + act );
            }
            /* finally if the entry is zero, we have an error */
            else if( act == 0 ) {
                // stop and try normal recover
                break;
            }
        }

        return super.error_recovery( debug );
    }
}
