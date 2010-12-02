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
package tinyos.yeti.search.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.ISearchResultPage;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.parser.ASTNodeFilterFactory;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.search.model.ASTModelNodeSearchResult;
import tinyos.yeti.search.model.ISearchTreeContentProvider;
import tinyos.yeti.search.model.group.DelegateBinder;
import tinyos.yeti.search.model.group.Group;
import tinyos.yeti.search.model.group.GroupGenerator;
import tinyos.yeti.search.model.group.declaration.FileGroupGenerator;
import tinyos.yeti.search.model.group.declaration.GroupNodeContentProvider;
import tinyos.yeti.search.model.group.declaration.NodeDelegateBinder;
import tinyos.yeti.search.model.group.declaration.ProjectGroupGenerator;
import tinyos.yeti.search.model.group.declaration.SearchNodeContentProvider;
import tinyos.yeti.views.NodeContentProvider;
import tinyos.yeti.views.NodeLabelProvider;

/**
 * This page shows the contents of a {@link ASTModelNodeSearchResult}.
 * @author besigg
 */
public class SearchDeclarationResultPage extends SearchResultPage<IASTModelNode> implements ISearchResultPage{
	public static final String GROUP = "SearchResultPage.group";
	
	public SearchDeclarationResultPage(){
		super( "search.declaration" );
		addGroup( "project", "Group by project", ProjectGroupGenerator.getProjectImageDescriptor(), new ProjectGroupGenerator() );
		addGroup( "file", "Group by file", NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_NESC_FILE ), new FileGroupGenerator() );
		addGroup( "null", "Do not group", NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_PLAIN_PAGE ), null );
	}
	
	@SuppressWarnings("unchecked")
	public List<IASTModelNode> getSelection(){
		if( getResult() == null )
			return new ArrayList<IASTModelNode>();
		
		ISelection selection = getViewer().getSelection();
		List<IASTModelNode> result = new ArrayList<IASTModelNode>();
		
		if( selection instanceof StructuredSelection ){
			Iterator<Object> iterator = ((StructuredSelection)selection).iterator();
			
			while( iterator.hasNext() ){
				Object next = iterator.next();
				collectSelection( next, result );
			}
		}
		
		return result;
	}
	
	private void collectSelection( Object node, List<IASTModelNode> selection ){
		if( node instanceof Group.Wrapper ){
			node = ((Group.Wrapper)node).getNode();
		}
		
		if( node instanceof Group ){
			Group group = (Group)node;
			for( Group.Wrapper child : group ){
				collectSelection( child, selection );
			}
			return;
		}
		
		DelegateBinder binder = getBinder();
		if( binder != null ){
			node = binder.groupChildToContentNode( node );
		}
		
		if( node instanceof NodeContentProvider.Element ){
			if( ((NodeContentProvider.Element)node).getParent() == null ){
				IASTModelNode model = ((NodeContentProvider.Element)node).getNode();
				if( node != null ){
					selection.add( model );
				}
			}
		}
	}
	
	@Override
	public ASTModelNodeSearchResult getResult(){
		return (ASTModelNodeSearchResult)super.getResult();
	}

	@Override
	protected DelegateBinder createBinder( ITreeContentProvider provider,
			GroupGenerator<?, IASTModelNode> generator ){
		return new NodeDelegateBinder( (NodeContentProvider)provider );
	}

	@Override
	protected ISearchTreeContentProvider createContentProvider( GroupGenerator<?, IASTModelNode> generator ){
		SearchNodeContentProvider provider;
		if( generator == null )
			provider = new SearchNodeContentProvider( ASTNodeFilterFactory.all() );
		else
			provider = new GroupNodeContentProvider( ASTNodeFilterFactory.all(), generator );
		
		/*int index = 0;

		ASTModelNodeSearchResult result = getResult();
		if( result != null ){
			for( ProjectNodes node : result.getNodes() ){
				IASTModel model = node.toModel();
				if( model.getSize() > 0 ){
					provider.setBackup( index++, node.getProject().getModel() );
				}
			}
		}*/
		
		return provider;
	}
	
	@Override
	protected ILabelProvider createLabelProvider(){
		return new NodeLabelProvider();
	}

	@Override
	protected Object createInput(){
		/*if( result instanceof ASTModelNodeSearchResult ){
			ASTModelNodeSearchResult nodeResult = (ASTModelNodeSearchResult)result;
			
			List<IASTModel> models = new ArrayList<IASTModel>();
			
			for( ProjectNodes node : nodeResult.getNodes() ){
				IASTModel model = node.toModel();
				if( model.getSize() > 0 ){
					models.add( model );
				}
			}
			
			return models.toArray( new IASTModel[ models.size() ] );
		}
		else{
			return new IASTModel[]{};
		}*/
		return new IASTModel[]{};
	}

	@Override
	protected boolean hasSelection(){
		return !getSelection().isEmpty();
	}
	
	public void doRemoveAll(){
	 	ASTModelNodeSearchResult result = getResult();
		if( result != null ){
			result.clear();
		}
	}
	
	public void doRemoveSelection(){
		List<IASTModelNode> selection = getSelection();
		ASTModelNodeSearchResult result = getResult();
		
		if( !selection.isEmpty() ){
			result.removeAll( selection );
		}
	}
}
