package tinyos.yeti.refactoring.selection;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;

public class ImplementationLocalVariableSelectionIdentifier extends SelectionIdentifier {

	private ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables(astUtil);
	
	public ImplementationLocalVariableSelectionIdentifier(Identifier identifier, AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}

	public ImplementationLocalVariableSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}
	
	/**
	 * Checks if this identifier is part of a variable declaration.
	 * @param identifier
	 * @return
	 */
	public boolean isImplementationLocalVariableDeclaration(){
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,moduleAnalyzer.getImplementationLocalVariableDeclarationNames());
	}
	
	/**
	 * Checks if this identifier is part of a variable reference in a function body.
	 * @param identifier
	 * @return
	 */
	public boolean isImplementationLocalVariableUsage(){
		if(!astUtil4Variables.isVariableUsage(identifier)){
			return false;
		}
		if(astUtil4Variables.isLocalVariable(identifier)){
			return false;
		}
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return moduleAnalyzer.getImplementationLocalVariableDeclarationNames().contains(identifier);
	}
	
	/**
	 * Checks if the given identifier represents a implementation local variable.
	 * @return
	 */
	public boolean isImplementationLocalVariable(){
		return isImplementationLocalVariableDeclaration()
			||isImplementationLocalVariableUsage();
	}
	

}
