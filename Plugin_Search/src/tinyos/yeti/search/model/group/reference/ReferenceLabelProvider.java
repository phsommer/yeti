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
package tinyos.yeti.search.model.group.reference;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.search.model.ASTReferenceSearchResult;
import tinyos.yeti.search.util.SearchIcons;

public class ReferenceLabelProvider implements ILabelProvider{
	private Image image = SearchIcons.icons().get( SearchIcons.LINE, true );
	
	public Image getImage( Object element ){
		if( element instanceof ASTReferenceSearchResult.Result ){
			return image;
		}
		return null;
	}

	public String getText( Object element ){
		if( element instanceof ASTReferenceSearchResult.Result ){
			ASTReferenceSearchResult.Result result = (ASTReferenceSearchResult.Result)element;
			IFileRegion region = result.getReference().getSource();
			return "Line " + region.getLine() + ", '" + result.getNode().getLabel() + "' in " + region.getParseFile().getName(); 
		}

		return null;
	}

	public void addListener( ILabelProviderListener listener ){
		// ignore
	}

	public void dispose(){
	}

	public boolean isLabelProperty( Object element, String property ){
		return true;
	}

	public void removeListener( ILabelProviderListener listener ){
		// ignore
	}
}
