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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.INesCWiring;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.nodes.ConnectionModelNode;
import tinyos.yeti.nesc12.ep.nodes.EndpointModelConnection;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Wire;

public class NesCWire extends AbstractNesCBinding implements INesCWiring{
    private ConnectionModelNode connection;
    
    private NesCEndpoint left;
    private NesCEndpoint right;
    
    private BindingResolver bindings;
    
    public NesCWire( ConnectionModelNode connection, BindingResolver bindings ){
        super( "Wire" );
        this.connection = connection;
        this.bindings = bindings;
    }
    
    @Override
    public String getBindingValue() {
        if( isLeftToRight() )
            return "->";
        if( isRightToLeft() )
            return "<-";
        if( isAssign() )
            return "=";
        return null;
    }
    
    public int getSegmentCount() {
        return 2;
    }
    
    public String getSegmentName( int segment ) {
        switch( segment ){
            case 0: return "left";
            case 1: return "right";
            default: return null;
        }
    }
    
    public int getSegmentSize( int segment ) {
        return 1;
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        switch( segment ){
            case 0: return getLeft();
            case 1: return getRight();
            default: return null;
        }
    }
    
    public IASTModelNodeConnection asConnection(){
	    return null;
    }
    
    public IASTModelNode asNode(){
    	return connection;
    }
    
    public int getReferenceKindCount(){
    	return 2;
    }
    
    public InspectionKey<?> getReferenceKind( int index ){
    	switch( index ){
    		case 0: return USED;
    		case 1: return PROVIDED;
    		default: return null;
    	}
    }
    
    @SuppressWarnings("unchecked")
	public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	if( USED.equals( key ) ){
    		List<INesCNode> list = new ArrayList<INesCNode>();
    		NesCEndpoint left = getLeft();
    		if( left != null && left.isUsed() ){
    			insertReference( left, list );
    		}
    		
    		NesCEndpoint right = getRight();
    		if( right != null && right.isUsed() ){
    			insertReference( right, list );
    		}
    		
    		return (K[])list.toArray( new INesCNode[ list.size() ] );
    	}
    	if( PROVIDED.equals( key ) ){
    		List<INesCNode> list = new ArrayList<INesCNode>();
    		NesCEndpoint left = getLeft();
    		if( left != null && left.isProvided() ){
    			insertReference( left, list );
    		}
    		
    		NesCEndpoint right = getRight();
    		if( right != null && right.isProvided() ){
    			insertReference( right, list );
    		}
    		
    		return (K[])list.toArray( new INesCNode[ list.size() ] );
    	}
    	
    	return null;
    }
    
    private void insertReference( NesCEndpoint point, List<INesCNode> list ){
    	NesCInterfaceReference interfaze = point.getInterface();
    	if( interfaze != null ){
    		list.add( interfaze );
    	}
    	else{
    		Field function = point.getFunction();
    		if( function != null ){
    			list.add( function );
    		}
    	}
    }
    
    public boolean isLeftToRight(){
        return connection.getDirection() == Wire.Direction.LEFT_TO_RIGHT;
    }
    
    public boolean isRightToLeft(){
        return connection.getDirection() == Wire.Direction.RIGHT_TO_LEFT;
    }
    
    public boolean isAssign(){
        return connection.getDirection() == Wire.Direction.ASSIGN;
    }
    
    public IASTModelPath getPath(){
        return connection.getPath();
    }
    
    public boolean isImplicit(){
        return connection.isImplicit();
    }
    
    public NesCEndpoint getLeft(){
        if( left == null ){
            EndpointModelConnection connection = this.connection.getLeft();
            if( connection != null ){
                left = connection.resolve( bindings );
            }
        }
        return left;
    }
    
    public NesCEndpoint getRight(){
        if( right == null ){
            EndpointModelConnection connection = this.connection.getRight();
            if( connection != null ){
                right = connection.resolve( bindings );
            }
        }
        return right;        
    }
    
    public NesCEndpoint getUser(){
        if( isLeftToRight() )
            return getLeft();
        else if( isRightToLeft() )
            return getRight();
        
        return null;
    }
    
    public NesCEndpoint getProvider(){
        if( isLeftToRight() )
            return getRight();
        else if( isRightToLeft() )
            return getLeft();
        
        return null;        
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( getLeft() );
        if( isLeftToRight() )
            builder.append( "->" );
        else if( isRightToLeft() )
            builder.append( "<-" );
        else if( isAssign() )
            builder.append( "=" );
        else
            builder.append( "?" );
        builder.append( getRight() );
        return builder.toString();
    }
}
