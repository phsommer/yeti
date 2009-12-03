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
package tinyos.yeti.nesc12.parser.ast.elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.preprocessor.FileInfo;
import tinyos.yeti.preprocessor.RangeDescription;

public class LazyRangeDescription{
    public static final IGenericFactory<LazyRangeDescription> FACTORY = 
        new IGenericFactory<LazyRangeDescription>(){
        
        public LazyRangeDescription create(){
            return new LazyRangeDescription();
        }
        
        public void write( LazyRangeDescription value, IStorage storage ) throws IOException{
            storage.write( value.getRange(), RANGE_DESCRIPTION_FACTORY );
        }
        
        public LazyRangeDescription read( LazyRangeDescription value, IStorage storage ) throws IOException{
            value.range = storage.read( RANGE_DESCRIPTION_FACTORY );
            return value;
        }
    };
    
    public static final IGenericFactory<RangeDescription> RANGE_DESCRIPTION_FACTORY =
        new IGenericFactory<RangeDescription>(){

        public RangeDescription create(){
            return null; 
        }
        
        public void write( RangeDescription value, IStorage storage ) throws IOException{
        	DataOutputStream out = storage.out();
        	
        	// version
        	out.writeInt( -2 );

            out.writeInt( value.getLeft() );
            out.writeInt( value.getRight() );

            // write out all ranges
            int count = value.getRangeCount();
            out.writeInt( count );
            
            for( int i = 0; i < count; i++ ){
            	RangeDescription.Range minor = value.getRange( i );
            
                out.writeInt( minor.left() );
                out.writeInt( minor.right() );
                out.writeInt( minor.line() );
                out.writeInt( minor.sourceFlags() );
                storage.write( minor.file() );
                
                write( minor.fineRanges(), out );
                write( minor.roughRanges(), out );
            }
            
            // write the roots
            write( value.getRoots(), out );
        }
        
        private void write( RangeDescription.Range[] ranges, DataOutputStream out ) throws IOException{
        	int count = ranges == null ? 0 : ranges.length;
        	out.writeInt( count );
        	for( int i = 0; i < count; i++ ){
        		out.writeInt( ranges[i].internalIndex() );
        	}
        }
        
        public RangeDescription read( RangeDescription value, IStorage storage ) throws IOException{
            DataInputStream in = storage.in();

            int version = in.readInt();
            if( version != -2 )
            	throw new IOException( "unsupported version, note: file will automatically be rebuilt with correct version number" );
            
            int left = in.readInt();
            int right = in.readInt();
            int count = in.readInt();

            RangeDescription range = new RangeDescription( left, right );
            for( int i = 0; i < count; i++ ){
                left = in.readInt();
                right = in.readInt();
                int line = in.readInt();
                int flags = in.readInt();
                FileInfo file = storage.read();
            
                RangeDescription.Range[] fine = read( range, in );
                RangeDescription.Range[] rough = read( range, in );
                
                range.add( left, right, line, flags, file, rough, fine );
            }
            
            RangeDescription.Range[] roots = read( range, in );
            if( roots != null ){
            	for( RangeDescription.Range root : roots ){
            		range.addRoot( root );
            	}
            }
            
            return range;
        }
        
        private RangeDescription.Range[] read( RangeDescription range, DataInputStream in ) throws IOException{
        	int count = in.readInt();
        	if( count == 0 )
        		return null;
        	
        	RangeDescription.Range[] result = new RangeDescription.Range[ count ];
        	for( int i = 0; i < count; i++ ){
        		result[i] = range.getRange( in.readInt() );
        	}
        	
        	return result;
        }
    };

    private RangeDescription range;
    private ASTNode location;
    private Parser parser;

    private LazyRangeDescription(){
        // nothing
    }
    
    public LazyRangeDescription( ASTNode location, Parser parser ){
        this.location = location;
        this.parser = parser;
    }

    public LazyRangeDescription( RangeDescription range ){
        this.range = range;
    }
    
    public LazyRangeDescription( FileRegion region ){
        range = new RangeDescription( -1, -1 );
        IParseFile file = region.getParseFile();
        NesC12FileInfo info = null;
        if( file != null )
            info = new NesC12FileInfo( file );
        
        range.addRoot( range.addRough( region.getOffset(), region.getOffset() + region.getLength(), region.getLine(), 0, info, null, null ) );
    }

    public void resolve(){
        if( range == null && location != null && parser != null ){
            range = parser.resolveLocation( true, location );
            location = null;
            parser = null;
        }
    }

    public RangeDescription getRange(){
        resolve();
        return range;
    }
}
