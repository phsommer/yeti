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
package tinyos.yeti.environment.basic.preferences.widgets;

import java.io.File;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import tinyos.yeti.environment.basic.path.IPathTranslator;

/**
 * A file editor that shows the model-path instead of the true selected
 * file.
 * @author Benjamin Sigg
 */
public class PathTranslatingFileFieldEditor extends FileFieldEditor {
    private IPathTranslator translator;
    
    public PathTranslatingFileFieldEditor( IPathTranslator translator ){
        super();
        this.translator = translator;
    }

    public PathTranslatingFileFieldEditor( IPathTranslator translator, String name, String labelText,
            boolean enforceAbsolute, Composite parent ) {
        super( name, labelText, enforceAbsolute, parent );
        this.translator = translator;
    }

    public PathTranslatingFileFieldEditor( IPathTranslator translator, String name, String labelText,
            Composite parent ) {
        super( name, labelText, parent );
        this.translator = translator;
    }

    @Override
    protected String changePressed() {
        File file = translator.modelToSystem( getTextControl().getText() );
        if (file != null && !file.exists()) {
            file = null;
        }
        File d = getFile(file);
        if( d == null ){
            return null;
        }

        return translator.systemToModel( d );
    }

    @Override
    protected boolean checkState() {
        String msg = null;

        String path = getTextControl().getText();
        if (path != null) {
            path = path.trim();
        } else {
            path = "";//$NON-NLS-1$
        }
        if (path.length() == 0) {
            if (!isEmptyStringAllowed()) {
                msg = getErrorMessage();
            }
        } else {
            File file = translator.modelToSystem( path );
            if( file != null && !file.isFile() ){
                msg = getErrorMessage();
            }
        }

        if( msg != null ){ // error
            showErrorMessage(msg);
            return false;
        }

        // OK!
        clearErrorMessage();
        return true;
    }

    private File getFile(File startingDirectory) {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        if (startingDirectory != null) {
            dialog.setFileName(startingDirectory.getPath());
        }
        String file = dialog.open();
        if (file != null) {
            file = file.trim();
            if (file.length() > 0) {
                return new File(file);
            }
        }

        return null;
    }

}
