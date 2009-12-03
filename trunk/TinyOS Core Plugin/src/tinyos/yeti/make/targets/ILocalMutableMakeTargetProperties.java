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

/**
 * A {@link IMakeTargetProperties} that internally has some backup mechanism,
 * clients can tell the properties whether to use this backup mechanism or not.
 * @author Benjamin Sigg
 */
public interface ILocalMutableMakeTargetProperties extends IMakeTargetProperties{
	/**
	 * Sets the local value of a property.
	 * @param key the name of the property
	 * @param value the property, may be <code>null</code> for some properties
	 */
	public <T> void putLocalProperty( MakeTargetPropertyKey<T> key, T value );

	/**
	 * Sets how this {@link IMutableMakeTargetProperties} should behave when asked for
	 * the property <code>key</code>.
	 * @param key the key of the property
	 * @param useLocalProperty if <code>true</code> then the local property
	 * is returned, if <code>false</code> then a backup property is returned
	 */
	public <T> void setUseLocalProperty( MakeTargetPropertyKey<T> key, boolean useLocalProperty );

	/**
	 * Tells whether for property <code>key</code> always the local value
	 * should be used.
	 * @param key the name of the property
	 * @return <code>true</code> if the local value should be used
	 */
	public <T> boolean isUseLocalProperty( MakeTargetPropertyKey<T> key );

}
