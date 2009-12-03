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
package tinyos.yeti.creation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class is needed only for development and has no further use in
 * the project.
 * @author Benjamin Sigg
 */
public class CParseYAnalzyer {
    public static void main( String[] args ) throws IOException{
        Scanner parse = new Scanner( new File( "bin/data/tinyos/parser/nesc.txt" ));
        Map<String, Rule> rules = new HashMap<String, Rule>();
        Rule current = null;
        
        while( parse.hasNextLine() ){
            String line = parse.nextLine();
            if( line.length() != 0 ){
                if( Character.isWhitespace( line.charAt( 0 ))){
                    // belongs to current rule
                    current.line( line, rules );
                }
                else{
                    // starts a new rule
                    String name = line.trim();
                    if( name.endsWith( ":" ))
                        name = name.substring( 0, name.length()-1 );
                    
                    current = rules.get( name );
                    if( current == null ){
                        current = new Rule( name );
                        rules.put( name, current );
                    }
                }
            }
        }
        
        parse.close();
        run( rules );
    }
    
    public static void run( Map<String, Rule> rules ){
        Scanner in = new Scanner( System.in );
        while( true ){
            System.out.print( "> " );
            String cmd = in.next();
            if( "print".equals( cmd )){
                Rule rule = rules.get( in.next() );
                if( rule == null )
                    System.out.println( "No rule found" );
                else
                    rule.print();
            }
            else if( "cup".equals( cmd )){
                Rule rule = rules.get( in.next() );
                if( rule == null )
                    System.out.println( "No rule found" );
                else
                    rule.cup();
            }
            else if( "children".equals( cmd )){
                Rule rule = rules.get( in.next() );
                if( rule == null )
                    System.out.println( "No rule found" );
                else
                    rule.children();
            }
            else if( "mark".equals( cmd )){
                Rule rule = rules.get( in.next() );
                if( rule == null )
                    System.out.println( "No rule found" );
                else
                    rule.mark();
            }
            else if( "markchildren".equals( cmd )){
                Rule rule = rules.get( in.next() );
                if( rule == null )
                    System.out.println( "No rule found" );
                else
                    rule.markchildren();
            }
            else if( "markall".equals( cmd )){
                Rule rule = rules.get( in.next() );
                if( rule == null )
                    System.out.println( "No rule found" );
                else{
                    rule.markall();
                    rule.cleanup();
                }
            }
            else if( "umark".equals( cmd )){
                Rule rule = rules.get( in.next() );
                if( rule == null )
                    System.out.println( "No rule found" );
                else
                    rule.umark();
            }
            else if( "marked".equals( cmd )){
                for( Map.Entry<String, Rule> entry : rules.entrySet() ){
                    if( entry.getValue().marked )
                        System.out.println( entry.getValue().name );
                }
            }
            else if( "umarked".equals( cmd )){
                for( Map.Entry<String, Rule> entry : rules.entrySet() ){
                    if( !entry.getValue().marked )
                        System.out.println( entry.getValue().name );
                }
            }
            else if( "quit".equals( cmd ) || "exit".equals( cmd )){
                System.exit( 0 );
            }
            else{
                System.out.println( "Unknown command: " + cmd );
            }
        }
    }
}


