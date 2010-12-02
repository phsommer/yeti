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

import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class Endpoint extends AbstractFixedASTNode{
    public static final String COMPONENT = "component";
    public static final String SPECIFICATION = "specification";
    
    public Endpoint(){
        super( "Endpoint", COMPONENT, SPECIFICATION );
    }
    
    public Endpoint( ASTNode scope, ASTNode name ){
        this();
        setField( COMPONENT, scope );
        setField( SPECIFICATION, name );
    }
    
    public Endpoint( ParameterizedIdentifier component, ParameterizedIdentifier specification ){
        this();
        setComponent( component );
        setSpecification( specification );
    }
    
    /**
     * Tells whether this endpoint has the form "x" (false) or
     * "x.y" (true).
     * @return <code>true</code> if this endpoint uses two identifiers
     */
    public boolean isTwoComponent(){
        return getSpecification() != null;
    }
    
    public LazyRangeDescription[] resolveParameterRanges( AnalyzeStack stack ){
        ParameterizedIdentifier specification = getSpecification();
        if( specification != null )
            return specification.resolveParameterRanges( stack );
        
        ParameterizedIdentifier component = getComponent();
        if( component != null )
            return component.resolveParameterRanges( stack );
        
        return null;
    }
    
    public Expression getParametersSource(){
        ParameterizedIdentifier specification = getSpecification();
        if( specification != null )
            return specification.getExpression();
        
        ParameterizedIdentifier component = getComponent();
        if( component != null )
            return component.getExpression();
        
        return null;
    }
    
    /**
     * Gets the parameters which are in the parameter clause, like "[1, 2, 3]".
     * @return the parameters, <code>null</code> if there are none
     */
    public Value[] resolveParameters(){
        if( isResolved( "parameters" ))
            return resolved( "parameters" );
        
        ParameterizedIdentifier specification = getSpecification();
        if( specification != null )
            return resolved( "parameters", specification.resolveParameters() );
        
        ParameterizedIdentifier component = getComponent();
        if( component != null )
            return resolved( "parameters", component.resolveParameters() );
        
        return resolved( "parameters", null );
    }
    
    /**
     * Gets the component which is accessed by this endpoint. Note that
     * an endpoint does not necessarily have to access a component, so it
     * is perfectly legal if this reference is <code>null</code>
     * @return the reference to the component if there is any
     */
    public ComponentReferenceModelConnection resolveReference( AnalyzeStack stack ){
        if( isResolved( "reference" ))
        	return resolved( "reference" );
        
        ParameterizedIdentifier component = getComponent();
        if( component != null ){
            Identifier name = component.getIdentifier();
            if( name != null ){
                ComponentReferenceModelConnection reference = stack.getComponentReference( name.getName() );
                return resolved( "reference", reference );
            }
        }
        
        return resolved( "reference", null );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        ParameterizedIdentifier component = getComponent();
        
        if( stack.isReportErrors() ){
            if( component != null && component.getExpression() != null ){
                if( getSpecification() != null ){
                    stack.error( "parameters must be placed at the end of the two identifiers", component.getExpression() );
                }
            }
        }
    }
    
    public void setComponent( ParameterizedIdentifier component ){
        setField( 0, component );
    }
    public ParameterizedIdentifier getComponent(){
        return (ParameterizedIdentifier)getNoError( 0 );
    }
    
    public void setSpecification( ParameterizedIdentifier specification ){
        setField( 1, specification );
    }
    public ParameterizedIdentifier getSpecification(){
        return (ParameterizedIdentifier)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof ParameterizedIdentifier ) )
            throw new ASTException( node, "Must be a ParameterizedIdentifier" );
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
