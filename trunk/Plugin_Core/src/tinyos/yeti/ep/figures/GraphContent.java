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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.Debug;
import tinyos.yeti.ep.parser.*;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

/**
 * A content that creates a {@link GraphFigure}.
 * @author Benjamin Sigg
 */
public class GraphContent implements IASTFigureContent{
    public static final IGenericFactory<GraphContent> FACTORY = new IGenericFactory<GraphContent>(){
        public GraphContent create(){
            return new GraphContent();
        }
        
        public void write( GraphContent value, IStorage storage ) throws IOException{
            DataOutputStream out = storage.out();
            
            // nodes
            out.writeInt( value.nodes.size() );
            for( IASTFigureContent content : value.nodes ){
                storage.write( content );
            }
            
            // edges
            out.writeInt( value.edges.size() );
            for( Edge edge : value.edges ){
                out.writeInt( value.nodes.indexOf( edge.source ) );
                out.writeInt( value.nodes.indexOf( edge.target ) );
                out.writeBoolean( edge.dotted );
                out.writeBoolean( edge.arrow );
                storage.writeString( edge.text );
                storage.write( edge.path );
            }
            
            // paths
            out.writeInt( value.paths.size() );
            for( IASTModelPath path : value.paths ){
                storage.write( path );
            }
        }
        
        public GraphContent read( GraphContent value, IStorage storage ) throws IOException{
            DataInputStream in = storage.in();
            
            // nodes
            int size = in.readInt();
            for( int i = 0; i < size; i++ ){
                value.nodes.add( storage.<IASTFigureContent>read() );
            }
            
            // edges
            size = in.readInt();
            for( int i = 0; i < size; i++ ){
                Edge edge = new Edge();
                edge.source = value.nodes.get( in.readInt() );
                edge.target = value.nodes.get( in.readInt() );
                edge.dotted = in.readBoolean();
                edge.arrow = in.readBoolean();
                edge.text = storage.readString();
                edge.path = storage.read();
                value.edges.add( edge );
            }
            
            // paths
            size = in.readInt();
            for( int i = 0; i < size; i++ ){
                IASTModelPath path = storage.read();
                value.paths.add( path );
            }
            
            return value;
        }
    };
    
    private List<IASTFigureContent> nodes = new ArrayList<IASTFigureContent>();
    private List<Edge> edges = new ArrayList<Edge>();
    private List<IASTModelPath> paths = new ArrayList<IASTModelPath>();
    
    public void addNode( IASTFigureContent node ){
        if( node == null )
            throw new IllegalArgumentException( "node must not be null" );
        
        nodes.add( node );
    }
    
    protected List<IASTFigureContent> getNodes(){
        return nodes;
    }
    
    /**
     * Adds another path that will be returned by {@link IRepresentation#getPaths()}.
     * @param path the additional path
     */
    public void addPath( IASTModelPath path ){
    	paths.add( path );
    }
    
    /**
     * Adds an arrow pointing from <code>source</code> to <code>target</code>.
     * @param source the source node, not <code>null</code>
     * @param target the target node, not <code>null</code>, not <code>source</code>
     * @param text text that should be shown beside the arrow, can be <code>null</code>
     * @param dotted whether the line should be drawn dotted or solid
     * @param path which {@link IASTModelNode} the edge represents, can be <code>null</code>
     * @param arrow whether to paint an arrow or not
     */
    public void addEdge( IASTFigureContent source, IASTFigureContent target, String text, boolean dotted, IASTModelPath path, boolean arrow ){
        if( source == null )
            throw new IllegalArgumentException( "source must not be null" );
        
        if( target == null )
            throw new IllegalArgumentException( "target must not be null" );
        
        if( source == target ){
            throw new IllegalArgumentException( "no self references allowed" );
        }
        
        if( Debug.DEBUG ){
            if( !nodes.contains( source ) )
                throw new IllegalArgumentException( "Source is unknown: " + source );

            if( !nodes.contains( target ) )
                throw new IllegalArgumentException( "Target is unknown: " + target );
        }
        
        Edge edge = new Edge();
        edge.source = source;
        edge.target = target;
        edge.dotted = dotted;
        edge.text = text;
        edge.path = path;
        edge.arrow = arrow;
        edges.add( edge );
    }
    
    public IASTFigure createContent( IASTFigureFactory factory, IProgressMonitor monitor ) {
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        int size = nodes.size() + edges.size();
        monitor.beginTask( "Update Graph Content", size );
        
        GraphFigure graph = new GraphFigure( factory.getFontNormal() );
        graph.setPaths( paths );
        
        Map<IASTFigureContent, IASTFigure> figures = new HashMap<IASTFigureContent, IASTFigure>();
        for( IASTFigureContent content : nodes ){
            IASTFigure figure = content.createContent( factory, new SubProgressMonitor( monitor, 1 ) );
            if( monitor.isCanceled() ){
                monitor.done();
                return null;
            }
            monitor.worked( 1 );
            figures.put( content, figure );
            graph.addNode( figure );
        }
        
        for( Edge edge : edges ){
            IASTFigure source = figures.get( edge.source );
            IASTFigure target = figures.get( edge.target );
            graph.addEdge( source, target, edge.text, edge.dotted, edge.path, edge.arrow );
            monitor.worked( 1 );
        }
        
        return graph;
    }
    
    private static class Edge{
        public IASTFigureContent source;
        public IASTFigureContent target;
        public boolean dotted;
        public String text;
        public IASTModelPath path;
        public boolean arrow;
    }
}
