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
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

/**
 * A list containing {@link Access}es or {@link Datadef}s.
 * @author Benjamin Sigg
 */
public class AccessList extends AbstractListASTNode<ASTNode>{
    public AccessList(){
        super( "AccessList" );
    }
    
    public AccessList( ASTNode child ){
        this();
        add( child );
    }
    
    public ModelNode resolveModel(){
    	return resolved( "model" );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        if( stack.isCreateModel() ){
            ModelNode specification = new StandardModelNode( "specification", false, ASTModel.SPECIFICATION );
            stack.put( Configuration.SPECIFICATION_NODE, specification );
            specification.setLabel( "Specification" );
            
            NodeStack nodes = stack.getNodeStack();
            
            nodes.include( specification, this );
            nodes.addChild( specification, null );
            nodes.pushNode( specification );
            nodes.setRange( getRange() );
            
            super.resolve( stack );

            resolved( "model", specification );
            nodes.popNode( null );
        }
        else{
            super.resolve( stack );
        }
    }
    
    @Override
    public AccessList add( ASTNode node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( ASTNode child ) throws ASTException {
        if( !(child instanceof Access) && !(child instanceof Datadef ))
            throw new ASTException( child, "Must be an Access or Datadef" );
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
