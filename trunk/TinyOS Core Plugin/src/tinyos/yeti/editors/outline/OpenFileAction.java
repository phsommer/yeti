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
package tinyos.yeti.editors.outline;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.views.NodeContentProvider;

public class OpenFileAction extends Action implements ISelectionChangedListener {
	private IParseFile file;
	private IFileRegion region;
    
    public OpenFileAction() {
        super( "open file" );
    }
    
    @Override
    public void run() {
    	if( file != null ){
    		try{
    			ITextEditor editor = TinyOSPlugin.getDefault().openFileInTextEditor( file );
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

    protected IParseFile getFile( Object selection ){
    	IFileRegion region = getRegion( selection );
    	if( region != null )
    		return region.getParseFile();
    	return null;
    }
    
    protected IFileRegion getRegion( Object selection ){
        if( selection instanceof NodeContentProvider.Element ){
            return ((NodeContentProvider.Element)selection).getNodeRegion();
        }	
        return null;
    }
    
    public void selectionChanged(SelectionChangedEvent event) {
        ISelection sel = event.getSelection();
        Object obj = ((IStructuredSelection) sel).getFirstElement();

        region = getRegion( obj );
        if( region == null )
        	file = getFile( obj );
        else
        	file = region.getParseFile();
        
        setEnabled( region != null || file != null );
    }
}