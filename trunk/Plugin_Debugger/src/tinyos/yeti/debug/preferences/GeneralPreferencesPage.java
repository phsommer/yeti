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
package tinyos.yeti.debug.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.debug.TinyOSDebugPlugin;

public class GeneralPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
    public GeneralPreferencesPage(){
        super( GRID );
        setPreferenceStore( TinyOSDebugPlugin.getDefault().getPreferenceStore() );
    }

    @Override
    protected void createFieldEditors(){
        /*
        Composite outlineParent = getFieldEditorParent();
        StringFieldEditor separatorField = new StringFieldEditor( 
        		PreferenceConstants.C_VARIABLE_SEPARATOR,
        		"Variable separator used by the nested C compiler",
        		outlineParent );
        separatorField.setEmptyStringAllowed( false );
        separatorField.setTextLimit( 4 );
        String currentVal = TinyOSDebugPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.C_VARIABLE_SEPARATOR);
        if(currentVal == "") {
        	currentVal = ITinyOSDebugConstants.DEFAULT_NESC_SEPARATOR;
        }
        separatorField.setStringValue(currentVal);
        String outlineTooltip = "Variable separator used by the nested C compiler";
        separatorField.getTextControl( outlineParent ).setToolTipText( outlineTooltip );
        separatorField.getLabelControl( outlineParent ).setToolTipText( outlineTooltip );
        
        addField( separatorField );
        */
    }

    public void init( IWorkbench workbench ){
        // nothing to do
    }
}
