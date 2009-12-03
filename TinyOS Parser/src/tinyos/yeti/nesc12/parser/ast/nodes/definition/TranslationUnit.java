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
package tinyos.yeti.nesc12.parser.ast.nodes.definition;

import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.nodes.UnitModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Unit;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.util.ModifierValidator;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.util.validators.GlobalValidator;

public class TranslationUnit extends AbstractListASTNode<ExternalDeclaration> {
    public TranslationUnit(){
        super( "TranslationUnit" );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented

        if( stack.isReportErrors() ){
            stack.put( ModifierValidator.MODIFIER_VALIDATOR, new GlobalValidator() );
        }
        
        stack.getDeclarationStack().push( stack.getParseFile().getPath() );
        

        if( stack.isCreateModel() ){
            UnitModelNode unitNode = new UnitModelNode( stack.getParseFile(), stack.getParser().getPreprocessorReader().getBaseFileLength() );
            NodeStack nodes = stack.getNodeStack();
            
            nodes.pushNode( unitNode );
            super.resolve( stack );
            stack.checkCancellation();
            nodes.setRange( getRange() );
            nodes.popNode( null );
            
            resolved( "node", unitNode );
        }
        else{
            super.resolve( stack );
            stack.checkCancellation();
        }

        stack.getDeclarationStack().pop();
        
        if( stack.isReportErrors() ){
            stack.remove( ModifierValidator.MODIFIER_VALIDATOR );
        }
    }

    public Unit resolve( BindingResolver bindings ){
        UnitModelNode unit = resolved( "node" );
        if( unit == null )
            return null;
        return unit.resolve( bindings );
    }

    /**
     * Creates a momentary snapshot of this unit. The snapshot is an abstract
     * view of the whole application. It will expand lazily whenever an new
     * query is asked.
     * @return a new abstract view of this unit
     */
    public Unit resolve(){
        UnitModelNode unit = resolved( "node" );
        if( unit == null )
            return null;
        return unit.resolve();
    }

    public TranslationUnit( ExternalDeclaration child ){
        this();
        add( child );
    }

    @Override
    public TranslationUnit add( ExternalDeclaration node ) {
        super.add( node );
        return this;
    }

    @Override
    protected void checkChild( ExternalDeclaration child ) throws ASTException {

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
