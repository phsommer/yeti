package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class RenameLocalVariableDelegate implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private ISelection selection;

	@Override
	public void dispose() {
		window = null;

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;

	}
	
	/**
	 * @param root
	 * @param pos
	 * @return The AST Leaf that covers this Postion, or null if the Position is not covered by a leaf.
	 */
	private  ASTNode getASTLeafAtPos(NesC12AST ast,int pos){
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
				if(child==null){ continue; }
				
				INesC12Location location = ast.getOffsetAtEnd(child);
				if(location.getInputfileOffset() >= pos){
					foundChild=true;
					root=root.getChild(i);
				}
			}	
		}
		
		if(foundChild){
			return root;
		} else {
			// Happens for example if the Cursor is at a blank position
			return null;
		}
	}

	@Override
	public void run(IAction action) {
		String oldName="";
		MultiPageNesCEditor multiPageEditor = getEditor();
		NesCEditor editor = multiPageEditor.getNesCEditor();
		
		
		ISelection selectionTmp = editor.getSelectionProvider().getSelection();
		
		if(selectionTmp.isEmpty() || !(selectionTmp instanceof ITextSelection)){
			throw new RuntimeException("----- Es war keine ITextSelection");
		}
		
		
		ITextSelection selection = (ITextSelection) selectionTmp;
		int selectionStart = selection.getOffset();
		NesC12AST ast=(NesC12AST)editor.getAST();
		
		ASTNode ours = getASTLeafAtPos(ast, selectionStart);
		System.err.println("Found Area of marked ASTNode: "+ours.getRange().getLeft() + " <-> " + ours.getRange().getRight());
		
		Identifier id = (Identifier)ours;
		System.err.println("Name: "+id.getName());
		System.err.println("ASTNodeName: "+id.getASTNodeName());
		System.err.println("Class: "+id.getClass().getName());
		
		System.err.println("Selection: "+selection.getText());
		
		
		
		
		/*for(IASTModelElement element:editor.getSelectedElements()){
			if(element instanceof Identifier){
				Identifier identifier = (Identifier) element;
				oldName=identifier.getName();
				System.err.println("Jubidubidei es isch ein Identifyer.");
			} else {
				// TODO: This happens if not an identifier is selected. 
				//throw new IllegalStateException("Only an Identifyer is allowed to be selected.");
				System.out.println("Element hat den Type:"+element.getClass().getName());
				System.out.println("The Identifier is:"+((IASTModelElement)element).getIdentifier());
			}
		}*/

		RenameLocalVariableInfo info = new RenameLocalVariableInfo(oldName);
		RenameLocalVariableProcessor processor = new RenameLocalVariableProcessor(editor);
		RenameLocalVariableRefactoring refactoring = new RenameLocalVariableRefactoring(processor);
		RenameLocalVariableWizard wizard = new RenameLocalVariableWizard(refactoring,info);
		RefactoringWizardOpenOperation wizardStarter = new RefactoringWizardOpenOperation(
				wizard);

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}

	private MultiPageNesCEditor getEditor() {
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return null;
		}

		IEditorPart editorTmp = activePage.getActiveEditor();
		if (editorTmp == null || !(editorTmp instanceof MultiPageNesCEditor)) {
			throw new RuntimeException("----- Es war kein MultiPageNesCEditor");
		}
		ITextEditor editor = (ITextEditor) editorTmp;

		if (editor instanceof MultiPageNesCEditor) {
			return (MultiPageNesCEditor) editor;
		} else {
			throw new IllegalStateException(
					"Rename Local Varibel Refactoring is only allowed if a NesC Editor is in use. But "+editor.getClass().getName()+" was in use.");
		}
	}

}
