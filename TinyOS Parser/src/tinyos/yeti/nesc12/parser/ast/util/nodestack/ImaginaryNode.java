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
import java.util.List;

import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class ImaginaryNode implements Node{
    private static final Object IS_CHILD = new Object();
    private static final Object IS_REFERENCE = new Object();
    private static final Object IS_CONNECTION = new Object();

    private List<Object> children;
    private List<FileRegion> fileRegions;

    private boolean error = false;
    private boolean warning = false;

    private ModelNode node;
    private ModelConnection connection;

    private Range range;
    
    public void addChild( ModelNode node, ASTNode ast ){
        if( children == null )
            children = new ArrayList<Object>();

        children.add( IS_CHILD );
        children.add( node );
        children.add( ast );
    }

    public void addConnection( ModelConnection connection ){
        if( children == null )
            children = new ArrayList<Object>();

        children.add( IS_CONNECTION );
        children.add( connection );
    }

    public void addReference( ModelNode node, ASTNode ast ){
        if( children == null )
            children = new ArrayList<Object>();

        children.add( IS_REFERENCE );
        children.add( node );
        children.add( ast );
    }

    public void removeChild( String target ){
        for( int i = 0, n = children.size(); i<n; i += 2 ){
            Object kind = children.get( i );
            if( kind == IS_CHILD ){
                ModelNode child = (ModelNode)children.get( i+1 );
                if( child.getIdentifier().equals( target )){
                	children.remove( i-2 );
                    children.remove( i-1 );
                    children.remove( i );
                    i -= 3;
                }
            }
            if( kind == IS_CONNECTION ){
                ModelConnection child = (ModelConnection)children.get( i+1 );
                if( child.getIdentifier().equals( target )){
                    children.remove( i-1 );
                    children.remove( i );
                    i -= 2;
                }
            }
        }
    }
    
    public void addFileRegion( FileRegion region ){
        if( fileRegions == null )
            fileRegions = new ArrayList<FileRegion>();

        fileRegions.add( region );
    }

    public void setRange( Range range ){
	    this.range = range;	
    }
    
    public Range getRange(){
    	return range;
    }
    
    public String getIdentifier() {
        if( node == null )
            return null;

        return node.getIdentifier();
    }

    public ModelConnection getConnection(){
        return connection;
    }

    public void setConnection( ModelConnection connection ){
        if( this.connection == null )
            this.connection = connection;
    }

    public ModelNode getNode(){
        return node;
    }

    public void setNode( ModelNode node ){
        if( this.node == null )
            this.node = node;
    }

    public void putErrorFlag(){
        error = true;
    }

    public void putWarningFlag(){
        warning = true;
    }
    
    public void pop(){
        if( connection != null ){
            if( error )
                connection.getTags().add( NesC12ASTModel.ERROR );
            if( warning )
                connection.getTags().add( NesC12ASTModel.WARNING );

            if( fileRegions != null ){
                for( FileRegion region : fileRegions ){
                    connection.addRegion( region );
                }
            }
        }

        if( node != null ){
            if( error )
                node.putErrorFlag();
            if( warning )
                node.putWarningFlag();

            if( fileRegions != null ){
                for( FileRegion region : fileRegions ){
                    node.addRegion( region );
                }
            }

            if( children != null ){
                for( int i = 0, n = children.size(); i<n; i += 2 ){
                    Object kind = children.get( i );
                    Object value = children.get( i+1 );
                    
                    if( kind == IS_CONNECTION ){
                        node.addChild( (ModelConnection)value );
                    }
                    else{
                    	Object ast = children.get( i+2 );
                    	i++;
                    	
                    	if( kind == IS_CHILD ){
                    		node.addChild( (ModelNode)value, (ASTNode)ast );
                    	}
                    	else if( kind == IS_REFERENCE ){
                    		node.addReference( (ModelNode)value, (ASTNode)ast );
                    	}
                    }
                }
            }
        }
    }
}
