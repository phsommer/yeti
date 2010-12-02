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

public class OperatorRule implements IRule {

    /** NesC operators */
    private final char[] NESC_OPERATORS= {
            ';',
            '(',
            ')',
            '{',
            '}',
            '.',
            '=',
            '/',
            '\\',
            '+',
            '-',
            '*',
            '[',
            ']',
            '<',
            '>', 
            ':', 
            '?',
            '!',
            ',',
            '|',
            '&',
            '^',
            '%',
            '~'
            };
    
    /** Token to return for this rule */
    private final IToken fToken;

    /**
     * Creates a new operator rule.
     *
     * @param token Token to use for this rule
     */
    public OperatorRule(IToken token) {
        fToken= token;
    }

    /**
     * Is this character an operator character?
     *
     * @param character Character to determine whether it is an operator character
     * @return <code>true</code> iff the character is an operator, <code>false</code> otherwise.
     */
    public boolean isOperator(char character) {
        for (int index= 0; index < NESC_OPERATORS.length; index++) {
            if (NESC_OPERATORS[index] == character)
                return true;
        }
        return false;
    }

    /*
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {
        int character= scanner.read();
        if (isOperator((char) character)) {
            do {
                character= scanner.read();
            } while (isOperator((char) character));
            scanner.unread();
            return fToken;
        } else {
            scanner.unread();
            return Token.UNDEFINED;
        }
    }
}
