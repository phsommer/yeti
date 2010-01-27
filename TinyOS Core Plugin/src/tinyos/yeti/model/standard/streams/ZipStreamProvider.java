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
package tinyos.yeti.model.standard.streams;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import tinyos.yeti.model.ProjectModel;

/**
 * Like the {@link LinkedStreamProvider}, but applies 'zip' to compact the files.
 * @author Benjamin Sigg
 */
public class ZipStreamProvider extends LinkedStreamProvider{
	public ZipStreamProvider( ProjectModel model, IPathConverter pathConverter ){
		super( model, new ZipStreamConverter(), pathConverter );
	}

	@Override
	protected IPath derivedFilePath( File file, String extension ){
		return new Path( file.getAbsolutePath() + ".zip" );
	}
}
