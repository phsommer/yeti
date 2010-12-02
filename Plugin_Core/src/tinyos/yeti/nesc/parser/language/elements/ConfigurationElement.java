/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.nesc.parser.language.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.editors.nesc.util.ConfigurationElementContent;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.figures.LazyContent;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.parser.standard.ASTModelLeaf;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.ep.parser.standard.FileRegion;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.ComponentASTModelNode;
import tinyos.yeti.nesc.RenamedASTModelNodeConnection;
import tinyos.yeti.nesc.scanner.Token;
import tinyos.yeti.utility.Icon;



public class ConfigurationElement extends Element {

    private Hashtable<String,RenamedIdentifierElement> components;

    private Hashtable<String,ISpecificationElement> usesprovides;

    public ConfigurationElement(String string, Token t, Element e) {
        super(string,t,e);
        image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_CONFIGURATION);
    }

    public boolean isFoldable() {
        return true;
    }

    public ConfigurationImplElement getConfigurationImplementation() {
        return (ConfigurationImplElement) extractClass(children, ConfigurationImplElement.class);
    }

    public SpecificationListElement getSpecification() {
        return (SpecificationListElement) extractClass(children, SpecificationListElement.class);
    }

    private LazyContent toContent( ProjectModel model, String name, Tag tag, Kind... kind ){
        IDeclaration declaration = model.getDeclaration( name, kind );
        if( declaration == null ){
            return new LazyContent( name, new Icon( TagSet.get( tag ) ), null );
        }
        else{
            return new LazyContent( declaration.getName(), new Icon( declaration ), declaration.getPath() );
        }
    }
    
    @Override
    public void toNode( ASTModelNode parent, ProjectModel project, ASTModel model, IParseFile file ){
	final ConfigurationElementContent content = new ConfigurationElementContent();
	final Map<String, String> renaming = new HashMap<String, String>();
	
        ComponentASTModelNode node = new ComponentASTModelNode( parent, getName(), getName(), getLabel( this ), file,
                new IFileRegion[]{ new FileRegion( getPositionForOutline(), getLine(), file ) }, 
                Tag.CONFIGURATION, Tag.COMPONENT, Tag.OUTLINE, Tag.FIGURE );

        if( !model.addNode( node ) )
            return;

        // Specification: uses and provides interfaces
// TODO check also used/provided functions, AND SET THE FUNCTION-TAG
        ASTModelNode specification = new ASTModelNode( node, node.getIdentifier() + ".specification", null, "Specification", file, null, ASTModel.SPECIFICATION );
        if( model.addNode( specification ) )
            node.addChild( specification );
        
        TagSet usesInterface = TagSet.get( Tag.INTERFACE, Tag.USES, Tag.AST_CONNECTION_ICON_RESOLVE, Tag.AST_CONNECTION_LABEL_RESOLVE );
        TagSet usesComponent = TagSet.get( Tag.COMPONENT, Tag.USES );
        TagSet providesInterface = TagSet.get( Tag.INTERFACE, Tag.PROVIDES, Tag.AST_CONNECTION_ICON_RESOLVE, Tag.AST_CONNECTION_LABEL_RESOLVE );
        TagSet providesComponent = TagSet.get( Tag.COMPONENT, Tag.PROVIDES );
        
        SpecificationListElement sle = (SpecificationListElement)extractClass( children, SpecificationListElement.class );
        Iterator<Element> sleChildren = sle.getChildren().iterator();
        while( sleChildren.hasNext() ){
            Element sleChild = sleChildren.next();
            if( sleChild instanceof SpecificationElement ){
                SpecificationElement se = (SpecificationElement)sleChild;
                
                String name = se.getName();
                String rename = se.getRenamed();
                
                String label = name;
                
                if( rename != null && !rename.equals( name )){
                    label = rename + " (" + name + ")";
                    renaming.put( rename, name );
                }
                else{
                    renaming.put( name, name );
                }
                
                if( se.isProvides() ){
                    ASTModelNode module = (ASTModelNode)model.getNode( null, name, providesComponent );
                    if( module == null ){
                        module = new ASTModelNode( null, name, name, name, file, null, providesComponent );
                        model.addNode( module );
                    }
                    module.addReference( node.getIdentifier(), node.getLabel(), null, node.getTags() );
                    RenamedASTModelNodeConnection reference = new RenamedASTModelNodeConnection( specification, true, name, rename, label, null, providesInterface );
                    specification.addConnection( reference );
                    content.addProvides( toContent( project, name, Tag.INTERFACE, Kind.INTERFACE ), rename == null ? name : rename );
                    node.addProvides( name, rename == null ? name : rename );
                }
                else{ // uses
                    ASTModelNode module = (ASTModelNode)model.getNode( null, name, usesComponent );
                    if( module == null ){
                        module = new ASTModelNode( null, name, name, name, file, null, usesComponent );
                        model.addNode( module );
                    }
                    module.addReference( node.getIdentifier(), node.getLabel(), null, node.getTags() );
                    RenamedASTModelNodeConnection reference = new RenamedASTModelNodeConnection( specification, true, name, rename, label, null, usesInterface );
                    specification.addConnection( reference );
                    content.addUses( toContent( project, name, Tag.INTERFACE, Kind.INTERFACE ), rename == null ? name : rename );
                    node.addUses( name, rename == null ? name : rename );
                }
            }
        }
        
        // implementation: includes components and wires them
        ASTModelNode implementation= new ASTModelNode( node, node.getIdentifier() + ".implementation", null, "Implementation", file, null, ASTModel.IMPLEMENTATION );
        if( model.addNode( implementation ) )
            node.addChild( implementation );
        
        // check components
        Hashtable<String, RenamedIdentifierElement> componentNames = getComponentNames();
        ConnectionListElement list = getConfigurationImplementation().getConnectionList();
        ConnectionElement[] connections = null;
        if( list != null )
            connections = list.getConnections();

        if( (components != null && components.size() > 0) || (connections != null && connections.length > 0) ){
            if( components != null && components.size() > 0 ){
                ASTModelNode components = new ASTModelNode( implementation, implementation.getIdentifier() + ".components", null, "Components", file, null, ASTModel.COMPONENTS );
                if( model.addNode( components ) )
                    implementation.addChild( components );

                for( Map.Entry<String, RenamedIdentifierElement> entry : componentNames.entrySet() ){
                    TagSet tags = TagSet.get( Tag.COMPONENT, Tag.AST_CONNECTION_ICON_RESOLVE, Tag.AST_CONNECTION_LABEL_RESOLVE );

                    String name = entry.getValue().getName();
                    String renamed = entry.getValue().getRenamed();
                    String label;
                    if( renamed != null && !name.equals( renamed )){
                        label = renamed + " (" + name + ")";
                        tags.add( Tag.RENAMED );
                        renaming.put( renamed, name );
                    }
                    else{
                        label = name;
                        renaming.put( name, name );
                    }

                    
                    RenamedASTModelNodeConnection reference = 
                        new RenamedASTModelNodeConnection( components, true, 
                                name, renamed, label, 
                                new IFileRegion[]{ new FileRegion( entry.getValue().getPositionForOutline(), getLine(), file ) },
                                tags );
                    
                    components.addConnection( reference );
                    content.addComponent( toContent( project, name, Tag.COMPONENT, Kind.MODULE, Kind.CONFIGURATION ), renamed == null ? name : renamed );
                }
            }
        }
        
        // check wiring
        if( connections != null && connections.length > 0 ){
            ASTModelNode connection = new ASTModelNode( implementation, node.getIdentifier() + ".connection", null, "Connections", file, null, ASTModel.CONNECTIONS );
            if( model.addNode( connection )){
                implementation.addChild( connection );
                int index = 0;

                for( ConnectionElement element : connections ){
                    String left = element.getLeft().getLabel( element.getLeft() );
                    String right = element.getRight().getLabel( element.getRight() );
                    int operator = element.getOperator();

                    String label = null;
                    TagSet tags = TagSet.get( Tag.CONNECTION );
                    
                    String leftBegin, leftEnd;
                    String rightBegin, rightEnd;
                    
                    int point = left.indexOf( '.' );
                    if( point > 0 ){
                        leftBegin = left.substring( 0, point );
                        leftEnd = left.substring( point+1 );
                    }
                    else{
                        leftBegin = left;
                        leftEnd = null;
                    }
                    
                    point = right.indexOf( '.' );
                    if( point > 0 ){
                        rightBegin = right.substring( 0, point );
                        rightEnd = right.substring( point+1 );
                    }
                    else{
                        rightBegin = right;
                        rightEnd = null;
                    }
                    
                    switch( operator ){
                        case ConnectionElement.LINK_WIRES:
                            label = left + " -> " + right;
                            tags.add( Tag.CONNECTION_RIGHT );
                            // content.addWireLeftToRight( leftBegin, rightBegin );
                            break;
                        case ConnectionElement.LINK_WIRES_INVERSE:
                            label = left + " <- " + right;
                            tags.add( Tag.CONNECTION_LEFT );
                            // content.addWireRightToLeft( leftBegin, rightBegin );
                            break;
                        case ConnectionElement.EQUATE_WIRES:
                            label = left + " = " + right;
                            tags.add( Tag.CONNECTION_BOTH );
                            // content.addWireEqual( leftBegin, rightBegin );
                            break;
                    }

                    IASTModelPath path = null;
                    
                    if( label != null ){
                        IFileRegion[] region = new IFileRegion[]{ new FileRegion( element.getPositionForOutline(), getLine(), file ) };
                        IASTModelNode wire = new ASTModelLeaf( connection, connection.getIdentifier() + "." + (index++), null, label, file, region, tags );
                        model.addNode( wire );
                        connection.addChild( wire );
                        path = wire.getPath();
                    }
                    
                    content.wire( this, element, project, renaming, path );
                }
            }
        }
        node.setContent( content );
    }
    

    /**
     * tests if the connection has an implicit wiring, e.g.
     * a . x <-> b . y
     * x or y or both are missing and that a and b are not
     * an external specification element
     * @param c the ConnectionElement
     * @return true if its explicit, false otherwise
     */
    public boolean isImplicitConnectionElement(ConnectionElement c) {
        EndpointElement left = c.getLeft();
        EndpointElement right = c.getRight();

        String left1 = left.getComponentElementName();
        String left2 = left.getSpecificationElementName();

        String right1 = right.getComponentElementName();
        String right2 = right.getSpecificationElementName();

        // has one ident..
        boolean htiLeft = (left2== null) || (left2.equals(left1));
        boolean htiRight = (right2== null) || (right2.equals(right1));	

        // is external specification element means 
        //		that name is in usesprovides list
        boolean extLeft = !getUsesProvidesNames().containsKey(left1);
        boolean extRight = !getUsesProvidesNames().containsKey(right1);

        boolean implicitLeft;
        boolean implicitRight;

        implicitLeft =  htiLeft&&extLeft;
        implicitRight = htiRight&&extRight;

        return (implicitLeft || implicitRight);
    }

    private Hashtable<String,ISpecificationElement> getUsesProvidesNames() {
        if (usesprovides!=null) return usesprovides;
        usesprovides = new Hashtable<String,ISpecificationElement>();

        List<ISpecificationElement> l = getSpecification().getChildren();
        Iterator iter = l.iterator();

        if (iter.hasNext()) {
            while(iter.hasNext()) {
                ISpecificationElement temp = (ISpecificationElement) iter.next();
                if (temp instanceof SpecificationElement) {
                    usesprovides.put(((SpecificationElement)temp).getRenamed(),temp);					
                } else if (temp instanceof DeclarationElement) {
                    usesprovides.put(((DeclarationElement)temp).getFunctionName(),temp);				
                } else {
                    System.err.println("getUsesProvidesNames() cannot determine type");
                }
            }			
        }

        return usesprovides;
    }

    private Hashtable<String,RenamedIdentifierElement> getComponentNames() {
        if (components!=null) return components;

        try {
            List<RenamedIdentifierElement> l = getConfigurationImplementation().getComponentList().getChildren();

            Iterator iter = l.iterator();
            if (iter.hasNext()) {
                components = new Hashtable<String,RenamedIdentifierElement>();	
            }
            while(iter.hasNext()) {
                RenamedIdentifierElement temp = (RenamedIdentifierElement) iter.next();
                components.put(temp.getRenamed(),temp);
            }

        } catch (NullPointerException e) {

        }
        return components;
    }

    /*
     * resolves implicit connection
     * if a file could not be parsed, it's assumed the interface exists 
     * and it's not renamed. this guesses are marked with a "*" as suffix 
     *
     * @param implicit
     * @param explicit
     * @param renamedInterface
     * @param e 
     * @return name of corresponding interface
     * @throws FunctionNotFoundException 
     * @throws MultipleFunctionsFoundException 
     *
    private String resolveImplicitConnection(NesCModel implicit, NesCModel explicit, String renamedInterface, ConnectionElement connElement) throws IncompatibleWiringStatementException,InterfaceNotFoundException, MultipleInterfaceFoundException, FunctionNotFoundException, MultipleFunctionsFoundException {

        // specificationelements can be renamed.. extract the element 
        ISpecificationElement seExplicit = null;
        if (explicit.type == NesCModel.CONFIGURATION) {
            seExplicit = ((ConfigurationElement)explicit.getTypeElement()).getSpecificationElement(renamedInterface);
        } else if (explicit.type == NesCModel.MODULE) {
            seExplicit = ((ModuleElement)explicit.getTypeElement()).getSpecificationElement(renamedInterface);
        } else if (explicit.type == NesCModel.UNDEFINED) {
            // Error parsing the file
            // return as if the file had same interface name (assume that its is correct)
            // 	Example: TimerM.PowerManagement -> HPLPowerManagementM;
            //		return PowerManagement
            return (renamedInterface+"*");		
        }


        // set function flag
        if (seExplicit instanceof DeclarationElement) {
            connElement.setWiresFunctionEndpoints(true);
        }

        // extract specification list 
        SpecificationListElement sle = null;
        if (implicit.type == NesCModel.CONFIGURATION) {
            sle = ((ConfigurationElement) implicit.getTypeElement()).getSpecification();
        } else if (implicit.type == NesCModel.MODULE) {
            sle = ((ModuleElement) implicit.getTypeElement()).getSpecification();
        } else if (implicit.type == NesCModel.UNDEFINED) {
            // Error parsing the file
            // return as if the file had same interface name (assume that its is correct)
            // 	Example: TimerM.PowerManagement -> HPLPowerManagementM;
            //		return PowerManagement
            return (seExplicit.getName()+"*");			
        }

        // check if one specification matches.. 
        Iterator iter = sle.getChildren().iterator();
        int count = 0;
        String interfaceName = null;

        while(iter.hasNext()) {
            ISpecificationElement se = (ISpecificationElement) iter.next();
            if (se.getName().equals(seExplicit.getName())) {
                if ((se.isProvides()) == (!seExplicit.isProvides())) {
                    interfaceName = se.getName();	
                    count++;
                } else {
                    throw new IncompatibleWiringStatementException();
                }
            }
        }
        // reset counter


        // if there are function elements / if only one ist matching (the name doesn't bother)
        // .. then ok
        if (connElement.wiresFunctions) {
            count = 0;

            iter = sle.getChildren().iterator();			
            while(iter.hasNext()) {
                ISpecificationElement se = (ISpecificationElement) iter.next();
                if (se instanceof DeclarationElement) {
                    if ((se.isProvides()) == (!seExplicit.isProvides())) {
                        if (((DeclarationElement)se).getType() ==  
                            ((DeclarationElement)seExplicit).getType()) {

                            interfaceName = se.getName();	
                            count++;
                        }
                    }
                }
            }
        }

        if (!connElement.wiresFunctions) {
            if (count == 0) throw new InterfaceNotFoundException();
            if (count > 1) throw new MultipleInterfaceFoundException();
        } else {
            if (count == 0) throw new FunctionNotFoundException();
            if (count > 1) throw new MultipleFunctionsFoundException();
        }


        return interfaceName;
    }

    public class MultipleFunctionsFoundException extends Exception {

    }

    public class MultipleInterfaceFoundException extends Exception {

    }
    public class FunctionNotFoundException extends Exception {

    }
    public class InterfaceNotFoundException extends Exception {

    }

    public class IncompatibleWiringStatementException extends Exception {

    }*/
/*
    public ConnectionElement resolveImplicitConnection(Hashtable<String, RenamedIdentifierElement> modulesHashList, ConnectionElement e) throws IncompatibleWiringStatementException, InterfaceNotFoundException, MultipleInterfaceFoundException, FunctionNotFoundException, MultipleFunctionsFoundException {

        String left1 = e.getLeft().getComponentElementName(); 
        String right1 = e.getRight().getComponentElementName();

        if (modulesHashList.containsKey(left1)) {
            left1 = modulesHashList.get(left1).getName();
        }
        if (modulesHashList.containsKey(right1)) {
            right1 = modulesHashList.get(right1).getName();
        }

        NesCModel leftModel = TinyOSPlugin.getDefault().getModel(left1);
        NesCModel rightModel = TinyOSPlugin.getDefault().getModel(right1);

        String originalInterface = null;
        if (e.getLeft().getSpecificationElementName() == null) {
            originalInterface = resolveImplicitConnection(leftModel,rightModel,e.getRight().getSpecificationElementName(),e);
            e.getLeft().setSpecificationElementName(originalInterface);
        } else {
            originalInterface = resolveImplicitConnection(rightModel, leftModel,e.getLeft().getSpecificationElementName(),e);
            e.getRight().setSpecificationElementName(originalInterface);
        }

        return e;


    }*/

    ISpecificationElement getSpecificationElement(String renamedInterfaceName) {
        SpecificationListElement sle = getSpecification();
        Iterator iter = sle.getChildren().iterator();

        while(iter.hasNext()) {
            ISpecificationElement se = (ISpecificationElement) iter.next();
            if (se.getRenamed().equals(renamedInterfaceName)) {
                return se;
            }
        }
        return null;
    }

    protected final int PROVIDED_INTERFACE = 0;
    protected final int USED_INTERFACE = 1;
    protected final int UNDECLARED = 2;
 /*   private int resolveUsesProvides(NesCModel m, String interfaceName) {
        String[] provided = null;
        String[] used = null;

        if (m.type == NesCModel.CONFIGURATION) {
            ConfigurationElement ce = (ConfigurationElement) m.getTypeElement();
            provided = ce.getProvidedInterfaces();
            used = ce.getUsedInterfaces();

        } else if (m.type == NesCModel.MODULE) {
            ModuleElement mod = (ModuleElement) m.getTypeElement();
            provided = mod.getProvidedInterfaces();
            used = mod.getUsedInterfaces();
        }

        if (provided != null) {
            for (int i = 0; i < provided.length; i++) {
                String string = provided[i];
                if (string.equals(interfaceName)) {
                    return PROVIDED_INTERFACE;
                }
            }
        }
        if (used != null) {
            for (int i = 0; i < used.length; i++) {
                String string = used[i];
                if (string.equals(interfaceName)) {
                    return USED_INTERFACE;
                }
            }
        }


        return UNDECLARED;
    }*/

    public String[] getUsedInterfaces() {
        SpecificationListElement sle = (SpecificationListElement) extractClass(children,SpecificationListElement.class);

        Iterator iter = sle.children.iterator();
        ArrayList implemented = new ArrayList();
        while(iter.hasNext()) {
            Element element = (Element) iter.next();
            if (!(element instanceof SpecificationElement)) continue;
            SpecificationElement se = (SpecificationElement) element;
            if (!se.isProvides()) {
                implemented.add(se.getName());
            }
        }
        return (String[]) implemented.toArray(new String[implemented.size()]);
    }

    public String[] getProvidedInterfaces() {
        SpecificationListElement sle = (SpecificationListElement) extractClass(children,SpecificationListElement.class);

        Iterator iter = sle.children.iterator();
        ArrayList provides = new ArrayList();
        while(iter.hasNext()) {
            Element element = (Element) iter.next();
            if (!(element instanceof SpecificationElement)) continue;
            SpecificationElement se = (SpecificationElement) element;
            if (se.isProvides()) {
                provides.add(se.getName());
            }
        }
        return (String[]) provides.toArray(new String[provides.size()]);
    }

    public boolean isLeftInterface(ConnectionElement ce) {
        if (components.containsKey(ce.getLeft().getComponentElementName())) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isRightInterface(ConnectionElement ce) {
        if (components.containsKey(ce.getRight().getComponentElementName())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 
     * @param ce
     * @return
     */
    public boolean hasExternalInterfaces(ConnectionElement ce) {
        return getComponentNames().containsKey(ce.getLeft().getComponentElementName()) ||
        getComponentNames().containsKey(ce.getRight().getComponentElementName());
    }

    public ISpecificationElement getSpecification(String renamedName) {
        SpecificationListElement sle = getSpecification();
        Iterator iter = sle.getChildren().iterator();
        while(iter.hasNext()) {
            ISpecificationElement e = (ISpecificationElement)iter.next();
            if (e.getRenamed().equals(renamedName)) {
                return e;
            }
        }
        return null;
    }

    public SpecificationElement[] getInterfaces() {
        SpecificationListElement sle = (SpecificationListElement) extractClass(children,SpecificationListElement.class);

        Iterator iter = sle.children.iterator();
        ArrayList<SpecificationElement> implemented = new ArrayList<SpecificationElement>();
        while(iter.hasNext()) {
            Element element = (Element) iter.next();
            if (!(element instanceof SpecificationElement)) continue;
            SpecificationElement se = (SpecificationElement) element;
            implemented.add(se);
        }
        return (SpecificationElement[])implemented.toArray(new SpecificationElement[implemented.size()]);
    }

//  public void testsomething() {
//  ConnectionListElement cle = getConfigurationImplementation().getConnectionList();
//  List list = cle.getChildren(); // ConnectionElements

//  Iterator iter = list.iterator();
//  while(iter.hasNext()) {	
//  ConnectionElement e = (ConnectionElement) iter.next();
//  if (isImplicitConnectionElement(e)) {
//  // switch left1, left2, right1,right2 accordingly
//  System.out.println("Implicit Wiring :"+e.getLabel(null));
//  } 
//  }
//  }

}
