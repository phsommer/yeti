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
package tinyos.yeti.nesc12.ep.rules.scanner;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class AttributeRule implements IRule {

    /** Token to return for this rule */
    private final IToken fToken;

    /**
     * Creates a new operator rule.
     *
     * @param token Token to use for this rule
     */
    public AttributeRule(IToken token) {
        fToken= token;
    }

    /*
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate( ICharacterScanner scanner ){
    	int character = scanner.read();
    	if( character == '@' ){
    		while( (character = scanner.read()) != ICharacterScanner.EOF ){
    			if( !Character.isJavaIdentifierPart( character )){
    				scanner.unread();
    				break;
    			}
    		}
    		return fToken;
    	}
    	else{
    		scanner.unread();
    		return Token.UNDEFINED;
    	}
    }
}
