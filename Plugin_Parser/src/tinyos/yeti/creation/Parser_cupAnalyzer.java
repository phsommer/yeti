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

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import tinyos.yeti.preprocessor.lexer.PurgingReader;

public class Parser_cupAnalyzer {
    public static void main( String[] args ) throws Exception{
        Scanner parse = new Scanner( new PurgingReader( new FileReader( "bin/data/tinyos/parser/parser.cup" ), null, null ));
        Map<String, Rule> rules = new HashMap<String, Rule>();
        Rule current = null;
        
        boolean code = false;
        
        while( parse.hasNextLine() ){
            String line = parse.nextLine();
            if( line.length() != 0 ){
                if( line.contains( "{:" )){
                    code = true;
                }
                if( !code && !line.trim().equals( "|" ) && !line.trim().equals( ";" )){
                    if( current != null && Character.isWhitespace( line.charAt( 0 ))){
                        // belongs to current rule
                        current.line( line, rules );
                    }
                    else{
                        // starts a new rule
                        String name = line.trim();
                        if( name.endsWith( "::=" )){
                            name = name.substring( 0, name.length()-3 );
                            name = name.trim();
                        
                            current = rules.get( name );
                            if( current == null ){
                                current = new Rule( name );
                                rules.put( name, current );
                            }
                        }
                    }
                }
                if( line.contains( ":}" )){
                    code = false;
                }
            }
        }
        
        parse.close();
        CParseYAnalzyer.run( rules );
    }
}
