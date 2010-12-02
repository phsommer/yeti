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
package tinyos.yeti.editors.outline.filter;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.outline.IOutlineFilter;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelPath;

public class FromThisFileOnly implements IOutlineFilter{
	private NesCEditor editor;
	
	private boolean fileResolved = false;
	private IParseFile file;
	
	public void setEditor( NesCEditor editor ){
		this.editor = editor;
	}
	
	public boolean include( IASTModelNode node ){
		return include( node.getParseFile() );
	}

	public boolean include( IASTModelNodeConnection connection ){
		if( connection.isReference() ){
			IASTModelPath path = connection.getReferencedPath();
			if( path != null ){
				return include( path.getParseFile() );
			}
		}
		return include( connection.getParseFile() );
	}	
	
	private boolean include( IParseFile file ){
		if( file == null )
			return true;
		
		if( !fileResolved ){
			fileResolved = true;
			this.file = editor.getParseFile();
		}
		
		if( this.file == null )
			return true;
		
		return file.equals( this.file );
	}
}
