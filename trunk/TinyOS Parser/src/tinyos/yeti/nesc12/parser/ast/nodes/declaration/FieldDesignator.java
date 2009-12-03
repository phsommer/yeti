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
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.InitializerCounter;

public class FieldDesignator extends AbstractFixedASTNode implements Designator {
    public static final String NAME = "name";
    
    public FieldDesignator(){
        super( "FieldDesignator", NAME );
    }
    
    public FieldDesignator( ASTNode name ){
        this();
        setField( NAME, name );
    }
    
    public FieldDesignator( Identifier name ){
        this();
        setName( name );
    }
    
    public Type resolveType( Type base ) {
        String id = "type"+base;
        if( isResolved( id ))
            return resolved( id );
        
        if( base.asDataObjectType() == null )
            return resolved( id, null );
        Identifier name = getName();
        if( name == null )
            return resolved( id, null );
        
        DataObjectType data = base.asDataObjectType();
        Field field = data.getField( new SimpleName( null, name.getName() ));
        if( field == null )
            return resolved( id, null );
        
        return resolved( id, field.getType() );
    }
    
    public Field resolveField(){
        return resolved( "field" );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        cleanResolved();
        
        Identifier name = getName();
        if( name != null ){
            name.resolve( stack );
            stack.checkCancellation();
            
            InitializerCounter counter = stack.get( Initializer.INITIALIZER_COUNTER );
            if( counter != null ){
                Field field = counter.field( stack.name( name ), name );
                resolved( "field", field );
                
                if( stack.isCreateReferences() && field != null ){
                	stack.reference( this, field.getPath() );
                }
            }
        }
        else{
            resolveError( 0, stack );
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
}
