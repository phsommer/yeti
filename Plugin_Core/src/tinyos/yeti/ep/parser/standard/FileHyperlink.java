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

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileHyperlink;
import tinyos.yeti.ep.parser.IFileRegion;

/**
 * Standard implementation of {@link IFileHyperlink}.
 * @author Benjamin Sigg
 */
public class FileHyperlink implements IFileHyperlink{
    private String name;
    private String type;
    private IFileRegion source;
    private IFileRegion target;
    private IParseFile file;

    public FileHyperlink( IFileRegion source, IParseFile target ){
        this.source = source;
        this.file = target;
    }
    
    public FileHyperlink( IFileRegion source, IFileRegion target ){
        this.source = source;
        this.target = target;
        this.file = target.getParseFile();
    }
    
    public String getHyperlinkName(){
        return name;
    }
    
    public void setHyperlinkName( String name ){
        this.name = name;
    }

    public String getHyperlinkType(){
        return type;
    }
    
    public void setHyperlinkType( String type ){
        this.type = type;
    }

    public IFileRegion getSourceRegion(){
        return source;
    }
    
    public void setSourceRegion( IFileRegion source ){
        this.source = source;
    }

    public IFileRegion getTargetRegion(){
        return target;
    }
    
    public void setTargetRegion( IFileRegion target ){
        this.target = target;
    }
    
    public IParseFile getParseFile(){
        return file;
    }
    
    public void setParserFile( IParseFile file ){
        this.file = file;
    }
}
