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
package tinyos.yeti.editors.nesc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.projection.ProjectionDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import tinyos.yeti.TinyOSPlugin;

/**
 * This partition scanner divides its document into sub-documents which are then scanned
 * independent from each other.
 * @author Benjamin Sigg
 */
public class ProjectingPartitionScanner implements IPartitionTokenScanner{
	private IPartitionTokenScanner decider;
	private Map<String, ScannerInfo> scanners = new HashMap<String, ScannerInfo>();
	
	/** the currently analyzed document */
	private IDocument document;
	
	/** currently known partitions */
	private List<Partition> partitions = new ArrayList<Partition>();
	
	private int tokenOffset;
	private int tokenLength;
	
	/** offset of the range to scan */
	private int rangeOffset;
	/** offset of the range to scan */
	private int rangeLength;
	
	public ProjectingPartitionScanner( IPartitionTokenScanner decider ){
		this.decider = decider;
	}
	
	public void setContentScanner( String contentType, IPartitionTokenScanner scanner ){
		ScannerInfo info = new ScannerInfo();
		info.contentType = contentType;
		info.scanner = scanner;
		info.index = scanners.size();
		scanners.put( contentType, info );
	}
	
	public void setPartialRange( IDocument document, int offset, int length, String contentType, int partitionOffset ) {	
		this.document = document;
		partitions.clear();
		
		if( partitionOffset > -1 ){
			int delta = offset - partitionOffset;
			if( delta > 0 ){
				setRange( document, partitionOffset, length+delta );
				return;
			}
		}
		
		setRange( document, offset, length );
	}

	public void setRange( IDocument document, int offset, int length ){
		try{
			this.document = document;
		
			this.rangeOffset = offset;
			this.rangeLength = length;
		
			findPartitions();
			updatetProjections();
		
			tokenOffset = -1;
			tokenLength = -1;
		}
		catch( BadLocationException ex ){
			TinyOSPlugin.log( ex );
		}
	}
	
	/**
	 * Analyzes the current document and finds all its partitions.
	 */
	private void findPartitions(){
		partitions.clear();
		
		decider.setRange( document, 0, document.getLength() );
		
		IToken token;
		Partition partition = null;
		
		while( !(token = decider.nextToken()).isEOF() ){
			if( partition == null || !partition.contentType.equals( token.getData() )){
				partition = new Partition();
				partition.length = decider.getTokenLength();
				partition.offset = decider.getTokenOffset();
				partition.contentType = token.getData().toString();
				partitions.add( partition );
			}
			else{
				partition.length += decider.getTokenLength();
			}
		}
	}
	
	/**
	 * Disconnects this {@link ProjectingPartitionScanner}. This means that
	 * all resources held by this scanner are freed and the states are lost,
	 * however the scanner can be reused.
	 */
	public void disconnect(){
		for( ScannerInfo info : scanners.values() ){
			if( info.document != null ){
				info.document.dispose();
				info.document = null;
			}
		}
	}
	
	private void updatetProjections() throws BadLocationException {	
		for( ScannerInfo info : scanners.values() ){
			// while direct instances should not be created, we don't require
			// the additional support for changing master documents that a
			// ProjectionDocumentManager would provide.
			if( info.document != null ){
				info.document.dispose();
			}
			
			info.document = new ProjectionDocument( document );
			info.token = null;
			info.length = -1;
			info.offset = -1;
		}
		
		int[] scannerLength = new int[ scanners.size() ];
		boolean[] accessed = new boolean[ scanners.size() ];
		
		for( Partition partition : partitions ){
			ScannerInfo info = scanners.get( partition.contentType );
			accessed[ info.index ] = true;
			info.document.addMasterDocumentRange( partition.offset, partition.length );
			if( (info.offset == -1) || (partition.offset < this.rangeOffset) ){
				info.offset = scannerLength[ info.index ];
			}
			
			scannerLength[ info.index ] += partition.length;
			
			if( info.length == -1 ){
				if( partition.offset + partition.length >= this.rangeOffset + this.rangeLength ){
					info.length = scannerLength[ info.index ] - info.offset;
				}
			}
		}
		
		for( ScannerInfo info : scanners.values() ){
			if( info.length == -1 && accessed[ info.index ]){
				info.length = scannerLength[ info.index ] - info.offset;
			}
			
			if( info.length >= 0 ){
				info.scanner.setRange( info.document, info.offset, info.length );
			}
		}
	}
	
	private Partition partitionAt( int offset ){
		for( Partition partition : partitions ){
			if( partition.offset <= offset && partition.offset + partition.length > offset )
				return partition;
		}
		
		return null;
	}
	
	public int getTokenLength() {
		return tokenLength;
	}

	public int getTokenOffset() {
		return tokenOffset;
	}

	public IToken nextToken() {
		int offset = tokenOffset == -1 ? rangeOffset : (tokenOffset+tokenLength);
		Partition partition = partitionAt( offset );
		if( partition == null ){
			tokenOffset += tokenLength;
			tokenLength = 0;
			return Token.EOF;
		}
		
		ScannerInfo scanner = scanners.get( partition.contentType );
		
		while( !tokenAtOffset( scanner, offset )){
			advance( scanner );
		}
		
		tokenOffset = offset;
		
		tokenLength = Math.min( Math.min( Math.min( 
									partition.length, 											// can not step over the partition
									scanner.scanner.getTokenLength() ),							// obvious...
									partition.offset - tokenOffset + partition.length ),		// token goes over the partition
									scanner.tokenOffset + scanner.tokenLength - partition.offset );		// token ends in this partition
		
		return scanner.token;
	}
	
	private boolean tokenAtOffset( ScannerInfo scanner, int offset ){
		if( scanner.token == null )
			return false;
		
		if( scanner.token.isEOF() )
			return true;
		
		return scanner.tokenOffset <= offset && scanner.tokenOffset + scanner.tokenLength > offset;
	}
	
	private void advance( ScannerInfo scanner ){
		scanner.token = scanner.scanner.nextToken();
		
		scanner.tokenOffset = masterOffset( scanner.contentType, scanner.scanner.getTokenOffset() );
		scanner.tokenLength = masterOffset( 
				scanner.contentType, 
				scanner.scanner.getTokenOffset() + scanner.scanner.getTokenLength() - 1 ) - scanner.tokenOffset + 1;
	}
	
	private int masterOffset( String contentType, int offset ){
		int slaveOffset = 0;
		for( Partition partition : partitions ){
			if( partition.contentType.equals( contentType )){
				if( slaveOffset + partition.length > offset ){
					return partition.offset + offset - slaveOffset;
				}
				
				slaveOffset += partition.length;
			}
		}
		
		return -1;
	}
	
	private class Partition{
		public String contentType;
		public int offset;
		public int length;
		
		@Override
		public String toString() {
			return "offset=" + offset + ", length=" + length + ", content=" + contentType;
		}
	}
	
	private class ScannerInfo{
		public String contentType;
		public IPartitionTokenScanner scanner;
		public ProjectionDocument document;
		
		/** range to scan */
		public int offset;
		/** range to scan */
		public int length;
		
		/** start of token in master document */
		public int tokenOffset;
		/** end of token in master document */
		public int tokenLength;
		public IToken token;
		
		public int index;
		
		@Override
		public String toString() {
			if( token == null ){
				return "null";
			}
			else{
				return "offset=" + tokenOffset + ", length=" + tokenLength + ", token=" + token.getData(); 
			}
		}
	}
}
