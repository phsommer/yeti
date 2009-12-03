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
package tinyos.yeti.model.missing;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.storage.GenericArrayFactory;
import tinyos.yeti.ep.storage.IGenericFactory;

/**
 * Represents some resource that is missing. That might be a file or some
 * declaration or anything else that 
 * @author Benjamin Sigg
 */
public interface IMissingResource{
    public static final IGenericFactory<IMissingResource[]> ARRAY_FACTORY = new GenericArrayFactory<IMissingResource>(){
        @Override
        public IMissingResource[] create( int size ){
            return new IMissingResource[ size ];
        }
    };
    
    /**
     * Checks whether this resource is still missing.
     * @param project the project to check
     * @param self the file which is responsible for this missing resource, if
     * a resource finds out, that it is missing itself, then it must return
     * <code>false</code>
     * @param monitor to interact with the outside world
     * @return <code>true</code> if the resource is now available and not <code>self</code>,
     * <code>false</code> if the resource is still missing 
     */
    public boolean checkAvailable( ProjectTOS project, IParseFile self, IProgressMonitor monitor );
}
