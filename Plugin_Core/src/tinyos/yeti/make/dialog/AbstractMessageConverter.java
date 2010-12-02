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

import java.util.HashMap;
import java.util.Map;

/**
 * Collects the messages of {@link IMakeTargetDialogPage}s and selects one
 * of the messages to show.
 * @author Benjamin Sigg
 */
public abstract class AbstractMessageConverter{
    private Map<IMakeTargetDialogPage<?>, Message> messages = new HashMap<IMakeTargetDialogPage<?>, Message>();
    private IMakeTargetDialogPage<?>[] pages;
    private IMakeTargetDialogPage<?> page;
    
    public void setMessage( IMakeTargetDialogPage<?> page, String text, IMakeTargetDialog.Severity severity ){
        if( text == null || severity == null ){
            messages.remove( page );
        }
        else{
            Message message = new Message();
            message.page = page;
            message.message = text;
            message.severity = severity;
            
            messages.put( page, message );
        }
        
        updateLabel();
    }
    
    public void setPages( IMakeTargetDialogPage<?>[] pages ){
        this.pages = pages;
    }
    
    public void setPage( IMakeTargetDialogPage<?> page ){
        this.page = page;
        
        updateLabel();
    }
    
    protected abstract void showMessage( IMakeTargetDialog.Severity severity, String message, IMakeTargetDialogPage<?> page );
    
    protected void updateLabel(){
        if( page != null && pages != null ){
            Message message = getMessage();
            if( message == null ){
                showMessage( null, null, null );
            }
            else{
                showMessage( message.severity, message.message, message.page );
            }
        }
    }
    
    private Message getMessage(){
        Message message = getMessage( page, IMakeTargetDialog.Severity.ERROR );
        if( message == null )
            message = getMessage( null, IMakeTargetDialog.Severity.ERROR );
        
        if( message == null )
            message = getMessage( page, IMakeTargetDialog.Severity.WARNING );
        if( message == null )
            message = getMessage( null, IMakeTargetDialog.Severity.WARNING );
        
        if( message == null )
            message = getMessage( page, IMakeTargetDialog.Severity.INFO );
        if( message == null )
            message = getMessage( null, IMakeTargetDialog.Severity.INFO );
        
        if( message == null )
            message = getMessage( page, IMakeTargetDialog.Severity.DESCRIPTION );
        
        return message;
    }
    
    private Message getMessage( IMakeTargetDialogPage<?> restraint, IMakeTargetDialog.Severity severity ){
        if( restraint == null ){
            for( IMakeTargetDialogPage<?> page : pages ){
                Message result = getMessage( page, severity );
                if( result != null )
                    return result;
            }
        }
        else{
            Message message = messages.get( restraint );
            if( message != null && message.severity == severity )
                return message;
        }
        
        return null;
    }
    
    public boolean hasNoErrors(){
        for( Message message : messages.values() ){
            if( message.severity == IMakeTargetDialog.Severity.ERROR )
                return false;
        }
        
        return true;
    }
    
    private class Message{
        public IMakeTargetDialogPage<?> page;
        public String message;
        public IMakeTargetDialog.Severity severity;
    }

}
