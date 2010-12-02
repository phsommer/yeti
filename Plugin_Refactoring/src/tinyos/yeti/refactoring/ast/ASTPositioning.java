package tinyos.yeti.refactoring.ast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.preprocessor.PreprocessorReader;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class ASTPositioning {
	private NesC12AST ast;
	private PreprocessorReader reader;
	
//	/**
//	 * Finds the editor by itself. Attention, this Constructor works only if the Editor has the Focus. 
//	 * As soon as an other Window opens, this is no longer the case.
//	 * @throws NullPointerException If the AST is not yet initialized.
//	 * @throws IllegalStateException If the found AST or Editor is not of the expected type.
//	 */
//	public ASTPositioning(){
//		IWorkbenchWindow w=	RefactoringPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
//		IEditorPart editorPart = w.getActivePage().getActiveEditor();
//		
//		NesCEditor editor = null;
//		if(editorPart instanceof NesCEditor){
//			editor = (NesCEditor)editorPart;
//		} else if (editorPart instanceof MultiPageNesCEditor) {
//			editor = ((MultiPageNesCEditor) editorPart).getNesCEditor();
//		} else {
//			throw new IllegalStateException("Found editor was not a NesCEditor but a " + editorPart.getClass().getCanonicalName());
//		}
//		INesCAST ast = editor.getAST();
//		if(ast instanceof NesC12AST){
//			init((NesC12AST) ast);
//		} else if(ast == null){
//			throw new NullPointerException("The AST must not be NULL");
//		} else {
//			(new Exception()).printStackTrace();
//			throw new IllegalStateException("The AST of the Editor has to be a NesC12AST but was "+ ast.getClass().getCanonicalName());
//		}
//	}
	
	/**
	 * 
	 * @param ast The AST to be used. Must not be NULL
	 * @throws NullPointerException if the ast is NULL
	 */
	public ASTPositioning(NesC12AST ast){
		if(ast == null){
			throw new NullPointerException("The given AST must not be NULL");
		}
		init(ast);
	}
	
	
	private void init(NesC12AST ast){
		this.ast=ast;
		reader=ast.getReader();
	}
	


	/**
	 * Finds an AST leave at a Preprocessed Pos
	 * TODO: Is that really necessary to have twice, almost the same Method?
	 */
	public  ASTNode getASTLeafAtPreprocessedPos(int pos){
		ASTNode root = ast.getRoot();
		boolean foundChild=true;
		while(root.getChildrenCount() > 0 && foundChild){
			foundChild=false;
			for(int i=0; i < root.getChildrenCount()&& !foundChild; i++){
				ASTNode child = root.getChild(i);
				
				// It happend to us that we got null values
				if(child!=null){
					if(child.getRange().getRight() >= pos){
						foundChild=true;
						root=root.getChild(i);
					}
				}
			}	
		}
		
		// Cause it's only checked if end(child) >= pos the start has to be checked too.  
		if(foundChild && pos >= root.getRange().getLeft()){
			return root;
		} else {
			// Happens for example if the Cursor is at a blank position
			return null;
		}
	}
	
	/**
	 * Retruns the deepest AST node which spans over the given Position 
	 * @param pos
	 * @return The AST Node spaning over the Position. If no vaild Position is given, the Root node is returned.
	 */
	public ASTNode getDeepestAstNodeAtPos(int pos){
		return getDeepestAstNodeOverRange(pos,0);
	}
	
	/**
	 * Retruns the deepest AST node which spans over the given range. 
	 * @param pos
	 * @return The AST Node spaning over the range. If no valid Position is given, the Root node is returned.
	 */
	public ASTNode getDeepestAstNodeOverRange(int offset,int length){
		int start=offset;
		int end=start+length;
		ASTNode root = ast.getRoot();
		boolean foundChild=true;
		while(root.getChildrenCount() > 0 && foundChild){
			foundChild=false;
			for(int i=0; i < root.getChildrenCount() && !foundChild; i++){
				ASTNode child = root.getChild(i);
				
				// It happened to us that we got null values
				if(child!=null){
					if(end(child) >= end && start(child) <= start){
						foundChild=true;
						root=root.getChild(i);
					}
				}
			}	
		}
		
		return root;
	}
	
	/**
	 * Method returns the AST-Leaf that relates to the Position specified in pos in the not preprocessed input file. 
	 * @param pos Position in the original Input File
	 * @return The AST Leaf that covers this Position, or null if the Position is not covered by a leaf.
	 */
	private  ASTNode getASTLeafAtPos(int pos){
		
		ASTNode node = getDeepestAstNodeAtPos(pos);
		  
		if(node.getChildrenCount() == 0){
			return node;
		} else {
			// Happens for example if the Cursor is at a blank position
			return null;
		}
	}
	
	/**
	 * Method returns the AST-Leaf that relates to the Position specified in the not preprocessed input file.
	 * Uses the middle point of the selection, which may lead to a more accurate result. 
	 * @param start The assumed Position where the leaf you are looking for starts.
	 * @param length The assumed length of the area which the leaf includes.
	 * @return	 The AST Leaf that covers this Position, or null if the Position is not covered by a leaf.
	 */
	public  ASTNode getASTLeafAtPos(int start,int length){
		start+=(length-1)/2;
		return getASTLeafAtPos(start);
	}
	
	/**
 	*
 	* @param <T> The type which the Leaf is you are looking for. 
 	* @param start The assumed Position where the leaf you are looking for starts.
 	* @param lenth The assumed length of the area which the leaf includes.
 	* @param type	The type which the Leaf is you are looking for. 
 	* @return The currently selected ASTNode Element. Null if the given type does not match the selected Element.
 	*/
	@SuppressWarnings("unchecked") // Eclipse thinks that we have a unchecked Class cast. But it's not unchecked.
	public <T extends ASTNode> T getASTLeafAtPos(int start,int length,Class<T> type) {
		ASTNode currentlySelected = this.getASTLeafAtPos(start,length);
		if(type.isInstance(currentlySelected)){
			return (T) currentlySelected;
		} else {
			return null;
		}
	}
	

	
	/**
	 * Returns the Begin-Offset of node in the not preprocessed input file
	 * @param node The node you wan't to know the offset of.
	 * @return	Well, the offset.
	 */
	public int start(ASTNode node){
		return reader.inputLocation(ast.getOffsetAtBegin(node).getPreprocessedOffset(), true);
	}

	/**
	 * As start, just the end.
	 */
	public int end(ASTNode node){
		return reader.inputLocation(ast.getOffsetAtEnd(node).getPreprocessedOffset(), true);
	}
	
	/**
	 * Returns the code, this Part of the Ast was generated from
	 * @throws MissingNatureException 
	 * @throws CoreException 
	 * @throws IOException 
	 */
	public String getSourceCode(ASTNode node, ProjectUtil projectUtil) throws CoreException, MissingNatureException, IOException {
		int begin = start(node);
		int end = end(node);
		int len = end - begin;
	
		IFile nodeSource = projectUtil.getIFile4ParseFile(ast.getParseFile());
		
		return getStringFromFile(begin, len, nodeSource);
	}
	
	private String getStringFromFile(int offset, int len, IFile file) throws CoreException, MissingNatureException, IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		br.skip(offset);
		char[] buff = new char[len];
		br.read(buff, 0, len);
		String sourceCode = new String(buff);
		
		return sourceCode;
	}
	
	/**
	 * Returns the SourceCode between two AST nodes
	 */
	public String getSourceBetween(ASTNode first, ASTNode second, ProjectUtil projectUtil) throws CoreException, MissingNatureException, IOException{
		if(end(first) > start(second)){
			throw new IllegalArgumentException("The second node has to be left of the first Node.");
		}
		
		int endFirst =end(first);
		int beginSecond = start(second);
		int len = beginSecond - endFirst;
		
		IFile nodeSource = projectUtil.getIFile4ParseFile(ast.getParseFile());
		
		return getStringFromFile(endFirst, len , nodeSource);
	}
	
	public CompoundStatement getDeepedstSuperCompoundSuperstatement(int pos) {
		ASTNode beginNode = getDeepestAstNodeAtPos(
				pos);

		CompoundStatement ret = null;
		if (beginNode instanceof CompoundStatement) {
			ret = (CompoundStatement) beginNode;
		} else {
			ret = (CompoundStatement) (new ASTUtil()).getParentForName(beginNode,
					CompoundStatement.class);
		}
		return ret;
	}
	
}
