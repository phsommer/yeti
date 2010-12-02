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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.dialogs.PreferencesUtil;

import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTarget;

public class EditProperties extends SelectionListenerAction{
	private Shell shell;
	private IProject selection;
	
	public EditProperties( Shell shell ){
		super( "Edit Default Target" );
		this.shell = shell;
	}
	
	public void dispose(){
		// ignore
	}

	@Override
	public void run(){
		if( selection != null && selection.isAccessible() ){
			String propertyPageId = "tinyOS.properties.maketarget.component";
			PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn( shell, selection, propertyPageId, null, selection );
			dialog.open();
		}
	}

	@Override
	protected boolean updateSelection( IStructuredSelection selection ){
		this.selection = null;
		
		if( selection.size() == 1 ){ 
			Object first = selection.getFirstElement();
			if( first instanceof IProject )
				this.selection = (IProject)first;
			else if( first instanceof IResource )
				this.selection = ((IResource)first).getProject();
			else if( first instanceof IAdaptable )
				this.selection = (IProject) ((IAdaptable)first).getAdapter( IProject.class );
			else if( first instanceof MakeTarget )
				this.selection = ((MakeTarget)first).getProject();
		}
		
		if( this.selection != null ){
			try{
				if( !this.selection.isAccessible() ){
					this.selection = null;
				}
				else if( !this.selection.hasNature( TinyOSCore.NATURE_ID )){
					this.selection = null;
				}
			}
			catch( CoreException ex ){
				TinyOSPlugin.log( ex );
				this.selection = null;
			}
		}
		
		return this.selection != null;
	}
	
}
