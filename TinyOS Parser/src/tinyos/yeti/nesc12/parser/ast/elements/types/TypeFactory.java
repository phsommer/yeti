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
package tinyos.yeti.nesc12.parser.ast.elements.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.FieldUtility;
import tinyos.yeti.nesc12.parser.ast.elements.Type;

/**
 * A class that can write and read {@link Type}s
 * @author Benjamin Sigg
 */
public final class TypeFactory{
    @SuppressWarnings("unchecked")
    private static TypeGenericFactory FACTORY = new TypeGenericFactory();
    
    @SuppressWarnings("unchecked")
    public static <T extends Type> IGenericFactory<T> factory(){
        return FACTORY;
    }
    
    private static final char ID_NULL           = '0';
    private static final char ID_ARRAY          = 'a';
    private static final char ID_BASE           = 'b';
    private static final char ID_CONST          = 'c';
    private static final char ID_DATA_OBJECT    = 'd';
    private static final char ID_ENUM           = 'e';
    private static final char ID_FUNCTION       = 'f';
    private static final char ID_GENERIC        = 'g';
    private static final char ID_POINTER        = 'p';
    private static final char ID_TYPEDEF        = 't';

    private TypeFactory(){
        // ignore
    }

    public static void writeTypeArray( Type[] type, IStorage storage ) throws IOException{
        if( type == null ){
            storage.out().writeInt( -1 );
        }
        else{
            storage.out().writeInt( type.length );
            for( Type next : type ){
                write( next, storage );
            }
        }
    }
    
    public static Type[] readTypeArray( IStorage storage ) throws IOException{
        int size = storage.in().readInt();
        if( size == -1 )
            return null;
        
        Type[] result = new Type[ size ];
        for( int i = 0; i < size; i++ ){
            result[i] = read( storage );
        }
        
        return result;
    }
    
    public static void write( Type type, IStorage storage ) throws IOException{
        WriteTypeList list = new WriteTypeList();
        list.write( type, storage );
    }

    private static void write( Type type, IStorage storage, WriteTypeList list ) throws IOException{
        DataOutputStream out = storage.out();

        if( type == null ){
            out.writeChar( ID_NULL );
        }
        else if( type instanceof ArrayType ){
            out.writeChar( ID_ARRAY );
            writeArray( (ArrayType)type, storage, list );
        }
        else if( type instanceof BaseType ){
            out.writeChar( ID_BASE );
            writeBase( (BaseType)type, storage, list );
        }
        else if( type instanceof ConstType ){
            out.writeChar( ID_CONST );
            writeConst( (ConstType)type, storage, list );
        }
        else if( type instanceof DataObjectType ){
            out.writeChar( ID_DATA_OBJECT );
            writeDataObject( (DataObjectType)type, storage, list );
        }
        else if( type instanceof EnumType ){
            out.writeChar( ID_ENUM );
            writeEnum( (EnumType)type, storage, list );
        }
        else if( type instanceof FunctionType ){
            out.writeChar( ID_FUNCTION );
            writeFunction( (FunctionType)type, storage, list );
        }
        else if( type instanceof GenericType ){
            out.writeChar( ID_GENERIC );
            writeGeneric( (GenericType)type, storage, list );
        }
        else if( type instanceof PointerType ){
            out.writeChar( ID_POINTER );
            writePointer( (PointerType)type, storage, list );
        }
        else if( type instanceof TypedefType ){
            out.writeChar( ID_TYPEDEF );
            writeTypedef( (TypedefType)type, storage, list );
        }
    }

    public static Type read( IStorage storage ) throws IOException{
        ReadTypeList list = new ReadTypeList();
        return list.read( storage );
    }

    private static Type read( IStorage storage, ReadTypeList list ) throws IOException{
        char kind = storage.in().readChar();

        switch( kind ){
            case ID_NULL: return null;
            case ID_ARRAY: return readArray( storage, list );
            case ID_BASE: return readBase( storage, list );
            case ID_CONST: return readConst( storage, list );
            case ID_DATA_OBJECT: return readDataObject( storage, list );
            case ID_ENUM: return readEnum( storage, list );
            case ID_FUNCTION: return readFunction( storage, list );
            case ID_GENERIC: return readGeneric( storage, list );
            case ID_POINTER: return readPointer( storage, list );
            case ID_TYPEDEF: return readTypedef( storage, list );
            default: throw new IOException( "unknown kind of type: " + kind );
        }
    }

    // Array
    private static void writeArray( ArrayType type, IStorage storage, WriteTypeList list ) throws IOException{
        DataOutputStream out = storage.out();
        ArrayType.Size size = type.getSize();
        if( size == null ){
            out.writeInt( 0 );
        }
        else{
            switch( size ){
                case INCOMPLETE:
                    out.writeInt( 1 );
                    break;
                case VARIABLE:
                    out.writeInt( 2 );
                    break;
                case SPECIFIED:
                    out.writeInt( 3 );
                    out.writeInt( type.getLength() );
                    break;
                case SPECIFIED_UNKNOWN:
                	out.writeInt( 4 );
                	break;
            }
        }

        list.write( type.getRawType(), storage );
    }
    private static ArrayType readArray( IStorage storage, ReadTypeList list ) throws IOException{
        int sizeKind = storage.in().readInt();
        ArrayType.Size size;
        int length = 0;

        switch( sizeKind ){
            case 0:
                size = null;
                break;
            case 1:
                size = ArrayType.Size.INCOMPLETE;
                break;
            case 2:
                size = ArrayType.Size.VARIABLE;
                break;
            case 3:
                size = ArrayType.Size.SPECIFIED;
                length = storage.in().readInt();
                break;
            case 4:
            	size = ArrayType.Size.SPECIFIED_UNKNOWN;
            	break;
            default:
                throw new IOException( "unknown kind of size: " + sizeKind );
        }

        Type raw = list.read( storage );
        return new ArrayType( raw, size, length );
    }

    // Const
    private static void writeConst( ConstType type, IStorage storage, WriteTypeList list ) throws IOException{
        list.write( type.getRawType(), storage );
    }
    private static ConstType readConst( IStorage storage, ReadTypeList list ) throws IOException{
        return new ConstType( list.read( storage ) );
    }

    // DataObject
    private static void writeDataObject( DataObjectType type, IStorage storage, WriteTypeList list ) throws IOException{
        DataOutputStream out = storage.out();

        DataObjectType.Kind kind = type.getKind();
        if( kind == null ){
            out.writeInt( -1 );
        }
        else{
            switch( kind ){
                case ATTRIBUTE:
                    out.writeInt( 0 );
                    break;
                case NX_STRUCT:
                    out.writeInt( 1 );
                    break;
                case NX_UNION:
                    out.writeInt( 2 );
                    break;
                case STRUCT:
                    out.writeInt( 3 );
                    break;
                case UNION:
                    out.writeInt( 4 );
                    break;
            }
        }

        String name = type.getName();
        if( name == null ){
            out.writeBoolean( false );
        }
        else{
            out.writeBoolean( true );
            out.writeUTF( name );
        }

        out.writeInt( type.getFieldCount() );
        for( int i = 0, n = type.getFieldCount(); i<n; i++ ){
            FieldUtility.write( type.getField( i ), storage );
        }
    }

    private static DataObjectType readDataObject( IStorage storage, ReadTypeList list ) throws IOException{
        DataInputStream in = storage.in();
        int dataKind = in.readInt();
        DataObjectType.Kind kind;
        switch( dataKind ){
            case 0:
                kind = DataObjectType.Kind.ATTRIBUTE;
                break;
            case 1:
                kind = DataObjectType.Kind.NX_STRUCT;
                break;
            case 2:
                kind = DataObjectType.Kind.NX_UNION;
                break;
            case 3:
                kind = DataObjectType.Kind.STRUCT;
                break;
            case 4:
                kind = DataObjectType.Kind.UNION;
                break;
            default: 
                kind = null;
            break;
        }

        String name;
        if( in.readBoolean() ){
            name = in.readUTF();
        }
        else{
            name = null;
        }
        
        int size = in.readInt();
        Field[] fields = new Field[ size ];

        for( int i = 0; i < size; i++ ){
            fields[i] = FieldUtility.read( storage );
        }

        return new DataObjectType( kind, name, fields );
    }

    // Enum
    private static void writeEnum( EnumType type, IStorage storage, WriteTypeList list ) throws IOException{
        DataOutputStream out = storage.out();
        
        String name = type.getName();
        if( name == null ){
            out.writeBoolean( false );
        }
        else{
            out.writeBoolean( true );
            out.writeUTF( name );
        }
        
        if( type.isIncomplete() ){
            out.writeInt( -1 );
        }
        else{
            String[] constants = type.getConstants();
            out.writeInt( constants.length );
            for( int i = 0, n = constants.length; i<n; i++ ){
                out.writeUTF( constants[i] );
            }
        }
    }
    
    private static EnumType readEnum( IStorage storage, ReadTypeList list ) throws IOException{
        DataInputStream in = storage.in();
        String name;
        if( in.readBoolean() ){
            name = in.readUTF();
        }
        else{
            name = null;
        }
        
        int size = in.readInt();
        if( size < 0 ){
            return new EnumType( name, null );
        }
        else{
            String[] constants = new String[ size ];
            for( int i = 0; i < size; i++ ){
                constants[i] = in.readUTF();
            }
            return new EnumType( name, constants );
        }
    }

    // Function
    private static void writeFunction( FunctionType type, IStorage storage, WriteTypeList list ) throws IOException{
        list.write( type.getResult(), storage );

        int count = type.getArgumentCount();
        storage.out().writeInt( count );
        for( int i = 0; i < count; i++ ){
            list.write( type.getArgument( i ), storage );
        }

        storage.out().writeBoolean( type.isVararg() );
    }
    private static FunctionType readFunction( IStorage storage, ReadTypeList list ) throws IOException{
        DataInputStream in = storage.in();
        Type result = list.read( storage );

        int count = in.readInt();
        Type[] arguments = new Type[ count ];
        for( int i = 0; i < count; i++ ){
            arguments[i] = list.read( storage );
        }

        boolean vararg = in.readBoolean();
        return new FunctionType( result, arguments, vararg );
    }

    // Generic
    private static void writeGeneric( GenericType type, IStorage storage, WriteTypeList list ) throws IOException{
        storage.out().writeUTF( type.getName() );
    }
    private static GenericType readGeneric( IStorage storage, ReadTypeList list ) throws IOException{
        return new GenericType( storage.in().readUTF() );
    }

    // Pointer
    private static void writePointer( PointerType type, IStorage storage, WriteTypeList list ) throws IOException{
        list.write( type.getRawType(), storage );
    }
    private static PointerType readPointer( IStorage storage, ReadTypeList list ) throws IOException{
        return new PointerType( list.read( storage ));
    }

    //Base
    private static void writeBase( BaseType type, IStorage storage, WriteTypeList list ) throws IOException{
        DataOutputStream out = storage.out();
        switch( type ){
            case BOOL: out.writeInt( 0 ); break;
            case DOUBLE: out.writeInt( 1 ); break;
            case DOUBLE_COMPLEX: out.writeInt( 2 ); break;
            case FLOAT: out.writeInt( 3 ); break;
            case FLOAT_COMPLEX: out.writeInt( 4 ); break;
            case LONG_DOUBLE: out.writeInt( 5 ); break;
            case LONG_DOUBLE_COMPLEX: out.writeInt( 6 ); break;
            case S_CHAR: out.writeInt( 7 ); break;
            case S_INT: out.writeInt( 8 ); break;
            case S_LONG: out.writeInt( 9 ); break;
            case S_LONG_LONG: out.writeInt( 10 ); break;
            case S_SHORT: out.writeInt( 11 ); break;
            case U_CHAR: out.writeInt( 12 ); break;
            case U_INT: out.writeInt( 13 ); break;
            case U_LONG: out.writeInt( 14 ); break;
            case U_LONG_LONG: out.writeInt( 15 ); break;
            case U_SHORT: out.writeInt( 16 ); break;
            case VOID: out.writeInt( 17 ); break;
        }
    }
    private static BaseType readBase( IStorage storage, ReadTypeList list ) throws IOException{
        int kind = storage.in().readInt();
        switch( kind ){
            case 0: return BaseType.BOOL;
            case 1: return BaseType.DOUBLE;
            case 2: return BaseType.DOUBLE_COMPLEX;
            case 3: return BaseType.FLOAT;
            case 4: return BaseType.FLOAT_COMPLEX;
            case 5: return BaseType.LONG_DOUBLE;
            case 6: return BaseType.LONG_DOUBLE_COMPLEX;
            case 7: return BaseType.S_CHAR;
            case 8: return BaseType.S_INT;
            case 9: return BaseType.S_LONG;
            case 10: return BaseType.S_LONG_LONG;
            case 11: return BaseType.S_SHORT;
            case 12: return BaseType.U_CHAR;
            case 13: return BaseType.U_INT;
            case 14: return BaseType.U_LONG;
            case 15: return BaseType.U_LONG_LONG;
            case 16: return BaseType.U_SHORT;
            case 17: return BaseType.VOID;
            default: throw new IOException( "unknown base type: " + kind );
        }
    }

    // Typedef
    private static void writeTypedef( TypedefType type, IStorage storage, WriteTypeList list ) throws IOException{
        storage.out().writeUTF( type.getName() );
        list.write( type.getBase(), storage );
    }
    private static TypedefType readTypedef( IStorage storage, ReadTypeList list ) throws IOException{
        String name = storage.in().readUTF();
        Type base = list.read( storage );
        return new TypedefType( name, base );
    }

    private static class WriteTypeList{
        private Map<Type, Integer> map = new HashMap<Type, Integer>();

        public void write( Type type, IStorage storage ) throws IOException{
            DataOutputStream out = storage.out();

            if( type instanceof DataObjectType ){
                out.writeBoolean( true );

                Integer value = map.get( type );
                if( value == null ){
                    int index = map.size();
                    map.put( type, index );
                    out.writeBoolean( false );
                    out.writeInt( index );
                    TypeFactory.write( type, storage, this );
                }
                else{
                    out.writeBoolean( true );
                    out.writeInt( value );
                }
            }
            else{
                out.writeBoolean( false );
                TypeFactory.write( type, storage, this );
            }
        }
    }

    private static class ReadTypeList{
        private Map<Integer, Type> map = new HashMap<Integer, Type>();

        public Type read( IStorage storage ) throws IOException{
            DataInputStream in = storage.in();
            boolean mapped = in.readBoolean();
            if( mapped ){
                boolean reference = in.readBoolean();
                int index = in.readInt();

                if( reference ){
                    return map.get( Integer.valueOf( index ) );
                }
                else{
                    Type type = TypeFactory.read( storage, this );
                    if( type != null )
                        map.put( Integer.valueOf( index ), type );
                    return type;
                }
            }
            else{
                return TypeFactory.read( storage, this );
            }
        }
    }
    
    private static class TypeGenericFactory<T extends Type> implements IGenericFactory<T>{
        public T create(){
            return null;
        }
        
        public void write( T value, IStorage storage ) throws IOException{
            TypeFactory.write( value, storage );
        }
        
        @SuppressWarnings("unchecked")
        public T read( T value, IStorage storage ) throws IOException{
            return (T)TypeFactory.read( storage ); 
        }
    };
}






















