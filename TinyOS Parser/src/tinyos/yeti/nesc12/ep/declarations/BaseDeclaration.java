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
package tinyos.yeti.nesc12.ep.declarations;

import java.io.IOException;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.DeclarationKindFactory;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;

public class BaseDeclaration implements IDeclaration{
    public static final IGenericFactory<BaseDeclaration> FACTORY = new IGenericFactory<BaseDeclaration>(){
        public BaseDeclaration create(){
            return new BaseDeclaration();
        }
        
        public void write( BaseDeclaration value, IStorage storage ) throws IOException{
            storage.writeString( value.name );
            storage.writeString( value.label );
            DeclarationKindFactory.writeKind( value.kind, storage );
            storage.write( value.parseFile );
            storage.write( value.fileRegion );
            storage.write( value.path );
            storage.write( value.tags );   
        }
        
        public BaseDeclaration read( BaseDeclaration value, IStorage storage ) throws IOException{
            value.name = storage.readString();
            value.label = storage.readString();
            value.kind = DeclarationKindFactory.readKind( storage );
            value.parseFile = storage.read();
            value.fileRegion = storage.read();
            value.path = storage.read();
            value.tags = storage.read();
            
            return value;
        }
    };
    
    private String name;
    private String label;
    
    private IParseFile parseFile;
    private FileRegion fileRegion;
    
    private Kind kind;
    private ASTModelPath path;
    private TagSet tags;
    
    protected BaseDeclaration(){
        // nothing
    }
    
    public BaseDeclaration( Kind kind, String name, String label, IParseFile file, ASTModelPath path, TagSet tags ){
        setKind( kind );
        setName( name );
        setLabel( label );
        setParseFile( file );
        setPath( path );
        setTags( tags );
    }
    
    public void resolveRanges(){
        // nothing to do
    }
    
    public Kind getKind(){
        return kind;
    }

    public void setKind( Kind kind ){
        this.kind = kind;
    }
    
    public String getLabel(){
        return label;
    }

    public void setLabel( String label ){
        this.label = label;
    }
    
    public String getName(){
        return name;
    }

    public void setName( String name ){
        this.name = name;
    }
    
    public Name createName(){
		FileRegion region = getFileRegion();
		LazyRangeDescription range = null;
		if( region != null )
			range = new LazyRangeDescription( region );
		
		return new SimpleName( range, getName() );
    }
    
    public IParseFile getPresetParseFile(){
        return parseFile;
    }
    
    public IParseFile getParseFile(){
        if( parseFile != null )
            return parseFile;
        
        if( fileRegion != null )
            return fileRegion.getParseFile();
        
        return null;
    }

    public void setParseFile( IParseFile parseFile ){
        this.parseFile = parseFile;
    }
    
    public FileRegion getFileRegion(){
        return fileRegion;
    }
    
    public void setFileRegion( FileRegion file ){
        fileRegion = file;
    }
    
    public ASTModelPath getPath(){
        return path;
    }

    public void setPath( ASTModelPath path ){
        this.path = path;
    }
    
    public TagSet getTags(){
        return tags;
    }
    
    public void setTags( TagSet tags ){
        this.tags = tags;
    }
    
    @Override
    public String toString(){
        return "Declaration[name=" + name + ",tags=" + tags + "]";
    }
}
