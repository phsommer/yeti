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
package tinyos.yeti.search.util;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

import tinyos.yeti.search.SearchPlugin;
import tinyos.yeti.utility.Icons;
import tinyos.yeti.utility.NesCImageDescriptor;

public class SearchIcons extends Icons{
	public static final String REMOVE_SELECTION = "remove.selection";
	public static final String REMOVE_ALL = "remove.all";
	public static final String LINE = "line";
	
	private static final SearchIcons ICONS = new SearchIcons();
	
	public SearchIcons(){
		super( createURL() );
		
		declareRegistryImage( REMOVE_SELECTION, "cross.png" );
		declareRegistryImage( REMOVE_ALL, "cross_two.png" );
		declareRegistryImage( LINE, "arrow_blue.png" );
	}
	
	public static SearchIcons icons(){
		return ICONS;
	}
	
	private static URL createURL(){
        String pathSuffix = "icons/";
        return SearchPlugin.getDefault().getBundle().getEntry( pathSuffix );
	}
	
	@Override
	protected ImageDescriptor getDecoration( String decoration ){
		return null;
	}

    @Override
    protected void setupDecoratable( NesCImageDescriptor image ){
        int width = image.getWidth();
	    int height = image.getHeight();
	    image.setSize( width+9+4, height );
	    image.setBaseLocation( 4, 0 );
    }
}
