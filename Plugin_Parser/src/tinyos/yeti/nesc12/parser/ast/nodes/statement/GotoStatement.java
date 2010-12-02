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
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.LabeledStatement.LabelResolver;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;

public class GotoStatement extends AbstractFixedASTNode implements Statement {
    public static final String LABEL = "label";
    
    public GotoStatement(){
        super( "GotoStatement", LABEL );
    }
    
    public GotoStatement( ASTNode label ){
        this();
        setField( LABEL, label );
    }
    
    public GotoStatement( Identifier label ){
        this();
        setLabel( label );
    }
    
    public void setLabel( Identifier label ){
        setField( 0, label );
    }
    public Identifier getLabel(){
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

    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            LabelResolver resolver = stack.get( LabeledStatement.LABEL_RESOLVER );
            if( resolver != null ){
                resolver.use( this );
            }
        }
    }
    
    public void flow( ControlFlow flow ){
        Identifier name = getLabel();
        Statement target = name == null ? null : flow.labeled( name.getName() );
        if( target == null )
            flow.follow( this );
        else
            flow.link( this, target );
    }
    
    public Type resolveExpressionType(){
        return BaseType.VOID;
    }
    
    public boolean isFunctionEnd(){
        return false;
    }
}
