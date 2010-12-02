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
package tinyos.yeti.preprocessor.output;

import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.parser.ElementVisitor;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class TokenSequenceToSource implements ElementVisitor{
	private StringBuilder builder = new StringBuilder();
	
	public static String toString( PreprocessorElement sequence ){
		TokenSequenceToSource builder = new TokenSequenceToSource();
		sequence.visit( builder );
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return builder.toString();
	}
	
	public void visit( PreprocessorElement element ) {
		PreprocessorToken token = element.getToken();
		if( token != null ){
			String text = token.getText();
			if( text != null ){
				builder.append( text );
			}
		}
	}
	public void endVisit(PreprocessorElement element) {
		// ignore
	}
}
