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

import java.util.List;

import org.eclipse.jface.text.BadLocationException;

import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.ep.rules.ReverseWordReader;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;

public class InterfaceFunctionCallRule implements IProposalRule{
    
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            int offset = collector.getOffset();
            IDocumentMap document = collector.getLocation().getDocument();
            
            int point = RuleUtility.begin( offset-1, collector.getLocation().getDocument(), "." );
            if( point < 0 )
            	return;
            
            int block = RuleUtility.blockBegin( offset-1, document );
            if( block >= 0 ){
                String word = RuleUtility.reverseWord( block-1, document );
                if( "implementation".equals( word )){
                    return;
                }
            }
            
            INesC12Location location = ast.getOffsetInput( point-1 );
            ReverseWordReader reader = new ReverseWordReader( document, point-1 );
            
            String name = reader.previous();
            String key = reader.previous();
            
            if( name == null )
            	return;
            
            boolean noCommand = "signal".equals( key );
            boolean noEvent = "call".equals( key );
            
            List<InterfaceReferenceModelConnection> references = ast.getRanges().getInterfaceReferences( location.getInputfileOffset() );
            for( InterfaceReferenceModelConnection reference : references ){
                if( reference.getName().toIdentifier().equals( name )){
                    NesCInterfaceReference interfaceReference = reference.resolve( ast.getBindingResolver() );
                    if( interfaceReference != null ){
                        NesCInterface interfaze = interfaceReference.getParameterizedReference();
                        if( interfaze != null ){
                            for( int i = 0, n = interfaze.getFieldCount(); i<n; i++ ){
                                Field field = interfaze.getField( i );
                                Modifiers modifier = field.getModifiers();
                                // a -> !b is the same as !a | !b
                                if( modifier == null || ((!noEvent || !modifier.isEvent()) && (!noCommand || !modifier.isCommand()))){
                                	collector.add( ProposalUtility.createProposal( field, collector.getLocation(), ast ));
                                }
                            }
                        }
                    }
                }
            }
        }
        catch( BadLocationException ex ){
            // ignore
        }
    }
}
