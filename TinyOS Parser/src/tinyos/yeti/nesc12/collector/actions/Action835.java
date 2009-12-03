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
public final class Action835 implements ParserAction{
	public final java_cup.runtime.Symbol do_action(
		int                        CUP$parser$act_num,
		java_cup.runtime.lr_parser CUP$parser$parser,
		java.util.Stack            CUP$parser$stack,
		int                        CUP$parser$top,
		parser                     parser)
		throws java.lang.Exception{
	java_cup.runtime.Symbol CUP$parser$result;
 // c_enum_specifier ::= K_ENUM c_all_identifier n_attributes c_curly_open c_curly_close 
            {
              TypeSpecifier RESULT =null;
		Token rl = (Token)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-4)).value;
		Identifier i = (Identifier)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-3)).value;
		AttributeList a = (AttributeList)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		Token rr = (Token)((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
		 EnumDeclaration declaration = new EnumDeclaration( i, a, new EnumConstantList() ); 			RESULT = declaration; 			declaration.setRanges( rl, rr ); 
              CUP$parser$result = parser.getSymbolFactory().newSymbol("c_enum_specifier",95, ((java_cup.runtime.Symbol)CUP$parser$stack.elementAt(CUP$parser$top-4)), ((java_cup.runtime.Symbol)CUP$parser$stack.peek()), RESULT);
            }
          return CUP$parser$result;

	}
}
