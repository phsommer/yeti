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
package tinyos.yeti.ep.parser.standard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.ASTNodeFilterFactory;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelConnectionFilter;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;

/**
 * Default implementation of an {@link IASTModel}. Does not need any
 * special implementation of nodes or connections.
 * @author Benjamin Sigg
 */
public class ASTModel implements IASTModel{
    /** tag indicating an implementation block */
    public static final Tag IMPLEMENTATION = new Tag( "implementation", true );
    
    public static final Tag MODULE_IMPLEMENTATION = new Tag( "module implementation", false );
    public static final Tag CONFIGURATION_IMPLEMENTATION = new Tag( "configuration implementation", false );

    /** tag indicating a specification block */
    public static final Tag SPECIFICATION = new Tag( "specification", true );

    /** tag indicating a components block */
    public static final Tag COMPONENTS = new Tag( "components", true );

    /** tag indicating a node full of connections */
    public static final Tag CONNECTIONS = new Tag( "connections", true );


    /**
     * Creates a new {@link TagSet} with all the {@link Tag}s that are 
     * used by this model.
     * @return the set of tags
     */
    public static TagSet getSupportedTags(){
    	TagSet set = new TagSet();
    	
    	set.add( Tag.INTERFACE );
    	set.add( Tag.MODULE );
    	set.add( Tag.CONFIGURATION );
    	set.add( Tag.BINARY_COMPONENT );
    	set.add( Tag.COMPONENT );
    	set.add( Tag.USES );
    	set.add( Tag.PROVIDES );
    	set.add( Tag.RENAMED );
    	set.add( Tag.STRUCT );
    	set.add( Tag.UNION );
    	set.add( Tag.DATA_OBJECT );
    	set.add( Tag.ATTRIBUTE );
    	set.add( Tag.FUNCTION );
    	set.add( Tag.ASYNC );
    	set.add( Tag.EVENT );
    	set.add( Tag.COMMAND );
    	set.add( Tag.TASK );
    	set.add( Tag.CONNECTION );
    	set.add( Tag.CONNECTION_LEFT );
    	set.add( Tag.CONNECTION_RIGHT );
    	set.add( Tag.CONNECTION_BOTH );
    	set.add( Tag.OUTLINE );
    	set.add( Tag.FIGURE );
    	set.add( Tag.AST_CONNECTION_ICON_RESOLVE );
    	set.add( Tag.AST_CONNECTION_GRAPH_ICON_RESOLVE );
    	set.add( Tag.AST_CONNECTION_LABEL_RESOLVE );
    	set.add( Tag.AST_CONNECTION_GRAPH_LABEL_RESOLVE );
    	set.add( Tag.INCLUDED );
    	set.add( Tag.NO_BASE_EXPANSION );
    	set.add( IMPLEMENTATION );
        set.add( MODULE_IMPLEMENTATION );
    	set.add( CONFIGURATION_IMPLEMENTATION );
    	set.add( SPECIFICATION );
    	set.add( COMPONENTS );
    	set.add( CONNECTIONS );
    	
    	return set;
    }

    public static ImageDescriptor getImageFor( TagSet tags ){
        if( tags.contains( CONNECTIONS ))
            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_CONNECTION );

        if( tags.contains( Tag.CONNECTION )){
            if( tags.contains( Tag.CONNECTION_RIGHT ))
                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_LINK_WIRES  );
            if( tags.contains( Tag.CONNECTION_LEFT ))
                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_LINK_WIRES_INVERSE  );
            if( tags.contains( Tag.CONNECTION_BOTH ))
                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_EQUATE_WIRES  );

            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_CONNECTION );
        }

        if( tags.contains( IMPLEMENTATION )){
        	if( tags.contains( MODULE_IMPLEMENTATION ))
        		return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_IMPLEMENTATION_MODULE );
        	if( tags.contains( CONFIGURATION_IMPLEMENTATION ))
        		return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_IMPLEMENTATION_CONFIGURATION );
        	
            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_PLAIN_PAGE );
        }

        if( tags.contains( SPECIFICATION ))
            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_SPECIFICATION );

        if( tags.contains( Tag.INTERFACE )){
            if( tags.contains( Tag.USES ))
                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_USES_INTERFACE );

            if( tags.contains( Tag.PROVIDES ))
                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_PROVIDES_INTERFACE );

            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_INTERFACE );
        }

        if( tags.contains( Tag.MODULE ))
            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_MODULE );

        if( tags.contains( Tag.CONFIGURATION ))
            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_CONFIGURATION );

        if( tags.contains( Tag.BINARY_COMPONENT ))
            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_BINARY_COMPONENT );

        if( tags.contains( Tag.COMPONENT ) || tags.contains( COMPONENTS )){
            if( tags.contains( Tag.USES ))
                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_USES_INTERFACE );

            if( tags.contains( Tag.PROVIDES ))
                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_PROVIDES_INTERFACE );

            if( tags.contains( Tag.RENAMED ))
                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_COMPONENT_RENAMED );

            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_COMPONENT );
        }

        if( tags.contains( Tag.FUNCTION )){
            boolean command = tags.contains( Tag.COMMAND );
            boolean event = tags.contains( Tag.EVENT );
            boolean async = tags.contains( Tag.ASYNC );
            boolean uses = tags.contains( Tag.USES );
            boolean provides = tags.contains( Tag.PROVIDES );
            boolean task = tags.contains( Tag.TASK );

            if( command ){
                if( async ){
                    if( uses )
                        return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_COMMAND_ASYNC_USES );
                    if( provides )
                        return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_COMMAND_ASYNC_PROVIDES );

                    return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_COMMAND_ASYNC );
                }
                if( uses )
                    return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_COMMAND_USES );
                if( provides )
                    return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_COMMAND_PROVIDES );

                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_COMMAND );
            }
            if( event ){
                if( async ){
                    if( uses )
                        return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_EVENT_ASYNC_USES );
                    if( provides )
                        return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_EVENT_ASYNC_PROVIDES );

                    return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_EVENT_ASYNC );
                }
                if( uses )
                    return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_EVENT_USES );
                if( provides )
                    return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_EVENT_PROVIDES );

                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_EVENT );
            }
            if( task ){
                if( async )
                    return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_TASK_ASYNC );

                return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_TASK ); 
            }

            return NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_CFUNCTION );
        }

        return null;
    }
    
    private LinkedHashMap<IASTModelPath, IASTModelNode> map = new LinkedHashMap<IASTModelPath, IASTModelNode>();
    
    private int removeMark = 0;
    
    private IProject project;
    
    public ASTModel( IProject project ){
    	this.project = project;
    }
    
    public IProject getProject(){
	    return project;
    }

    public boolean addNode( IASTModelNode node ){
    	return addNode( node, false );
    }
    
    public boolean addNode( IASTModelNode node, boolean override ){
        if( node == null )
            throw new IllegalArgumentException( "node must not be null" );

        boolean add;
        
        if( override ){
        	removeNode( node.getPath() );
        	add = true;
        }
        else{
        	add = !map.containsKey( node.getPath() );
        }
        
        if( add ){
            map.put( node.getPath(), node );
            return true;
        }
        else{
            return false;
        }
    }

    public void addNodes( IASTModelNode[] nodes ){
        for( IASTModelNode node : nodes ){
            addNode( node );
        }
    }

    public void clear() {
        removeMark = 0;
        map.clear();
    }

    public int getSize(){
        return map.size();
    }
    
    public IASTModelNode[] getNodes( IASTModelNodeFilter filter ) {
        List<IASTModelNode> result = new ArrayList<IASTModelNode>();
        for( IASTModelNode node : map.values() ){
            if( filter.include( node ))
                result.add( node );
        }
        return result.toArray( new IASTModelNode[ result.size() ] );
    }
    
    public Iterable<IASTModelNode> getNodes(){
    	return map.values();
    }

    public IASTModelNode getNode( IASTModelNodeFilter filter ) {
        IASTModelNode[] nodes = getNodes( filter );
        if( nodes.length == 0 )
            return null;

        if( nodes.length == 1 )
            return nodes[0];

        int index = -1;
        IASTModelNode best = null;

        for( IASTModelNode check : nodes ){
            if( best == null ){
                best = check;
                index = check.getParseFile().getIndex();
            }
            else if( check.getParseFile().getIndex() < index ){
                index = check.getParseFile().getIndex();
                best = check;
            }
        }

        return best;
    }

    public IASTModelNode[] getNodes( IASTModelPath parent, String identifier, TagSet tags ) {
        return getNodes( ASTNodeFilterFactory.filter( parent, identifier, tags ));
    }

    public IASTModelNode getNode( IASTModelPath parent, String identifier, TagSet tags ) {
        return getNode( ASTNodeFilterFactory.filter( parent, identifier, tags ) );
    }

    public IASTModelNode getNode( IASTModelNodeConnection connection ){
        if( connection.isReference() ){
            IASTModelPath referenced = connection.getReferencedPath();
            if( referenced == null )
                return getNode( null, connection.getIdentifier(), connection.getTags().keySet() );
            else
                return getNode( referenced );
        }
        else
            return getNode( connection.getPath(), connection.getIdentifier(), connection.getTags() );
    }

    public IASTModelNode getNode( IASTModelPath path ) {
        // return getNode( ASTNodeFilterFactory.path( path ) );
        return map.get( path );
    }
    
    public Iterator<IASTModelNode> iterator(){
    	return map.values().iterator();
    }
    
    public IASTModelNode removeNode( IASTModelPath path ){
    	return map.remove( path );
    }

    public void markForLaterRemoving(){
        removeMark = map.size();
    }

    public void removeNodes( IParseFile parseFile ) {
        removeNodes( ASTNodeFilterFactory.origin( parseFile ) );
    }

    private Iterator<IASTModelNode> iteratorAt( int index ){
    	if( index >= map.size() )
    		return null;
    	
    	Iterator<IASTModelNode> iterator = map.values().iterator();
    	while( index > 0 ){
    		index--;
    		iterator.next();
    	}
    	
    	return iterator;
    }
    
    public void removeNodes( IASTModelNodeFilter filter ) {
        if( removeMark >= map.size() ){
            removeMark = 0;
            return;
        }
        else{
            Iterator<IASTModelNode> iterator = iteratorAt( removeMark );
            removeMark = 0;

            if( iterator != null ){
	            while( iterator.hasNext() ){
	                IASTModelNode next = iterator.next();
	                if( filter.include( next )){
	                    iterator.remove();
	                    map.remove( next.getPath() );
	                }
	            }
            }
        }
    }

    public void remove(IASTModelNodeFilter nodes, IASTModelConnectionFilter connections) {
        if( removeMark >= this.map.size() ){
            removeMark = 0;
            return;
        }
        else{
            Iterator<IASTModelNode> iterator = iteratorAt( removeMark );
            removeMark = 0;

            if( iterator != null ){
	            while( iterator.hasNext() ){
	                IASTModelNode next = iterator.next();
	
	                if( nodes.include( next )){
	                    iterator.remove();
	                    map.remove( next.getPath() );
	                }
	                else
	                    next.removeConnections( connections );
	            }
            }
        }    	
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "model[size=" );
        builder.append( map.size() );
        builder.append( "]" );
        for( IASTModelNode node : map.values() ){
            builder.append( "\n\t" );
            builder.append( node );
        }
        return builder.toString();
    }
}
