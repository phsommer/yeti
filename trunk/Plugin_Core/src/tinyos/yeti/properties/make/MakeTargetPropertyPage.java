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
package tinyos.yeti.properties.make;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.IProjectMakeTargets;
import tinyos.yeti.make.ISharedMakeTargetListener;
import tinyos.yeti.make.SharedMakeTarget;
import tinyos.yeti.make.dialog.IMakeTargetDialog;
import tinyos.yeti.make.dialog.IMakeTargetDialogPage;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

/**
 * A wrapper around a {@link IMakeTargetDialogPage} to use the page as
 * {@link IWorkbenchPropertyPage}.
 * @author Benjamin Sigg
 *
 */
public class MakeTargetPropertyPage extends PropertyPage implements IWorkbenchPropertyPage{
	private IMakeTargetDialogPage<MakeTargetSkeleton> page;
	private SharedMakeTarget<MakeTargetSkeleton> target;
	
	public MakeTargetPropertyPage( IMakeTargetDialogPage<MakeTargetSkeleton> page ){
		this.page = page;
		setDescription( page.getDescription() );
		noDefaultAndApplyButton();
	}
	
	@Override
	protected Control createContents( Composite parent ){
		IProject project = (IProject)getElement();
		IProjectMakeTargets targets = TinyOSPlugin.getDefault().getTargetManager().getProjectTargets( project );
		target = targets.openDefaults( true );
		
		final ISharedMakeTargetListener<MakeTargetSkeleton> listener =
			new ISharedMakeTargetListener<MakeTargetSkeleton>(){
			public void targetUpdated( SharedMakeTarget<MakeTargetSkeleton> source ){
				page.show( target.getMakeTarget(), target );
				page.check( target.getMakeTarget(), target );
			}
		};
		
		target.addListener( listener );
		
		setTitle( page.getName() );
		
		page.createControl( parent );
		page.show( target.getMakeTarget(), target );
		
		page.getControl().addDisposeListener( new DisposeListener(){
			public void widgetDisposed( DisposeEvent e ){
				target.removeListener( listener );
			}
		});
		
		page.setDialog( new IMakeTargetDialog(){
			public void setMessage( IMakeTargetDialogPage<?> page, String message, Severity severity ){
				if( message == null ){
					MakeTargetPropertyPage.this.setMessage( null, NONE );
				}
				else{
					int type = 0;
					
					switch( severity ){
						case DESCRIPTION:
							type = NONE;
							message = page.getName();
							break;
						case ERROR:
							type = ERROR;
							break;
						case INFO:
							type = INFORMATION;
							break;
						case WARNING:
							type = WARNING;
							break;
					}
					
					MakeTargetPropertyPage.this.setMessage( message, type );
				}
			}
			
			public void contentChanged(){
				// ignore	
			}
		});
		
		
		return page.getControl();
	}
	
	@Override
	public boolean okToLeave(){
		if( target != null ){
			page.store( target.getMakeTarget() );
			target.fireTargetChanged();
		}
		return super.okToLeave();
	}
	
	@Override
	public void dispose(){
		page.setDialog( null );
		page.dispose();
		super.dispose();
	}
	
	@Override
	public boolean performCancel(){
		if( target != null ){
			target.cancel();
		}
		return true;
	}
	
	@Override
	protected void performApply(){
		if( target != null ){
			page.store( target.getMakeTarget() );
			target.applyCounted();
		}
	}
	
	@Override
	public boolean performOk(){
		if( target != null ){
			page.store( target.getMakeTarget() );
			target.close();
		}
		return true;
	}
}
