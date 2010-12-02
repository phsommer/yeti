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

/**
 * Example: in "a.b[short id]" would "a.b" be the declarator, and "short id" the
 * parameters.
 * @author Benjamin Sigg
 */
public class ParameterizedDeclarator extends AbstractFixedASTNode implements Declarator {
    public ParameterizedDeclarator(){
        super( "ParameterizedDeclarator", "declarator", "parameters" );
    }
    
    public ParameterizedDeclarator( Declarator declarator, ParameterTypeList parameters ){
        this();
        setDeclarator( declarator );
        setParameters( parameters );
    }
    
    public void setDeclarator( Declarator declarator ){
        setField( 0, declarator );
    }
    
    public Declarator getDeclarator(){
        return (Declarator)getNoError( 0 );
    }
    
    public void setParameters( ParameterTypeList parameters ){
        setField( 1, parameters );
    }
    
    public ParameterTypeList getParameters(){
        return (ParameterTypeList)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Declarator ) )
                throw new ASTException( node, "Must be a Declarator" );
        }
        if( index == 1 ) {
            if( !( node instanceof ParameterTypeList ) )
                throw new ASTException( node, "Must be a ParameterTypeList" );
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
    
    public ModelAttribute[] resolveAttributes(){
    	if( isResolved( "attributes" ))
    		return resolved( "attributes" );
    	
    	Declarator decl = getDeclarator();
    	if( decl == null )
    		return resolved( "attributes", null );
    		
    	return resolved( "attributes", decl.resolveAttributes() );
    }
    
    public Field[] resolveIndices() {
    	if( isResolved( "indices" ))
    		return resolved( "indices" );
    	
    	ParameterTypeList list = getParameters();
    	if( list == null )
    		return resolved( "indices", null );
    	
    	
    	return resolved( "indices", list.resolveFields() );
    }
    
    public Name resolveName() {
        if( isResolved( "name" ))
            return resolved( "name" );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return resolved( "name", null );
        
        return resolved( "name", decl.resolveName() );
    }

    public Type resolveType( Type name, AnalyzeStack stack ) {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return resolved( "type", null );
        
        return resolved( "type", decl.resolveType( name, stack ));
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
