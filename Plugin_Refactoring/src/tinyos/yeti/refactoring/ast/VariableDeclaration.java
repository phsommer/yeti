package tinyos.yeti.refactoring.ast;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclaratorList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParameterDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Pointer;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.PointerDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.RefactoringInfo;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.StringUtil;

/**
 * Provides an Interface for different kind of Declarators
 */
public class VariableDeclaration {
	ParameterDeclaration pd = null;
	Declaration d = null;
	RefactoringInfo info;
	ASTUtil astUtil = new ASTUtil();

	public VariableDeclaration(ParameterDeclaration pd, RefactoringInfo info) {
		this.pd = pd;
		this.info = info;
	}

	public VariableDeclaration(Declaration d, RefactoringInfo info) {
		this.d = d;
		this.info = info;
	}

	public boolean isParameterDeclaration() {
		return pd != null;
	}

	public List<Identifier> getVariables() {
		LinkedList<Identifier> ret = new LinkedList<Identifier>();
		if (isParameterDeclaration()) {
			ret.add(getFirstIdentifier(pd.getDeclarator()));
		} else {
			for (int j = 0; d.getInitlist().getChildrenCount() > j; j++) {
				InitDeclaratorList idl = d.getInitlist();
				Declarator declarator = idl.getNoError(j).getDeclarator();
				Identifier id = (Identifier) getFirstIdentifier(declarator);
				ret.add(id);
			}
		}
		// It happend that we had null values in the List
		ret.removeFirstOccurrence(null);
		return ret;
	}

	private PointerDeclarator getPointerDeclarator(Identifier id) {
		PointerDeclarator dec = null;

		if (isParameterDeclaration()) {
			// Cause there is only one, the name can be ignored
			if (pd.getDeclarator() instanceof PointerDeclarator) {
				dec = (PointerDeclarator) pd.getDeclarator();
			}
		} else {
			InitDeclarator initDec = astUtil.getParentForName(id,
					InitDeclarator.class);
			if (initDec.getDeclarator() instanceof PointerDeclarator) {
				dec = (PointerDeclarator) initDec.getDeclarator();
			}
		}

		return dec;
	}

	private int getPointerLevel(Identifier name) {
		int ret = 0;
		PointerDeclarator dec = getPointerDeclarator(name);

		if (dec != null) {
			Pointer pointer = dec.getPointer();
			while (pointer != null) {
				ret++;
				pointer = pointer.getPointer();
			}
		}
		return ret;
	}

	public List<String> getPointerNames() throws CoreException,
			MissingNatureException, IOException {
		List<Identifier> vars = getVariables();
		List<String> ret = new LinkedList<String>();
		for (Identifier var : vars) {
			ret.add(getPointerName(var));
		}

		return ret;
	}
	
	private String getPointerName(Identifier var){
		StringBuffer pointerName = new StringBuffer();

		for (int i = 0; i < getPointerLevel(var); i++) {
			pointerName.append("*");
		}
		

		pointerName.append(var.getName());
		return pointerName.toString();
	}
	
	public String getPointerName(String name){
		for(Identifier id: getVariables()){
			if(id.getName().endsWith(name)){
				return getPointerName(id);
			}
		}
		throw new IllegalArgumentException("\""+name +"\" is not a defined Variable in this Declaration. This Decalaration declarates: "+getVariableNames());
	}

	/**
	 * Type without Pointer
	 * 
	 * @return
	 * @throws CoreException
	 * @throws MissingNatureException
	 * @throws IOException
	 */
	public String getType() throws CoreException, MissingNatureException,
			IOException {
		StringBuffer ret = new StringBuffer();
		if (isParameterDeclaration()) {
			ret.append(getSourceCode(pd.getSpecifiers()));
		} else {
			ret.append(getSourceCode(d.getSpecifiers()));
		}
		return ret.toString();
	}

	private String getSourceCode(ASTNode node) throws CoreException,
			MissingNatureException, IOException {
		return info.getAstPositioning().getSourceCode(node,
				info.getProjectUtil());
	}

	public List<String> getVariableNames() {
		List<String> ret = new LinkedList<String>();
		for (Identifier id : getVariables()) {
			ret.add(id.getName());
		}
		return ret;
	}

	public String getPartialDeclaration(Set<String> variableNamesToKeep)
			throws CoreException, MissingNatureException, IOException {
		variableNamesToKeep = new HashSet<String>(variableNamesToKeep);
		variableNamesToKeep.retainAll(getVariableNames());
		if (variableNamesToKeep.size() == 0) {
			return "";
		}
		if (isParameterDeclaration()) {
			return getSourceCode(pd);
		}

		StringBuffer ret = new StringBuffer();
		ret.append(getType());
		ret.append(" ");
		InitDeclaratorList varList = d.getInitlist();
		List<String> keptDeclarations = new LinkedList<String>();
		for (int i = 0; i < varList.getChildrenCount(); i++) {
			InitDeclarator initDec = varList.getNoError(i);
			if (initDec != null
					&& initDec.getDeclarator() != null
					&& variableNamesToKeep.contains(getFirstIdentifier(
							initDec.getDeclarator()).getName())) {
				keptDeclarations.add(getSourceCode(initDec));
			}
		}
		ret.append(StringUtil.joinString(keptDeclarations, ", "));
		ret.append(";");

		return ret.toString();
	}

	/**
	 * Returns the first found identifier or null if no Identifier found
	 */
	private Identifier getFirstIdentifier(ASTNode node) {
		Queue<ASTNode> q = new LinkedList<ASTNode>();
		q.add(node);
		while (!q.isEmpty()) {
			node = q.poll();
			if (node instanceof Identifier) {
				return (Identifier) node;
			}
			q.addAll(astUtil.getChilds(node));
		}
		return null;
	}

	public ASTNode getAstNode() {
		if (isParameterDeclaration()) {
			return this.pd;
		} else {
			return this.d;
		}
	}

	public static boolean isDeclaration(ASTNode node) {
		return isDeclarationP(node) || isParameterDeclaration(node);
	}

	private static boolean isDeclarationP(ASTNode node) {
		return (node instanceof Declaration);
	}

	private static boolean isParameterDeclaration(ASTNode node) {
		return (node instanceof ParameterDeclaration);
	}

	/**
	 * Returns a VariableDeclaration for the ASTNode if it was a Variable
	 * declaration
	 * 
	 * @throws IllegalArgumentException
	 *             if the AST node is not a Variable declaration
	 */
	public static VariableDeclaration factory(ASTNode node, RefactoringInfo info) {
		if (isDeclarationP(node)) {
			return new VariableDeclaration((Declaration) node, info);
		} else if (isParameterDeclaration(node)) {
			return new VariableDeclaration((ParameterDeclaration) node, info);
		} else {
			throw new IllegalArgumentException(
					"VariableDeclaration Object can only be created using ASTNodes that declare a Variable.");
		}
	}
	
	public static <T extends Collection<? extends ASTNode>> Set<VariableDeclaration> getTopLevelDeclarations(T statements, RefactoringInfo info) {
		Set<VariableDeclaration> declarations = new HashSet<VariableDeclaration>();
		for (ASTNode s : statements) {
			if (VariableDeclaration.isDeclaration(s)) {
				VariableDeclaration dec = VariableDeclaration.factory(s, info);
				declarations.add(dec);
			}
		}
		return declarations;
	}
}
