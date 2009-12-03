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
package tinyos.yeti.ep.parser.standard;

import org.eclipse.jface.text.Position;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileRegion;

/**
 * A simple implementation of {@link IFileRegion}.
 * @author Benjamin Sigg
 */
public class FileRegion implements IFileRegion{
    private int offset;
    private int length;
    private int line;
    private IParseFile file;
    
    public FileRegion(){
	// do nothing
    }
    
    public FileRegion( int offset, int length, int line, IParseFile file ){
	this.offset = offset;
	this.length = length;
	this.line = line;
	this.file = file;
    }
    
    public FileRegion( Position position, int line, IParseFile file ){
	this( position.getOffset(), position.getLength(), line, file );
    }
    
    public int getOffset(){
	return offset;
    }
    
    public void setOffset( int offset ){
	this.offset = offset;
    }
    
    public int getLength(){
        return length;
    }
    
    public void setLength( int length ){
	this.length = length;
    }
    
    public IParseFile getParseFile(){
        return file;
    }
    
    public void setParseFile( IParseFile file ){
	this.file = file;
    }
    
    public void setLine( int line ){
        this.line = line;
    }
    
    public int getLine(){
        return line;
    }
}
