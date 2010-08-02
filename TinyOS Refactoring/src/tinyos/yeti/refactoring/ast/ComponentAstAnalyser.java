package tinyos.yeti.refactoring.ast;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Access;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceReference;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceType;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedInterface;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedInterfaceList;
import tinyos.yeti.refactoring.utilities.ASTUtil;

public class ComponentAstAnalyser extends NesCAstAnalyzer {
	
	protected AccessList specification;
	
	private Collection<InterfaceReference> interfaceReferences;
	private Collection<Identifier> referencedInterfaceIdentifiers;
	private Collection<Identifier> referencedInterfaceAliasIdentifiers;
	private Map<Identifier,Identifier> alias2AliasedInterface;
	private Map<Identifier,Identifier> interfaceLocalName2InterfaceGlobalName;
	
	public ComponentAstAnalyser(TranslationUnit root,Identifier componentIdentifier, AccessList specification) {
		super(root,componentIdentifier);
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
			Collection<ParameterizedInterfaceList> interfaceLists=astUtil.collectFieldsWithName(accesses, Access.INTERFACES);
			Collection<ParameterizedInterface> parametrizedInterfaces=new LinkedList<ParameterizedInterface>();
			for(ParameterizedInterfaceList list: interfaceLists){
				parametrizedInterfaces.addAll(astUtil.getChildsOfType(list, ParameterizedInterface.class));
			}
			interfaceReferences=astUtil.collectFieldsWithName(parametrizedInterfaces,ParameterizedInterface.REFERENCE);
		}
		return interfaceReferences;
	}
	
	/**
	 * Gathers all interface identifiers of the interfaces which are referenced in the specification of this NesC component.
	 * @return
	 */
	public Collection<Identifier> getReferencedInterfaceIdentifiers(){
		if(referencedInterfaceIdentifiers==null){
			Collection<InterfaceType> interfaceTypes=astUtil.collectFieldsWithName(getInterfaceReferences(),InterfaceReference.NAME);
			referencedInterfaceIdentifiers=astUtil.collectFieldsWithName(interfaceTypes, InterfaceType.NAME);
		}
		return referencedInterfaceIdentifiers;
	}
	
	/**
	 * Gathers all interface alias identifiers of the interfaces which are referenced in the specification of this NesC component and are aliased.
	 * @return
	 */
	public Collection<Identifier> getReferencedInterfaceAliasIdentifiers(){
		if(referencedInterfaceAliasIdentifiers==null){
			referencedInterfaceAliasIdentifiers=astUtil.collectFieldsWithName(getInterfaceReferences(), InterfaceReference.RENAME);
		}
		return referencedInterfaceAliasIdentifiers;
	}

	/**
	 * Returns a map which maps the interfaceLocalNameIdentifier, which is an interface rename with the NesC "as" keyword in the Component Specification,
	 * to the interfaceGlobalName, which is the interface bevor the as keyword in the specificaion.
	 * If there is no as keyword for a given interface reference then interfaceGlobalName==interfaceLocalName.
	 * @return
	 */
	public Map<Identifier,Identifier> getInterfaceLocalName2InterfaceGlobalName(){
		if(interfaceLocalName2InterfaceGlobalName==null){
			interfaceLocalName2InterfaceGlobalName=new HashMap<Identifier, Identifier>();
			for(InterfaceReference reference:getInterfaceReferences()){
				InterfaceType type=(InterfaceType)reference.getField(InterfaceReference.NAME);
				if(type!=null){
					Identifier interfaceGlobalName=(Identifier)type.getField(InterfaceType.NAME);
					if(interfaceGlobalName!=null){
						Identifier interfaceLocalName=(Identifier)reference.getField(InterfaceReference.RENAME);
						if(interfaceLocalName==null){
							interfaceLocalName=interfaceGlobalName;
						}
						interfaceLocalName2InterfaceGlobalName.put(interfaceLocalName, interfaceGlobalName);
					}
				}
			}
		}
		return interfaceLocalName2InterfaceGlobalName;
	}
	
	/**
	 * Returns a map which maps an interface alias identifier to the identifier of the interface it aliases, in the specification of a NesC Component.
	 * Returns null if there is no such alias in the specification.
	 * @return
	 */
	public Map<Identifier,Identifier> getAlias2AliasedInterface(){
		if(alias2AliasedInterface==null){
			alias2AliasedInterface=new HashMap<Identifier, Identifier>();
			Map<Identifier,Identifier> interfaceLocal2Global=getInterfaceLocalName2InterfaceGlobalName();
			for(Identifier interfaceLocalName:interfaceLocal2Global.keySet()){
				Identifier interfaceGlobalName=interfaceLocal2Global.get(interfaceLocalName);
				if(interfaceLocalName!=interfaceGlobalName){
					alias2AliasedInterface.put(interfaceLocalName,interfaceGlobalName);
				}
			}
		}
		return alias2AliasedInterface;
	}
	
	/**
	 * Returns the identifier of the interface which is aliased with the given alias, in the specification of a NesC Component.
	 * Use {@link tinyos.yeti.refactoring.ast.AstAnalyzer#getAliasIdentifier4InterfaceAliasName(String alias) getIdentifierForInterfaceAliasName} to get the alias identifier.
	 * @param alias
	 * @return
	 */
	public Identifier getInerfaceIdentifier4InterfaceAliasIdentifier(Identifier alias){
		return getAlias2AliasedInterface().get(alias);
	}
	
	/**
	 * Returns the Identifier of the interface alias with the given name in the specification of a NesC Component.
	 * Returns null if there is no alias with the given name.
	 * @param alias
	 * @return
	 */
	public Identifier getAliasIdentifier4InterfaceAliasName(String alias){
		for(Identifier identifier:getReferencedInterfaceAliasIdentifiers()){
			if(alias.equals(identifier.getName())){
				return identifier;
			}
		}
		return null;
	}
	
	/**
	 * Returns the identifier of the interface with the given name in the specification of a NesC Component.
	 * Returns null if there is no alias with the given name.
	 * @param alias
	 * @return
	 */
	public Identifier getInterfaceIdentifier4InterfaceAliasName(String alias){
		Identifier aliasIdentifier= getAliasIdentifier4InterfaceAliasName(alias);
		if(aliasIdentifier!=null){
			return getInerfaceIdentifier4InterfaceAliasIdentifier(aliasIdentifier);
		}
		return null;
	}
	
	/**
	 * Returns the name of the interface with the given name in the specification of a NesC Component.
	 * Returns null if there is no alias with the given name.
	 * @param alias
	 * @return
	 */
	public String getInterfaceName4InterfaceAliasName(String alias){
		Identifier interfaceIdentifier= getInterfaceIdentifier4InterfaceAliasName(alias);
		if(interfaceIdentifier!=null){
			return interfaceIdentifier.getName();
		}
		return null;
	}
	
	/**
	 * Checks if the given name is actually an alias, which is a rename with the NesC "as" keyword, of an interface in the specification of a NesC component.
	 * @param name
	 * @return
	 */
	public boolean isDefinedInterfaceAliasName(String name){
		Identifier alisaIdentifier=getAliasIdentifier4InterfaceAliasName(name);
		return alisaIdentifier!=null;
	}
}

	