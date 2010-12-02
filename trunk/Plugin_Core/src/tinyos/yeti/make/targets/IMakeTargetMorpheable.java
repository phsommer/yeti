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

import org.eclipse.core.resources.IProject;

import tinyos.yeti.make.IProjectMakeTargets;
import tinyos.yeti.make.MakeTarget;

/**
 * An object that can be transformed to, or is a, {@link MakeTarget}.
 * @author Benjamin Sigg
 */
public interface IMakeTargetMorpheable{
	public MakeTarget toMakeTarget();
		
    /**
     * Get the target build container.
     * @return IContainer of where target build will be invoked. 
     */
    public IProject getProject();
    
    /**
     * Gets all the targets that are in the same project as this target.
     * @return all the other targets
     */
    public IProjectMakeTargets getTargets();
}
