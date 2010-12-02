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
package tinyos.yeti.search.ui.event;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.SearchResultEvent;

import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.model.ProjectModel;

public class ItemsAddedSearchResultEvent extends SearchResultEvent{
	private IASTModel model;
	private ProjectModel project;
	private IASTModelNode[] items;
	
	public ItemsAddedSearchResultEvent( ISearchResult searchResult, IASTModel model, ProjectModel project, IASTModelNode[] items ){
		super( searchResult );
		this.model = model;
		this.project = project;
		this.items = items;
	}
	
	public IASTModel getModel(){
		return model;
	}
	
	public ProjectModel getProject(){
		return project;
	}
	
	public IASTModelNode[] getItems(){
		return items;
	}
}
