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
package tinyos.yeti.nesc12.ep.rules.hyperlink;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;

import tinyos.yeti.ep.parser.IDocumentRegion;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos_parser.NesC12ParserPlugin;

public class MacroHyperlink implements IHyperlinkRule{
	public void search( NesC12AST ast, HyperlinkCollector collector ){
		IDocumentRegion location = collector.getLocation();
		try{
			int offset = collector.getOffset().getInputfileOffset();
			String word = RuleUtility.wordAt( offset, location.getDocument() );
			
			if( word != null ){
				IFileRegion target = ast.getMacroLinks().getTarget( word, offset );
				if( target != null ){
					IFileRegion source = RuleUtility.regionOfWordAt( ast.getParseFile(), offset, location.getDocument() );
					
					collector.add( new FileHyperlink( source, target ) );
				}
			}
		}
		catch( BadLocationException e ){
			NesC12ParserPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, NesC12ParserPlugin.PLUGIN_ID, e.getMessage(), e ) );
		}
	}

}
