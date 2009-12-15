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

import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.source.ISourceViewer;

import tinyos.yeti.editors.NesCEditor;

/**
 * Factory creating new formatting strategies.
 * @author Benjamin Sigg
 */
public interface INesCFormattingStrategyFactory{
	/**
	 * Creates a new formatter for <code>editor</code>.
	 * @param viewer the viewer which will use the formatter
	 * @param editor the editor which owns <code>viewer</code>
	 * @return the new formatter, may be <code>null</code>
	 */
	public IContentFormatter createFormatter( ISourceViewer viewer, NesCEditor editor );
	
	/**
	 * Whether {@link #createFormatter(ISourceViewer, NesCEditor)} will return
	 * something else than <code>null</code>
	 * @return if a formatter can be created
	 */
	public boolean isFormatter();
	
	/**
	 * Creates a new formatter which corrects indentation only for <code>editor</code>.
	 * @param viewer the viewer which will use the formatter
	 * @param editor the editor which owns <code>viewer</code>
	 * @return the new indentation correction formatter, may be <code>null</code>
	 */
	public IContentFormatter createIndenter( ISourceViewer viewer, NesCEditor editor );	
	
	/**
	 * Whether {@link #createIndenter(ISourceViewer, NesCEditor)} will return
	 * something else than <code>null</code>
	 * @return if a formatter can be created
	 */
	public boolean isIndenter();
	
	/**
	 * Gets a small human readable name for this factory.
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Gets a unique id. 
	 * @return the unique identifier
	 */
	public String getId();
}
