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
package tinyos.yeti.preprocessor.lexer;

import tinyos.yeti.preprocessor.parser.sym;

public enum Symbols {
    // lexer 3
    WHITESPACE( sym.WHITESPACE ),
    NEWLINE( sym.NEWLINE ),
    QUOTE( sym.QUOTE ),
    TEXT( sym.TEXT ),
    MASKED_QUOTE( sym.MASKED_QUOTE ),
    SHARP( sym.SHARP ),
    CONCAT( sym.CONCAT ),
    GREATER( sym.GREATER ),
    SMALLER( sym.SMALLER ),
    VARARG( sym.VARARG ),
    
    K_DEFINE( sym.K_DEFINE ),
    K_DEFINED( sym.K_DEFINED ),
    K_UNDEF( sym.K_UNDEF ),
    K_IF( sym.K_IF ),
    K_IFDEF( sym.K_IFDEF ),
    K_IFNDEF( sym.K_IFNDEF ),
    K_ELSE( sym.K_ELSE ),
    K_ELIF( sym.K_ELIF ),
    K_ENDIF( sym.K_ENDIF ),
    K_INCLUDE( sym.K_INCLUDE ),
    K_LINE( sym.K_LINE ),
    K_PRAGMA( sym.K_PRAGMA ),
    K_WARNING( sym.K_WARNING ),
    K_ERROR( sym.K_ERROR ),
    
    IDENTIFIER(sym.IDENTIFIER ),
    OPEN(sym.OPEN ),
    CLOSE(sym.CLOSE ),
    COMMA(sym.COMMA ),
    
    EOF( sym.EOF );
    
    private int key;
    private Symbols( int key ){
        this.key = key;
    }
    
    public int sym(){
        return key;
    }
}
