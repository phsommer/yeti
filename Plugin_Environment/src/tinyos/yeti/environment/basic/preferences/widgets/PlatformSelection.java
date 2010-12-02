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
package tinyos.yeti.environment.basic.preferences.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.ep.IPlatform;

public abstract class PlatformSelection{
    private Combo combo;
    private IPlatform[] choices;
    
    public void setChoices( IPlatform[] choices ){
        this.choices = choices;
        fillCombo();
    }
    
    public IPlatform getChoice(){
        int index = combo.getSelectionIndex();
        if( index <= 0 )
            return null;
        
        return choices[ index-1 ]; 
    }
    
    protected abstract void changed( IPlatform newChoice );
    
    private void fillCombo(){
        if( combo != null && choices != null ){
            combo.removeAll();
            
            combo.add( "<All Platforms>" );
            combo.select( 0 );
            
            for( IPlatform choice : choices ){
                combo.add( choice.getName() );
            }
        }
    }
    
    public void createControl( Composite parent ){
        combo = new Combo( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
        fillCombo();
        combo.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                changed( getChoice() );
            }
            public void widgetSelected( SelectionEvent e ){
                changed( getChoice() );
            }
        });
    }
    
    public Control getControl(){
        return combo;
    }
}
