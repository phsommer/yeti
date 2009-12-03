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

public class ExtensionExternalDefinition extends AbstractFixedASTNode implements NesCExternalDefinition{
    public static final String DEFINITION = "definition";
    
    public ExtensionExternalDefinition(){
        super( "ExtensionExternalDefinition", DEFINITION ); 
    }
    
    public ExtensionExternalDefinition( NesCExternalDefinition definition ){
        this();
        setDefinition( definition );
    }
    
    public ExtensionExternalDefinition( ASTNode definition ){
        this();
        setField( DEFINITION, definition );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    public void setDefinition( NesCExternalDefinition definition ){
        setField( 0, definition );
    }
    public NesCExternalDefinition getDefinition(){
        return (NesCExternalDefinition)getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof NesCExternalDefinition ) )
            throw new ASTException( node, "Must be a NesCExternalDefinition" );
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
