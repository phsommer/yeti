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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import tinyos.yeti.editors.INesCPartitions;

/**
 * A partition scanner for NesC code, find comments, strings and preprocessor
 * directives. This class cannot handle some more exotic code with nested preprocessor
 * directives and comments, it is now replaced with {@link NesCPartitionScanner2}
 * @author Benjamin Sigg
 * @deprecated replaced by {@link NesCPartitionScanner2}
 */
@Deprecated
public class NesCPartitionScanner extends RuleBasedPartitionScanner implements INesCPartitions {
	private IToken preprocessorDirective = new Token( PREPROCESSOR_DIRECTIVE );
    private IToken multiLineComment= new Token( MULTI_LINE_COMMENT );
    private IToken singleLineComment= new Token( NESC_SINGLE_LINE_COMMENT );
    private IToken nesCDoc= new Token( NESC_DOC );
    private IToken stringToken = new Token( NESC_STRING );


    /**
     * Detector for empty comments.
     */
    static class EmptyCommentDetector implements IWordDetector {

        /*
         * @see IWordDetector#isWordStart
         */
        public boolean isWordStart(char c) {
            return (c == '/');
        }

        /*
         * @see IWordDetector#isWordPart
         */
        public boolean isWordPart(char c) {
            return (c == '*' || c == '/');
        }
    }


    /**
     * Word rule for empty comments.
     */
    static class EmptyCommentRule extends WordRule implements IPredicateRule {

        private IToken fSuccessToken;
        /**
         * Constructor for EmptyCommentRule.
         * @param successToken
         */
        public EmptyCommentRule(IToken successToken) {
            super(new EmptyCommentDetector());
            fSuccessToken= successToken;
            addWord("/**/", fSuccessToken); //$NON-NLS-1$
        }

        /*
         * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
         */
        public IToken evaluate(ICharacterScanner scanner, boolean resume) {
            return evaluate(scanner);
        }

        /*
         * @see IPredicateRule#getSuccessToken()
         */
        public IToken getSuccessToken() {
            return fSuccessToken;
        }
    }

    public NesCPartitionScanner() {
        super();
        
        List<IPredicateRule> rules= new ArrayList<IPredicateRule>();

        rules.add(new WhitespaceStartRule(new EndOfLineRule( "#", preprocessorDirective, '\\', true )));
        
        rules.add(new EndOfLineRule("//", singleLineComment));			

        // Add special case word rule.
        EmptyCommentRule wordRule= new EmptyCommentRule(multiLineComment);
        rules.add(wordRule);

        // Add rules for multi-line comments and javadoc.
        rules.add(new SingleLineRule("\"", "\"", stringToken, '\\'));
        rules.add(new SingleLineRule("L\"", "\"", stringToken, '\\'));
        rules.add(new SingleLineRule("'", "'", stringToken, '\\'));
        rules.add(new SingleLineRule("L'", "'", stringToken, '\\'));
        
        rules.add(new MultiLineRule("/**","*/",nesCDoc));
        rules.add(new MultiLineRule("/*","*/",multiLineComment));	

        // Add rule for single line comments.
//      rules.add(new EndOfLineRule("//", singleLineComment));


        IPredicateRule[] result= new IPredicateRule[rules.size()];
        rules.toArray(result);
        setPredicateRules(result);
    }
}
