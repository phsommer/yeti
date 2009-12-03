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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.Debug;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.widgets.BooleanFieldEditor2;

public class DebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
    public DebugPreferencePage(){
        super( GRID );
        
        setDescription( "Debug settings for the TinyOS Plugin (not for the projects)" );
        setTitle( "Debug" );
        setPreferenceStore( TinyOSPlugin.getDefault().getPreferenceStore() );
    }

    @Override
    protected void createFieldEditors(){
        BooleanFieldEditor2 debug = new BooleanFieldEditor2( Debug.DEBUG_PREFERENCE, "Debug mode active", getFieldEditorParent() );
        BooleanFieldEditor2 debugConsole = new BooleanFieldEditor2( Debug.DEBUG_PREFERENCE_CONSOLE, "Print output on TinyOS Console", getFieldEditorParent() );

        debug.getChangeControl( getFieldEditorParent() ).setToolTipText( "If set, then this plugin will print debug information into std-out.\n" +
        		"Also new views will be made available. Note that some features are only changed after a restart of Eclipse." );
        
        addField( debug );
        addField( debugConsole );
    }

    public void init( IWorkbench workbench ){
        // ignore
    }
}
