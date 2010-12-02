package tinyos.yeti.refactoring.entities.function.rename;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.FunctionCall;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.refactoring.abstractrefactoring.rename.SelectionIdentifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;

public class FunctionSelectionIdentifier extends SelectionIdentifier {

	private ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions(astUtil);
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionCallAncestorSequence=new Class[]{
		IdentifierExpression.class,
		FunctionCall.class
	};
	
	public FunctionSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	public FunctionSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	
	/**
	 * Tests if the given identifier is the name of a function, in a function call.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function call. False otherwise.
	 */
	public boolean isFunctionCall(Identifier identifier){
		return astUtil.checkAncestorSequence(identifier,functionCallAncestorSequence);	
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
		return isFunctionCall(identifier);
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
		if(!isFunctionCall(identifier)){
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
