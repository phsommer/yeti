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
package tinyos.yeti.properties.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.model.INesCPathListener;
import tinyos.yeti.model.NesCPath;
import tinyos.yeti.utility.TinyOSProjects;
import tinyos.yeti.utility.TinyOSProjectsListener;

public class SourceFolderDecorator implements ILightweightLabelDecorator, INesCPathListener{
	private List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();
	private ImageDescriptor image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_NESC_DECORATION );
	
	private TinyOSProjects projects;
	
	private Map<NesCPath, IFolder[]> paths = new HashMap<NesCPath, IFolder[]>();
	
	public SourceFolderDecorator(){
		projects = TinyOSPlugin.getDefault().getProjects();
		projects.addListener( new TinyOSProjectsListener(){
			public void projectAdded( IProject project ){
				link( project );
			}
			public void projectRemoved( IProject project ){
				unlink( project );
			}
		});
		for( IProject project : projects.getProjects() ){
			link( project );
		}
	}
	
	private void link( IProject project ){
		synchronized( paths ){
			NesCPath path = TinyOSPlugin.getDefault().getPaths( project );
			paths.put( path, path.getSourceFolders() );
			path.addListener( SourceFolderDecorator.this );
		}
	}
	
	private void unlink( IProject project ){
		synchronized( paths ){
			NesCPath path = TinyOSPlugin.getDefault().getPaths( project );
			paths.remove( path );
			path.removeListener( SourceFolderDecorator.this );
		}
	}
	
	public void sourceFoldersChanged( final NesCPath path ){
		UIJob job = new UIJob( "Update Decoration" ){
			@Override
			public IStatus runInUIThread( IProgressMonitor monitor ){
				monitor.beginTask( "Update Decorations", IProgressMonitor.UNKNOWN );
				synchronized( paths ){
					Set<IFolder> changed = new HashSet<IFolder>();
					IFolder[] folders = paths.get( path );
					for( IFolder folder : folders ){
						changed.add( folder );
					}
				
					folders = path.getSourceFolders();
					paths.put( path, folders );
					for( IFolder folder : folders ){
						changed.add( folder );
					}
					
					changed( changed.toArray( new IFolder[ changed.size() ] ));
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule( path.getProject() );
		job.setPriority( Job.DECORATE );
		job.setSystem( true );
		job.schedule();
	}
	
	public void decorate( Object element, IDecoration decoration ){
		if( element instanceof IAdaptable ){
			IFolder folder = (IFolder)((IAdaptable)element).getAdapter( IFolder.class );
			if( folder != null ){
				IProject project = folder.getProject();
				try{
					if( project.hasNature( TinyOSCore.NATURE_ID )){
						TinyOSPlugin plugin = TinyOSPlugin.getDefault();
						if( plugin != null ){
							NesCPath path = plugin.getPaths( project );
							IFolder[] folders = path.getSourceFolders();
							for( IFolder check : folders ){
								if( check.equals( folder )){
									decoration.addOverlay( image, IDecoration.TOP_RIGHT );					
									return;
								}
							}
						}
					}
				}
				catch( CoreException ex ){
					// swallow, it is not *that* important that the icons are correct...
				}
			}
		}
	}

	public void addListener( ILabelProviderListener listener ){
		if( listener != null && !listeners.contains( listener )){
			listeners.add( listener );
		}
	}

	public void removeListener( ILabelProviderListener listener ){
		listeners.remove( listener );
	}
	
	private void changed( IFolder[] folders ){
		LabelProviderChangedEvent event = new LabelProviderChangedEvent( this, folders );
		
		for( ILabelProviderListener listener : listeners.toArray( new ILabelProviderListener[ listeners.size() ] )){
			listener.labelProviderChanged( event );
		}
	}

	public void dispose(){
		projects.dispose();
	}

	public boolean isLabelProperty( Object element, String property ){
		return false;
	}
}
