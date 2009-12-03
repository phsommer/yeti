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
package tinyos.yeti.views.make;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.SelectionListenerAction;

import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.ProjectTargets;

/**
 * Copies the currently selected make target(s) into the clipboard.
 * @author Benjamin Sigg
 */
public class CopyTargetAction extends SelectionListenerAction {
	private Set<MakeTarget> selection = new HashSet<MakeTarget>();
	private Clipboard clipboard;
	
    public CopyTargetAction( Display display ) {
        super("Copy");
        setToolTipText( "" );
        clipboard = new Clipboard( display );
        //MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_EDIT); //$NON-NLS-1$
    }
    
    public void dispose(){
    	clipboard.dispose();
    }
    
    @Override
    public void run() {
    	if( selection.size() > 0 ){
    		String content = ProjectTargets.convert( selection );
    		clipboard.setContents( new Object[]{ content }, new Transfer[]{ TextTransfer.getInstance() } );
    	}
    }

    public Set<MakeTarget> getSelection() {
		return selection;
	}
    
    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
        boolean result = super.updateSelection( selection );
        updateList( selection );
        return result && (this.selection.size() > 0);
    }

    private void updateList( IStructuredSelection selection ){
    	this.selection.clear();
        for( Object element : selection.toArray() ){
        	if( element instanceof MakeTarget ){
        		this.selection.add( (MakeTarget)element );
        	}
        }
    }
}
