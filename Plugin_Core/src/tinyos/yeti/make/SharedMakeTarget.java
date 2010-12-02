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
package tinyos.yeti.make;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.targets.MakeTargetSkeleton;
import tinyos.yeti.nature.MissingNatureException;

/**
 * Information needed to edit a {@link MakeTarget} or a {@link MakeTargetSkeleton}.<br>
 * Note that this class implements {@link IMakeTargetInformation}.<br>
 * This class supports a listener mechanism with {@link ISharedMakeTargetListener},
 * it is however the clients responsibility to fire the events when appropriate.
 * @author Benjamin Sigg
 *
 */
public abstract class SharedMakeTarget<M extends MakeTargetSkeleton> implements IMakeTargetInformation{
	private M original;
	private M copy;
	
	private boolean loaded = false;
	
	private IProject project;
	private ProjectTOS tos;
	
	private IEnvironment environment;
	private IPlatform[] platforms;
	
	private int access = 0;
	private int applyCount = 0;
	
	private List<ISharedMakeTargetListener<M>> listeners = 
		new ArrayList<ISharedMakeTargetListener<M>>();
	
	public SharedMakeTarget( IProject project, M original ){
		this.project = project;
		this.original = original;
	}
	
	public void addListener( ISharedMakeTargetListener<M> listener ){
		listeners.add( listener );
	}
	
	public void removeListener( ISharedMakeTargetListener<M> listener ){
		listeners.remove( listener );
	}
	
	public void fireTargetChanged(){
		for( ISharedMakeTargetListener<M> listener : listeners ){
			listener.targetUpdated( this );
		}
	}
	
	/**
	 * Reads the contents of <code>original</code> and returns a new copy of
	 * it.
	 * @param original the original values, may be <code>null</code>
	 * @return the copy, must not be <code>null</code>
	 */
	protected abstract M read( M original );
	
	/**
	 * Writes the contents of <code>copy</code> into <code>original</code>.
	 * @param copy the copy with the new values
	 * @param original the original settings, may be <code>null</code>
	 * @returns either <code>original</code> or if <code>original</code> was
	 * <code>null</code> then a new <code>M</code> into which this shared
	 * make-target will write from now on.
	 */
	protected abstract M write( M copy, M original );
	
	public IProject getProject(){
		return project;
	}
	
	public M getMakeTarget(){
		return copy;
	}
	
	public void open(){
		if( access == 0 )
			copy = read( original );
		
		access++;
	}
	
	public void close(){
		if( access > 0 ){
			access--;
		
			if( access == 0 )
				original = write( copy, original );
		}
	}
	
	/**
	 * Forces this target to write right now.
	 */
	public void apply(){
		original = write( copy, original );
	}
	
	public void applyCounted(){
		applyCount++;
		if( applyCount == access ){
			apply();
			applyCount = 0;
		}
	}
	
	public void cancel(){
		access = 0;
	}
	
	private void load(){
		if( !loaded ){
			loaded = true;
			
			if( project == null )
				return;
			
			try{
				tos = TinyOSPlugin.getDefault().getProjectTOS( project );
				if( tos == null )
					return;
			}
			catch( MissingNatureException ex ){
				// silent
				return;
			}
			
			environment = tos.getEnvironment();
			if( environment == null )
				return;
			
			platforms = environment.getPlatforms();
		}
	}
	
	public IEnvironment getEnvironment(){
		load();
		return environment;
	}
	public IPlatform[] getPlatforms(){
		load();
		return platforms;
	}
	public IPlatform getSelectedPlatform(){
		load();
		if( platforms == null )
			return null;
		
		String target = copy.getTarget();
		if( target == null )
			return null;
		
		for( IPlatform platform : platforms ){
			if( target.equals( platform.getName() ))
				return platform;
		}
		
		return null;
	}
}
