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
package tinyos.yeti.nesc12.parser.ast.elements;

/**
 * A {@link BindingTreeNode} encapsulates a {@link Binding} and has
 * other {@link BindingTreeNode}s as children. The children are created
 * using the contents of the encapsulated {@link Binding}. Other than
 * bindings, treenodes do never have cyclic dependencies. A treenode
 * does create its children lazily and only when they are needed.
 * @author Benjamin Sigg
 */
public class BindingTreeNode {
    private Binding binding;
    private String name;
    
    private BindingTreeNode parent;
    private BindingTreeNode[] children;
    
    /**
     * Creates a new node.
     * @param name the name of this node, can be <code>null</code>
     * @param binding the binding of this node, can be <code>null</code>
     */
    public BindingTreeNode( String name, Binding binding ){
        this.name = name;
        this.binding = binding;
    }
    
    @Override
    public String toString() {
        Object value = getValue();
        if( value == null ){
            if( name == null )
                return "<null>";
            
            return name + " = <null>";
        }
        else{
            if( name == null )
                return value.toString();
            
            return name + " = " + value.toString();
        }
    }
    
    public Object getValue(){
        if( binding == null )
            return null;
        
        String type = binding.getBindingType();
        String value = binding.getBindingValue();
        
        if( type == null )
            return value;
        
        if( value == null )
            return type;
        
        return type + ": " + value;
    }
    
    public BindingTreeNode getParent() {
        return parent;
    }
    
    public boolean isLeaf(){
        if( binding == null )
            return true;
        
        return binding.getSegmentCount() == 0;
    }
    
    private void resolveChild( int index ){
        if( children == null ){
            if( binding != null ){
                int size = 0;
                for( int i = 0, n = binding.getSegmentCount(); i<n; i++ )
                    size += binding.getSegmentSize( i );
                
                children = new BindingTreeNode[ size ];
            }
            else
                children = new BindingTreeNode[ 0 ];
        }
        
        if( index >= 0 && index < children.length ){
            if( children[ index ] == null ){
                int count = index;
                
                for( int i = 0, n = binding.getSegmentCount(); i<n; i++ ){
                    int size = binding.getSegmentSize( i );
                    if( count < size ){
                        BindingTreeNode node = createNodeFor( 
                                binding.getSegmentName( i ),
                                binding.getSegmentChild( i, count ) );
                        if( node == null )
                            node = new BindingTreeNode(
                                    binding.getSegmentName( i ),
                                    null );
                        node.parent = this;
                        children[ index ] = node;
                        break;
                    }
                    else{
                        count -= size;
                    }
                }
            }
        }
    }
    
    public int getChildrenCount(){
        resolveChild( -1 );
        return children.length;
    }
    
    public BindingTreeNode getChild( int index ){
        resolveChild( index );
        return children[ index ];
    }
    
    public BindingTreeNode[] getChildren(){
        for( int i = 0, n = getChildrenCount(); i<n; i++ )
            resolveChild( i );
        
        return children;
    }
    
    public int indexOf( BindingTreeNode child ){
        if( children == null || child == null )
            return -1;
        
        for( int i = 0, n = children.length; i<n; i++ ){
            if( children[i] == child )
                return i;
        }
        
        return -1;
    }
    
    /**
     * Creates a new node for <code>binding</code>.
     * @param name the name of the new node
     * @param binding the binding, can be <code>null</code>
     * @return the new node, can be <code>null</code>
     */
    protected BindingTreeNode createNodeFor( String name, Binding binding ){
        return new BindingTreeNode( name, binding );
    }
}
