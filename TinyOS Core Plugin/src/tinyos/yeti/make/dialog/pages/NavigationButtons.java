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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A vertical list of buttons.
 * @author Benjamin Sigg
 */
public abstract class NavigationButtons implements ICustomizeablePage{
    private Control control;
    
    private String[] ids;
    private String[] labels;
    
    private Button[] buttons;
    
    public NavigationButtons( String[] ids, String[] labels ){
        this.ids = ids;
        this.labels = labels;
    }
    
    public void setCustomEnabled( boolean enabled ){
	    if( buttons != null ){	
	    	for( Button button : buttons ){
	    		button.setEnabled( enabled );
	    	}
	    }
    }
    
    public void createControl( Composite parent ){
        Composite base = new Composite( parent, SWT.NONE );
        base.setLayout( new GridLayout( 1, false ) );
        control = base;
    
        buttons = new Button[ ids.length ];
        
        for( int i = 0, n = ids.length; i<n; i++ ){
            Button button = new Button( base, SWT.PUSH );
            buttons[i] = button;
            button.setText( labels[i] );
            button.addSelectionListener( new Action( ids[i] ) );
            button.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        }
    }
    
    public Control getControl(){
        return control;
    }
    
    protected abstract void doOperation( String id );
    
    private class Action implements SelectionListener{
        private String id;
        
        public Action( String id ){
            this.id = id;
        }
        
        public void widgetDefaultSelected( SelectionEvent e ){
            doOperation( id );
        }
        public void widgetSelected( SelectionEvent e ){
            doOperation( id );
        }
    }
}
