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
package tinyos.yeti.widgets.helper;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import tinyos.yeti.widgets.ITreeFilter;

/**
 * A filter used in conjunction with <code>FilteredTree</code>.
 */
public class PatternFilter extends ViewerFilter implements ITreeFilter {

    private Map<Object, Object[]> cache = new HashMap<Object, Object[]>();

    private StringMatcher matcher;

    public PatternFilter() {

    }
    
    public ViewerFilter filter( String filter ){
        setPattern( filter );
        return this;
    }

    @Override
    public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
        if (matcher == null )
            return elements;
        
        Object[] filtered = cache.get(parent);
        if (filtered == null) {
            filtered = super.filter(viewer, parent, elements);
            cache.put(parent, filtered);
        }
        return filtered;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        String labelText = ((ILabelProvider) ((StructuredViewer) viewer)
                .getLabelProvider()).getText(element);

        if((labelText!=null)&&(match(labelText))) return true;

        return false;
    }

    /**
     * 
     * @param patternString
     */
    public void setPattern(String patternString) {
        cache.clear();
        if (patternString == null || patternString.equals("")) //$NON-NLS-1$
            matcher = null;
        else
            matcher = new StringMatcher("*"+patternString + "*", true, false); //$NON-NLS-1$
    }

    /**
     * Answers whether the given String matches the pattern.
     * 
     * @param string the String to test
     * @return whether the string matches the pattern
     */
    protected boolean match(String string) {
    	if( matcher == null )
    		return true;
        return matcher.match(string);
    }

}