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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;
import tinyos.yeti.nesc12.parser.ast.visitors.ASTVisitorAdapter;

public class SwitchStatement extends AbstractFixedASTNode implements Statement {
    public static final Key<SwitchResolver> SWITCH_RESOLVER = new Key<SwitchResolver>( "switch resolver" ); 
    
    public static final String EXPRESSION = "expression";
    public static final String BODY = "body";
    
    public SwitchStatement(){
        super( "SwitchStatement", EXPRESSION, BODY );
    }
    
    public SwitchStatement( ASTNode expression, ASTNode body ){
        this();
        setField( EXPRESSION, expression );
        setField( BODY, body );
    }
    
    public SwitchStatement( Expression expression, Statement body ){
        this();
        setExpression( expression );
        setBody( body );
    }
    
    public void setExpression( Expression expression ){
        setField( 0, expression );
    }
    public Expression getExpression(){
        return (Expression)getNoError( 0 );
    }
    
    public void setBody( Statement body ){
        setField( 1, body );
    }
    public Statement getBody(){
        return (Statement)getNoError( 1 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Expression ) )
                throw new ASTException( node, "Must be an Expression" );
        }
        if( index == 1 ) {
            if( !( node instanceof Statement ) )
                throw new ASTException( node, "Must be a Statement" );
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

    public boolean resolveHasDefault(){
        return resolved( "default" );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        if( stack.isReportErrors() ){
            SwitchResolver switchResolver = new SwitchResolver( stack );
            stack.put( SWITCH_RESOLVER, switchResolver );
            
            boolean breakable = stack.presentLevel( BreakStatement.BREAKABLE );
            if( !breakable )
                stack.put( BreakStatement.BREAKABLE );
            
            super.resolve( stack );
            stack.checkCancellation();
            
            if( !breakable )
                stack.remove( BreakStatement.BREAKABLE );
            
            stack.remove( SWITCH_RESOLVER );
            switchResolver.finish();
            
            resolved( "default", switchResolver.hasDefault() );
        }
        else{
            super.resolve( stack );
        }
    }
    
    /**
     * Used to check the validity of a switch statement.
     */
    public static class SwitchResolver{
        private AnalyzeStack stack;
        
        private Map<Value, CaseStatement> labels = new HashMap<Value, CaseStatement>();
        private Set<Value> seenTwice = new HashSet<Value>();
        private List<DefaultStatement> defaults = new ArrayList<DefaultStatement>();
        
        public SwitchResolver( AnalyzeStack stack ){
            this.stack = stack;
        }
        
        public void put( Value value, CaseStatement statement ){
        	if( !(value instanceof UnknownValue )){
        		if( seenTwice.contains( value )){
        			stack.error( "case label used more than once", statement.getCondition() );
        		}
        		else if( labels.containsKey( value )){
        			CaseStatement first = labels.get( value );
        			stack.error( "case label used more than once", statement.getCondition(), first.getCondition() );
        			seenTwice.add( value );
        		}
        		else{
        			labels.put( value, statement );
        		}
        	}
        }
        
        public void put( DefaultStatement statement ){
            defaults.add( statement );
        }
        
        public void finish(){
            if( defaults.size() > 1 ){
                Token[] tokens = new Token[ defaults.size() ];
                for( int i = 0, n = tokens.length; i<n; i++ )
                    tokens[i] = defaults.get( i ).getKeyword();
                
                stack.error( "more than one default statement in switch", tokens );
            }
        }
        
        public boolean hasDefault(){
            return defaults.size() > 0;
        }
    }
    
    public void flow( ControlFlow flow ){
        final List<Statement> cases = new ArrayList<Statement>();
        accept( new ASTVisitorAdapter(){
            @Override
            public boolean visit( SwitchStatement node ){
                return node == SwitchStatement.this;
            }
            
            @Override
            public boolean visit( CaseStatement node ){
                cases.add( node );
                return true;
            }
        });
        
        flow.choice( this, cases.toArray( new Statement[ cases.size() ] ) );
        if( !resolveHasDefault() ){
            flow.follow( this );
        }
    }
    
    public Type resolveExpressionType(){
        return BaseType.VOID;
    }
    
    public boolean isFunctionEnd(){
        return false;
    }
}
