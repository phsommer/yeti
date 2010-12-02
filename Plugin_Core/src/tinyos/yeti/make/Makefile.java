/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2010 ETH Zurich
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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

/**
 * Class that interprets the contents of a makefile to create a {@link MakeTargetSkeleton}.
 * @author Benjamin Sigg
 */
public class Makefile{
	private ProjectTOS project;
	private String content;
	
	public Makefile( ProjectTOS project, String content ){
		this.project = project;
		
		if( content == null ){
			this.content = "";
		}
		else{
			this.content = content;
		}
	}
	
	public MakeTargetSkeleton toSkeleton(){
		MakeTargetSkeleton skeleton = new MakeTargetSkeleton( null );
		
		String application = getApplication();
		if( application != null ){
			IContainer source = project.getLegacySourceContainer();
			IFile file = source.getFile( new Path( application + ".nc" ) );
			skeleton.setCustomComponentFile( file );
		}
		
		return skeleton;
	}
	

	/**
	 * Tries to find the name of the application specified in <code>makefile</code>.
	 * @return the name of the COMPONENT or <code>null</code>
	 */
	private String getApplication(){
		int index = content.indexOf( "COMPONENT" );
		if( index == -1 )
			return null;

		index = content.indexOf( "=", index + "COMPONENT".length() );
		if( index == -1 )
			return null;

		// the next word should be the value of COMPONENT
		index++;
		int length = content.length();
		while( index < length && Character.isWhitespace( content.charAt( index ) )){
			index++;
		}

		int begin = index;
		while( index < length && !Character.isWhitespace( content.charAt( index ) )){
			index++;
		}

		if( begin == index )
			return null;

		return content.substring( begin, index );
	}
	
//	private CharSequence get( String flag ){
//		StringBuilder result = new StringBuilder();
//		int offset = 0;
//		
//		while( true ){
//			offset = content.indexOf( flag, offset );
//			if( offset < 0 )
//				return result;
//			
//			
//		}
//	}
}
