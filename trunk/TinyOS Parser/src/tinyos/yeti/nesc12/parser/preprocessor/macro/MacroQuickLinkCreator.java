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
package tinyos.yeti.nesc12.parser.preprocessor.macro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.meta.QuickLinks;
import tinyos.yeti.preprocessor.MacroCallback;
import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * Used to create {@link QuickLinks} for macros.
 * @author besigg
 */
public class MacroQuickLinkCreator implements MacroCallback{
	private List<Entry> collections = new ArrayList<Entry>();
	private Map<Macro, Entry> current = new HashMap<Macro, Entry>();
	
	private Parser parser;
	
	public MacroQuickLinkCreator( Parser parser ){
		this.parser = parser;
	}
	
	public QuickLinks toLinks(){
		int count = 0;
		for( Entry entry : collections ){
			if( entry.prepare() ){
				count++;
			}
		}
		
		QuickLinks links = new QuickLinks( count );
		int index = 0;
		for( Entry entry : collections ){
			if( entry.isValid() ){
				links.put( index++, entry.getName(), entry.getFileRegion(), entry.getOffsets() );
			}
		}
		
		return links;
	}
	
	public void declared( Macro macro ){
		Entry entry = new Entry( macro );
		collections.add( entry );
		current.put( macro, entry );
	}
	
	public void applied( Macro macro, PreprocessorElement identifier ){
		Entry entry = current.get( macro );
		if( entry != null ){
			entry.add( identifier );
		}
	}
	
	public void undeclared( String name, Macro macro ){
		// ignore	
	}
	
	private class Entry{
		private Macro macro;
		private List<PreprocessorElement> references;
		
		private IFileRegion region;
		private int[] offsets;
		
		public Entry( Macro macro ){
			this.macro = macro;
		}
		
		public void add( PreprocessorElement reference ){
			if( references == null ){
				references = new ArrayList<PreprocessorElement>();
			}
			references.add( reference );
		}
		
		public boolean prepare(){
			if( references == null || references.size() == 0 )
				return false;
			
			region = RuleUtility.source( parser.resolveLocation( false, macro.getLocation() ) );
			if( region == null )
				return false;
			
			offsets = new int[ references.size() ];
			int index = 0;
			
			for( PreprocessorElement element : references ){
				offsets[ index++ ] = element.getToken().getBeginLocation();
			}
			
			if( index == 0 ){
				region = null;
				offsets = null;
				return false;
			}
			
			return true;
		}
		
		public boolean isValid(){
			return region != null;
		}
	
		public String getName(){
			return macro.getName();
		}
		
		public IFileRegion getFileRegion(){
			return region;
		}
		
		public int[] getOffsets(){
			return offsets;
		}
	}
}
