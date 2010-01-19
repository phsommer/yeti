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
package tinyos.yeti.editors.nesc.doc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.IEditorTokenScanner;
import tinyos.yeti.editors.nesc.util.NesCWhitespaceDetector;
import tinyos.yeti.utility.preferences.IMultiPreferenceProvider;
import tinyos.yeti.utility.preferences.IPreferenceProvider;
import tinyos.yeti.utility.preferences.PreferenceToken;
import tinyos.yeti.utility.preferences.TextAttributeConstants;

/**
 * A rule based NesCDoc scanner.
 */
public class NesCDocScanner extends RuleBasedScanner implements IEditorTokenScanner {

    /**
     * A key word detector.
     */
    private static class NesCDocWordDetector implements IWordDetector {
        public boolean isWordStart(char c) {
            return c == '@';
        }

        public boolean isWordPart(char c) {
            return Character.isLetter(c);
        }
    }

    /**
     * A comment word detector.
     */
    static class NesCDocCommentWordDetector implements IWordDetector {
        public boolean isWordPart(char c) {	
            return (c != '<' && c != '>' && 
            		c != '{' && c != '}' &&
            		!Character.isWhitespace( c ) );//Character.isUnicodeIdentifierPart(c);
        }

        public boolean isWordStart(char c) {
            return !Character.isWhitespace( c );
        }
    }

    private static String[] fgKeywords= {
        // I found only a list of keywords for TOS 1.x, see http://www.tinyos.net/tinyos-1.x/doc/nesdoc
        "@author",
        "@param",
        "@return",
        "@modified",
        
        /*
         These keywords seem to be copy & paste from JavaDoc?
         
        "@author", 
        "@deprecated", 
        "@exception", 
        "@param", 
        "@return", 
        "@see", 
        "@serial", 
        "@serialData",
        "@serialField", 
        "@since", 
        "@throws", 
        "@version" */
        };

    private IMultiPreferenceProvider preferences;
    
    /**
     * Create a new NescDoc scanner for the given color provider.
     * 
     * @param preferences the preferences to use in this scanner
     */
    public NesCDocScanner( IMultiPreferenceProvider preferences ) {
        super();
    
        this.preferences = preferences;
        
        buildRules();
    }

    private void buildRules(){
        IPreferenceProvider<TextAttribute> provider = preferences.getTextAttributes();

        IToken keyword = new PreferenceToken<TextAttribute>( TextAttributeConstants.NESC_DOC_KEYWORD, provider );
        IToken tag = new PreferenceToken<TextAttribute>( TextAttributeConstants.NESC_DOC_TAG, provider );
        IToken link = new PreferenceToken<TextAttribute>( TextAttributeConstants.NESC_DOC_LINK, provider );
        IToken task = new PreferenceToken<TextAttribute>( TextAttributeConstants.NESC_DOC_TASK, provider );
        IToken otherToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.NESC_DOC_COMMENT, provider );
        
        List<IRule> list= new ArrayList<IRule>();


        // Add rule for tags.
        list.add(new SingleLineRule("<", ">", tag));//tag)); //$NON-NLS-2$ //$NON-NLS-1$

        // Add rule for links.
        list.add(new SingleLineRule("{", "}", link));// link)); //$NON-NLS-2$ //$NON-NLS-1$

        // Add generic whitespace rule.
        list.add(new WhitespaceRule(new NesCWhitespaceDetector()));

        // Add word rule for keywords.
        WordRule wordRule= new WordRule(new NesCDocWordDetector(), keyword );
        for (int i= 0; i < fgKeywords.length; i++)
            wordRule.addWord(fgKeywords[i], keyword );
        
        list.add(wordRule);
        
        // Add word rule for task tags
        list.add( new TaskTagWordRule( task ) );

        //Comment-Rule
        list.add( new WordRule( new NesCDocCommentWordDetector(), otherToken ));

        setDefaultReturnToken( otherToken );
        
        IRule[] result= new IRule[list.size()];
        list.toArray(result);
        setRules(result);
    }

	public void setEditor( NesCEditor editor ){
		// ignore
	}
}
