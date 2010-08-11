package tinyos.yeti.refactoring.selection;

import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;

public class GlobalFunctionSelectionIdentifier extends SelectionIdentifier {

	private ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions(astUtil);
	
	public GlobalFunctionSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	public GlobalFunctionSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	
	
	/**
	 * Tests if the given identifier is the name of the function, in a function declaration.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function declaration. False otherwise.
	 */
	public boolean isGlobalFunctionDeclaration(){
		if(!factory4Selection.hasCAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,cAnalyzer.getGlobalFunctionDeclarationNames());
	}
	
	/**
	 * Tests if the given identifier is the name of the function, in a function definition.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function definition. False otherwise.
	 */
	public boolean isGlobalFunctionDefinition(){
		if(!factory4Selection.hasCAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,cAnalyzer.getGlobalFunctionDefinitionNames());
	}
	
	
	/**
	 * Tests if the given identifier is the name of the function, in a function call.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function call. False otherwise.
	 */
	public boolean isGlobalFunctionCall(){
		if(!factory4Selection.hasCAnalyzerCreated()){
			return false;
		}
		FunctionDefinition definition=astUtil.getParentForName(identifier, FunctionDefinition.class);
		if(definition==null){
			return false;
		}
		Identifier definitionId=astUtil4Functions.getIdentifierOfFunctionDefinition(definition);
		if(definitionId==null){
			return false;
		}
		if(!astUtil.containsIdentifierInstance(definitionId, cAnalyzer.getGlobalFunctionDefinitionNames())){
			return false;
		}
		return astUtil4Functions.isFunctionCall(identifier);
	}

	/**
	 * Checks if the given identifier represents a global function.
	 * @return
	 */
	public boolean isGlobalFunction() {
		return isGlobalFunctionDeclaration()
			||isGlobalFunctionDefinition()
			||isGlobalFunctionCall();	
	}
	

}
