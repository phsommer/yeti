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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public class ProjectTab extends AbstractLaunchConfigurationTab{
	private MakeTargetLaunchTabGroup group;
	
	private IProject[] projects;
	private Combo projectCombo;
	
	private ILaunchConfigurationWorkingCopy workingCopy;
	
	private boolean disposed = false;
	private boolean onChange = false;
	
	private ILaunchConfigurationWorkingCopy defaults;
	
	public ProjectTab( MakeTargetLaunchTabGroup group ){
		this.group = group;
	}
	
	@Override
	public Image getImage(){
		return NesCIcons.icons().get( NesCIcons.ICON_NESC );
	}
	
	public void createControl( Composite parent ){
		Composite ground = new Composite( parent, SWT.NONE );
		ground.setLayout( new GridLayout( 1, false ) );
		
		Composite base = new Composite( ground, SWT.NONE );
		base.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		base.setLayout( new GridLayout( 2, false ) );
		
		Label label = new Label( base, SWT.NONE );
		label.setText( "Project" );
		label.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
		
		projectCombo = new Combo( base, SWT.DROP_DOWN | SWT.READ_ONLY );
		projectCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		projectCombo.addSelectionListener( new SelectionListener(){
			public void widgetDefaultSelected( SelectionEvent e ){
				contentChanged();
			}
			public void widgetSelected( SelectionEvent e ){
				contentChanged();
			}
		});
		
		setControl( ground );
		
		if( defaults != null ){
			initializeFrom( defaults );
		}
	}
	
	private void contentChanged(){
		try{
			if( !disposed && !onChange ){
				updateLaunchConfigurationDialog();
			}
			group.check();
			check();
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
	
	@Override
	public void dispose(){
		disposed = true;
		super.dispose();
	}
	
	private IProject[] listAvailableProjects(){
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
	 	IProject[] projects = workspace.getRoot().getProjects();
	 	
	 	List<IProject> result = new ArrayList<IProject>();
	 	for( IProject project : projects ){
	 		try{
	 			if( project.isAccessible() && project.hasNature( TinyOSCore.NATURE_ID )){
	 				result.add( project );
	 			}
	 		}
	 		catch( CoreException ex ){
	 			TinyOSPlugin.log( ex.getStatus() );
	 		}
	 	}
	 	
	 	return result.toArray( new IProject[ result.size() ] );
	}

	public String getName(){
		return "Project";
	}
	
	public void initializeFrom( ILaunchConfiguration configuration ){
		try{
			onChange = true;
			
			projects = listAvailableProjects();
			projectCombo.removeAll();
			projectCombo.add( "" );
			for( IProject project : projects ){
				projectCombo.add( project.getName() );
			}
			
			IProject project = group.getCurrentTarget().getProject();
			if( project == null ){
				projectCombo.select( 0 );
			}
			else{
				for( int i = 0; i < projects.length; i++ ){
					if( projects[i] == project ){
						projectCombo.select( i+1 );
						break;
					}
				}
			}
			
			check();
		}
		finally{
			onChange = false;
		}
	}

	private IProject getSelectedProject(){
		int selection = projectCombo.getSelectionIndex();
		if( selection > 0 ){
			return projects[ selection-1 ];
		}
		return null;
	}
	
	private void check(){
		if( getSelectedProject() == null ){
			setErrorMessage( "Need to select a project" );
		}
		else{
			setErrorMessage( null );
		}
		
		if( !onChange ){
			ILaunchConfigurationDialog dialog = getLaunchConfigurationDialog();
			if( dialog != null ){
				Control control = getControl();
				if( control != null && !control.isDisposed() ){
					dialog.updateMessage();
				}
			}
		}
	}
	
	@Override
	public void activated( ILaunchConfigurationWorkingCopy workingCopy ){
		super.activated( workingCopy );
		this.workingCopy = workingCopy;
	}
	
	@Override
	public void deactivated( ILaunchConfigurationWorkingCopy workingCopy ){
		super.deactivated( workingCopy );
		this.workingCopy = null;
	}
	
	public void performApply( ILaunchConfigurationWorkingCopy configuration ){
		writeMakeTarget();
		group.store( configuration );
	}
	
	private void writeMakeTarget(){
		IProject project = getSelectedProject();
		
		MakeTarget target = group.getCurrentTarget();
		target.setProject( project );
		
		if( project == null )
			target.setDefaults( new MakeTargetSkeleton( null ) );
		else
			target.setDefaults( target.getProjectTOS().getMakeTargets().getDefaults() );
			
		if( workingCopy != null ){
			group.store( workingCopy );
		}
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ){
		defaults = configuration;
		if( getControl() != null ){
			initializeFrom( configuration );
		}
	}
}
