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

import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.parser.IASTModelAttribute;
import tinyos.yeti.ep.parser.IASTModelElement;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

/**
 * An icon which can be translated into an {@link Image} or an
 * {@link ImageDescriptor}.
 * @author Benjamin Sigg
 *
 */
public class Icon{
	public static final IGenericFactory<Icon> FACTORY = new IGenericFactory<Icon>(){
		public Icon create(){
			return new Icon();
		}
		
		public void write( Icon value, IStorage storage ) throws IOException{
			storage.write( value.tags );
			storage.write( value.attributes );
		}
		
		public Icon read( Icon value, IStorage storage ) throws IOException{
			value.tags = storage.read();
			value.attributes = storage.read();
			return value;
		}
	};
	
	private TagSet tags;
	private IASTModelAttribute[] attributes;
	
	protected Icon(){
		// nothing
	}
	
	public Icon( TagSet tags ){
		this.tags = tags;
	}
	
	public Icon( TagSet tags, IASTModelAttribute[] attributes ){
		this.tags = tags;
		this.attributes = attributes;
	}
	
	public Icon( IASTModelElement element ){
		this( element.getTags(), element.getAttributes() );
	}
	
	public Icon( IDeclaration declaration ){
		this( declaration.getTags() );
	}
	
	public void setTags( TagSet tags ){
		this.tags = tags;
	}
	
	public TagSet getTags(){
		return tags;
	}
	
	public void setAttributes( IASTModelAttribute[] attributes ){
		this.attributes = attributes;
	}
	
	public IASTModelAttribute[] getAttributes(){
		return attributes;
	}
	
	/**
	 * Gets the descriptor of the image.
	 * @param filter whether filtering of decorations is allowed or not.
	 * @return the image
	 */
	public ImageDescriptor getImageDescriptor( boolean filter ){
		return getImageDescriptor( filter, false );
	}

	/**
	 * Gets the descriptor of the image.
	 * @param filter whether filtering of decorations is allowed or not.
	 * @param hideAttributeSpace if <code>true</code> then the attribute
	 * space is hidden if there are no attributes.
	 * @return the image
	 */
	public ImageDescriptor getImageDescriptor( boolean filter, boolean hideAttributeSpace ){
		TagSet tags = this.tags;
		if( filter )
			tags = IconDecorationFilter.filter( tags );
		
		if( hideAttributeSpace && (attributes == null || attributes.length == 0 ))
			return NesCIcons.getImageDescriptor( tags );
		else
			return NesCIcons.icons().getImageDescriptor( tags, attributes );
	}
	
	/**
	 * Gets the image of this icon.
	 * @param filter whether filtering of decorations is allowed or not
	 * @return the image
	 */
	public Image getImage( boolean filter ){
		return getImage( filter, false );
	}
	
	/**
	 * Gets the image of this icon.
	 * @param filter whether filtering of decorations is allowed or not
	 * @param hideAttributeSpace if <code>true</code> then the attribute
	 * space is hidden if there are no attributes.
	 * @return the image
	 */	
	public Image getImage( boolean filter, boolean hideAttributeSpace ){
		return NesCIcons.icons().get( getImageDescriptor( filter, hideAttributeSpace ) );
	}
}
