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
package tinyos.yeti.nesc12.parser.ast.elements.values;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeFactory;

/**
 * A factory that can read or write {@link Value}s.
 * @author Benjamin Sigg
 */
public final class ValueFactory{
    @SuppressWarnings("unchecked")
    private static ValueGenericFactory FACTORY = new ValueGenericFactory();
    
    @SuppressWarnings("unchecked")
    public static <T extends Value> IGenericFactory<T> factory(){
        return FACTORY;
    }
    
    private static final int ID_NULL     = 0;
    private static final int ID_ARRAY    = 1;
    private static final int ID_OBJECT   = 2;
    private static final int ID_FLOATING = 3;
    private static final int ID_INTEGER  = 4;
    private static final int ID_STRING   = 5;
    private static final int ID_UNKNOWN  = 6;
    
    private ValueFactory(){
        // do nothing
    }

    /**
     * Writes <code>value</code> into <code>out</code>.
     * @param value the value to write, can be <code>null</code>
     * @param storage the stream to write into
     * @throws IOException forwarded from <code>out</code>
     */
    public static void write( Value value, IStorage storage ) throws IOException{
    	DataOutputStream out = storage.out();
    	
        if( value == null ){
            out.writeInt( ID_NULL );
        }
        else if( value instanceof ArrayValue ){
            out.writeInt( ID_ARRAY );
            writeArray( (ArrayValue)value, storage );
        }
        else if( value instanceof DataObject ){
            out.writeInt( ID_OBJECT );
            writeDataObject( (DataObject)value, storage );
        }
        else if( value instanceof FloatingValue ){
            out.writeInt( ID_FLOATING );
            writeFloating( (FloatingValue)value, storage );
        }
        else if( value instanceof IntegerValue ){
            out.writeInt( ID_INTEGER );
            writeInteger( (IntegerValue)value, storage );
        }
        else if( value instanceof StringValue ){
            out.writeInt( ID_STRING );
            writeString( (StringValue)value, storage );
        }
        else if( value instanceof UnknownValue ){
        	out.writeInt( ID_UNKNOWN );
        	writeUnknown( (UnknownValue)value, storage );
        }
    }

    /**
     * Reads a value that was written earlier.
     * @param storage the stream to read from
     * @return the value that was written, might be <code>null</code> if 
     * {@link #write(Value, IStorage) write} was called with <code>null</code>
     * @throws IOException if the stream can't be accessed or if the stream delivers
     * illegal bytes
     */
    public static Value read( IStorage storage ) throws IOException{
        int kind = storage.in().readInt();
        switch( kind ){
            case ID_NULL: return null;
            case ID_ARRAY: return readArray( storage );
            case ID_OBJECT: return readDataObject( storage );
            case ID_FLOATING: return readFloating( storage );
            case ID_INTEGER: return readInteger( storage );
            case ID_STRING: return readString( storage );
            case ID_UNKNOWN: return readUnknown( storage );
            default: throw new IOException( "unknown kind of value: " + kind );
        }
    }
    
    public static void writeArray( ArrayValue value, IStorage storage ) throws IOException{
        TypeFactory.write( value.getType(), storage );
        int size = value.getLength();
        storage.out().writeInt( size );
        for( int i = 0; i < size; i++ ){
            write( value.getValue( i ), storage );
        }
    }
    
    public static ArrayValue readArray( IStorage storage ) throws IOException{
        Type type = TypeFactory.read( storage );
        int size = storage.in().readInt();
        Value[] values = new Value[ size ];
        for( int i = 0; i < size; i++ )
            values[i] = read( storage );
        
        return new ArrayValue( type.asArrayType(), values );
    }

    public static void writeDataObject( DataObject value, IStorage storage ) throws IOException{
        TypeFactory.write( value.getType(), storage );
        int count = value.getFieldCount();
        storage.out().writeInt( count );
        for( int i = 0; i < count; i++ ){
            write( value.getValue( i ), storage );
        }
    }
    
    public static DataObject readDataObject( IStorage storage ) throws IOException{
        Type type = TypeFactory.read( storage );
        int count = storage.in().readInt();
        Value[] fields = new Value[ count ];
        for( int i = 0; i < count; i++ )
            fields[i] = read( storage );
        
        return new DataObject( type.asDataObjectType(), fields );
    }

    public static void writeFloating( FloatingValue value, IStorage storage ) throws IOException{
        TypeFactory.write( value.getType(), storage );
        storage.out().writeDouble( value.getValue() );
    }
    
    public static FloatingValue readFloating( IStorage storage ) throws IOException{
        Type type = TypeFactory.read( storage );
        double value = storage.in().readDouble();
        return new FloatingValue( type.asBase(), value );
    }

    public static void writeInteger( IntegerValue value, IStorage storage ) throws IOException{
    	DataOutputStream out = storage.out();
        TypeFactory.write( value.getType(), storage );
        byte[] intern = value.getValue().toByteArray();
        out.writeInt( intern.length );
        out.write( intern );
    }
    
    public static IntegerValue readInteger( IStorage storage ) throws IOException{
    	DataInputStream in = storage.in();
        Type type = TypeFactory.read( storage );
        int length = in.readInt();
        byte[] intern = new byte[ length ];
        
        int read = 0;
        while( read < intern.length ){
            int next = in.read( intern, read, intern.length-read );
            if( next <= 0 )
                throw new IOException( "can't read enough bytes" );
            read += next;
        }
        
        return new IntegerValue( type.asBase(), new BigInteger( intern ));
    }

    public static void writeString( StringValue value, IStorage storage ) throws IOException{
    	DataOutputStream out = storage.out();
        TypeFactory.write( value.getType(), storage );
        int[] intern = value.getValue();
        
        out.writeInt( intern.length );
        for( int i = 0, n = intern.length; i<n; i++ )
            out.writeInt( intern[i] );
        
        out.writeBoolean( value.isCharacter() );
    }
    
    public static StringValue readString( IStorage storage ) throws IOException{
    	DataInputStream in = storage.in();
        Type type = TypeFactory.read( storage );
        int length = in.readInt();
        int[] intern = new int[ length ];
        for( int i = 0; i < length; i++ )
            intern[i] = in.readInt();
        boolean character = in.readBoolean();
        
        return new StringValue( type, character, intern );
    }
    
    public static void writeUnknown( UnknownValue value, IStorage storage ) throws IOException{
    	TypeFactory.write( value.getType(), storage );
    }
    
    public static UnknownValue readUnknown( IStorage storage ) throws IOException{
    	return new UnknownValue( TypeFactory.read( storage ));
    }
    
    private static class ValueGenericFactory<T extends Value> implements IGenericFactory<T>{
        public T create(){
            return null;
        }
        
        public void write( T value, IStorage storage ) throws IOException{
            ValueFactory.write( value, storage );
        }
        
        @SuppressWarnings("unchecked")
        public T read( T value, IStorage storage ) throws IOException{
            return (T)ValueFactory.read( storage ); 
        }
    };
}
