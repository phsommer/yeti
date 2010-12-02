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
package tinyos.yeti.nesc.parser.language.elements;

import tinyos.yeti.nesc.scanner.Token;

public class EndpointElement extends Element{
	
	/**
	 * by parsing it's not determined if its a 
	 * external specification element or a component
	 * name from the component-list
	 * 
	 * excerpt from page 12 from nesc-ref.pdf
	 * 
	 * 		A compile-time error occurs if the identifier-path of 
	 * 		an endpoint is not of one the three following
	 *		forms:
	 *
	 * 		X, where X names an external specification element.
	 * 		
	 * 		K where K is a some component name from the component-list. 
	 * 		This form is used in implicit connections, discussed 
	 * 		in Section 7.3. Note that this form cannot be 
	 * 		used when parameter values are specified.
	 */
	String componentOrExternalSpezName = null;
	
	String specificationElementName = null;
	String componentElementName = null;
	
	public String interfaceResolved = null;

	/**
	 * The returned component name is potentially a renamed
	 * component name
	 * @return string (re)named component name
	 */
	public String getComponentElementName() {
		if (componentOrExternalSpezName == null) {
			return componentElementName;
		} else {
			return componentOrExternalSpezName;
		}
	}
	
	public String getSpecificationElementName() {
		return specificationElementName;
	}


	public EndpointElement(Token token) {
		super(token);
	}

	public EndpointElement(String string, Token token, Token token2) {
		super(string,token,token2);
	}

	public EndpointElement(Token token, Token token2) {
		super(token,token);
	}

	public void setComponentOrExternalSpecificationName(String string) {
		this.componentOrExternalSpezName = string;

	}

	public void setText(String string) {
		this.name = string;
	}

	public void setSpecificationElementName(String string) {
		if (string == null) {
			this.specificationElementName = null;
		} else if (string.equals("")) {
			this.specificationElementName = null;
		} else {
			this.specificationElementName = string;	
		}
		
	}

	public void setComponentElementName(String string) {
		componentElementName = string;
	}
	
	public void setResolvedInterface(String interfaceName) {
		this.interfaceResolved = interfaceName;
	}
	
}
