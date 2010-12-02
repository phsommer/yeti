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
package tinyos.yeti.nesc12.parser.ast.nodes.nesc;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class InterfaceReference extends AbstractFixedASTNode {
    public static final String NAME = "name";
    public static final String RENAME = "rename";
    
    public InterfaceReference(){
        super( "InterfaceReference", NAME, RENAME );
    }
    
    public InterfaceReference( ASTNode name, ASTNode rename ){
        this();
        setField( NAME, name );
        setField( RENAME, rename );
    }
    
    public InterfaceReference( InterfaceType name, Identifier rename ){
        this();
        setName( name );
        setRename( rename );
    }
    
    public boolean resolveIsUsed(){
        if( isResolved( "used" ))
            return resolved( "used" );
        
        return resolved( "used", false );
    }
    
    public boolean resolveIsProvided(){
        if( isResolved( "provided" ))
            return resolved( "provided" );
        
        return resolved( "provided", false );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.present( Access.ACCESS ) ){
            if( stack.present( Access.ACCESS_USES  )){
                resolved( "used", true );
            }
            if( stack.present( Access.ACCESS_PROVIDES )){
                resolved( "provided", true );
            }
        }
    }
    
    public void setName( InterfaceType name ){
        setField( 0, name );
    }
    public InterfaceType getName(){
        return (InterfaceType)getNoError( 0 );
    }
    
    public void setRename( Identifier rename ){
        setField( 1, rename );
    }
    public Identifier getRename(){
        return (Identifier)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof InterfaceType ) )
                throw new ASTException( node, "Must be an InterfaceType" );
        }
        if( index == 1 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
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
}
