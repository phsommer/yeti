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
package tinyos.yeti.ep.parser;

import tinyos.yeti.ep.IParseFile;


/**
 * A set of generic filters.
 * @author Benjamin Sigg
 */
public class ASTNodeFilterFactory {
    private ASTNodeFilterFactory(){
        // no need for objects
    }

    /**
     * Creates a filter that is equivalent to {@link IASTModel#getNode(IASTModelPath, String, TagSet)}.
     * @param parent a parent a node must have or <code>null</code>
     * @param identifier the identifier a node must have or <code>null</code>
     * @param tags the tags a node must have or <code>null</code>
     * @return a new filter
     */
    public static IASTModelNodeFilter filter( final IASTModelPath parent, final String identifier, final TagSet tags ){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                if( parent != null && !parent.equals( node.getPath().getParent() ))
                    return false;
                
                if( identifier != null && !identifier.equals( node.getIdentifier() ))
                    return false;
                
                if( tags != null && !node.getTags().contains( tags ))
                    return false;
                
                return true;
            }
        };
    }

    /**
     * Searches for exactly the {@link Tag}s <code>tags</code>.
     * @param tags the tags to search for
     * @return the new filter
     */
    public static IASTModelNodeFilter tags( final TagSet tags ){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                return node.getTags().equals( tags );
            }
        };
    }
    
    /**
     * Gets a filter that allows only nodes whose tags are a superset
     * of tags.
     * @param tags the subset of tags, not <code>null</code>
     * @return the new filter
     */
    public static IASTModelNodeFilter subset( Tag... tags ){
        return subset( TagSet.get( tags ));
    }

    /**
     * Gets a filter that allows only nodes whose tags are a superset
     * of tags.
     * @param tags the subset of tags, not <code>null</code>
     * @return the new filter
     */
    public static IASTModelNodeFilter subset( final TagSet tags ){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                return node.getTags().contains( tags );
            }
        };
    }    
    

    /**
     * Gets a filter that allows only nodes whose tags are a subset
     * of tags.
     * @param tags the superset of tags, not <code>null</code>
     * @return the new filter
     */
    public static IASTModelNodeFilter superset( Tag... tags ){
        return superset( TagSet.get( tags ));
    }

    /**
     * Gets a filter that allows only nodes whose tags are a subset
     * of tags.
     * @param tags the superset of tags, not <code>null</code>
     * @return the new filter
     */
    public static IASTModelNodeFilter superset( final TagSet tags ){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                return tags.contains( node.getTags() );
            }
        };
    }
    
    /**
     * Gets a filter that allows only nodes that have the identifier <code>identifier</code>.
     * @param identifier the identifier, can be <code>null</code>
     * @return a new filter
     */
    public static IASTModelNodeFilter identifier( final String identifier ){
        if( identifier == null ){
            return new IASTModelNodeFilter(){
                public boolean include( IASTModelNode node ) {
                    return node.getIdentifier() == null;
                }
            };
        }
        
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                return identifier.equals( node.getIdentifier() );
            }
        };
    }
    
    /**
     * Gets a filter that accepts only node <code>node</code>.
     * @param node some node
     * @return the new filter
     */
    public static IASTModelNodeFilter is( final IASTModelNode node ){
    	return new IASTModelNodeFilter(){
    		public boolean include( IASTModelNode check ){
    			return check == node;
    		}
    	};
    }
    
    /**
     * Checks the parse file of the {@link IASTModelNode}s.
     * @param file the file that is searched, can be <code>null</code>
     * @return a new filter
     * @see IASTModelNode#getParseFile()
     */
    public static IASTModelNodeFilter origin( final IParseFile file ){
        if( file == null ){
            return  new IASTModelNodeFilter(){
                public boolean include( IASTModelNode node ) {
                    return node.getParseFile() == null;
                }
            };
        }
        
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                return file.equals( node.getParseFile() );
            }
        };
    }
    
    /**
     * Gets a filter that allows only the nodes whose path equals <code>path</code>.
     * @param path the path to search
     * @return the new filter
     */
    public static IASTModelNodeFilter path( final IASTModelPath path ){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                return node.getPath().equals( path );
            }
        };
    }
    
    /**
     * Gets a filter that allows only nodes whose parent equals <code>parent</code>
     * @param parent the parent path, not <code>null</code>
     * @return the new filter
     */
    public static IASTModelNodeFilter parent( final IASTModelPath parent ){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                return parent.equals( node.getPath().getParent() );
            }
        };
    }
    
    /**
     * Gets a filter that allows all nodes.
     * @return the all inclusive filter
     */
    public static IASTModelNodeFilter all(){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                return true;
            }
        };
    }

    /**
     * Creates a filter that does the reverse of <code>filter</code>.
     * @param filter some filter
     * @return the reverse of <code>filter</code>
     */
    public static IASTModelNodeFilter not( final IASTModelNodeFilter filter ){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ) {
                return !filter.include( node );
            }
        };
    }

    /**
     * Filters out all nodes that are filtered out by at least one of
     * <code>filters</code>.
     * @param filters the list of filters
     * @return the AND-filter
     */
    public static IASTModelNodeFilter and( final IASTModelNodeFilter... filters ){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ){
                for( IASTModelNodeFilter filter : filters )
                    if( !filter.include( node ))
                        return false;
                
                return true;
            }
        };
    }

    /**
     * Allows all nodes that are allowed by at least one of <code>filters</code>. 
     * @param filters a list of filters
     * @return the OR-filter
     */
    public static IASTModelNodeFilter or( final IASTModelNodeFilter... filters ){
        return new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ){
                for( IASTModelNodeFilter filter : filters )
                    if( filter.include( node ))
                        return true;
                
                return false;
            }
        };
    }
}
