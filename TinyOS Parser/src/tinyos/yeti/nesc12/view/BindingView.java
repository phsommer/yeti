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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import tinyos.yeti.nesc12.parser.ast.elements.BindingTreeNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;

/**
 * A view that shows the bindings of the current selected nesc file.
 * @author Benjamin Sigg
 */
public class BindingView extends ParsingView {
    private TreeViewer viewer;
    
    public BindingView(){
        super( true );
    }
    
    @Override
    protected Display getDisplay(){
        if( viewer == null || viewer.getControl().isDisposed())
            return null;
        
        return viewer.getControl().getDisplay();
    }
    
    @Override
    protected void setAST( ASTNode root ) {
        if( viewer != null && !viewer.getControl().isDisposed() ){
            if( root instanceof TranslationUnit ){
                viewer.setInput( new BindingTreeNode( null, ((TranslationUnit)root).resolve() ) );
            }
            else{
                viewer.setInput( new BindingTreeNode( null, null ) );
            }
        }
    }

    @Override
    public void createPartControl( Composite parent ) {
        viewer = new TreeViewer( parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.setContentProvider( new BindingTreeContentProvider() );
        viewer.setInput( new BindingTreeNode( null, null ) );
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}
