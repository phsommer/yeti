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

import java.io.IOException;

import java_cup.runtime.Symbol;

/**
 * {@link PreprocessorScanner}s build a chain of scanners and each scanner may
 * influence the stream of tokens. There are three scanners:
 * <ul>
 * 	<li>{@link PreprocessorLexer}: the basic lexer. In its basic configuration
 * this scanner reads tokens directly from a file, with the help of {@link Stream}s
 * the tokens can be modified. </li>
 *  <li>{@link MacroLexer}: Built upon the {@link PreprocessorLexer} this scanner
 *  searches for identifiers and replaces them by macros. </li>
 *  <li>{@link ConditionalLexer}: built upon the {@link MacroLexer} this
 *  scanner detects if, elif, ifdef... instructions, it is responsible of
 *  telling the {@link MacroLexer} when to apply macros, and when not.</li>
 * </ul> 
 * @author Benjamin Sigg
 *
 */
public interface PreprocessorScanner {
    public Symbol next() throws IOException;
    public PreprocessorLexer getBase();
}
