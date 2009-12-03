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
import java.util.List;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;

public class ASTModelNode extends ASTModelLeaf{
    private List<IASTModelNodeConnection> children = new ArrayList<IASTModelNodeConnection>();

    public ASTModelNode( IASTModelNode parent, String identifier, String name, String label, IParseFile file, IFileRegion[] origin, Tag... tags ){
        super( parent, identifier, name, label, file, origin, tags );
    }

    public ASTModelNode( IASTModelNode parent, String identifier, String name, String label, IParseFile file, IFileRegion[] origin, TagSet tags ){
        super( parent, identifier, name, label, file, origin, tags );
    }

    public void addConnection( IASTModelNodeConnection connection ){
        if( connection == null )
            throw new IllegalArgumentException( "connection must not be null" );
        
        if( !connection.getPath().equals( getPath() ))
            throw new IllegalArgumentException( "connections path must equal this path" );
        
        children.add( connection );
    }
    
    public ASTModelNodeConnection addChild( IASTModelNode node ){
        return addChild( node.getIdentifier(), node.getLabel(), node.getRegions(), node.getTags() );
    }
    
    public ASTModelNodeConnection addChild( String identifier, String label, IFileRegion[] regions, TagSet relation ){
        ASTModelNodeConnection connection = new ASTModelNodeConnection( this, false, identifier, label, regions, relation );
        children.add( connection );
        return connection;
    }

    public ASTModelNodeConnection addReference( String identifier, String label, IFileRegion[] regions, TagSet relation ){
        ASTModelNodeConnection connection = new ASTModelNodeConnection( this, true, identifier, label, regions, relation );
        children.add( connection );
        return connection;
    }

    public int getChildrenCount(){
        return children.size();
    }

    @Override
    public IASTModelNodeConnection[] getChildren(){
        return children.toArray( new IASTModelNodeConnection[ children.size() ] );
    }
}
