/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2010 ETH Zurich
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
package tinyos.yeti.model.standard.streams;

import org.eclipse.core.runtime.IPath;

import tinyos.yeti.ProjectTOS;

/**
 * Uses only the last segment of a path.
 * @author Benjamin Sigg
 */
public class ReducingPathConverter extends MappingConverter{
	public ReducingPathConverter( ProjectTOS project ){
		super( project );
	}
	
	@Override
	protected IPath convertPath( Namespace namespace, IPath path ){
		int count = path.segmentCount();
		if( count <= 1 )
			return path;
		
		return path.removeFirstSegments( count-1 );
	}
}
