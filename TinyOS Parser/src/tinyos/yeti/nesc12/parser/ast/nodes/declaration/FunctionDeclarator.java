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

import static tinyos.yeti.nesc12.parser.ast.elements.types.TypeUtility.function;
import static tinyos.yeti.nesc12.parser.ast.elements.types.TypeUtility.result;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class FunctionDeclarator extends AbstractFixedASTNode implements Declarator {
    public FunctionDeclarator(){
        super( "FunctionDeclarator", "declarator", "parameters" );
    }

    public FunctionDeclarator( Declarator declarator, ASTNode parameters ){
        this();
        setDeclarator( declarator );
        setField( "parameters", parameters );
    }

    public FunctionDeclarator getFunction(){
    	return this;
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
    	
    	Declarator decl = getDeclarator();
    	if( decl == null )
    		return resolved( "indices", null );
    	
    	return resolved( "indices", decl.resolveIndices() );
    }
    
    public Name resolveName() {
        if( isResolved( "name" ))
            return resolved( "name" );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return resolved( "name", null );
        
        return resolved( "name", decl.resolveName() );
    }
    
    public Type resolveType( Type base, AnalyzeStack stack ) {
        String id = "type";
        if( isResolved( id ))
            return resolved( id );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return resolved( id, null );
        
        ASTNode parameters = getParameters();
        if( parameters == null ){
            base = new FunctionType( base, new Type[]{} );
        }
        else if( parameters instanceof ParameterTypeList ){
            ParameterTypeList list = (ParameterTypeList)parameters;
            Type[] arguments = list.resolveTypes( true );
            if( arguments == null )
                arguments = new Type[]{};
            base = new FunctionType( base, arguments, list.isOpenEnded() );
        }
        else if( parameters instanceof IdentifierList ){
            Type[] arguments = new Type[ ((IdentifierList)parameters).getChildrenCount() ];
            base = new FunctionType( base, arguments );
        }
        
        return resolved( id, decl.resolveType( base, stack ) );
    }
    
    public Name[] resolveArgumentNames( AnalyzeStack stack ){
        if( isResolved( "names" ))
            return resolved( "names" );
        
        ASTNode parameters = getParameters();
        Name[] result;
        
        if( parameters instanceof ParameterTypeList ){
            ParameterTypeList list = (ParameterTypeList)parameters;
            result = new Name[ list.getChildrenCount() ];
            for( int i = 0; i < result.length; i++ ){
                ParameterDeclaration decl = list.getTypedChild( i );
                if( decl != null ){
                     result[i] = decl.resolveName();
                }
            }
        }
        else if( parameters instanceof IdentifierList ){
            IdentifierList list = (IdentifierList)parameters;
            result = new Name[ list.getChildrenCount() ];
            for( int i = 0; i < result.length; i++ ){
                Identifier id = list.getTypedChild( i );
                if( id != null ){
                    result[i] = stack.name( id );
                }
            }
        }
        else{
            result = null;
        }
        
        return resolved( "names", result );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        if( getParent() instanceof ErrorASTNode ){
            // this might be a function definition. We do not know and have
            // no way to find out. Let's just assume it is, because when writing
            // in a function it is not unlikely to cause an error.
            stack.put( ParameterDeclaration.STORE_FIELDS );
            super.resolve( stack );
            stack.remove( ParameterDeclaration.STORE_FIELDS );
        }
        else{
            super.resolve( stack );
        }
    }
    
    public void checkNotIncomplete( AnalyzeStack stack ){
        Type type = resolved( "type" );
        if( type == null )
            return;
        
        Declarator declarator = getDeclarator();
        if( declarator != null ){
            Type result = result( function( type ));
            if( result != null && result.isIncomplete() ){
                stack.error( "resulting type is an incomplete type", declarator );
            }
        }
        
        ASTNode parameters = getParameters();
        
        if( parameters instanceof ParameterTypeList ){
            ParameterTypeList list = (ParameterTypeList)parameters;
            Type[] arguments = list.resolveTypes( true );
            
            for( int i = 0, n = arguments.length; i<n; i++ ){
                if( arguments[i] != null && arguments[i].isIncomplete() ){
                    ParameterDeclaration parameter = list.getTypedChild( i );
                    Name name = parameter == null ? null : parameter.resolveName();
                    if( name != null ){
                        stack.error( "parameter " + (i+1) + " is an incomplete type", name.getRange() );
                    }
                }
            }
        }
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
    public void setParameters( IdentifierList parameters ){
        setField( 1, parameters );
    }
    public ASTNode getParameters(){
        return getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof Declarator))
                throw new ASTException( node, "Must be a Declarator" );
        }
        if( index == 1 ){
            if( !(node instanceof ParameterTypeList) && !(node instanceof IdentifierList))
                throw new ASTException( node, "Must be a ParameterTypeList or an IdentifierList" );
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
