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
package tinyos.yeti.ep;

/**
 * Default implementation of {@link IMakeExtraDescription}.
 * @author Benjamin Sigg
 */
public class MakeExtraDescription implements IMakeExtraDescription{
	private String name;
	private String description;
	
	private String parameterName;
	private String parameterDescription;

	public MakeExtraDescription(){
		// nothing
	}
	
	public MakeExtraDescription( String name ){
		this.name = name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription( String description ){
		this.description = description;
	}

	public String getName(){
		return name;
	}

	public void setName( String name ){
		this.name = name;
	}
	
	public String getParameterDescription(){
		return parameterDescription;
	}
	
	public void setParameterDescription( String parameterDescription ){
		this.parameterDescription = parameterDescription;
	}

	public String getParameterName(){
		return parameterName;
	}
	
	public void setParameterName( String parameterName ){
		this.parameterName = parameterName;
	}

	public boolean hasParameter(){
		return parameterName != null;
	}
	
}
