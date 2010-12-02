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
package tinyos.yeti.nesc12.parser.ast.elements;


/**
 * A binding is concentrated information about one or many elements of the ast.
 * Each binding is parted in segments, each segment represents a group of
 * referenced bindings. Segments are just here to make reading more easy,
 * they do not have any semantic meaning.<br>
 * Please note that there are no limitations how bindings references each other.
 * Circles, even self-references, are allowed.<br>
 * Bindings can contain <code>null</code> references. They are either generated
 * by invalid source code, or when a reference is optional.
 *  
 * @author Benjamin Sigg
 */
public interface Binding {
    /**
     * Gets a small, human readable name for this binding.
     * @return the name of this binding, not <code>null</code>
     */
    public String getBindingType();
    
    /**
     * Gets a small, human readable text that represents the value of this binding.
     * @return the value, can be <code>null</code>
     */
    public String getBindingValue();
    
    /**
     * Gets the number of segments that this binding contains.
     * @return the number of segments, at least 1.
     */
    public int getSegmentCount();
    
    /**
     * Gets the name of the segment with index <code>segment</code>.
     * @param segment the index of a segment
     * @return the name of the segment, can be <code>null</code> if this
     * binding has only one segment
     */
    public String getSegmentName( int segment );
    
    /**
     * Gets the number of children the segment <code>segment</code> has.
     * @param segment a segment
     * @return the number of children the segment
     */
    public int getSegmentSize( int segment );
    
    /**
     * Gets the <code>index</code>'th child of the segment <code>segment</code>.
     * @param segment the segment.
     * @param index the index of a child in the segment
     * @return the child, may be <code>null</code>
     */
    public Binding getSegmentChild( int segment, int index );
}
