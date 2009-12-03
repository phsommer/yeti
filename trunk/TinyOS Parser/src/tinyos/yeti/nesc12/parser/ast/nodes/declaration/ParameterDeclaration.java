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
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleField;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.util.ModifierValidator;

public class ParameterDeclaration extends AbstractFixedASTNode {
    /** whether to store fields or not */
    public static final Flag STORE_FIELDS = new Flag( "parameter declarations stores fields" );
    
    public ParameterDeclaration(){
        super( "ParameterDeclaration", "specifiers", "declarator" );
    }
    public ParameterDeclaration( DeclarationSpecifierList specifiers, ASTNode declarator ){
        this();
        setSpecifiers( specifiers );
        setField( "declarator", declarator );
    }
    
    public Type resolveType(){
        if( !isResolved( "type" ))
            return resolveType( null );
        return resolved( "type" );
    }
    
    /**
     * Finds the type that is specified for this parameter.
     * @param stack used to report any errors
     * @return the type or <code>null</code>
     */
    private Type resolveType( AnalyzeStack stack ){
        if( isResolved( "type" ))
            return resolved( "type" );
        
        DeclarationSpecifierList list = getSpecifiers();
        if( list == null )
            return resolved( "type", null );
        
        Type base = list.resolveType();
        ASTNode declarator = getDeclarator();
        if( declarator == null )
            return resolved( "type", base );
        
        if( declarator instanceof Declarator ){
            Declarator decl = (Declarator)declarator;
            return resolved( "type", decl.resolveType( base, stack ) );
        }
        
        if( declarator instanceof AbstractDeclarator ){
            AbstractDeclarator decl = (AbstractDeclarator)declarator;
            return resolved( "type", decl.resolveType( base, stack ) );
        }
        
        return resolved( "type", null );
    }
    
    public Name resolveName(){
        if( isResolved( "name" ))
            return resolved( "name" );
        
        ASTNode declarator = getDeclarator();
        if( declarator != null && declarator instanceof Declarator )
            return resolved( "name", ((Declarator)declarator).resolveName());
        
        return resolved( "name", null );
    }
    
    public ModelAttribute[] resolveAttributes(){
    	if( isResolved( "attributes" ))
    		return resolved( "attributes" );
    	
    	ASTNode declarator = getDeclarator();
    	if( declarator == null || !(declarator instanceof Declarator ))
    		return resolved( "attributes", null );
    	
    	return resolved( "attributes", ((Declarator)declarator).resolveAttributes() );
    }
    
    public Field resolveField(){
        return resolved( "field" );
    }
    
    public Field resolveField( AnalyzeStack stack ){
        if( isResolved( "field" ))
            return resolved( "field" );
        
        Name name = resolveName();
        Type type = resolveType();
        
        if( name == null && type == null )
            return resolved( "field", null );

        LazyRangeDescription range = null;
        if( name == null )
            range = new LazyRangeDescription( this, stack.getParser() );
        
        
        ModelAttribute[] attributes = resolveAttributes();
        return resolved( "field", new SimpleField( null, type, name, attributes, null, range, null ));
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        cleanResolved();
        
        if( stack.presentLevel( STORE_FIELDS )){
            stack.remove( STORE_FIELDS );
            super.resolve( stack );
            stack.checkCancellation();
            stack.put( STORE_FIELDS );
            
            if( stack.isReportErrors() )
                resolveType( stack );
            
            Field field = resolveField( stack );
            if( field != null && field.getName() != null ){
                stack.putField( field, getRange().getRight(), false );
            }
        }
        else{
            super.resolve( stack );
            stack.checkCancellation();
            
            if( stack.isReportErrors() )
                resolveType( stack );
        }
        
        if( stack.isReportErrors() ){
            ModifierValidator checker = stack.get( ModifierValidator.MODIFIER_VALIDATOR );
            if( checker != null ){
                checker.check( stack, this );
            }
        }
    }
    
    public void setSpecifiers( DeclarationSpecifierList specifiers ){
        setField( 0, specifiers );
    }
    public DeclarationSpecifierList getSpecifiers(){
        return (DeclarationSpecifierList)getNoError( 0 );
    }
    
    public void setDeclarator( Declarator declarator ){
        setField( 1, declarator );
    }
    public void setDeclarator( AbstractDeclarator declarator ){
        setField( 1, declarator );
    }
    public ASTNode getDeclarator(){
        return getNoError( 1 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof DeclarationSpecifierList))
                throw new ASTException( node, "Must be DeclarationSpecifierList" );
        }
        if( index == 1 ){
            if( !(node instanceof Declarator) && !(node instanceof AbstractDeclarator))
                throw new ASTException( node, "Must be a Declarator or an AbstractDeclarator" );
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
