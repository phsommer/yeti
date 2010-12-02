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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SaveAsGroup {
	private Text name;
	private Group base;
	private String extension;
	
	public SaveAsGroup( String extension ){
		this.extension = extension;
	}
	
	public void setName( String file ){
		name.setText( file );
	}
	
	public String getName(){
		String name = this.name.getText();
		
		if( name == null )
			return null;
		
		name = name.trim();
		if( name.length() == 0 )
			return null;
		
		if( !name.endsWith( "." + extension ))
			name += "." + extension;
		
		return name;
	}
	
	public void createControl( Composite parent ){
		base = new Group( parent, SWT.NONE );
        base.setText( "Save setting as..." );
        base.setLayout( new GridLayout( 2, false ) );
        base.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
        Label ncgSettingNameLabel = new Label( base, SWT.NONE );
        ncgSettingNameLabel.setText( "Filename (optional)" );
        ncgSettingNameLabel.setToolTipText( "Path to a file which will store this settings for later reuse" );
        ncgSettingNameLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        
        name = new Text( base, SWT.BORDER | SWT.SINGLE );
        name.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
	}
	
	public Control getControl(){
		return base;
	}
}
