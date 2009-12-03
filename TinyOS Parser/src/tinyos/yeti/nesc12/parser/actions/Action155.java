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
public final class Action155 implements ParserAction{
	public final java_cup.runtime.Symbol do_action(
		int                        CUP$parser$act_num,
		java_cup.runtime.lr_parser CUP$parser$parser,
		java.util.Stack            CUP$parser$stack,
		int                        CUP$parser$top,
		parser                     parser)
		throws java.lang.Exception{
	java_cup.runtime.Symbol CUP$parser$result;
 // n_configuration ::= NK_CONFIGURATION error n_component_parameters n_uses_or_provides_block n_configuration_implementation 
            {
              Configuration RESULT =null;
		Token k = (Token)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-4)).value;
		TemplateParameterList p = (TemplateParameterList)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		AccessList u = (AccessList)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		ConfigurationDeclarationList c = (ConfigurationDeclarationList)((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
		 RESULT = new Configuration(false,parser.errorNode( "configuration", "identifier" ),p,null,u,c); 			RESULT.setLeft( k.getLeft() ); 
              CUP$parser$result = parser.getSymbolFactory().newSymbol("n_configuration",180, ((java_cup.runtime.Symbol)CUP$parser$stack.elementAt(CUP$parser$top-4)), ((java_cup.runtime.Symbol)CUP$parser$stack.peek()), RESULT);
            }
          return CUP$parser$result;

	}
}
