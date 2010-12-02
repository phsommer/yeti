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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.reconciler.DirtyRegion;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.nesc.scanner.Token;

public class DeclarationElement extends Element implements ISpecificationElement {
	
	public static int COMMAND = 1;
	public static int EVENT = 2;
	public static int TASK = 3;
	
	Hashtable storageClassSpecifierElements = new Hashtable(3);
	Hashtable typeQualifiers = new Hashtable(2);
	Hashtable typeSpecifierElements = new Hashtable(2);
	Hashtable keywordElements = new Hashtable(2);
	
	boolean defaultImpl = false;
	boolean command = false;
	boolean event = false;
	boolean task = false;
	boolean async = false;
	
	String skeleton = null;

	// can also be used inside used/provides clause
	private boolean provides = false;
	private boolean uses = false;
	
	DirectDeclaratorElement directDeclarator = null;
	InitDeclaratorElement initDeclarator = null;
	
	/** 
	 * COMMAND / EVENT or TASK 
	 * @return
	 */
	public int getType() {
		if (command) return COMMAND;
		if (event) return EVENT;
		if (task) return TASK;
		
		return 0;
	}
	
	public boolean provides() {
		return provides;
	}
	
	public boolean uses() {
		return uses;
	}
	
	
	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		Iterator iter = storageClassSpecifierElements.values().iterator();
		while (iter.hasNext()) {
			((Element) iter.next()).updatePosition(region);
		}
		
		iter =  typeQualifiers.values().iterator();
		while (iter.hasNext()) {
			((Element) iter.next()).updatePosition(region);
		}
		
		iter = typeSpecifierElements.values().iterator();
		while (iter.hasNext()) {
			((Element) iter.next()).updatePosition(region);
		}
		
		iter = keywordElements.values().iterator();
		while (iter.hasNext()) {
			((Element) iter.next()).updatePosition(region);
		}
		
		if (directDeclarator != null) directDeclarator.updatePosition(region);
		if (initDeclarator != null) initDeclarator.updatePosition(region);
		
	}
	
	public DirectDeclaratorElement getDirectDeclarator() {
		if (initDeclarator!=null) {
			return initDeclarator.directDeclarator;
		}
		return null;
	}
	
	public String getFunctionName() {
	    if( directDeclarator != null )
	        return directDeclarator.identifier;
	    
		if (initDeclarator != null) {
			DirectDeclaratorElement d = initDeclarator.directDeclarator;
			if (d != null) {
				return d.identifier;
			}
		}
		return null;
	}
	
	public ImageDescriptor getImageDescriptor(Object object)	{
		if (image != null) return image;
		if (command) {
			if (async) {
				if (provides) {
					return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMMAND_ASYNC_PROVIDES);
				} else if (uses) {
					return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMMAND_ASYNC_USES);
				}
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMMAND_ASYNC);
			}
			if (provides) {
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMMAND_PROVIDES);
			} else if (uses) {
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMMAND_USES);
			}
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMMAND);
		}
		if (event) {
			if (async) {
				if (uses) {
					return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_EVENT_ASYNC_USES);
				} else if (provides) {
					return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_EVENT_ASYNC_PROVIDES);
				}
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_EVENT_ASYNC);
			}
			
			if (uses) {
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_EVENT_USES);
			} else if (provides) {
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_EVENT_PROVIDES);
			}
			
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_EVENT);
		}
		if (task) {
			if (async) {
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_TASK_ASYNC);
			}
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_TASK);
		}
		return null;	
	}
		
	public DeclarationElement(String aName, int offset, int length, int line) {
		super(aName, offset, length, line);
		image = null;
	}
	
	
	public DeclarationElement(ArrayList list, Token token) {
		super("",list,token);
		image = null;
	}

	public String getLabel(Object o) {
		if (directDeclarator != null) {
			return directDeclarator.getLabel(null);
		} else if (initDeclarator != null){
			return initDeclarator.getLabel(null);
		}  
		return "";
	}
	
	public void addInitDeclarator(InitDeclaratorElement e) {
		this.initDeclarator = e;
		this.length = e.length;
		this.offset = e.offset;
	}
	

	public int getParameterCount(){
	    if( initDeclarator == null )
	        return 0;
	    
	    if( initDeclarator.directDeclarator == null )
	        return 0;
	    
	    return initDeclarator.directDeclarator.getParameterCount();
	}
	
	@SuppressWarnings("unchecked")
	public void addSpecifiers(Element e) {	
		this.line = e.line;
		if (e instanceof StorageClassSpecifierElement) {
			storageClassSpecifierElements.put(e.name,e);
			
			if (e.name.equalsIgnoreCase("command")) {
				command = true;
			} else if (e.name.equalsIgnoreCase("event")) {
				event = true;
			} else if (e.name.equalsIgnoreCase("task")) {
				task = true;
			}
		} else if (e instanceof TypeQualifier) {
			typeQualifiers.put(e.name,e);
		} else if (e instanceof TypeSpecifierElement) {
			typeSpecifierElements.put(e.name,e);
		} else if (e instanceof KeywordElement) {
			keywordElements.put(e.getName(),e);
			if (e.name.equalsIgnoreCase("default")) {
				defaultImpl = true;
			} else if (e.name.equalsIgnoreCase("async")) {
				async = true;
			}
		}
	}
	
	public String getSkeleton(String renamedInterface) {
		//if (skeleton != null) return skeleton;
		
		String result = "\t";
		if (defaultImpl) result += "default ";
		if (command) result += "command ";
		if (event) result += "event ";
		if (task) result += "task ";
		if (async) result += "async ";
		
		// return param
		boolean hasreturn = false;
		String returnType = null;
		Enumeration enumer = typeSpecifierElements.elements();
		if (enumer.hasMoreElements())  {
			Element elem = (Element) enumer.nextElement();
			returnType = elem.getName();
			result += returnType+" ";
			hasreturn=true;
		}
		if (hasreturn == false) result += "void ";
		
		if (renamedInterface!=null) {
			result += renamedInterface+".";
		}
		
		result += initDeclarator.directDeclarator.identifier +"(";
		
		if (initDeclarator.directDeclarator.parameterTypeList!= null) {
		Iterator iter = initDeclarator.directDeclarator.parameterTypeList.iterator();
		int i=0;
		while (iter.hasNext()) {
			ParameterDeclarationElement e = (ParameterDeclarationElement) iter.next();
			TypeSpecifierElement tse = (TypeSpecifierElement) e.declarationSpecifiers.get(0);
			PointerElement pe = e.declarator.pointer;
			if (pe != null) {
				result += tse.getName()+pe.getName();
			} else {
				result += tse.getName();
			}
			
			result += " param"+(i++);
			if (iter.hasNext()) result +=", ";
		}
		}
		result += ") {" + NEWLINE;
		
		if (hasreturn) {
			result += "\t\treturn ";
			if (returnType.equals("result_t")) {
				result += "SUCCESS;";
			} else {
				result += ";";
			}
		}
		result += NEWLINE + "\t}";
		
		skeleton = result;
		return result;
	}

	public void setProvides() {
		this.provides = true;
		
	}

	public void setUses() {
		this.uses = true;
		this.provides = false;
	}

	public String getRenamed() {
		return getFunctionName();
	}
	@Override
	public String getName() {
		return getFunctionName();
	}

	public boolean isProvides() {
		return provides;
	}

}
