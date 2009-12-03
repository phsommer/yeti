package storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.nesc12.ep.NesC12GenericStorage;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.preprocessor.RangeDescription;

public class NesC12GenericStorageTest extends TestCase{
    
    @Test
    public void testFileRegion() throws IOException{
        FileRegion region = new FileRegion( NullParseFile.NULL, 10, 9, 8 );
        run( region );
    }
    
    @Test
    public void testFileInfo() throws IOException{
        NesC12FileInfo info = new NesC12FileInfo( NullParseFile.NULL, "name" );
        run( info );
    }
    
    @Test
    public void testRangeDescription() throws IOException{
        RangeDescription range = new RangeDescription( 1, 2 );
        range.addRoot( range.add( 3, 5, 1, 0, new NesC12FileInfo( NullParseFile.NULL, "bubum" ), null, null ) );
        range.addRoot( range.add( 6, 8, 2, 1, new NesC12FileInfo( NullParseFile.NULL, "laba" ), null, null ) );
        run( range );
    }
    
    private Object run( Object value ) throws IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( bout );
        
        NesC12GenericStorage storage = new NesC12GenericStorage( null, out, null );
        storage.write( value );
        out.close();
        
        DataInputStream in = new DataInputStream( new ByteArrayInputStream( bout.toByteArray() ));
        storage = new NesC12GenericStorage( null, in, null );
        Object read = storage.read();
        
        return read;
    }
}
