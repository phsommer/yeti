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
	 * Creates a component Ast.
	 * @param node An arbitrary node of the ast of which we want a component ast. 
	 */
	public AstType createAnalyzer(ASTNode node){
		TranslationUnit root=ASTUtil.getAstRoot(node);
		boolean valid=false;
		if(ASTUtil4Components.isConfiguration(root)){
			valid=initializeConfigurationComponent(root);
			createdType=AstType.CONFIGURATION;
		}else if(ASTUtil4Components.isModule(root)){
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
		Configuration configuration=ASTUtil.getFirstChildOfType(root, Configuration.class);
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
		Module module=ASTUtil.getFirstChildOfType(root, Module.class);
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
	
	/**
	 * Checks if the created Type is an AstComponentType.
	 * @return
	 */
	public boolean hasComponentCreated(){
		return createdType==AstType.MODULE||createdType==AstType.CONFIGURATION;
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
	
	
}
