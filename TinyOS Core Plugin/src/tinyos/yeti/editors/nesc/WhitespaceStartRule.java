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
package tinyos.yeti.editors.nesc;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class WhitespaceStartRule implements IPredicateRule{
	private IPredicateRule remainder;
	
	public WhitespaceStartRule( IPredicateRule rule ){
		this.remainder = rule;
	}
	
	public IToken getSuccessToken() {
		return remainder.getSuccessToken();
	}
	
	public IToken evaluate( ICharacterScanner scanner ){
		return evaluate( scanner, false );
	}
	
	public IToken evaluate( ICharacterScanner scanner, boolean resume ){
		if( !resume ){
			int column = scanner.getColumn();

			for( int i = 0; i<column; i++ )
				scanner.unread();

			boolean white = true;
			for( int i = 0; i < column; i++ ){
				int next = scanner.read();
				if( !Character.isWhitespace( (char)next )){
					white = false;
				}
			}

			if( !white )
				return Token.UNDEFINED;
		}
		
		return remainder.evaluate( scanner, resume );
	}
}
