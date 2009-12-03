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
package tinyos.yeti.make.dialog.pages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TypedefDialog extends Dialog{
    private Text name;
    private Text type;
    
    private String nameValue;
    private String typeValue;
    
    private String title = "";
    
    public TypedefDialog( Shell shell ){
        super( shell );
        setBlockOnOpen( true );
    }

	protected boolean isResizable() {
    	return true;
    }
    
    public boolean open( String type, String name ){
        if( type == null || name == null )
            title = "Create new typedef";
        else
            title = "Edit typedef";
        
        Shell shell = getShell();
        if( shell != null )
            shell.setText( title );
        
        nameValue = name == null ? "" : name;
        typeValue = type == null ? "" : type;
        
        int state = super.open();
        
        if( state == OK ){
            return true;
        }
        
        return false;
    }
    
    public String getName(){
        return nameValue;
    }
    
    public String getType(){
        return typeValue;
    }
    
    @Override
    protected Control createDialogArea( Composite parent ){
        getShell().setText( title );
        
        Composite content = (Composite)super.createDialogArea( parent );
        
        Label info = new Label( content, SWT.NONE );
        info.setText( "Example: 'typedef int x[47]' becomes 'name = x', 'type = int [47]'" );
        info.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        Composite fields = new Composite( content, SWT.NONE );
        fields.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        fields.setLayout( new GridLayout( 2, false ) );
        
        Label nameLabel = new Label( fields, SWT.NONE );
        nameLabel.setText( "Name: " );
        nameLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        
        name = new Text( fields, SWT.BORDER | SWT.SINGLE );
        name.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        name.setText( nameValue );
        name.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                contentChanged();
            }
        });
        
        Label constantLabel = new Label( fields, SWT.NONE );
        constantLabel.setText( "Type: " );
        constantLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

        type = new Text( fields, SWT.BORDER | SWT.SINGLE );
        type.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        type.setText( typeValue );
        type.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                contentChanged();
            }
        });
        
        checkOkButton();
        
        return content;
    }
    
    private void contentChanged(){
        nameValue = name.getText().trim();
        typeValue = type.getText().trim();
        
        checkOkButton();
    }
    
    private void checkOkButton(){
        Button button = getButton( IDialogConstants.OK_ID );
        if( button != null )
            button.setEnabled( nameValue.length() > 0 && typeValue.length() > 0 );
    }
}
