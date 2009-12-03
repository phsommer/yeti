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
public final class Action530 implements ParserAction{
	public final java_cup.runtime.Symbol do_action(
		int                        CUP$parser$act_num,
		java_cup.runtime.lr_parser CUP$parser$parser,
		java.util.Stack            CUP$parser$stack,
		int                        CUP$parser$top,
		parser                     parser)
		throws java.lang.Exception{
	java_cup.runtime.Symbol CUP$parser$result;
 // n_interface_type ::= NK_INTERFACE c_identifier c_arrow_open error c_arrow_close 
            {
              InterfaceType RESULT =null;
		Token rl = (Token)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-4)).value;
		Identifier i = (Identifier)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-3)).value;
		Token rr = (Token)((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
		 RESULT =  new InterfaceType( i, parser.errorNode( "interface", "type list" ) ); 
			RESULT.setRanges( rl, rr ); 
              CUP$parser$result = parser.getSymbolFactory().newSymbol("n_interface_type",171, ((java_cup.runtime.Symbol)CUP$parser$stack.elementAt(CUP$parser$top-4)), ((java_cup.runtime.Symbol)CUP$parser$stack.peek()), RESULT);
            }
          return CUP$parser$result;

	}
}
