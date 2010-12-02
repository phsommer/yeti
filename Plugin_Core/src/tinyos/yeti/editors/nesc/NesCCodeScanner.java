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

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.util.NesCWhitespaceDetector;
import tinyos.yeti.editors.nesc.util.NesCWordDetector;
import tinyos.yeti.utility.preferences.IMultiPreferenceProvider;
import tinyos.yeti.utility.preferences.IPreferenceProvider;
import tinyos.yeti.utility.preferences.PreferenceToken;
import tinyos.yeti.utility.preferences.TextAttributeConstants;

public class NesCCodeScanner extends RuleBasedScanner implements IEditorTokenScanner{


    /**
     * Rule to detect nesc operators
     */
    protected class OperatorRule implements IRule {

        /** NesC operators */
        private final char[] NESC_OPERATORS= { ';', '(', ')', '{', '}', '.', '=', '/', '\\', '+', '-', '*', '[', ']', '<', '>', ':', '?', '!', ',', '|', '&', '^', '%', '~'};
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


    private static final String DELIMITER = ",";
    private static final String SINGLE_LINE_COMMENT = "//";
    private static final String PREPROCESSOR_SYMBOL = "#";

    private static String[] Keywords1 = Messages.getString("NesCCodeScanner.Keywords1").split(DELIMITER); //$NON-NLS-1$ //$NON-NLS-2$
    private static String[] Keywords2 = Messages.getString("NesCCodeScanner.Keywords2").split(DELIMITER); //$NON-NLS-1$
    private static String[] Keywords3 = Messages.getString("NesCCodeScanner.Keywords3").split(DELIMITER); //$NON-NLS-1$
    private static String[] Functions = Messages.getString("NesCCodeScanner.Functions").split(DELIMITER); //$NON-NLS-1$
    private static String[] CKeywords = Messages.getString("NesCCodeScanner.CKeywords").split(DELIMITER); //$NON-NLS-1$
    private static String[] VarTypes = Messages.getString("NesCCodeScanner.VarTypes").split(DELIMITER); //$NON-NLS-1$

    public NesCCodeScanner( IMultiPreferenceProvider preferences ) {
    	// create tokens
    	IPreferenceProvider<TextAttribute> provider = preferences.getTextAttributes();

        IToken singleLineCommentToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.COMMENT_SINGLE_LINE, provider );
        IToken preprocessorToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.PREPROCESSOR, provider );

        IToken keyword1Token = new PreferenceToken<TextAttribute>( TextAttributeConstants.KEYWORDS1, provider );
        IToken keyword2Token = new PreferenceToken<TextAttribute>( TextAttributeConstants.KEYWORDS1, provider );
        IToken keyword3Token = new PreferenceToken<TextAttribute>( TextAttributeConstants.KEYWORDS1, provider );

        IToken functionToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.FUNCTION, provider );
        IToken cKeywordsToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.CKEYWORDS, provider );
        IToken varTypesToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.VARTYPES, provider );

        IToken stringToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.STRING, provider );

        IToken operatorToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.OPERATOR, provider );

        IToken otherToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.DEFAULT, provider );

        // construct rule
        List<IRule> rules= new ArrayList<IRule>();

        // Different types of comments
        rules.add(new EndOfLineRule(SINGLE_LINE_COMMENT, singleLineCommentToken));

        // Different types of keywords
        WordRule wordRule= new WordRule(new NesCWordDetector(), otherToken);
        for (int i = 0; i < Keywords1.length; i++) {
            wordRule.addWord(Keywords1[i], keyword1Token);
        }	
        for (int i = 0; i < Keywords2.length; i++) {
            wordRule.addWord(Keywords2[i], keyword2Token);
        }
        for (int i = 0; i < Keywords3.length; i++) {
            wordRule.addWord(Keywords3[i], keyword3Token);
        }
        for (int i = 0; i < Functions.length; i++) {
            wordRule.addWord(Functions[i], functionToken);
        }
        for (int i = 0; i < CKeywords.length; i++) {
            wordRule.addWord(CKeywords[i], cKeywordsToken);
        }
        for (int i = 0; i < VarTypes.length; i++) {
            wordRule.addWord(VarTypes[i], varTypesToken);
        }

        rules.add(wordRule);


        // Strings
        rules.add(new SingleLineRule("\"","\"", stringToken,'\\'));

        // Other
        // Add rule for operators and brackets
        rules.add(new OperatorRule(operatorToken));

        // Add generic whitespace rule.
        rules.add(new WhitespaceRule(new NesCWhitespaceDetector()));

        // Preprocessor Statement
        rules.add(new EndOfLineRule(PREPROCESSOR_SYMBOL,preprocessorToken,'\\', true)); //$NON-NLS-1$

        IRule[] result= new IRule[rules.size()];
        result = rules.toArray(result);
        setRules(result);
    }
    
    public void setEditor(NesCEditor editor) {
    	// ignore
    }
}

