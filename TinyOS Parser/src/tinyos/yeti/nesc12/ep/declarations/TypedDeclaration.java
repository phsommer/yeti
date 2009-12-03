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
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeFactory;

public class TypedDeclaration extends BaseDeclaration{
    public static final IGenericFactory<TypedDeclaration> FACTORY =
        new ReferenceFactory<TypedDeclaration>( BaseDeclaration.FACTORY ){
      
        public TypedDeclaration create(){
            return new TypedDeclaration();
        }
        
        @Override
        public void write( TypedDeclaration value, IStorage storage ) throws IOException{
            super.write( value, storage );
            TypeFactory.write( value.type, storage );
        }
        
        @Override
        public TypedDeclaration read( TypedDeclaration value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.type = TypeFactory.read( storage );
            return value;
        }
    };
    
    private Type type;
    
    protected TypedDeclaration(){
        // nothing
    }
    
    public TypedDeclaration( Kind kind, Type type, String name, String label, IParseFile file, ASTModelPath path, TagSet tags ){
        super( kind, name, label, file, path, tags );
        setType( type );
    }
    
    @Override
    public void resolveRanges(){
        super.resolveRanges();
        if( type != null )
            type.resolveNameRanges();
    }
    
    public Type getType(){
        return type;
    }
    
    public void setType( Type type ){
        this.type = type;
    }
    
    @Override
    public String toString(){
        if( type == null )
            return super.toString();
        
        return "Declaration[type=" + type.toLabel( getName(), Type.Label.EXTENDED ) + ",tags=" + getTags() + "]";
    }
}
