package tinyos.yeti.refactoring.ast;

import java.util.Collection;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Access;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceReference;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceType;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedInterface;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedInterfaceList;
import tinyos.yeti.refactoring.utilities.ASTUtil;

public class ComponentAstAnalyser extends AstAnalyzer {
	
	protected TranslationUnit root;
	protected Identifier componentIdentifier;
	protected AccessList specification;
	
	private Collection<InterfaceReference> interfaceReferences;
	private Collection<Identifier> referencedInterfaceIdentifiers;
	private Collection<Identifier> referencedInterfaceAliasIdentifiers;
	
	public ComponentAstAnalyser(TranslationUnit root,Identifier componentIdentifier, AccessList specification) {
		super();
		this.root = root;
		this.componentIdentifier = componentIdentifier;
		this.specification = specification;
	}
	
	/**
	 * Gathers all interfaces which are referenced in the specification of this NesC component.
	 * @return
	 */
	public Collection<InterfaceReference> getInterfaceReferences(){
		if(interfaceReferences==null){
			ASTUtil astUtil=getASTUtil();
			Collection<Access> accesses=astUtil.getChildsOfType(specification, Access.class);
			Collection<ParameterizedInterfaceList> interfaceLists=collectFieldsWithName(accesses, Access.INTERFACES);
			Collection<ParameterizedInterface> parametrizedInterfaces=new LinkedList<ParameterizedInterface>();
			for(ParameterizedInterfaceList list: interfaceLists){
				parametrizedInterfaces.addAll(astUtil.getChildsOfType(list, ParameterizedInterface.class));
			}
			interfaceReferences=collectFieldsWithName(parametrizedInterfaces,ParameterizedInterface.REFERENCE);
		}
		return interfaceReferences;
	}
	
	/**
	 * Gathers all interface identifiers of the interfaces which are referenced in the specification of this NesC component.
	 * @return
	 */
	public Collection<Identifier> getReferencedInterfaceIdentifiers(){
		if(referencedInterfaceIdentifiers==null){
			Collection<InterfaceType> interfaceTypes=collectFieldsWithName(getInterfaceReferences(),InterfaceReference.NAME);
			referencedInterfaceIdentifiers=collectFieldsWithName(interfaceTypes, InterfaceType.NAME);
		}
		return referencedInterfaceIdentifiers;
	}
	
	/**
	 * Gathers all interface alias identifiers of the interfaces which are referenced in the specification of this NesC component and are aliased.
	 * @return
	 */
	public Collection<Identifier> getReferencedInterfaceAliasIdentifiers(){
		if(referencedInterfaceAliasIdentifiers==null){
			referencedInterfaceAliasIdentifiers=collectFieldsWithName(getInterfaceReferences(), InterfaceReference.RENAME);
		}
		return referencedInterfaceAliasIdentifiers;
	}
	
	/**
	 * Collects of every given parent the field with the fieldName and adds it to the returned collection, if it is not null.
	 * @param <CHILD_TYPE>	The type which the field with the given fielName has.
	 * @param parents	The AbstractFixedASTNodes of which we want to collect a field/child. 
	 * @param fieldName The name of the field we are interested in.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <CHILD_TYPE> Collection<CHILD_TYPE> collectFieldsWithName(Collection<? extends AbstractFixedASTNode> parents,String fieldName){
		Collection<CHILD_TYPE> childs=new LinkedList<CHILD_TYPE>();
		for(AbstractFixedASTNode parent:parents){
			CHILD_TYPE child=(CHILD_TYPE)parent.getField(fieldName);
			if(child!=null){
				childs.add(child);
			}
		}
		return childs;
	}
	
}
