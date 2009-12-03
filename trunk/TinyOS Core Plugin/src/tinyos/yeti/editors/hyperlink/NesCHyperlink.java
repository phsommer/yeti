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
package tinyos.yeti.editors.hyperlink;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.parser.IFileHyperlink;
import tinyos.yeti.ep.parser.IFileRegion;

public class NesCHyperlink implements IHyperlink{
    private IFileHyperlink file;
    
    public NesCHyperlink( IFileHyperlink link ){
        this.file = link;
    }

    public IRegion getHyperlinkRegion(){
        IFileRegion source = file.getSourceRegion();
        if( source == null )
            return null;
        
        return new Region( source.getOffset(), source.getLength() );
    }

    public String getHyperlinkText(){
        return file.getHyperlinkName();
    }

    public String getTypeLabel(){
        return file.getHyperlinkType();
    }

    public void open(){
        try{
        	ITextEditor editor = TinyOSPlugin.getDefault().openFileInTextEditor( file.getParseFile().getProject(), file.getParseFile().getPath() );
            IFileRegion target = file.getTargetRegion();
            if( editor != null && target != null ){
                editor.selectAndReveal( target.getOffset(), target.getLength() );
            }
        }
        catch ( CoreException e ){
            TinyOSPlugin.getDefault().getLog().log( e.getStatus() );
        }
    }
}
