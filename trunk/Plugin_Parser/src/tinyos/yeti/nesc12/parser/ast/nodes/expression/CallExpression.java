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

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.CallKind.Call;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCName;

public class CallExpression extends AbstractFixedExpression implements Expression {
    public static final String CALL = "call";
    public static final String NAME = "name";
    public static final String ARGUMENTS = "arguments";
    
    public CallExpression(){
        super( "CallExpression", CALL, NAME, ARGUMENTS );
    }
    
    public CallExpression( ASTNode call, ASTNode name, ASTNode arguments ){
        this();
        setField( CALL, call );
        setField( NAME, name );
        setField( ARGUMENTS, arguments );
    }
    
    public CallExpression( CallKind call, NesCName name, ArgumentExpressionList arguments ){
        this();
        setCall( call );
        setName( name );
        setArguments( arguments );
    }
    
    public Type resolveType() {
    	return resolved( "type" );
    }
    
    public Type resolveType( AnalyzeStack stack ){
        if( isResolved( "type" ))
            return resolved( "type" );
        
        CallKind call = getCall();
        if( call != null && call.getCall() == Call.POST ){
        	Type error = stack.getTypedef( new SimpleName( null, "error_t" ) );
        	return resolved( "type", error );
        }
        
        NesCName name = getName();
        if( name == null )
            return resolved( "type", null );
        
        Field field = name.resolveField();
        if( field == null )
            return resolved( "type", null );
        
        Type type = field.getType();
        if( type == null )
            return resolved( "type", null );
        
        FunctionType function = type.asFunctionType();
        if( function == null )
            return resolved( "type", null );
        
        type = function.getResult();
        return resolved( "type", type );
    }
    
    public Field resolveField(){
    	if( isResolved( "field" ))
    		return resolved( "field" );
    	
    	NesCName name = getName();
    	if( name == null )
    		return resolved( "field", null );
    	
    	return resolved( "field", name.resolveField() );
    }
    
    public Value resolveConstantValue() {
        return null;
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();
        
        resolveType( stack );
        
        if( stack.isReportErrors() ){
            checkCallKind( stack );
            checkArguments( stack );
        }
        
        if( stack.isCreateReferences() ){
        	Field field = resolveField();
        	if( field != null ){
        		stack.reference( this, field.getPath() );
        	}
        }
    }
    
    private void checkCallKind( AnalyzeStack stack ){
        CallKind call = getCall();
        if( call == null || call.getCall() == null )
            return;
        
        NesCName name = getName();
        if( name == null )
            return;
        
        Field field = name.resolveField();
        if( field == null )
            return;
        
        Modifiers modifiers = field.getModifiers();
        String fieldName = Name.toIdentifier( field.getName() );
        
        switch( call.getCall() ){
            case POST:
                if( modifiers == null || !modifiers.isTask() ){
                    stack.error( "not a task: '" + fieldName + "'", name );
                }
                break;
            case SIGNAL:
                if( modifiers == null || !modifiers.isEvent() ){
                    stack.error( "not an event: '" + fieldName + "'", name );
                }
                break;
            case CALL:
                if( modifiers == null || !modifiers.isCommand() ){
                    stack.error( "not a command: '" + fieldName + "'", name );
                }
                break;
        }
    }
    
    private void checkArguments( AnalyzeStack stack ){
        NesCName name = getName();
        if( name == null )
            return;
        
        Field field = name.resolveField();
        if( field == null )
            return;
        
        Type type = field.getType();
        if( type == null )
            return;
        
        if( type.asFunctionType() == null ){
            stack.error( "not a function: '" + Name.toIdentifier( field.getName() ) + "'", name );
            return;
        }
        
        ArgumentExpressionList arguments = getArguments();
        FunctionCall.checkFunctionCall( type.asFunctionType(), arguments, name, stack );
    }
    
    public void setCall( CallKind call ){
        setField( 0, call );
    }
    public CallKind getCall(){
        return (CallKind)getNoError( 0 );
    }
    
    public void setName( NesCName name ){
        setField( 1, name );
    }
    
    public NesCName getName(){
        return (NesCName)getNoError( 1 );
    }
    
    public void setArguments( ArgumentExpressionList arguments ){
        setField( 2, arguments );
    }
    
    public ArgumentExpressionList getArguments(){
        return (ArgumentExpressionList)getNoError( 2 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof CallKind ))
                throw new ASTException( node, "Must be a CallKind" );
        }
        if( index == 1 ){
            if( !(node instanceof NesCName ))
                throw new ASTException( node, "Must be a NesCName" );
        }
        if( index == 2 ){
            if( !(node instanceof ArgumentExpressionList ))
                throw new ASTException( node, "Must be an ArgumentExpressionList" );
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

    public boolean hasCommas() {
        return false;
    }

    public boolean isConstant() {
        return false;
    }
}
