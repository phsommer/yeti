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
package tinyos.yeti.nesc12.parser.ast.nodes.declaration;

import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.util.DeclarationStack;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class DataObjectFieldDeclarator extends AbstractFixedASTNode{
    public static final String DECLARATOR = "declarator";
    public static final String EXPRESSION = "expression";
    
    public DataObjectFieldDeclarator(){
        super( "DataObjectFieldDeclarator", DECLARATOR, EXPRESSION );
    }
    
    public DataObjectFieldDeclarator( ASTNode declarator, ASTNode expression ){
        this();
        setField( DECLARATOR, declarator );
        setField( EXPRESSION, expression );
    }
    
    public DataObjectFieldDeclarator( Declarator declarator, Expression expression ){
        this();
        setDeclarator( declarator );
        setExpression( expression );
    }
    
    public Field resolveField(){
        return resolved( "field" );
    }
    
    /**
     * Tries to resolve the field that is described by this declarator.
     * @param base the type an unmodified field would have
     * @param stack used to report errors when necessary
     * @return the field or <code>null</code> if this does not describe a field
     */
    public Field resolveField( Type base, AnalyzeStack stack ){
        String id = "field";
        
        if( isResolved( id ))
            return resolved( id );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return resolved( id, null );
        
        Name name = decl.resolveName();
        Type type = decl.resolveType( base, stack );
        
        LazyRangeDescription range = null;
        if( name == null ){
            range = new LazyRangeDescription( decl, stack.getParser() );
        }
        
        DeclarationStack declarations = stack.getDeclarationStack();
        declarations.push( FieldModelNode.fieldId( name, type ) );
        Field field = declarations.set( null, type, name, null, null, range );
        declarations.pop();
        return resolved( id, field );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        // int x : 3, 3 is the size of the field, must be at least 1
        if( stack.isCreateFullModel() ){
        	NodeStack nodes = stack.getNodeStack();
        	nodes.pushNode( null );
        	nodes.setRange( getRange() );
        }
        
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isCreateFullModel() ){
            Type base = stack.getLevel( DataObjectFieldDeclaration.BASE_TYPE );
            Declarator decl = getDeclarator();
            Name name = decl == null ? null : decl.resolveName();
            ModelAttribute[] attributes = decl == null ? null : decl.resolveAttributes();
            
            Type type = null;
            if( decl != null && base != null )
                type = decl.resolveType( base, stack );
            
            if( name != null ){
                NodeStack nodes = stack.getNodeStack();
                
                FieldModelNode node = new FieldModelNode( name, attributes, null, type, null );
                node.setDocumentation( getComments() );
                nodes.include( node, this );
                nodes.setNode( node );
                nodes.addLocation( node.getRange() );
                nodes.addChild( node, 1, this );
                
                nodes.addType( base );
                
                resolved( "field", node );
            }
        
            stack.getNodeStack().popNode( null );
        }
    }
    
    @Override
    public Range getCommentAnchor(){
    	Declarator declarator = getDeclarator();
    	if( declarator == null )
    		return null;
    	return declarator.getRange();
    }
    
    public void setDeclarator( Declarator declarator ){
        setField( 0, declarator );
    }
    public Declarator getDeclarator(){
        return (Declarator)getNoError( 0 );
    }
    
    public void setExpression( Expression expression ){
        setField( 1, expression );
    }
    public Expression getExpression(){
        return (Expression)getNoError( 1 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof Declarator ))
                throw new ASTException( node, "Must be a Declarator" );
        }
        
        if( index == 1 ){
            if( !(node instanceof Expression ))
                throw new ASTException( node, "Must be an Expression" );
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
