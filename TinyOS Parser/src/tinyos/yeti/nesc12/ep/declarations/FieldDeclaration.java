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
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleField;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;

/**
 * A declaration for a field or a function.
 * @author Benjamin Sigg
 */
public class FieldDeclaration extends TypedDeclaration{
    public static final IGenericFactory<FieldDeclaration> FACTORY = new ReferenceFactory<FieldDeclaration>( TypedDeclaration.FACTORY ){
        public FieldDeclaration create(){
            return new FieldDeclaration();
        }
        
        @Override
        public void write( FieldDeclaration value, IStorage storage ) throws IOException{
            super.write( value, storage );
            storage.write( value.modifiers );
            storage.write( value.name );
            storage.write( value.value );
            storage.write( value.attributes );
        }
        
        @Override
        public FieldDeclaration read( FieldDeclaration value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.modifiers = storage.read();
            value.name = storage.read();
            value.value = storage.read();
            value.attributes = storage.read();
            return value;
        }
    };
    
    private Modifiers modifiers;
    private Name name;
    private Value value;
    private ModelAttribute[] attributes;
    
    private SimpleField field;
    
    protected FieldDeclaration(){
        
    }

    public FieldDeclaration( Kind kind, Modifiers modifiers, Type type, Name name, ModelAttribute[] attributes, Value value,
            IParseFile file, ASTModelPath path, TagSet tags ){
        
        super( kind, type, name.toIdentifier(), type == null ? name.toIdentifier() : type.toLabel( name.toIdentifier(), Type.Label.EXTENDED ), file, path, tags );
        
        this.modifiers = modifiers;
        this.name = name;
        this.value = value;
        this.attributes = attributes;
        
        name.resolveRange();
        
        if( value != null )
            type.resolveNameRanges();
        
        if( value != null )
            value.resolveNameRanges();
    }
    
    public SimpleField toField(){
        if( field == null ){
            field = new SimpleField( modifiers, getType(), name, attributes, value, null, getPath() );
        }
        return field;
    }
}
