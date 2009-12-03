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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.wizards.content.NesCFileTree;
import tinyos.yeti.wizards.content.ResourceTree;
import tinyos.yeti.wizards.content.SelectResourceDialog;

/**
 * A field where the user can select an nc-file
 * @author Benjamin Sigg
 */
public class NesCFileSelection{
    private ProjectTOS project;
    
    private Text nesCFile;
    private Composite base;
    
    public void createControl( Composite parent ){
        base = new Composite( parent, SWT.NONE );
        GridLayout nesCFilePaneLayout = new GridLayout( 2, false );
        nesCFilePaneLayout.marginHeight = 0;
        nesCFilePaneLayout.marginWidth = 0;
        base.setLayout( nesCFilePaneLayout );
        base.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        nesCFile = new Text( base, SWT.BORDER | SWT.SINGLE );
        nesCFile.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        Button nesCFileButton = new Button( base, SWT.PUSH );
        nesCFileButton.setText( "Browse..." );
        nesCFileButton.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                browseNesCFile();
            }
            public void widgetSelected( SelectionEvent e ){
                browseNesCFile();
            }
        });
    }
    
    public Control getControl(){
        return base;
    }
    
    public void setProject( ProjectTOS project ){
        this.project = project;
    }
    
    public String getFile(){
        return nesCFile.getText();
    }
    
    public void setFile( String file ){
        nesCFile.setText( file );
    }
    
    private void browseNesCFile(){
        SelectResourceDialog dialog = new SelectResourceDialog( base.getShell() ){
            @Override
            protected ResourceTree createTree( Composite parent ){
                return new NesCFileTree( parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
            }
        };
        dialog.setTitle( "Select NesC-file" );
        dialog.setProject( project.getProject() );
        
        IPath previous = new Path( nesCFile.getText() );
        if( previous.segmentCount() > 0 ){
            IFile file = project.getProject().getFile( previous );
        	if( file.exists() ){
                dialog.setSelection( file );
            }
        }
        
        if( dialog.open() == SelectResourceDialog.OK ){
            IResource resource = dialog.getSelection();
            if( resource != null ){
                IPath path = resource.getProjectRelativePath();
                nesCFile.setText( path.toString() );
            }
        }
    }
    
}
