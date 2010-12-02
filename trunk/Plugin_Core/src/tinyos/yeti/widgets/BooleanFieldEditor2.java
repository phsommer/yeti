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
package tinyos.yeti.widgets;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * An editor that provides access to the checkbox control.
 */
public class BooleanFieldEditor2 extends BooleanFieldEditor {
    private  Button control;

    public BooleanFieldEditor2( String name, String labelText, int style, Composite parent) {
        super(name, labelText, style, parent);
    }
    
    public BooleanFieldEditor2( String name, String labelText, Composite parent ) {
        super( name, labelText, parent );
    }

    @Override
    public Button getChangeControl(Composite parent) {
        if (control == null) {
            control = super.getChangeControl(parent);
        } 
        return control;
    }
}
