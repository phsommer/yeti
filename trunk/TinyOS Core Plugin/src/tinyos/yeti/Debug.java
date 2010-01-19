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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * The global debug object is used to print out debug information that
 * is provided from various places within this project.
 * @author Benjamin Sigg
 *
 */
@Debug.DebugInvisible
public class Debug {
    /**
     * Classes marked with this interface are not visible for {@link Debug}.
     * @author Benjamin Sigg
     */
    @Target( { ElementType.TYPE } )
    @Retention( RetentionPolicy.RUNTIME )
    public static @interface DebugInvisible {}


    /** whether debug is on of off */
    public static boolean DEBUG = false;
    
    private static boolean DEBUG_TO_TINYOS_CONSOLE = false;
    
    public static final String DEBUG_PREFERENCE = "debug.state";
    public static final String DEBUG_PREFERENCE_CONSOLE = "debug.console";
    
    public static void connect( final TinyOSPlugin plugin ){
        IPreferenceStore store = plugin.getPreferenceStore();
        store.setDefault( DEBUG_PREFERENCE, false );
        store.setDefault( DEBUG_PREFERENCE_CONSOLE, false );
        
        DEBUG = store.getBoolean( DEBUG_PREFERENCE );
        DEBUG_TO_TINYOS_CONSOLE = store.getBoolean( DEBUG_PREFERENCE_CONSOLE );

        store.addPropertyChangeListener( new IPropertyChangeListener(){
            public void propertyChange( PropertyChangeEvent event ){
                if( DEBUG_PREFERENCE.equals( event.getProperty() )){
                    IPreferenceStore store = plugin.getPreferenceStore();
                    DEBUG = store.getBoolean( DEBUG_PREFERENCE );
                }
                if( DEBUG_PREFERENCE_CONSOLE.equals( event.getProperty() )){
                    IPreferenceStore store = plugin.getPreferenceStore();
                    DEBUG_TO_TINYOS_CONSOLE = store.getBoolean( DEBUG_PREFERENCE_CONSOLE );
                }
            }
        });
    }
    
    public static void println( String text ){
        if( DEBUG_TO_TINYOS_CONSOLE ){
            TinyOSPlugin.getDefault().getConsole().out().println( text );
        }
        System.out.println( text );
    }
    
    public static void print( String text ){
        if( DEBUG ){
            // NOT printing on tinyos console
            
            System.out.print( text );
        }
    }
    
    public static void info( String info ){
        if( DEBUG ){
            println( "[info] " + caller() + ": " + info );
        }
    }

    public static void info( Object info ){
        if( DEBUG ){
            info( String.valueOf( info ));
        }
    }

    public static void warning( String warning ){
        if( DEBUG ){
            println( "[warning] " + caller() + ": " + warning );
        }
    }

    public static void error( String error ){
        if( DEBUG ){
            println( "[error] " + caller() + ": " + error );
        }
    }

    public static void error( Throwable error ){
        error( error.getMessage() );
    }

    public static void enter(){
        if( DEBUG ){
            println( "[->] " + caller() );
        }
    }

    public static void leave(){
        if( DEBUG ){
            println( "[<-] " + caller() );
        }
    }

    private static String caller(){
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for( StackTraceElement element : elements ){
            try {
                String className = element.getClassName();
                if( !className.startsWith( "java" )){
                    Class<?> clazz = Class.forName( className );
                    if( clazz.getAnnotation(DebugInvisible.class) == null ){
                        return caller( element );
                    }
                }
            }
            catch (ClassNotFoundException e) {
                return caller( element );
            }
        }
        return null;
    }

    private static String caller( StackTraceElement element ){
        return element.getClassName() + "." + element.getMethodName() + " at " + element.getLineNumber();
    }
}
