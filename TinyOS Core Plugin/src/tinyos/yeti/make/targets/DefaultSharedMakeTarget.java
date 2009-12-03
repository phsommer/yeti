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

import tinyos.yeti.make.IProjectMakeTargets;
import tinyos.yeti.make.SharedMakeTarget;

/**
 * Sharing of a projects default {@link MakeTargetSkeleton}.
 * @author Benjamin Sigg
 */
public class DefaultSharedMakeTarget extends SharedMakeTarget<MakeTargetSkeleton>{
	private IProjectMakeTargets targets;
	
	public DefaultSharedMakeTarget( IProjectMakeTargets targets ){
		super( targets.getProject(), targets.getDefaults() );
		this.targets = targets;
	}
	
	@Override
	protected MakeTargetSkeleton read( MakeTargetSkeleton original ){
		if( original == null )
			return new MakeTargetSkeleton( getProject() );
		else
			return original.copy();
	}
	
	@Override
	protected MakeTargetSkeleton write( MakeTargetSkeleton copy, MakeTargetSkeleton original ){
		if( original == null ){
			original = copy.copy();
			targets.setDefaults( original );
		}
		else{
			original.copy( copy );
			targets.informDefaultsChanged();
		}
		return original;
	}
}
