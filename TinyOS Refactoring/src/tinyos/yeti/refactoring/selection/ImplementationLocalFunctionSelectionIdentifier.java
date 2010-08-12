package tinyos.yeti.refactoring.selection;

import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;

public class ImplementationLocalFunctionSelectionIdentifier extends SelectionIdentifier {

	private ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions(astUtil);
	
	public ImplementationLocalFunctionSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	public ImplementationLocalFunctionSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	
	
	/**
	 * Tests if the given identifier is the name of the function, in a implementation local function declaration.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function declaration. False otherwise.
	 */
	public boolean isImplementationLocalFunctionDeclaration(){
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,moduleAnalyzer.getImplementationLocalFunctionDeclarationNames());
	}
	
	/**
	 * Tests if the given identifier is the name of the function, in a implementation local function definition.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function definition. False otherwise.
	 */
	public boolean isImplementationLocalFunctionDefinition(){
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,moduleAnalyzer.getImplementationLocalFunctionDefinitionNames());
	}
	
	
	/**
	 * Tests if the given identifier is the name of the function, in a implementation local function call.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function call. False otherwise.
	 */
	public boolean isImplementationLocalFunctionCall(){
		if(astUtil.getParentForName(identifier, NesCExternalDefinitionList.class)==null){ //Make sure the functionCall candidate appears in an implementation scope.	
			return false;
		}
		if(!astUtil4Functions.isFunctionCall(identifier)){
			return false;
		}
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		
		return moduleAnalyzer.getImplementationLocalFunctionDefinitionNames().contains(identifier);
	}

	/**
	 * Checks if the given identifier represents a implementation local function.
	 * @return
	 */
	public boolean isImplementationLocalFunction() {
		return isImplementationLocalFunctionDeclaration()
			||isImplementationLocalFunctionDefinition()
			||isImplementationLocalFunctionCall();	
	}
	

}
