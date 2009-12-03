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

import org.eclipse.jface.text.BadLocationException;

import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.EnumerationConstant;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.preprocessor.RangeDescription;

/**
 * Hyperlinks for local fields.
 * @author Benjamin Sigg
 *
 */
public class LocalFieldHyperlink implements IHyperlinkRule{
    public void search( NesC12AST ast, HyperlinkCollector collector ){
        try{
            if( ast.getRanges() == null )
                return;
            
            // ensure not conflicting with an access rule, no . or -> in front
            IDocumentMap document = collector.getLocation().getDocument();
            
            int offset = collector.getOffset().getInputfileOffset();
            
            if( RuleUtility.hasBefore( offset, document, "." ))
                return;
            
            if( RuleUtility.hasBefore( offset, document, "->" ))
                return;
            
            INesC12Location location = collector.getOffset();
            ASTNode node = collector.getNode();
            
            if( !(node instanceof Identifier) )
                return;
            
            ASTNode parent = node.getParent();
            if( parent instanceof IdentifierExpression ||
                    parent instanceof EnumerationConstant ){
                
                String name = ((Identifier)node).getName();
                List<Field> fields = ast.getRanges().getFields( location.getInputfileOffset() );
                
                IFileRegion sourceRegion = ast.getRegion( node );
                
                for( Field field : fields ){
                    if( field.getName() != null && field.getName().toIdentifier().equals( name )){
                        RangeDescription range = field.getRange();
                        IFileRegion targetRegion = RuleUtility.source( range );
                        if( targetRegion != null ){
                            FileHyperlink hyperlink = new FileHyperlink( sourceRegion, targetRegion );
                            collector.add( hyperlink );
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
