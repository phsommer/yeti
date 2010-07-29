package tinyos.yeti.refactoring.selection;

import java.util.Collection;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ComponentAstAnalyser;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;
import tinyos.yeti.refactoring.ast.ModuleAstAnalyzer;

public class SelectionIdentifier {

	protected Identifier identifier;
	protected AstAnalyzerFactory analyzerFactory;
	protected ComponentAstAnalyser componentAnalyzer;
	protected ConfigurationAstAnalyzer configurationAnalyzer;
	protected ModuleAstAnalyzer moduleAnalyzer;
	
	/**
	 * Creates A Selection Identifier for the given identifier.
	 * Is convenience constructor for the call new SelectionIdentifier(identifier,new AstAnalyzerFactory(identifier) )
	 * @param identifier
	 */
	protected SelectionIdentifier(Identifier identifier){
		this(identifier,new AstAnalyzerFactory(identifier));
	}
	
	/**
	 * Creates A Selection Identifier for the given AstAnalyzerFactory.
	 * The factory has to be initialized with the given identifier for this to be of use.
	 * This constructor can be used to avoid multiple analyzing of the same Ast. 
	 * @param identifier
	 */
	protected SelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory){
		this.identifier=identifier;
		this.analyzerFactory=analyzerFactory;
		if(analyzerFactory.hasComponentAnalyzerCreated()){
			componentAnalyzer=analyzerFactory.getComponentAnalyzer();
		}
		if(analyzerFactory.hasConfigurationAnalyzerCreated()){
			configurationAnalyzer=analyzerFactory.getConfigurationAnalyzer();
		}
		if(analyzerFactory.hasModuleAnalyzerCreated()){
			moduleAnalyzer=analyzerFactory.getModuleAnalyzer();
		}
	}
	
	/**
	 * Returns the identifier, for which this class checks the selection type.
	 * @return
	 */
	public Identifier getSelection(){
		return identifier;
	}
	
	/**
	 * Checks if the given identifier instance is part of the given collection.
	 * @return
	 */
	protected boolean containsIdentifierInstance(Collection<Identifier> identifiers){
		for(Identifier identifier:identifiers){
			if(identifier==this.identifier){
				return true;
			}
		}
		return false;
	}
	
}
