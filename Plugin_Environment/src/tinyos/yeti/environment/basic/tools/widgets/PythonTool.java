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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.environment.basic.tools.BaseSetting;

public class PythonTool implements IToolPage{
    private Control control;
    
    private Text pythonClassName;
    private Text pythonExtends;
    
    public String getToolLabel(){
        return "Python";
    }
    
    public void write( BaseSetting setting ){
        setting.setToolOption( "python", "-python-classname", pythonClassName.getText() );
        setting.setToolOption( "python", "-python-extends", pythonExtends.getText() );
    }
    
    public void read( BaseSetting setting ){
        pythonClassName.setText( setting.getToolOption( "python", "-python-classname", "" ) );
        pythonExtends.setText( setting.getToolOption( "python", "-python-extends", "" ) );
    }
    
    public String getToolName(){
        return "python";
    }
    
    public void createControl( Composite parent ){
        Composite base = new Composite( parent, SWT.NONE );
        control = base;
        base.setLayout( new GridLayout( 1, false ) );
        
        base = new Composite( base, SWT.NONE );
        base.setLayout( new GridLayout( 2, false ) );
        base.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
        
        Label pythonClassNameLabel = new Label( base, SWT.NONE );
        pythonClassNameLabel.setText( "Class-name" );
        pythonClassNameLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        
        pythonClassName = new Text( base, SWT.SINGLE | SWT.BORDER );
        pythonClassName.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        Label pythonExtendsLabel = new Label( base, SWT.NONE );
        pythonExtendsLabel.setText( "Extends class" );
        pythonExtendsLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        
        pythonExtends = new Text( base, SWT.SINGLE | SWT.BORDER );
        pythonExtends.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }
    
    public Control getControl(){
        return control;
    }
}
