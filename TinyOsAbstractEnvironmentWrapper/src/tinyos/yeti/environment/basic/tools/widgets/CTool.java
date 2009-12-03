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

public class CTool implements IToolPage{
    private Control control;
    
    private Text cPrefix;
    
    public String getToolLabel(){
        return "C";
    }
    
    public void write( BaseSetting setting ){
        setting.setToolOption( "c", "-c-prefix", cPrefix.getText() );
    }
    
    public void read( BaseSetting setting ){
        cPrefix.setText( setting.getToolOption( "c", "-c-prefix", "" ) );
    }
    
    public String getToolName(){
        return "c";
    }
    
    public void createControl( Composite parent ){
        Composite base = new Composite( parent, SWT.NONE );
        control = base;
        base.setLayout( new GridLayout( 1, false ) );
        
        base = new Composite( base, SWT.NONE );
        base.setLayout( new GridLayout( 2, false ) );
        base.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
        
        Label cPrefixName = new Label( base, SWT.NONE );
        cPrefixName.setText( "C-prefix" );
        cPrefixName.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        
        cPrefix = new Text( base, SWT.SINGLE | SWT.BORDER );
        cPrefix.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }
    
    public Control getControl(){
        return control;
    }
}
