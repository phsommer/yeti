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
package tinyos.yeti.make.targets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tinyos.yeti.ep.MakeExtra;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.make.MakeTypedef;
import tinyos.yeti.make.targets.IMakeTargetPropertyComparator.Order;

/**
 * Default generic implementation of {@link IMutableMakeTargetProperties}.
 * @author Benjamin Sigg
 */
public class DefaultMakeTargetProperties implements IMutableMakeTargetProperties{
	private Map<MakeTargetPropertyKey<?>, Property<?>> properties =
		new HashMap<MakeTargetPropertyKey<?>, Property<?>>();
	
	private int priority;
	
	public DefaultMakeTargetProperties( int priority ){
		this.priority = priority;
		installDefaults();
	}
	
	public int getPriority(){
		return priority;
	}
	
	private void installDefaults(){
		putOrder( MakeTargetPropertyKey.BOARDS, new IMakeTargetPropertyComparator<String>(){
			public Order compare( String first, String second ){
				if( first.equals( second ))
					return Order.HIDE_SECOND;
				return Order.EQUAL;
			}
		});
		putOrder( MakeTargetPropertyKey.MAKE_EXTRAS, new IMakeTargetPropertyComparator<MakeExtra>(){
			public Order compare( MakeExtra first, MakeExtra second ){
				if( first.getName().equals( second.getName() ))
					return Order.HIDE_SECOND;
				return Order.EQUAL;
			}
		});
		putOrder( MakeTargetPropertyKey.MACROS, new IMakeTargetPropertyComparator<IMacro>(){
			public Order compare( IMacro first, IMacro second ){
				if( first.getName().equals( second.getName() ))
					return Order.HIDE_SECOND;
				return Order.EQUAL;
			}
		});
		putOrder( MakeTargetPropertyKey.TYPEDEFS, new IMakeTargetPropertyComparator<MakeTypedef>(){
			public Order compare( MakeTypedef first, MakeTypedef second ){
				if( first.getName().equals( second.getName() ))
					return Order.HIDE_SECOND;
				return Order.EQUAL;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProperty( MakeTargetPropertyKey<T> key ){
		Property<T> property = get( key, false );
		if( property == null )
			return null;
		
		if( key.isArray() ){
			List<T> collection = new LinkedList<T>();
			if( property.useLocalProperty && property.value != null )
				collection.add( property.value );
			
			if( property.backup != null ){
				for( IMakeTargetProperties backup : property.backup ){
					T result = backup.getProperty( key );
					if( result != null ){
						collection.add( result );
					}
				}
			}
			
			T result = key.combine( collection );
			return (T)order( (MakeTargetPropertyKey)key, (Object[])result );
		}
		else if( property.useLocalProperty ){
			return property.value;
		}
		else{
			if( property.backup != null ){
				for( IMakeTargetProperties backup : property.backup ){
					T result = backup.getProperty( key );
					if( result != null )
						return result;
				}
			}
			return null;
		}
	}
	
	public <T> T getLocalProperty( MakeTargetPropertyKey<T> key ){
		Property<T> property = get( key, false );
		if( property == null )
			return null;
		return property.value;
	}
	
	public <T> void putLocalProperty( MakeTargetPropertyKey<T> key, T value ){
		Property<T> property = get( key, true );
		property.value = value;
		maybeDelete( key, property );
	}
	
	public <T> void setUseLocalProperty( MakeTargetPropertyKey<T> key, boolean useLocalProperty ){
		Property<T> property = get( key, true );
		property.useLocalProperty = useLocalProperty;
		maybeDelete( key, property );
	}
	
	public <T> boolean isUseLocalProperty( MakeTargetPropertyKey<T> key ){
		Property<T> property = get( key, false );
		if( property == null )
			return false;
		return property.useLocalProperty;
	}
	
	public <T> void addBackup( MakeTargetPropertyKey<T> key, IMakeTargetProperties target ){
		Property<T> property = get( key, true );
		if( property.backup == null )
			property.backup = new ArrayList<IMakeTargetProperties>();
		property.backup.add( target );
		sort( property.backup );
	}
	
	public <T> void addBackups( MakeTargetPropertyKey<T> key, IMakeTargetProperties... targets ){
		if( targets.length > 0 ){
			Property<T> property = get( key, true );
			if( property.backup == null )
				property.backup = new ArrayList<IMakeTargetProperties>();
			
			for( IMakeTargetProperties backup : targets )
				property.backup.add( backup );
			
			sort( property.backup );
		}
	}
	
	public <T> void putOrder( MakeTargetPropertyKey<T[]> key, IMakeTargetPropertyComparator<T> order ){
		get( key, true ).order = order;
	}
	
	private void sort( List<IMakeTargetProperties> properties ){
		Collections.sort( properties, new Comparator<IMakeTargetProperties>(){
			public int compare( IMakeTargetProperties o1, IMakeTargetProperties o2 ){
				int p1 = o1.getPriority();
				int p2 = o2.getPriority();
				
				if( p1 < p2 )
					return -1;
				if( p1 > p2 )
					return 1;
				return 0;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private Object[] order( MakeTargetPropertyKey key, Object[] array ){
		// let's fall back to non-generic programming, this would get too hard otherwise
		final IMakeTargetPropertyComparator comparator = get( key, true ).order;
		if( comparator == null )
			return array;
		
		// need to compare anything with anything to get all the "hide"s
		boolean[] hidden = new boolean[ array.length ];
		for( int i = 0; i < hidden.length; i++ ){
			for( int j = i+1; j < hidden.length; j++ ){
				Order order = comparator.compare( array[i], array[j] );
				switch( order ){
					case HIDE_FIRST:
						hidden[i] = true;
						break;
					case HIDE_SECOND:
						hidden[j] = true;
						break;
					case HIDE_BOTH:
						hidden[i] = true;
						hidden[j] = true;
						break;
				}
			}
		}
		
		int remaining = 0;
		for( boolean hide : hidden ){
			if( !hide ){
				remaining++;
			}
		}
		
		if( remaining != array.length ){
			Object[] temp = (Object[])key.array( remaining );
			int index = 0;
			for( int i = 0; i < array.length; i++ ){
				if( !hidden[i] ){
					temp[index++] = array[i];
				}
			}
			array = temp;
		}
		else{
			Object[] temp = (Object[])key.array( array.length );
			System.arraycopy( array, 0, temp, 0, array.length );
			array = temp;
		}
		
		Arrays.sort( array, new Comparator<Object>(){
			public int compare( Object o1, Object o2 ){
				Order order = comparator.compare( o1, o2 );
				switch( order ){
					case EQUAL: return 0;
					case SMALLER: return -1;
					case GREATER: return 1;
					default: throw new IllegalStateException( "illegal order: " + order );
				}
			}
		});
		
		return array;
	}
	
	public <T> IMakeTargetProperties[] getBackups( MakeTargetPropertyKey<T> key ){
		Property<T> property = get( key, false );
		if( property == null || property.backup == null )
			return new IMutableMakeTargetProperties[]{};
		else
			return property.backup.toArray( new IMakeTargetProperties[ property.backup.size() ] );
	}

	public <T> void removeBackup( MakeTargetPropertyKey<T> key, IMakeTargetProperties target ){
		Property<T> property = get( key, false );
		if( property != null && property.backup != null ){
			property.backup.remove( target );
			if( property.backup.isEmpty() ){
				property.backup = null;
				maybeDelete( key, property );
			}
		}
	}
	
	public <T> void removeBackups( MakeTargetPropertyKey<T> key ){
		Property<T> property = get( key, false );
		if( property != null && property.backup != null ){
			property.backup = null;
			maybeDelete( key, property );
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> Property<T> get( MakeTargetPropertyKey<T> key, boolean create ){
		Property<T> result = (Property<T>)properties.get( key );
		if( result == null && create ){
			result = new Property<T>();
			properties.put( key, result );
		}
		return result;
	}
	
	private <T> void maybeDelete( MakeTargetPropertyKey<T> key, Property<T> property ){
		if( property.value == null && !property.useLocalProperty && property.backup == null && property.order == null ){
			properties.remove( key );
		}
	}
	
	private static class Property<T>{
		public T value;
		public List<IMakeTargetProperties> backup;
		public boolean useLocalProperty = false;
		public IMakeTargetPropertyComparator<?> order;
	}
}
