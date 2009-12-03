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
package tinyos.yeti.nesc12.parser.ast.util.pushers;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

/**
 * Used to report errors concerning redefinitions of structs, unions or
 * enums.
 * @author Benjamin Sigg
 */
public class TagPusher{
    private List<Definition> definitions = new ArrayList<Definition>();
    
    private String name;
    private AnalyzeStack stack;
    
    public TagPusher( String name, AnalyzeStack stack ){
        this.name = name;
        this.stack = stack;
    }
    
    public void push( Identifier name, Type type ){
        definitions.add( new Definition( name, type ) );
    }
    
    /**
     * Checks the validity of the tags that were added.
     */
    public void resolve(){
        /*
         * There must be only one complete definition, and all types
         * must resolve to the same one. The later condition is already 
         * implemented at the locations where the types get read.
         */
        
        // 1. check exactly one complete type
        checkComplete( stack );
    }
    
    private void checkComplete( AnalyzeStack stack ){
        int count = 0;
        for( Definition definition : definitions ){
            if( !definition.type.isIncomplete() ){
                count++;
            }
        }
        
        if( count > 1 ){
            List<Identifier> list = new ArrayList<Identifier>();
            for( Definition definition : definitions ){
                if( !definition.type.isIncomplete() ){
                    list.add( definition.name );
                }
            }
            stack.error( "redefinition of '" + name + "'", list.toArray( new ASTNode[ list.size() ] ) );
        }
    }
    
    /*
    private ASTNode[] getAllNodes(){
        ASTNode[] nodes = new ASTNode[ definitions.size() ];
        int index = 0;
        for( Definition definition : definitions ){
            nodes[ index++ ] = definition.name;
        }
        return nodes;
    }
    */
    
    private class Definition{
        public Identifier name;
        public Type type;
        
        public Definition( Identifier name, Type type ){
            this.name = name;
            this.type = type;
        }
    }
}
