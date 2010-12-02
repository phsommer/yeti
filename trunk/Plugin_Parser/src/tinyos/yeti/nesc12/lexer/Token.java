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
package tinyos.yeti.nesc12.lexer;

import tinyos.yeti.nesc12.parser.ast.Range;

public class Token implements Range{
    private String text;
    private int scope;
    private int left;
    private int right;
    
    public Token( String text, int left, int right, int scope ){
        this.text = text;
        this.scope = scope;
        this.left = left;
        this.right = right;
    }
    
    public String getText() {
        return text;
    }
    
    public int getScopeLevel() {
        return scope;
    }
    
    public int getLeft() {
        return left;
    }
    
    public int getRight() {
        return right;
    }
}
