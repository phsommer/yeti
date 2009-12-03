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
package tinyOS.debug.ep;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;

import tinyOS.debug.TinyOSDebugPlugin;
import tinyos.yeti.ep.IEditorInputConverter;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.ProjectModel;

public class ExternalEditorInputConverter implements IEditorInputConverter{
	@SuppressWarnings("restriction")
	@Override
	public boolean matches( IEditorInput input ){
		return input instanceof ExternalEditorInput;
	}	
	
	@SuppressWarnings("restriction")
	@Override
	public IProject getProject( IEditorInput input ){
		IResource resource = ((ExternalEditorInput)input).getMarkerResource();
		if( resource != null )
			return resource.getProject();
		
		return null;
	}
	
	@SuppressWarnings("restriction")
	@Override
	public IParseFile getFile( IEditorInput input, ProjectModel model ){
		
		for( int i = 0; i < 2; i++ ){
			try{
				switch( i ){
					case 0:
						return tryReadStorage( (ExternalEditorInput)input, model );
					case 1:
						return tryReadPath( (ExternalEditorInput)input, model );
				}
			}
			catch( IllegalStateException ex ){
				// expected, ignore
			}
			catch( Exception e ){
				// not expected, log
				TinyOSDebugPlugin.getDefault().log( "Cannot validate external file", e );
			}
			catch( Throwable t ){
				// not expected, log
				TinyOSDebugPlugin.getDefault().log( "Cannot validate external file: " + t.getMessage() );
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("restriction")
	private IParseFile tryReadStorage( ExternalEditorInput input, ProjectModel model ){
		try{
			Class<? extends ExternalEditorInput> clazz = (Class<? extends ExternalEditorInput>)input.getClass();
			
			Method getStorage = clazz.getMethod( "getStorage", new Class[]{} );
			IStorage storage = (IStorage)getStorage.invoke( input );
			
			if( storage == null )
				return null;
			
			IPath path = storage.getFullPath();
			if( path == null )
				return null;
			
			return model.parseFile( path.toFile() );
		}
		catch( NoSuchMethodException ex ){
			throw new IllegalStateException();
		}
		catch( IllegalArgumentException e ){
			throw new IllegalStateException();
		}
		catch( IllegalAccessException e ){
			throw new IllegalStateException();
		}
		catch( InvocationTargetException e ){
			throw new IllegalStateException();
		}
	}
	
	@SuppressWarnings("restriction")
	private IParseFile tryReadPath( ExternalEditorInput input, ProjectModel model ){
		try{
			Class<? extends ExternalEditorInput> clazz = (Class<? extends ExternalEditorInput>)input.getClass();
			
			Method getPath = clazz.getMethod( "getPath", new Class[]{} );
			IPath path = (IPath)getPath.invoke( input );
			
			if( path == null )
				return null;
			
			return model.parseFile( path.toFile() );
		}
		catch( NoSuchMethodException ex ){
			throw new IllegalStateException();
		}
		catch( IllegalArgumentException e ){
			throw new IllegalStateException();
		}
		catch( IllegalAccessException e ){
			throw new IllegalStateException();
		}
		catch( InvocationTargetException e ){
			throw new IllegalStateException();
		}
	}

	@Override
	public IResource getResource( IEditorInput input ){
		return null;
	}
}
