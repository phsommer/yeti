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
package tinyos.yeti.ep;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.IEditorTokenScanner;

/**
 * Default implementation of {@link IEditorDamageRepairer}.
 * @author Benjamin Sigg
 *
 */
public class DefaultEditorDamageRepairer extends DefaultDamagerRepairer implements IEditorDamageRepairer{
    private IEditorTokenScanner scanner;
    
    public DefaultEditorDamageRepairer( IEditorTokenScanner scanner ){
        super( scanner );
        this.scanner = scanner;
    }
    
    @Deprecated
    public DefaultEditorDamageRepairer( IEditorTokenScanner scanner, TextAttribute defaultTextAttribute ){
        super( scanner, defaultTextAttribute );
        this.scanner = scanner;
    }
    
    public void setEditor( NesCEditor editor ){
        scanner.setEditor( editor );
    }
    
    public IPresentationDamager asDamager(){
        return this;
    }
    
    public IPresentationRepairer asRepairer(){
        return this;
    }
}
