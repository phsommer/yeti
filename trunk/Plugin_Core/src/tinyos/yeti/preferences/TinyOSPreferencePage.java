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
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.widgets.BooleanFieldEditor2;

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

public class TinyOSPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public TinyOSPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(TinyOSPlugin.getDefault().getPreferenceStore());
        setDescription("Preferences for TinyOS-Editor");
    }


    @Override
    protected void createFieldEditors() {
        String label = "Environment: Enable modifiying content of files outside workspace (Experts only)";
        addField(new BooleanFieldEditor(PreferenceConstants.ENABLE_MODIFY_ON_OUTSIDE_FILES,label,getFieldEditorParent()));
        
        Composite outlineParent = getFieldEditorParent();
        IntegerFieldEditor outline = new IntegerFieldEditor( 
        		PreferenceConstants.OUTLINE_UPDATE_DELAY,
        		"Update delay after typing",
        		outlineParent );
        outline.setEmptyStringAllowed( false );
        outline.setTextLimit( 4 );
        String outlineTooltip = "Milliseconds to wait after typing until the outline gets updated";
        outline.getTextControl( outlineParent ).setToolTipText( outlineTooltip );
        outline.getLabelControl( outlineParent ).setToolTipText( outlineTooltip );
        addField( outline );
        
        addField(new RadioGroupFieldEditor(
                PreferenceConstants.USE_TABS,"Code modifications should use:",
                2,
                new String[][]{{"Tabs","true"},{"Spaces","false"}},
                getFieldEditorParent(),
                true
        ));
        
        IntegerFieldEditor spacesToTabs = new IntegerFieldEditor( PreferenceConstants.SPACES_AS_TABS, "Spaces per tab", getFieldEditorParent() );
        spacesToTabs.setValidRange( 1, 15 );
        addField( spacesToTabs );
        
        addBoolean( PreferenceConstants.AUTO_STRATEGY_IDENT, "Automatic indentation", "Automatically insert spaces or tabs when starting a new line." );
        addBoolean( PreferenceConstants.AUTO_BRACKETS, "Automatic brackets", "Automatically insert a closing bracket if an opening bracket is entered. E.g. insert ')' if you enter '('." );
        addBoolean( PreferenceConstants.ERROR_TO_INFO, "Allow to hide error/warning messages.", "Allows to convert error and warning messages to info messages. Changing this setting does not affect already converted messages." );
        addBoolean( PreferenceConstants.SAVE_FILES_AUTOMATICALLY, "Save files before building or debugging", "Automatically saving all modified *.h and *.nc files before building or debugging an application." );
    }

    private void addBoolean( String name, String label, String tooltip ){
    	BooleanFieldEditor2 editor = new BooleanFieldEditor2( name, label, SWT.NONE, getFieldEditorParent() );
    	addField( editor );
    	
    	editor.getChangeControl( getFieldEditorParent() ).setToolTipText( tooltip );
    }
    
    /*
    private void addLabel(String text, int span) {
        Label label = new Label(getFieldEditorParent(), SWT.NULL);
        GridData gd = new GridData();
        gd.horizontalSpan = span;
        label.setLayoutData(gd);
        label.setText(text);
    }
    */
    
    public void init(IWorkbench workbench) {
        // nothing to do
    }

}