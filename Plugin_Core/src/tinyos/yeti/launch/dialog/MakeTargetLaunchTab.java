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
package tinyos.yeti.launch.dialog;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.dialog.IMakeTargetDialog;
import tinyos.yeti.make.dialog.IMakeTargetDialogPage;

/**
 * Wrapper around a {@link IMakeTargetDialogPage} to show in the launch configurations.
 * @author Benjamin Sigg
 */
public class MakeTargetLaunchTab extends AbstractLaunchConfigurationTab{
	private IMakeTargetDialogPage<? super MakeTarget> page;
	private MakeTargetLaunchTabGroup group;
	
	private boolean disposed = false;
	
	private ILaunchConfigurationWorkingCopy defaults;
	
	private boolean onChange = false;
	
	public MakeTargetLaunchTab( IMakeTargetDialogPage<? super MakeTarget> page, MakeTargetLaunchTabGroup group ){
		this.page = page;
		this.group = group;
		
		page.setDialog( new IMakeTargetDialog(){
			public void setMessage( IMakeTargetDialogPage<?> page, String message, Severity severity ){
				if( severity == Severity.ERROR || severity == Severity.WARNING ){
					MakeTargetLaunchTab.this.setMessage( null );
					setErrorMessage( message );
				}
				else{
					setErrorMessage( null );
					MakeTargetLaunchTab.this.setMessage( message );
				}
			}
			public void contentChanged(){
				try{
					if( !disposed && !onChange ){
						updateLaunchConfigurationDialog();
					}

					MakeTargetLaunchTab.this.group.check();

					if( !disposed && !onChange ){
						updateLaunchConfigurationDialog();
					}
				}
				catch( SWTException ex ){
					// since it seems impossible to find out whether the dialog still 
					// exists, let's just use the most ugly hack possible.
					if( ex.code != SWT.ERROR_WIDGET_DISPOSED )
						throw ex;
				}
			}
		});
	}
	
	@Override
	public Image getImage(){
		return page.getImage();
	}
	
	@Override
	public void dispose(){
		disposed = true;
		super.dispose();
	}
	
	public void check( MakeTarget target ){
		page.check( target, group.getInformation() );
	}

	public void createControl( Composite parent ){
		page.createControl( parent );
		setControl( page.getControl() );
		
		if( defaults != null ){
			initializeFrom( defaults );
		}
	}

	public String getName(){
		return page.getName();
	}
	
	public void initializeFrom( ILaunchConfiguration configuration ){
		try{
			onChange = true;
			page.show( group.getCurrentTarget(), group.getInformation() );
		}
		finally{
			onChange = false;
		}
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration ){
		page.store( group.getCurrentTarget() );
		group.store( configuration );
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ){
		defaults = configuration;
		if( getControl() != null ){
			initializeFrom( configuration );
		}
	}
}
