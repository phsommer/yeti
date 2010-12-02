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
package tinyos.yeti.nesc12.ep.rules.proposals;

import org.eclipse.jface.text.BadLocationException;

import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

/**
 * A rule that searches the optimal location for a proposal.
 * @author Benjamin Sigg
 */
public abstract class AbstractLocalRule implements IProposalRule{
    private String[] mustBePredecessor;
    private String[] mustNotBePredecessor;
    
    private boolean notWithinComponents = true;
    
    public void setMustBePredecessor( String...mustBePredecessor ){
        this.mustBePredecessor = mustBePredecessor;
    }
    
    public void setMustNotBePredecessor( String...mustNotBePredecessor ){
        this.mustNotBePredecessor = mustNotBePredecessor;
    }
    
    public void setNotWithinComponents( boolean notWithinComponents ) {
		this.notWithinComponents = notWithinComponents;
	}
    
    protected abstract void propose( NesC12AST ast, CompletionProposalCollector collector, INesC12Location location );
    
    protected boolean checkPredecessor( int offset, IDocumentMap document ) throws BadLocationException{
        if( mustNotBePredecessor != null ){
            for( String check : mustNotBePredecessor ){
                if( RuleUtility.hasBefore( offset, document, check ))
                    return false;
            }
        }
        if( mustBePredecessor != null ){
            for( String check : mustBePredecessor ){
                if( RuleUtility.hasBefore( offset, document, check )){
                    return true;
                }
            }
            return false;
        }
    
        return true;
    }
    
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            if( ast.getRanges() == null )
                return;
            
            
            if( notWithinComponents ){
            	if( IncludeComponentRule.isAfterCommaBegin( ast, collector )){
            		return;
            	}
            }
        
            
            // ensure not conflicting with an access rule, no . or -> in front
            IDocumentMap document = collector.getLocation().getDocument();
            
            int offset = collector.getOffset();
            if( !checkPredecessor( offset-1, document ))
                return;
            
            INesC12Location location = ast.getOffsetInput( offset );
            ASTNode node = RuleUtility.nodeAtOrBefore( location, ast.getRoot() );
            if( node == null )
                return;
            
            INesC12Location nodeLocation = ast.getOffsetPreprocessed( node.getRange().getRight() );
            int next = Math.min( offset, nodeLocation.getInputfileOffset() );
            int lastIndex = RuleUtility.lastIndexOf( next, offset - next, document, "}" );
            if( lastIndex >= 0 ){
                next = Math.max( next, lastIndex+2 );
            }
            
            if( next != offset ){
                location = ast.getOffsetInput( next );
            }
            
            propose( ast, collector, location );
        }
        catch( BadLocationException ex ){
            // ignore
        }
    }
    
    
}
