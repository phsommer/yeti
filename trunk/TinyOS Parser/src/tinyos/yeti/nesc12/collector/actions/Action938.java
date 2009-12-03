package tinyos.yeti.nesc12.collector.actions;
import tinyos.yeti.nesc12.parser.StringRepository;
import tinyos.yeti.nesc12.parser.ScopeStack;
import tinyos.yeti.nesc12.parser.RawParser;
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
import tinyos.yeti.nesc12.collector.*;
public final class Action938 implements ParserAction{
	public final java_cup.runtime.Symbol do_action(
		int                        CUP$parser$act_num,
		java_cup.runtime.lr_parser CUP$parser$parser,
		java.util.Stack            CUP$parser$stack,
		int                        CUP$parser$top,
		parser                     parser)
		throws java.lang.Exception{
	java_cup.runtime.Symbol CUP$parser$result;
 // c_pointer ::= P_STAR c_type_qualifier_list c_pointer 
            {
              Pointer RESULT =null;
		Token rl = (Token)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		DeclarationSpecifierList t = (DeclarationSpecifierList)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		Pointer p = (Pointer)((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
		 RESULT = new Pointer( t, p ); 
			RESULT.setLeft( rl.getLeft() ); 
              CUP$parser$result = parser.getSymbolFactory().newSymbol("c_pointer",105, ((java_cup.runtime.Symbol)CUP$parser$stack.elementAt(CUP$parser$top-2)), ((java_cup.runtime.Symbol)CUP$parser$stack.peek()), RESULT);
            }
          return CUP$parser$result;

	}
}
