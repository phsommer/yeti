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
package tinyos.yeti.ep;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IContainer;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

/**
 * The parse file that represents the null value.
 * @author Benjamin Sigg
 *
 */
public final class NullParseFile implements IParseFile{
    public static final NullParseFile NULL = new NullParseFile();
    
    public static final IGenericFactory<NullParseFile> FACTORY = new IGenericFactory<NullParseFile>(){
        public NullParseFile create(){
            return NullParseFile.NULL;
        }
        
        public void write( NullParseFile value, IStorage storage ) throws IOException{
            // nothing
        }
        
        public NullParseFile read( NullParseFile value, IStorage storage )throws IOException{
            return value;
        }
    };
    
    private NullParseFile(){
        // nothing
    }
    
    @SuppressWarnings("unchecked")
	public Object getAdapter( Class adapter ){
    	if( adapter.equals( IParseFile.class ))
    		return this;
    	
    	return null;
    }
    
    public int getIndex(){
        return Integer.MAX_VALUE;
    }

    public String getName(){
        return "?";
    }

    public String getPath(){
        return "?";
    }

    public File toFile(){
    	return null;
    }
    
    public boolean isProjectFile(){
        return false;
    }
    
    public IContainer getProjectSourceContainer(){
    	return null;
    }

    public ProjectTOS getProject(){
    	return null;
    }
}
