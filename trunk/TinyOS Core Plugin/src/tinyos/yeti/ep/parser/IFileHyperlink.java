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
 * Represents the target of a hyperlink.
 * @author Benjamin Sigg
 */
public interface IFileHyperlink{
    /**
     * Gets the file to which this hyperlink points.
     * @return the file
     */
    public IParseFile getParseFile();
    
    /**
     * Gets the region to which the hyperlink points. This value is
     * optional.
     * @return the target region, can be <code>null</code>
     */
    public IFileRegion getTargetRegion();
    
    /**
     * Gets the region which represents this hyperlink.
     * @return the source region
     */
    public IFileRegion getSourceRegion();
    
    /**
     * Gets a short text describing this hyperlink.
     * @return the text or <code>null</code>
     */
    public String getHyperlinkName();
    
    /**
     * Gets the type of this hyperlink, may be presented to the user.
     * @return the type or <code>null</code>
     */
    public String getHyperlinkType();
}
