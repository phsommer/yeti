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
package tinyos.yeti.ep.storage;

import java.io.DataOutputStream;
import java.io.IOException;

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;

public class DeclarationKindFactory implements IGenericFactory<IDeclaration.Kind>{
    /** all kind of declarations */
    private static final IDeclaration.Kind[] DECLARATION_KIND = IDeclaration.Kind.values();
    
    public Kind create(){
        return null;
    }

    public Kind read( Kind value, IStorage storage ) throws IOException{
        return DECLARATION_KIND[ storage.in().readInt() ];
    }

    public void write( Kind value, IStorage storage ) throws IOException{
        writeKind( value, storage );
    }
    
    public static void writeArray( Kind[] kind, IStorage storage ) throws IOException{
        int size = kind == null ? -1 : kind.length;
        DataOutputStream out = storage.out();
        out.writeInt( size );
        for( int i = 0; i < size; i++ ){
            writeKind( kind[i], storage );
        }
    }
    
    public static void writeKind( Kind kind, IStorage storage ) throws IOException{
        int index = -1;
        for( int i = 0; i < DECLARATION_KIND.length; i++ ){
            if( DECLARATION_KIND[i] == kind ){
                index = i;
                break;
            }
        }
        
        storage.out().writeInt( index );
    }
    
    public static Kind[] readArray( IStorage storage ) throws IOException{
        int size = storage.in().readInt();
        if( size == -1 )
            return null;
        
        Kind[] result = new Kind[ size ];
        for( int i = 0; i < size; i++ ){
            result[i] = readKind( storage );
        }
        
        return result;
    }
    
    public static Kind readKind( IStorage storage) throws IOException{
        int index = storage.in().readInt();
        if( index == -1 )
            return null;
        
        return DECLARATION_KIND[index];
    }
}
