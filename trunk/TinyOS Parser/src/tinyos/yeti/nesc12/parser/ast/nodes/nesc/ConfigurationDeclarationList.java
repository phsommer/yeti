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
package tinyos.yeti.nesc12.parser.ast.nodes.nesc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class ConfigurationDeclarationList extends AbstractListASTNode<ConfigurationDeclaration>{

    public static final Key<ModelNode> COMPONENT_NODE = new Key<ModelNode>( "components" );
    public static final Key<ModelNode> CONNECTION_NODE = new Key<ModelNode>( "connections" );

    public static final Key<Integer> IMPLEMENTATION_START = new Key<Integer>( "configurationDeclarationList.start" );
    
    public ConfigurationDeclarationList(){
        super( "ConfigurationDeclarationList" );
    }
    
    public ConfigurationDeclarationList( ConfigurationDeclaration child ){
        this();
        add( child );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        if( stack.isCreateModel() ){
            NodeStack nodes = stack.getNodeStack();
            
            ModelNode implementation = new StandardModelNode( "implementation", false, ASTModel.IMPLEMENTATION, ASTModel.CONFIGURATION_IMPLEMENTATION );
            implementation.setLabel( "Implementation" );

            nodes.include( implementation, this );
            nodes.addChild( implementation, null );
            nodes.pushNode( implementation );
            nodes.setRange( getRange() );
            nodes.preventChildrenClose( 2 );
            
            ModelNode components = new StandardModelNode( "components", false, ASTModel.COMPONENTS );
            components.setLabel( "Components" );
            components.getTags().add( implementation.getTags(), Tag.INCLUDED );
            stack.put( COMPONENT_NODE, components );
            nodes.addChild( components, null );
            
            ModelNode connections = new StandardModelNode( "connections", false, ASTModel.CONNECTIONS );
            connections.setLabel( "Connections" );
            connections.getTags().add( implementation.getTags(), Tag.INCLUDED );
            stack.put( CONNECTION_NODE, connections );
            nodes.addChild( connections, null );
            
            stack.put( IMPLEMENTATION_START, getRange().getLeft() );
            
            super.resolve( stack );
            stack.checkCancellation();
            
            stack.remove( IMPLEMENTATION_START );
            
            if( stack.isReportErrors() ){
            	checkNoRedefinition( stack );
            }
            
            nodes.popNode( null );
            stack.put( COMPONENT_NODE, null );
            stack.put( CONNECTION_NODE, null );
        }
        else{
            super.resolve( stack );
        }
    }
    
    private void checkNoRedefinition( AnalyzeStack stack ){
    	// collect
    	List<Component> components = new ArrayList<Component>();
    	
    	for( int i = 0, n = getChildrenCount(); i<n; i++ ){
    		ConfigurationDeclaration child = getNoError( i );
    		if( child != null ){
    			child.reportComponents( components );
    		}
    	}
    	
    	// find errors
    	Set<String> seenOnce = new HashSet<String>();
    	Set<String> seenTwice = new HashSet<String>();
    	
    	for( Component component : components ){
    		String name = component.getFinalName();
    		if( name != null ){
    			if( !seenOnce.add( name )){
    				seenTwice.add( name );
    			}
    		}
    	}
    	
    	// report errors
    	for( String name : seenTwice ){
    		List<ASTNode> location = new ArrayList<ASTNode>();
    		
    		for( Component check : components ){
    			if( name.equals( check.getFinalName() )){
    				location.add( check.getFinalNameNode() );
    				stack.getNodeStack().putErrorFlag( check.resolveConnection(), 0 );
    			}
    		}
    		
    		stack.error( "Components with the same name: '" + name + "'",
    				location.toArray( new ASTNode[ location.size() ]));
    	}
    }
    
    /*
    
    public static void checkExistence( Component component, AnalyzeStack stack ){
    	String name = component.getFinalName();
    	Map<String, Component> map = stack.get( COMPONENTS );
    	
    	Component previous = map.get( "f" + name );
    	if( previous != null ){
    		stack.error( "Redefinition of component '" + name + "'", component.getFinalNameNode(), previous.getFinalNameNode() );
    		stack.getNodeStack().putErrorFlag( previous.resolveConnection() );
    		map.remove( "f" + name );
    		map.put( "s" + name, component );
    	}
    	else{
    		previous = map.get( "s" + name );
    		if( previous != null ){
    			stack.error( "Redefinition of component '" + name + "'", component.getFinalNameNode() );
    		}
    		else{
    			map.put( "f" + name, component );
    		}
    	}
    }*/
    
    @Override
    public ConfigurationDeclarationList add( ConfigurationDeclaration node ) {
        super.add( node );
        return this;
    }

    @Override
    protected void checkChild( ConfigurationDeclaration child ) throws ASTException {
        
    }

    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }
}
