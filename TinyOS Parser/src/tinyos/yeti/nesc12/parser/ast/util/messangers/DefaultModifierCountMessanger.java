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
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorSpecifier;
import tinyos.yeti.nesc12.parser.ast.util.ModifierCount;
import tinyos.yeti.nesc12.parser.ast.util.ModifierCountMessanger;

public class DefaultModifierCountMessanger implements ModifierCountMessanger{
    public void reportMissing( AnalyzeStack stack, int expected, List<DeclaratorSpecifier> found ){
        StringBuilder builder = new StringBuilder();
        int count = Integer.bitCount( expected );
        if( count == 1 ){
            builder.append( "missing modifier" );
            appendName( builder );
            builder.append( ": '" );
            builder.append( ModifierCount.toString( expected ));
            builder.append( "'" );
        }
        else{
            builder.append( "missing modifier, one of: " );
            list( expected, builder );
        }
        report( stack, builder.toString(), found );
    }
    
    public void reportForbidden( AnalyzeStack stack, int forbidden, List<DeclaratorSpecifier> found ){
        StringBuilder builder = new StringBuilder();
        builder.append( "modifier '" );
        builder.append( ModifierCount.toString( forbidden ) );
        builder.append( "' not allowed" );
        appendName( builder );
        report(  stack, builder.toString(), found );   
    }

    public void reportMultiOccurence( AnalyzeStack stack, int mask, List<DeclaratorSpecifier> found ){
        StringBuilder builder = new StringBuilder();
        builder.append( "modifier '" );
        builder.append( ModifierCount.toString( mask ) );
        builder.append( "' can be used only once" );
        appendName( builder );
        report( stack, builder.toString(), found );
    }
    
    public void reportNotSet( AnalyzeStack stack, int set, List<DeclaratorSpecifier> specifiers ){
        StringBuilder builder = new StringBuilder();
        builder.append( "only one of " );
        list( specifiers, builder );
        builder.append( " is allowed" );
        appendName( builder );
        report( stack, builder.toString(), specifiers );
    }
    
    /**
     * May add some text like "for function 'x'" to the message. If text is
     * added to <code>builder</code>, then a trailing whitespace should be 
     * added as well.
     * @param builder the builder that contains the error message
     */
    protected void appendName( StringBuilder builder ){
        // ignore
    }
    
    protected void list( List<DeclaratorSpecifier> specifiers, StringBuilder builder ){
        int found = 0;
        for( DeclaratorSpecifier specifier : specifiers ){
            found |= ModifierCount.typeOf( specifier );
        }
        list( found, builder );
    }
    
    protected void list( int mask, StringBuilder builder ){
        int count = Integer.bitCount( mask );
        int index = count;
        for( int modifier : Modifiers.ALL_MODIFIERS ){
            if( (mask & modifier) == modifier ){
                index--;
                if( index == 0 )
                    builder.append( " or " );
                else if( index+1 < count )
                    builder.append( ", " );

                builder.append( "'" );
                builder.append( ModifierCount.toString( modifier ));
                builder.append( "'" );
            }
        }        
    }
    
    protected void report( AnalyzeStack stack, String message, List<DeclaratorSpecifier> specifiers ){
        stack.error( message, specifiers.toArray( new ASTNode[ specifiers.size() ] ) );
        

    }
}
