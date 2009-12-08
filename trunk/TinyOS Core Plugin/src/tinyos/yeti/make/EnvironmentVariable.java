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
package tinyos.yeti.make;

public class EnvironmentVariable{
	public static EnvironmentVariable[] combine( EnvironmentVariable[]...environmentVariables ){
		// search for a single entry
		int count = 0;
		int index = 0;
		int length = 0;
		
		for( int i = 0; i < environmentVariables.length; i++ ){
			if( environmentVariables[i] != null ){
				count++;
				index = i;
				length += environmentVariables[i].length;
			}
		}
		if( count == 0 )
			return new EnvironmentVariable[]{};
		else if( count == 1 )
			return environmentVariables[index];
		
		EnvironmentVariable[] result = new EnvironmentVariable[ length ];
		index = 0;
		for( EnvironmentVariable[] array : environmentVariables ){
			if( array != null ){
				System.arraycopy( array, 0, result, index, array.length );
				index += array.length;
			}
		}
		
		return result;
	}
	
	private String key;
	private String value;
	
	public EnvironmentVariable( String key, String value ){
		this.key = key;
		this.value = value;
	}
	
	public String getKey(){
		return key;
	}
	
	public String getValue(){
		return value;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals( Object obj ){
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		EnvironmentVariable other = (EnvironmentVariable)obj;
		if( key == null ){
			if( other.key != null )
				return false;
		}
		else if( !key.equals( other.key ) )
			return false;
		if( value == null ){
			if( other.value != null )
				return false;
		}
		else if( !value.equals( other.value ) )
			return false;
		return true;
	}
}
