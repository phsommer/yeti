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

import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionMap;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class FunctionCall extends AbstractFixedExpression implements Expression{
    public static final String EXPRESSION = "expression";
    public static final String ARGUMENTS = "arguments";
    
    public FunctionCall(){
        super( "FunctionCall", EXPRESSION, ARGUMENTS );
    }
    
    public FunctionCall( ASTNode expression, ASTNode arguments ){
        this();
        setField( EXPRESSION, expression );
        setField( ARGUMENTS, arguments );
    }
    
    public FunctionCall( Expression expression, ArgumentExpressionList arguments ){
        this();
        setExpression( expression );
        setArguments( arguments );
    }
    
    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Expression expr = getExpression();
        if( expr == null )
            return resolved( "type", null );
        
        Type function = expr.resolveType();
        if( function == null || function.asFunctionType() == null )
            return resolved( "type", null );
        
        return resolved( "type", function.asFunctionType().getResult() );
    }
    
    private void resolveConstantValue( AnalyzeStack stack ){
    	Expression expr = getExpression();
        if( expr instanceof IdentifierExpression ){
            Identifier name = ((IdentifierExpression)expr).getIdentifier();
            if( name != null ){
            	if( stack.isConstantFunction( name.getName() )){
            	    resolved( "constant", new UnknownValue( BaseType.U_INT ));
                }
            }
        }
    }
    
    public Value resolveConstantValue() {
        return resolved( "constant" );
    }
    
    public Expression getExpression(){
        return (Expression)getNoError( 0 );
    }
    
    public void setExpression( Expression expression ){
        setField( 0, expression );
    }
    
    public ArgumentExpressionList getArguments(){
        return (ArgumentExpressionList)getNoError( 1 );
    }
    
    public void setArguments( ArgumentExpressionList arguments ){
        setField( 1, arguments );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( node instanceof Expression )
                return;
            
            throw new ASTException( node, "Must be an Expression" );
        }
        
        if( index == 1 ){
            if( node instanceof ArgumentExpressionList )
                return;
            
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
        Expression expr = getExpression();
        return expr != null && expr.hasCommas();
    }

    public boolean isConstant() {
        return false;
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();
        
        resolveConstantValue( stack );
        
        if( stack.isReportErrors() ){
            checkValidity( stack );
            checkMissingCallKeyword( stack );
            checkMissingPostKeyword( stack );
        }
    }
    
    private void checkValidity( AnalyzeStack stack ){
        Expression expr = getExpression();
        if( expr == null )
            return;
        
        Type type = expr.resolveType();
        if( type == null )
            return;
        
        if( type.asFunctionType() == null ){
            stack.error( "called object is not a function", expr );
            return;
        }
        
        ArgumentExpressionList arguments = getArguments();
        
        checkFunctionCall( type.asFunctionType(), arguments, expr, stack );
    }

    /**
     * Makes the general checks that are needed for a function call (nr. of
     * arguments, types of arguments, ...)
     * @param function the function that gets called
     * @param arguments the arguments for the function, might be <code>null</code> 
     * to indicate the absence of any arguments
     * @param location the location where to report errors
     * @param stack used to report errors
     */
    public static void checkFunctionCall( FunctionType function, ArgumentExpressionList arguments, ASTNode location, AnalyzeStack stack ){
        int required = function.getArgumentCount();
        int available = arguments == null ? 0 : arguments.getChildrenCount();
        
        // check types of matching elements
        for( int i = 0, n = Math.min( required, available ); i<n; i++ ){
            Type requiredType = function.getArgument( i );
            Expression availableExpression = arguments.getTypedChild( i );
            if( availableExpression != null ){
                if( requiredType == null ){
                    stack.warning( "parser can't resolve required type", availableExpression );
                }
                else{
                    Type availableType = availableExpression.resolveType();
                    if( availableType != null ){
                        ConversionTable.instance().check( availableType, requiredType, ConversionMap.assignment( stack, availableExpression, availableExpression.resolveConstantValue() ));
                    }
                }
            }
        }
        
        // report missing arguments
        for( int i = available; i<required; i++ ){
            Type requiredType = function.getArgument( i );
            if( requiredType == null ){
                stack.error( "missing argument nr. " + (i+1), location );
            }
            else{
                stack.error( "missing argument nr. " + (i+1) + " of type '" + requiredType.toLabel( null, Type.Label.EXTENDED ) + "'", location );
            }
        }
        
        if( !function.isVararg() ){
            // report too many arguments
            for( int i = required; i < available; i++ ){
                ASTNode node = arguments.getChild( i );
                if( node != null ){
                    stack.error( "too many arguments for function", node );
                }
            }
        }
    }
    
    private void checkMissingCallKeyword( AnalyzeStack stack ){
        Expression expr = getExpression();
        if( !(expr instanceof FieldAccess ))
            return;
        
        FieldAccess access = (FieldAccess)expr;
        expr = access.getExpression();
        if( !(expr instanceof IdentifierExpression ))
            return;
        
        IdentifierExpression idexpr = (IdentifierExpression)expr;
        if( idexpr.resolveField() != null )
            return;
        
        Identifier id = idexpr.getIdentifier();
        if( id == null )
            return;
        
        InterfaceReferenceModelConnection interfaze = stack.getInterfaceReference( new SimpleName( null, id.getName() ) );
        if( interfaze != null ){
            Identifier functionNameId = access.getName();
            String functionName = functionNameId == null ? null : functionNameId.getName();
            
            Field function = null;
            
            if( functionName != null ){
                NesCInterfaceReference reference = interfaze.resolve( stack.getBindingResolver() );
                if( reference != null ){
                    NesCInterface raw = reference.getRawReference();
                    if( raw != null ){
                        function = raw.getField( functionName );
                    }
                }
            }
            
            Modifiers modifiers = function == null ? null : function.getModifiers();
            
            if( modifiers != null && modifiers.isCommand() ){
                stack.error( "Maybe missing keyword 'call'?", access );
            }
            else if( modifiers != null && modifiers.isEvent() ){
                stack.error( "Maybe missing keyword 'signal'?", access );
            }
            else{
                stack.error( "Maybe missing keyword 'call' or 'signal'?", access );
            }
        }
    }
    
    private void checkMissingPostKeyword( AnalyzeStack stack ){
        Expression expr = getExpression();
        if( !(expr instanceof IdentifierExpression ))
            return;
        
        IdentifierExpression access = (IdentifierExpression)expr;
        Identifier name = access.getIdentifier();
        if( name == null )
            return;
        	
        Field field = stack.getField( new SimpleName( null, name.getName() ) );
        if( field == null )
        	return;
        
        Modifiers modifiers = field.getModifiers();
        if( modifiers == null )
        	return;
        
        if( modifiers.isTask() ){
        	stack.error( "posting task '"+ name.getName() + "' without 'post'", name );
        }
    }
}






























