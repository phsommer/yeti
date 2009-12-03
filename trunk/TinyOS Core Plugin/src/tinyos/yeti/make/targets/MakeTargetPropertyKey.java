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

import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import tinyos.yeti.ep.MakeExtra;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.make.MakeExclude;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeTypedef;
import tinyos.yeti.make.targets.factories.BoardFactory;
import tinyos.yeti.make.targets.factories.BooleanFactory;
import tinyos.yeti.make.targets.factories.ExcludeFactory;
import tinyos.yeti.make.targets.factories.FileFactory;
import tinyos.yeti.make.targets.factories.IncludeFactory;
import tinyos.yeti.make.targets.factories.MacroFactory;
import tinyos.yeti.make.targets.factories.MakeExtraFactory;
import tinyos.yeti.make.targets.factories.ProjectFactory;
import tinyos.yeti.make.targets.factories.StringFactory;
import tinyos.yeti.make.targets.factories.TypedefFactory;

/**
 * Key to access a property in an {@link IMutableMakeTargetProperties}.
 * @author Benjamin Sigg
 */
public class MakeTargetPropertyKey<T>{
	public static final MakeTargetPropertyKey<IProject> PROJECT =
		new MakeTargetPropertyKey<IProject>( "project", false, true, new ProjectFactory() );
	
	public static final MakeTargetPropertyKey<String> TARGET =
		new MakeTargetPropertyKey<String>( "target", false, false, new StringFactory() );
	
	public static final MakeTargetPropertyKey<MakeExtra[]> MAKE_EXTRAS =
		new MakeTargetPropertyKey<MakeExtra[]>( "make-extras", true, false, new MakeExtraFactory() ){
			@Override
			public MakeExtra[] array( int size ){
				return new MakeExtra[ size ];
			}
		};
		
	public static final MakeTargetPropertyKey<MakeInclude[]> INCLUDES =
		new MakeTargetPropertyKey<MakeInclude[]>( "includes", true, false, new IncludeFactory() ){
			@Override
			public MakeInclude[] array( int size ){
				return new MakeInclude[ size ];
			}
		};
		
	public static final MakeTargetPropertyKey<MakeExclude[]> EXCLUDES =
		new MakeTargetPropertyKey<MakeExclude[]>( "excludes", true, false, new ExcludeFactory() ){
			@Override
			public MakeExclude[] array( int size ){
				return new MakeExclude[ size ];
			}
		};	
		
	public static final MakeTargetPropertyKey<MakeTypedef[]> TYPEDEFS =
		new MakeTargetPropertyKey<MakeTypedef[]>( "typedefs", true, false, new TypedefFactory() ){
			@Override
			public MakeTypedef[] array( int size ){
				return new MakeTypedef[ size ];
			}
		};
		
	public static final MakeTargetPropertyKey<IMacro[]> MACROS =
		new MakeTargetPropertyKey<IMacro[]>( "macros", true, false, new MacroFactory() ){
			@Override
			public IMacro[] array( int size ){
				return new IMacro[ size ];
			}
		};
	
	public static final MakeTargetPropertyKey<String[]> BOARDS =
		new MakeTargetPropertyKey<String[]>( "boards", true, false, new BoardFactory() ){
			@Override
			public String[] array( int size ){
				return new String[ size ];
			}
		};
		
	public static final MakeTargetPropertyKey<Boolean> NO_STD_INCLUDE =
		new MakeTargetPropertyKey<Boolean>( "no-std-incl", false, true, new BooleanFactory() );
	
	@Deprecated
	public static final MakeTargetPropertyKey<String> COMPONENT =
		new MakeTargetPropertyKey<String>( "component", false, false, new StringFactory() );
	
	public static final MakeTargetPropertyKey<IFile> COMPONENT_FILE =
		new MakeTargetPropertyKey<IFile>( "component-file", false, false, new FileFactory() );
	
	public static final MakeTargetPropertyKey<Boolean> INCLUDE_LAST_BUILD =
		new MakeTargetPropertyKey<Boolean>( "include-last-build", false, true, new BooleanFactory() );
	
	public static final MakeTargetPropertyKey<Boolean> INCLUDE_ENVIRONMENT_DEFAULT_PATHS =
		new MakeTargetPropertyKey<Boolean>( "include-env-default-paths", false, true, new BooleanFactory() );
		
	/**
	 * A list of all keys that are available for properties.
	 */
	@SuppressWarnings("deprecation")
	public static final MakeTargetPropertyKey<?>[] KEYS = new MakeTargetPropertyKey[]{
		PROJECT,
		TARGET,
		MAKE_EXTRAS,
		INCLUDES,
		EXCLUDES,
		TYPEDEFS,
		MACROS,
		BOARDS,
		NO_STD_INCLUDE,
		COMPONENT,
		COMPONENT_FILE,
		INCLUDE_LAST_BUILD,
		INCLUDE_ENVIRONMENT_DEFAULT_PATHS
	};
	
	private String name;
	private boolean array;
	private IMakeTargetPropertyFactory<T> factory;
	private boolean likeLocal;
	
	private MakeTargetPropertyKey( String name, boolean array, boolean likeLocal, IMakeTargetPropertyFactory<T> factory ){
		this.name = name;
		this.array = array;
		this.factory = factory;
		this.likeLocal = likeLocal;
	}
	
	public String getName(){
		return name;
	}
	
	/**
	 * Whether this key normally describes a local property.
	 * @return <code>true</code> if normally a local property
	 */
	public boolean likeLocal(){
		return likeLocal;
	}
	
	/**
	 * Creates a new <code>T</code> array of size <code>size</code>.
	 * @param size the size of the new array
	 * @return the new array
	 * @throws UnsupportedOperationException if <code>T</code> is not an array
	 */
	public T array( int size ){
		throw new UnsupportedOperationException();
	}
	
	public IMakeTargetPropertyFactory<T> getFactory(){
		return factory;
	}
	
	@SuppressWarnings("unchecked")
	public T combine( List<T> elements ){
		if( elements.size() == 0 )
			return array( 0 );
		
		LinkedHashSet<Object> set = new LinkedHashSet<Object>();
		
		for( T element : elements ){
			Object[] array = (Object[])element;
			for( Object value : array ){
				set.add( value );
			}
		}
		
		return (T)set.toArray( (Object[])array( set.size() ) );
	}
	
	/**
	 * Whether this key denotes an array of properties or a single property. 
	 * @return if an array of properties.
	 */
	public boolean isArray(){
		return array;
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
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
		MakeTargetPropertyKey other = (MakeTargetPropertyKey)obj;
		if( name == null ){
			if( other.name != null )
				return false;
		}
		else if( !name.equals( other.name ) )
			return false;
		return true;
	}
	
	
}
