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

import org.eclipse.core.resources.IProject;

import tinyos.yeti.make.targets.IMakeTargetMorpheable;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

/**
 * Interface used to communicate with the {@link MakeTarget}s that are associated
 * with a specific {@link IProject}.
 * @author Benjamin Sigg
 */
public interface IProjectMakeTargets{
	public IProject getProject();
	
	public MakeTarget[] getStandardTargets();
	
	public IMakeTargetMorpheable[] getSelectableTargets();
	
	/**
	 * Gets a human readable name for <code>morph</code>
	 * @param morph some morph
	 * @return the human readable name or description
	 */
	public String getNameForSelectable( IMakeTargetMorpheable morph );
	
	public boolean containsStandardTarget( MakeTarget target );
	
	public MakeTarget findStandardTarget( String name );
	
	public boolean addStandardTarget( MakeTarget target );
	
	public boolean removeStandardTarget( MakeTarget target );
	
	public void informStandardTargetChanged( MakeTarget target );
	
	public boolean setSelectedTarget( IMakeTargetMorpheable target );
	
	public IMakeTargetMorpheable getSelectedTarget();
	
	public MakeTargetSkeleton getDefaults();
	
	public void setDefaults( MakeTargetSkeleton defaults );
	
	public void informDefaultsChanged();
	
	/**
	 * Opens the default {@link MakeTargetSkeleton} for writing, this method
	 * always returns the same object until the appropriate number of
	 * {@link SharedMakeTarget#close() close} calls are committed, the changes
	 * will be automatically transfered once enough closes are committed.
	 * @param open whether {@link SharedMakeTarget#open()} should be called or not
	 * @return the shared make target
	 */
	public SharedMakeTarget<MakeTargetSkeleton> openDefaults( boolean open );
	
	/**
	 * Stores some additional information persistently.
	 * @param key some key
	 * @param value some value
	 */
	public void put( String key, String value );
	
	/**
	 * Gets some information that was stored persistently.
	 * @param key the key
	 * @return the information or <code>null</code> if not present
	 */
	public String get( String key );
}
