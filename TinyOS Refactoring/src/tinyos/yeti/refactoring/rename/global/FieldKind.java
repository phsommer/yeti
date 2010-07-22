package tinyos.yeti.refactoring.rename.global;

/**
 * The FieldKind gives information about the role that an occurrence of a field has.
 * @author Max Urech
 *
 */
public enum FieldKind{
	DECLARATION,
	INCLUDED_DECLARATION,
	FORWARD_DECLARATION,
	REFERENCE,
	DEFINITION
}