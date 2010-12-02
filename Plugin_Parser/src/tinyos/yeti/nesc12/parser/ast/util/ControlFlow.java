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
package tinyos.yeti.nesc12.parser.ast.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.LabeledStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.nesc12.parser.ast.visitors.ConvergingASTVisitor;

/**
 * Used for analyzing a function for the existance of a return value.
 * @author Benjamin Sigg
 */
public class ControlFlow{
    private Map<Statement, Statement[]> lines = new HashMap<Statement, Statement[]>();
    private Map<Statement, Statement[]> choices = new HashMap<Statement, Statement[]>();
    private Map<Statement, Statement> links = new HashMap<Statement, Statement>();
    
    /** for each statement of a line or choice, its parent is listed up here */
    private Map<Statement, Statement> parents = new HashMap<Statement, Statement>();
    
    /** A list of all known statements */
    private List<Statement> statements = new ArrayList<Statement>();
    
    private Map<String, Statement> labeled = new HashMap<String, Statement>();
    
    /** the set of statements which are at the end of the flow */
    private Set<Statement> flowEnd = new HashSet<Statement>();
    
    /** entry point to the control flow */
    private FlowNode entry;
    
    /** all the nodes of the graph */
    private List<FlowNode> graph;
    
    /**
     * Creates a new controlflow
     * @param source the entry point of the statements
     */
    public ControlFlow( Statement source ){
        // collect all statements
        source.accept( new ConvergingASTVisitor(){
            @Override
            public boolean convergedVisit( ASTNode node ){
                if( node instanceof Statement ){
                    statements.add( (Statement)node );
                    if( node instanceof LabeledStatement ){
                        Identifier label = ((LabeledStatement)node).getLabel();
                        if( label != null ){
                            labeled.put( label.getName(), (Statement)node );
                        }
                    }
                }
                
                return true;
            }

            @Override
            public void convergedEndVisit( ASTNode node ){
                // nothing
            }
        });
        
        // collect flow information
        source.accept( new ConvergingASTVisitor(){
            @Override
            public boolean convergedVisit( ASTNode node ){
                if( node instanceof Statement ){
                    ((Statement)node).flow( ControlFlow.this );
                }
                return true;
            }
            
            @Override
            public void convergedEndVisit( ASTNode node ){
                // nothing
            }
        });
        
        // build flow graph
        toGraph( source );
    }
    
    /**
     * Checks whether every path of this controlflow ends with a 
     * statement that {@link Statement#isFunctionEnd() is a function end}.
     * @return <code>true</code> if every path ends with a returning statement
     */
    public boolean checkReturnsAlways(){
        return entry.checkReturnsAlways();
    }
    
    /**
     * Checks whether at least one path of this controlflow ends with a statement
     * that {@link Statement#isFunctionEnd() is a function end}.
     * @return <code>true</code> if at least one path leads out of this method
     */
    public boolean checkReturnsSometimes(){
        return entry.checkReturnsSometimes();
    }
    
    /**
     * Gets all the nodes of the graph.
     * @return the graph
     */
    public List<FlowNode> getGraph(){
        return graph;
    }
    
    /**
     * Stores a set of statements that would normally be executed one by
     * one (not considering the existance of gotos, returns, if-else, ...). The
     * <code>parent</code>s successor is either one of the statements or 
     * {@link #follow(Statement)} if <code>statements</code> is empty
     * @param parent the parent of <code>statements</code> 
     * @param statements a line of statements, may contain <code>null</code> values
     */
    public void line( Statement parent, Statement... statements ){
        statements = trim( statements );
        lines.put( parent, statements );
        
        for( Statement statement : statements ){
            parents.put( statement, parent );
        }
        
        if( statements.length == 0 )
            follow( parent );
        else
            link( parent, statements[0] );
    }
    
    /**
     * Stores a set of choices for <code>parent</code>. If <code>parent</code> is
     * hit, then one of <code>statements</code> has to be next (if a {@link #follow(Statement)}
     * or a {@link #link(Statement, Statement)} has <code>parent</code> as
     * source, then these links are considered to be additional choices).
     * @param parent the parent node
     * @param statements the possible choices, may contain <code>null</code> values
     */
    public void choice( Statement parent, Statement... statements ){
        statements = trim( statements );
        choices.put( parent, statements );
        
        for( Statement statement : statements ){
            parents.put( statement, parent );
        }
    }

    private Statement[] trim( Statement[] list ){
        int count = 0;
        for( Statement check : list ){
            if( check != null )
                count++;
        }
        
        if( count == list.length )
            return list;
        
        Statement[] result = new Statement[ count ];
        int i = 0;
        for( Statement check : list ){
            if( check != null ){
                result[i++] = check;
            }
        }
        
        return result;
    }
    
    /**
     * Just connects <code>statement</code> with the next statement that
     * follows after <code>previous</code> in a {@link #line(Statement, Statement[])}.
     * @param statement some statement which points to the element after <code>previous</code>
     * @param previous the statement before the element to which <code>statement</code> points to
     */
    public void follow( Statement statement, Statement previous ){
        Statement parent = parents.get( previous );
        while( parent != null ){
            Statement[] line = lines.get( parent );
            if( line != null ){
                for( int i = 0; i < line.length; i++ ){
                    if( line[i] == previous ){
                        if( i+1 < line.length ){
                            link( statement, line[i+1 ]);
                            return;
                        }
                    }
                }
            }
            
            previous = parent;
            parent = parents.get( parent );
        }
        
        flowEnd.add( statement );
    }
    
    /**
     * Just connects <code>statement</code> with the next statement that
     * was in a {@link #line(Statement, Statement[])}.
     * @param statement the statement which does not alter the control flow
     */
    public void follow( Statement statement ){
        follow( statement, statement );
    }
    
    /**
     * Makes sure that after <code>source</code> the statement <code>target</code>
     * gets executed.
     * @param source the source of the link
     * @param target where the link points to
     */
    public void link( Statement source, Statement target ){
        if( target != null ){
            links.put( source, target );
        }
    }
    
    /**
     * Searches the labeled statement <code>label</code>.
     * @param label the name of a statement
     * @return the labeled statement
     */
    public Statement labeled( String label ){
        return labeled.get( label );
    }
    
    private void toGraph( Statement entry ){
        Map<Statement, FlowNode> map = new HashMap<Statement, FlowNode>();
        
        // choices
        for( Map.Entry<Statement, Statement[]> choice : choices.entrySet() ){
            Statement source = choice.getKey();
            for( Statement target : choice.getValue() ){
                getNode( source, map ).connect( getNode( target, map ) );
            }
        }
        
        // direct links
        for( Map.Entry<Statement, Statement> link : links.entrySet() ){
            getNode( link.getKey(), map ).connect( getNode( link.getValue(), map ) );
        }
        
        this.entry = getNode( entry, map );
        graph = new ArrayList<FlowNode>( map.values() );
        
        // find the end of the flows
        for( FlowNode node : graph ){
            node.setFlowEnd( flowEnd.contains( node.getSource() ) );
        }
    }
    
    private FlowNode getNode( Statement statement, Map<Statement, FlowNode> map ){
        FlowNode node = map.get( statement );
        if( node == null ){
            node = new FlowNode( statement );
            map.put( statement, node );
        }
        return node;
    }
    
    private class FlowNode{
        private Statement source;
        private List<FlowNode> targets = new ArrayList<FlowNode>();
        
        private boolean marked = false;
        private boolean flowEnd;
        
        public FlowNode( Statement source ){
            this.source = source;
        }
        
        public void setFlowEnd( boolean flowEnd ){
            this.flowEnd = flowEnd;
        }
        
        public void connect( FlowNode next ){
            targets.add( next );
        }
        
        public Statement getSource(){
            return source;
        }
        
        public boolean checkReturnsAlways(){
            if( !marked ){
                if( source.isFunctionEnd() )
                    return true;
                
                marked = true;
                for( FlowNode next : targets ){
                    if( !next.checkReturnsAlways() ){
                        marked = false;
                        return false;
                    }
                }
                
                marked = false;
                return !flowEnd;
            }
            return true;
        }
        
        public boolean checkReturnsSometimes(){
            if( !marked ){
                if( source.isFunctionEnd() )
                    return true;
                
                marked = true;
                for( FlowNode next : targets ){
                    if( next.checkReturnsSometimes() ){
                        marked = false;
                        return true;
                    }
                }
                
                marked = false;
            }
            
            return false;
        }
        
        public boolean unmark(){
            boolean result = marked;
            marked = false;
            return result;
        }
        
        @Override
        public String toString(){
            StringBuilder builder = new StringBuilder();
            builder.append( source.getClass().getSimpleName() );
            for( FlowNode next : targets ){
                builder.append( "\n\t->" );
                builder.append( next.getSource().getClass().getSimpleName() );
            }
            return builder.toString();
        }
    }
}
