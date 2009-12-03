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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import tinyos.yeti.preprocessor.BaseFileInfo;
import tinyos.yeti.preprocessor.Preprocessor;
import tinyos.yeti.preprocessor.PreprocessorReader;
import tinyos.yeti.preprocessor.lexer.Macro.VarArg;
import tinyos.yeti.preprocessor.lexer.macro.GenericMacro;

/*
 *                   WARNING
 *              
 *     This is an important class, do not delete!
 * 
 */

/**
 * This class contains all the code that is necessary to read
 * the "parser.cup" file which contains preprocessor directives,
 * call CUP and then rewrite the output of CUP such that the limit
 * for initializers is not exceeded.
 * @author Benjamin Sigg
 *
 */
public class ParserCreation {
    public static void main( String[] args ) throws Exception{
        ParserCreation c;
        
        c= new ParserCreation();
        c.runCollectorLexer();
        
        c= new ParserCreation();
        c.runCollectorParser();
        
        c= new ParserCreation();
        c.runMainLexer();
        
        c= new ParserCreation();
        c.runMainParser();
    }
    
    /** number of string repository files that were created */
    private int repositoryCount = 0;
    
    /** package into which the parser will be put */
    public static final String MAIN_PACKAGE_NAME = "tinyos.yeti.nesc12.parser";
    /** name of the directory into which the parser will be put */
    public static final String MAIN_DIRECTORY_NAME = "src/" + MAIN_PACKAGE_NAME.replaceAll( "\\.", "/" );
    
    /** name of the package into which the actions will be put*/
    public static final String MAIN_ACTION_PACKAGE_NAME = "tinyos.yeti.nesc12.parser.actions";
    /** name of the directory into which the actions will be put */
    public static final String MAIN_ACTION_DIRECTORY_NAME = "src/" + MAIN_ACTION_PACKAGE_NAME.replaceAll( "\\.", "/" );
    
    /** package into which the parser will be put */
    public static final String COLLECTOR_PACKAGE_NAME = "tinyos.yeti.nesc12.collector";
    /** name of the directory into which the parser will be put */
    public static final String COLLECTOR_DIRECTORY_NAME = "src/" + COLLECTOR_PACKAGE_NAME.replaceAll( "\\.", "/" );
    
    /** name of the package into which the actions will be put*/
    public static final String COLLECTOR_ACTION_PACKAGE_NAME = "tinyos.yeti.nesc12.collector.actions";
    /** name of the directory into which the actions will be put */
    public static final String COLLECTOR_ACTION_DIRECTORY_NAME = "src/" + COLLECTOR_ACTION_PACKAGE_NAME.replaceAll( "\\.", "/" );
    
    /** the imports every action needs to have */
    public static final String[] MAIN_IMPORTS = new String[]{
        //"java_cup.runtime.Symbol",
        "tinyos.yeti.nesc12.lexer.Token",
        "tinyos.yeti.nesc12.lexer.Lexer",
        "tinyos.yeti.nesc12.parser.ast.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.declaration.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.definition.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.error.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.expression.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.general.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.nesc.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.statement.*"
    };
    
    public static final String[] COLLECTOR_IMPORTS = new String[]{
        //"java_cup.runtime.Symbol",
        "tinyos.yeti.nesc12.parser.StringRepository",
        "tinyos.yeti.nesc12.parser.ScopeStack",
        "tinyos.yeti.nesc12.parser.RawParser",
        
        "tinyos.yeti.nesc12.lexer.Token",
        "tinyos.yeti.nesc12.lexer.Lexer",
        "tinyos.yeti.nesc12.parser.ast.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.declaration.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.definition.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.error.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.expression.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.general.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.nesc.*",
        "tinyos.yeti.nesc12.parser.ast.nodes.statement.*"
    }; 
        
    
    /**
     * Runs the CUP parser generator.
     * @throws Exception on any error
     */
    public void runMainParser() throws Exception{
        String dir = "bin/data/tinyos/parser/";
        String name = dir + "parser.cup";
        String temp = "temp.cup";
        
        Preprocessor preprocessor = new Preprocessor();
        PreprocessorReader in = preprocessor.open( new BaseFileInfo( new File( name )), new FileReader( name ), null );
        
        OutputStream out = new BufferedOutputStream( new FileOutputStream( temp ));
        int read;
        while( (read = in.read()) != -1 )
            out.write( read );
        out.close();
        
        System.setIn( new BufferedInputStream( new FileInputStream( temp ) ));
        
        runMain( "java_cup.Main",
                "-package", MAIN_PACKAGE_NAME,
                "-expect", "0"
                //"-nopositions",
                //"-dump_grammar",
                //"-dump_states",
                //"-dump_tables",   
                // "-debug"
                
        );
        
        // copy the files
        move( "parser.java", MAIN_DIRECTORY_NAME + "/parser.java", true,
                MAIN_DIRECTORY_NAME, MAIN_PACKAGE_NAME,
                MAIN_ACTION_DIRECTORY_NAME, MAIN_ACTION_PACKAGE_NAME,
                MAIN_IMPORTS );
        move( "sym.java", MAIN_DIRECTORY_NAME + "/sym.java", false, null, null,
                null, null, null );
    }
    
    

    /**
     * Runs the CUP parser generator.
     * @throws Exception on any error
     */
    public void runCollectorParser() throws Exception{
        String dir = "bin/data/tinyos/parser/";
        String name = dir + "parser.cup";
        String temp = "temp.cup";
        
        Preprocessor preprocessor = new Preprocessor();
        preprocessor.addMacro( new GenericMacro( "COLLECTOR", new String[]{}, VarArg.NO, "" ) );
        PreprocessorReader in = preprocessor.open( new BaseFileInfo( new File( name )), new FileReader( name ), null );
        
        OutputStream out = new BufferedOutputStream( new FileOutputStream( temp ));
        int read;
        while( (read = in.read()) != -1 )
            out.write( read );
        out.close();
        
        System.setIn( new BufferedInputStream( new FileInputStream( temp ) ));
        
        runMain( "java_cup.Main",
                "-package", COLLECTOR_PACKAGE_NAME,
                "-expect", "0"
                //"-nopositions",
                //"-dump_grammar",
                //"-dump_states",
                //"-dump_tables",   
                // "-debug"
        );
        
        // copy the files
        move( "parser.java", COLLECTOR_DIRECTORY_NAME + "/parser.java", true,
                COLLECTOR_DIRECTORY_NAME, COLLECTOR_PACKAGE_NAME,
                COLLECTOR_ACTION_DIRECTORY_NAME, COLLECTOR_ACTION_PACKAGE_NAME,
                COLLECTOR_IMPORTS );
        move( "sym.java", COLLECTOR_DIRECTORY_NAME + "/sym.java", false, null, null,
                null, null, null );
    }
    
    private void runMain( String className, String... args ){
        try{
            Class<?> clazz = Class.forName( className );
            Method main = clazz.getMethod( "main", String[].class );
            main.invoke( null, new Object[]{ args } );
        }
        catch( Exception e ){
            e.printStackTrace();
        }
    }
    
    public void runMainLexer() throws Exception{
        String dir = "src/tinyos/yeti/nesc12/lexer";
        String name = "bin/data/tinyos/parser/tokens.jflex";
        String temp = "temp.jflex";
        
        Preprocessor preprocessor = new Preprocessor();
        PreprocessorReader in = preprocessor.open( new BaseFileInfo( new File( name )), new FileReader( name ), null );
        
        OutputStream out = new BufferedOutputStream( new FileOutputStream( temp ));
        int read;
        while( (read = in.read()) != -1 )
            out.write( read );
        out.close();
        
        runMain( "JFlex.Main", "-d", dir, temp );
    }
    
    public void runCollectorLexer() throws Exception{
        String dir = "src/tinyos/yeti/nesc12/collector";
        String name = "bin/data/tinyos/parser/tokens.jflex";
        String temp = "temp.jflex";
        
        Preprocessor preprocessor = new Preprocessor();
        preprocessor.addMacro( new GenericMacro( "COLLECTOR", new String[]{}, VarArg.NO, "" ) );
        PreprocessorReader in = preprocessor.open( new BaseFileInfo( new File( name )), new FileReader( name ), null );
        
        OutputStream out = new BufferedOutputStream( new FileOutputStream( temp ));
        int read;
        while( (read = in.read()) != -1 )
            out.write( read );
        out.close();
        
        runMain( "JFlex.Main", "-d", dir, temp );
    }
    
    
    /**
     * Moves the file with path <code>source</code> to <code>dest</code>.
     * @param source the source file
     * @param dest the destination file
     * @param replace whether to parse and replace elements of the moved file 
     * @throws IOException
     */
    private void move( String source, String dest, boolean replace,
            String directoryName, String packageName,
            String actionDirectoryName, String actionPackageName,
            String[] imports ) throws IOException{
        System.out.println( "Move " + source + " to " + dest );
        
        InputStream in = new FileInputStream( source );
        if( replace ){
            in = new ReplacingStream( in, directoryName, packageName, actionDirectoryName, actionPackageName, imports );
        }
        OutputStream out = new FileOutputStream( dest );
        
        int next;
        while( (next=in.read()) != -1 )
            out.write( next );
        
        in.close();
        out.close();
        
        new File( source ).delete();
        System.out.println( "... done" );
    }
    
    /**
     * Opens a writer to write a string repository file.
     * @return the new writer
     * @throws IOException
     */
    private StringFileWriter nextFile( String directoryName ) throws IOException{
        String name = "repository"+(repositoryCount++)+".txt";
        String file = directoryName + "/" + name;
        DataOutputStream out = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( file )));
        return new StringFileWriter( name, out );
    }
    
    /**
     * Opens a writer that can write actions.
     * 
     * @return the action writer
     * @throws IOException
     */
    private ActionClassWriter openActionWriter( String directoryName,
            String actionDirectoryName, String packageName,
            String actionPackageName, String[] imports ) throws IOException{
        String className = "ParserActionRepository";
        String file = directoryName + "/" + className + ".java";
        PrintStream out = new PrintStream( new BufferedOutputStream(
                new FileOutputStream( file ) ) );

        ActionClassWriter writer = new ActionClassWriter( className, out,
                actionDirectoryName, actionPackageName, packageName, imports );
        return writer;
    }
    
    /** the name of the method whose arguments have to be rewritten */
    private static final String METHOD = "unpackFromStrings";
    private static final String SWITCH = "switch (CUP$parser$act_num)";
    private static final String CALL = "return action_obj.CUP$parser$do_action(act_num, parser, stack, top);";
    private static final String PARSER = "public class parser extends java_cup.runtime.lr_parser";
    
    /**
     * Reads the characters of a string and returns them as an array of integers.
     * @param string the string to read
     * @return the characters of <code>string</code>
     */
    private int[] toInt( String string ){
        int[] result = new int[ string.length() ];
        for( int i = 0, n = result.length; i<n; i++ )
            result[ i ] = string.charAt( i );
        return result;
    }
    
    private class ReplacingStream extends InputStream{
        private InputStream base;
        
        private Buffer buffer;
        
        private int[] methodName = toInt( METHOD );
        private boolean justReadMethod = false;
        
        private int[] switchName = toInt( SWITCH );
        private boolean justReadSwitch = false;
        
        private int[] callName = toInt( CALL );
        private boolean justReadCall = false;
        
        private int[] parserName = toInt( PARSER );
        
        private String replace;
        private int replaceOffset;
        private boolean replacing = false;
        
        private ActionClassWriter actionClassWriter;
        
        private String directoryName;
        private String packageName;
        private String actionDirectoryName;
        private String actionPackageName;
        private String[] imports;
        
        public ReplacingStream( InputStream base, String directoryName,
                String packageName, String actionDirectoryName,
                String actionPackageName, String[] imports ){
            
            this.base = base;
            this.directoryName = directoryName;
            this.packageName = packageName;
            this.actionDirectoryName = actionDirectoryName;
            this.actionPackageName = actionPackageName;
            this.imports = imports;
        }
        
        private void init() throws IOException{
            actionClassWriter = openActionWriter( directoryName, actionDirectoryName, packageName, actionPackageName, imports );
            actionClassWriter.open();
            
            int n = CALL.length() + 1;
            buffer = new Buffer( n );
            int i = 0;
            while( i < n ){
                buffer.put( i, base.read() );
                i++;
            }
        }
        
        @Override
        public void close() throws IOException{
            actionClassWriter.close();
            super.close();
        }
        
        @Override
        public int read() throws IOException {
            if( buffer == null )
                init();
            
            int top = buffer.top();
            int read;
            if( replacing ){
                if( replaceOffset >= replace.length() ){
                    read = base.read();
                    replacing = false;
                    justReadMethod = false;
                    justReadSwitch = false;
                    replace = "";
                    replaceOffset = 0;
                }
                else{
                    read = replace.charAt( replaceOffset++ );
                }
            }
            else{
                read = base.read();
            }
            
            buffer.push( read );
            
            
            if( justReadMethod && !replacing ){
                if( read == '(' ){
                    skipMethod();
                    replaceOffset = 0;
                    replacing = true;
                }
            }
            else if( justReadSwitch && !replacing ){
                if( read == '{' ){
                    // buffer.replaceLast( 1, ' ' );
                    buffer.delete( 1 );
                    skipSwitch();
                    buffer.push( base.read() );
                    
                    replace = "return null;";
                    replacing = true;
                    replaceOffset = 0;
                }
            }
            else if( justReadCall && !replacing ){
                if( read == '}' ){
                    justReadCall = false;
                    replacing = true;
                    replaceOffset = 0;
                    replace = "\n" + actionClassWriter.createActionList();
                }
            }
            else{
                if( buffer.isEnding( callName )){
                    justReadCall = true;
                    buffer.delete( CALL.length() );
                    replace = "return " + actionClassWriter.callAction() + ";";
                    replacing = true;
                    replaceOffset = CALL.length();
                    
                    buffer.fill( CALL.length(), replace, base );
                }
                else if( buffer.isEnding( parserName )){
                    buffer.delete( PARSER.length() );
                    replace = "public abstract class parser extends java_cup.runtime.lr_parser implements RawParser";
                    replacing = true;
                    replaceOffset = 0; 
                }
                else{
                    justReadMethod = isMethod();
                    justReadSwitch = isSwitch();
                    if( justReadSwitch ){
                        buffer.replaceLast( SWITCH.length(), ' ' );
                    }
                }
            }
            
            return top;
        }
        
        private void skipMethod() throws IOException{
            int read;
            boolean string = false;
            boolean inString = false;
            
            StringFileWriter writer = nextFile( directoryName );
            
            while( (read = base.read()) != ')' ){
                if( string ){
                    if( read == '"' )
                        inString = !inString;
                    
                    if( read == ',' || read == '}' ){
                        string = false;
                        writer.endString();
                    }
                    else{
                        if( inString && read != '"' ){
                            writer.push( read );
                        }
                    }
                }
                else if( read == '"' ){
                    string = true;
                    inString = true;
                    writer.beginString();
                }
            }
            
            writer.close();
            replace = writer.getStringAccess() + ")";
        }
        
        private void skipSwitch() throws IOException{
            int[] caseName = toInt( "case" );
            int[] returnName = toInt( "return" );
            int[] semiName = toInt( ";" );
            int[] colonName = toInt( ":" );
            boolean caseRead = false;
            
            int bufferLength = returnName.length;
            Buffer buffer = new Buffer( bufferLength );
            
            for( int i = 0; i < bufferLength; i++ )
                buffer.put( i, base.read() );
            
            int read;
            int count = 1;
            
            StringBuilder builder = new StringBuilder();
            
            boolean building = false;
            boolean onReturn = false;
            boolean inString = false;
            
            int currentCaseNumber = -1;
            
            while( count != 0 && (read=base.read()) != -1 ){
                buffer.push( read );
                if( !caseRead && buffer.isEnding( caseName )){
                    caseRead = true;
                    StringBuilder number = new StringBuilder();
                    while( !buffer.isEnding( colonName )){
                        read = base.read();
                        buffer.push( read );
                        if( Character.isDigit( read )){
                            number.append( (char)read );
                        }
                    }
                    currentCaseNumber = Integer.parseInt( number.toString() );
                    building = true;
                }
                else if( building ){
                    if( (char)read == '"' )
                        inString = !inString;
                    
                    builder.append( (char)read );
                    
                    if( !inString ){
                        if( buffer.isEnding( returnName )){
                            onReturn = true;
                        }
                        
                        if( onReturn && buffer.isEnding( semiName )){
                            building = false;
                            onReturn = false;
                            
                            actionClassWriter.push( currentCaseNumber, builder.toString() );
                            caseRead = false;
                            
                            builder.setLength( 0 );
                        }
                    }
                }
                
                if( !inString ){
                    if( read == '{' )
                        count++;
                    else if( read == '}' )
                        count--;
                }
            }
        }
        
        private boolean isMethod(){
            return buffer.isEnding( methodName );
        }
        
        private boolean isSwitch(){
            return buffer.isEnding( switchName );
        }
    }
    
    /**
     * A buffer holding some integers, the buffer can tell whether the last
     * n items equal another list of integers.
     * @author Benjamin Sigg
     */
    private class Buffer{
        private int[] buffer;
        private int offset;
        
        public Buffer( int size ){
            buffer = new int[ size ];
        }
        
        public void put( int index, int value ){
            buffer[ index ] = value;
        }
        
        public int top(){
            return buffer[ offset ];
        }
        
        public void push( int value ){
            buffer[ offset ] = value;
            offset = (offset+1) % buffer.length;
        }
        
        public boolean isEnding( int[] item ){
            int j = offset - item.length;
            if( j < 0 )
                j += buffer.length;
            
            for( int i = 0, n = item.length; i<n; i++ ){
                if( buffer[j] != item[i])
                    return false;
                
                j = (j+1)%buffer.length;
            }
            return true;
        }
        
        public void delete( int length ){
            offset -= length;
            if( offset < 0 )
                offset += buffer.length;
        }
        
        public void fill( int length, String start, InputStream more ) throws IOException{
            for( int i = 0, n = Math.min( length, start.length() ); i<n; i++ )
                push( start.charAt( i ) );
            
            for( int i = Math.min( length, start.length() ); i<length; i++ )
                push( more.read() );
        }
        
        public void replaceLast( int length, char replacement ){
            for( int i = 0, j = offset-1; i<length; i++, j-- ){
                if( j < 0 )
                    j += buffer.length;
                
                buffer[j] = replacement;
            }
        }
    }
    
    /**
     * Class generating files which contain the strings for the 
     * method {@value ParserCreation#METHOD}.
     */
    private class StringFileWriter{
        private DataOutputStream out;
        private String file;
        
        private StringBuilder buffer = new StringBuilder();
        private StringBuilder builder = new StringBuilder();
        
        private boolean justPutBackslash = false;
        private boolean onUnicode = false;
        
        public StringFileWriter( String file, DataOutputStream out ){
            this.out = out;
            this.file = file;
        }
        
        public void beginString() throws IOException{
            builder.setLength( 0 );
        }
        
        public void push( int c ) throws IOException{
            if( c == '\\' ){
                buffer.append( (char)c );
                justPutBackslash = true;
                onUnicode = false;
            }
            else if( buffer.length() > 0 ){
                if( justPutBackslash && c == 'u' )
                    onUnicode = true;
                justPutBackslash = false;
                buffer.append( (char)c );
                
                if( onUnicode && buffer.length() == 6 ){
                    int i = Integer.parseInt( buffer.substring( 2, 6 ), 16 );
                    builder.append( (char)i );
                    buffer.setLength( 0 );
                }
                else if( !onUnicode && buffer.length() == 4 ){
                    int i = Integer.parseInt( buffer.substring( 1, 4 ), 8 );
                    builder.append( (char)i );
                    buffer.setLength( 0 );
                }
            }
            else{
                justPutBackslash = false;
                builder.append( (char)c );
            }
        }

        public void endString() throws IOException{
            out.writeInt( builder.length() );
            for( int i = 0, n = builder.length(); i<n; i++ ){
                out.writeChar( builder.charAt( i ) );
            }            
        }
        
        public void close() throws IOException{
            out.writeInt( -1 );
            out.close();
        }
        
        public String getStringAccess(){
            return "StringRepository.get( parser.class, \"" + file + "\" )";
        }
    }

    /**
     * A class writing a single class file containing the different 
     * case-statements as action-objects.
     * @author Benjamin Sigg
     */
    private class ActionClassWriter{
        private PrintStream out;
        private String className;
        
        private String actionDirectoryName;
        private String actionPackageName;
        private String packageName;
        private String[] imports;
        
        private Set<Integer> numbers = new HashSet<Integer>();
        
        public ActionClassWriter( String className, PrintStream out,
                String actionDirectoryName,
                String actionPackageName,
                String packageName,
                String[] imports ){
            
            this.className = className;
            this.out = out;
            this.actionDirectoryName = actionDirectoryName;
            this.actionPackageName = actionPackageName;
            this.packageName = packageName;
            this.imports = imports;
        }

        public String createActionList(){
            return "\tprotected static final ParserAction[] _parser_actions = " +
                    "ParserActionRepository.cases();";
        }
        
        public String callAction(){
            return "_parser_actions[ act_num ].do_action( act_num, parser, stack, top, this )";
        }
        
        public void open() throws IOException{
            out.print( "package " );
            out.print( packageName );
            out.println( ";" );
            
            out.print( "import " );
            out.print( actionPackageName );
            out.println( ".*;" );
            
            out.println();
            
            out.print( "public class " );
            out.print( className );
            out.println( "{" );
            
            
            PrintStream interfaze = new PrintStream( new FileOutputStream( actionDirectoryName + "/ParserAction.java" ));
            interfaze.print( "package " );
            interfaze.print( actionPackageName );
            interfaze.println( ";" );
            interfaze.print( "import " );
            interfaze.print( packageName );
            interfaze.println( ".*;" );
            interfaze.println();
            interfaze.println( "public interface ParserAction{" );
            interfaze.println( "\tpublic java_cup.runtime.Symbol do_action(" );
            interfaze.println( "\t\tint                        CUP$parser$act_num," );
            interfaze.println( "\t\tjava_cup.runtime.lr_parser CUP$parser$parser," );
            interfaze.println( "\t\tjava.util.Stack            CUP$parser$stack," );
            interfaze.println( "\t\tint                        CUP$parser$top," );
            interfaze.println( "\t\tparser                     parser)" );
            interfaze.println( "\t\tthrows java.lang.Exception;" );
            interfaze.println( "}" );
            interfaze.close();
        }
        
        public void push( int number, String code )throws IOException{
            numbers.add( number );
            
            PrintStream out = new PrintStream( new FileOutputStream( actionDirectoryName + "/Action" + number + ".java" ));
            
            out.print( "package " );
            out.print( actionPackageName );
            out.println( ";" );
            
            for( String im : imports ){
                out.print( "import " );
                out.print( im );
                out.println( ";" );
            }
            
            out.print( "import " );
            out.print( packageName );
            out.println( ".*;" );
            
            out.print( "public final class Action" );
            out.print( number );
            out.println( " implements ParserAction{" );
            
            out.println( "\tpublic final java_cup.runtime.Symbol do_action(" );
            out.println( "\t\tint                        CUP$parser$act_num," );
            out.println( "\t\tjava_cup.runtime.lr_parser CUP$parser$parser," );
            out.println( "\t\tjava.util.Stack            CUP$parser$stack," );
            out.println( "\t\tint                        CUP$parser$top," );
            out.println( "\t\tparser                     parser)" );
            out.println( "\t\tthrows java.lang.Exception{" );
            out.println( "\tjava_cup.runtime.Symbol CUP$parser$result;" );
            out.println( filterRanges( code ) );
            out.println( "\t}" );
            out.println( "}" );
            
            out.close();
        }
        
        private String filterRanges( String code ){
            StringBuilder builder = new StringBuilder();
            Scanner sc = new Scanner( code );
            while( sc.hasNextLine() ){
                String line = sc.nextLine();
                String lineTrimmed = line.trim();
                String check = null;
                if( lineTrimmed.startsWith( "int " ) && lineTrimmed.endsWith( ".left;" )){
                    check = checkFilterRanges( lineTrimmed, "left" );
                }
                else if( lineTrimmed.startsWith( "int " ) && lineTrimmed.endsWith( ".right;" )){
                    check = checkFilterRanges( lineTrimmed, "right" );
                }
                if( check != null ){
                    if( moreThanOnce( code, check )){
                        builder.append( line );
                        builder.append( "\n" );
                    }
                }
                else{
                    builder.append( line );
                    builder.append( "\n" );
                }
            }
            
            return builder.toString();
        }
        
        private String checkFilterRanges( String line, String name ){
            int index = line.indexOf( name );
            return line.substring( 4, index + name.length() );
        }
        
        private boolean moreThanOnce( String code, String variable ){
            int index = code.indexOf( variable );
            if( index < 0 )
                return false;
            
            index = code.indexOf( variable, index+1 );
            return index >= 0;
        }
        
        public void close(){
            
            out.println( "\tpublic static ParserAction[] cases(){" );
            out.println( "\t\tParserAction[] result = new ParserAction[]{" );
            int max = 0;
            for( int n : numbers )
                max = Math.max( n, max );
            
            for( int i = 0; i <= max; i++ ){
                if( numbers.contains( i )){
                    out.print( "\t\t\tnew Action" );
                    out.print( i );
                    out.println( "()," );
                }
            }
            
            out.println( "\t\t};" );
            out.println( "\t\treturn result;" );
            out.println( "\t}" );
            
            out.println( "}" );
            out.close();
        }
    }
}
