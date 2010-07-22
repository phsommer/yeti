package tinyos.yeti.refactoring.rename.global;

import org.eclipse.core.resources.IFile;

import tinyos.yeti.nesc12.parser.ast.elements.Field;

/**
 * A class which encapsulates data about one occurrence of a field in a file.
 * @author Max Urech
 *
 */
public class FieldInfo{
	private IFile file;
	private Field field;
	private FieldKind kind;
	public FieldInfo(IFile file, Field field,FieldKind kind) {
		super();
		this.file = file;
		this.field = field;
		this.kind=kind;
	}
	public IFile getFile() {
		return file;
	}
	public Field getField() {
		return field;
	}
	public FieldKind getKind() {
		return kind;
	}
	
	
}
