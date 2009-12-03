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

import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class TypedefName extends AbstractFixedASTNode implements TypeSpecifier{
    public TypedefName(){
        super( "TypedefName", "name" );
    }

    public TypedefName( Identifier name ){
        this();
        setName( name );
    }
    
    public Type resolveType(){
        return resolved( "type" );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();
        
        Identifier name = getName();
        if( name != null ){
        	Name simple = new SimpleName( null, name.getName() );
        	
            Type type = stack.getTypedef( simple );
            if( type == null )
                resolved( "type", new GenericType( name.getName() ) );
            else{
                resolved( "type", type );
            }
            
            if( stack.isCreateReferences() ){
            	ASTModelPath path = null;
            	ModelNode node = stack.getTypedefModel( name.getName() );
            	if( node != null ){
            		path = node.getPath();
            	}
            	if( path == null ){
            		path = stack.getTypedefPath( simple );
            	}
            	stack.reference( this, path );
            }
        }
    }

    public boolean isStorageClass() {
        return false;
    }
    
    public boolean isSpecifier() {
        return true;
    }
    
    public boolean isPrimitive() {
        return false;
    }
    
    public boolean isDataObject() {
        return false;
    }
    
    public boolean isEnum() {
        return false;
    }
    
    public boolean isTypedefName() {
        return true;
    }
    
    public boolean isAttribute(){
        return false;
    }
    
    public void setName( Identifier name ){
        setField( 0, name );
    }
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof Identifier ) )
            throw new ASTException( node, "Must be an Identifier" );
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
