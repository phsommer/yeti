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

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;

public interface FieldPusherFactory{
    public static final FieldPusherFactory STANDARD = new FieldPusherFactory(){
        public FieldPusher create( String name, AnalyzeStack stack ){
            return new StandardFieldPusher( name, stack );
        }
    };
    
    public static final FieldPusherFactory INTERFACE = new FieldPusherFactory(){
        public FieldPusher create( String name, AnalyzeStack stack ){
            return new InterfaceFieldPusher( name, stack );
        }
    };
    
    public static final FieldPusherFactory MODULE = new FieldPusherFactory(){
        public FieldPusher create( String name, AnalyzeStack stack ){
            return new ModuleFieldPusher( name, stack );
        }
    };
    
    public static final FieldPusherFactory COMPONENT = new FieldPusherFactory(){
        public FieldPusher create( String name, AnalyzeStack stack ){
            return new ComponentFieldPusher( name, stack );
        }
    };
    
    public FieldPusher create( String name, AnalyzeStack stack );
}
