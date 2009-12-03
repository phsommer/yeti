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
package tinyos.yeti.environment.basic.tools.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

public class OutputFileChoice{
    private Text file;
    private Composite base;
    
    public void setFile( String file ){
        this.file.setText( file );
    }
    
    public String getFile(){
        return file.getText();
    }
    
    private void browse(){
        FileDialog dialog = new FileDialog( base.getShell(), SWT.SAVE );
        String file = getFile();
        if( file != null ){
            dialog.setFileName( file );
        }
        
        String selection = dialog.open();
        if( selection != null ){
            setFile( selection );
        }
    }
    
    public void createControl( Composite parent ){
        base = new Composite( parent, SWT.NONE );
        GridLayout baseLayout = new GridLayout( 2, false );
        baseLayout.marginHeight = 0;
        baseLayout.marginWidth = 0;
        base.setLayout( baseLayout );
        
        file = new Text( base, SWT.SINGLE | SWT.BORDER );
        file.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        Button browse = new Button( base, SWT.PUSH );
        browse.setText( "Browse..." );
        browse.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
        browse.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                browse();
            }
            public void widgetSelected( SelectionEvent e ){
                 browse();   
            }
        });
    }
    
    public Control getControl(){
        return base;
    }
}
