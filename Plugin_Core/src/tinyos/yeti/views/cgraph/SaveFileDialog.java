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
package tinyos.yeti.views.cgraph;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class SaveFileDialog {

	private final String extensions[] = new String[]{ "gif", "jpg", "png" };
	
	private final int swtConst[] = new int[]{ SWT.IMAGE_GIF, SWT.IMAGE_JPEG, SWT.IMAGE_PNG };

	private FileDialog f;
	private String fileName;

	public SaveFileDialog(Shell parent) {
		f = new FileDialog(parent, SWT.SAVE);
		String[] filters = new String[ extensions.length ];
		for( int i = 0; i < filters.length; i++ ){
			filters[i] = "*." + extensions[i];
		}
		f.setFilterExtensions( filters );
	}

	public String open() {
		fileName = f.open();
		String extension = getSelectedExtension();
		if( extension != null ){
			String end = "." + extension;
			if( !fileName.endsWith( end )){
				fileName += end; 
			}
		}
		
		return fileName;
	}
	
	public String getSelectedExtension(){
		int index = f.getFilterIndex();
		if( index >= 0 ){
			return extensions[ index ];
		}
		return null;
	}

	public int getSwtImageFileFormat() {
		String ext = getSelectedExtension();
		if( ext == null )
			return 0;
		
		for( int i = 0; i < extensions.length; i++ ){
			if( extensions[i].equals( ext )){
				return swtConst[i];
			}
		}
		
		return SWT.IMAGE_JPEG;
	}
}
