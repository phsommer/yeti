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
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class ComponentList extends AbstractListASTNode<Component> implements ConfigurationDeclaration{
    public ComponentList(){
        super( "ComponentList" );
    }
    
    public ComponentList( Component child ){
        this();
        add( child );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        if( stack.isCreateModel() ){
            ModelNode components = stack.get( ConfigurationDeclarationList.COMPONENT_NODE );
            NodeStack nodes = stack.getNodeStack();
            nodes.pushNode( components );
            nodes.setRange( getRange() );
            super.resolve( stack );
            stack.checkCancellation();
            nodes.popNode( null );
        }
        else{
            super.resolve( stack );
        }
    }
    
    public void reportComponents(Collection<Component> components) {
    	for( int i = 0, n = getChildrenCount(); i<n; i++ ){
    		Component component = getNoError( i );
    		if( component != null )
    			components.add( component );
    	}
    }
    
    @Override
    public ComponentList add( Component node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( Component child ) throws ASTException {
        
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
