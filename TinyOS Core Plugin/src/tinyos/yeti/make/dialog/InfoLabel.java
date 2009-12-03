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
package tinyos.yeti.make.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import tinyos.yeti.make.dialog.IMakeTargetDialog.Severity;

/**
 * 
 * @author Benamin Sigg
 * @deprecated no longer used anywhere
 */
@Deprecated
public class InfoLabel extends AbstractMessageConverter{
    private Label label;
    
    public void createControl( Composite parent ){
        label = new Label( parent, SWT.NONE );
        updateLabel();
    }
    
    public Control getControl(){
        return label;
    }
    
    
    @Override
    protected void showMessage( Severity severity, String message, IMakeTargetDialogPage<?> page ){     
        if( label == null || label.isDisposed() )
            return;
        
        if( severity == null ){
            label.setText( "nothing to report" );
        }
        else{
            StringBuilder text = new StringBuilder();
            
            switch( severity ){
                case ERROR:
                    text.append( "ERROR " );
                    break;
                case WARNING:
                    text.append( "WARNING " );
                    break;
                case INFO:
                    text.append( "INFO " );
                    break;
            }
            
            text.append( "(" );
            text.append( page.getName() );
            text.append( "): " );
            text.append( message );
            label.setText( text.toString() );
        }   
    }
}