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
package tinyos.yeti.launch.veto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.jobs.SaveAllJob;
import tinyos.yeti.launch.ILaunchVeto;
import tinyos.yeti.preferences.PreferenceConstants;

public class SaveAllVeto implements ILaunchVeto{
	
	public boolean veto( IProgressMonitor monitor ){
		monitor.beginTask( "Save", 100 );
		List<ISaveablePart> parts = listPartsToSave();
		
		if( parts == null ){
			monitor.done();
			return true;
		}
		
		if( parts.size() > 0 ){
			final Display display = PlatformUI.getWorkbench().getDisplay();
			SaveAllJob save = new SaveAllJob( display, parts );
			save.run( new SubProgressMonitor( monitor, 100 ));
		}
		
		monitor.done();
		return false;
	}
	
	public static List<ISaveablePart> listPartsToSave(){
		final Display display = PlatformUI.getWorkbench().getDisplay();
		
		class Call implements Runnable{
			public List<ISaveablePart> part;
			
			public void run(){
				if( !display.isDisposed() ){	
					part = listPartsToSave( display.getActiveShell() );
				}
			}
		}
		
		Call call = new Call();
	 	display.syncExec( call );
	 	return call.part;
	}
	
	
    /**
     * Opens a dialog in which the user has to select some resources to store.
     * Returns the selected resources or <code>null</code> if the operation 
     * was canceled.
     * @param shell parent of the dialog
     * @return selected resources to store (may be empty) or <code>null</code>
     * if operation is canceled
     */
    public static List<ISaveablePart> listPartsToSave( Shell shell ){
    	IPreferenceStore store = TinyOSPlugin.getDefault().getPreferenceStore();
    	boolean safeAutomatically = store.getBoolean( PreferenceConstants.SAVE_FILES_AUTOMATICALLY );
    	
        List<IEditorPart> dirty = getDirtyEditors();
        if( (dirty == null) || (dirty.size() == 0) ){
            return Collections.emptyList();
        }
        if( safeAutomatically ){
        	List<ISaveablePart> result = new ArrayList<ISaveablePart>( dirty.size() );
        	for( IEditorPart part : dirty ){
        		result.add( part );
        	}
        	return result;
        }
        
        // Convert the list into an element collection.
        AdaptableList input = new AdaptableList(dirty);

        //ListSelectionDialog dlg = new ListSelectionDialog(new Shell(), input,
        SaveAllDialog dlg = new SaveAllDialog( shell, input,
                new BaseWorkbenchContentProvider(),
                new WorkbenchPartLabelProvider(), 
                "Select the resources to save.");

        dlg.setInitialSelections( dirty.toArray() );
        dlg.setTitle("Save Resources");
        int action = dlg.open();

        // Just return false to prevent the operation continuing
        if( action == IDialogConstants.CANCEL_ID ){
            return null;
        }

        if( dlg.saveAutomatically ){
        	store.setValue( PreferenceConstants.SAVE_FILES_AUTOMATICALLY, true );
        }
        
        Object[] result = dlg.getResult();
        if( result == null ){
            return null;
        }
        
        // If the editor list is empty return.
        if( result.length == 0 ){
            return Collections.emptyList();
        }
        
        List<ISaveablePart> saving = new ArrayList<ISaveablePart>( result.length );
        for( Object saveable : result ){
            saving.add( (ISaveablePart)saveable );
        }
        
        return saving;
    }
    
    /**
     * Gets a list of all the editors which contain unsaved resources.
     * @return the list of editors
     */
    private static List<IEditorPart> getDirtyEditors() {
        List<IEditorPart> al = new ArrayList<IEditorPart>();

        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            IWorkbenchPage[] pages = windows[i].getPages();
            for (int j = 0; j < pages.length; j++) {
                IWorkbenchPage page = pages[j];

                IEditorPart[] editors = page.getDirtyEditors();
                for( IEditorPart part : editors )
                    al.add( part );
            }
        }
        return al;
    }
    
    private static class SaveAllDialog extends ListSelectionDialog{
    	private boolean saveAutomatically = false;
    	
		public SaveAllDialog( Shell parentShell, Object input,
				IStructuredContentProvider contentProvider,
				ILabelProvider labelProvider, String message ){
			super( parentShell, input, contentProvider, labelProvider, message );
		}
    	
		@Override
		protected Control createDialogArea( Composite parent ){
			Composite composite = (Composite)super.createDialogArea( parent );
			
			final Button button = new Button( composite, SWT.CHECK );
			button.setSelection( saveAutomatically );
			button.setText( "Always save files and do not ask again." );
			button.addSelectionListener( new SelectionListener(){
				public void widgetDefaultSelected( SelectionEvent e ){
					saveAutomatically = button.getSelection();
				}
				public void widgetSelected( SelectionEvent e ){
					saveAutomatically = button.getSelection();
				}
			});
			
			button.setLayoutData( new GridData( SWT.LEFT, SWT.BOTTOM, true, false ) );
			
			return composite;
		}
    }
}
