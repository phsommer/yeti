package tinyos.yeti.refactoring.selection;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ComponentAstAnalyzer;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class AliasSelectionIdentifier extends SelectionIdentifier{
	
	
	/**
	 * @see SelectionIdentifier(Identifier identifier)
	 * @param identifier
	 */
	public AliasSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}
	
	/**
	 * @see SelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory)
	 */
	public AliasSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory){
		super(identifier,analyzerFactory);
	}

	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC alias like a component alias or a interface alias, which are introduces with the "as" keyword.
	 * @param identifier
	 * @return
	 */
	public boolean isAlias(){
		if(!factory4Selection.hasComponentAnalyzerCreated()){
			return false;
		}
		return isComponentAlias()
			||isInterfaceAlias();
	}
	
	/**
	 * Checks if the given identifier is an Alias for a component in the implementation of a nesc configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAlias(){
		return isComponentAliasingInComponentsStatement()
			||isComponentAliasingInComponentWiring();
	}
	
	/**
	 * Checks if the given identifier is an Alias for an interface in the given component.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAlias(){
		return isInterfaceAliasingInSpecification()
		||isInterfaceAliasInNescFunction();
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC "components" statement in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAliasingInComponentsStatement(){
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			return false;
		}
		Collection<Identifier> componentAliases=configurationAnalyzer.getComponentAliasIdentifiers();
		return componentAliases.contains(identifier);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC component wiring in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAliasingInComponentWiring(){
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			return false;
		}
		Collection<Identifier> componentWirings=configurationAnalyzer.getWiringComponentPartIdentifiers();
		if(!astUtil.containsIdentifierInstance(identifier,componentWirings)){
			return false;
		}
		Collection<Identifier> componentAliases=configurationAnalyzer.getComponentAliasIdentifiers();
		return componentAliases.contains(identifier);

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





















