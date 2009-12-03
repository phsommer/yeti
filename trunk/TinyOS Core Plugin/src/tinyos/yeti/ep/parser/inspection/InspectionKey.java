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
package tinyos.yeti.ep.parser.inspection;

/**
 * An {@link InspectionKey} is required by a {@link INesCNode} to access
 * the various references to other nodes.
 * @author Benjamin Sigg
 */
public class InspectionKey<K extends INesCNode>{
	private Class<K> kind;
	private String usage;
	
	public InspectionKey( Class<K> kind, String usage ){
		if( kind == null )
			throw new IllegalArgumentException( "kind must not be null" );
		
		if( usage == null )
			throw new IllegalArgumentException( "usage must not be null" );
		
		this.kind = kind;
		this.usage = usage;
	}
	
	public Class<K> getKind(){
		return kind;
	}
	
	public String getUsage(){
		return usage;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((usage == null) ? 0 : usage.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals( Object obj ){
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		InspectionKey other = (InspectionKey)obj;
		if( kind == null ){
			if( other.kind != null )
				return false;
		}
		else if( !kind.equals( other.kind ) )
			return false;
		if( usage == null ){
			if( other.usage != null )
				return false;
		}
		else if( !usage.equals( other.usage ) )
			return false;
		return true;
	}
}
