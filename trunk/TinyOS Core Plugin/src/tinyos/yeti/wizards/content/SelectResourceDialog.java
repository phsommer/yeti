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
package tinyos.yeti.wizards.content;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog that can be used to select a resource from a project.
 * @author Benjamin Sigg
 */
public abstract class SelectResourceDialog extends Dialog{
    private ResourceTree tree;
    private IProject project;
    private IResource selection;
    private String title;
    
    public SelectResourceDialog( Shell parentShell ){
        super( parentShell );
        setShellStyle( SWT.DIALOG_TRIM | SWT.RESIZE );
    }
    
    public void setTitle( String title ){
        this.title = title;
    }
    
    @Override
    protected void configureShell( Shell newShell ){
        super.configureShell( newShell );
        if( title != null ){
            newShell.setText( title );
        }
    }
    
    public void setProject( IProject project ){
        this.project = project;
    }
    
    public void setSelection( IResource selection ){
        this.selection = selection;
    }
    
    public IResource getSelection(){
        return selection;
    }
    
    protected abstract ResourceTree createTree( Composite parent );
    
    @Override
    protected Control createDialogArea( Composite parent ){
    	Composite base = new Composite( parent, SWT.NONE );
    	base.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ));
    	GridLayout baseLayout = new GridLayout( 1, false );
    	baseLayout.marginWidth = 10;
    	baseLayout.marginTop = 5;
    	base.setLayout( baseLayout );
    	
    	Label info = new Label( base, SWT.NONE );
    	info.setText( "Select a resource:" );
    	info.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ));
    	
        tree = createTree( base );
        tree.setProject( project );
        if( selection != null ){
            tree.select( selection );
        }
        tree.addListener( new GenericListener<IContainer>(){
            public void trigger( IContainer value ){
                IResource next = tree.getResource();
                if( next != null ){
                    selection = next;
                }
            }
        });
        
        Control control = tree.getControl();
        GridData data = new GridData( SWT.FILL, SWT.FILL, true, true );
        data.minimumHeight = 400;
        data.minimumWidth = 400;
        control.setLayoutData( data );
        
        return control;
    }
}
