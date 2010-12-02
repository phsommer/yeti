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
import tinyos.yeti.nesc12.parser.ast.SimpleRange;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;

public class BaseWarningASTNode extends AbstractASTNode implements ErrorASTNode{
    private String message;
    private Range range;
    
    public BaseWarningASTNode( String message, Range range ){
        super( "warning" );
        this.message = message;
        this.range = range;
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            stack.warning( message, this );
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

    public ASTNode getChild( int index ) {
        return null;
    }

    public int getChildrenCount() {
        return 0;
    }

    public Range getRange() {
        if( range == null ){
            ASTNode parent = getParent();
            if( parent == null )
                return new SimpleRange( -1, -1 );
            
            return parent.getRange();
        }
        
        return range;
    }
}