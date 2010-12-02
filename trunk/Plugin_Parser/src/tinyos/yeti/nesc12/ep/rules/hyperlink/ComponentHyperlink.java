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

import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NewComponent;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.RefComponent;

public class ComponentHyperlink extends DeclarationHyperlink{
    public ComponentHyperlink(){
        super( Kind.CONFIGURATION, Kind.MODULE, Kind.BINARY_COMPONENT );
    }
    
    @Override
    protected boolean valid( Identifier node ){
        ASTNode parent = node.getParent();
        if( parent instanceof RefComponent ){
            return ((RefComponent)parent).getName() == node;
        }
        if( parent instanceof NewComponent ){
            return ((NewComponent)parent).getName() == node;
        }
        return false;
    }
}
