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

public class NullPathConverter implements IPathConverter{
	public ICachePath convert( Namespace namespace, IPath path ){
		return new CachePath( path );
	}
	
	private static class CachePath implements ICachePath{
		private IPath path;
		
		public CachePath( IPath path ){
			this.path = path;
		}
		
		public IPath getPath(){
			return path;
		}
		
		public void open(){
			// ignore	
		}
		
		public void close(){
			// ignore	
		}
	}
}
