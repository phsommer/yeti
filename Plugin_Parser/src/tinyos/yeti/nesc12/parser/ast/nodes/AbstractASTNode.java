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
package tinyos.yeti.nesc12.parser.ast.nodes;

import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.IllegalASTArgumentException;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.visitors.ASTPrinterVisitor;
import tinyos.yeti.nesc12.parser.preprocessor.comment.NesCDocComment;


/**
 * An {@link AbstractASTNode} is one node in the abstract syntax tree. Each node
 * is of a certain kind which introduce various restrictions.
 * @author Benjamin Sigg
 */
public abstract class AbstractASTNode implements ASTNode{
	private static final String COMMENTS = "node.comments";
	
    private ASTNode parent;
    private String name;
    private boolean included;
    
    private Map<String, Object> resolved;

    public AbstractASTNode( String name ){
        this.name = name;
    }
    
    public String getASTNodeName() {
        return name;
    }
    
    public ASTNode getParent() {
        return parent;
    }
    
    public void setParent( ASTNode parent ) {
        this.parent = parent;
    }
    
    public boolean isAncestor( ASTNode node ){
        if( node == this )
            return false;
        
        if( parent == node )
            return true;
        
        if( parent == null )
            return false;
        
        return parent.isAncestor( node );
    }
    
    protected <A> A resolved( String id, A value ){
        if( resolved == null )
            resolved = new HashMap<String, Object>();

        resolved.put( id, value );
        return value;
    }
    
    protected boolean isResolved( String id ){
        if( resolved == null )
            return false;
        
        return resolved.containsKey( id );
    }
    
    @SuppressWarnings("unchecked")
    protected <A> A resolved( String id ){
        if( resolved == null )
            return null;
        
        return (A)resolved.get( id );
    }
    
    protected void connectChild( ASTNode node ){
        if( node.getParent() != null )
            throw new IllegalASTArgumentException( this, "New child already has a parent" );
        
        ASTNode parent = this;
        while( parent != null ){
            if( parent == node )
                throw new IllegalASTArgumentException( this, "Can't introduce a cycle in the tree" );
            parent = parent.getParent();
        }
        
        node.setParent( this );
    }
    
    protected void disconnectChild( ASTNode node ){
        if( node.getParent() != this )
            throw new IllegalASTArgumentException( this, "Not a child of this node" );
        node.setParent( this );
    }
    
    public void accept( ASTVisitor visitor ){
        if( visit( visitor )){
            for( int i = 0, n = getChildrenCount(); i<n; i++ ){
                ASTNode node = getChild( i );
                if( node != null ){
                    node.accept( visitor );
                }
            }
        }
        endVisit( visitor );
    }
    
    /**
     * Called when this node is visited.
     * @param visitor the visitor
     * @return <code>true</code> if the children should be visited.
     */
    protected abstract boolean visit( ASTVisitor visitor );
    
    /**
     * Called when the visit to this node ends.
     * @param visitor the visitor
     */
    protected abstract void endVisit( ASTVisitor visitor );
    
    /**
     * This implementation just calls {@link #resolve(AnalyzeStack)} on
     * all non null children of this node.
     */
    public void resolve( AnalyzeStack stack ) {
        stack.checkCancellation();
        cleanResolved();
        
        resolveComments( stack );
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            ASTNode node = getChild( i );
            if( node != null ){
                node.resolve( stack );
            }
        }
    }
    
    protected void cleanResolved(){
    	if( resolved != null ){
    		Object comments = resolved.get( COMMENTS );
    		resolved.clear();
    		resolved.put( COMMENTS, comments );
    	}
    }
    
    public Range getCommentAnchor(){
    	return null;
    }
    
    protected void resolveError( int index, AnalyzeStack stack ){
       ASTNode child = getChild( index );
       if( child instanceof ErrorASTNode ){
           child.resolve( stack );
       }
    }
    
    protected void resolveComments( AnalyzeStack stack ){
    	NesCDocComment[] comments = getComments();
    	if( comments != null ){
    		for( NesCDocComment comment : comments ){
    			comment.resolve( this, stack );
    		}
    	}
    }
    
    protected void resolve( int index, AnalyzeStack stack ){
        ASTNode child = getChild( index );
        if( child != null ){
            child.resolve( stack );
        }
    }
    
    public void setComments( NesCDocComment[] comments ){
	    resolved( COMMENTS, comments );	
    }
    
    public NesCDocComment[] getComments(){
	    return resolved( COMMENTS );
    }
    
    public boolean isIncluded(){
        return included;
    }
    
    public void setIncluded( boolean included ){
        this.included = included;
    }
    
    @Override
    public String toString() {
        ASTPrinterVisitor visitor = new ASTPrinterVisitor();
        accept( visitor );
        return visitor.toString();
    }
}
