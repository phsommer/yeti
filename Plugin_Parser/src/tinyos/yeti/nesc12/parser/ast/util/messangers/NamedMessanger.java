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
package tinyos.yeti.nesc12.parser.ast.util.messangers;

import java.util.List;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorSpecifier;

public class NamedMessanger extends DefaultModifierCountMessanger{
    private boolean error;
    private Name name;
    
    public NamedMessanger( boolean error, Name name ){
        this.error = error;
        this.name = name;
    }
    
    @Override
    protected void appendName( StringBuilder builder ){
        if( name != null ){
            builder.append( " for '" );
            builder.append( name.toIdentifier() );
            builder.append( "'" );
        }
    }
    
    @Override
    protected void report( AnalyzeStack stack, String message, List<DeclaratorSpecifier> specifiers ){
        if( name == null ){
            if( error )
                stack.error( message, specifiers.toArray( new ASTNode[ specifiers.size() ] ) );
            else
                stack.warning( message, specifiers.toArray( new ASTNode[ specifiers.size() ] ) );
        }
        else{
            if( error )
                stack.error( message, name.getRange() );
            else
                stack.warning( message, name.getRange() );
        }
    }
}
