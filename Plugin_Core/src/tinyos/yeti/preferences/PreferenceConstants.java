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

import tinyos.yeti.TinyOSPlugin;



/**
 * Preference constants used in the Nesc preference store. Clients should only read the
 * Nesc preference store using these values. Clients are not allowed to modify the 
 * preference store programmatically.
 * 
  */
public abstract class PreferenceConstants {

	private PreferenceConstants() {
	}
	
	public static final String CURRENT_LINE_HIGHLIGHT = "nesc_currentLineHighlight";
	public static final String CURRENT_LINE_HIGHLIGHT_COLOR = "nesc_currentLineHighlightColor";
	
	public static final String BRACKET_HIGHLIGHT = "nesc_highlightMatchingBrackets";
	public static final String BRACKET_HIGHLIGHT_COLOR = "nesc_highlightMatchingBracketsColor";
	public static final String ENABLE_MODIFY_ON_OUTSIDE_FILES = "nesc_enableModifyingOutsideWorkspace";
	
	// bg colorer
	public static final String BRACKET_BG_COLORER = "nesc_BgColorer";
	public static final String BRACKET_BG_START_COLOR = "nesc_BgColorerStart";
	public static final String BRACKET_BG_INCREMENT = "nesc_BgColorerIncrement" ;
	public static final String BRACKET_BG_ERROR_COLOR = "nesc_BgColorerError" ;
	
	// general settings
	public static final String USE_TABS = "nesc_useTabsInQuickfix";
	public static final String SPACES_AS_TABS = "nesc_spacesAsTabs";
	
	public static final String CLEAN_FULL = "nesc_clean";
	public static final String THUMBNAIL_POPUP = "nesc_thumbnail_popup";
	public static final String OUTLINE_UPDATE_DELAY = "nesc_outlineUpdateDelay";
	public static final String ICONS_ALWAYS_DECORATED = "nesc_iconsDecorated";
	
	// automatic indentation and brackets
	public static final String AUTO_STRATEGY_IDENT = "nesc_autoident_strategy";
	public static final String AUTO_BRACKETS = "nesc_autobrackets";
	public static final String ERROR_TO_INFO = "nesc_errorToInfo";
	
	// code formatting and indentation
	public static final String CF_INDENTATION_STRATEGY = "nesc_cf_indentation_strategy";
	public static final String CF_CODE_FORMATTING_STRATEGY = "nesc_cf_code_formatting_strategy";
	
	public static final String SAVE_FILES_AUTOMATICALLY = "nesc_safeFilesAutomatically";
	
	public static final String PROJECT_CACHE = "nesc_projectCache";
	
	public static final String OUTLINE_FILTER = "nesc_outlineFilter";
	
	public static String spacesPerTab(){
		TinyOSPlugin plugin = TinyOSPlugin.getDefault();
		int count = plugin.getPreferenceStore().getInt( SPACES_AS_TABS );
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < count; i++ )
			builder.append( ' ' );
		return builder.toString();
	}
}

