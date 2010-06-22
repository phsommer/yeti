package tinyos.yeti.refactoring.rename;

import org.eclipse.core.runtime.CoreException;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.refactoring.ASTUtil;

public abstract class RenameProcessor extends org.eclipse.ltk.core.refactoring.participants.RenameProcessor {

	private RenameInfo info;
	private ASTUtil utility;

	public RenameProcessor(RenameInfo info) {
		super();
		this.info = info;

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
}
