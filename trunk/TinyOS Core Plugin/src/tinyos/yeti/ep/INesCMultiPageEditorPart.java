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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.editors.INesCEditorParserClient;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.outline.NesCOutlinePage;

/**
 * Represents a single page on the multi-page nesc editor.
 * @author Benjamin Sigg
 */
public interface INesCMultiPageEditorPart {
	/**
     * Gets the name of this page. 
     * @return the name that will be shown in the tab for this page
     */
    public String getPartName();
    
    /**
     * Creates the controls of this part.
     * @param parent the parent of the control
     * @param editor the editor in which the source code gets edited, a
     * {@link INesCEditorParserClient} might be added to the editor
     * @return the new control
     */
    public Control createControl( Composite parent, NesCEditor editor );
    
    /**
     * This method may be called several times and informs this part that
     * an outline view has been set.
     * @param outline the new outline view, might be <code>null</code>
     */
    public void setOutlinePage( NesCOutlinePage outline );
    
    /**
     * Informs this part that it either became selected (and visible) or 
     * lost focus. The initial value of this parameter is <code>false</code>.
     * @param selected <code>true</code> if visible
     */
    public void setSelected( boolean selected );
    
    /**
     * Tells this part to dispose its resources.
     */
    public void dispose();
}
