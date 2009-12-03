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

import java.io.File;

import tinyos.yeti.ep.IEnvironment;

/**
 * Combination of many {@link MakeExclude}s.
 * @author Benjamin Sigg
 */
public class MultiMakeExclude{
	private MakeExclude[] excludes;
	private IEnvironment environment;
	
	public MultiMakeExclude( MakeExclude[] excludes, IEnvironment environment ){
		this.excludes = excludes;
		this.environment = environment;
	}
	
	public boolean shouldExclude( File file ){
		if( excludes == null || environment == null )
			return false;
		
		String path = environment.systemToModel( file );
		if( path == null )
			return true;
		return shouldExclude( path );
	}
	
	public boolean shouldExclude( String path ){
		if( excludes != null ){
			for( MakeExclude exclude : excludes ){
				if( exclude.exclude( path ))
					return true;
			}
		}
		return false;
	}
}
