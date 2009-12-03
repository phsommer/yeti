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

/**
 * Looks out for constructs like "uses{ interface X; }" and makes proposals.
 * @author Benjamin Sigg
 */
public class ComplexIncludeInterfaceRule extends SimpleIncludeInterfaceRule{
    @Override
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            int offset = collector.getOffset();
            IDocumentMap document = collector.getLocation().getDocument();

            boolean hasInterface = RuleUtility.hasBefore( offset-1, document, "interface" );
            boolean hasOpenBracket = RuleUtility.hasBefore( offset-1, document, "{" );
            
            if( hasInterface || hasOpenBracket || RuleUtility.hasBefore( offset-1, document, ";" ) ){
                int begin = RuleUtility.blockBegin( offset, document );
                if( begin >= 0 ){
                    if( RuleUtility.hasBefore( begin-1, document, "uses" ) || 
                            RuleUtility.hasBefore( begin-1, document, "provides" )){
                        
                        listInterfaces( ast, collector, !hasInterface );
                    }
                }
            }
        }
        catch( BadLocationException ex ){
            // ignore
        }
    }
}
