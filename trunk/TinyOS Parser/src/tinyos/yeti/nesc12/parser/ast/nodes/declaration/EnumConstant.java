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

import tinyos.yeti.nesc12.ep.declarations.EnumConstantDeclaration;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionMap;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.NumberValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class EnumConstant extends AbstractFixedASTNode {
    public static final Key<Value> LAST_ENUM_CONST = new Key<Value>( "last enum const value" );
    
    public static final String NAME = "name";
    public static final String CONSTANT = "constant";
    
    public EnumConstant(){
        super( "EnumConstant", NAME, CONSTANT );
    }
    
    public EnumConstant( ASTNode name, ASTNode constant ){
        this();
        setField( NAME, name );
        setField( CONSTANT, constant );
    }
    
    public EnumConstant( Identifier name, Expression constant ){
        this();
        setName( name );
        setConstant( constant );
    }
    
    public void setName( Identifier name ){
        setField( 0, name );
    }
    
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setConstant( Expression constant ){
        setField( 1, constant );
    }
    
    public Expression getConstant(){
        return (Expression)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){    
            if( !( node instanceof Identifier ))
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ){
            if( !( node instanceof Expression ))
                throw new ASTException( node, "Must be an Expression" );
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

    public Value resolveValue(){
    	return resolved( "value" );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        NodeStack nodes = stack.getNodeStack();
        if( stack.isCreateFullModel() ){
            nodes.pushNode( null );
            nodes.setRange( getRange() );
        }
        
        stack.getDeclarationStack().push();
        
        super.resolve( stack );
        stack.checkCancellation();
        
        Identifier name = getName();
        if( name != null ){
            Expression constant = getConstant();
            Value value = null;
            
            if( constant != null ){
                value = constant.resolveConstantValue();
                if( value == null ){
                    if( stack.isReportErrors() ){
                        stack.error( "not a constant value", constant );
                    }
                }
                else{
                    Type type = value.getType();
                    if( type != null ){
                        ConversionTable.instance().check( type, BaseType.S_INT, ConversionMap.assignment( stack, constant, value ) );
                    }
                    value = BaseType.S_INT.cast( value );
                }
            }
            
            if( value == null || !(value instanceof NumberValue || value instanceof UnknownValue)){
            	value = stack.get( LAST_ENUM_CONST );
            		
            	if( value == null ){
            		value = new IntegerValue( BaseType.S_INT, 0 );
            	}
            	else if( value instanceof NumberValue ){
            		value = new IntegerValue( BaseType.S_INT, ((NumberValue)value).asInteger()+1 );
            	}
            }
            
            resolved( "value", value );
            stack.put( LAST_ENUM_CONST, value );
            
            Field field = null;
            String fieldId = null;
            
            if( stack.isCreateFullModel() ){
                FieldModelNode node = new FieldModelNode( stack.name( name ), null, null, BaseType.S_INT, value, true );
                fieldId = node.getIdentifier();
                field = node;
                nodes.setNode( node );
                nodes.include( node, this );
                nodes.addLocation( name );
                nodes.addChild( node, 1 , name );
            }
            
            if( fieldId == null )
        		fieldId = FieldModelNode.fieldId( stack.name( name ), BaseType.S_INT );
            
            if( stack.isCreateDeclarations() ){
            	stack.getDeclarationStack().set( new EnumConstantDeclaration( name.getName(), stack.getParseFile(), null, value ), fieldId );
            }
            else{
            	stack.getDeclarationStack().set( fieldId );
            }
            
            if( field == null ){
                field = stack.getDeclarationStack().set( null, BaseType.S_INT, stack.name( name ), null, value, null );
            }
            stack.putEnum( field, 1 );
        }
        
        if( stack.isCreateFullModel() )
            nodes.popNode( null );
        
        stack.getDeclarationStack().pop();
    }
}







