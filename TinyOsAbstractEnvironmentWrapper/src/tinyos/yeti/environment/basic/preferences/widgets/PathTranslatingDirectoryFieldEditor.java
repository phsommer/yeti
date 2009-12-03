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

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import tinyos.yeti.environment.basic.path.IPathTranslator;

/**
 * A directory editor that shows the model-path instead of the true selected
 * file.
 * @author Benjamin Sigg
 */
public class PathTranslatingDirectoryFieldEditor extends DirectoryFieldEditor{
    private IPathTranslator translator;
    
    public PathTranslatingDirectoryFieldEditor( IPathTranslator translator ){
        super();
        this.translator = translator;
    }

    public PathTranslatingDirectoryFieldEditor( IPathTranslator translator, String name, String labelText, Composite parent ) {
        super( name, labelText, parent );
        this.translator = translator;
    }

    @Override
    protected String changePressed() {
        File file = translator.modelToSystem( getTextControl().getText() );
        if ( file != null && !file.exists()) {
            file = null;
        }
        File directory = getDirectory(file);
        if( directory == null ){
            return null;
        }

        return translator.systemToModel( directory );
    }

    @Override
    protected boolean doCheckState() {
        String fileName = getTextControl().getText();
        fileName = fileName.trim();
        if( fileName.length() == 0 && isEmptyStringAllowed() ){
            return true;
        }
        File file = translator.modelToSystem( fileName );
        if( file == null )
            return true;
        return file.isDirectory();
    }

    private File getDirectory( File startingDirectory ){
        DirectoryDialog fileDialog = new DirectoryDialog(getShell(), SWT.OPEN);
        if (startingDirectory != null) {
            fileDialog.setFilterPath(startingDirectory.getPath());
        }
        String dir = fileDialog.open();
        if (dir != null) {
            dir = dir.trim();
            if (dir.length() > 0) {
                return new File(dir);
            }
        }

        return null;
    }
}
