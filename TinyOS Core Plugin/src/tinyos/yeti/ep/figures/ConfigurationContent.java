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
package tinyos.yeti.ep.figures;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;

/**
 * A content that can be used to build the graph of a configuration.
 * @author Benjamin Sigg
 */
public class ConfigurationContent extends GraphContent{
    public static final IGenericFactory<ConfigurationContent> FACTORY = new ReferenceFactory<ConfigurationContent>( GraphContent.FACTORY ){
        public ConfigurationContent create(){
            return new ConfigurationContent();
        }
        
        @Override
        public void write( ConfigurationContent value, IStorage storage ) throws IOException{
            super.write( value, storage );
            
            List<IASTFigureContent> nodes = value.getNodes();
            DataOutputStream out = storage.out();
            out.writeInt( value.namedNodes.size() );
            for( Map.Entry<String, IASTFigureContent> entry : value.namedNodes.entrySet() ){
                out.writeUTF( entry.getKey() );
                out.writeInt( nodes.indexOf( entry.getValue() ) );
            }
        }
        
        @Override
        public ConfigurationContent read( ConfigurationContent value, IStorage storage ) throws IOException{
            super.read( value, storage );
            
            List<IASTFigureContent> nodes = value.getNodes();
            DataInputStream in = storage.in();
            int size = in.readInt();
            for( int i = 0; i < size; i++ ){
                String name = in.readUTF();
                int index = in.readInt();
                value.namedNodes.put( name, nodes.get( index ) );
            }
            
            return value;
        }
    };
    
    private Map<String, IASTFigureContent> namedNodes = new HashMap<String, IASTFigureContent>();
    
    public void addUses( IASTFigureContent content, String rename ){
        namedNodes.put( rename, content );
        addNode( content );
    }
    
    public void addProvides( IASTFigureContent content, String rename ){
        namedNodes.put( rename, content );
        addNode( content );
    }
    
    public void addComponent( IASTFigureContent content, String rename ){
        namedNodes.put( rename, content );
        addNode( content );
    }
    
    public void addWireLeftToRight( String left, String right, String text, boolean dot, IASTModelPath path ){
        IASTFigureContent leftNode = namedNodes.get( left );
        IASTFigureContent rightNode = namedNodes.get( right );
        
        if( leftNode != null && rightNode != null )
            addEdge( leftNode, rightNode, text, dot, path, true );
    }
    
    public void addWireRightToLeft( String left, String right, String text, boolean dot, IASTModelPath path ){
        IASTFigureContent leftNode = namedNodes.get( left );
        IASTFigureContent rightNode = namedNodes.get( right );
        
        if( leftNode != null && rightNode != null )
            addEdge( rightNode, leftNode, text, dot, path, true );
    }

    public void addWireEqual( String left, String right, String text, boolean dot, IASTModelPath path ){
        IASTFigureContent leftNode = namedNodes.get( left );
        IASTFigureContent rightNode = namedNodes.get( right );
        
        if( leftNode != null && rightNode != null )
            addEdge( leftNode, rightNode, text, dot, path, false );
    }
}
