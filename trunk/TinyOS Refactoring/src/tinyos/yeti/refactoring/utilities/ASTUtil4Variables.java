package tinyos.yeti.refactoring.utilities;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclaratorList;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;

public class ASTUtil4Variables {
	
	private ASTUtil astUtil;
	private ASTUtil4Functions astUtil4Functions;
	
	public ASTUtil4Variables(){
		this(new ASTUtil());
	}
	
	public ASTUtil4Variables(ASTUtil astUtil) {
		super();
		this.astUtil = astUtil;
		this.astUtil4Functions=new ASTUtil4Functions(astUtil);
	}

	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] variableDeclarationAncestorSequence=new Class[]{
		DeclaratorName.class,
		InitDeclarator.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] declarationIdentifierSuccessorSequence=new Class[]{
		Declaration.class,
		InitDeclaratorList.class,
		InitDeclarator.class,
		DeclaratorName.class,
		Identifier.class
	};
	
	
	/**
	 * Checks if this identifier is part of a variable declaration.
	 * @param identifier
	 * @return
	 */
	public boolean isVariableDeclaration(Identifier identifier){
		return astUtil.checkAncestorSequence(identifier, variableDeclarationAncestorSequence);
	}
	
	/**
	 * Checks if this identifier is part of a variable reference.
	 * @param identifier
	 * @return
	 */
	public boolean isVariableUsage(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!(parent instanceof IdentifierExpression)){
			return false;
		}
		return true;
		//TODO if this really not is needed delete it.
//		parent=parent.getParent();
//		return
//			parent instanceof AssignmentExpression
//			||parent instanceof ArgumentExpressionList
//			||parent instanceof ArithmeticExpression
//			||parent instanceof PrefixExpression
//			||parent instanceof PostfixExpression
//			||parent instanceof ExpressionStatement;
	}
	
	/**
	 * Checks if this identifier is part of a global variable.
	 * @param identifier
	 * @return
	 */
	public boolean isGlobalVariable(Identifier identifier){
		if(isLocalVariableOrFunctionParameter(identifier)||isImplementationLocalVariable(identifier)){
			return false;
		}
		return isVariableDeclaration(identifier)||isVariableUsage(identifier);
	}
	
	/**
	 * Checks if this identifier is part of a local variable, which means a variable inside a function.
	 * @param identifier
	 * @return
	 */
	public boolean isLocalVariable(Identifier identifier){
		CompoundStatement declaringCompound=findDeclaringCompoundStatement(identifier);
		return declaringCompound!=null;
	}
	
	/**
	 * Checks if the given identifier is a local variable or a function parameter.
	 * @param identifier
	 * @return
	 */
	public boolean isLocalVariableOrFunctionParameter(Identifier identifier){
		return isLocalVariable(identifier)||isFunctionParameter(identifier);
	}
	
	/**
	 * Checks if this identifier is part of a implementation variable, which means a variable with nesc implementation scope.
	 * @param identifier
	 * @return
	 */
	public boolean isImplementationLocalVariable(Identifier identifier){
		return null!=getImplementationLocalVariableDeclarationIdentifier(identifier);
	}
	
	/**
	 * returns the identifier of the implementation local variable declaration, if the given identifier is part of such a variable.
	 * @param identifier
	 * @return the identifier, null if the given identifier is not part of a implementation local variable.
	 */
	public Identifier getImplementationLocalVariableDeclarationIdentifier(Identifier identifier){
		NesCExternalDefinitionList implementationNode=astUtil.getModuleImplementationNodeIfInside(identifier);
		if(implementationNode==null){	//the identifier is outside of a implementation scope
			return null;
		}
		String identifierName=identifier.getName();
		Collection<Declaration> declarations=astUtil.getChildsOfType(implementationNode, Declaration.class);
		for(Declaration declaration:declarations){
			Identifier id=(Identifier)astUtil.checkSuccessorSequence(declaration, declarationIdentifierSuccessorSequence);
			if(id!=null&&identifierName.equals(id.getName())){
				return id;
			}
		}
		return null;
	}
	
	
	/**
	 * It is supposed, that the declaration is in the given CompoundStatement.
	 * To find the declaring CompoundStatement, use findDeclaringCompoundStatement.
	 * Then the Method collects all Occurrences of this identifier, which have no own declaration, in all subCompoundStatements.
	 * @param compount
	 * @param identifierName
	 * @param result
	 * @return
	 */
	public Collection<Identifier> getAllIdentifiersWithoutOwnDeclaration(ASTNode compound,String identifierName){
		//Add identifiers of the current Compound. This Compound must declare The identifier.
		Collection<Identifier> identifiers=getIncludedIdentifiers(compound, identifierName, CompoundStatement.class);
		Collection<CompoundStatement> candidates=getEnclosedCompounds(compound);
		Collection<CompoundStatement> newCandidates=null;
		Collection<Identifier> currentIdentifiers=null;
		while(candidates.size()>0){
			newCandidates=new LinkedList<CompoundStatement>();
			currentIdentifiers=null;
			for(CompoundStatement candidate:candidates){
				currentIdentifiers=getIdentifiersWithoutOwnDeclaration(candidate, identifierName);
				if(currentIdentifiers!=null){	//If identifiers==null then there was an own declaration in the compound
					if(currentIdentifiers.size()>0){
						identifiers.addAll(currentIdentifiers);
					}
					newCandidates.addAll(getEnclosedCompounds(candidate));
				}
			}
			candidates=newCandidates;
		}
		return identifiers;
	}

	
	/**
	 * returns all CompoundStatement one level deeper in the tree then the given.
	 * This means that a CompoundStatement that is enclosed in a CompountStatement enclosed by the given CompoundStatement will not be added to the result.
	 * @param parent
	 * @return 
	 */
	private Collection<CompoundStatement> getEnclosedCompounds(ASTNode parent){
		List<CompoundStatement> result=new LinkedList<CompoundStatement>();
		getEnclosedCompounds_sub(parent, result);
		return result;
	}
	
	private void getEnclosedCompounds_sub(ASTNode parent,Collection<CompoundStatement> result){
		for(ASTNode child: astUtil.getChilds(parent)){
			if(child!=null){
				if(child instanceof CompoundStatement){
					result.add((CompoundStatement)child);
				} else {
					getEnclosedCompounds_sub(child,result);
				}
			}
		}
	}
	
	
	/**
	 * Returns the Identifiers in the given CompoundStatement which are not in a sub CompountStatement of the given CompountStatement and contain no own Declarator.
	 * @param compound
	 * @param identifierName
	 * @return The identifiers in the given Compound, which are not in a sub-Compound. EmptyList, if there are no identifiers, null if there are identifiers with an own declarator.
	 */
	private Collection<Identifier> getIdentifiersWithoutOwnDeclaration(CompoundStatement compound,String identifierName){
		Collection<Identifier> identifiers=getIncludedIdentifiers(compound, identifierName,CompoundStatement.class);
		if(identifiers.size()==0){
			return Collections.emptyList();
		}
		if(getDeclaratorName(identifiers)==null){
			return identifiers;
		}else{
			return null;
		}
	}
	
	
	/**
	 * 
	 * @param root ASTNode which child's are checked for being Identifier with name indentifierName 
	 * @param identifierName Name of the Identifier you are looking for
	 * @param stopClass 
	 * @return A collection with all occurrences of Identifiers below the root in the AST
	 */
	public <T> Collection<Identifier> getIncludedIdentifiers(ASTNode root, String identifierName,Class<T> stopClass){
		LinkedList<Identifier> ret = new LinkedList<Identifier>();
		getIncludedIdentifiers_sub(root, identifierName, ret,stopClass);
		return ret;
	}
	
	private <T> void getIncludedIdentifiers_sub(ASTNode root,String identifierName,Collection<Identifier> result,Class<T> stopClass){
		ASTNode child=null;
		Identifier identifier=null;
		for(int i=0;i<root.getChildrenCount();++i){
			child=root.getChild(i);
			if(child!=null){
				if(child instanceof Identifier){
					identifier=(Identifier)child;
					if(identifier.getName().equals(identifierName)){
						result.add(identifier);
					}
				} else if(!child.getClass().equals(stopClass)){
					getIncludedIdentifiers_sub(child, identifierName, result,stopClass);
				}
			}
		}
	}
	

	/**
	 * This Method can be used to Check, if these identifiers are part of a local variable.
	 * @param identifiers
	 * @return Returns a DeclaratorName, if some of the given Identifiers has such a parent. Null if there is no such parent.
	 */
	public DeclaratorName getDeclaratorName(Collection<Identifier> identifiers){
		ASTNode candidate=null;
		for(Identifier identifier:identifiers){
			candidate=astUtil.getParentForName(identifier, DeclaratorName.class);
			if(candidate!=null){
				return (DeclaratorName)candidate;
			}
		}
		return null;
	}
	
	/**
	 * Looks for the CompoundStatement which declares the given Identifier.
	 * @param identifier
	 * @return
	 */
	public CompoundStatement findDeclaringCompoundStatement(Identifier identifier){
		String name=identifier.getName();
		ASTNode child=identifier;
		CompoundStatement parent=null;
		boolean extendedToFunctionBorder=false;
		boolean foundDeclaration=false;
		//Search for the declaration in the current and upper CompountStatements, add all found Identifiers.
		while(!foundDeclaration){

			//Find Enclosing CompoundStatement
			parent = astUtil.getEnclosingCompound(child);
			if(parent==null)return null;
			if(parent.getParent() instanceof FunctionDefinition){
				extendedToFunctionBorder=true;
			}

			//Check if the declaration is in the actual compound statement. If so, this is a local variable
			foundDeclaration=getLocalVariableDeclarationIfInside(name,parent)!=null;
			if(!foundDeclaration&&extendedToFunctionBorder){	//This is not a local variable
				return null;
			} 
			//Maybe the declaration of the variable is in an CompountStatement outside the actual one but inside the FunctionDefinition-->Do another round
			child=parent;
		}
		return parent;
	}
	
	/**
	 * Returns the identifier which is the declaration of the local variable with the given variableName, if there is such a declaration in the given parent.
	 * @param variableName
	 * @param parent
	 * @return
	 */
	public Identifier getLocalVariableDeclarationIfInside(String variableName,CompoundStatement parent){
		//Get Identifiers in Compound with same Name
		Collection<Identifier> identifiers=getIncludedIdentifiers(parent, variableName,CompoundStatement.class);

		//Check if the declaration is in the actual compound statement. If so, this is a local variable
		DeclaratorName decName= getDeclaratorName(identifiers);
		if(decName==null){
			return null;
		}
		return decName.getName();
	}
	
	/**
	 * Returns the InitDeclarator of which the given identifier is part.
	 * @param id
	 * @return The ancestor InitDeclarator. Null if this id is not part of a InitDeclarator.
	 */
	public InitDeclarator identifierToInitDeclarator(Identifier id){
		if(!astUtil.checkAncestorSequence(id,variableDeclarationAncestorSequence)){
			return null;
		}
		return (InitDeclarator)id.getParent().getParent();
		
	}
	
	/**
	 * Checks if the given identifier is a identifier in a function parameter list of a function declaration.
	 * @param identifier
	 * @return
	 */
	public boolean isInFunctionDeclarationParameterList(Identifier identifier){
		if(isInFunctionDefinitionParameterList(identifier)){	//Without this test the result was also true if it was a parameter of a functionDefinition.
			return false;
		}
		FunctionDeclarator declarator=astUtil.getParentForName(identifier, FunctionDeclarator.class);
		if(declarator==null){
			return false;
		}
		return astUtil4Functions.isInFunctionDeclaratorParameterList(identifier, declarator);
	}
	
	/**
	 * Checks if the given identifier is a identifier in a function parameter list of a function definition.
	 * @param identifier
	 * @return
	 */
	public boolean isInFunctionDefinitionParameterList(Identifier identifier){
		FunctionDefinition definition=astUtil.getParentForName(identifier, FunctionDefinition.class);
		if(definition==null){
			return false;
		}
		FunctionDeclarator declarator=astUtil4Functions.getFunctionDeclarator(definition);
		if(declarator==null){
			return false;
		}
		return astUtil4Functions.isInFunctionDeclaratorParameterList(identifier, declarator);
	}
	
	/**
	 * Checks if the given identifier is a identifier in the body of a function definition which references a function parameter.
	 * @param identifier
	 * @return
	 */
	public boolean isFunctionParameterInFunctionBody(Identifier identifier){
		if(isLocalVariable(identifier)){
			return false;
		}
		FunctionDefinition definition=astUtil.getParentForName(identifier, FunctionDefinition.class);
		if(definition==null){
			return false;
		}
		FunctionDeclarator declarator=astUtil4Functions.getFunctionDeclarator(definition);
		Integer index=astUtil4Functions.getIndexOfParameterWithName(identifier.getName(), declarator);
		return index!=null;
	}
	
	/**
	 * Checks if the given identifier is a functionParameter.
	 * @param identifier
	 * @return
	 */
	public boolean isFunctionParameter(Identifier identifier){
		return isInFunctionDeclarationParameterList(identifier)
			||isInFunctionDefinitionParameterList(identifier)
			||isFunctionParameterInFunctionBody(identifier);
	}
	
	
}
