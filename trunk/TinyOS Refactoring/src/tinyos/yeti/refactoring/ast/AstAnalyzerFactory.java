package tinyos.yeti.refactoring.ast;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Configuration;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.DatadefList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Interface;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class AstAnalyzerFactory {
	
	private ASTUtil astUtil=new ASTUtil();

	public static enum AstType{
		MODULE,
		CONFIGURATION,
		INTERFACE,
		NESCENTITY,
		INVALID
	}
	
	private AstType createdType=AstType.INVALID;
	private ConfigurationAstAnalyzer configurationAnalyzer;
	private ModuleAstAnalyzer moduleAnalyzer;
	private InterfaceAstAnalyzer interfaceAnalyzer;
	
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
	 * Tries to create an AstAnalyzerFactory for the ast of the given file.
	 * @param file
	 * @param monitor
	 * @throws IOException
	 * @throws MissingNatureException
	 */
	public AstAnalyzerFactory(IFile file, ProjectUtil util, IProgressMonitor monitor) throws IOException, MissingNatureException{
		NesC12AST ast=util.getAst(file, monitor);
		createAnalyzer(ast.getRoot());
	}
	
	/**
	 * Tries to create an AstAnalyzer.
	 * @param node
	 * @return The AstType for which an specific AstAnalyzer has been created. Returns AstType.INVALID if not an valid ast is given.
	 */
	public AstType createAnalyzer(ASTNode node){
		TranslationUnit root=astUtil.getAstRoot(node);
		boolean valid=false;
		if(isConfiguration(root)){
			valid=initializeConfigurationAnalyzer(root);
			createdType=AstType.CONFIGURATION;
		}else if(isModule(root)){
			valid=initializeModuleAnalyzer(root);
			createdType=AstType.MODULE;
		}else if(isInterface(node)){
			valid=initializeInterfaceAnalyzer(root);
			createdType=AstType.INTERFACE;
		}
		if(!valid){
			createdType=AstType.INVALID;
		}
		return createdType;
	}
	
	/**
	 * Initializes this class for the case that the root node is part of a NesC Interface Ast.
	 * @return true if initialization was possible, false if it failed.
	 */
	private boolean initializeInterfaceAnalyzer(TranslationUnit root) {
		Interface interfac=astUtil.getFirstChildOfType(root, Interface.class);
		if(interfac==null){
			return false;
		}
		Identifier interfacIdentifier=(Identifier)interfac.getField(Interface.NAME);
		DatadefList body=(DatadefList)interfac.getField(Interface.BODY);
		if(interfacIdentifier==null||body==null){
			return false;
		}
		interfaceAnalyzer=new InterfaceAstAnalyzer(root, interfacIdentifier, body);
		return true;
	}

	/**
	 * Initializes this class for the case that the root node is part of a NesC Configuration Ast.
	 * @return true if initialization was possible, false if it failed.
	 */
	private boolean initializeConfigurationAnalyzer(TranslationUnit root){
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
	private boolean initializeModuleAnalyzer(TranslationUnit root){
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

	/**
	 * Returns the AstType for which this factory created an Analyzer.
	 * @return
	 */
	public AstType getCreatedType() {
		return createdType;
	}

	/**
	 * Returns the ConfigurationAstAnalyzer which this factory has created.
	 * Throws IllegalStateException if this factory did not create an ConfigurationAstAnalyzer analyzer.
	 * Check first with the hasConfigurationAnalyzerCreated method.
	 * @return
	 */
	public ConfigurationAstAnalyzer getConfigurationAnalyzer() {
		if(createdType==AstType.CONFIGURATION){
			return configurationAnalyzer;
		}else{
			throw new IllegalStateException("No configuration analyzer has been created!");
		}
	}

	/**
	 * Same as getConfigurationAnalyzer() but for NesC Modules.
	 * @return
	 */
	public ModuleAstAnalyzer getModuleAnalyzer() {
		if(createdType==AstType.MODULE){
			return moduleAnalyzer;
		}else{
			throw new IllegalStateException("No module analyzer has been created!");
		}
	}
	
	/**
	 * Same as getConfigurationAnalyzer() but for NesC Interfaces.
	 * @return
	 */
	public InterfaceAstAnalyzer getInterfaceAnalyzer() {
		if(createdType==AstType.INTERFACE){
			return interfaceAnalyzer;
		}else{
			throw new IllegalStateException("No interface analyzer has been created!");
		}
	}
	
	/**
	 * Same as getConfigurationAnalyzer() but for NesC Components.
	 * @return
	 */
	public ComponentAstAnalyser getComponentAnalyzer() {
		if(createdType==AstType.MODULE){
			return moduleAnalyzer;
		}else if(createdType==AstType.CONFIGURATION){
			return configurationAnalyzer;
		}else{
			throw new IllegalStateException("No component has been created!");
		}
	}
	
	/**
	 * Same as getConfigurationAnalyzer() but for NesC Entities.
	 * @return
	 */
	public NesCAstAnalyzer getNesCAnalyzer() {
		if(createdType==AstType.MODULE){
			return moduleAnalyzer;
		}else if(createdType==AstType.CONFIGURATION){
			return configurationAnalyzer;
		}else if(createdType==AstType.INTERFACE){
			return interfaceAnalyzer;
		}else{
			throw new IllegalStateException("No nesc ast analyzer has been created!");
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
	 * Checks if this factory created a InterfaceAstAnalyzer.
	 * @return
	 */
	public boolean hasInterfaceAnalyzerCreated() {
		return createdType==AstType.INTERFACE;
	}
	
	/**
	 * Checks if this factory created a ComponentAstAnalyzer.
	 * @return
	 */
	public boolean hasComponentAnalyzerCreated(){
		return createdType==AstType.MODULE||createdType==AstType.CONFIGURATION;
	}
	
	/**
	 * Checks if this factory created a NesCAstAnalyzer.
	 * A NesCAnalyzer can be an AstAnalyzer for a NesC component or a NesC interface.
	 * @return
	 */
	public boolean hasNesCAnalyzerCreated(){
		return hasComponentAnalyzerCreated()||createdType==AstType.INTERFACE;
	}
	
	/**
	 * Returns the "Module" node of the AST which includes the given node.Returns null if the node is not in an ast with a module node, which means this is with verry high probability no module file.
	 * @param node
	 * @return
	 */
	private Module getModuleNode(ASTNode node){
		ASTNode root=astUtil.getAstRoot(node);
		Collection<Module> modules=astUtil.getChildsOfType(root,Module.class);
		if(modules.size()!=1){
			return null;
		}
		return modules.iterator().next();
	}
	
	/**
	 * Same as getModuleNode, but for Configuration.
	 * @param node
	 * @return
	 */
	private Configuration getConfigurationNode(ASTNode node){
		ASTNode root=astUtil.getAstRoot(node);
		Collection<Configuration> configuration=astUtil.getChildsOfType(root,Configuration.class);
		if(configuration.size()!=1){
			return null;
		}
		return configuration.iterator().next();
	}
	
	/**
	 * Same as getModuleNode, but for Interface.
	 * @param node
	 * @return
	 */
	private Interface getInterfaceNode(ASTNode node){
		ASTNode root=astUtil.getAstRoot(node);
		Collection<Interface> interfaces=astUtil.getChildsOfType(root,Interface.class);
		if(interfaces.size()!=1){
			return null;
		}
		return interfaces.iterator().next();
	}
	
	/**
	 * Checks if the given node is part of a module ast.
	 * @param node
	 * @return
	 */
	private boolean isModule(ASTNode node){
		return getModuleNode(node)!=null;
	}
	
	/**
	 * Checks if the given node is part of a configuration ast.
	 * @param node
	 * @return
	 */
	private boolean isConfiguration(ASTNode node){
		return getConfigurationNode(node)!=null;
	}
	
	/**
	 * Checks if the given node is part of a interface ast.
	 * @param node
	 * @return
	 */
	private boolean isInterface(ASTNode node){
		return getInterfaceNode(node)!=null;
	}
	
	
}
