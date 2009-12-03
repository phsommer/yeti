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
package tinyos.yeti.nesc12.parser.meta;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection where ranges and values are paired up. It is possible to find
 * out for a specific location, which ranges are intersected and thus which
 * values are available at this location.<br>
 * Clients first have to call {@link #put(int, int, Object)} until all
 * objects are collected, then they should call {@link #optimize()} once, adding
 * children after {@link #optimize()} was called will result in unspecified
 * behavior.
 * @param <V> the kind of value in this collection
 * @author Benjamin Sigg
 */
public class RangedCollection<V> {
    private NodeEntry root;
    
    /**
     * Adds a new range and value to this collection. The behavior of this
     * method is unspecified if {@link #optimize()} was called.
     * @param left the begin of the range
     * @param right the end of the range
     * @param value the value of the range
     */
    public void put( int left, int right, V value ){
        if( root == null ){
            root = new NodeEntry( Integer.MIN_VALUE, Integer.MAX_VALUE );
        }
        
        root.add( new ValueEntry( left, right, value ));
    }
    
    /**
     * Optimizes this collection for searches within ranges
     */
    public void optimize(){
        if( root != null ){

            List<ValueEntry> values = new ArrayList<ValueEntry>();
            root.list( values );

            Distance<Entry> distance = new Distance<Entry>(){
                public double distance( Entry a, Entry b ) {
                    return Math.max( a.right, b.right ) - Math.min( a.left, b.left );
                }
            };

            DistanceMatrix<Entry, ValueEntry> matrix = new DistanceMatrix<Entry, ValueEntry>( values, distance ){
                @Override
                protected Entry create( ValueEntry element ) {
                    return element;
                }
                @Override
                protected Entry create( Entry a, Entry b ) {
                    if( a.left <= b.left && a.right >= b.right ){
                        if( a instanceof RangedCollection.NodeEntry ){
                            ((NodeEntry)a).add( b );
                            return a;
                        }
                        else{
                            NodeEntry result = new NodeEntry( a.left, a.right );
                            result.add( a );
                            result.add( b );
                            return result;
                        }
                    }
                    
                    if( a.left >= b.left && a.right <= b.right ){
                        if( b instanceof RangedCollection.NodeEntry ){
                            ((NodeEntry)b).add( a );
                            return b;
                        }
                        else{
                            NodeEntry result = new NodeEntry( b.left, b.right );
                            result.add( a );
                            result.add( b );
                            return result;
                        }
                    }
                    
                    int left = Math.min( a.left, b.left );
                    int right = Math.max( a.right, b.right );
                    NodeEntry result = new NodeEntry( left, right );
                    result.add( a );
                    result.add( b );
                    return result;
                }
            };

            Point buffer = null;

            for( int i = matrix.getClusterSize(); i > 1; i-- ){
                buffer = matrix.findMin( buffer );
                matrix.cluster( buffer );
            }

            Entry root = matrix.getCluster( 0 );
            if( root != null && !(root instanceof RangedCollection.NodeEntry )){
                this.root = new NodeEntry( root.left, root.right );
                this.root.add( root );
            }
            else{
                this.root = (NodeEntry)root;
            }
        }
    }
    
    /**
     * Tells whether there is a range at <code>location</code> that contains
     * <code>value</code> (using equal comparison).
     * @param location the location
     * @param value some value, not <code>null</code>
     * @return <code>true</code> if a range that encloses <code>location</code>
     * and <code>value</code> was found
     */
    public boolean contains( int location, V value ){
        if( root == null )
            return false;
        
        return root.contains( location, value );
    }
    
    /**
     * Gets the content at <code>location</code>. 
     * @param location some location
     * @return the content
     * @see #scanner()
     */
    public List<V> get( int location ){
        Scanner scanner = scanner();
        scanner.move( location );
        return scanner.getContent();
    }
    
    /**
     * Visits all elements at <code>location</code>
     * @param location some location
     * @param visitor a visitor for the elements at that location
     */
    public void visit( int location, Visitor<? super V> visitor ){
        if( root != null ){
            root.visit( location, visitor );
        }
    }
    
    /**
     * Creates a new scanner for the content of this collection.
     * @return the new scanner
     */
    public Scanner scanner(){
        return new Scanner();
    }
    
    @Override
    public String toString(){
        return root.toString();
    }
    
    /**
     * A scanner can be used to scan the contents of a {@link RangedCollection}.
     * @author Benjamin Sigg
     */
    public class Scanner{
        private List<V> content = new ArrayList<V>();
        
        private Scanner(){
            // do nothing
        }
        
        /**
         * Moves the scanner to a new location.
         * @param location the new location
         */
        public void move( int location ){
            if( root != null ){
                content.clear();
                root.values( location, content );
            }
        }
        
        /**
         * Gets the elements at the current location. The result of this
         * method will be modified by the scanner if it is {@link #move(int) moved}
         * again. Clients should not change the resulting list.
         * @return the contents at the current location
         */
        public List<V> getContent() {
            return content;
        }
    }
    
    public static interface Visitor<V>{
        /**
         * Visits an element.
         * @param value the current value that is visited
         * @return <code>true</code> if another value should be visited, <code>false</code>
         * to cancel the operation
         */
        public boolean visit( V value );
    }
    
    private abstract class Entry{
        public int left;
        public int right;
        
        public Entry( int left, int right ){
            this.left = left;
            this.right = right;
        }
        
        @Override
        public String toString(){
            StringBuilder builder = new StringBuilder();
            toString( builder, 0 );
            return builder.toString();
        }
        
        public abstract void values( int location, List<V> list );
        public abstract boolean visit( int location, Visitor<? super V> visitor );
        public abstract void list( List<ValueEntry> values );
        public abstract boolean contains( int location, V value );
        
        public abstract void toString( StringBuilder builder, int tabs );
        
        protected void tab( StringBuilder builder, int tabs ){
            for( int i = 0; i < tabs; i++ ){
                builder.append( "  " );
            }
        }
        
        protected void range( StringBuilder builder ){
            builder.append( "[" );
            builder.append( left );
            builder.append( ", " );
            builder.append( right );
            builder.append( "]" );
        }
    }
    
    private class ValueEntry extends Entry{
        public V value;
        
        public ValueEntry( int left, int right, V value ){
            super( left, right );
            this.value = value;
        }
        
        @Override
        public void list( List<ValueEntry> values ) {
            values.add( this );
        }
        
        @Override
        public void values( int location, List<V> list ) {
            if( location < left || location > right )
                return;
            
            list.add( value );
        }
        
        @Override
        public boolean visit( int location, Visitor<? super V> visitor ){
            if( location < left || location > right )
                return true;
            
            return visitor.visit( value );
        }
        
        @Override
        public boolean contains( int location, V value ) {
            if( location < left || location > right )
                return false;
            
            return value.equals( this.value );
        }
        
        @Override
        public void toString( StringBuilder builder, int tabs ){
            tab( builder, tabs );
            range( builder );
            builder.append( " " );
            builder.append( value.toString() );
        }
    }
    
    private class NodeEntry extends Entry{
        private List<Entry> children = new ArrayList<Entry>( 2 );
        
        public NodeEntry( int left, int right ){
            super( left, right );
        }
        
        public void add( Entry entry ){
            children.add( entry );
        }
        
        @Override
        public void list( List<ValueEntry> values ) {
            for( Entry child : children )
                child.list( values );
        }
        
        @Override
        public void values( int location, List<V> list ) {
            if( location < left || location > right )
                return;
            
            for( Entry child : children ){
                child.values( location, list );
            }
        }

        @Override
        public boolean visit( int location, Visitor<? super V> visitor ){
            if( location < left || location > right )
                return true;
            
            for( Entry child : children ){
                if( !child.visit( location, visitor ) )
                    return false;
            }
            
            return true;
        }
        
        @Override
        public boolean contains( int location, V value ) {
            if( location < left || location > right )
                return false;
            
            for( Entry child : children ){
                if( child.contains( location, value ) )
                    return true;
            }
            
            return false;
        }
        
        @Override
        public void toString( StringBuilder builder, int tabs ){
            tab( builder, tabs );
            range( builder );
            for( Entry child : children ){
                builder.append( "\n" );
                child.toString( builder, tabs+1 );
            }
        }
    }
}
