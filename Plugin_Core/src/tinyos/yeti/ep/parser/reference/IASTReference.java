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
package tinyos.yeti.ep.parser.reference;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.storage.GenericArrayFactory;
import tinyos.yeti.ep.storage.IGenericFactory;

/**
 * A {@link IASTReference} tells for a given region where it points to.
 * @author Benjamin Sigg
 */
public interface IASTReference{
	public static IGenericFactory<IASTReference[]> ARRAY_FACTORY = new GenericArrayFactory<IASTReference>(){
		@Override
		public IASTReference[] create( int size ){
			return new IASTReference[ size ];
		}		
	};
	
	/**
	 * The source of the reference, some text in some file.
	 * @return the source
	 */
	public IFileRegion getSource();
	
	/**
	 * The target of the reference, some ast node.
	 * @return the target
	 */
	public IASTModelPath getTarget();
}
