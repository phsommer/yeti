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
package tinyos.yeti.editors.format;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.source.ISourceViewer;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.NesCSourceViewerConfiguration;

/**
 * Factory creating a {@link NesCFormattingStrategy2}.
 * @author Benjamin Sigg
 *
 */
public class NesCFormattingStrategy2Factory implements INesCFormattingStrategyFactory{
	
	public IContentFormatter createFormatter( ISourceViewer viewer, NesCEditor editor ){
		return null;
	}
	
	public boolean isFormatter(){
		return false;
	}
	
	public IContentFormatter createIndenter( ISourceViewer viewer, NesCEditor editor ){
		NesCSourceViewerConfiguration configuration = editor.getConfiguration();
		
        MultiPassContentFormatter formatter = new MultiPassContentFormatter(
        		configuration.getConfiguredDocumentPartitioning( viewer ), IDocument.DEFAULT_CONTENT_TYPE );

        formatter.setMasterStrategy( new NesCFormattingStrategy2() );
		
        return formatter;
	}
	
	public boolean isIndenter(){
		return true;
	}

	public String getName(){
		return "Yeti 2 - simple default"; 
	}
	
	public String getId(){
		return "tinyos.yeti.default";
	}
}
