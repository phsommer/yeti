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
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.ConstType;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.util.ModifierCount;
import tinyos.yeti.nesc12.parser.ast.util.ModifierCountMessanger;
import tinyos.yeti.nesc12.parser.ast.util.SpecifierCount;

public class DeclarationSpecifierList extends AbstractListASTNode<DeclaratorSpecifier>{
    public DeclarationSpecifierList(){
        super( "DeclarationSpecifierList" );
    }
    
    public DeclarationSpecifierList( DeclaratorSpecifier child ){
        super( "DeclarationSpecifierList" );
        add( child );
    }
    
    @Override
    public DeclarationSpecifierList add( DeclaratorSpecifier node ) {
        super.add( node );
        return this;
    }
    
    public Type resolveType(){
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Type raw = SpecifierCount.resolveRawType( this );
        raw = resolveModifiedType( raw );
        
        return resolved( "type", raw );
    }
    
    public Modifiers resolveModifiers(){
        if( isResolved( "modifiers" ))
            return resolved( "modifiers" );
        
        Modifiers modifiers = ModifierCount.resolveModifiers( this );
        if( !modifiers.isAtLeastOneOf( ~0 ))
            return resolved( "modifiers", null );
        
        return resolved( "modifiers", modifiers );
    }
    
    /**
     * Searches for the modifiers in this list and upgrades <code>type</code>
     * with the modifiers.
     * @param type some type, can be <code>null</code>
     * @return either <code>type</code> or a modified version of <code>type</code>.
     * <code>null</code> if <code>type</code> was <code>null</code>.
     */
    public Type resolveModifiedType( Type type ){
        if( type == null )
            return null;
        
        Modifiers modifiers = resolveModifiers();
        if( modifiers == null )
            return type;
        
        if( !modifiers.isConst() )
            return type;
        
        return new ConstType( type );
    }
    
    public boolean isTypedef(){
    	Modifiers modifiers = resolveModifiers();
    	if( modifiers == null )
    		return false;
    	
    	return modifiers.isTypedef();
    }
    
    /**
     * Searches the first {@link TypeSpecifier} whose method
     * {@link TypeSpecifier#isDataObject()} returns <code>true</code>.
     * @return the first data object specifier
     */
    public TypeSpecifier findDataObjectSpecifier(){
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            DeclaratorSpecifier decl = getTypedChild( i );
            if( decl != null && decl.isSpecifier() ){
                TypeSpecifier spec = (TypeSpecifier)decl;
                if( spec.isDataObject() )
                    return spec;
            }
        }
        return null;
    }
    
    /**
     * Counts the various keywords and elements that specify and qualify
     * a new type.
     * @return the result of the counting
     */
    public SpecifierCount countSpecifiers(){
        SpecifierCount count = new SpecifierCount();
        count.count( this );
        return count;
    }
    
    @Override
    protected void checkChild( DeclaratorSpecifier child ) throws ASTException {    
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
        // TODO method not implemented, (check that this describes a type)
        super.resolve( stack );
    }
    
    /**
     * Checks whether this {@link DeclarationSpecifierList} resolves to a 
     * type or not. If it does not, then an error is reported at <code>stack</code>.
     * @param stack the stack where to report errors
     */
    public void checkResolvesType( AnalyzeStack stack ){
        SpecifierCount.checkSpecifiers( this, stack );
    }
    
    public void checkModifiers( AnalyzeStack stack, int[] presentCheck, int onceCheck, int noneCheck, int[] oneOfCheck, boolean error, Name name ){
        ModifierCount.checkModifiers( this, stack, presentCheck, onceCheck, noneCheck, oneOfCheck, error, name );
    }
    
    public void checkModifiers( AnalyzeStack stack, int[] presentCheck, int onceCheck, int noneCheck, int[] oneOfCheck, ModifierCountMessanger messanger ){
        ModifierCount.checkModifiers( this, stack, presentCheck, onceCheck, noneCheck, oneOfCheck, messanger );
    }
}
