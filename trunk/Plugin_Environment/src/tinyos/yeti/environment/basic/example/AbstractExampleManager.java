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
package tinyos.yeti.environment.basic.example;

import java.io.File;
import java.io.FilenameFilter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IEnvironmentListener;

/**
 * An implementation of {@link IExampleManager} that lazily reads the files
 * necessary for examples.
 * @author Benjamin Sigg
 */
public abstract class AbstractExampleManager implements IExampleManager{
    private IExample[] examples;
    private boolean initialized = false;
    
    public IExample[] getExamples(){
    	if( !initialized ){
    		initialized = true;
    		getEnvironment().addEnvironmentListener( new IEnvironmentListener(){
    			public void reinitialized( IEnvironment environment ){
	    			examples = null;	
    			}
    		});
    	}
    	
        if( examples == null ){
        	File directory = getExampleDirectory();
        	if( directory == null ){
        		return new IExample[]{};
        	}
        	List<IExample> list = new ArrayList<IExample>();
        	recursiveCollectExamples( true, directory, list );
        	examples = list.toArray( new IExample[ list.size() ] );
        	
        }

        return examples;
    }
    
    protected abstract IEnvironment getEnvironment();

    private void recursiveCollectExamples( boolean toplevel, File directory, List<IExample> examples ){
    	if( !exclude( directory )){
    		if( !toplevel && isExample( directory )){
    			examples.add( new Example( directory ) );
    		}
    		else{
    			File[] children = directory.listFiles();
    			if( children != null ){
    				Arrays.sort( children, new Comparator<File>(){
    					private Collator collator = Collator.getInstance();
    					
    					public int compare( File o1, File o2 ){
    						return collator.compare( o1.getName(), o2.getName() );
    					}
    				});
    				
    				for( File child : children ){
    					recursiveCollectExamples( false, child, examples );
    				}
    			}
    		}
    	}
    }
    
    /**
     * Gets the basic example directory, the directory containing all the examples.
     * @return the example directory
     */
    protected abstract File getExampleDirectory();
    
    /**
     * Tells whether <code>directory</code> is an example
     * @param directory some directory
     * @return <code>true</code> if it is an example
     */
    protected boolean isExample( File directory ){
    	File[] makefiles = directory.listFiles( new FilenameFilter(){
			public boolean accept( File dir, String name ){
				return "Makefile".equals( name );
			}
    	});
    	return makefiles != null && makefiles.length > 0;
    }
    
    protected boolean exclude( File directory ){
    	String name = directory.getName();
    	if( name.startsWith( "." ))
    		return true;
    	if( name.equalsIgnoreCase( "cvs" ))
    		return true;
    	
    	return false;
    }

    protected String getExampleName( File directory ){
    	File base = getExampleDirectory();
    	LinkedList<String> path = new LinkedList<String>();
    	while( !directory.equals( base )){
    		path.addFirst( directory.getName() );
    		directory = directory.getParentFile();
    	}
    	
    	StringBuilder builder = new StringBuilder();
    	for( String element : path ){
    		if( builder.length() > 0 )
    			builder.append( "/" );
    		builder.append( element );
    	}
    	
    	return builder.toString();
    }
    
    private class Example implements IExample{
        private String name;
        private File directory;

        public Example( File directory ){
            this.directory = directory;
        }

        public String getName(){
        	if( name == null )
        		name = getExampleName( directory );
        	
            return name;
        }

        public File getDirectory(){
        	return directory;
        }        
    }
}
