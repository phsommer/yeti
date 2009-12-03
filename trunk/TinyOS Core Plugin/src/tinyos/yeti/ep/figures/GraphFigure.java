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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTModelPath;

/**
 * A figure that draws a graph with {@link IASTFigure}s.
 * @author Benjamin Sigg
 */
public class GraphFigure extends ASTFigure implements IRepresentation{
    private final int GAP = 20;

    private Dimension lastPreferred;

    private NodeList nodes = new NodeList();
    private EdgeList edges = new EdgeList();

    private Map<IASTFigure, Node> figureNodes = new HashMap<IASTFigure, Node>();

    private ConnectionLayer connectionLayer;

    public GraphFigure( Font font ){
        setFont( font );
        setLayoutManager( new XYLayout() );

        connectionLayer = new ConnectionLayer();
        connectionLayer.setConnectionRouter( new ShortestPathConnectionRouter( this ) );

        add( connectionLayer );
    }
    
    @Override
    public void validate() {
        connectionLayer.setBounds( new Rectangle( getBounds() ) );
        super.validate();
    }

    @SuppressWarnings("unchecked")
    public void addNode( IASTFigure node ){
        Node n = new Node( node );
        nodes.add( n );
        figureNodes.put( node, n );

        add( node );
    }

    @SuppressWarnings("unchecked")
    public void addEdge( IASTFigure source, IASTFigure target, String text, boolean dotted, IASTModelPath path, boolean arrow ){
        Node s = figureNodes.get( source );
        Node t = figureNodes.get( target );

        // reuse edge if already present
        for( int i = 0, n = edges.size(); i<n; i++ ){
            Edge edge = edges.getEdge( i );
            if( edge.source == s && edge.target == t ){
                GraphConnectionFigure connection = (GraphConnectionFigure)edge.data;
                if( arrow )
                	connection.setTargetDecoration();
                
                connection.add( path, text );
                if( !dotted )
                    connection.setDotted( dotted );
                return;
            }
            if( edge.target == s && edge.source == t ){
                GraphConnectionFigure connection = (GraphConnectionFigure)edge.data;
                if( arrow )
                	connection.setSourceDecoration();
                
                connection.add( path, text );
                if( !dotted )
                    connection.setDotted( dotted );
                return;
            }
        }

        GraphConnectionFigure connection = new GraphConnectionFigure( source, target, getFont() );
        connection.setDotted( dotted );
        if( arrow )
        	connection.setTargetDecoration();
        connection.add( path, text );
        edges.add( new Edge( connection, s, t ));

        connectionLayer.add( connection );
    }

    @Override
    public void collapseAST() {
        for( IASTFigure figure : figureNodes.keySet() ){
            figure.collapseAST();
        }
    }

    @Override
    public void expandAST( int depth, final IExpandCallback callback ){
        if( depth > 0 ){
            depth--;

            ConvergingExpandCallback converge = new ConvergingExpandCallback( this, figureNodes.keySet(), callback ){
                @Override
                protected void expanded() {
                    asyncLayoutAST( callback );
                }
            };

            for( IASTFigure figure : figureNodes.keySet() ){
                figure.expandAST( depth, converge );
            }
        }
        else{
            layoutAST();
            if( callback != null )
                callback.expanded( this );
        }
    }

    private void asyncLayoutAST( final IExpandCallback callback ){
        Job job = new UIJob( "Update Layout" ){
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                layoutAST();
                if( callback != null )
                    callback.expanded( GraphFigure.this );
                return Status.OK_STATUS;
            }
        };

        job.setSystem( true );
        job.setPriority( Job.INTERACTIVE );
        job.schedule();
    }

    @Override
    public Dimension getPreferredSize( int hint, int hint2 ) {
        Dimension size = super.getPreferredSize( hint, hint2 );
        if( lastPreferred == null )
            return size;

        return new Dimension( 
                Math.min( size.width + GAP, Math.max( size.width, lastPreferred.width )),
                Math.min( size.height + GAP, Math.max( size.height, lastPreferred.height )));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void layoutAST(){
        if( nodes.size() == 0 )
            return;

        // update node size
        for( Map.Entry<IASTFigure, Node> entry : figureNodes.entrySet() ){
            IASTFigure figure = entry.getKey();
            Node node = entry.getValue();

            figure.layoutAST();
            Dimension size = figure.getPreferredSize();
            node.width = size.width + GAP;
            node.height = size.height + GAP;
        }

        DirectedGraphLayout layout = new DirectedGraphLayout();
        DirectedGraph dg = new DirectedGraph();

        NodeList nodesCopy = new NodeList();
        Map<Node, Node> nodeCopies = new HashMap<Node, Node>();
        for( int i = 0, n = nodes.size(); i<n; i++ ){
            Node old = nodes.getNode( i );
            Node node = new Node( old.data );
            Dimension preferred = ((Figure)old.data).getPreferredSize();
            node.width = preferred.width;
            node.height = preferred.height;
            nodesCopy.add( node );
            nodeCopies.put( old, node );
        }

        EdgeList edgesCopy = new EdgeList();
        for( int i = 0, n = edges.size(); i<n; i++ ){
            Edge edge = edges.getEdge( i );
            edgesCopy.add( new Edge( edge.data, nodeCopies.get( edge.source ), nodeCopies.get( edge.target ) ) );
        }

        dg.edges = edgesCopy;
        dg.nodes = nodesCopy;

        layout.visit(dg);

        int minX = GAP;
        int minY = GAP;

        for( int i = 0; i < nodes.size(); i++ ){
            Node n = dg.nodes.getNode( i );
            minX = Math.min( minX, n.x );
            minY = Math.min( minY, n.y );
        }

        int dx = GAP - minX;
        int dy = GAP - minY;

        int maxX = 0;
        int maxY = 0;

        for( int i = 0; i < nodes.size(); i++ ){
            Node n = dg.nodes.getNode( i );
            IASTFigure figure = (IASTFigure)(n.data);
            // figure.setLocation( new Point( n.x+dx, n.y+dy ) );
            setConstraint( figure, new Rectangle( n.x+dx, n.y+dy, -1, -1 ) );

            maxX = Math.max( maxX, n.x + dx + n.width );
            maxY = Math.max( maxY, n.y + dy + n.height );
        }

        lastPreferred = new Dimension( maxX + GAP, maxY + GAP );
    }
}
