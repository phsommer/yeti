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

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;

public class LabeledStatement extends AbstractFixedASTNode implements Statement{
    public static final Key<LabelResolver> LABEL_RESOLVER = new Key<LabelResolver>( "label resolver" );
    
    public static final String LABEL = "label";
    public static final String STATEMENT = "statement";
    
    public LabeledStatement(){
        super( "LabeledStatement", LABEL, STATEMENT );
    }
    
    public LabeledStatement( ASTNode label, ASTNode statement ){
        this();
        setField( LABEL, label );
        setField( STATEMENT, statement );
    }
    
    public LabeledStatement( Identifier label, Statement statement ){
        this();
        setLabel( label );
        setStatement( statement );
    }
    
    public void setLabel( Identifier label ){
        setField( 0, label );
    }
    public Identifier getLabel(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setStatement( Statement statement ){
        setField( 1, statement );
    }
    public Statement getStatement(){
        return (Statement)getNoError( 1 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
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

    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            LabelResolver resolver = stack.get( LABEL_RESOLVER );
            if( resolver != null ){
                resolver.declare( this );
            }
        }
    }
    
    /**
     * Used to determine which labels were declared more than once, and which
     * were not used at all.
     */
    public static class LabelResolver{
        private AnalyzeStack stack;
        
        private Map<String, LabeledStatement> labelMap = new HashMap<String, LabeledStatement>();
        private Set<String> seenTwice = new HashSet<String>();
        private List<LabeledStatement> labels = new ArrayList<LabeledStatement>();
        
        private List<GotoStatement> gotos = new ArrayList<GotoStatement>();
        
        public LabelResolver( AnalyzeStack stack ){
            this.stack = stack;
        }
        
        public void declare( LabeledStatement statement ){
            Identifier label = statement.getLabel();
            String text = label == null ? null : label.getName();
            if( text != null ){
                labels.add( statement );
                
                if( seenTwice.contains( text )){
                    stack.error( "duplicate label declaration '" + text + "'", label );
                }
                else if( labelMap.containsKey( text )){
                    seenTwice.add( text );
                    stack.error( "duplicate label declaration '" + text + "'", label, labelMap.get( text ).getLabel() );
                }
                else{
                    labelMap.put( text, statement );
                }
            }
        }
        
        public void use( GotoStatement statement ){
            gotos.add( statement );
        }
        
        public void finish(){
            // check whether gotos point to labels
            Set<String> usedLabels = new HashSet<String>();
            for( GotoStatement statement : gotos ){
                Identifier label = statement.getLabel();
                if( label != null ){
                    String text = label.getName();
                    if( text != null ){
                        if( !labelMap.containsKey( text )){
                            stack.error( "label '" + text + "' not declared", label );
                        }
                        else{
                            usedLabels.add( text );
                        }
                    }
                }
            }
            
            // check whether labels are used
            for( LabeledStatement statement : labels ){
                Identifier label = statement.getLabel();
                if( !usedLabels.contains( label.getName() )){
                    stack.warning( "label '" + label.getName() + "' is never used", label );
                }
            }
        }
    }
    
    public void flow( ControlFlow flow ){
        flow.line( this, getStatement() );
    }
    
    public Type resolveExpressionType(){
        Statement statement = getStatement();
        if( statement == null )
            return null;
        return statement.resolveExpressionType();
    }
    
    public boolean isFunctionEnd(){
        return false;
    }
}
