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

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.MakeTargetManager;

public class DeleteTargetAction extends SelectionListenerAction {

    private Shell shell;

    public DeleteTargetAction( Shell shell ){
        super("Delete"); 
        this.shell = shell;

        setToolTipText("");
        //MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_DELETE); //$NON-NLS-1$
    }

    /**
     * Asks the user to confirm a delete operation.
     *
     * @return <code>true</code> if the user says to go ahead, and <code>false</code>
     *  if the deletion should be abandoned
     */
    boolean confirmDelete() {
        List<?> targets = getTargetsToDelete();
        String title;
        String msg;
        if( targets.size() == 1 ){
            title = "Confim Delete";
            MakeTarget target = (MakeTarget) targets.get(0);
            msg = MessageFormat.format("Do you really want do delete '"+ target.getName() +"'?", new Object[] { target.getName()});
        }
        else {
            title = "Delete multiple Targets";
            msg =
                MessageFormat.format(
                        "Delete multiple targets at once?",
                        new Object[] { new Integer(targets.size())});
        }
        return MessageDialog.openQuestion( shell, title, msg );
    }

    @Override
    public void run() {
        List<?> targets = getTargetsToDelete();
        
        if (!canDelete( targets ) || confirmDelete() == false)
            return;
        
        MakeTargetManager manager = TinyOSPlugin.getDefault().getTargetManager();
        Iterator<?> iter = targets.iterator();
        
        while (iter.hasNext()) {
        	manager.removeTarget((MakeTarget) iter.next());
        }
    }

    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
        return super.updateSelection(selection) && canDelete( getTargetsToDelete() );
    }

    private List<?> getTargetsToDelete() {
        return getStructuredSelection().toList();
    }

    private boolean canDelete( List<?> elements ) {
        if (elements.size() > 0) {
            Iterator<?> iterator = elements.iterator();
            while (iterator.hasNext()) {
                if (!(iterator.next() instanceof MakeTarget)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
