package tinyos.yeti.refactoring;

import java.util.List;

import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.preprocessor.PreprocessorReader;

public class ASTUtil {
	
	private NesC12AST ast;
	private PreprocessorReader reader;
	public ASTUtil(NesC12AST ast){
		this.ast=ast;
		reader=ast.getReader();
	}
	/**
	 * @param root
	 * @param pos
	 * @return The AST Leaf that covers this Postion, or null if the Position is not covered by a leaf.
	 */
	public static  ASTNode getASTLeafAtPos(NesC12AST ast,int pos){
		if(ast==null){
			throw new IllegalArgumentException("The ast Parameter must not be null.");
		}
		ASTNode root = ast.getRoot();
		
		boolean foundChild=true;
		while(root.getChildrenCount() > 0 && foundChild){
			foundChild=false;
			for(int i=0; i < root.getChildrenCount()&& !foundChild; i++){
				ASTNode child = root.getChild(i);
				
				// It happend to us that we got null values
				if(child!=null){

					INesC12Location location = ast.getOffsetAtEnd(child);
					if(location.getInputfileOffset() >= pos){
						foundChild=true;
						root=root.getChild(i);
					}
				}
			}	
		}
		
		if(foundChild){
			System.err.println("Pos: "+pos+" root.range:"+ast.getOffsetAtBegin(root).getInputfileOffset()+"-"+ast.getOffsetAtEnd(root).getInputfileOffset()+" root:"+root);
			return root;
		} else {
			// Happens for example if the Cursor is at a blank position
			return null;
		}
	}
	
	public static ASTNode getParentForName(ASTNode child,String name){
		ASTNode parent=child.getParent();
		if(parent==null){
			return null;
		}
		if(parent.getASTNodeName().equals(name)){
			return parent;
		}
		return getParentForName(parent, name);
	}
	
	public static void getIncludedIdentifiers(ASTNode root,String identifierName,List<Identifier> result){
		ASTNode child=null;
		Identifier identifier=null;
		for(int i=0;i<root.getChildrenCount();++i){
			child=root.getChild(i);
			if(child!=null){
				if(child instanceof Identifier){
					identifier=(Identifier)child;
					if(identifier.getName().equals(identifierName)){
						result.add((Identifier)child);
					}
				}else{
					getIncludedIdentifiers(child, identifierName, result);
				}
			}
		}
	}
	
	public int start(ASTNode node){
		return reader.inputLocation(ast.getOffsetAtBegin(node).getPreprocessedOffset(), true);
	}

	public int end(ASTNode node){
		return reader.inputLocation(ast.getOffsetAtEnd(node).getPreprocessedOffset(), true);
	}

}
