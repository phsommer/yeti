package tinyos.yeti.refactoring;


public enum Refactoring {
	
	RENAME_LOCAL_VARIABLE(
			"renameLocalVariable",
			"local variable",
			new tinyos.yeti.refactoring.entities.variable.rename.local.AvailabilityTester()),

	RENAME_FUNCTION_PARAMETER(
			"renameFunctionParameter",
			"function parameter",
			new tinyos.yeti.refactoring.rename.functionparameter.AvailabilityTester()),

	RENAME_GLOBAL_VARIABLE(
			"renameGlobalVariable",
			"global variable",
			new tinyos.yeti.refactoring.entities.variable.rename.global.GlobalVariableAvailabilityTester()),
			
	RENAME_IMPLEMENTATION_LOCAL_VARIABLE(
			"renameImplementationLocalVariable",
			"implementation local variable",
			new tinyos.yeti.refactoring.entities.variable.rename.implementation.AvailabilityTester()),
			
	RENAME_LOCAL_FUNCTION(
			"renameLocalFunction",
			"local function",
			new tinyos.yeti.refactoring.rename.function.local.AvailabilityTester()),
			
	RENAME_GLOBAL_FUNCTION(
			"renameGlobalFunction",
			"global function",
			new tinyos.yeti.refactoring.rename.function.global.GlobalFunctionAvailabilityTester()),
			
	RENAME_INTERFACE(
			"renameInterface",
			"interface",
			new tinyos.yeti.refactoring.rename.interfaces.AvailabilityTester()),
			
	RENAME_COMPONENT(
			"renameComponent",
			"component",
			new tinyos.yeti.refactoring.entities.component.rename.AvailabilityTester()),
			
	RENAME_COMPONENT_ALIAS(
			"renameComponentAlias",
			"component alias",
			new tinyos.yeti.refactoring.entities.component.alias.rename.AvailabilityTester()),
			
	RENAME_INTERFACE_ALIAS(
			"renameInterfaceAlias",
			"interface alias",
			new tinyos.yeti.refactoring.rename.alias.interfaces.AvailabilityTester()),
			
	RENAME_NESC_FUNCTION(
			"renameNescFunction",
			"nesc function",
			new tinyos.yeti.refactoring.rename.function.nesc.AvailabilityTester()),
			
	EXTRACT_FUNCTION(
			"extractFunction",
			"function",
			new tinyos.yeti.refactoring.extractFunction.AvailabilityTester()),
			
	NO_REFACTORING_AVAILABLE(
			"NoRefactoringAvailable",
			"none",
			new tinyos.yeti.refactoring.entities.notavailable.AvailabilityTester());
	

	private String propertyName;
	private String entityName;
	private IRefactoringAvailabilityTester tester;
	
	private Refactoring(String propertyName,String entityName,IRefactoringAvailabilityTester tester){
		this.propertyName=propertyName;
		this.entityName=entityName;
		this.tester=tester;
	}
	
	/**
	 * The name which is used in the plugin.xml for the property
	 */
	public String getPropertyName() {
		return propertyName;
	}
	
	/**
	 * The entity which is edited with this refactoring. Is used for user output.
	 */
	public String getEntityName() {
		return entityName;
	}
	
	/**
	 * The tester for testing, if this refactoring is available.
	 * @return
	 */
	public IRefactoringAvailabilityTester getTester(){
		return tester;
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
