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
package tinyos.yeti.search.model.group.declaration;

import tinyos.yeti.search.model.group.Group;
import tinyos.yeti.search.model.group.GroupGenerator;

public abstract class NodeGroupGenerator<K, N> extends GroupGenerator<K, N>{
	private Object input;
	
	public void setInput( Object input ){
		this.input = input;
	}
	
	@Override
	protected Group createRoot(){
		return new RootGroup( input );
	}
	
	public static class RootGroup extends Group{
		private Object input;
		
		public RootGroup( Object input ){
			super( null, null, null );
			this.input = input;
		}
		
		public Object getInput(){
			return input;
		}
	}
}
