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
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;

public abstract class AbstractTypeRule implements IProposalRule{
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        if( checkValidLocation( ast, collector )){
            securePropose( ast, collector );
        }
    }
    
    protected abstract void securePropose( NesC12AST ast, CompletionProposalCollector collector );
    
    protected boolean checkValidLocation( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            int offset = collector.getOffset();
            IDocumentMap document = collector.getLocation().getDocument();
            
            if( RuleUtility.hasBefore( offset-1, document, "." ) ||
                RuleUtility.hasBefore( offset-1, document, "->" ) || 
                RuleUtility.hasBefore( offset-1, document, "<-" )){
                
                return false;
            }
            
            String word = RuleUtility.reverseWord( offset-2, document );
            if(
                    "call".equals( word ) || 
                    "signal".equals( word ) || 
                    "post".equals( word ) ||
                    "uses".equals( word ) ||
                    "prodives".equals( word ) ||
                    "interface".equals( word ) ||
                    "component".equals( word ) ||
                    "components".equals( word ) ||
                    "module".equals( word ) ||
                    "configuration".equals( word ) ||
                    "new".equals( word )){
            	return false;
            }
            
            if( IncludeComponentRule.isAfterCommaBegin( ast, collector ))
            	return false;
            
            return true;
        }
        catch( BadLocationException ex ){
            return false;
        }
    }
}
