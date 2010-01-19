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
package tinyos.yeti.model.standard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import tinyos.yeti.model.ProjectModel;

/**
 * Like the {@link LinkedStreamProvider}, but applies 'zip' to compact the files.
 * @author Benjamin Sigg
 */
public class ZipStreamProvider extends LinkedStreamProvider{
	public ZipStreamProvider( ProjectModel model ){
		super( model );
	}

	@Override
	protected InputStream modify( InputStream in ) throws IOException{
		ZipInputStream zip = new ZipInputStream( in );
		zip.getNextEntry();
		return zip;
	}
	
	@Override
	protected OutputStream modify( OutputStream out ) throws IOException{
		ZipOutputStream zip = new ZipOutputStream( out );
		zip.putNextEntry( new ZipEntry( "entry" ) );
		return zip;
	}
}
