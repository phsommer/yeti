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

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AttributeList;

public class ParenthesizedAbstractDeclarator extends AbstractFixedASTNode implements AbstractDeclarator {
    public ParenthesizedAbstractDeclarator(){
        super( "ParenthesizedAbstractDeclarator", "attributes", "declarator" );
    }
    
    public ParenthesizedAbstractDeclarator( AttributeList attributes, AbstractDeclarator declarator ){
        this();
        setAttributes( attributes );
        setDeclarator( declarator );
    }

    public Type resolveType( Type base, AnalyzeStack stack ) {
        String id = "type";
        if( isResolved( id ))
            return resolved( id );
        
        AbstractDeclarator decl = getDeclarator();
        if( decl == null )
            return resolved( id, null );
        
        return resolved( id, decl.resolveType( base, stack ));
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    public void setAttributes( AttributeList attributes ){
        setField( 0, attributes );
    }
    public AttributeList getAttributes(){
        return (AttributeList)getNoError( 0 );
    }
    
    public void setDeclarator( AbstractDeclarator declarator ){
        setField( 1, declarator );
    }
    public AbstractDeclarator getDeclarator(){
        return (AbstractDeclarator)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof AttributeList ) )
                throw new ASTException( node, "Must be a NesCAttributeList" );
        }
        if( index == 1 ) {
            if( !( node instanceof AbstractDeclarator ) )
                throw new ASTException( node, "Must be an AbstractDeclarator" );
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
