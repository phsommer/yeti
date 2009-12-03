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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Font;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.utility.Icon;
import tinyos.yeti.views.cgraph.HoverManager;

/**
 * A figure factory is used to create new figures that show the contents
 * of some {@link IASTModelNode}s. Clients do not have to implement this
 * interface, an {@link IASTFigureFactory}-object will be provided by the
 * core plugin.
 * @author Benjamin Sigg
 */
public interface IASTFigureFactory {
    /**
     * Gets the big version of the font.
     * @return the big version
     */
    public Font getFontBig();
    
    /**
     * Gets the font that is normally used
     * @return the normal font
     */
    public Font getFontNormal();
    
    /**
     * Font to be used in tooltips.
     * @return the font
     */
    public Font getFontTooltip();
    
    /**
     * Bold version of the font to be used in tooltips.
     * @return the font
     */
    public Font getFontTooltipBold();
    
    /**
     * Gets the hover-manager to be used for additional hovering elements.
     * @return the manager
     */
    public HoverManager getHoverManager();
    
    /**
     * Gets the project for which this factory is used.
     * @return the project, might be <code>null</code>
     */
    public ProjectTOS getProject();
    
    /**
     * Gets an inspector to gain detailed information about the file for which
     * the figures are built.
     * @return the inspector, may be <code>null</code>
     */
    public INesCInspector getInspector();
    
    /**
     * Searches the node for <code>path</code>.
     * @param path some path
     * @param monitor informs the user about the state and can cancel the operation
     * @return the node for the path, can be <code>null</code>
     */
    public IASTModelNode getNode( IASTModelPath path, IProgressMonitor monitor );
    
    /**
     * Creates a figure for a specific node.
     * @param node the node for which the figure should be created
     * @param monitor informs the user about the state and can cancel the operation
     * @return the new figure
     */
    public IASTFigure create( IASTModelNode node, IProgressMonitor monitor );
    
    /**
     * Creates a figure for a connection. The factory might resolve the
     * connection and use the underlying {@link IASTModelNode}.
     * @param connection the connection for which the figure should be created
     * @param monitor informs the user about the state and can cancel the operation
     * @return the new figure
     */
    public IASTFigure create( IASTModelNodeConnection connection, IProgressMonitor monitor );
    
    /**
     * Creates a figure for a declaration. The factory might parse the file
     * from which the declaration comes and then use the {@link IASTModelNode}
     * that issued the declaration.
     * @param declaration the declaration for which the figure should be created
     * @param monitor informs the user about the state and can cancel the operation
     * @return the new figure
     */
    public IASTFigure create( IDeclaration declaration, IProgressMonitor monitor );
    
    /**
     * Creates a figure for an unknown path.
     * @param path the path to resolve, might be <code>null</code>
     * @param label backup text if path can't be resolved
     * @param icon backup icon if path can't be resolved
     * @param monitor for interaction with the user
     * @return a new figure
     */
    public IASTFigure create( IASTModelPath path, String label, Icon icon, IProgressMonitor monitor );
}
