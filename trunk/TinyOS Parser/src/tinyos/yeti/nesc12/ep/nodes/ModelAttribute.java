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
package tinyos.yeti.nesc12.ep.nodes;

import java.io.IOException;

import tinyos.yeti.ep.parser.IASTModelAttribute;
import tinyos.yeti.ep.storage.GenericArrayFactory;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;

public class ModelAttribute implements IASTModelAttribute, Binding{
	public static final IGenericFactory<ModelAttribute> FACTORY = new IGenericFactory<ModelAttribute>(){
		public ModelAttribute create(){
			return new ModelAttribute( null );
		}
		
		public ModelAttribute read( ModelAttribute value, IStorage storage ) throws IOException{
			value.name = storage.readString();
			return value;
		}
		
		public void write( ModelAttribute value, IStorage storage ) throws IOException{
			storage.writeString( value.name );
		}
	};
	
	public static final IGenericFactory<ModelAttribute[]> ARRAY_FACTORY = new GenericArrayFactory<ModelAttribute>( ){
		@Override
		public ModelAttribute[] create( int size ){
			return new ModelAttribute[ size ];
		}
	};
	
	private String name;
	
	public ModelAttribute( String name ){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public String getBindingType(){
		return "attribute";
	}
	
	public String getBindingValue(){
		return name;
	}
	
	public Binding getSegmentChild( int segment, int index ){
		return null;
	}
	
	public int getSegmentCount(){
		return 0;
	}
	
	public String getSegmentName( int segment ){
		return null;
	}
	
	public int getSegmentSize( int segment ){
		return 0;
	}
}
