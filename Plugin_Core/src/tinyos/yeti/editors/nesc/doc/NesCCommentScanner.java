/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2010 ETH Zurich
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
import org.eclipse.jface.text.rules.RuleBasedScanner;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.IEditorTokenScanner;
import tinyos.yeti.utility.preferences.IMultiPreferenceProvider;
import tinyos.yeti.utility.preferences.IPreferenceProvider;
import tinyos.yeti.utility.preferences.PreferenceToken;
import tinyos.yeti.utility.preferences.TextAttributeConstants;

public class NesCCommentScanner extends RuleBasedScanner implements IEditorTokenScanner {
    private IMultiPreferenceProvider preferences;
    private String line;
    
    /**
     * Create a new NescDoc scanner for the given color provider.
     * 
     * @param preferences the preferences to use in this scanner
     * @param line what kind of line this scanner scans, one of {@link TextAttributeConstants#COMMENT_MULTI_LINE}
     * or {@link TextAttributeConstants#COMMENT_SINGLE_LINE} 
     */
    public NesCCommentScanner( IMultiPreferenceProvider preferences, String line ) {
        super();
    
        this.preferences = preferences;
        this.line = line;
        
        buildRules();
    }

    private void buildRules(){
        IPreferenceProvider<TextAttribute> provider = preferences.getTextAttributes();

        IToken task = new PreferenceToken<TextAttribute>( TextAttributeConstants.NESC_DOC_TASK, provider );
        IToken word = new PreferenceToken<TextAttribute>( line, provider );
        
        List<IRule> list= new ArrayList<IRule>();

        // Add word rule for task tags
        list.add( new TaskTagWordRule( task ) );

        setDefaultReturnToken( word );
        
        IRule[] result= new IRule[list.size()];
        list.toArray(result);
        setRules(result);
    }

	public void setEditor( NesCEditor editor ){
		// ignore
	}
}