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


/**
 * A {@link TagDescription} describes what a {@link Tag} is good for, 
 * this is done in a human readable way.
 * @author Benjamin Sigg
 */
public class TagDescription{
	private String name;
	private String description;
	
	/** marks a tag that would be an good tag to be used on some search page */
	private boolean toplevelSearch;
	
	public TagDescription( String name, String description, boolean toplevelSearch ){
		this.name = name;
		this.description = description;
		this.toplevelSearch = toplevelSearch;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public boolean isToplevelSearch(){
		return toplevelSearch;
	}
}
