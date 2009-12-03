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

import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class NesCExternalDefinitionList extends AbstractListASTNode<NesCExternalDefinition>{
    /** if this flag is set, then this list will create a new {@link ModelNode} for an implementation block */
    public static final Flag IMPLEMENTATION = new Flag( "create implementation node" );
    
    public NesCExternalDefinitionList(){
        super( "NesCExternalDefinitionList" );
    }

    public NesCExternalDefinitionList( NesCExternalDefinition child ){
        this();
        add( child );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        if( stack.isCreateModel() && stack.present( IMPLEMENTATION )){
            NodeStack nodes = stack.getNodeStack();
            ModelNode node = new StandardModelNode( "implementation", false, ASTModel.IMPLEMENTATION, ASTModel.MODULE_IMPLEMENTATION );	
            
            node.setLabel( "Implementation" );
            
            nodes.include( node, this );
            nodes.addChild( node, this );
            nodes.pushNode( node );
            nodes.setRange( getRange() );
            super.resolve( stack );
            stack.checkCancellation();
            nodes.popNode( null );
        }
        else{
            super.resolve( stack );
        }
    }
    
    @Override
    public NesCExternalDefinitionList add( NesCExternalDefinition node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( NesCExternalDefinition child ) throws ASTException {
        
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
