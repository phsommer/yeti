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
package tinyOS.debug.views.annotation;

import java.io.File;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import tinyos.yeti.editors.annotation.IAnnotationModelFactory;

// Shamelessly copied from org.eclipse.cdt.debug.internal.ui.DebugMarkerAnnotationModelFactory
public class DebugMarkerAnnotationModelFactory implements IAnnotationModelFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.ResourceMarkerAnnotationModelFactory#createAnnotationModel(org.eclipse.core.runtime.IPath)
	 */
	public IAnnotationModel createAnnotationModel( IPath location ) {
		IFile file = FileBuffers.getWorkspaceFileAtLocation( location );
		if ( file != null ) {
			return new ResourceMarkerAnnotationModel(file);
		}
		File osFile = new File( location.toOSString() );
		if ( osFile.exists() ) {
			return new DebugMarkerAnnotationModel( osFile );
		}
		return null;
	}

}
