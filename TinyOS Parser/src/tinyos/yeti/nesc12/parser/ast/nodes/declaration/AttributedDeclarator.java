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

import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AttributeList;

public class AttributedDeclarator extends AbstractFixedASTNode implements Declarator{
    public AttributedDeclarator(){
        super( "AttributedDeclarator", "declarator", "attributes" );
    }
    
    public AttributedDeclarator( Declarator declarator, AttributeList attributes ){
        this();
        setDeclarator( declarator );
        setAttributes( attributes );
    }
    
    public ModelAttribute[] resolveAttributes(){
    	AttributeList list = getAttributes();
    	if( list == null )
    		return null;
    	return list.resolveModelAttributes();
    }
    
    public Field[] resolveIndices() {
    	if( isResolved( "indices" ))
    		return resolved( "indices" );
    	
    	Declarator decl = getDeclarator();
    	if( decl == null )
    		return resolved( "indices" );
    	
    	return resolved( "indices", decl.resolveIndices() );
    }
    
    public Type resolveType( Type name, AnalyzeStack stack ) {
        String id = "type";
        if( isResolved( id ))
            return resolved( id );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return null;
        return resolved( id, decl.resolveType( name, stack ));
    }
    
    public Name resolveName() {
        if( isResolved( "name" ))
            return resolved( "name" );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return null;
        
        return resolved( "name", decl.resolveName() );
    }
    
    public void setDeclarator( Declarator declarator ){
        setField( 0, declarator );
    }
    
    public Declarator getDeclarator(){
        return (Declarator)getNoError( 0 );
    }
    
    public void setAttributes( AttributeList attributes ){
        setField( 1, attributes );
    }
    
    public AttributeList getAttributes(){
        return (AttributeList)getField( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !( node instanceof Declarator ))
                throw new ASTException( node, "Must be a Declarator" );
        }
        if( index == 1 ){
            if( !( node instanceof AttributeList ))
                throw new ASTException( node, "Must be a NesCAttributeList" );
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

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    

    public FunctionDeclarator getFunction(){
    	Declarator declarator = getDeclarator();
    	if( declarator == null ){
    		return null;
    	}
    	else{
    		return declarator.getFunction();
    	}
    }
}
