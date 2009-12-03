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
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WordRule;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.utility.preferences.IMultiPreferenceProvider;
import tinyos.yeti.utility.preferences.IPreferenceProvider;
import tinyos.yeti.utility.preferences.PreferenceToken;
import tinyos.yeti.utility.preferences.TextAttributeConstants;

public class PreprocessorDirectiveScanner extends RuleBasedScanner implements IEditorTokenScanner{
	private String[] directives = {
			"#define",
			"#undef",
			
			"#if",
			"#ifdef",
			"#ifndef",
			"#else",
			"#elif",
			"#endif",
			
			"#include",
			
			"#pragma",
			
			"#error",
			"#warning",
			
			"#line"
	};
	
    static class DirectiveDetector implements IWordDetector {
        public boolean isWordStart(char c) {
            return c == '#';
        }

        public boolean isWordPart(char c) {
            return Character.isLetter(c);
        }
    }
	
	public PreprocessorDirectiveScanner( IMultiPreferenceProvider preferences ) {
    	// create tokens
    	IPreferenceProvider<TextAttribute> provider = preferences.getTextAttributes();
//
//        IToken singleLineCommentToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.COMMENT_SINGLE_LINE, provider );
//        IToken multiLineCommentToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.COMMENT_MULTI_LINE, provider );
//        IToken nescDocToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.NESC_DOC_COMMENT, provider );
        
    	IToken preprocessorToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.PREPROCESSOR, provider );
    	IToken directiveToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.PREPROCESSOR_DIRECTIVE, provider );


        List<IRule> rules = new ArrayList<IRule>();
        
        setDefaultReturnToken( preprocessorToken );
        
        WordRule words = new WordRule( new DirectiveDetector() );
        for( String directive : directives ){
        	words.addWord( directive, directiveToken );
        }
        rules.add( words );
        
        setRules( rules.toArray( new IRule[ rules.size() ]));
	}
	
	public void setEditor( NesCEditor editor ){
		// ignore
	}
}
