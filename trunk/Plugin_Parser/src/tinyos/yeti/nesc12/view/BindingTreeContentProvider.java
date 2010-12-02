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
package tinyos.yeti.nesc12.view;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import tinyos.yeti.nesc12.parser.ast.elements.BindingTreeNode;

public class BindingTreeContentProvider implements ITreeContentProvider{
    public void dispose() {

    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        TreeViewer tree = (TreeViewer)viewer;
        tree.refresh();
    }

    public Object[] getChildren( Object parentElement ) {
        return ((BindingTreeNode)parentElement).getChildren();
    }

    public Object getParent( Object element ) {
        return ((BindingTreeNode)element).getParent();
    }

    public boolean hasChildren( Object element ) {
        return !((BindingTreeNode)element).isLeaf();
    }

    public Object[] getElements( Object inputElement ) {
        return getChildren( inputElement );
    }
}
