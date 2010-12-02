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
package tinyos.yeti.views.cgraph;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Node;

/**
 * Custom Layoutmanager with additional capabilities to 
 * compact the given graph
 *
 */
public class MyGraphLayout implements LayoutManager {
	
		private XYLayout xy;

		private DirectedGraph graph;
	
		public MyGraphLayout() {
			xy = new XYLayout();
		}

		public Object getConstraint(IFigure child) {
			return xy.getConstraint(child);
		}

		public Dimension getMinimumSize(IFigure container, int wHint, int hHint) {
			return xy.getMinimumSize(container,wHint,hHint);
		}

		public Dimension getPreferredSize(IFigure container, int wHint, int hHint) {
			return xy.getPreferredSize(container,wHint,hHint);
		}

		public void invalidate() {
			xy.invalidate();
		}

		public void layout(IFigure container) {
			xy.layout(container);
		}

		public void remove(IFigure child) {
			xy.remove(child);
		}

		public void setConstraint(IFigure child, Object constraint) {
			xy.setConstraint(child,constraint);
		}

		public void setGraph(DirectedGraph dg) {
			this.graph = dg;
		};

		public void compactGraph() {
			if (graph == null) return;
			for (int i = 0; i < graph.nodes.size(); i++) {
				Node node = (Node) graph.nodes.get(i);
				IFigure fff = (IFigure) node.data;
				node.width = fff.getPreferredSize().width;
				node.height = fff.getPreferredSize().height;
			}
			
			DirectedGraphLayout dgl = new DirectedGraphLayout();
			dgl.visit(graph);
			
			for (int i = 0; i < graph.nodes.size(); i++) {
				Node n = (Node)graph.nodes.get(i);
				IFigure child = (IFigure) n.data;
				setConstraint(child,new Rectangle(n.x,n.y,-1,-1));
			}
		}
}
