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
package tinyos.yeti.views; 

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.Debug;
import tinyos.yeti.ep.parser.IASTModelElement;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.views.NodeContentProvider.Element;
import tinyos.yeti.views.NodeContentProvider.ModelInfo;

/**
 * A helper class to update the tree by reusing as many {@link Element}s
 * as possible.
 * @author Benjamin Sigg
 */
public class NodeTreeRefiller {
	/** the old roots of the tree */
	private Element[] oldRoots;
	
	/** the viewer which shows the tree */
	private NodeContentProvider provider;
	
	public NodeTreeRefiller( NodeContentProvider provider, Element[] oldRoots ){
		this.provider = provider;
		this.oldRoots = oldRoots;
	}
	
	/**
	 * Refills the whole tree with the nodes from <code>models</code>. This 
	 * method starts its own thread and will return immediately.
	 * @param model the new models, not <code>null</code>
	 */
	public void refill( final ModelInfo[] models ){
		Job job = new Job( "Update Node Tree" ){
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if( provider.isDisposed() ){
					return Status.OK_STATUS;
				}
				
				final List<Element> newRoots = new ArrayList<Element>();
				
				Debug.enter();
				
				for( ModelInfo info : models ){
					IASTModelNode[] roots = info.model.getNodes( provider.getRootFilter() );
				
					int[] occurrence = buildOccurrenceMap( roots );
					for( int i = 0; i < roots.length; i++ ){
						IASTModelNode root = roots[i];
						Element element = search( oldRoots, root.getIdentifier(), occurrence[i] );
						if( element == null ){
							element = provider.new Element( null, root, null, info );
						}
						else{
							refill( element, null, root );
						}
						newRoots.add( element );
					}
				}
				
				UIJob finish = new UIJob( "Update Node Tree"){
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if( !provider.isDisposed() ){
							provider.setRoots( newRoots.toArray( new Element[ newRoots.size() ]) );
						}
						return Status.OK_STATUS;
					}
				};
				finish.setSystem( true );
				finish.schedule();
				
				Debug.leave();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	/**
	 * Creates an array where the index'th position tells how many times
	 * the identifier of the index'th element was already found. To be used
	 * together with {@link #search(Element[], String, int)}.
	 * @param elements the element to analyze
	 * @return the occurrences
	 */
	private int[] buildOccurrenceMap( IASTModelElement[] elements ){
		if( elements == null )
			return null;
		
		int[] result = new int[ elements.length ];
		
		loop:for( int i = 0; i < elements.length; i++ ){
			String id = elements[i].getIdentifier();
			
			for( int j = i-1; j >= 0; j-- ){
				if( elements[j].getIdentifier().equals( id )){
					result[i] = result[j]+1;
					continue loop;
				}
			}
			
			result[i] = 0;
		}
		
		return result;
	}
	
	/**
	 * Searches for the one {@link Element} whose identifier matches <code>identifier</code>.
	 * @param choices the possible answers
	 * @param identifier the identifier to search
	 * @param occurrence how many times to ignore a result and continue the search
	 * @return either an element of <code>choices</code> or <code>null</code>
	 */
	private Element search( Element[] choices, String identifier, int occurrence ){
		if( choices != null ){
			for( Element choice : choices ){
				if( choice.getIdentifier().equals( identifier )){
					if( occurrence == 0 )
						return choice;
					
					occurrence--;
				}
			}
		}
		return null;
	}
	
	/**
	 * Updates <code>element</code> such that it shows <code>connection</code>.
	 * @param element the element to update
	 * @param connection the new value of <code>element</code>
	 */
	private void refill( Element element, IASTModelNodeConnection connection ){
		if( provider.isExpanded( element )){
			IASTModelNode node = provider.resolveNode( element, connection, null );
			if( node != null ){
				refill( element, connection, node );
			}
			else{
				provider.setContent( element, true, null, connection, null, null );
			}
		}
		else{
			provider.setContent( element, false, null, connection, null, null );
		}
	}

	/**
	 * Updates <code>element</code> such that it shows <code>node</code>.
	 * @param element the element to update
	 * @param connection the connection that points to <code>node</code>
	 * @param node the new value of <code>element</code>
	 */
	private void refill( Element element, IASTModelNodeConnection connection, IASTModelNode node ){
		if( provider.isExpanded( element )){
			Element[] childElements = element.getChildren();
			IASTModelNodeConnection[] childNodes = node.getChildren();

			List<Element> children = new ArrayList<Element>();
			if( childNodes != null ){
				int[] occurrences = buildOccurrenceMap( childNodes );
				for( int i = 0; i < childNodes.length; i++ ){
					IASTModelNodeConnection childConnection = childNodes[i];
					Element child = search( childElements, childConnection.getIdentifier(), occurrences[i] );
					if( child == null ){
						child = provider.new Element( element, childConnection, null, element.getModelInfo() );
					}
					else{
						refill( child, childConnection );
					}
					children.add( child );
				}
			}

			provider.setContent( element, true, node, connection,
					children.toArray( new Element[ children.size() ]), null );
		}
		else{
			provider.setContent( element, false, node, null, null, null );
		}
	}
}
