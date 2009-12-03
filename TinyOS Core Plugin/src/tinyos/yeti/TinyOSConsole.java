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
package tinyos.yeti;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.ui.console.MessageConsole;

import tinyos.yeti.utility.TinyOSConsoleStream;
import tinyos.yeti.views.make.NescFilePatternMatchListener;

public class TinyOSConsole {
    private MessageConsole console;
    
    private NescFilePatternMatchListener patternMatcher;
    
    private TinyOSConsoleStream info;
    private TinyOSConsoleStream out;
    private TinyOSConsoleStream err;

    public TinyOSConsole( MessageConsole console ){
        this.console = console;
        
        info = new TinyOSConsoleStream( this, console.newMessageStream() );
        info.setColor( ColorConstants.black );
        
        out = new TinyOSConsoleStream( this, console.newMessageStream() );
        out.setColor( ColorConstants.blue );
        
        err = new TinyOSConsoleStream( this, console.newMessageStream() );
        err.setColor( ColorConstants.red );
    }
    
    public MessageConsole getConsole(){
        return console;
    }
    
    public void setProject( ProjectTOS project ){
        if( patternMatcher != null ){
            console.removePatternMatchListener( patternMatcher );
            patternMatcher = null;
        }

        if( project != null ){
            patternMatcher = new NescFilePatternMatchListener();
            patternMatcher.setProject( project );
            console.addPatternMatchListener( patternMatcher );
        }
    }
    
    public void clear(){
        console.clearConsole();
    }
    
    public TinyOSConsoleStream info(){
        return info;
    }
    
    public TinyOSConsoleStream out(){
        return out;
    }
    
    public TinyOSConsoleStream err(){
        return err;
    }
}
