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
 * A set of properties that are needed to call the compiler. This map
 * may not support all {@link MakeTargetPropertyKey}s and return
 * <code>null</code> for some of them.
 * @author Benjamin Sigg
 */
public interface IMakeTargetProperties{
	/**
	 * Gets the priority of this properties, a low number means high priority.
	 * The priority is needed to determine in which oder the backup-properties
	 * should be evaluated.
	 * @return the priority
	 */
	public int getPriority();
	
	/**
	 * Gets the value of the property <code>key</code>, this method should
	 * include all backup-targets when calculating the current value of
	 * this property.
	 * @param key the name of the property
	 * @return the property, may be <code>null</code> for some properties
	 */
	public <T> T getProperty( MakeTargetPropertyKey<T> key );
	
	/**
	 * Gets the local value of a property, this value is independent
	 * of any backup-target.
	 * @param key the name of the property
	 * @return the property, may be <code>null</code> for some properties
	 */
	public <T> T getLocalProperty( MakeTargetPropertyKey<T> key );
}
