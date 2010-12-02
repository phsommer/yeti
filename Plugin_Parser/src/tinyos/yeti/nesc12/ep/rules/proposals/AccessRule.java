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

import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

/**
 * A rule that analyzes statements like "x . |" (where | is the cursor) and finds
 * out what "x" is.
 * @author Benjamin Sigg
 */
public abstract class AccessRule implements IProposalRule{
    private String accessSign;

    public AccessRule( String accessSign ){
        this.accessSign = accessSign;
    }

    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            int offset = RuleUtility.begin( collector.getOffset()-1, collector.getLocation().getDocument(), accessSign );
            if( offset < 0 )
                return;
    
            INesC12Location location = ast.getOffsetInput( offset );
            
            offset = RuleUtility.reverseWhitespace( location, collector.getLocation().getDocument() );
            if( offset < 0 )
                return;
    
            ASTNode node = RuleUtility.nodeAt( location, ast.getRoot() );
            if( node == null )
                return;
    
            // +1: to be after or within the access sign
            node = RuleUtility.highestWithout( ast.getOffsetInput( offset+1 ), node );
            propose( node, ast, collector );
        }
        catch( BadLocationException ex ){
            ex.printStackTrace();
        }
    }

    /**
     * Called if the access sign was found before the cursor.
     * @param beforeAccessSign the node before the access sign, might be <code>null</code>
     * @param ast the abstract syntax tree
     * @param collector information about what the user is doing
     */
    protected abstract void propose( ASTNode beforeAccessSign, NesC12AST ast, CompletionProposalCollector collector );
}
