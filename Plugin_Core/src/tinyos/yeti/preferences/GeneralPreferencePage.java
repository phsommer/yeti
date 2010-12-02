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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.widgets.BooleanFieldEditor2;

public class GeneralPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
    public GeneralPreferencePage(){
        super( GRID );
        setPreferenceStore( TinyOSPlugin.getDefault().getPreferenceStore() );
    }

    @Override
    protected void createFieldEditors(){
        Composite cleanParent = getFieldEditorParent();
        BooleanFieldEditor2 clean = new BooleanFieldEditor2( 
                PreferenceConstants.CLEAN_FULL, 
                "Clean: clear library caches as well", 
                cleanParent );
        
        clean.getChangeControl( cleanParent ).setToolTipText(
                    "If set: 'Clean' will wipe every cache a project has and rebuild the project from scratch.\n" +
                    "If not set: 'Clean' will only clean files belonging to the project, but not those of any library." );
        
        Composite thumbnailParent = getFieldEditorParent();
        BooleanFieldEditor2 thumbnail = new BooleanFieldEditor2(
                PreferenceConstants.THUMBNAIL_POPUP,
                "Thumbnail: show up when graph view selected",
                thumbnailParent );
        thumbnail.getChangeControl( thumbnailParent ).setToolTipText( 
                "If set: the 'Thumbnail View' will show up as soon as the graph of a '*.nc' file is shown.\n" +
                "If not set: nothing happens." );
        
        Composite decorationParent = getFieldEditorParent();
        BooleanFieldEditor2 decoration = new BooleanFieldEditor2(
        		PreferenceConstants.ICONS_ALWAYS_DECORATED,
        		"Decorate icons in hovers and graph view",
        		decorationParent );
        decoration.getChangeControl( decorationParent ).setToolTipText(
        		"If set: icons shown on hovers and in the graph view are decorated with warning/error sign.\n" +
        		"If not set: icons remain undecorated" );
        
        addField( clean );
        addField( thumbnail );
        addField( decoration );
    }

    public void init( IWorkbench workbench ){
        // nothing to do
    }
}
