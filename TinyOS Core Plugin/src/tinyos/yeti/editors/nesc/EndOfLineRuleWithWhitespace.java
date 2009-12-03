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

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * An {@link EndOfLineRule} that can have whitespaces after its escape
 * character.
 * @author Benjamin Sigg
 */
public class EndOfLineRuleWithWhitespace implements IPredicateRule{
	private IToken token;
	private char[] startSequence;
	private char escapeSequence;
	
	public EndOfLineRuleWithWhitespace( String startSequence, IToken token, char escapeSequence ){
		this.startSequence = startSequence.toCharArray();
		this.escapeSequence = escapeSequence;
		this.token = token;
	}
	
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		if( sequenceDetected( scanner, startSequence, false )){
			char[][] lineDelimiters = scanner.getLegalLineDelimiters();
			while( true ){
				int read = scanner.read();

				if( read == escapeSequence ){
					escape( scanner, lineDelimiters );
				}
				
				if( read == ICharacterScanner.EOF )
					return token;

				for( int i = 0; i < lineDelimiters.length; i++ ){
					if( read == lineDelimiters[i][0] && sequenceDetected( scanner, lineDelimiters[i], true )){
						for( int j = 0, n = lineDelimiters[i].length; j<n; j++ ){
							scanner.unread();
						}
						return token;
					}
				}
			}
		}
		
		return Token.UNDEFINED;
	}
	
	private void escape( ICharacterScanner scanner, char[][] lineDelimiters ){
		while( true ){
			int read = scanner.read();
			
			if( read == ICharacterScanner.EOF )
				return;
			
			for( int i = 0; i < lineDelimiters.length; i++ ){
				if( read == lineDelimiters[i][0] && sequenceDetected( scanner, lineDelimiters[i], true )){
					return;
				}
			}
			
			if( !Character.isWhitespace( read )){
				scanner.unread();
				return;
			}
		}
	}
	
	private boolean sequenceDetected( ICharacterScanner scanner, char[] sequence, boolean firstRead ){
		int count = 0;
		
		for( int i = firstRead ? 1 : 0; i < sequence.length; i++ ){
			count++;
			if( scanner.read() != sequence[i] ){
				for( int j = 0; j < count; j++ ){
					scanner.unread();
				}
				return false;
			}
		}
		
		return true;
	}
	

	public IToken getSuccessToken() {
		return token;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		return evaluate( scanner, false );
	}
}
