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
package tinyos.yeti.ep.parser;

import org.eclipse.jface.text.TextPresentation;

import tinyos.yeti.utility.Icon;

/**
 * Default implementation of {@link IHoverInformation}
 * @author Benjamin Sigg
 */
public class HoverInformation implements IHoverInformation{
	private String title;
	private String content;
	private String html;
	private TextPresentation contentPresentation;
	private Icon icon;
	
	public HoverInformation( Icon icon, String title, String content, TextPresentation contentPresentation ){
		setIcon( icon );
		setTitle( title );
		setContent( content );
		setContentPresentation( contentPresentation );
	}
	
	public HoverInformation( Icon icon, String title, String html ){
		setIcon( icon );
		setTitle( title );
		setHTML( html );
	}
	
	public void setContent( String content ){
		this.content = content;
	}
	
	public String getContent(){
		return content;
	}

	public void setContentPresentation( TextPresentation contentPresentation ){
		this.contentPresentation = contentPresentation;
	}
	
	public TextPresentation getContentPresentation(){
		return contentPresentation;
	}

	public void setIcon( Icon icon ){
		this.icon = icon;
	}
	
	public Icon getIcon(){
		return icon;
	}

	public void setTitle( String title ){
		this.title = title;
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setHTML( String html ){
		this.html = html;
	}
	
	public String getHTML(){
		return html;
	}
}
