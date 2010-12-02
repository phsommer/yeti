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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.preprocessor.RangeDescription;

public class FileRegion implements IFileRegion{
    public static final IGenericFactory<FileRegion> FACTORY = new IGenericFactory<FileRegion>(){
        public FileRegion create(){
            return new FileRegion();
        }
        
        public void write( FileRegion value, IStorage storage ) throws IOException{
            DataOutputStream out = storage.out();
            
            storage.write( value.file );
            out.writeInt( value.offset );
            out.writeInt( value.length );
            out.writeInt( value.line );    
        }
        
        public FileRegion read( FileRegion value, IStorage storage ) throws IOException{
            DataInputStream in = storage.in();
            value.file = storage.read();
            value.offset = in.readInt();
            value.length = in.readInt();
            value.line = in.readInt();
            return value;
        }
    };
    
    private IParseFile file;
    private int offset;
    private int length;
    private int line;
    
    private FileRegion(){
        // nothing
    }
    
    public FileRegion( RangeDescription.Range range ){
        this( (NesC12FileInfo)range.file(), range.left(), range.right()-range.left(), range.line() );
    }
    
    public FileRegion( NesC12FileInfo file, int offset, int length, int line ){
        this( file == null ? null : file.getParseFile(), offset, length, line );
    }
    
    public FileRegion( IParseFile file, int offset, int length, int line ){
        this.file = file;
        this.offset = offset;
        this.length = length;
        this.line = line;
    }
    
    public IParseFile getParseFile() {
        return file;
    }
    
    public void setParseFile( IParseFile file ) {
        this.file = file;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset( int offset ) {
        this.offset = offset;
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength( int length ) {
        this.length = length;
    }
    
    public int getLine(){
        return line;
    }
    
    public void setLine( int line ){
        this.line = line;
    }
}
