package tinyos.yeti.refactoring.utilities;

import java.util.Collection;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;

/**
 * Conglomeration of Methods that are useful while working whit the AST in a Refactoring Plugin
 */
public class ASTUtil {
	
	/**
	 * Tests if given ASTNode is of expected type.
	 * @param node Which is father of interest.
	 * @param type Class Type which we are expecting.
	 * @return true if node instanceof type. False if node is null or not instanceof type
	 */
	public boolean isOfType(ASTNode node,Class<? extends ASTNode> type){
		return type.isInstance(node);
	}
	
	/**
	 * Returns the next higher AST node of type
	 * @param child The AST node to start from
	 * @param type The type of the ASTNode we are looking for
	 * @return null if no parent matches, else matching Parent.
	 */
	public ASTNode getParentForName(ASTNode child,Class<? extends ASTNode> type){
		ASTNode parent=child.getParent();
		if(parent == null){
			return null;
		}
		if(parent.getClass().equals(type)){
			return parent;
		}
		return getParentForName(parent, type);
	}
	
	/**
	 * Checks if the ancestor nodes of the given child equal the given sequence.
	 * The first node in the ancestorSequence is the expected type of the parent of the child, the second the type of the grand parent and so on.
	 * @param child The child whichs ancestor sequence is to be checked.
	 * @param ancestorSequence The sequence of expected class types.
	 * @return True if the childsAncestor sequence(or a long enough part of it) matches the given ancestorSequence. 
	 */
	public boolean checkAncestorSequence(ASTNode child,Class<? extends ASTNode>[] ancestorSequence){
		ASTNode parent=child;
		for(Class<? extends ASTNode> c:ancestorSequence){
			parent=parent.getParent();
			if(!c.isInstance(parent)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Tries to find the first possible sequence of successors of the root node.
	 * The first Node in the successor sequence has to equal the type of the root node, the second the type of at least one child of the root, and so on.
	 * @param root
	 * @param successorSequence the types of the expected sequence.
	 * @return the node which is at the end of the sequence with the type of the last element in the successorSequence. Null if there is no such sequence.
	 */
	public ASTNode checkSuccessorSequence(ASTNode root,Class<? extends ASTNode>[] successorSequence){
		if(!isOfType(root, successorSequence[0])){
			return null;
		}
		ASTNode parent=root;
		for(int successorIndex=1;successorIndex<successorSequence.length;++successorIndex){
			Class<? extends ASTNode> type=successorSequence[successorIndex];
			DebugUtil.addOutput("Checking Type "+type);
			int childrens=parent.getChildrenCount();
			boolean foundNextSuccessor=false;
			//try to find a matching child for the current type
			for(int childIndex=0;!foundNextSuccessor&&childIndex<childrens;++childIndex){
				ASTNode child=parent.getChild(childIndex);
				if(child!=null){
					DebugUtil.addOutput("\tchild type: "+child.getClass());
					if(isOfType(child,type)){
						//The last type matches a child
						if(successorIndex==successorSequence.length-1){
							return parent.getChild(childIndex);
						}
						foundNextSuccessor=true;
						parent=child;
					}
				}
			}
			//No matching child was found.
			if(!foundNextSuccessor){
				return null;
			}
		}
		DebugUtil.addOutput("is Null");
		return null;
	}
	
	/**
	 * Returns the Children of a node as Collection of ASTNode
	 * @param node
	 * @return
	 */
	public Collection<ASTNode> getChilds(ASTNode node){
		Collection<ASTNode> ret = new LinkedList<ASTNode>();
		for(int i = 0; i < node.getChildrenCount(); i++){
			if(node.getChild(i) != null){
				ret.add(node.getChild(i));
			}
		}
		return ret;
	}
	
	/**
	 * @param node
	 * @return the CompoundStatement which encloses the given Node, null if the Node is not in a Function.
	 */
	public CompoundStatement getEnclosingCompound(ASTNode node) {
		ASTNode parent = getParentForName(node,CompoundStatement.class);
		if (parent == null) {
			// System.err.println("NOT IN A CompoundStatement!!!");
			return null;
		} else {
			return (CompoundStatement) parent;
		}
	}
	
	/**
	 * Returns all nodes which are successors of the root or the root itself, if they have the given type 
	 * @param <T>
	 * @param root	The node where we start searching.
	 * @param type	The type we are looking for.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getAllNodesOfType(ASTNode root,Class<T> type){
		//Add identifiers of the current Compound. This Compound must declare The identifier.
		Collection<T> matchingNodes=new LinkedList<T>();
		if(type.isInstance(root)){
			matchingNodes.add((T)root);
		}
		Collection<ASTNode> candidates=new LinkedList<ASTNode>();
		candidates.addAll(getChilds(root));
		Collection<ASTNode> newCandidates=null;
		while(candidates.size()>0){
			newCandidates=new LinkedList<ASTNode>();
			for(ASTNode candidate:candidates){
				if(candidate!=null){
					if(type.isInstance(candidate)){	
						matchingNodes.add((T)candidate);
					}
					newCandidates.addAll(getChilds(candidate));
				}
				candidates=newCandidates;
			}
		}
		return matchingNodes;
	}
	
	/**
	 * Collects all direct childs of parent with the given Type.
	 * @param <T>
	 * @param parent
	 * @param type
	 * @return The matching childs, empty list if no matches.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ASTNode> Collection<T> getChildsOfType(ASTNode parent,Class<T> type){
		Collection<T> results=new LinkedList<T>();
		Collection<ASTNode> childs=getChilds(parent);
		for(ASTNode child:childs){
			if(isOfType(child,type)){
				results.add((T)child);
			}
		}
		return results;
	}
	
	/**
	 * Checks if the fieldName of the childToCheck in the given parent equals the expectedName
	 * @param parent
	 * @param childToCheck
	 * @param expectedName
	 * @return
	 */
	public boolean checkFieldName(AbstractFixedASTNode parent,ASTNode childToCheck,String expectedName){
		if(expectedName==null||parent==null){
			return false;
		}
		String fieldName=parent.getFieldName(childToCheck);
		return expectedName.equals(fieldName);
	}
	
	/**
	 * Returns the root of the ast which this node includes;
	 * @param node
	 * @return
	 */
	public TranslationUnit getAstRoot(ASTNode node){
		ASTNode parent=node.getParent();
		ASTNode child=node;
		while(parent!=null){
			child=parent;
			parent=parent.getParent();
		}
		return (TranslationUnit)child;
	}
	
	/**
	 * Returns the first child of parent which is found with the given type.
	 * Returns null if there is no such child.
	 * @param <T>
	 * @param parent
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends ASTNode> T getFirstChildOfType(ASTNode parent,Class<T> type){
		Collection<ASTNode> childs=getChilds(parent);
		for(ASTNode child:childs){
			if(isOfType(child,type)){
				return (T)child;
			}
		}
		return null;
	}
}
