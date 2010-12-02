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

import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.TinyOSPlugin;

/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class TemplatePreferencePage extends org.eclipse.ui.texteditor.templates.TemplatePreferencePage implements IWorkbenchPreferencePage {

    public TemplatePreferencePage() {
        setPreferenceStore(TinyOSPlugin.getDefault().getPreferenceStore());
        setTemplateStore(TinyOSPlugin.getDefault().getTemplateStore());
        setContextTypeRegistry(TinyOSPlugin.getDefault().getContextTypeRegistry());
    }

    @Override
    protected boolean isShowFormatterSetting() {
        return false;
    }


    @Override
    public boolean performOk() {
        boolean ok= super.performOk();

        TinyOSPlugin.getDefault().savePluginPreferences();

        return ok;
    }
}
