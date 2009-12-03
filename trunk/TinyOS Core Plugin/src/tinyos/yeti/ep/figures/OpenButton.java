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
package tinyos.yeti.ep.figures;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileRegion;

public class OpenButton extends Clickable{
	private IFileRegion region;
	
	public OpenButton( IFileRegion region ){
		super( new ImageFigure( NesCIcons.icons().get( NesCIcons.ICON_OPEN ) ), Clickable.STYLE_BUTTON );
		this.region = region;
    	setBorder( null );
    	
    	addActionListener( new ActionListener(){
    		public void actionPerformed( ActionEvent event ){
    			open();
    		}
    	});
	}

    public void open(){
    	IParseFile parseFile = region.getParseFile();
    	
    	if( parseFile != null ){
			try{
				ITextEditor editor = TinyOSPlugin.getDefault().openFileInTextEditor( parseFile );
				if( editor != null && region != null ){
					editor.selectAndReveal( region.getOffset(), region.getLength() );
				}
			}
			catch (CoreException e) {
				TinyOSPlugin.log( e.getStatus() );
			} 
			catch (NullPointerException e) {
				TinyOSPlugin.log( e );
			}
		}
    }    
}
