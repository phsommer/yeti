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
package tinyos.yeti.nesc.parser.language.elements;

import java.io.File;

import tinyos.yeti.nesc.scanner.Token;

public class PreprocessorDefineConstant extends Token {

	String replacement = null;
	File f = null;
	
	public PreprocessorDefineConstant(int code, String text, int lineBegin, int charBegin, int charEnd) {
		super(code, text, lineBegin, charBegin, charEnd);
	}

	public PreprocessorDefineConstant(Token t, File f) {
		super(t);
		this.f = f;
	}

	public void setReplacement(String r) {
		this.replacement = r;
	}
	
	public String toString() {
		return text + " -> " + replacement + " @line "+this.line;
	}


}
