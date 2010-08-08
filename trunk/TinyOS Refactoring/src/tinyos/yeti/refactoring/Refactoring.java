package tinyos.yeti.refactoring;

public enum Refactoring {
	
	RENAME_LOCAL_VARIABLE(
			"renameLocalVariable",
			"local variable"),
			
	RENAME_GLOBAL_VARIABLE(
			"renameGlobalVariable",
			"global variable"),
			
	RENAME_IMPLEMENTATION_LOCAL_VARIABLE(
			"renameImplementationLocalVariable",
			"implementation local variable"),
			
	RENAME_LOCAL_FUNCTION(
			"renameLocalFunction",
			"local function"),
			
	RENAME_GLOBAL_FUNCTION(
			"renameGlobalFunction",
			"global function"),
			
	RENAME_INTERFACE(
			"renameInterface",
			"interface"),
			
	RENAME_COMPONENT(
			"renameComponent",
			"component"),
			
	RENAME_COMPONENT_ALIAS(
			"renameComponentAlias",
			"component alias"),
			
	RENAME_INTERFACE_ALIAS(
			"renameInterfaceAlias",
			"interface alias"),
			
	RENAME_NESC_FUNCTION(
			"renameNescFunction",
			"nesc function"),
			
	EXTRACT_FUNCTION(
			"extractFunction",
			"function"),
			
	NO_REFACTORING_AVAILABLE(
			"NoRefactoringAvailable",
			"none");
	
	/**
	 * The name which is used in the plugin xml for the property
	 */
	private String propertyName;
	
	/**
	 * The entity which is edited with this refactoring. Is used for user output.
	 */
	private String entityName;
	private Refactoring(String propertyName,String entityName ){
		this.propertyName=propertyName;
		this.entityName=entityName;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public String getEntityName() {
		return entityName;
	}
	
	/**
	 * Returns the Refactoring enum constant, which has as property string the given string.
	 * Returns null, if there is no refactoring with the given string.
	 * @param property
	 * @return
	 */
	public static Refactoring getRefactoring4Property(String property){
		for(Refactoring ref:Refactoring.values()){
			if(property.equals(ref.getPropertyName())){
				return ref;
			}
		}
		return null;
	}
	
	
};
