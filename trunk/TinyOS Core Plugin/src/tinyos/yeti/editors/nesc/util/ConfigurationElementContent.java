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
package tinyos.yeti.editors.nesc.util;

import java.util.Map;

import tinyos.yeti.Debug;
import tinyos.yeti.ep.figures.ConfigurationContent;
import tinyos.yeti.ep.parser.*;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.ComponentASTModelNode;
import tinyos.yeti.nesc.RenamedASTModelNodeConnection;
import tinyos.yeti.nesc.parser.language.elements.ConfigurationElement;
import tinyos.yeti.nesc.parser.language.elements.ConnectionElement;

public class ConfigurationElementContent extends ConfigurationContent{
    public static final IGenericFactory<ConfigurationElementContent> FACTORY =
        new ReferenceFactory<ConfigurationElementContent>( ConfigurationContent.FACTORY ){
        
        public ConfigurationElementContent create(){
            return new ConfigurationElementContent();
        }
    };

    /**
     * Tries to insert an edge into that represents
     * <code>connection</code>.
     * @param base the source of the connection
     * @param connection some connection
     * @param model model of the application, might be <code>null</code>
     * @param renaming a map containing the the real name for each interface and component
     * that was in the the uses/provides/components list
     * @param path the path to the node which represents the connection, might be <code>null</code>
     */
    public void wire( ConfigurationElement base, ConnectionElement connection, ProjectModel model, Map<String, String> renaming, IASTModelPath path ){
        boolean isImplicit = base.isImplicitConnectionElement( connection );

        if ( base.isLeftInterface( connection ) && base.isRightInterface( connection )) {
            Debug.info( "left interface and right interface - ignoring" );
        }
        else if( base.isLeftInterface( connection )) {
            drawInterfaceConnection( base, connection, model, renaming, path );
        }
        else if( base.isRightInterface( connection )) {
            drawInterfaceConnection( base, connection, model, renaming, path );
        }
        else if (isImplicit) {
            resolveImplicitConnection( connection, model, renaming );
            drawImplicitConnection( connection, path );
        } 
        else { // explicit
            drawExplicitConnection( connection, path );
        }
    }

    private void drawInterfaceConnection( ConfigurationElement base, ConnectionElement ce, ProjectModel model, Map<String, String> renaming, IASTModelPath path ) {
        String leftName = ce.getLeft().getComponentElementName();
        String rightName = ce.getRight().getComponentElementName();

        if( leftName == null || rightName == null ){
            Debug.error( "Unknown reference: '" + ce.getLeft().getComponentElementName() + "' -> '" + leftName + "'" );
            Debug.error( "Unknown reference: '" + ce.getRight().getComponentElementName() + "' -> '" + rightName + "'" );
            return;
        }

        String interfaceName = null;
        String componentName = null;
        switch( ce.getOperator() ){
            case ConnectionElement.LINK_WIRES:
                interfaceName = renaming.get( leftName );
                componentName = rightName;
                break;
            case ConnectionElement.LINK_WIRES_INVERSE:
                interfaceName = renaming.get( rightName );
                componentName = leftName;
                break;
        }
        
        String text = null;
        if( interfaceName != null && componentName != null ){
            if( base.isLeftInterface( ce )){
                text = ce.getRight().getSpecificationElementName();
                if( text == null ){
                    text = resolveInterface( interfaceName, componentName, model );
                }
            }
            else{
                text = ce.getLeft().getSpecificationElementName();
                if( text == null ){
                    text = resolveInterface( interfaceName, componentName, model );
                }
            }
        }
        
        switch( ce.getOperator() ) {
            case ConnectionElement.EQUATE_WIRES:
                addWireEqual( leftName, rightName, text, false, path );
                break;
            case ConnectionElement.LINK_WIRES_INVERSE:
                addWireRightToLeft( leftName, rightName, text, false, path );
                break;
            case ConnectionElement.LINK_WIRES :
                addWireLeftToRight( leftName, rightName, text, false, path );
                break;
        }
    }
    
    private String resolveInterface( String interfaceName, String componentName, ProjectModel model ){
        IDeclaration declaration = model.getDeclaration( componentName, Kind.MODULE, Kind.CONFIGURATION );
        if( declaration == null ){
            Debug.error( "can't find declaration for '" + componentName + "'" );
            return null;
        }
        
        try{
            model.freeze( declaration.getParseFile() );
            IASTModelNode component = model.getNode( declaration, null );
            if( component == null ){
                Debug.error( "can't find node for '" + componentName + "'" );
                return null;
            }
            
            String[] names = ((ComponentASTModelNode)component).getRenamedUsesProvides( interfaceName );
            if( names.length == 0 ){
                Debug.error( "no interfaces of name '" + interfaceName + "'" );
                return null;
            }
            if( names.length > 1 ){
                Debug.error( "too many interfaces with name '" + interfaceName + "'" );
                return null;
            }
            
            return names[0];
        }
        finally{
            model.melt( declaration.getParseFile() );
        }
    }

    private void drawExplicitConnection( ConnectionElement ce, IASTModelPath path ) {
        drawImplicitConnection( ce, path );
    }

    private void drawImplicitConnection( ConnectionElement ce, IASTModelPath path ) {
        String text = null;
        if (ce.wiresFunctionEndpoints()) {
            //text = "func:"+ce.getLeft().getSpecificationElementName();
            text = "func:"+ce.getRight().getSpecificationElementName();
        }
        else {
//          text = ce.getLeft().getSpecificationElementName();
            text = ce.getRight().getSpecificationElementName();
        }

        switch (ce.getOperator()) {
            case ConnectionElement.EQUATE_WIRES:
                addWireEqual( 
                        ce.getLeft().getComponentElementName(),
                        ce.getRight().getComponentElementName(), 
                        text, 
                        true,
                        path );
                break;
            case ConnectionElement.LINK_WIRES_INVERSE:
                addWireRightToLeft(  
                        ce.getLeft().getComponentElementName(),
                        ce.getRight().getComponentElementName(), 
                        text, 
                        false,
                        path );
                break;
            case ConnectionElement.LINK_WIRES :
                addWireEqual( 
                        ce.getLeft().getComponentElementName(),
                        ce.getRight().getComponentElementName(), 
                        text, 
                        false,
                        path );
        }
    }


    private void resolveImplicitConnection( ConnectionElement element, ProjectModel model, Map<String, String> renaming ) {
        String leftName = renaming.get( element.getLeft().getComponentElementName() );
        String rightName = renaming.get( element.getRight().getComponentElementName() );
        if( leftName == null || rightName == null ){
            Debug.error( "Unknown reference: '" + element.getLeft().getComponentElementName() + "' -> '" + leftName + "'" );
            Debug.error( "Unknown reference: '" + element.getRight().getComponentElementName() + "' -> '" + rightName + "'" );
            return;
        }

        IDeclaration leftDecl = model.getDeclaration( leftName, Kind.INTERFACE, Kind.MODULE, Kind.CONFIGURATION );
        IDeclaration rightDecl = model.getDeclaration( rightName, Kind.INTERFACE, Kind.MODULE, Kind.CONFIGURATION );

        if( leftDecl == null || rightDecl == null ){
            Debug.error( "can't resolve declarations of '" + leftName + "' or '" + rightName + "'" );
            return;
        }

        try{
            model.freeze( leftDecl.getParseFile() );
            ASTModelNode leftNode = (ASTModelNode)model.getNode( leftDecl, null );

            model.freeze( rightDecl.getParseFile() );
            ASTModelNode rightNode = (ASTModelNode)model.getNode( rightDecl, null );

            if( leftNode == null || rightNode == null ){
                Debug.error( "can't resolve nodes of '" + leftName + "' or '" + rightName + "'" );
                return;
            }

            if( element.getLeft().getSpecificationElementName() == null ){
                String originalInterface = resolveImplicitConnection(
                        model, 
                        (ComponentASTModelNode)leftNode,
                        (ComponentASTModelNode)rightNode, 
                        element.getRight().getSpecificationElementName(),
                        element );
                element.getLeft().setSpecificationElementName( originalInterface );
            }
            else  {
                String originalInterface = resolveImplicitConnection(
                        model,
                        (ComponentASTModelNode)rightNode, 
                        (ComponentASTModelNode)leftNode,
                        element.getLeft().getSpecificationElementName(), 
                        element );
                element.getRight().setSpecificationElementName( originalInterface );
            }
        }
        finally{
            model.melt( leftDecl.getParseFile() );
            model.melt( rightDecl.getParseFile() );
        }
    }


    /*
     * Tries to resolve an implicit connection.
     */
    private String resolveImplicitConnection( ProjectModel model, ComponentASTModelNode implicit, ComponentASTModelNode explicit, String renamedInterface, ConnectionElement connElement ){
        if( !implicit.getTags().contains( Tag.MODULE ) && !implicit.getTags().contains( Tag.CONFIGURATION )){
            return renamedInterface + "*";
        }

        // the frozen model containing the tree for explicit and implicit
        IASTModel ast = model.getCacheModel();

        // find the specification element
        IASTModelNode exSpecifications = ast.getNode( explicit.getPath(), null, TagSet.get( ASTModel.SPECIFICATION ) );
        if( exSpecifications == null ){
            Debug.error( "Missing specification list" );
            return renamedInterface + "*";
        }

        String specificationElementName = explicit.get( renamedInterface );
        if( specificationElementName == null ){
            Debug.error( "Can't find '" + renamedInterface + "' in '" + explicit.getLabel() + "'" );
            return renamedInterface + "*";
        }
        boolean specificationIsUsed = explicit.getUses( renamedInterface ) != null;

        // the element that is referenced by the "uses/provides X as Y" clause
        IDeclaration specificationDeclaration = model.getDeclaration( specificationElementName, Kind.INTERFACE, Kind.FUNCTION );
        if( specificationDeclaration == null ){
            Debug.error( "Can't find '" + specificationElementName + "'" );
            return specificationElementName + "*";
        }
        
        IASTModelNode specificationElement = model.getNode( specificationDeclaration, null );
        
        if( specificationElement == null ){
            Debug.error( "Can't find specification element '" + renamedInterface + "' in '" + explicit.getLabel() + "'" );
            return renamedInterface + "*";
        }

        if( specificationElement.getTags().contains( Tag.FUNCTION )){
            connElement.setWiresFunctionEndpoints( true );
        }

        String[] names = implicit.getRenamedUsesProvides( specificationElement.getIdentifier() );
        if( names.length > 1 ){
            Debug.error( "Too many possibilities for '" + specificationElement.getIdentifier() + "'" );
            return specificationElement.getLabel() + "*";
        }
        if( names.length == 0 ){
            Debug.error( "Can't resolve '" + specificationElement.getIdentifier() + "'" );
            return specificationElement.getLabel() + "*";
        }

        // check correctness
        if( specificationIsUsed ){
            if( implicit.getUses( names[0] ) != null ){
                Debug.error( "Twice used" );
            }
        }
        else{
            if( implicit.getProvides( names[0] ) != null ){
                Debug.error( "Twice provided" );
            }
        }

        if( connElement.wiresFunctionEndpoints() ){
            IASTModelNode imSpecifications = ast.getNode( implicit.getPath(), null, TagSet.get( ASTModel.SPECIFICATION ) );
            if( imSpecifications == null ){
                Debug.error( "missing specification for '" + implicit.getLabel() + "'" );
            }
            else{
                IASTModelNodeConnection[] references = imSpecifications.getChildren();

                for( IASTModelNodeConnection reference : references ){
                    if( ((RenamedASTModelNodeConnection)reference).getRename().equals( specificationElement.getIdentifier() )){
                        if( reference.getTags().contains( Tag.TASK ) != specificationElement.getTags().contains( Tag.TASK ) ){
                            Debug.error( "tasks do not match" );
                        }
                        if( reference.getTags().contains( Tag.EVENT ) != specificationElement.getTags().contains( Tag.EVENT ) ){
                            Debug.error( "events do not match" );
                        }
                        if( reference.getTags().contains( Tag.COMMAND ) != specificationElement.getTags().contains( Tag.COMMAND ) ){
                            Debug.error( "commands do not match" );
                        }
                    }

                    break;
                }
            }
        }

        return names[0];
    }

}
