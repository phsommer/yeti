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
package tinyos.yeti.nesc12.parser.ast.util.nodestack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

/**
 * Represents a node in the tree of all elements that get pushed or popped
 * to or from a {@link NodeStack}.
 * @author Benjamin Sigg
 */
public class StackLevel implements Iterable<StackLevel>{
    private Node node;
    
    private StackLevel parent;
    private List<StackLevel> children;
    private List<Runnable> runOnPop;
    
    private NodeStack stack;
    
    private boolean located = false;
    private boolean override = false;
    
    private int preventChildrenClose = 0;
    
    public StackLevel( NodeStack stack, StackLevel parent, Node node ){
        this.stack = stack;
        this.parent = parent;
        this.node = node;
    }
    
    public void executeOnPop( Runnable runnable ){
    	if( runOnPop == null )
    		runOnPop = new ArrayList<Runnable>();
    	runOnPop.add( runnable );
    }
    
    /**
     * Executes all {@link Runnable}s registered at {@link #executeOnPop(Runnable)}
     * now and forgets them.
     */
    public void executeOnPop(){
    	if( runOnPop != null ){
    		for( Runnable run : runOnPop ){
    			run.run();
    		}
    		runOnPop = null;
    	}
    }
    
    /**
     * Sets the override flag. It indicates that a {@link ModelNode} with the
     * same path as this node might already exist and that this other node
     * must be replaced by the value of this node.
     * @param override <code>true</code> if overriding should be performed
     */
    public void setOverride( boolean override ){
    	this.override = override;
    }
    
    public void setPreventChildrenClose( int preventChildrenClose ){
		this.preventChildrenClose = preventChildrenClose;
	}
    
    public int getPreventChildrenClose() {
		return preventChildrenClose;
	}
    
    private boolean isAnyPreventChildrenClose(){
    	return isAnyPreventChildrenClose( 0 );
    }
    
    private boolean isAnyPreventChildrenClose( int depth ){
    	if( parent == null )
    		return false;
    	
    	if( parent.getPreventChildrenClose() == -1 )
    		return true;
    	
    	if( parent.getPreventChildrenClose() > depth )
    		return true;
    	
    	return parent.isAnyPreventChildrenClose( depth+1 );
    }
    
    public void addChild( StackLevel level ){
        if( children == null )
            children = new ArrayList<StackLevel>();
        
        children.add( level );
    }
    
    public Node[] getChildrenNodes(){
    	if( children == null )
    		return new Node[]{};
    	
    	Node[] result = new Node[ children.size() ];
    	int index = 0;
    	for( StackLevel child : children ){
    		result[index++] = child.getNode();
    	}
    	
    	return result;
    }
    
    public Iterator<StackLevel> iterator() {
    	if( children == null ){
    		return new Iterator<StackLevel>(){
    			public boolean hasNext() {
    				return false;
    			}
    			public StackLevel next() {
    				return null;
    			}
    			public void remove() {
    				// ignore
    			}
    		};
    	}
    	return children.iterator();
    }
    
    public Node getNode(){
        return node;
    }
    
    public ASTModelPath getPath(){
    	if( parent == null ){
    		IParseFile file = stack.getAnalyzeStack().getParseFile();
    		String id = node.getIdentifier();
    		if( file == null || id == null )
    			return null;
    		
    		return new ASTModelPath( file, id );
    	}
    	
    	ASTModelPath path = parent.getPath();
    	String id = node.getIdentifier();
    	if( id == null )
    		return null;
    	return path.getChild( id );
    }
    
    public void close( StackLevel child, List<StackLevel> closed ){
        if( child.close( closed ) ){
            children.remove( child );
        }
    }
    
    public void popped(){
    	stack.tryAddDirectives();
    }
    
    /**
     * Tries to complete this level and write anything on this level
     * into the {@link ModelNode} or {@link ModelConnection} which is stored
     * here.
     * @param closed a list into which this level will insert itself and 
     * all children that were closed
     * @return <code>true</code> if this level is completed and can safely be
     * deleted 
     */
    public boolean close( List<StackLevel> closed ){
    	tryLocate();
    	
        if( isAnyPreventChildrenClose() ){
        	return false;
        }
        
        preventChildrenClose = 0;
        
        if( located ){
            if( closeChildren( closed ) ){
                node.pop();
                closed.add( this );
                return true;
            }
            
            return false;
        }
        
        // not at correct position, try again later
        if( getNode().getNode() != null )
            return false;
        
        // there is no node present, try the best possible and delete
        // this incomplete level anyway
        closeChildren( closed );    
        node.pop();
        closed.add( this );
        return true;
    }
    
    /**
     * Tries to put the node of this level at its correct position in the
     * tree.
     */
    public void tryLocate(){
        if( located )
            return;
     
        ModelNode modelNode = node.getNode();
        if( modelNode == null )
        	return;
        
        if( validLocation() ){
            located = true;
            if( parent == null ){
                IParseFile file = stack.getAnalyzeStack().getParseFile();
                modelNode.setParseFile( file );
                modelNode.setPath( new ASTModelPath( file, modelNode.getIdentifier() ) );
            }
            else{
                modelNode.setParent( parent.getNode().getNode() );
            }
        }
        
        if( located ){
            // property changed, this node has now a valid location
            stack.addToModel( modelNode, override );
            
            //  try the children as well, their state might be changed too
            if( children != null ){
                for( StackLevel child : children )
                    child.tryLocate();
            }
        }
        
        return;
    }
    
    private boolean validLocation(){
        ModelNode model = node.getNode();
        if( model == null )
            return false;
        
        if( model.getIdentifier() == null )
            return false;
        
        if( parent == null )
            return true;
        
        return parent.validLocation();
    }
    
    private boolean closeChildren( List<StackLevel> closed ){
    	if( children == null )
            return true;
        
        Iterator<StackLevel> iterator = children.iterator();
        while( iterator.hasNext() ){
            StackLevel next = iterator.next();
            if( next.close( closed ) ){
                iterator.remove();
            }
        }
        return children.isEmpty();
    }
}
