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
package tinyos.yeti.views;

import java.text.Collator;
import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.views.NodeContentProvider.Element;

public class NodeLocationComparator extends ViewerComparator implements Comparator<NodeContentProvider.Element>{
    private Comparator<TagSet> tagComparator = TinyOSPlugin.getDefault().getParserFactory().createComparator();
    private Collator stringComparator = Collator.getInstance();

    private String[] properties;
    
    public NodeLocationComparator( String... properties ){
    	this.properties = properties;
    }
    
    public int compare( Element a, Element b ){
        TagSet tagsA = a.getTags();
        TagSet tagsB = b.getTags();

        int check = tagComparator.compare( tagsA, tagsB );
        if( check != 0 )
            return check;

        IFileRegion regionA = a.getRegion();
        IFileRegion regionB = b.getRegion();

        if( regionA != null && regionB != null ){
        	int offsetA = regionA.getOffset();
        	int offsetB = regionB.getOffset();
        	if( offsetA != offsetB ){
        		if( offsetA < offsetB )
        			return -1;
        		else
        			return 1;
        	}
        }
        else if( regionA != null ){
        	return -1;
        }
        else if( regionB != null ){
        	return 1;
        }
        
        String labelA = a.getLabel();
        String labelB = b.getLabel();

        if( labelA == null && labelB == null )
            return 0;

        if( labelA == null )
            return 1;

        if( labelB == null )
            return -1;

        return stringComparator.compare( labelA, labelB );
    }

    @Override
    public int compare( Viewer viewer, Object e1, Object e2 ){
    	if( e1 instanceof Element && e2 instanceof Element )
    		return compare( (Element)e1, (Element)e2 );
    	else
    		return 0;
    }
    
    @Override
    public boolean isSorterProperty(Object element, String property) {
    	if( this.properties == null )
    		return true;
    	
    	for( String check : properties ){
    		if( check.equals( property )){
    			return true;
    		}
    	}
    	
    	return false;
    }
}
