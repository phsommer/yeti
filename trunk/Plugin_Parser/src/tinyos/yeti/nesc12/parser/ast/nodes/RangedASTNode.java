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

import tinyos.yeti.nesc12.parser.ast.Range;

public abstract class RangedASTNode extends AbstractASTNode implements Range{

    private int left = -1;
    private int right = -1;
    
    /**
     * Creates a new node.
     * @param name the name of this node
     */
    protected RangedASTNode( String name ){
        super( name );
    }
    
    public void setRanges( int left, int right ){
        this.left = left;
        this.right = right;
    }
    
    public void setRanges( Range left, Range right ){
        this.left = left == null ? -1 : left.getLeft();
        this.right = right == null ? -1 : right.getRight();
    }
    
    public void setLeft( int left ){
        this.left = left;
    }
    
    public void setRight( int right ){
        this.right = right;
    }
    
    public Range getRange() {
        return this;
    }

    public int getLeft() {
        if( left != -1 )
            return left;
        
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            ASTNode child = getChild( i );
            if( child != null ){
                Range range = child.getRange();
                if( range != null ){
                    int result = range.getLeft();
                    if( result >= 0 ){
                        return result;
                    }
                }
            }
        }
        
        return 0;
    }
    
    public int getRight() {
        if( right != -1 )
            return right;
        
        for( int i = getChildrenCount()-1; i >= 0; i-- ){
            ASTNode child = getChild( i );
            if( child != null ){
                Range range = child.getRange();
                if( range != null ){
                    int result = range.getRight();
                    if( result >= 0 ){
                        return result;
                    }
                }
            }
        }
        
        return 0;
    }
}
