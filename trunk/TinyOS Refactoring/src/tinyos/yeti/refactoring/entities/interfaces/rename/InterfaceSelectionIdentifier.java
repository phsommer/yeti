package tinyos.yeti.refactoring.entities.interfaces.rename;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.abstractrefactoring.rename.SelectionIdentifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ComponentAstAnalyzer;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class InterfaceSelectionIdentifier extends SelectionIdentifier{
	
	private ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions(astUtil);
	
	/**
	 * @see SelectionIdentifier
	 * @param identifier
	 */
	public InterfaceSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	/**
	 * @see SelectionIdentifier
	 * @param identifier
	 */
	public InterfaceSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	
	/**
	 * Checks if the given identifier is part of an AST node associated to an interface.
	 * If the selected Identifier is an alias in a nesc function definition, this function will return false.
	 * @param identifier
	 * @return
	 */
	public boolean isInterface(){
		return isInterfaceDeclaration()
			||isInterfaceDefinition()
			||isInterfaceImplementationAndNoAlias()
			||isComponentWiringInterfacePart()
			||isInterfacePartInNesCFunctionCallAndNoAlias();
	}

	/**
	 * Checks if the given identifier is part of an interface definition,
	 * which is the interface identifier in the file which defines the interface.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceDefinition(){
		if(!factory4Selection.hasInterfaceAnalyzerCreated()){
			return false;
		}
		return interfaceAnalyzer.getEntityIdentifier()==identifier;
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a provide or use statement of a component, which is a module or configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceDeclaration(){
		if(!factory4Selection.hasComponentAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,componentAnalyzer.getReferencedInterfaceIdentifiers());
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a event or call implementation of a module.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceImplementation(){
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,moduleAnalyzer.getNesCFunctionImplementationInterfaceIdentifiers());
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a event or call implementation of a module and also that it is no interface alias.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceImplementationAndNoAlias(){
		if(!isInterfaceImplementation()){
			return false;
		}
		return !isInterfaceAliasInNescFunction();
	}
	
	/**
	 * Checks if the given identifier is the interface part of a nesc function call.
	 * And that the identifier is really the name of a interface and not an local defined alias.
	 * @return
	 */
	public boolean isInterfacePartInNesCFunctionCallAndNoAlias() {
		if(!astUtil4Functions.isInterfacePartInNesCFunctionCall(identifier)){
			return false;
		}
		return !isInterfaceAliasInNescFunctionCall();
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a module/component wiring.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentWiringInterfacePart(){
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,configurationAnalyzer.getWiringInterfacePartIdentifiers());
	}
	
	/**
	 * Checks if the given identifier is an Alias for an interface in the given component.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAlias(){
		return isInterfaceAliasingInSpecification()
		||isInterfaceAliasInNescFunction()
		||isInterfaceAliasInNescFunctionCall();
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC interface alias in the specification of a module/configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAliasingInSpecification(){
		if(!factory4Selection.hasComponentAnalyzerCreated()){
			return false;
		}
		Identifier alias=componentAnalyzer.getAliasIdentifier4InterfaceAliasName(identifier.getName());
		return alias==identifier;
	}

	/**
	 * Checks if the given identifier is an Alias for a interface in the implementation of a nesc module in a call/event statement.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAliasInNescFunction() {
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		Collection<Identifier> identifiers=moduleAnalyzer.getNesCFunctionImplementationInterfaceIdentifiers();
		if(!astUtil.containsIdentifierInstance(identifier,identifiers)){
			return false;
		}
		boolean val=moduleAnalyzer.isDefinedInterfaceAliasName(identifier.getName());
		return val;
	}
	
	/**
	 * Checks if the given identifier is an Alias for a interface in a NesC component wiring in the implementation of a NesC Configuration implementation.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAliasInNescComponentWiring(ProjectUtil util, IProgressMonitor monitor){
		return getDefinitionOfInterfaceAliasInNescComponentWiring(util, monitor)!=null;
	}
	
	/**
	 * Checks if the given identifier is an alias for a interface in a NesC function call.
	 * @return
	 */
	public boolean isInterfaceAliasInNescFunctionCall() {
		if(!astUtil4Functions.isInterfacePartInNesCFunctionCall(identifier)){
			return false;
		}
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return moduleAnalyzer.getAliasIdentifier4InterfaceAliasName(identifier.getName())!=null;
	}
	
	/**
	 * Checks if the given identifier is an Alias for a interface in a NesC component wiring in the implementation of a NesC Configuration.
	 * But: Instead of returning a boolean value, this method returns a AstAnalyzer factory which is initialized for the component ast, which defines the interface alias in its specification.
	 * Returns null, if the identifier is no interface alias in a NesC component wiring or if the defining component is out of project range, or if the selection is a alias defined in this configuration itself. 
	 * @param identifier
	 * @return
	 */
	public AstAnalyzerFactory getDefinitionOfInterfaceAliasInNescComponentWiring(ProjectUtil util,IProgressMonitor monitor){
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			return null;
		}
		if(!astUtil.containsIdentifierInstance(identifier,configurationAnalyzer.getWiringInterfacePartIdentifiers())){
			return null;
		}
		String componentName=configurationAnalyzer.getUseDefiningComponent4InterfaceInWiring(identifier);
		try {
			IDeclaration sourceDefinition=util.getComponentDefinition(componentName);
			if(sourceDefinition==null){
				return null;
			}
			IFile declaringFile=util.getDeclaringFile(sourceDefinition);
			if(declaringFile==null||!util.isProjectFile(declaringFile)){	//If the source definition is not in this project, we are not allowed/able to rename the alias.
				return null;
			}
			AstAnalyzerFactory factory4DefiningAst=new AstAnalyzerFactory(declaringFile,util,monitor);
			if(!factory4DefiningAst.hasComponentAnalyzerCreated()){
				return null;
			}
			ComponentAstAnalyzer componentAnalyzer=factory4DefiningAst.getComponentAnalyzer();
			Identifier aliasDefinition=componentAnalyzer.getAliasIdentifier4InterfaceAliasName(identifier.getName());
			if(aliasDefinition==null){
				return null;
			}
			return factory4DefiningAst;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

}
