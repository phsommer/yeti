package tinyos.yeti.refactoring.utilities;

import java.util.Collection;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ComponentAstAnalyser;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;

public class AliasSelectionIdentifier {
	
	private Identifier identifier;
	private AstAnalyzerFactory analyzerFactory;
	
	/**
	 * Creates A Selection Identifier for the given identifier.
	 * @param identifier
	 */
	public AliasSelectionIdentifier(Identifier identifier){
		this.identifier=identifier;
		this.analyzerFactory=new AstAnalyzerFactory(identifier);
	}
	
	/**
	 * Creates A Selection Identifier for the given AstAnalyzerFactory.
	 * The factory has to be initialized with the given identifier for this to be of use.
	 * This constructor can be used to avoid multiple analyzing of the same Ast. 
	 * @param identifier
	 */
	public AliasSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory){
		this.identifier=identifier;
		this.analyzerFactory=analyzerFactory;
	}
	
	/**
	 * Returns the identifier, for which this class checks the selection type.
	 * @return
	 */
	public Identifier getSelection(){
		return identifier;
	}
	
	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC alias like a component alias or a interface alias, which are introduces with the "as" keyword.
	 * @param identifier
	 * @return
	 */
	public boolean isAlias(Identifier identifier){
		if(!analyzerFactory.hasComponentAnalyzerCreated()){
			return false;
		}
		return isInterfaceAliasingInSpecification()
			||isComponentAlias();
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC interface alias in the specification of a module/configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAliasingInSpecification(){
		ComponentAstAnalyser analyzer=analyzerFactory.getComponentAnalyzer();
		Identifier alias=analyzer.getAliasIdentifier4InterfaceAliasName(identifier.getName());
		return alias!=null;
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC "components" statement in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAliasingInComponentsStatement(){
		if(!analyzerFactory.hasConfigurationAnalyzerCreated()){
			return false;
		}
		ConfigurationAstAnalyzer analyzer=analyzerFactory.getConfigurationAnalyzer();
		Collection<Identifier> componentAliases=analyzer.getComponentAliasIdentifiers();
		return componentAliases.contains(identifier);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC component wiring in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAliasingInComponentWiring(){
		if(!analyzerFactory.hasConfigurationAnalyzerCreated()){
			return false;
		}
		ConfigurationAstAnalyzer analyzer=analyzerFactory.getConfigurationAnalyzer();
		Collection<Identifier> componentAliasIdentifiers=analyzer.getComponentAliasIdentifiersWithName(identifier.getName());
		return componentAliasIdentifiers.contains(identifier);
	}
	
	/**
	 * Checks if the given identifier is an Alias for a component in the implementation of a nesc configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAlias(){
		return isComponentAliasingInComponentsStatement();
	}

	/**
	 * Checks if the given identifier is an Alias for a interface in the implementation of a nesc module in a call/event statement.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAliasInNescFunction() {
		if(!analyzerFactory.hasModuleAnalyzerCreated()){
			return false;
		}
		//TODO implement ModuleAnalyzer and change appropriate here.
		return true;
	}
}
