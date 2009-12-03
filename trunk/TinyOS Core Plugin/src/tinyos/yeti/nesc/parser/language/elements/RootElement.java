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



public class RootElement extends Element {

	public RootElement() {
		super("root",0,0,0);
	}

	public RootElement(Element e1, Element e2) {
		super("root",e1,e2);
	}

	public RootElement(Element e) {
		super("root",e);
	}
	

//	public ArrayList getCompletionProposals(String prefix, String indent, int offset) {
//		ArrayList al = null;
//		if (hasChildren() == false) {
//			al = new ArrayList();
//			// includes_list interface		
//			// includes_list module			
//			// includes_list configuration 	
//			// interface		
//			// al.add(InterfaceElement.getTemplate(prefix,indent,offset));
//			// module						
//			// configuration
//			
//			
//		}
//		
//		return al;		
//	}
	
	public RootElement(String string, int offset, int length, int line) {
		super(string,offset,length,line);
	}

	@Override
	public String getLabel(Object o) {
		return "";
	}

}
