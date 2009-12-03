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

import java.io.IOException;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

/**
 * Default implementation of {@link IASTReference}.
 * @author Benjamin Sigg
 */
public class ASTReference implements IASTReference{
	public static final IGenericFactory<ASTReference> FACTORY = new IGenericFactory<ASTReference>(){
		public ASTReference create(){
			return new ASTReference();
		}
		public ASTReference read( ASTReference value, IStorage storage )
				throws IOException{
			
			value.source = storage.read();
			value.target = storage.read();
			return value;
		}
		public void write( ASTReference value, IStorage storage ) throws IOException{
			storage.write( value.source );
			storage.write( value.target );
		}
	};
	
	private IFileRegion source;
	private IASTModelPath target;

	public ASTReference(){
		// nothing
	}
	
	public ASTReference( IFileRegion source, IASTModelPath target ){
		this.source = source;
		this.target = target;
	}
	
	public IFileRegion getSource(){
		return source;
	}

	public void setSource( IFileRegion source ){
		this.source = source;
	}
	
	public IASTModelPath getTarget(){
		return target;
	}
	
	public void setTarget( IASTModelPath target ){
		this.target = target;
	}
	
	@Override
	public String toString(){
		return "reference: " + source.getParseFile().getName() + " [" + source.getOffset() + ", " + source.getLength() + "] -> " + target.getParseFile().getName(); 
	}
}
