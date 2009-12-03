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
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.util.InitializerCounter;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;

public class MultiInitializer extends AbstractFixedASTNode implements Initializer{
    public static final String INITIALIZERS = "initializers";
    
    public MultiInitializer(){
        super( "MultiInitializer", INITIALIZERS );
    }
    
    public MultiInitializer( ASTNode initializers ){
        this();
        setField( INITIALIZERS, initializers );
    }
    
    public MultiInitializer( InitializerList initializers ){
        this();
        setInitializers( initializers );
    }
    
    public Type resolveType() {
        return resolved( "type" );
    }
    
    public boolean isAssignmentable(){
    	return false;
    }
    
    public Value resolveValue(){
    	return null;
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        cleanResolved();
        Type type = null;
        
        InitializerCounter counter = stack.get( INITIALIZER_COUNTER );
        if( counter != null ){
            counter.open( this );
            type = counter.currentType();
        }
        
        stack.push( FieldPusherFactory.STANDARD );
        super.resolve( stack );
        stack.pop( getRight() );
        stack.checkCancellation();
        
        if( counter != null )
            counter.close();
        
        resolved( "type", type );
    }  
    
    public void setInitializers( InitializerList initializers ){
        setField( 0, initializers );
    }
    public InitializerList getInitializers(){
        return (InitializerList)getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof InitializerList ) )
            throw new ASTException( node, "Must be an InitializerList" );
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
