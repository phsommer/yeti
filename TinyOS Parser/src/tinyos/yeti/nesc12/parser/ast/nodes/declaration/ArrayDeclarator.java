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
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;

public class ArrayDeclarator extends AbstractFixedASTNode implements Declarator {
    public ArrayDeclarator(){
        super( "ArrayDeclarator", "declarator", "qualifiers", "size" );
    }
    
    public ArrayDeclarator( Declarator declarator, DeclarationSpecifierList qualifiers, ASTNode size ){
        this();
        
        setDeclarator( declarator );
        setQualifiers( qualifiers );
        setField( "size", size );
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
    
    public Type resolveType( Type name, AnalyzeStack stack ) {
        String id = "type";
        if( isResolved( id ))
            return resolved( id );
        
        if( name == null )
            return resolved( id, null );
        
        Declarator decl = getDeclarator();
        ASTNode size = getSize();
        
        if( decl == null )
            return resolved( id, null );
        
        if( size == null ){
            name = ArrayType.variable( name, stack, this );
        }
        else if( size instanceof VariableLength ){
            name = ArrayType.incomplete( name, stack, this );
        }
        else if( size instanceof Expression ){
            Expression expr = (Expression)size;
            Value constant = expr.resolveConstantValue();
            if( constant == null )
                name = ArrayType.specified( name, 0, stack, this );
            
            else if( constant instanceof IntegerValue ){
                int length = ((IntegerValue)constant).intValue();
                if( length < 0 )
                    return resolved( id, null );
                
                name = ArrayType.specified( name, length, stack, this );
            }
            else if( constant instanceof UnknownValue ){
            	name = ArrayType.specifiedUnknown( name );
            }
            else{
            	name = null;
            }
        }
        
        if( name == null )
            return resolved( "type", null );
        
        return resolved( id, decl.resolveType( name, stack ) );
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
    
    public void setQualifiers( DeclarationSpecifierList qualifiers ){
        setField( 1, qualifiers );
    }
    public DeclarationSpecifierList getQualifiers(){
        return (DeclarationSpecifierList)getNoError( 1 );
    }
    
    public void setSize( Expression size ){
        setField( 2, size );
    }
    public void setSize( VariableLength size ){
        setField( 2, size );
    }
    public ASTNode getSize(){
        return getNoError( 2 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof Declarator ))
                throw new ASTException( node, "Must be a Declarator" );
        }
        if( index == 1 ){
            if( !(node instanceof DeclarationSpecifierList ))
                throw new ASTException( node, "Must be a DeclarationSpecifierList" );
        }
        if( index == 2 ){
            if( !(node instanceof Expression ) && !(node instanceof VariableLength))
                throw new ASTException( node, "Must be an Expression or VariableLength" );
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
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            checkSize( stack );
            checkQualifiers( stack );
        }
    }
    
    private void checkSize( AnalyzeStack stack ){
        ASTNode size = getSize();
        if( !(size instanceof Expression ))
            return;
        
        Expression expr = (Expression)size;
        Type type = expr.resolveType();
        if( type == null )
            return;
        
        BaseType base = type.asBase();
        if( base == null || !base.isIntegerType() ){
            stack.error( "size of array not an integer type", expr );
        }
    }
    
    private void checkQualifiers( AnalyzeStack stack ){
        DeclarationSpecifierList list = getQualifiers();
        if( list == null )
            return;
        
        list.checkModifiers( 
                stack,
                null,
                Modifiers.STATIC | Modifiers.ALL_TYPE_QUALIFIER,
                (~(Modifiers.STATIC | Modifiers.ALL_TYPE_QUALIFIER)) & Modifiers.ALL,
                null,
                true,
                null );
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
