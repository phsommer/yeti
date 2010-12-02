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
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;

/**
 * A rule looking for statements like "components x;" or "components new x, y;"
 * @author Benjamin Sigg
 *
 */
public class IncludeComponentRule implements IProposalRule{
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            int offset = collector.getOffset();
            IDocumentMap document = collector.getLocation().getDocument();
            
            boolean found = false;
            boolean hasNew = RuleUtility.hasBefore( offset-1, document, "new" );
            
            if( hasNew || RuleUtility.hasBefore( offset-1, document, "," )){
                if( RuleUtility.hasBeforeWithinStatement( offset-1, document, "components" )){
                    found = true;
                }
            }
            else if( RuleUtility.hasBefore( offset-1, document, "components" )){
                found = true;
            }
            
            if( found ){
                listComponents( ast, collector, hasNew );
            }
        }
        catch( BadLocationException ex ){
            // ignore
        }
    }
    
    protected void listComponents( NesC12AST ast, CompletionProposalCollector collector, boolean hasNew ){
        IDeclaration[] declarations = ast.getResolver().resolveAll( null, Kind.BINARY_COMPONENT, Kind.CONFIGURATION, Kind.MODULE );
        for( IDeclaration declaration : declarations ){
            boolean generic = declaration.getTags().contains( NesC12ASTModel.GENERIC );
            
            if( !hasNew && generic )
                collector.add( ProposalUtility.createProposal( declaration, collector.getLocation(), "new ", null, ast ) );
            else if( hasNew == generic )
                collector.add( ProposalUtility.createProposal( declaration, collector.getLocation(), ast ) );
        }
    }
    
    public static boolean isAfterCommaBegin( NesC12AST ast, CompletionProposalCollector collector ){
    	try{
    		int offset = collector.getOffset();
    		IDocumentMap document = collector.getLocation().getDocument();
    	
    		if( !RuleUtility.hasBefore( offset-1, document, "," ))
    			return false;
    		
    		offset = RuleUtility.begin( offset-1, document, "," );
    		
    		return RuleUtility.hasBeforeWithinStatement( offset-1, document, "components" );
    	}
    	catch( BadLocationException ex ){
    		return false;
    	}
    }
}
