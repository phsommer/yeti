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
package tinyos.yeti.model;

import java.io.File;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;

/**
 * Abstract implementation of {@link IParseFile}, should not be accessed
 * by clients.
 * @author Benjamin Sigg
 */
public abstract class ParseFile implements IParseFile{
    private File file;
    private int index;
    private ProjectTOS project;
    
    public ParseFile( File file, ProjectTOS project ){
        if( file == null )
            throw new NullPointerException( "filename must not be null" );
        this.file = file;
        this.project = project;
    }
    
    @SuppressWarnings("unchecked")
	public Object getAdapter( Class adapter ){
    	if( adapter.equals( IParseFile.class ))
    		return this;
    	
    	return null;
    }
    
    public ProjectTOS getProject(){
	    return project;
    }
    
    public File toFile(){
        return file;
    }
    
    public String getName(){
        return file.getName();
    }
    
    public String getPath(){
        return file.getAbsolutePath();
    }
    
    public void setIndex( int index ){
        this.index = index;
    }
    
    public int getIndex(){
        return index;
    }
    
    @Override
    public String toString(){
        return getPath();
    }
    
    @Override
    public boolean equals( Object obj ){
        if( obj == this )
            return true;
        
        if( obj == null )
            return false;
        
        if( !(obj instanceof ParseFile))
            return false;
        
        return file.equals( ((ParseFile)obj).file );
    }
    @Override
    public int hashCode(){
        return file.hashCode();
    }
}
