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
package tinyos.yeti.nesc12.parser.ast.nodes;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.NoSuchASTFieldException;
import tinyos.yeti.nesc12.parser.ast.Range;


/**
 * A {@link AbstractFixedASTNode} has a fixed number of fields, some of them might
 * be <code>null</code>.
 * @author Benjamin Sigg
 */
public abstract class AbstractFixedASTNode extends RangedASTNode implements FixedASTNode, Range{
    private NodeField[] fields;
    
    public AbstractFixedASTNode( String name, String... fields ) {
        super( name );
        if( fields != null )
            setFields( fields );
        else
            setFields();
    }
    
    private void setFields( String... names ){
        this.fields = new NodeField[ names.length ];
        for( int i = 0; i < names.length; i++ )
            fields[i] = new NodeField( names[i] );
    }
    
    public int getChildrenCount() {
        return getFieldCount();
    }
    
    public ASTNode getChild( int index ) {
        return getField( index );
    }
    
    /**
     * Gets the number of children this node has.
     * @return the number of children
     */
    public int getFieldCount(){
        return fields == null ? 0 : fields.length;
    }
    
    public String getFieldName( int index ){
        return fields[ index ].getName();
    }
    
    public int getFieldLocation( String name ){
        if( fields == null )
            return -1;
        
        for( int i = 0, n = fields.length; i<n; i++ ){
            if( fields[i].getName().equals( name ))
                return i;
        }
        
        return -1;
    }
    
    public int getFieldLocation( ASTNode node ){
        if( fields == null )
            return -1;
        
        if( node == null )
            return -1;
        
        for( int i = 0, n = fields.length; i<n; i++ ){
            if( fields[i].getNode() == node )
                return i;
        }
        
        return -1;
    }
    
    public String getFieldName( ASTNode node ){
        int index = getFieldLocation( node );
        if( index >= 0 )
            return getFieldName( index );
        
        return null;
    }
    
    public ASTNode getField( int index ){
        return fields[ index ].getNode();
    }
    
    protected ASTNode getNoError( int index ){
        ASTNode node = getField( index );
        if( node instanceof ErrorASTNode )
            return null;
        
        return node;
    }
    
    public ASTNode getField( String name ){
        int index = getFieldLocation( name );
        if( index < 0 ){
            throw new NoSuchASTFieldException( this, "No such field: " + name );
        }
        
        return getField( index );
    }
    
    public void setField( int index, ASTNode node ) throws ASTException{
        if( node != null ){
            if( !(node instanceof ErrorASTNode )){
                checkField( index, node );
            }
        }
        fields[ index ].setNode( node );
    }
    
    /**
     * Called before a field is changed. A subclass can enforce its constraints.<br>
     * @param index the location of the changing field.
     * @param node the new value of the field, never <code>null</code> since
     * <code>null</code> is accepted anyway
     * @throws ASTException if the new value is not acceptable
     */
    protected abstract void checkField( int index, ASTNode node ) throws ASTException;
    
    public void setField( String name, ASTNode node ) throws ASTException{
        int index = getFieldLocation( name );
        if( index < 0 ){
            throw new NoSuchASTFieldException( this, "No such field: " + name );
        }
        setField( index, node );
    }
    
    private class NodeField{
        private String name;
        private ASTNode node;
        
        public NodeField( String name ){
            this.name = name;
        }
        
        public NodeField( String name, ASTNode node ){
            this.name = name;
            this.node = node;
            if( node != null )
                connectChild( node );
        }
        
        public String getName() {
            return name;
        }
        
        public ASTNode getNode() {
            return node;
        }
        
        public void setNode( ASTNode node ) throws ASTException{
            if( node != null )
                connectChild( node );
            if( this.node != null )
                disconnectChild( this.node );
            this.node = node;
        }
    }
}
