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
package tinyos.yeti.nesc12.ep.rules.hyperlink;

import java.util.List;

import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.EnumDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.IncompleteDataObject;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.meta.NamedType;
import tinyos.yeti.preprocessor.RangeDescription;

public class TypeTagHyperlink implements IHyperlinkRule{
    public void search( NesC12AST ast, HyperlinkCollector collector ){
        ASTNode node = collector.getNode();
        
        if( !(node instanceof Identifier ))
            return;
        
        if( !(node.getParent() instanceof IncompleteDataObject ) && 
                !(node.getParent() instanceof EnumDeclaration ))
            return;
        
        List<NamedType> tags = ast.getRanges().getTypeTags( collector.getOffset().getInputfileOffset() );
        IFileRegion sourceRegion = collector.getSourceRegion();
        String check = ((Identifier)node).getName();
        for( NamedType tag : tags ){
            Name name = tag.getName();
            if( check.equals( name.toIdentifier() )){
                RangeDescription range = name.getRange();
                
                IFileRegion targetRegion = RuleUtility.source( range );
                if( targetRegion != null ){
                    collector.add( new FileHyperlink( sourceRegion, targetRegion ) );
                }
            }
        }
    }
}
