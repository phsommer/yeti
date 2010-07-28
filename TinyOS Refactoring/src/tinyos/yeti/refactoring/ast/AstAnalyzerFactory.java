package tinyos.yeti.refactoring.ast;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Configuration;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Components;

public class AstAnalyzerFactory {
	
	private ASTUtil astUtil=new ASTUtil();

	public static enum AstType{
		MODULE,
		CONFIGURATION,
		INTERFACE,
		INVALID
	}
	
	private AstType createdType=AstType.INVALID;
	private ConfigurationAstAnalyzer configurationAnalyzer;
	private ModuleAstAnalyzer moduleAnalyzer;
	
	/**
	 * Creates a new AstAnalyzerFactory.
	 * To be of use the createAnalyzer method has first to be called.
	 */
	public AstAnalyzerFactory(){}
	
	/**
	 * The same as calling the empty constructor and afterwards createAnalyzer.
	 * @param node
	 */
	public AstAnalyzerFactory(ASTNode node){
		createAnalyzer(node);
	}
	
	/**
	 * Tries to create an AstAnalyzer.
	 * @param node
	 * @return The AstType for which an specific AstAnalyzer has been created. Returns AstType.INVALID if not an valid ast is given.
	 */
	public AstType createAnalyzer(ASTNode node){
		ASTUtil4Components astUtil4Components=new ASTUtil4Components(astUtil);
		TranslationUnit root=astUtil.getAstRoot(node);
		boolean valid=false;
		if(astUtil4Components.isConfiguration(root)){
			valid=initializeConfigurationComponent(root);
			createdType=AstType.CONFIGURATION;
		}else if(astUtil4Components.isModule(root)){
			valid=initializeModuleComponent(root);
			createdType=AstType.MODULE;
		}
		if(!valid){
			createdType=AstType.INVALID;
		}
		return createdType;
	}
	
	/**
	 * Initializes this class for the case that the root node is part of a NesC Configuration Ast.
	 * @return true if initialization was possible, false if it failed.
	 */
	private boolean initializeConfigurationComponent(TranslationUnit root){
		Configuration configuration=astUtil.getFirstChildOfType(root, Configuration.class);
		if(configuration==null){
			return false;
		}
		Identifier componentIdentifier=(Identifier)configuration.getField(Configuration.NAME);
		AccessList specification=(AccessList)configuration.getField(Configuration.CONNECTIONS);
		ConfigurationDeclarationList implementation=(ConfigurationDeclarationList)configuration.getField(Configuration.IMPLEMENTATION);
		if(componentIdentifier==null||specification==null||implementation==null){
			return false;
		}
		configurationAnalyzer=new ConfigurationAstAnalyzer(root, componentIdentifier, specification, implementation);
		return true;
		
	}
	
	/**
	 * Initializes this class for the case that the root node is part of a NesC Configuration Ast.
	 * @return true if initialization was possible, false if it failed.
	 */
	private boolean initializeModuleComponent(TranslationUnit root){
		Module module=astUtil.getFirstChildOfType(root, Module.class);
		if(module==null){
			return false;
		}
		Identifier componentIdentifier=(Identifier)module.getField(Module.NAME);
		AccessList specification=(AccessList)module.getField(Module.CONNECTIONS);
		NesCExternalDefinitionList implementation=(NesCExternalDefinitionList)module.getField(Module.IMPLEMENTATION);
		if(componentIdentifier==null||specification==null||implementation==null){
			return false;
		}
		moduleAnalyzer=new ModuleAstAnalyzer(root, componentIdentifier, specification, implementation);
		return true;
	}

	public AstType getCreatedType() {
		return createdType;
	}

	public ConfigurationAstAnalyzer getConfigurationAnalyzer() {
		if(createdType==AstType.CONFIGURATION){
			return configurationAnalyzer;
		}else{
			throw new IllegalStateException("No Configuration has been created!");
		}
	}

	public ModuleAstAnalyzer getModuleAnalyzer() {
		if(createdType==AstType.MODULE){
			return moduleAnalyzer;
		}else{
			throw new IllegalStateException("No Module has been created!");
		}
	}
	
	public ComponentAstAnalyser getComponentAnalyzer() {
		if(createdType==AstType.MODULE){
			return moduleAnalyzer;
		}else if(createdType==AstType.CONFIGURATION){
			return configurationAnalyzer;
		}else{
			throw new IllegalStateException("No Module has been created!");
		}
	}

	/**
	 * Checks if this factory created a ConfigurationAstAnalyzer.
	 * @return
	 */
	public boolean hasConfigurationAnalyzerCreated() {
		return createdType==AstType.CONFIGURATION;
	}
	
	/**
	 * Checks if this factory created a ModuleAstAnalyzer.
	 * @return
	 */
	public boolean hasModuleAnalyzerCreated() {
		return createdType==AstType.MODULE;
	}
	
	/**
	 * Checks if this factory created a ComponentAstAnalyzer.
	 * @return
	 */
	public boolean hasComponentAnalyzerCreated(){
		return createdType==AstType.MODULE||createdType==AstType.CONFIGURATION;
	}
	
	
}
