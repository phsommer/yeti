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
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;

public class CallInterfaceRule extends AbstractLocalRule{
    public CallInterfaceRule(){
        setMustBePredecessor( "call", "signal" );
    }
    
    @Override
    protected void propose( NesC12AST ast, CompletionProposalCollector collector, INesC12Location location ){
        List<InterfaceReferenceModelConnection> list = ast.getRanges().getInterfaceReferences( location.getInputfileOffset() );
        for( InterfaceReferenceModelConnection reference : list ){
            collector.add( ProposalUtility.createProposal( reference, collector.getLocation() ) );
        }
    }
}
