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
 * Provides all the information necessary to call <code>ncc</code> or to
 * build a project. {@link IMutableMakeTargetProperties}s can be nested, an {@link IMutableMakeTargetProperties}.
 * @author Benjamin Sigg
 */
public interface IMutableMakeTargetProperties extends ILocalMutableMakeTargetProperties{
		
	/**
	 * Adds a backup target for property <code>key</code>.
	 * @param key the key of the property
	 * @param target the backup target
	 */
	public <T> void addBackup( MakeTargetPropertyKey<T> key, IMakeTargetProperties target );
	
	/**
	 * Adds backup targets for property <code>key</code>.
	 * @param key the key of the property
	 * @param targets the backup targets
	 */
	public <T> void addBackups( MakeTargetPropertyKey<T> key, IMakeTargetProperties... targets );
	
	/**
	 * Gets all the backups that are used for property <code>key</code>
	 * @param key name of a property
	 * @return all the backups for <code>key</code>
	 */
	public <T> IMakeTargetProperties[] getBackups( MakeTargetPropertyKey<T> key );
	
	/**
	 * Removes a backup target for property <code>key</code>.
	 * @param key the key of the property
	 * @param target the target to remove
	 */
	public <T> void removeBackup( MakeTargetPropertyKey<T> key, IMakeTargetProperties target );
	
	/**
	 * Removes all backups for <code>key</code>.
	 * @param key name of a property
	 */
	public <T> void removeBackups( MakeTargetPropertyKey<T> key );
	
	/**
	 * Tells this properties how to order the array property <code>key</code>.
	 * @param key the name of a property
	 * @param order how to order this property
	 */
	public <T> void putOrder( MakeTargetPropertyKey<T[]> key, IMakeTargetPropertyComparator<T> order );
}
