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
package tinyOS.debug.launch.configuration.prerun;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.launch.configuration.ILaunchPrerun;
import tinyOS.debug.launch.configuration.ILaunchPrerunTab;
import tinyOS.debug.launch.configuration.ILaunchPrerunTabHandle;
import tinyOS.debug.launch.configuration.ITinyOSDebugLaunchConstants;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.IProjectMakeTargets;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.targets.IMakeTargetMorpheable;
import tinyos.yeti.nature.MissingNatureException;

public class MakeOptionPrerun implements ILaunchPrerun, ILaunchPrerunTab{
	public static final String TARGET = ITinyOSDebugLaunchConstants.ID_LAUNCH_TINYOS_DEBUG + ".prerun.make-option";
	
	private Combo combo;
	private IMakeTarget[] targets;
	
	private ILaunchPrerunTabHandle handle;
	
	@Override
	public IMakeTarget getMakeTarget( ILaunchConfiguration configuration, IProject project ) throws CoreException{
		if( !project.isAccessible() )
			throw new CoreException( new Status( IStatus.ERROR, TinyOSDebugPlugin.PLUGIN_ID, "Project not accessible '" + project.getName() + "'" ));
		
		if( !project.hasNature( TinyOSCore.NATURE_ID ))
			throw new CoreException( new Status( IStatus.ERROR, TinyOSDebugPlugin.PLUGIN_ID, "Project has no TinyOS nature '" + project.getName() + "'" ));
		
		String id = configuration.getAttribute( TARGET, "" );
		
		if( id.equals( "" ))
			return null;
		
		try{
			ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
			IMakeTargetMorpheable[] selectable = tos.getMakeTargets().getSelectableTargets();
			for( IMakeTargetMorpheable morph : selectable ){
				IMakeTarget target = morph.toMakeTarget();
				if( target.getId().equals( id ))
					return target;
			}
		}
		catch( MissingNatureException ex ){
			// ignore
		}
		
		throw new CoreException( new Status( IStatus.ERROR, TinyOSDebugPlugin.PLUGIN_ID, "could not find make-option with id '" + id + "'" ) );
	}

	@Override
	public void apply( ILaunchConfigurationWorkingCopy configuration ){
		int index = combo.getSelectionIndex();
		if( index < 0 )
			configuration.setAttribute( TARGET, "" );
		else{
			configuration.setAttribute( TARGET, targets[ index ].getId() );
		}
	}
	
	@Override
	public void read( ILaunchConfiguration configuration ){
		try{
			String id = configuration.getAttribute( TARGET, "" );
			int index = indexOf( id );
			combo.select( index );
		}
		catch( CoreException e ){
			TinyOSDebugPlugin.getDefault().log( e.getMessage(), e );
		}
	}
	
	private int indexOf( String id ){
		if( targets == null )
			return -1;
		
		for( int i = 0; i < targets.length; i++ ){
			if( targets[i].getId().equals( id )){
				return i;
			}
		}
		return -1;
	}

	@Override
	public Control getControl( Composite parent ){
		Composite content = new Composite( parent, SWT.NONE );
		content.setLayout( new GridLayout( 1, false ) );
		
		combo = new Combo( content, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY );
		combo.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		
		combo.addSelectionListener( new SelectionListener(){
			@Override
			public void widgetDefaultSelected( SelectionEvent e ){
				if( !handle.isInitializing() ){
					handle.setDirty();
				}
			}
			@Override
			public void widgetSelected( SelectionEvent e ){
				if( !handle.isInitializing() ){
					handle.setDirty();
				}
			}
		});
		
		return content;
	}

	@Override
	public String getErrorMessage(){
		int index = combo.getSelectionIndex();
		if( index < 0 )
			return "Executing a make-option before starting the debug session, but no make-option is selected";
		return null;
	}
	
	@Override
	public String getName(){
		return "execute a make-option";
	}


	@Override
	public void setHandle( ILaunchPrerunTabHandle handle ){
		this.handle = handle;
	}
	
	@Override
	public void setProject( IProject project ){
		try{
			if( project == null || !project.isAccessible() || !project.hasNature( TinyOSCore.NATURE_ID )){
				combo.setEnabled( false );
				combo.removeAll();
			}
			else{
				combo.setEnabled( true );
				
			    ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
			    IProjectMakeTargets targets = tos.getMakeTargets();
			    IMakeTargetMorpheable[] selectable = targets.getSelectableTargets();
			    this.targets = new IMakeTarget[ selectable.length ];
			    
			    combo.removeAll();
			    
			    for( int i = 0; i < selectable.length; i++ ){
			    	MakeTarget target = selectable[i].toMakeTarget();
			    	this.targets[i] = target;
			    	combo.add( target.getName() );
			    }
			}
		}
		catch( MissingNatureException ex ){
			// never happens
			combo.setEnabled( false );
			TinyOSDebugPlugin.getDefault().log( ex.getMessage(), ex );
		}
		catch( CoreException ex ){
			combo.setEnabled( false );
			TinyOSDebugPlugin.getDefault().log( ex.getMessage(), ex );
		}
	}
}
