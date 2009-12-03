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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.TinyOSPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class HighlightingOptionsPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public HighlightingOptionsPreferencePage() {
		super(GRID);
		setPreferenceStore(TinyOSPlugin.getDefault().getPreferenceStore());
		setDescription("Brackets");
	}
	
	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.BRACKET_BG_COLORER,"Change background within brackets",getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.BRACKET_BG_START_COLOR,"Top level background color",getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.BRACKET_BG_INCREMENT,"Background Decrement",getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.BRACKET_BG_ERROR_COLOR,"Error background color",getFieldEditorParent()));
		
		addField( new BooleanFieldEditor( PreferenceConstants.BRACKET_HIGHLIGHT, "Highlight brackets", getFieldEditorParent() ) );
		addField( new ColorFieldEditor( PreferenceConstants.BRACKET_HIGHLIGHT_COLOR, "Bracket highlight color", getFieldEditorParent() ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}