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

import tinyos.yeti.ep.parser.macros.ConstantMacro;

/**
 * A small dialog that allows the user to create or modify {@link ConstantMacro}s.
 * @author Benjamin Sigg
 */
public class ConstantMacroDialog extends Dialog{
    private Text name;
    private Text constant;
    
    private String nameValue = "";
    private String constantValue = "";
    
    private String title = "";
    
    public ConstantMacroDialog( Shell shell ){
        super( shell );
        setBlockOnOpen( true );
    }

	@Override
	protected boolean isResizable() {
    	return true;
    }
    
    public ConstantMacro open( ConstantMacro macro ){
        if( macro == null )
            title = "Create new macro";
        else
            title = "Edit macro";
        
        Shell shell = getShell();
        if( shell != null )
            shell.setText( title );
        
        if( macro == null ){
            nameValue = "";
            constantValue = "";
        }
        else{
            nameValue = macro.getName();
            constantValue = macro.getConstant();
        }
        
        if( name != null )
            name.setText( nameValue );
        if( constant != null )
            constant.setText( constantValue );
        
        int state = super.open();
        ConstantMacro result = null;
        
        if( state == OK ){
            nameValue = nameValue.trim();
            if( nameValue.length() == 0 )
                nameValue = "unnamed";
            
            result = new ConstantMacro( nameValue, constantValue );
        }
        
        return result;
    }
    
    @Override
    protected Control createDialogArea( Composite parent ){
        getShell().setText( title );
        
        Composite content = (Composite)super.createDialogArea( parent );
        Label info1 = new Label( content, SWT.NONE );
        info1.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ));
        info1.setText( "Example: '#define PI 3.14' would become Name=PI, Value=3.14." );
        
        Label info2 = new Label( content, SWT.NONE );
        info2.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ));
        info2.setText( "Functions are not supported." );
        
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
        constantLabel.setText( "Value: " );
        constantLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

        constant = new Text( fields, SWT.BORDER | SWT.SINGLE );
        constant.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        constant.setText( constantValue );
        constant.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                contentChanged();
            }
        });
        
        checkOkButton();
        
        return content;
    }
    
    private void contentChanged(){
        nameValue = name.getText().trim();
        constantValue = constant.getText().trim();
        
        checkOkButton();
    }
    
    private void checkOkButton(){
        Button button = getButton( IDialogConstants.OK_ID );
        if( button != null )
            button.setEnabled( nameValue.length() > 0 );
    }
}
