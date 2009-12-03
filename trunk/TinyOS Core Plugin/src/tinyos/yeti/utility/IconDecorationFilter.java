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
package tinyos.yeti.utility;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.preferences.PreferenceConstants;

public class IconDecorationFilter{
	private static IconDecorationFilter filter;
	
	private static IconDecorationFilter filter(){
		if( filter == null ){
			synchronized( IconDecorationFilter.class ){
				filter = new IconDecorationFilter();
			}
		}
		return filter;
	}
	
	private boolean setting = true;
	
	private IconDecorationFilter(){
		TinyOSPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(
				new IPropertyChangeListener(){
					public void propertyChange( PropertyChangeEvent event ){
						if( event.getProperty().equals( PreferenceConstants.ICONS_ALWAYS_DECORATED )){	
							update();
						}
					}
				});
		update();
	}
	
	private void update(){
		setting = TinyOSPlugin.getDefault().getPreferenceStore().getBoolean( PreferenceConstants.ICONS_ALWAYS_DECORATED );
	}
	
	public static boolean doFilter(){
		return !filter().setting;
	}
	
	public static TagSet filter( TagSet set ){
		if( set == null )
			return null;
		
		if( doFilter() ){
			set = set.copy();
			set.remove( TinyOSPlugin.getDefault().getParserFactory().getDecoratingTags() );  
		}
		return set;
	}
}
