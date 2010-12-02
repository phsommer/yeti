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

import tinyos.yeti.ep.parser.IASTModelElement;
import tinyos.yeti.search.model.group.DelegateBinder;
import tinyos.yeti.search.model.group.Group;
import tinyos.yeti.views.NodeContentProvider;

public class NodeDelegateBinder implements DelegateBinder{
	private NodeContentProvider provider;
	
	public NodeDelegateBinder( NodeContentProvider provider ){
		this.provider = provider;
	}
	
	public Object getDelegateInput( Group root ){
		if( root instanceof NodeGroupGenerator.RootGroup )
			return ((NodeGroupGenerator.RootGroup)root).getInput();
		return null;
	}

	public Object groupChildToContentNode( Object element ){
		if( element instanceof IASTModelElement )
			return provider.getElement( (IASTModelElement)element );
		return element;
	}
}
