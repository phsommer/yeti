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
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.MakeTargetManager;
import tinyos.yeti.make.SharedMakeTarget;

/**
 * Shared standard {@link MakeTarget}, the make-target is read and written
 * from the {@link MakeTargetManager}.
 * @author Benjamin Sigg
 */
public class StandardSharedMakeTarget extends SharedMakeTarget<MakeTarget>{
	private IProjectMakeTargets targets;
	
	public StandardSharedMakeTarget( IProjectMakeTargets targets, MakeTarget target ){
		super( targets.getProject(), target );
		this.targets = targets;
	}
	
	@Override
	protected MakeTarget read( MakeTarget original ){
		if( original == null ){
			MakeTarget target = new MakeTarget( getProject(), null, null );
			target.setDefaults( targets.getDefaults() );
			for( MakeTargetPropertyKey<?> key : MakeTargetPropertyKey.KEYS ){
				if( key.likeLocal() ){
					target.setUseDefaultProperty( key, false );
					target.setUseLocalProperty( key, true );
				}
				else{
					target.setUseDefaultProperty( key, true );
					target.setUseLocalProperty( key, key.isArray() );
				}
			}
			
			target.setUsingPlatformIncludes( false );
			target.setUsingLastBuildIncludes( false );
			return target;
		}
		else{
			return original.copy();
		}
	}

	@Override
	protected MakeTarget write( MakeTarget copy, MakeTarget original ){
		if( original == null ){
            
			// set Name uniquely
            String name = copy.getName();
            name = generateUniqueName( name == null ? "" : name.trim() );
            copy.setName( name );
            original = copy.copy();
            targets.addStandardTarget( original );
        }
		else {
			original.copy( copy );
			targets.informStandardTargetChanged( original );
        }
		return original;
	}
	
	public String generateUniqueName(String targetString) {
        String newName = targetString;
        int i = 0;
        while( targets.findStandardTarget( newName) != null ){
        	i++;
        	newName = targetString + " (" + i + ")";
        }
        
        return newName;
    }
}
