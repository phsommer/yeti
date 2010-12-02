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
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType.Kind;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.meta.NamedType;

/**
 * To include typedef-names like "point" where "struct point{ int x; int y; };"
 * was used earlier.
 * @author Benjamin Sigg
 */
public class TypeTagRule extends AbstractTypeRule {
    @Override
    protected void securePropose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            int offset = collector.getOffset();
            
            IDocumentMap document = collector.getLocation().getDocument();
            String word = RuleUtility.reverseWord( offset-2, document );
            
            boolean structWord = "struct".equals( word );
            boolean nxStructWord = "nx_struct".equals( word );
            boolean unionWord = "union".equals( word );
            boolean nxUnionWord = "nx_union".equals( word );
            boolean enumWord = "enum".equals( word );

            List<NamedType> list = ast.getRanges().getTypeTags( offset );
            
            if( enumWord ){
                for( NamedType tag : list ){
                    Type type = tag.getType();
                    if( type.asTypedefType() == null && type.asEnumType() != null ){
                        collector.add( ProposalUtility.createProposal( tag.getName(), type, false, collector.getLocation(), ast ) );
                    }
                }                
            }
            else if( structWord || nxStructWord || unionWord || nxUnionWord ){
                DataObjectType.Kind kind = null;
                if( structWord )
                    kind = Kind.STRUCT;
                else if( nxStructWord )
                    kind = Kind.NX_STRUCT;
                else if( unionWord )
                    kind = Kind.UNION;
                else if( nxUnionWord )
                    kind = Kind.NX_UNION;
                
                for( NamedType tag : list ){
                    Type type = tag.getType();
                    if( type.asTypedefType() == null ){
                        DataObjectType object = type.asDataObjectType();
                        if( object != null && object.getKind() == kind ){
                            collector.add( ProposalUtility.createProposal( tag.getName(), type, false, collector.getLocation(), ast ) );
                        }
                    }
                }
            }
            else{
            	ASTNode node = RuleUtility.nodeAt( ast.getOffsetInput( offset ), ast.getRoot() );
            	if( !TypedefRule.isPlaceToAppendType( node ) ){
                	return;
                }
                
            	
                for( NamedType tag : list ){
                    collector.add( ProposalUtility.createProposal( tag.getName(), tag.getType(), true, collector.getLocation(), ast ) );
                }
            }
        }
        catch( BadLocationException ex ){
            // ignore
        }
    }
}
