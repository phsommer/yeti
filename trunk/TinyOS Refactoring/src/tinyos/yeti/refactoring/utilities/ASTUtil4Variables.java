package tinyos.yeti.refactoring.utilities;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclaratorList;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ArgumentExpressionList;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ArithmeticExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.AssignmentExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PostfixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PrefixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;

public class ASTUtil4Variables {

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
	public static boolean isVariableDeclaration(Identifier identifier){
		return ASTUtil.checkAncestorSequence(identifier, variableDeclarationAncestorSequence);
	}
	
	/**
	 * Checks if this identifier is part of a variable reference.
	 * @param identifier
	 * @return
	 */
	public static boolean isVariableUsage(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!(parent instanceof IdentifierExpression)){
			return false;
		}
		parent=parent.getParent();
		return
			parent instanceof AssignmentExpression
			||parent instanceof ArgumentExpressionList
			||parent instanceof ArithmeticExpression
			||parent instanceof PrefixExpression
			||parent instanceof PostfixExpression;
	}
	
	/**
	 * Checks if this identifier is part of a global variable.
	 * @param identifier
	 * @return
	 */
	public static boolean isGlobalVariable(Identifier identifier){
		if(isLocalVariable(identifier)||isImplementationLocalVariable(identifier)){
			return false;
		}
		return isVariableDeclaration(identifier)||isVariableUsage(identifier);
	}
	
	/**
	 * Checks if this identifier is part of a local variable, which means a variable inside a function.
	 * @param identifier
	 * @return
	 */
	public static boolean isLocalVariable(Identifier variable){
		CompoundStatement declaringCompound=findDeclaringCompoundStatement(variable);
		return (declaringCompound!=null);
	}
	
	/**
	 * Checks if this identifier is part of a implementation variable, which means a variable with nesc implementation scope.
	 * @param identifier
	 * @return
	 */
	public static boolean isImplementationLocalVariable(Identifier identifier){
		return null!=getImplementationLocalVariableDeclarationIdentifier(identifier);
	}
	
	/**
	 * returns the identifier of the implementation local variable declaration, if the given identifier is part of such a variable.
	 * @param identifier
	 * @return the identifier, null if the given identifier is not part of a implementation local variable.
	 */
	public static Identifier getImplementationLocalVariableDeclarationIdentifier(Identifier identifier){
		NesCExternalDefinitionList implementationNode=ASTUtil.getLocalImplementationNodeIfInside(identifier);
		if(implementationNode==null){	//the identifier is outside of a implementation scope
			return null;
		}
		String identifierName=identifier.getName();
		Collection<Declaration> declarations=ASTUtil.getChildsOfType(implementationNode, Declaration.class);
		for(Declaration declaration:declarations){
			Identifier id=(Identifier)ASTUtil.checkSuccessorSequence(declaration, declarationIdentifierSuccessorSequence);
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
	public Collection<Identifier> getAllIdentifiers(ASTNode compound,String identifierName){
		//Add identifiers of the current Compound. This Compound must declare The identifier.
		Collection<Identifier> identifiers=ASTUtil.getIncludedIdentifiers(compound, identifierName, CompoundStatement.class);
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
		for(ASTNode child: ASTUtil.getChilds(parent)){
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
		Collection<Identifier> identifiers=ASTUtil.getIncludedIdentifiers(compound, identifierName,CompoundStatement.class);
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
	 * This Method can be used to Check, if these identifiers are part of a local variable.
	 * @param identifiers
	 * @return Returns a DeclaratorName, if some of the given Identifiers has such a parent. Null if there is no such parent.
	 */
	public static DeclaratorName getDeclaratorName(Collection<Identifier> identifiers){
		ASTNode candidate=null;
		for(Identifier identifier:identifiers){
			candidate=ASTUtil.getParentForName(identifier, DeclaratorName.class);
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
	public static CompoundStatement findDeclaringCompoundStatement(Identifier identifier){
		String name=identifier.getName();
		ASTNode child=identifier;
		CompoundStatement parent=null;
		Collection<Identifier> identifiers=null;
		boolean extendedToFunctionBorder=false;
		boolean foundDeclaration=false;
		//Search for the declaration in the current and upper CompountStatements, add all found Identifiers.
		while(!foundDeclaration){

			//Find Enclosing CompoundStatement
			parent = ASTUtil.getEnclosingCompound(child);
			if(parent==null)return null;
			if(parent.getParent() instanceof FunctionDefinition){
				extendedToFunctionBorder=true;
			}
			//Get Identifiers in Compound with same Name
			identifiers=ASTUtil.getIncludedIdentifiers(parent, name,CompoundStatement.class);

			//Check if the declaration is in the actual compound statement. If so, this is a local variable
			if(getDeclaratorName(identifiers)!=null){
				foundDeclaration=true;
			}
			else{
				if(extendedToFunctionBorder&&!foundDeclaration){	//This is not a local variable
					return null;
				} 
			}
			//Maybe the declaration of the variable is in an CompountStatement outside the actual one but inside the FunctionDefinition-->Do another round
			child=parent;
		}
		return parent;
	}
	
	/**
	 * Returns the InitDeclarator of which the given identifier is part.
	 * @param id
	 * @return The ancestor InitDeclarator. Null if this id is not part of a InitDeclarator.
	 */
	public static InitDeclarator identifierToInitDeclarator(Identifier id){
		if(!ASTUtil.checkAncestorSequence(id,variableDeclarationAncestorSequence)){
			return null;
		}
		return (InitDeclarator)id.getParent().getParent();
		
	}
	
}
