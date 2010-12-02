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

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.SelectionListenerAction;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.MakeTargetManager;

public class PasteTargetAction extends SelectionListenerAction {
	private IProject selection;
	private Clipboard clipboard;
	
    public PasteTargetAction( Display display ) {
        super("Paste");
        setToolTipText( "" );
        setAccelerator( SWT.CTRL | 'v' );
        clipboard = new Clipboard( display );
        //MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_EDIT); //$NON-NLS-1$
    }
    
    public void dispose(){
    	clipboard.dispose();
    }
    
    @Override
    public void run() {
    	if( selection != null ){
    		String content = (String)clipboard.getContents( TextTransfer.getInstance() );
    		if( content != null ){
    			MakeTargetManager manager = TinyOSPlugin.getDefault().getTargetManager();
    			
    			Collection<MakeTarget> targets = manager.convert( content, selection );
    			for( MakeTarget target : targets ){
    				manager.pasteTarget( target );
    			}
    		}
    	}
    }

    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
        boolean result = super.updateSelection( selection );
        updateProject( selection );
        return result && this.selection != null;
    }

    private void updateProject( IStructuredSelection selection ){
    	this.selection = null;
    	Object first = selection.getFirstElement();
    	if( first instanceof IProject ){
    		this.selection = (IProject)first;
    	}
    }
}
