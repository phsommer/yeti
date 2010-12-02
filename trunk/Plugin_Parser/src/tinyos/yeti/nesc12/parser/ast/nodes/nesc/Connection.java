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

import java.util.Collection;

import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.ConfigurationModelNode;
import tinyos.yeti.nesc12.ep.nodes.ConnectionModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.visitors.ASTPrinterVisitor;

public class Connection extends AbstractFixedASTNode implements ConfigurationDeclaration{
    public static final String LEFT = "left";
    public static final String WIRE = "wire";
    public static final String RIGHT = "right";
    
    public static final Key<ConfigurationModelNode> CONFIGURATION = new Key<ConfigurationModelNode>( "configuration" );
    
    public Connection(){
        super( "Connection", "left", "wire", "right" );
    }
    
    public Connection( Endpoint left, Wire wire, Endpoint right ){
        this();
        setLeftEndpoint( left );
        setWire( wire );
        setRightEndpoint( right );
    }
    
    public Connection( ASTNode left, ASTNode wire, ASTNode right ){
        this();
        setField( LEFT, left );
        setField( WIRE, wire );
        setField( RIGHT, right );
    }
    
    public ConnectionModelNode resolveConnection(){
        return resolved( "node" );
    }
    
    @Override
    public void resolve( final AnalyzeStack stack ) {
        // TODO method not implemented
        
        if( stack.isCreateFullModel() || (stack.isCreateModel() && stack.isReportErrors()) ){
            ASTPrinterVisitor visitor = new ASTPrinterVisitor();
            accept( visitor );
            
            ModelNode parent = stack.get( ConfigurationDeclarationList.CONNECTION_NODE );
            
            NodeStack nodes = stack.getNodeStack();
            
            final ConnectionModelNode node = new ConnectionModelNode( "connection." + visitor.toString() );
            
            nodes.include( node, this );
            
            nodes.pushNode( parent );
            nodes.addChild( node, this );
            
            nodes.pushNode( node );
            nodes.setRange( getRange() );
            nodes.addLocation( this );
            
            super.resolve( stack );
            stack.checkCancellation();
            
            resolved( "node", node );
            final ConfigurationModelNode configuration = stack.get( CONFIGURATION );
            if( configuration != null ){
            	nodes.executeOnPop( new Runnable(){
            		public void run(){
            			node.resolve( configuration, Connection.this, stack );		
            		}
            	}, 2 );
            }
            
            nodes.popNode( null );
            nodes.popNode( null );
        }
        else{
            super.resolve( stack );
        }
    }
    
    public void reportComponents(Collection<Component> components) {
    	// ignore
    }
    
    public void setLeftEndpoint( Endpoint left ){
        setField( 0, left );
    }
    public Endpoint getLeftEndpoint(){
        return (Endpoint)getNoError( 0 );
    }
    
    public void setWire( Wire wire ) {
        setField( 1, wire );
    }
    public Wire getWire() {
        return (Wire)getNoError( 1 );
    }
    
    public void setRightEndpoint( Endpoint right ){
        setField( 2, right );
    }
    public Endpoint getRightEndpoint(){
        return (Endpoint)getNoError( 2 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 1 ) {
            if( !( node instanceof Wire ) )
                throw new ASTException( node, "Must be a Wire" );
        }
        if( index == 0 || index == 2 ){
            if( !( node instanceof Endpoint ) )
                throw new ASTException( node, "Must be an Endpoint" );
        }
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
