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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.make.dialog.IMakeTargetDialog.Severity;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public abstract class AbstractMakeTargetDialogPage<M extends MakeTargetSkeleton> implements IMakeTargetDialogPage<M> {
    private Control control;
    private String name;
    private IMakeTargetDialog dialog;
    
    private String message;
    private IMakeTargetDialog.Severity severity;
    
    private String defaultMesssage;
    private Image image;
    
    public AbstractMakeTargetDialogPage( String name ){
        this.name = name;
    }
    
    public void setImage( Image image ){
		this.image = image;
	}
    
    public Image getImage(){
    	return image;
    }
    
    public void setDialog( IMakeTargetDialog dialog ){
        this.dialog = dialog;
        if( dialog != null ){
            dialog.setMessage( this, message, severity );
        }
    }
    
    public IMakeTargetDialog getDialog(){
        return dialog;
    }
    
    protected void contentChanged(){
    	if( dialog != null ){
    		dialog.contentChanged();
    	}
    }
    
    protected void setInfo( String message ){
        setMessage( message, IMakeTargetDialog.Severity.INFO );
    }
    
    protected void setWarning( String message ){
        setMessage( message, IMakeTargetDialog.Severity.WARNING );
    }
    
    protected void setError( String message ){
        setMessage( message, IMakeTargetDialog.Severity.ERROR );
    }
    
    protected void setDefaultMessage( String defaultMesssage ){
        this.defaultMesssage = defaultMesssage;
        setDefaultMessage();
    }
    
    protected void setDefaultMessage(){
        if( defaultMesssage == null )
            setMessage( null, null );
        else
            setMessage( defaultMesssage, Severity.DESCRIPTION );
    }
    
    public String getDescription(){
    	return defaultMesssage;
    }
    
    protected void setMessage( String message, IMakeTargetDialog.Severity severity ){
        this.message = message;
        this.severity = severity;
        
        if( dialog != null ){
            dialog.setMessage( this, message, severity );
        }
    }

    public void check( M maketarget, IMakeTargetInformation information ){
        // ignore
    }
    
    public void dispose() {
        // ignore
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected void setControl(Control control) {
        this.control = control;
    }

    public Control getControl() {
        return control;
    }
}
