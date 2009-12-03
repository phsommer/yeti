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

import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;

public class LocalField extends AbstractLocalRule{
    public LocalField(){
        setMustNotBePredecessor(
                ".", "->", "<-", "call", "signal", "post", "interface",
                "new", "components", "uses", "provides" );
    }
    
    @Override
    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
    	ASTNode node = RuleUtility.nodeAtOrBefore( ast.getOffsetInput( collector.getOffset() ), ast.getRoot() );
        if( node != null ){
            node = node.getParent();

            if( node instanceof ParameterizedIdentifier )
                return;
            
            while( node != null ){
                if( node instanceof ConfigurationDeclarationList ){
                    return;
                }
                node = node.getParent();
            }
        }
 
        super.propose( ast, collector );
    }
    
    @Override
    protected void propose( NesC12AST ast, CompletionProposalCollector collector, INesC12Location location ){
        List<Field> list = ast.getRanges().getFields( location.getInputfileOffset() );
        for( Field field : list ){
            collector.add( ProposalUtility.createProposal( field, collector.getLocation(), ast ) );
        }
    }
}
