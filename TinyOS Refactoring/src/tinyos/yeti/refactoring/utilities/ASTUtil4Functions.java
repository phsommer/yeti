package tinyos.yeti.refactoring.utilities;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclaratorList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.NesCNameDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParameterDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParameterTypeList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.PointerDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.PrimitiveSpecifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypedefName;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.CallExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCName;

public class ASTUtil4Functions {
	
	private ASTUtil astUtil;
	
	public ASTUtil4Functions(){
		this(new ASTUtil());
	}
	
	public ASTUtil4Functions(ASTUtil astUtil) {
		super();
		this.astUtil = astUtil;
	}
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDefinitionIdentifierSuccessorSequence=new Class[]{
		FunctionDefinition.class,
		FunctionDeclarator.class,
		DeclaratorName.class,
		Identifier.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDefinitionIdentifierSuccessorSequenceWithPointer=new Class[]{
		FunctionDefinition.class,
		PointerDeclarator.class,
		FunctionDeclarator.class,
		DeclaratorName.class,
		Identifier.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] declarationIdentifierSuccessorSequence=new Class[]{
		Declaration.class,
		InitDeclaratorList.class,
		InitDeclarator.class,
		FunctionDeclarator.class,
		DeclaratorName.class,
		Identifier.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] nesCfunctionCallInterfacePartAncestorSequence=new Class[]{
		NesCName.class,
		CallExpression.class
	};
	
	/**
	 * Returns the identifier which is part of the local function definition, if the 
	 * given identifier is part of the same function. I.e. the given identifier is
	 * a function call of the function definition.
	 * @param identifier
	 * @return Null if the given identifier is not part of a local function.
	 */
	public Identifier getLocalFunctionDefinitionIdentifier(Identifier identifier){
		String targetName=identifier.getName();
		//Get the root node for the local implementation of this module.
		ASTNode root=astUtil.getModuleImplementationNodeIfInside(identifier);
		//Test if the identifier is located inside a implementation.
		if(root==null){
			return null;
		}
		//Try to find the functionDefinition with the name of the identifier.
		for(FunctionDefinition definition:astUtil.getChildsOfType(root, FunctionDefinition.class)){
			Identifier id=getIdentifierOfFunctionDefinition(definition);
			if(id!=null&&targetName.equals(id.getName())){
				return id;
			}
		}
		return null;
	}
	
	/**
	 * Returns the identifier which is part of the local function declaration, if the 
	 * given identifier is part of the same function. I.e. the given identifier is
	 * a function call of the function declaration.
	 * @param identifier
	 * @return Null if the given identifier is not part of a local function.
	 */
	public Identifier getLocalFunctionDeclarationIdentifier(Identifier identifier){
		String targetName=identifier.getName();
		//Get the root node for the local implementation of this module.
		ASTNode root=astUtil.getModuleImplementationNodeIfInside(identifier);
		//Test if the identifier is located inside a implementation.
		if(root==null){
			return null;
		}
		//Try to find the functionDeclaration with the name of the identifier.
		for(Declaration declaration:astUtil.getChildsOfType(root, Declaration.class)){
			Identifier id=getIdentifierOfFunctionDeclaration(declaration);
			if(id!=null&&targetName.equals(id.getName())){
				return id;
			}
		}
		return null;
	}
	
	/**
	 * Pulls the identifier out of a functionDefinition.
	 * @param definition
	 * @return	
	 */
	public Identifier getIdentifierOfFunctionDefinition(FunctionDefinition definition){
		Identifier defId=(Identifier)astUtil.checkSuccessorSequence(definition, functionDefinitionIdentifierSuccessorSequence);
		if(defId==null){
			defId=(Identifier)astUtil.checkSuccessorSequence(definition, functionDefinitionIdentifierSuccessorSequenceWithPointer);
		}
		return defId;
	}
	
	/**
	 * Returns the associated function name identifier of a FunctionDeclarator.
	 * @param functionDeclarator
	 * @return
	 */
	public Identifier getIdentifierOfFunctionDeclaration(FunctionDeclarator functionDeclarator) {
		ASTNode node=functionDeclarator.getField(FunctionDeclarator.DECLARATOR);
		if(node instanceof DeclaratorName){
			DeclaratorName decName=(DeclaratorName)node;
			return decName.getName();
		}if(node instanceof NesCNameDeclarator){
			NesCNameDeclarator decName=(NesCNameDeclarator)node;
			return (Identifier)decName.getField(NesCNameDeclarator.FUNCTION_NAME);
		}
		return null;
	}
	
	/**
	 * Pulls the identifier out of a declaration, if this declaration declares a function.
	 * @param definition
	 * @return	The identifier of the functionDeclaration, null if the given declaration doesn't contain a function declaration.
	 */
	public Identifier getIdentifierOfFunctionDeclaration(Declaration declaration){
		return (Identifier)astUtil.checkSuccessorSequence(declaration, declarationIdentifierSuccessorSequence);
	}
	
	/**
	 * Returns the FunctionDefinition of which the given identifier is part.
	 * @param id
	 * @return The ancestor FunctionDefinition. Null if this id is not part of a FunctionDefinition.
	 */
	public FunctionDefinition identifierToFunctionDefinition(Identifier id){
		FunctionDefinition definition=astUtil.getParentForName(id, FunctionDefinition.class);
		if(definition==null){
			return null;
		}
		Identifier definitionIdentifier=getIdentifierOfFunctionDefinition(definition);
		if(id==definitionIdentifier){
			return definition;
		}
		return null;
	}
	
	/**
	 * Returns the identifier of the parameter with the given index, null if there is no parameter with the given index.
	 * @param index
	 * @param declarator
	 * @return
	 */
	public Identifier getIdentifierOfParameterWithIndex(int index,FunctionDeclarator declarator){
		ParameterDeclaration paramDeclaration=getParameterDeclarationWithIndex(index, declarator);
		if(paramDeclaration==null){
			return null;
		}
		return getParameterName(paramDeclaration);

	}
	
	/**
	 * Returns the type identifier of the parameter with the given index, null if there is no parameter with the given index.
	 * @param index
	 * @param declarator
	 * @return
	 */
	public String getTypeNameOfParameterWithIndex(int index,FunctionDeclarator declarator){
		ParameterDeclaration paramDeclaration=getParameterDeclarationWithIndex(index, declarator);
		if(paramDeclaration==null){
			return null;
		}
		DeclarationSpecifierList specifiers=(DeclarationSpecifierList)paramDeclaration.getField(ParameterDeclaration.SPECIFIERS);
		if(specifiers==null){
			return null;
		}
		TypedefName typedefName=astUtil.getFirstChildOfType(specifiers, TypedefName.class);
		if(typedefName!=null){
			Identifier id= (Identifier)typedefName.getField(TypedefName.NAME);
			if(id!=null){
				return id.getName();
			}
		}
		PrimitiveSpecifier primitiveSpecifier=astUtil.getFirstChildOfType(specifiers, PrimitiveSpecifier.class);
		if(primitiveSpecifier!=null){
			return primitiveSpecifier.getType().name();
		}
		return null;
	}
	
	/**
	 * Pulls out the name identifier of the given ParameterDeclaration.
	 * Is needed since PointerDeclarators may sit between the ParameterDeclaration and the DeclaratorName.
	 * @param declaration
	 * @return
	 */
	public Identifier getParameterName(ParameterDeclaration declaration){
		DeclaratorName declaratorName=null;
		ASTNode node=declaration.getField(ParameterDeclaration.DECLARATOR);
		if(node instanceof DeclaratorName){
			declaratorName=(DeclaratorName)node;
		}else if(node instanceof PointerDeclarator){
			PointerDeclarator pointerDeclarator=(PointerDeclarator)node;
			declaratorName=(DeclaratorName)pointerDeclarator.getField(PointerDeclarator.DECLARATOR);
		}
		if(declaratorName!=null){
			return (Identifier)declaratorName.getField(DeclaratorName.NAME);
		}
		
		return null;
	} 
	
	/**
	 * Returns the ParameterDeclaration with the given name, null if there is no ParameterDeclaration with this index.
	 * @param index
	 * @param declarator
	 * @return
	 */
	public ParameterDeclaration getParameterDeclarationWithIndex(int index,FunctionDeclarator declarator){
		ParameterTypeList parameterList=(ParameterTypeList)declarator.getField(FunctionDeclarator.PARAMETERS);
		if(parameterList==null){
			return null;
		}
		parameterList.getTypedChild(index);
		return parameterList.getTypedChild(index);
	}
	
	/**
	 * Returns the index of the ParameterDeclaration with the given name, null if there is no ParameterDeclaration with this name.
	 * @param name
	 * @param declarator
	 * @return
	 */
	public Integer getIndexOfParameterWithName(String name,FunctionDeclarator declarator){
		ParameterTypeList parameterList=(ParameterTypeList)declarator.getField(FunctionDeclarator.PARAMETERS);
		if(parameterList==null){
			return null;
		}
		for(int i=0;i<parameterList.getChildrenCount();++i){
			ParameterDeclaration declaration=parameterList.getTypedChild(i);
			if(declaration!=null){
				Identifier identifier=getParameterName(declaration);
				if(identifier!=null){
					if(name.equals(identifier.getName())){
						return i;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the associated FunctionDeclarator for a FunctionDefinition.
	 * @param definition
	 * @return
	 */
	public FunctionDeclarator getFunctionDeclarator(FunctionDefinition definition){
		ASTNode node=definition.getField(FunctionDefinition.DECLARATOR);
		if(node instanceof FunctionDeclarator){
			return (FunctionDeclarator)node;
		}else if(node instanceof PointerDeclarator){
			PointerDeclarator pointerDeclarator=(PointerDeclarator)node;
			node=pointerDeclarator.getField(PointerDeclarator.DECLARATOR);
			if(node instanceof FunctionDeclarator){
				return (FunctionDeclarator)node;
			}
		}
		return null;
	} 
	
	/**
	 * Checks if the given declarator contains the given identifier in its parameter list.
	 * @param identifier
	 * @param declarator
	 * @return
	 */
	public boolean isInFunctionDeclaratorParameterList(Identifier identifier,FunctionDeclarator declarator){
		Integer index=getIndexOfParameterWithName(identifier.getName(), declarator);
		if(index==null){
			return false;
		}
		Identifier declarationIdentifier=getIdentifierOfParameterWithIndex(index, declarator);
		return declarationIdentifier==identifier;
	}
	
	/**
	 * Checks if the given identifier is the interface part of a nesc function call.
	 * Note, the identifier has not to be the interface name of a global interfac but can also be an alias.
	 * @return
	 */
	public boolean isInterfacePartInNesCFunctionCall(Identifier identifier) {
		return astUtil.checkAncestorSequence(identifier, nesCfunctionCallInterfacePartAncestorSequence);
	}
}
