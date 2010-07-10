package tinyos.yeti.refactoring.rename;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ASTUtil;
import tinyos.yeti.refactoring.ASTUtil4Variables;
import tinyos.yeti.refactoring.ActionHandlerUtil;

public abstract class RenameProcessor extends org.eclipse.ltk.core.refactoring.participants.RenameProcessor {

	private RenameInfo info;
	private ASTUtil utility;
	private ITextSelection selection;
	private ASTUtil4Variables varUtil = new ASTUtil4Variables();

	public RenameProcessor(RenameInfo info) {
		super();
		this.info = info;

		selection = ActionHandlerUtil.getSelection(info.getEditor());
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return (getAstUtil() != null);
	}

	/**
	 * Often the first initialization of the Class is before the AST is ready.
	 * This getter makes sure the AST is used, as soon as it is available.
	 * 
	 * @return
	 */
	protected ASTUtil getAstUtil() {
		if (utility == null) {
			NesC12AST ast = (NesC12AST) info.getEditor().getAST();
			if (ast != null) {
				utility = new ASTUtil(ast);
			}
		}
		return utility;
	}

	/**
	 * 
	 * @return	The Currently Selected Identifier, null if not an Identifier is Selected.
	 */
	protected Identifier getSelectedIdentifier() {
		int selectionStart = getSelection().getOffset();
		return utility.getASTLeafAtPos(selectionStart,Identifier.class);
	}



	protected ITextSelection getSelection() {
		return selection;
	}


	protected ASTUtil4Variables getVarUtil() {
		return varUtil;
	}
	protected void addChanges4Identifiers(Collection<Identifier> identifiers,String newName,MultiTextEdit multiTextEdit,NesC12AST ast){
		ASTUtil util;
		if(ast==null){
			util=getAstUtil();
		}else{
			util=new ASTUtil(ast);
		}
		for (Identifier identifier : identifiers) {
			//TODO erase
			System.err.println("ID FOUND: "+identifier);
			int beginOffset = util.start(identifier);
			int endOffset = util.end(identifier);
			//TODO erase
			System.err.println("beginOffset "+beginOffset);
			System.err.println("endOffset "+endOffset);
			int length = endOffset - beginOffset;
//			int beginOffset = ast.getOffsetAtBegin(identifier).getInputfileOffset();
//			int endOffset = ast.getOffsetAtEnd(identifier).getInputfileOffset();
//			int length = endOffset - beginOffset;
			//TODO erase
			System.err.println("Länge der zu ersetzenden Indentifyer: "+length);
			multiTextEdit.addChild(new ReplaceEdit(beginOffset, length, newName));
		}
	}
}
