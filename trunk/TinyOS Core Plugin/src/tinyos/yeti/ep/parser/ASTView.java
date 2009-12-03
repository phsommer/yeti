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

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchSite;

/**
 * Describes the contents of one panel of the "all files view".
 * @author Benjamin Sigg
 */
public class ASTView{
    private String label;
    private Image image;
    private IASTModelNodeFilter filter;
    private boolean selectionProvider;
    
    /**
     * Creates a new description.
     * @param label the name of the tab
     * @param image the icon of the tab
     * @param filter the filter that tells which nodes are roots
     * @param selectionProvider whether the tree created by this view should
     * become the selection provider, see {@link IWorkbenchSite#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)} 
     */
    public ASTView( String label, Image image, IASTModelNodeFilter filter, boolean selectionProvider ){
	this.label = label;
	this.image = image;
	this.filter = filter;
	this.selectionProvider = selectionProvider;
    }
    
    public String getLabel(){
	return label;
    }
    
    public Image getImage(){
	return image;
    }
    
    public IASTModelNodeFilter getFilter(){
        return filter;
    }
    
    public boolean isSelectionProvider(){
	return selectionProvider;
    }
}
