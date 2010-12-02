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
package tinyos.yeti.nesc12.parser;

import java.io.IOException;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.preprocessor.FileInfo;

public class NesC12FileInfo implements FileInfo{
    public static final IGenericFactory<NesC12FileInfo> FACTORY = new IGenericFactory<NesC12FileInfo>(){
        public NesC12FileInfo create(){
            return new NesC12FileInfo();
        }
        public void write( NesC12FileInfo value, IStorage storage ) throws IOException{
            storage.writeString( value.filename );
            storage.write( value.file );
        }
        public NesC12FileInfo read( NesC12FileInfo value, IStorage storage ) throws IOException{
            value.filename = storage.readString();
            value.file = storage.read();
            return value;
        }
    };
    
    private IParseFile file;
    private String filename;

    private NesC12FileInfo(){
        // nothing
    }
    
    public NesC12FileInfo( IParseFile file ){
        this( file, null );
    }

    public NesC12FileInfo( IParseFile file, String filename ){
        if( file == null )
            throw new IllegalArgumentException( "file must not be null" );

        this.file = file;
        if( filename == null )
            filename = file.getPath();

        this.filename = filename;
    }

    @Override
    public String toString() {
    	if( filename == null )
    		return file.getPath();
    	else
    		return filename;
    }
    
    public IParseFile getParseFile(){
        return file;
    }

    public String getFileName(){
        return filename;
    }

    public String getPath(){
        return file.getPath();
    }

    public String getName(){
        return file.getName();
    }
}
