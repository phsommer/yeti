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
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.nesc12.parser.meta.NamedType;

public class TypedefRule extends AbstractTypeRule{
    @Override
    protected void securePropose( NesC12AST ast, CompletionProposalCollector collector ){    
        try{
            int offset = collector.getOffset();
            IDocumentMap document = collector.getLocation().getDocument();
            String word = RuleUtility.reverseWord( offset-2, document );

            if( "struct".equals( word ) || "nx_struct".equals( word ) || "union".equals( word ) || "nx_union".equals( word ) || "enum".equals( word ) ){
                return;
            }
            
            ASTNode node = RuleUtility.nodeAt( ast.getOffsetInput( offset ), ast.getRoot() );
            if( !isPlaceToAppendType( node ) ){
            	return;
            }
            
            List<NamedType> list = ast.getTypedefs().get( collector.getOffset() );

            for( NamedType tag : list ){
                collector.add( ProposalUtility.createProposal( tag.getName(), tag.getType(), false, collector.getLocation(), ast ) );
            }
        }
        catch( BadLocationException ex ){
            // ignore
        }
    }
    
    public static boolean isPlaceToAppendType( ASTNode node ){
    	if( node instanceof ConfigurationDeclarationList )
    		return false;
    	
    	if( node instanceof Identifier ){
    		ASTNode parent = node.getParent();
    		if( parent instanceof ParameterizedIdentifier ){
    			if( ((ParameterizedIdentifier)parent).getIdentifier() == node )
    				return false;
    		}
    	}
    	
    	return true;
    }
}
