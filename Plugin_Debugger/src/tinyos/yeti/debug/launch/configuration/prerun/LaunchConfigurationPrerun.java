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
package tinyos.yeti.debug.launch.configuration.prerun;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.debug.TinyOSDebugPlugin;
import tinyos.yeti.debug.launch.configuration.ILaunchPrerun;
import tinyos.yeti.debug.launch.configuration.ILaunchPrerunTab;
import tinyos.yeti.debug.launch.configuration.ILaunchPrerunTabHandle;
import tinyos.yeti.debug.launch.configuration.ITinyOSDebugLaunchConstants;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.launch.LaunchConverter;
import tinyos.yeti.make.IMakeTarget;

public class LaunchConfigurationPrerun implements ILaunchPrerun, ILaunchPrerunTab{
	private IMakeTarget[] targets;
	private Combo combo;
	
	private ILaunchPrerunTabHandle handle;
	
	public static final String TARGET = ITinyOSDebugLaunchConstants.ATTR_CURRENT_LAUNCH_PRERUN + ".launchConfig.name";
	
	@Override
	public IMakeTarget getMakeTarget( ILaunchConfiguration configuration, IProject project ) throws CoreException{
		String id = configuration.getAttribute( TARGET, "" );
		if( id.equals( "" ))
			return null;
		
		IMakeTarget[] targets = getConfigurations( project );
		for( IMakeTarget target : targets ){
			if( target.getId().equals( id )){
				return target;
			}
		}
		
		throw new CoreException( new Status( IStatus.ERROR, TinyOSDebugPlugin.PLUGIN_ID, "Cannot find launch configuration with id '" + id + "'" ));
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
			return "Executing a launch configuration before starting the debug session, but no launch configuration is selected";
		return null;
	}
	
	@Override
	public String getName(){
		return "execute a launch configuration";
	}

	@Override
	public void read( ILaunchConfiguration configuration ){
		try{
			combo.deselectAll();
			String id = configuration.getAttribute( TARGET, "" );
			if( !id.equals( "" )){
				for( int i = 0; i < targets.length; i++ ){
					if( targets[i].getId().equals( id )){
						combo.select( i );
						break;
					}
				}
			}
		}
		catch( CoreException ex ){
			TinyOSDebugPlugin.getDefault().log( ex.getMessage(), ex );
		}
	}
	
	@Override
	public void apply( ILaunchConfigurationWorkingCopy configuration ){
		int index = combo.getSelectionIndex();
		if( index < 0 )
			configuration.setAttribute( TARGET, "" );
		else
			configuration.setAttribute( TARGET, targets[index].getId() );
	}
	
	@Override
	public void setHandle( ILaunchPrerunTabHandle handle ){
		this.handle = handle;
	}

	@Override
	public void setProject( IProject project ){
		try{
			combo.removeAll();
			if( project == null || !project.isAccessible() || !project.hasNature( TinyOSCore.NATURE_ID )){
				combo.setEnabled( false );
			}
			else{
				combo.setEnabled( true );
				targets = getConfigurations( project );
				
				for( IMakeTarget target : targets ){
					combo.add( target.getName() );
				}
			}
		}
		catch( CoreException ex ){
			combo.setEnabled( false );
			TinyOSDebugPlugin.getDefault().log( ex.getMessage(), ex );
		}
	}
	
	private IMakeTarget[] getConfigurations( IProject project ){
		try{
			if( project == null )
				return new IMakeTarget[]{};
			
			if( !project.isAccessible() )
				return new IMakeTarget[]{};
			
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType( "tinyos.launch.build" );
			if( type == null )
				return new IMakeTarget[]{};
			ILaunchConfiguration[] tinyosConfigs = manager.getLaunchConfigurations( type );
			List<IMakeTarget> projectTargets = new ArrayList<IMakeTarget>();
			for( ILaunchConfiguration configuration : tinyosConfigs ){
				if( project.equals( LaunchConverter.getProject( configuration ))){
					IMakeTarget target = LaunchConverter.read( configuration );
					projectTargets.add( target );
				}
			}
			
			IMakeTarget[] results = projectTargets.toArray( new IMakeTarget[ projectTargets.size() ] );
			Arrays.sort( results, new Comparator<IMakeTarget>(){
				private Collator collator = Collator.getInstance();
				
				@Override
				public int compare( IMakeTarget o1, IMakeTarget o2 ){
					return collator.compare( o1.getName(), o2.getName() );
				}
			});
			
			return results;
		}
		catch( CoreException ex ){
			TinyOSDebugPlugin.getDefault().log( ex.getMessage(), ex );
			return new IMakeTarget[]{};
		}
	}
}
