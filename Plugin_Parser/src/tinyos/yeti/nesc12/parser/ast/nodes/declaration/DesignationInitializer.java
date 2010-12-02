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

public class DesignationInitializer extends AbstractFixedASTNode implements Initializer{
    public static final String DESIGNATION = "designation";
    public static final String INITIALIZER = "initializer";
    
    public DesignationInitializer(){
        super( "DesignationInitializer", DESIGNATION, INITIALIZER );
    }
    
    public DesignationInitializer( ASTNode designation, ASTNode initializer ){
        this();
        setField( DESIGNATION, designation );
        setField( INITIALIZER, initializer );
    }
    
    public DesignationInitializer( DesignatorList designation, Initializer initializer ){
        this();
        setDesignation( designation );
        setInitializer( initializer );
    }
    
    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        DesignatorList designator = getDesignation();
        if( designator != null ){
            return resolved( "type", designator.resolveType() );
        }
        else{
            Initializer init = getInitializer();
            if( init != null ){
                return resolved( "type", init.resolveType() );
            }
        }
        
        return resolved( "type", null );
    }
    
    public boolean isAssignmentable(){
    	return false;
    }
    
    public Value resolveValue(){
    	return null;
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
    }
    
    public void setDesignation( DesignatorList designation ){
        setField( 0, designation );
    }
    
    public DesignatorList getDesignation(){
        return (DesignatorList)getNoError( 0 );
    }
    
    public void setInitializer( Initializer initializer ){
        setField( 1, initializer );
    }
    
    public Initializer getInitializer(){
        return (Initializer)getNoError( 1 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof DesignatorList ) )
                throw new ASTException( node, "Must be a DesignatorList" );
        }
        if( index == 1 ) {
            if( !( node instanceof Initializer ) )
                throw new ASTException( node, "Must be an Initializer" );
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
