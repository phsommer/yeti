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
package tinyos.yeti.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.utility.preferences.TextAttributeConstants;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
    public void initializeDefaultPreferences() {
    	IPreferenceStore store = TinyOSPlugin.getDefault().getPreferenceStore();
    	
        TextAttributeConstants.writeDefaults( store );

        // Bracket Highlight
        store.setDefault(PreferenceConstants.BRACKET_HIGHLIGHT,true);
        store.setDefault(PreferenceConstants.BRACKET_HIGHLIGHT_COLOR,"0,0,0");

        // Line Highlight
        store.setDefault(PreferenceConstants.CURRENT_LINE_HIGHLIGHT, true);
        store.setDefault(PreferenceConstants.CURRENT_LINE_HIGHLIGHT_COLOR,"247,231,121");

        // BracketBackround
        store.setDefault(PreferenceConstants.BRACKET_BG_COLORER, true);
        store.setDefault(PreferenceConstants.BRACKET_BG_START_COLOR, "255,255,255");
        store.setDefault(PreferenceConstants.BRACKET_BG_INCREMENT, "15,15,15");
        store.setDefault(PreferenceConstants.BRACKET_BG_ERROR_COLOR, "247,198,198");

        // Modify Outside Files
        store.setDefault(PreferenceConstants.ENABLE_MODIFY_ON_OUTSIDE_FILES, false);

        // General
        store.setDefault( PreferenceConstants.USE_TABS, true );
        store.setDefault( PreferenceConstants.SPACES_AS_TABS, 2 );
        store.setDefault( PreferenceConstants.CLEAN_FULL, true );
        store.setDefault( PreferenceConstants.THUMBNAIL_POPUP, true );
        store.setDefault( PreferenceConstants.OUTLINE_UPDATE_DELAY, 500 );
        store.setDefault( PreferenceConstants.ICONS_ALWAYS_DECORATED, true );
        
        // Code help
        store.setDefault( PreferenceConstants.AUTO_BRACKETS, true );
        store.setDefault( PreferenceConstants.AUTO_STRATEGY_IDENT, true );
        store.setDefault( PreferenceConstants.ERROR_TO_INFO, true );
        
        store.setDefault( PreferenceConstants.SAVE_FILES_AUTOMATICALLY, false );
    }
}
