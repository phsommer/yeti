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
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.values.ValueFactory;

public class EnumConstantDeclaration extends BaseDeclaration{
    public static final IGenericFactory<EnumConstantDeclaration> FACTORY = 
        new ReferenceFactory<EnumConstantDeclaration>( BaseDeclaration.FACTORY ){
      
        public EnumConstantDeclaration create(){
            return new EnumConstantDeclaration();
        }

        @Override
        public void write( EnumConstantDeclaration value, IStorage storage ) throws IOException{
            super.write( value, storage );
            ValueFactory.write( value.constant, storage );
        }
        
        @Override
        public EnumConstantDeclaration read( EnumConstantDeclaration value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.constant = ValueFactory.read( storage );
            return value;
        }
        
    };
    
    private Value constant;
    
    private EnumConstantDeclaration(){
        // nothing
    }
    
    public EnumConstantDeclaration( String name, IParseFile file, ASTModelPath path, Value constant ){
        super( Kind.ENUMERATION_CONSTANT, name, name, file, path, TagSet.get( NesC12ASTModel.FIELD ) );
        this.constant = constant;
    }

    public Value getConstant(){
        return constant;
    }
}
