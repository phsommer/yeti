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
package tinyos.yeti.nesc12.parser.meta;

import tinyos.yeti.ep.parser.IFileRegion;

/**
 * A list containing declarations and references to these declarations.
 * @author Benjamin Sigg
 */
public class QuickLinks{
	private Entry[] links;
	
	public QuickLinks( int size ){
		links = new Entry[ size ];
	}
	
	public void put( int index, String name, IFileRegion region, int[] offsets ){
		links[index] = new Entry( name, region, offsets );
	}
	
	public IFileRegion getTarget( String name, int offset ){
		for( Entry entry : links ){
			if( name.equals( entry.getName() )){
				if( entry.contains( offset )){
					return entry.getRegion();
				}
			}
		}
		return null;
	}
	
	private class Entry{
		private String name;
		private IFileRegion region;
		
		private int[] offsetReferences;
		
		public Entry( String name, IFileRegion region, int[] offsetReferences ){
			this.name = name;
			this.region = region;
			this.offsetReferences = offsetReferences;
		}
		
		public String getName(){
			return name;
		}
		
		public IFileRegion getRegion(){
			return region;
		}
		
		public int offset( int offset ){
			int length = name.length();
			
			for( int check : offsetReferences ){
				if( check <= offset && offset <= check + length )
					return check;
			}
			return -1;
		}
		
		public boolean contains( int offset ){
			return offset( offset) >= 0;
		}
	}
}
