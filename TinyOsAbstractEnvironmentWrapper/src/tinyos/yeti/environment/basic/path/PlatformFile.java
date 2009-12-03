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
package tinyos.yeti.environment.basic.path;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A platform file represents a file <code>.platform</code> <b>or</b> a file
 * that follows the same syntax as <code>.family</code> or <code>.sensor</code>.
 * @author Benjamin Sigg
 */
public class PlatformFile implements IPlatformFile{
    private String[] includes;
    private String architecture;
    private String mmcu;

    /**
     * Creates a new empty file
     */
    public PlatformFile(){
    }

    public void readFrom( File file ) throws IOException{
        BufferedReader reader = null;
        try{
            reader = new BufferedReader( new FileReader( file ));
            readPlatformFile( reader );
        }
        finally{
            if( reader != null ){
                reader.close();
            }
        }
    }

    private void readPlatformFile( Reader reader ) throws IOException{   
        StringBuffer buffer = new StringBuffer();

        int read;
        boolean ignore = false;
        while( (read = reader.read()) != -1 ){
            char c = (char)read;
            if( c == '#' ){
                ignore = true;
            }
            else{
                if( c == '\n' || c == '\r' )
                    ignore = false;

                if( !ignore )
                    buffer.append( (char)read );    
            }
        }
        String content = buffer.toString();

        // search for new includes
        List<String> includes = new ArrayList<String>();
        if( this.includes != null ){
            for( String include : this.includes ){
                includes.add( include );
            }
        }
        
        searchIncludes( content, includes );
        searchPushOpts( content, includes );
        
        this.includes = includes.toArray( new String[ includes.size() ] );

        String architecture = searchOpt( "-fnesc-target", content );
        if( architecture != null )
            this.architecture = architecture;

        String mmcu = searchOpt( "mmcu", content );
        if( mmcu != null )
            this.mmcu = mmcu;
    }

    private void searchIncludes( String content, List<String> includes ){
        Scanner scanner = new Scanner( content );

        if( scanner.findWithinHorizon( "\\@includes", content.length() ) == null ){
            scanner.close();
            return;
        }

        while( scanner.findInLine( "\\(" ) == null && scanner.hasNextLine() )
            scanner.nextLine();

        while( true ){
            String found = scanner.findInLine( "\\)|(([\\S&&[^\\)]]+)|(\\\".*\\\"))" );
            if( found != null ){
                if( found.equals( ")" ))
                    break;
                includes.add( found );
                continue;
            }
            if( !scanner.hasNextLine() )
                break;

            scanner.nextLine();
        }
    }
    
    private void searchPushOpts( String content, List<String> includes ){
        Scanner scanner = new Scanner( content );
        
        while( scanner.hasNextLine() ){
            String line = scanner.nextLine().trim();
            if( line.startsWith( "push" )){
                line = line.substring( 5 ).trim();
                List<String> args = readPushArguments( line );
                if( args.size() >= 2 ){
                    if( args.get( 0 ).equals( "@opts" )){
                        for( int i = 1, n = args.size(); i<n; i++ ){
                            String arg = args.get( i );
                            if( arg.startsWith( "-I" )){
                                arg = arg.substring( 2 );
                                arg = arg.replace( "$TOSDIR", "%T" );
                                includes.add( arg );
                            }
                        }
                    }
                }
            }
        }
    }
    
    private List<String> readPushArguments( String line ){
        List<String> list = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        
        boolean string = false;
        boolean reading = true;
        
        for( int i = 0, n = line.length(); i<n; i++ ){
            char c = line.charAt( i );
            if( c == '\"' ){
                string = !string;
                
                if( reading ){
                    if( string ){
                        builder.setLength( 0 );
                    }
                    else{
                        list.add( builder.toString() );
                        builder.setLength( 0 );
                        reading = false;
                    }
                }
            }
            else{
                if( string ){
                    if( reading ){
                        builder.append( c );
                    }
                }
                else{
                    if( c == ',' || c == ';' ){
                        if( reading ){
                            list.add( builder.toString().trim() );
                            builder.setLength( 0 );
                        }
                        reading = true;
                    }
                    else if( reading ){
                        if( Character.isWhitespace( c ) && builder.length() > 0 ){
                            list.add( builder.toString().trim() );
                            reading = false;
                        }
                        else{
                            builder.append( c );
                        }
                    }
                }
            }
        }
        
        return list;
    }
    
    private String searchOpt( String name, String file ){
        Scanner scanner = new Scanner( file );
        String result = null;

        scanner.findWithinHorizon( "\\@opts", file.length() );
        while( scanner.findInLine( "\\(" ) == null && scanner.hasNextLine() )
            scanner.nextLine();

        String pattern = "\\)|.*"+name+"=.*";

        while( true ){
            String found = scanner.findInLine( pattern );
            if( found != null ){
                if( found.equals( ")" ))        
                    break;

                int index = found.indexOf( "=" );
                found = found.substring( index+1 ).trim();
                
                int quoteIndex = found.indexOf( '\"' );
                int commaIndex = found.indexOf( ',' );
                
                if( quoteIndex >= 0 || commaIndex >= 0 ){
                    int cutIndex;
                    if( quoteIndex >= 0 && commaIndex >= 0 )
                        cutIndex = Math.min( quoteIndex, commaIndex );
                    else if( quoteIndex >= 0 )
                        cutIndex = quoteIndex;
                    else
                        cutIndex = commaIndex;
                    
                    found = found.substring( 0, cutIndex ).trim();
                }
                
                result = found;
                break;
            }
            if( !scanner.hasNextLine() )
                break;

            scanner.nextLine();
        }

        scanner.close();
        return result;
    }

    public String[] getIncludes(){
        return includes;
    }

    public String getArchitecture(){
        return architecture;
    }

    public String getMMCU(){
        return mmcu;
    }
}
