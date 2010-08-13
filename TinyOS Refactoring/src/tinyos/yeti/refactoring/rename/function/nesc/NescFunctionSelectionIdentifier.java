package tinyos.yeti.refactoring.rename.function.nesc;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.CallExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCName;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.rename.SelectionIdentifier;

public class NescFunctionSelectionIdentifier extends SelectionIdentifier {
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] nesCfunctionCallFunctionPartAncestorSequence=new Class[]{
		ParameterizedIdentifier.class,
		NesCName.class,
		CallExpression.class
	};
	
	public NescFunctionSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	public NescFunctionSelectionIdentifier(Identifier identifier, AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	

	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC function like a command or an event.
	 * @param identifier
	 * @return
	 */
	public boolean isNescFunction(Identifier identifier){
		return isNesCFunctionDeclaration()
			||isNesCFunctionDefinition()
			||isNesCFunctionCallFunctionPart();
	}


	/**
	 * Checks if the given identifier is the name identifier of a nesc function declaration in a nesc interface ast.
	 * @return
	 */
	public boolean isNesCFunctionDeclaration(){
		if(!factory4Selection.hasInterfaceAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,interfaceAnalyzer.getNesCFunctionIdentifiers());
	}
	
	/**
	 * Checks if the given identifier is the name identifier of a nesc function definition of a interface implementation in a nesc module ast.
	 * @return
	 */
	public boolean isNesCFunctionDefinition(){
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,moduleAnalyzer.getNesCFunctionImplementationFunctionIdentifiers());
	}

	/**
	 * Checks if the given identifier is the name identifier of a nesc function call.
	 * @return
	 */
	public boolean isNesCFunctionCallFunctionPart() {
		return astUtil.checkAncestorSequence(identifier, nesCfunctionCallFunctionPartAncestorSequence);
	}

	/**
	 * If the selection is a functionCall, use isFunctionCall to check, then this function returns the local interface name, with which the function is associated.
	 * Returns null if the selection is not a function call, or there was no associated interface found.
	 * @return
	 */
	public Identifier getAssociatedInterface2FunctionCall(){
		if(!isNesCFunctionCallFunctionPart()){
			return null;
		}
		NesCName nesCName=astUtil.getParentForName(identifier, NesCName.class);
		return (Identifier)nesCName.getField(NesCName.INTERFACE);
	}

}
