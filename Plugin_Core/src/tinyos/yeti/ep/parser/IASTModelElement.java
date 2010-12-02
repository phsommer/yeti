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

import tinyos.yeti.ep.IParseFile;

/**
 * The shared properties of {@link IASTModelNodeConnection} and
 * {@link IASTModelNode}. This interface describes elements that can occure
 * in an {@link IASTModel}.
 * @author Benjamin Sigg
 */
public interface IASTModelElement {
    /**
     * Gets the tags that are associated with this element.
     * @return the tags
     */
    public TagSet getTags();
    
    /**
     * Gets a set of attributes that has been attached to this element.
     * @return the attributes or <code>null</code>
     */
    public IASTModelAttribute[] getAttributes();
	
    /**
     * Gets the identifier associated with this element.
     * @return the name of the child
     */
    public String getIdentifier();
    
    /**
     * Gets a text that can be presented to the user.
     * @return the user friendly text
     */
    public String getLabel();

    /**
     * Gets the path to the file that was parsed when this node was created. This 
     * is always the first file that was given to the parser. It does not have
     * to be the same file as the file in which the code for this node actually
     * stands.
     * @return the file that was given to the parser
     */
    public IParseFile getParseFile();
    
    /**
     * Gets the file region in which this element was defined. This region is
     * used to mark the element in the editor, hence it may not cover the entire
     * definition but merrily the name of the element.
     * @return the region or <code>null</code>
     */
    public IFileRegion getRegion();
    
    /**
     * Gets the file regions in which this element was defined. Normally there
     * would be only one region, but various circumstances can lead to more
     * than one region:
     * <ul>
     * 	<li>Include directives and macros may create an element from many files</li>
     *  <li>The regions are separated in (maybe overlapping) regions to provide
     *  more details</li>
     * </ul> 
     * The first entry of the result should be {@link #getRegion()}.
     * @return the regions, can be <code>null</code> but does not contain
     * <code>null</code> entries
     */
    public IFileRegion[] getRegions();
}
