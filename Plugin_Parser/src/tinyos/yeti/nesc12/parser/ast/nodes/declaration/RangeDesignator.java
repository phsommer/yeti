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
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.util.InitializerCounter;

public class RangeDesignator extends AbstractFixedASTNode implements Designator {
    public static final String BEGIN = "begin";
    public static final String END = "end";
    
    public RangeDesignator(){
        super( "RangeDesignator", BEGIN, END );
    }
    
    public RangeDesignator( ASTNode begin, ASTNode end ){
        this();
        setField( BEGIN, begin );
        setField( END, end );
    }
    
    public RangeDesignator( Expression begin, Expression end ){
        this();
        setBegin( begin );
        setEnd( end );
    }
    
    public Type resolveType( Type base ) {
        if( base.asArrayType() == null )
            return null;
        
        return base.asArrayType().getRawType();
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        
        InitializerCounter counter = stack.get( Initializer.INITIALIZER_COUNTER );
        
        Expression begin = getBegin();
        Expression end = getEnd();
        
        checkType( begin, stack );
        checkType( end, stack );
        
        if( begin != null && end != null ){
            Value beginValue = begin.resolveConstantValue();
            Value endValue = end.resolveConstantValue();
            
            boolean beginOk = false;
            boolean endOk = false;
            
            if( beginValue == null ){
                stack.error( "Not a constant value", begin );
            }
            else if( !(beginValue instanceof IntegerValue) ){
                stack.error( "Does not resolve to an integer value", begin );
            }
            else{
                beginOk = true;
            }
            if( endValue == null ){
                stack.error( "Not a constant value", end );
            }
            else if( !(endValue instanceof IntegerValue )){
                stack.error( "Does not resolve to an integer value", end );
            }
            else{
                endOk = true;
            }
            
            if( beginOk && endOk && counter != null ){
                int beginIndex = ((IntegerValue)beginValue).intValue();
                int endIndex = ((IntegerValue)endValue).intValue();
                counter.range( beginIndex, endIndex, begin, end, this );
            }
        }
    }
    
    private void checkType( Expression expr, AnalyzeStack stack ){
        if( expr == null )
            return;
        
        Type type = expr.resolveType();
        if( type == null )
            return;
        
        BaseType base = type.asBase();
        if( base == null ){
            stack.error( "Not an integer type", expr );
        }
        
        if( !base.isIntegerType() ){
            stack.error( "Not an integer type", expr );
        }
    }
    
    public void setBegin( Expression begin ){
        setField( 0, begin );
    }
    public Expression getBegin(){
        return (Expression)getNoError( 0 );
    }
    
    public void setEnd( Expression end ){
        setField( 1, end );
    }
    public Expression getEnd(){
        return (Expression)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !(node instanceof Expression ))
            throw new ASTException( node, "Must be an Expression" );
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
