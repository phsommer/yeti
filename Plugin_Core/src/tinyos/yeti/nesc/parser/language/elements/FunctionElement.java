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
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.nesc.parser.language.SemanticError;
import tinyos.yeti.nesc.scanner.Token;

public class FunctionElement extends Element {
	
	Hashtable storageClassSpecifierElements = new Hashtable(2);
	Hashtable typeQualifiers = new Hashtable(2);
	TypeSpecifierElement returnType;
	CompoundElement compoundStatement = null;
		
	public static final int TASK = 0;
	public static final int COMMAND = 1;
	public static final int EVENT = 2;
	public static final int C = 3;
	
	private int type = 3;
	
	private boolean async = false;
	boolean defaultImpl = false;
	private DirectDeclaratorElement declarator = null;
	
	/** 
	 * returns the function type, type is either
	 * TASK, COMMAND, EVENT or C
	 * @return 
	 */
	public int getType() {
		return type;
	}
	
	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		if (returnType != null) returnType.updatePosition(region);
		if (compoundStatement != null) compoundStatement.updatePosition(region);
		
		Collection elements = storageClassSpecifierElements.values();
		Iterator iter = elements.iterator();
		while(iter.hasNext()) {
			((Element) iter.next()).updatePosition(region);
		}
				
		elements = typeQualifiers.values();
		iter = elements.iterator();
		while(iter.hasNext()) {
			((Element) iter.next()).updatePosition(region);
		}
		
		if (declarator != null) declarator.updatePosition(region);
	}
	
	public DirectDeclaratorElement getDeclarator() {
		return declarator;
	}
	
	public Position getPositionsForOutline() {
		return declarator.getPositionForOutline();
	}
	
	public void setDeclarator(DirectDeclaratorElement i) {
		this.declarator = i;
		addSpecifiers(i);
	}
	
	public Position getPositionForOutline() {
		return declarator.getPositionForOutline();
	}
	
	public void setDeclarationSpecifiers(ArrayList l) {
		Iterator iter = l.iterator();
		while(iter.hasNext()) {
			addSpecifiers((Element) iter.next());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void addSpecifiers(Element e) {	
		if (e instanceof StorageClassSpecifierElement) {
			storageClassSpecifierElements.put(e.name,e);
			if (e.name.equalsIgnoreCase("default")) {
				defaultImpl = true;
			} else if (e.name.equalsIgnoreCase("command")) {
				type = FunctionElement.COMMAND;
			} else if (e.name.equalsIgnoreCase("event")) {
				type = FunctionElement.EVENT;
			} else if (e.name.equalsIgnoreCase("task")) {
				type = FunctionElement.TASK;
			} else if (e.name.equalsIgnoreCase("async")) {
				async = true;
			}
		} else if (e instanceof TypeQualifier) {
			typeQualifiers.put(e.name,e);
		} else if (e instanceof TypeSpecifierElement) {  // return types
			returnType = (TypeSpecifierElement) e;
		} else if (e instanceof KeywordElement) {
			
		}
	}
	
	/**
	 * Checks done: If return type is given, check if compound statement hast
	 * return call with statement
	 */
	@SuppressWarnings("unchecked")
	public SemanticError[] getSemanticWarnings( ProjectTOS project ) {
		boolean hasReturn = false;
		boolean hasReturnValue = false;
		boolean shouldReturn = (returnType!=null)&&(!returnType.getName().equals("void"));
		
		Element returnElem = null;
		ArrayList errors = new ArrayList();

		// System.out.println("1");
		if (compoundStatement != null) {
			// Last Statement could be if .. else statement
			StatementElement t = null;
			if (compoundStatement.children.size() > 0) { 
			 t = (StatementElement) compoundStatement.children
					.get(compoundStatement.children.size() - 1);
			}
			//System.out.println(this.getLabel(null) + " Should return: "+shouldReturn+" Last Element: "+ t.getClass());
			// if (t instanceof SelectionStatementElement){
			// SelectionStatementElement ste = (SelectionStatementElement)t;
			// t = ste.elseStmt;
			// if (t != null) System.out.println(this.getLabel(null)+" Last
			// Element: "+t.getClass());
			// }
			while (t != null) {
				if (t instanceof SelectionStatementElement) {
					SelectionStatementElement ste = (SelectionStatementElement) t;
//					System.out.println("Slection: else ..");
					t = ste.elseStmt;
				} else if (t instanceof CompoundElement) {
//					System.out.println("Slection: compound ..");
					int count = ((CompoundElement) t).children.size();
					if (count > 0) {
						t = (StatementElement) ((CompoundElement) t).children
						.get(((CompoundElement) t).children.size() - 1);
					} else {
						t = null;
					}
				} else if (t instanceof JumpStatementElement) {
					// System.out.println("4");
					hasReturn = ((JumpStatementElement) t).isReturn;
					hasReturnValue =  ((JumpStatementElement) t).hasReturnExpression;
					returnElem = t;
					t = null;
				} else {
					t = null;
				}
			}

		} else {
			// System.out.println("1e");
			// is probably an interface definition...
		}

		if (hasReturn) {
			if (shouldReturn) {
				// bravo
				
			} else {
				if (hasReturnValue) {
//					System.out.println("Return parameter not expected..");
					SemanticError se = new SemanticError("Return parameter not expected..",returnElem);
					se.severity = IMarker.SEVERITY_WARNING;
					errors.add(se);
				}
			}
		} else {
			if (shouldReturn) {
//				System.out.println("No value returned..");
				SemanticError se = new SemanticError("No value returned..",compoundStatement);
				se.severity = IMarker.SEVERITY_WARNING;
				errors.add(se);
			} else {
				
			}
		}

		if (errors.size() > 0) {
			return (SemanticError[]) errors.toArray(new SemanticError[errors
					.size()]);
		}
		return null;
	}


	public FunctionElement(Element element, Element element2) {
		super("",element,element2);
		image = null;
	}

	public FunctionElement(String string, Element element, Element element2) {
		super("",element,element2);
		image = null;
	}

	public FunctionElement(ArrayList list, Element element) {
		super("",list,element);
		image = null;
	}
	

	public FunctionElement(Token object) {
		super(object);
	}

	public String getLabel(Object o) {
		if (declarator != null) {
			return declarator.getLabel(null);
		} else {
			return this.name;
		}
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		if (image != null)
			return image;
		if (type == FunctionElement.COMMAND) {
			if (async) {
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMMAND_ASYNC);
			}
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_COMMAND);
		}
		if (type == FunctionElement.EVENT) {
			if (async) {
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_EVENT_ASYNC);
			}
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_EVENT);
		}
		if (type == FunctionElement.TASK) {
			if (async) {
				return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_TASK_ASYNC);
			}
			return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_TASK);
		}

		return NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_CFUNCTION);
	}
	
	public boolean isFoldable() {
		return true;
	}

	public void setCompoundStatement(Element element) {
		this.compoundStatement = (CompoundElement) element;
		
	}
}
