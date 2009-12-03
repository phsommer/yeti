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
package tinyos.yeti.nesc12.parser;

import org.eclipse.core.resources.IProject;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.nodes.Inspectable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;

public class NesC12Inspector implements INesCInspector{
	private NesC12AST ast;
	private IProject project;
	
	public NesC12Inspector( IProject project, NesC12AST ast ){
		this.ast = ast;
		this.project = project;
	}
	
	public NesC12AST getAst(){
		return ast;
	}
	
	public IProject getProject(){
		return project;
	}
	
	public INesCNode getRoot(){
		if( ast == null )
			return null;
		
		ASTNode root = ast.getRoot();
		if( root instanceof TranslationUnit ){
			return ((TranslationUnit)root).resolve();
		}
		
		return null;
	}
	
	public INesCNode getNode( IASTModelNode node ){
		if( node instanceof INesCNode )
			return (INesCNode)node;
		
		if( node instanceof Inspectable ){
			if( ast != null ){
				BindingResolver bindings = ast.getBindingResolver();
				if( bindings != null ){
					return ((Inspectable)node).inspect( bindings );
				}
			}
		}
		
		return null;
	}
	
	public void open(){
		// nothing	
	}
	
	public void close(){
		// nothing
	}
}
