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
package tinyos.yeti.ep.parser.inspection;

import org.eclipse.core.resources.IProject;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.INesCParser;

/**
 * An {@link INesCInspector} provides high level information about
 * a file. Inspectors are created by {@link INesCParser}s.<br>
 * {@link INesCInspector}s are an optional addition to {@link INesCParser}s
 * and are intended to be used by plugins that need some information about
 * the source code, but should not depend on a special implementation of a 
 * parsers. The core plugin itself does not use inspectors at all.
 * @author Benjamin Sigg
 */
public interface INesCInspector{
	/** 
	 * Gets the project in which this inspector operates.
	 * @return the project
	 */
	public IProject getProject();
	
	/**
	 * Gets the root node of this inspector.
	 * @return the root node, may be <code>null</code>
	 */
	public INesCNode getRoot();
	
	/**
	 * Tries to find a representation of <code>node</code>.
	 * @param node some node, this node should have its origin in the same
	 * file as was parsed to obtain this inspector.
	 * @return the representation or <code>null</code> if not available
	 */
	public INesCNode getNode( IASTModelNode node );
	
	/**
	 * Informs this inspector that its content will be used.
	 */
	public void open();
	
	/**
	 * Called when a module does no longer need the contents of this
	 * inspector. If the number of calls to {@link #close()} reaches
	 * the number of calls to {@link #open()} the inspector can release
	 * its content. 
	 */
	public void close();
}
