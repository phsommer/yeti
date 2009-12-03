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
package tinyos.yeti.search.model;

import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.parser.ASTNodeFilterFactory;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;

/**
 * A set of nodes that were found in a specific project.
 * @author Benjamin Sigg
 */
public class ProjectNodes{
	private ProjectTOS project;
	
	private Map<IASTModelPath, IASTModelNode> nodes = new HashMap<IASTModelPath, IASTModelNode>();
	
	private IASTModel model;
	
	public ProjectNodes( ProjectTOS project ){
		this.project = project;
		model = project.getModel().newASTModel();
	}
	
	public boolean add( IASTModelNode node ){
		IASTModelPath logical = node.getLogicalPath();
		if( nodes.containsKey( logical ))
			return false;
		
		
		nodes.put( logical, node );
		model.addNodes( new IASTModelNode[]{ node });
		
		return true;
	}
	
	public boolean remove( IASTModelNode node ){
		IASTModelNode removed = nodes.remove( node.getLogicalPath() );
		if( removed != null ){
			model.removeNodes( ASTNodeFilterFactory.is( removed ) );
			return true;
		}
		return false;
	}
	
	public boolean isEmpty(){
		return nodes.isEmpty();
	}
	
	public ProjectTOS getProject(){
		return project;
	}
	
	public IASTModel toModel(){
		return model;
	}
}
