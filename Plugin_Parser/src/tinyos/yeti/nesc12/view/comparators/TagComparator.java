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
package tinyos.yeti.nesc12.view.comparators;

import java.util.Comparator;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;

/**
 * Compares two {@link TagSet}s and the one which contains a
 * certain tag will be considered to be smaller.
 * @author Benjamin Sigg
 */
public class TagComparator implements Comparator<TagSet>{
    private Tag tag;
    
    public TagComparator( Tag tag ){
        this.tag = tag;
    }
    
    public int compare( TagSet a, TagSet b ){
        boolean aSet = a.contains( tag );
        boolean bSet = b.contains( tag );
        
        if( aSet == bSet )
            return 0;
        
        if( aSet )
            return -1;
        
        return 1;
    }
}
