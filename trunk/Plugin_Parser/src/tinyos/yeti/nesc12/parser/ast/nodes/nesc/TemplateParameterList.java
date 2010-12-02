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

import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class TemplateParameterList extends AbstractListASTNode<TemplateParameter>{
    public TemplateParameterList(){
        super( "TemplateParameterList" );
    }
    
    public TemplateParameterList( TemplateParameter child ){
        this();
        add( child );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        if( stack.isCreateModel() && getChildrenCount() > 0 ){
            ModelNode node = new StandardModelNode( "generic.template.parameters",
                    false, NesC12ASTModel.PARAMETERS );
            node.setLabel( "Parameters" );
            
            NodeStack nodes = stack.getNodeStack();
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
    public TemplateParameterList add( TemplateParameter node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( TemplateParameter child ) throws ASTException {
        
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
