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
package tinyos.yeti.environment.basic.platform;

import org.eclipse.jface.preference.IPreferenceStore;

import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeInclude.Include;

public final class PlatformUtility{
	private PlatformUtility(){
		// nothing
	}

	public static void store( IPlatform platform, MakeInclude[] includes, IPreferenceStore store ){
		store( "makeincludes.platform." + platform.getName(), includes, store );
	}

	public static void storeGeneral( MakeInclude[] includes, IPreferenceStore store ){
		store( "makeincludes.general", includes, store );
	}

	public static void store( String key, MakeInclude[] includes, IPreferenceStore store ){
		StringBuilder builder = new StringBuilder();
		if( includes == null ){
			builder.append( 0 );
			builder.append( "." );
		}
		else{
			builder.append( includes.length );
			builder.append( "." );

			for( MakeInclude include : includes ){
				builder.append( 'v' );
				builder.append( include.isRecursive() ? '+' : '-' );
				builder.append( include.isNcc() ? 't' : 'f' );
				builder.append( include.isGlobal() ? 't' : 'f' );
				switch( include.getInclude() ){
					case NONE:
						builder.append( 'n' );
						break;
					case SOURCE:
						builder.append( 's' );
						break;
					case SYSTEM:
						builder.append( 'y' );
						break;
				}
				
				builder.append( include.getPath().length() );
				builder.append( "." );
				builder.append( include.getPath() );
			}
		}

		store.setValue( key, builder.toString() );
	}

	public static MakeInclude[] load( IPlatform platform, IPreferenceStore store ){
		return load( "makeincludes.platform." + platform.getName(), store );
	}

	public static MakeInclude[] loadGeneral( IPreferenceStore store ){
		return load( "makeincludes.general", store );
	}

	public static MakeInclude[] load( String key, IPreferenceStore store ){
		return decode( store.getString( key ) );
	}

	public static MakeInclude[] loadDefault( IPlatform platform, IPreferenceStore store ){
		return loadDefault( "makeincludes.platform." + platform.getName(), store );
	}

	public static MakeInclude[] loadDefaultGeneral( IPreferenceStore store ){
		return loadDefault( "makeincludes.general", store );
	}

	public static MakeInclude[] loadDefault( String key, IPreferenceStore store ){
		return decode( store.getDefaultString( key ) );
	}

	public static MakeInclude[] decode( String value ){
		if( value == null )
			return new MakeInclude[]{};

		int index = value.indexOf( '.' );
		if( index < 0 )
			return new MakeInclude[]{};

		int size = Integer.parseInt( value.substring( 0, index++ ) );
		MakeInclude[] result = new MakeInclude[ size ];

		for( int i = 0; i < size; i++ ){
			boolean recursive = false;
			boolean ncc = false;
			boolean global = false;
			Include include = Include.NONE;
			
			if( value.charAt( index ) == 'v' ){
				index++;
				recursive = value.charAt( index++ ) == '+';
				ncc = value.charAt( index++ ) == 't';
				global = value.charAt( index++ ) == 't';
				
				switch( value.charAt( index++ ) ){
					case 'n':
						include = Include.NONE;
						break;
					case 's':
						include = Include.SOURCE;
						break;
					case 'y':
						include = Include.SYSTEM;
						break;
				}
			}
			else{
				switch( value.charAt( index++ )){
					case 'g':
						global = true;
						break;
					case 'r':
						include = Include.SOURCE;
						ncc = true;
						break;
					case 's':
						include = Include.SYSTEM;
						break;
				}
				
				recursive = value.charAt( index++ ) == 't';
			}

			int point = value.indexOf( '.', index );
			int length = Integer.parseInt( value.substring( index, point ) );

			String path = value.substring( point+1, point+1+length );
			index = point+1+length;

			result[i] = new MakeInclude( path, include, recursive, ncc, global );
		}

		return result;

	}
}
