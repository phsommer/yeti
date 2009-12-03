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
package tinyos.yeti.search.model.group.declaration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.search.model.group.Group;

/**
 * Groups nodes by their {@link IParseFile}.
 * @author Benjamin Sigg
 */
public class FileGroupGenerator extends NodeGroupGenerator<IParseFile, IASTModelNode>{
	@Override
	protected IParseFile[] getKeys( IASTModelNode node ){
		IParseFile file = null;
		IASTModelPath path = node.getLogicalPath();
		if( path != null ){
			file = path.getParseFile();
		}
		if( file == null ){
			file = node.getParseFile();
		}
		return new IParseFile[]{ file };
	}
	
	@Override
	protected Group createGroupFor( IParseFile groupKey ){
		return createFileGroup( groupKey, true );
	}
	
	public static Group createFileGroup( IParseFile groupKey, boolean projectName ){
		String name = null;
		
		if( groupKey.isProjectFile() ){
			name = getProjectPath( groupKey, projectName );
		}
		
		if( name == null ){
			name = groupKey.getPath();
		}
		
		return new Group( name, NesCIcons.icons().get( NesCIcons.ICON_NESC_FILE, true ), groupKey );
	}
	
	public static String getProjectPath( IParseFile parseFile, boolean projectName ){
		ProjectTOS project = parseFile.getProject();
		if( project == null )
			return null;
		
		File file = parseFile.toFile();
		if( file == null )
			return null;
		
		IPath projectPath = project.getProject().getLocation();
		if( projectPath == null )
			return null;
		
		IPath result = null;
		boolean build = false;
		
		IContainer source = parseFile.getProjectSourceContainer();
		if( source != null ){
			result = diff( projectPath.append( source.getProjectRelativePath() ), file );
		}
		
		if( result == null ){
			IPath buildPath = projectPath.append( project.getBuildContainer().getProjectRelativePath() );
			result = diff( buildPath, file );
			build = true;
		}
		
		if( result == null )
			return null;
		
		if( projectName ){
			if( build )
				return project.getProject().getName() + "-build/" + result.toString();
			else
				return project.getProject().getName() + "/" + result.toString();
		}
		else{
			if( build )
				return "[build]/" + result.toString();
			else
				return result.toString();			
		}
	}
	
	private static IPath diff( IPath path, File file ){
		File directory = path.toFile();
		if( directory == null || !directory.exists() )
			return null;
		
		File check = file;
		List<String> names = new ArrayList<String>();
		
		while( check != null ){
			if( check.equals( directory )){
				StringBuilder result = new StringBuilder();
				for( int i = names.size()-1; i >= 0; i-- ){
					result.append( names.get( i ) );
					if( i > 0 ){
						result.append( File.separator );
					}
				}
				
				return new Path( result.toString() );
			}
			names.add( check.getName() );
			check = check.getParentFile();
		}
		
		return null;
	}
}
