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

import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.standard.FileHyperlink;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeUtility;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.FieldAccess;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PointerAccess;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.preprocessor.RangeDescription;

public class AccessDataObjectHyperlink implements IHyperlinkRule{
    public void search( NesC12AST ast, HyperlinkCollector collector ){
        ASTNode node = collector.getNode();
        if( !(node instanceof Identifier ))
            return;
        
        ASTNode parent = node.getParent();
        Expression dataObject = null;
        if( parent instanceof PointerAccess ){
            dataObject = ((PointerAccess)parent).getExpression();
        }
        else if( parent instanceof FieldAccess ){
            dataObject = ((FieldAccess)parent).getExpression();
        }
        
        if( dataObject != null ){
            Type base = dataObject.resolveType();
            DataObjectType type = TypeUtility.object( base );
            if( type == null )
                type = TypeUtility.object( TypeUtility.raw( TypeUtility.pointer( base ) ) );
            
            
            if( type != null ){
                String name = ((Identifier)node).getName();
                IFileRegion sourceRegion = ast.getRegion( node );
                
                for( Field field : type.getAllFields() ){
                	Name fieldName = field.getName();
                    if( fieldName != null ){
                        if( name.equals( fieldName.toIdentifier() )){
                            RangeDescription range = field.getRange();
                            IFileRegion targetRegion = RuleUtility.source( range );
                            if( targetRegion != null ){
                                collector.add( new FileHyperlink( sourceRegion, targetRegion ) );
                            }
                        }
                    }
                }
            }
        }
    }
}
