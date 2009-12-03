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
package tinyos.yeti.environment.basic.debug;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

import tinyos.yeti.Debug;

/**
 * A console where an input, output and error stream are available. The user
 * @author Benjamin Sigg
 */
public abstract class EnvironmentConsole implements IConsoleFactory{
    private IOConsole console;
    private Listener listener;

    private IOConsoleOutputStream info;
    private IOConsoleOutputStream out;
    private IOConsoleOutputStream error;

    public void openConsole(){
        ensureConsole();

        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
        manager.showConsoleView( console );
    }

    protected void ensureConsole(){
        if( console == null ){
            IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
            console = new IOConsole( "TinyOS Debug Console", "tiny os debug console", null );

            manager.addConsoles( new IConsole[]{ console } );

            listener = new Listener();
            console.addPatternMatchListener( listener );

            console.getInputStream().setColor( ColorConstants.green );

            out = console.newOutputStream();
            try{
                out.write( "> " );
            }
            catch ( IOException e ){
                e.printStackTrace();
            }

            error = console.newOutputStream();
            error.setColor( ColorConstants.red );
            
            info = console.newOutputStream();
            info.setColor( ColorConstants.blue );
        }
    }

    protected abstract void execute( String line, OutputStream info, OutputStream out, OutputStream error );

    private class Listener implements IPatternMatchListener{
        public int getCompilerFlags(){
            return 0;
        }

        public String getLineQualifier(){
            return null;
        }

        public String getPattern(){
            return ".*\\r(\\n?)|.*\\n";
        }

        public void connect( TextConsole console ){
            // nothing
        }

        public void disconnect(){
            // nothing
        }

        public void matchFound( PatternMatchEvent event ){
            IDocument document = console.getDocument();

            try{
                String line = document.get( event.getOffset(), event.getLength() );
                if( line.startsWith( "> " )){
                    line = line.substring( 1 ).trim();
                    Debug.info( line );
                    execute( line, info, out, error );
                    out.flush();
                    error.flush();
                    out.write( "\n> " );
                    out.flush();
                }
            }
            catch( BadLocationException e ){
                e.printStackTrace();
            }
            catch( IOException e ){
                e.printStackTrace();
            }
        }
    }
}
