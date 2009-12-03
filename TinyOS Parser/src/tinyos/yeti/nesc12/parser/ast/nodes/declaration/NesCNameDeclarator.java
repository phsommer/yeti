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

import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class NesCNameDeclarator extends AbstractFixedASTNode implements Declarator{
    public static final String INTERFACE = "interface";
    public static final String FUNCTION_NAME = "function";
    
    public NesCNameDeclarator(){
        super( "NesCNameDeclarator", INTERFACE, FUNCTION_NAME );
    }
    
    public NesCNameDeclarator( ASTNode container, ASTNode function ){
        this();
        setField( INTERFACE, container );
        setField( FUNCTION_NAME, function );
    }
    
    public NesCNameDeclarator( Identifier container, Identifier function ){
        this();
        setInterface( container );
        setFunctionName( function );
    }
    
    public Name resolveName() {
        return resolved( "name" );
    }
    
    public Type resolveType( Type name, AnalyzeStack stack ){
        return resolved( "type", name );
    }
    
    public ModelAttribute[] resolveAttributes(){
    	return null;
    }
    
    public Field[] resolveIndices() {
    	return null;
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        Identifier container = getInterface();
        Identifier function = getFunctionName();
        if( container == null || function == null )
            resolved( "name", null );
        else{
            resolved( "name", stack.name( 
                    this,
                    stack.name( container ),
                    stack.name( function ) ));
        }
    }

    public void setInterface( Identifier interfaze ){
        setField( 0, interfaze );
    }
    public Identifier getInterface(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setFunctionName( Identifier function ){
        setField( 1, function );
    }

    public FunctionDeclarator getFunction(){
    	return null;
    }
    
    public Identifier getFunctionName(){
        return (Identifier)getNoError( 1 );
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
}