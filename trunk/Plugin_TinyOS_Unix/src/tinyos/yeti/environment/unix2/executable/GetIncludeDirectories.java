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
package tinyos.yeti.environment.unix2.executable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.core.runtime.Status;

import tinyos.yeti.environment.basic.commands.AbstractCommand;
import tinyos.yeti.environment.basic.commands.IExecutionResult;
import tinyos.yeti.environment.basic.path.IPathReplacer;
import tinyos.yeti.environment.basic.path.IPlatformFile;
import tinyos.yeti.environment.basic.platform.IExtendedPlatform;
import tinyos.yeti.environment.unix2.TinyOSUnixEnvironmentPlugin2;
import tinyos.yeti.utility.FileUtility;

/**
 * Tries to find include directories for some architecture.
 * @author Benjamin Sigg
 */
public class GetIncludeDirectories extends AbstractCommand<String[]>{
    private File tosdir;
    private File directory;
    private IExtendedPlatform platform;
    private IPathReplacer replacer;
    
    public GetIncludeDirectories( IExtendedPlatform platform, String tosdir, IPathReplacer replacer ){
        setCommand( "ncc", "-v", "-target="+platform.getName(), "Dummy.nc" );
        this.tosdir = new File( tosdir );
        this.platform = platform;
        this.replacer = replacer;
    }
    
    public boolean shouldPrintSomething(){
        return true;
    }
    
    @Override
    public boolean setup(){
        // create temporary files
        File tempDirectory = new File( System.getProperty( "java.io.tmpdir" ) );
        Random random = new Random();
        
        directory = new File( tempDirectory, "nesc_temp" );
        while( directory.exists() ){
            directory = new File( tempDirectory, "nesc_temp" + random.nextInt() );
        }
        
        if( !directory.mkdirs() )
            return false;
        
        setDirectory( directory );
        
        File dummy = new File( directory, "Dummy.nc" );
        try{
            PrintWriter writer = new PrintWriter( dummy );
            writer.println( "configuration Dummy{ } implementation{ }" );
            writer.close();
            return true;
        }
        catch( IOException ex ){
            TinyOSUnixEnvironmentPlugin2.getDefault().getLog().log( new Status( Status.WARNING, TinyOSUnixEnvironmentPlugin2.PLUGIN_ID, ex.getMessage(), ex ));
            dummy.delete();
            directory.delete();
            return false;
        }
    }
    
    private Set<String> platformFiles(){
    	IPlatformFile file = platform.getPlatformFile();
    	if( file == null )
    		return Collections.emptySet();
    	
    	String[] includes = file.getIncludes();
    	if( includes == null )
    		return Collections.emptySet();
    	
    	Set<String> result = new HashSet<String>();
    	
    	for( String include : includes ){
    		include = replacer.replace( include );
    		if( include.endsWith( "/" ))
    			include = include.substring( 0, include.length()-1 );
    		result.add( include );
    	}
    	return result;
    }
    
    public String[] result( IExecutionResult result ){
        delete( directory );
        List<String> list = new ArrayList<String>();
        Scanner sc = new Scanner( result.getError() );
        
        boolean reading = false;
        Set<String> platformFiles = platformFiles();
        
        while( sc.hasNextLine() ){
            String line = sc.nextLine();
            if( line.contains( "#include" ) && line.contains( "search starts here" ) && line.contains( "<" ) && line.contains( ">" )){
                reading = true;
            }
            else if( reading ){
                if( line.contains( "End of search list" ))
                    reading = false;
                else{
                    line = transform( line.trim() );
                    if( line != null ){
                        File file = new File( line );
                        
                        if( !FileUtility.isAncestor( tosdir, file )){
                        	
                        	if( !platformFiles.contains( line ) && !list.contains( line )){
                                list.add( line );
                            }
                        }
                    }
                }
            }
        }
        sc.close();
        
        return list.toArray( new String[ list.size() ] );
    }
    
    private String transform( String path ){
    	boolean root = path.startsWith( File.separator );
    	
        String[] parts = path.split( "\\" + File.separatorChar );
        List<String> pathList = new ArrayList<String>();
        for( String part : parts ){
            if( part.equals( ".." )){
                if( pathList.size() == 0 )
                    return null;
                
                pathList.remove( pathList.size()-1 );
            }
            else if( part.length() > 0 ){
                pathList.add( part );
            }
        }
        
        StringBuilder result = new StringBuilder();
        for( String part : pathList ){
        	if( root ){
        		result.append( File.separatorChar );
        	}
        	else{
        		root = true;
        	}
            result.append( part );
        }
        return result.toString();
    }
    
    private void delete( File file ){
        File[] children = file.listFiles();
        if( children != null ){
            for( File child : children ){
                delete( child );
            }
        }
        file.delete();
    }
}
