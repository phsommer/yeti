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
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;

public class FunctionAbstractDeclarator extends AbstractFixedASTNode implements AbstractDeclarator{
    public static final String DECLARATOR = "declarator";
    public static final String PARAMETERS = "parameters";
    
    public FunctionAbstractDeclarator(){
        super( "FunctionAbstractDeclarator", DECLARATOR, PARAMETERS );
    }
    
    public FunctionAbstractDeclarator( ASTNode declarator, ASTNode parameters ){
        this();
        setField( DECLARATOR, declarator );
        setField( PARAMETERS, parameters );
    }
    
    public FunctionAbstractDeclarator( AbstractDeclarator declarator, ParameterTypeList parameters ){
        this();
        setDeclarator( declarator );
        setParameters( parameters );
    }
    
    public Type resolveType( Type base, AnalyzeStack stack ) {
        String id = "type";
        if( isResolved( id ))
            return resolved( id );
        
        
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
        
        AbstractDeclarator decl = getDeclarator();
        if( decl != null ){
            base = decl.resolveType( base, stack );
        }
        
        return resolved( id, base );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    public void setDeclarator( AbstractDeclarator declarator ){
        setField( 0, declarator );
    }
    
    public AbstractDeclarator getDeclarator(){
        return (AbstractDeclarator)getNoError( 0 );
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
            if( !( node instanceof AbstractDeclarator ) )
                throw new ASTException( node, "Must be an AbstractDeclarator" );
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
}
