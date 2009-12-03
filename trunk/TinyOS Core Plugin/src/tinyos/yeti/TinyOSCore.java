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
package tinyos.yeti;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import tinyos.yeti.nature.MissingNatureException;

public class TinyOSCore {
	final public static String OLD_NATURE_ID = "TinyOS.TinyOSProject";
	final public static String NATURE_ID = "tinyos.yeti.core.TinyOSProject";
	
	final public static String MAKEOPTIONS_FILE_NAME = ".targetsOptions";
	
	public static final void inform( final String operation, final MissingNatureException ex ){
		Runnable run = new Runnable(){
			public void run(){
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if( window != null ){
					MessageBox box = new MessageBox( window.getShell(), SWT.ICON_ERROR | SWT.OK );
					box.setMessage( "Unable to perform operation '" + operation + "': project '" + ex.getProject().getName() + "' is not a TinyOS project." );
					box.setText( "Wrong nature" );
					box.open();
				}
			}
		};
		
		Display display = TinyOSPlugin.getStandardDisplay();
		if( display != null ){
			display.asyncExec( run );
		}
		else{
			TinyOSPlugin.warning( ex );
		}
	}
}
