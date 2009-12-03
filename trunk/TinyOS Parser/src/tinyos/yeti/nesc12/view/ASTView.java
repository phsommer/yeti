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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

/**
 * This view shows the AST-tree of the current open NesC-file.
 * @author Benjamin Sigg
 */
public class ASTView extends ParsingView{
    private Text text;
    
    public ASTView() {
        super( false );
    }

    @Override
    public void createPartControl( Composite parent ) {
        text = new Text( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY );
    }
    
    @Override
    protected Display getDisplay(){
        if( text == null || text.isDisposed() )
            return null;
        
        return text.getDisplay();
    }
    
    @Override
    protected void setAST(ASTNode root){
        if( text != null && !text.isDisposed() ){
            String deliver = root == null ? "no content" : root.toString();
            text.setText( deliver );
        }
    }

    @Override
    public void setFocus() {
        if( text != null )
            text.setFocus();
    }
}