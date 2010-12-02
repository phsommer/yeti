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
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;

public class TaskRule extends AbstractLocalRule{
    public TaskRule(){
        setMustBePredecessor( "post" );
    }
    
    @Override
    protected void propose( NesC12AST ast, CompletionProposalCollector collector, INesC12Location location ){
        List<Field> fields = ast.getRanges().getFields( location.getInputfileOffset() );
        for( Field field : fields ){
            Modifiers modifiers = field.getModifiers();
            if( modifiers != null && modifiers.isTask() ){
                collector.add( ProposalUtility.createProposal( field, collector.getLocation(), ast ) );
            }
        }
    }
}
