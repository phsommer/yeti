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
package tinyos.yeti.nesc12.parser.ast.nodes.expression;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.declarations.EnumConstantDeclaration;
import tinyos.yeti.nesc12.ep.declarations.IgnoreDeclaration;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class EnumerationConstant extends AbstractFixedExpression implements Expression{
    public EnumerationConstant(){
        super( "EnumerationConstant", "name" );
    }
    
    public EnumerationConstant( Identifier name ){
        this();
        setName( name );
    }
    
    public Value resolveConstantValue() {
        return resolved( "value" );
    }
    
    public Type resolveType() {
        return BaseType.S_INT;
    }
    
    @Override
    public void resolve( final AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();
        
        Identifier name = getName();
        if( name != null ){
            boolean found = false;
            final Field field = stack.getEnum( new SimpleName( null, name.getName() ));
            
            IASTModelPath path = null;
            
            if( field != null ){
                resolved( "value", field.getInitialValue() );
                found = true;
                if( field.asNode() != null ){
                	path = field.asNode().getPath();
                }
            }
            
            if( !found ){
                IDeclaration declaration = stack.resolveDeclaration( name.getName(), false, Kind.ENUMERATION_CONSTANT );
                if( declaration instanceof EnumConstantDeclaration ){
                    resolved( "value", ((EnumConstantDeclaration)declaration).getConstant() );
                    path = ((EnumConstantDeclaration)declaration).getPath();
                    found = true;
                }
                else if( declaration instanceof IgnoreDeclaration ){
                    found = true;
                }
            }
            
            if( !found ){
                stack.error( "Missing enumeration constant: " + name.getName(), this );
            }
            if( stack.isCreateReferences() ){
            	if( path == null ){
            		if( field.asNode() != null ){
            			// this could be a situation where the node is accessed
            			// from within the enumeration, try to find the path later
            			stack.getNodeStack().executeOnPop( new Runnable(){
            				public void run(){
	            				IASTModelPath path = field.asNode().getPath();
	            				if( path != null ){
	            					stack.reference( EnumerationConstant.this, path );
	            				}
            				}
            			}, 0 );
            		}
            	}
            	else{
            		stack.reference( this, path );
            	}
            }
        }
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
    
    public boolean isConstant() {
        return true;
    }
    
    public boolean hasCommas() {
        return false;
    }
}
