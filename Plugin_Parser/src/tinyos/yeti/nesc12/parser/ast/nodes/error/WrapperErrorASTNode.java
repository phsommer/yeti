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
package tinyos.yeti.nesc12.parser.ast.nodes.error;

import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;
import tinyos.yeti.nesc12.parser.preprocessor.comment.NesCDocComment;

public class WrapperErrorASTNode implements ErrorASTNode{
    private ASTNode parent;
    private ASTNode base;
    
    public WrapperErrorASTNode( ASTNode base ){
        this.base = base;
        base.setParent( this );
    }
    
    public boolean isAncestor( ASTNode node ){
        if( parent == this )
            return false;
        
        if( parent == node )
            return true;
        
        if( parent == null )
            return false;
        
        return parent.isAncestor( node );
    }

    @Override
    public String toString(){
        return getASTNodeName();
    }
    
    public void accept( ASTVisitor visitor ){
        base.accept( visitor );
    }

    public String getASTNodeName(){
        return "Error wrapper";
    }

    public ASTNode getChild( int index ){
        return base;
    }

    public int getChildrenCount(){
        return 1;
    }

    public ASTNode getParent(){
        return parent;
    }

    public Range getRange(){
        return base.getRange();
    }

    public boolean isIncluded(){
        return base.isIncluded();
    }
    
    public Range getCommentAnchor(){
    	return null;
    }

    public void resolve( AnalyzeStack stack ){
        base.resolve( stack );
    }

    public void setComments( NesCDocComment[] comments ){
    	base.setComments( comments );
    }
    
    public NesCDocComment[] getComments(){
	    return base.getComments();
    }
    
    public void setIncluded( boolean included ){
        base.setIncluded( included );
    }

    public void setParent( ASTNode parent ){
        this.parent = parent;
    }
}
