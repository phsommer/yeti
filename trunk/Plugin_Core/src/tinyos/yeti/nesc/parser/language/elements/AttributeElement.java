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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.reconciler.DirtyRegion;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.nesc.parser.NesCparser;
import tinyos.yeti.nesc.parser.language.SemanticError;
import tinyos.yeti.nesc.scanner.ITokenInfo;
import tinyos.yeti.nesc.scanner.Token;

public class AttributeElement extends Element{
	
	boolean openBrackets = false;
	boolean closingBrackets = false;
	boolean spacer = true;
	boolean function_spacer= true;
	
	ArrayList syntacticErrors = new ArrayList();
	
	ArrayList wordAttribute = new ArrayList();
	ArrayList functionAttribute = new ArrayList();
	Hashtable functionParameters = new Hashtable();
	
	/** Permitted Word Attributes (nesC 1.1) */
	List permittedWordList = Arrays.asList(new String[]{"C","spontaneous"});
	
	/** Permitted Functions (nesC 1.1) */
	List permittedFunctionList = Arrays.asList(new String[]{"combine"});

	Token currentFunctionName = null;
	
	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		
		if (currentFunctionName != null) currentFunctionName.updatePosition(region);
		
		Iterator iter =  wordAttribute.iterator();
		while(iter.hasNext()) {
			((Token) iter.next()).updatePosition(region);
		}
		iter = functionAttribute.iterator();
		while(iter.hasNext()) {
			((Token) iter.next()).updatePosition(region);
		}
		
		iter = functionParameters.values().iterator();
		while(iter.hasNext()) {
			((Token) iter.next()).updatePosition(region);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void addFunctionArgument(Token t) {
		if (currentFunctionName != null) {
			functionParameters.put(currentFunctionName,t);
		}
	}
	
	public void addSpacer() {
		spacer = true;
	}
	
	/**
	 * method for scanner to report an error
	 *
	 */
	public void setError() {

	}
	
	/**
	 * finishes function declaration
	 *
	 */
	@SuppressWarnings("unchecked")
	public void finishFunction() {
		functionAttribute.add(currentFunctionName);
		currentFunctionName = null;
	}
	
	public void setOpenBrackets() {
		openBrackets = true;
	}
	
	public AttributeElement(ITokenInfo it) {
		super(it);
	}

	@SuppressWarnings("unchecked")
	public void addWord(Token t) {
		if (spacer == false ) {
			// hack.. is a syntactic error.. but parser-grammar would
			// be to complex to handle also __attribute__ element
			syntacticErrors.add(new SemanticError("Comma expected",t));
		}
		spacer = false;
		wordAttribute.add(t);
	}
	
	public void setClosingBrackets() {
		closingBrackets = true;
	}

	
	@SuppressWarnings("unchecked")
	public void addFunction(String fname, int line, int offset, int endoffset) {
		//return new Token(value, yytext(), yyline, yychar, yychar+ yytext().length());
		Token t = new Token(NesCparser.IDENTIFIER,fname,line, offset, endoffset);
		if (spacer == false ) {
			// hack.. is a syntactic error.. but parser-grammar would
			// be to complex to handle also __attribute__ element
			syntacticErrors.add(new SemanticError("Comma expected",t));
		}
		spacer = false;
		currentFunctionName = t;
		
	}
	
	public void addFunctionArgumentDelimiter() {
		function_spacer = true;
	}
	
	@SuppressWarnings("unchecked")
	public SemanticError[] getSemanticWarnings( ProjectTOS project ) {
		ArrayList<SemanticError> errors = new ArrayList<SemanticError>();
		
		Iterator iter = wordAttribute.iterator();
			
		// test valid words
		while (iter.hasNext()) {
			Token t = (Token) iter.next();
			if (!permittedWordList.contains(t.getText())) {
				SemanticError err = new SemanticError("Only \"C\" and \"spontaneous\" and \"combine(fname)\" are permitted by definition",t);
				errors.add(err);
			}
		}
		// test valid function names
		iter = functionAttribute.iterator();
		while (iter.hasNext()) {
			Token t= (Token) iter.next();
			if (!permittedFunctionList.contains(t.getText())) {
				SemanticError err = new SemanticError("Only \"C\" and \"spontaneous\" and \"combine(fname)\" are permitted by definition",t);
				errors.add(err);
			}
		}
		
		errors.addAll(syntacticErrors);
		
		
		return (SemanticError[]) errors.toArray(new SemanticError[errors.size()-1]);		
	}

}
