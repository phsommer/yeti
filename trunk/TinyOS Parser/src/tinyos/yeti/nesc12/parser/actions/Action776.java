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
package tinyos.yeti.nesc12.parser.actions;
import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.lexer.Lexer;
import tinyos.yeti.nesc12.parser.ast.*;
import tinyos.yeti.nesc12.parser.ast.nodes.*;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.*;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.*;
import tinyos.yeti.nesc12.parser.ast.nodes.error.*;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.*;
import tinyos.yeti.nesc12.parser.ast.nodes.general.*;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.*;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.*;
import tinyos.yeti.nesc12.parser.*;
public final class Action776 implements ParserAction{
	public final java_cup.runtime.Symbol do_action(
		int                        CUP$parser$act_num,
		java_cup.runtime.lr_parser CUP$parser$parser,
		java.util.Stack            CUP$parser$stack,
		int                        CUP$parser$top,
		parser                     parser)
		throws java.lang.Exception{
	java_cup.runtime.Symbol CUP$parser$result;
 // c_compound_statement ::= c_curly_open c_block_item_list c_curly_close 
            {
              CompoundStatement RESULT =null;
		Token lr = (Token)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		CompoundStatement i = (CompoundStatement)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		Token rr = (Token)((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
		 RESULT = i;
			i.setRanges( lr, rr ); 
              CUP$parser$result = parser.getSymbolFactory().newSymbol("c_compound_statement",124, ((java_cup.runtime.Symbol)CUP$parser$stack.elementAt(CUP$parser$top-2)), ((java_cup.runtime.Symbol)CUP$parser$stack.peek()), RESULT);
            }
          return CUP$parser$result;

	}
}
