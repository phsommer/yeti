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

import java.util.List;

import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.util.DeclarationStack;

public class DataObjectFieldDeclaration extends AbstractFixedASTNode {
    public static final String MODIFIERS = "modifiers";
    public static final String DECLARATORS = "declarators";
    
    public static final Key<Type> BASE_TYPE = new Key<Type>( "base type" );
    
    public DataObjectFieldDeclaration(){
        super( "DataObjectFieldDeclaration", MODIFIERS, DECLARATORS );
    }
    
    public DataObjectFieldDeclaration( ASTNode modifiers, ASTNode declarators ){
        this();
        setField( MODIFIERS, modifiers );
        setField( DECLARATORS, declarators );
    }
    
    public DataObjectFieldDeclaration( DeclarationSpecifierList modifiers, DataObjectFieldDeclaratorList declarators ){
        this();
        setModifiers( modifiers );
        setDeclarators( declarators );
    }
    
    /**
     * Tries to find all fields that are described by this field declaration
     * @param fields a list into which new fields will be inserted, can be <code>null</code>
     * @return the list of fields or <code>null</code> if an error occurred
     */
    public List<Field> resolveFields( List<Field> fields ){
    	if( isResolved( "field" )){
    		Field field = resolved( "field" );
    		fields.add( field );
    	}
    	
        DataObjectFieldDeclaratorList decls = getDeclarators();
        
        if( decls == null )
            return null;
        
        return decls.resolveFields( fields );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
    	Type base = null;
    	
        if( stack.isCreateFullModel() ){
        	resolveComments( stack );
            resolve( 0, stack );
            
            DeclarationSpecifierList list = getModifiers();
            base = list == null ? null : list.resolveType();
            
            if( base != null )
                stack.put( BASE_TYPE, base );
            
            resolve( 1, stack );
            
            if( base != null ){
                stack.remove( BASE_TYPE );
            }
        }
        else{
            super.resolve( stack );
            stack.checkCancellation();
            
            if( stack.isReportErrors() ){
            	DeclarationSpecifierList list = getModifiers();
                base = list == null ? null : list.resolveType();
            }
        }
        
        if( base != null ){
            if( stack.presentScope( DataObjectDeclaration.IN_DATA_OBJECT_DECLARATION )){
            	DataObjectFieldDeclaratorList decls = getDeclarators();
                if( decls != null && decls.getChildrenCount() == 0 ){
                    DeclarationSpecifierList list = getModifiers();
                    DeclarationStack declarations = stack.getDeclarationStack();
                    declarations.push( FieldModelNode.fieldId( null, base ) );
                	Field field = declarations.set( list.resolveModifiers(), base, null, null, null, stack.range( this ) );
                	declarations.pop();
            		resolved( "field", field );
                }
            }
        }
        
        if( stack.isReportErrors() ){
            checkModifiers( stack );
        }
        
        DeclarationSpecifierList modifiers = getModifiers();
        DataObjectFieldDeclaratorList decls = getDeclarators();
        if( modifiers != null && decls != null ){
            if( stack.isReportErrors() && decls.getChildrenCount() == 0 ){
            	if( base == null || base.asDataObjectType() == null || !stack.presentScope( DataObjectDeclaration.IN_DATA_OBJECT_DECLARATION )){
                	stack.warning( "declaration does declare nothing", this );
                }
            }
            
            Type type = modifiers.resolveType();
            if( type != null ){
                decls.resolveFields( type, stack );
            }
        }
    }
    
    private void checkModifiers( AnalyzeStack stack ){
        DeclarationSpecifierList list = getModifiers();
        if( list == null )
            return;

        list.checkResolvesType( stack );
        list.checkModifiers( 
                stack,
                null,
                Modifiers.CONST | Modifiers.VOLATILE,
                ~(Modifiers.COMMAND | Modifiers.VOLATILE) & Modifiers.ALL,
                null,
                true, 
                null );
    }
    
    public void setModifiers( DeclarationSpecifierList modifiers ){
        setField( 0, modifiers );
    }
    public DeclarationSpecifierList getModifiers(){
        return (DeclarationSpecifierList)getNoError( 0 );
    }
    
    public void setDeclarators( DataObjectFieldDeclaratorList declarators ){
        setField( 1, declarators );
    }
    public DataObjectFieldDeclaratorList getDeclarators(){
        return (DataObjectFieldDeclaratorList)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !( node instanceof DeclarationSpecifierList ))
                throw new ASTException( node, "Must be a DeclarationSpecifierList" );
        }
        if( index == 1 ){
            if( !( node instanceof DataObjectFieldDeclaratorList ))
                throw new ASTException( node, "Must be a DataObjectFieldDeclaratorList" );
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
