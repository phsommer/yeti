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
package tinyos.yeti.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A page for a wizard that can be used to create new files.
 * @author Benjamin Sigg
 */
public abstract class SimpleNewFileWizardPage extends FileWizardPage{
    private Text text;
    
    private String labelText;
    private Label label;
    
    private String[] extensions;
    
    public SimpleNewFileWizardPage( String pageName, String title, ImageDescriptor titleImage ){
        super( pageName, title, titleImage );
        setFileDescription( "Filename: " );
    }

    public SimpleNewFileWizardPage( String pageName ){
        super( pageName );
        setFileDescription( "Filename: " );
    }
    
    public void setExtensions( String... extensions ){
        this.extensions = extensions;
    }
    
    public void createFile() throws CoreException{
        createFile( text.getText(), extensions, getContent( ensureExtension( text.getText(), extensions ) ), true );
    }
    
    public String withoutExtension( String name ){
        return withoutExtension( name, extensions );
    }
    
    
    
    /**
     * Gets the currently selected file name, with or without extensions.
     * @return the file name
     */
    public String getFilename(){
        return text.getText();
    }
    
    protected abstract String getContent( String filename );
    
    @Override
    protected boolean checkValidity(){
        if( !super.checkValidity() )
            return false;
        
        String text = this.text.getText();
        return checkFileValidity( text, extensions );
    }
        
    public void setFileDescription( String name ){
        labelText = name;
        if( label != null )
            label.setText( name );
    }
    
    public void createControl( Composite parent ){
        Composite panel = new Composite( parent, SWT.NONE );
        panel.setLayout( new GridLayout( 1, false ) );
        
        createContainerControl( panel, SWT.BORDER );
        getContainerControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        Composite filename = new Composite( panel, SWT.BORDER );
        filename.setLayout( new GridLayout( 2, false ) );
        filename.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
        
        Label label = new Label( filename, SWT.NONE );
        label.setText( labelText );
        label.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        
        text = new Text( filename, SWT.BORDER );
        text.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        text.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                checkAllValidity();
            }                
        });
        
        text.setFocus();
        
        setControl( panel );
        
        checkAllValidity();
    }
}
