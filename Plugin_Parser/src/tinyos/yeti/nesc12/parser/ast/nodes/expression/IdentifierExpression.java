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

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.ep.declarations.FieldDeclaration;
import tinyos.yeti.nesc12.ep.declarations.IgnoreDeclaration;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class IdentifierExpression extends AbstractFixedExpression implements Expression {
    public static final String IDENTIFIER = "identifier";
    
    public IdentifierExpression(){
        super( "IdentifierExpression", IDENTIFIER );
    }
    
    public IdentifierExpression( Identifier identifier ){
        this();
        setIdentifier( identifier );
    }
    
    public IdentifierExpression( ASTNode identifier ){
        this();
        setField( IDENTIFIER, identifier );
    }
    
    public Value resolveConstantValue() {
    	// cannot give any guarantees...
    	if( isResolved( "value" ))
            return resolved( "value" );
        
        Field field = resolveField();
        if( field == null )
            return resolved( "value", null );
        
        Value value = field.getInitialValue();
        if( value instanceof UnknownValue )
        	return resolved( "value", value );
        
        return resolved( "value", null );
    }

    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Field field = resolveField();
        if( field == null )
            return resolved( "type", null );
        
        return resolved( "type", field.getType() );
    }
    
    public Field resolveField(){
        return resolved( "field" );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();
        
        Identifier name = getIdentifier();
        if( name != null ){
            if( stack.isCreateModel() ){
            	ASTModelPath path = null;
                Field field = stack.getField( stack.name( name ) );
                boolean ignore = false;
                
                if( field != null )
                	path = field.getPath();
                
                if( field == null ){
                    IDeclaration declaration = stack.resolveDeclaration( name.getName(), false, Kind.FIELD, Kind.FUNCTION );
                    if( declaration instanceof FieldDeclaration ){
                        field = ((FieldDeclaration)declaration).toField();
                        path = ((FieldDeclaration)declaration).getPath();
                    }
                    else if( declaration instanceof IgnoreDeclaration ){
                        ignore = true;
                    }
                }

                resolved( "field", field );

                if( stack.isCreateReferences() ){
                	if( field != null && path != null ){
                		stack.reference( this, path );
                	}
                }
                
                if( stack.isReportErrors() && !ignore ){
                    if( field == null ){
                        Name simpleName = new SimpleName( null, name.getName() );
                        
                        InterfaceReferenceModelConnection interfaze = stack.getInterfaceReference( simpleName );
                        if( interfaze != null ){
                            stack.error( "found an interface '" + name.getName() + "' but expected a field", name );
                        }
                        else{
                            ComponentReferenceModelConnection component = stack.getComponentReference( name.getName() );
                            if( component != null ){
                                stack.error( "found a component reference '" + name.getName() + "' but expected a field", name );    
                            }
                            else{
                                stack.error( "'" + name.getName() + "' undeclared", this );       
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void setIdentifier( Identifier identifier ){
        setField( 0, identifier );
    }
    
    public Identifier getIdentifier(){
        return (Identifier)getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !(node instanceof Identifier ))
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

    public boolean hasCommas() {
        return false;
    }

    public boolean isConstant() {
        // a field is never constant
        return false;
    }
}
