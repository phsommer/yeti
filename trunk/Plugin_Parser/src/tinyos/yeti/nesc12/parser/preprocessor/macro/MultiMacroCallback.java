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
package tinyos.yeti.nesc12.parser.preprocessor.macro;

import tinyos.yeti.preprocessor.MacroCallback;
import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class MultiMacroCallback implements MacroCallback{
	private MacroCallback[] callbacks;
	
	/**
	 * Creates a new {@link MultiMacroCallback}.
	 * @param callbacks the callback array, <code>null</code> entries
	 * will be ignored
	 */
	public MultiMacroCallback( MacroCallback... callbacks ){
		int count = 0;
		for( MacroCallback callback : callbacks ){
			if( callback != null ){
				count++;
			}
		}
		this.callbacks = new MacroCallback[ count ];
		int index = 0;
		for( MacroCallback callback : callbacks ){
			if( callback != null ){
				this.callbacks[ index++ ] = callback;
			}
		}
	}
	
	public void declared( Macro macro ){
		for( MacroCallback callback : callbacks ){	
			callback.declared( macro );
		}
	}
	
	public void undeclared( String name, Macro macro ){
		for( MacroCallback callback : callbacks ){
			callback.undeclared( name, macro );
		}
	}
	
	public void applied( Macro macro, PreprocessorElement identifier ){
		for( MacroCallback callback : callbacks ){
			callback.applied( macro, identifier );
		}
	}
}
