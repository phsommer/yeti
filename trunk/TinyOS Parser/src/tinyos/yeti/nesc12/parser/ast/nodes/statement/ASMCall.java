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
package tinyos.yeti.nesc12.parser.ast.nodes.statement;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinition;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;

public class ASMCall extends AbstractFixedASTNode implements Statement, NesCExternalDefinition{
    public static final String ARGUMENT = "argument";
    private boolean volatileAsm = false;
    
    public ASMCall(){
        super( "ASMCall", ARGUMENT );
    }
    
    public ASMCall( boolean volatileAsm, ASMArgumentsList arguments ){
        this();
        setArgument( arguments );
    }
    
    public void setVolatileAsm( boolean volatileAsm ){
        this.volatileAsm = volatileAsm;
    }
    public boolean isVolatileAsm(){
        return volatileAsm;
    }
    
    public void setArgument( ASMArgumentsList argument ){
        setField( 0, argument );
    }
    public ASMArgumentsList getArgument(){
        return (ASMArgumentsList)getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof ASMArgumentsList ) )
            throw new ASTException( node, "Must be an ASMArgumentsList" );
    }
    
    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }

    public void flow( ControlFlow flow ){
        // does nothing contribute
        flow.follow( this );
    }
    
    public Type resolveExpressionType(){
        return BaseType.VOID;
    }

    public boolean isFunctionEnd(){
        return false;
    }
}
