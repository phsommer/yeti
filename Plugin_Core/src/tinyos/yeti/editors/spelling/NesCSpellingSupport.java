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
package tinyos.yeti.editors.spelling;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import tinyos.yeti.editors.INesCEditorParserClient;
import tinyos.yeti.editors.INesCPartitions;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.NesCDocumentPartitioner;
import tinyos.yeti.ep.parser.INesCParser;

public class NesCSpellingSupport implements INesCEditorParserClient{
	private NesCEditor editor;
	private NesCSpellingCollector collector;
	private CheckJob checkJob = new CheckJob();
	
	public NesCSpellingSupport( NesCEditor editor ){
		this.editor = editor;
		editor.addParserClient( this );
		collector = new NesCSpellingCollector( editor );
	}
	
	public void setupParser( NesCEditor editor, INesCParser parser ){
		// nothing	
	}
	
	private IRegion[] getCommentRegions( IDocument document ){
		NesCDocumentPartitioner partitioner = new NesCDocumentPartitioner();
		List<IRegion> results = new ArrayList<IRegion>();
		
		try{
			partitioner.connect( document );
			ITypedRegion[] regions = partitioner.computePartitioning( 0, document.getLength() );

			if( regions != null ){
				for( ITypedRegion region : regions ){
					String type = region.getType();
					if( type != null ){
						if( INesCPartitions.MULTI_LINE_COMMENT.equals( type ) ||
								INesCPartitions.NESC_SINGLE_LINE_COMMENT.equals( type ) ||
								INesCPartitions.NESC_DOC.equals( type )){

							results.add( region );
						}
					}
				}
			}
		}
		finally{
			partitioner.disconnect();
		}
		
		return results.toArray( new IRegion[ results.size() ] ); 
	}
	
	public void closeParser( NesCEditor editor, boolean successful, INesCParser parser ){
		checkJob.schedule();
	}
	
	public void recheck(){
		checkJob.schedule();
	}
	
	private class CheckJob extends UIJob{
		public CheckJob(){
			super( "spell check" );
			setSystem( true );
			setPriority( INTERACTIVE );
		}
		
		@Override
		public IStatus runInUIThread( IProgressMonitor monitor ){
			monitor.beginTask( "spell check", 1 );
			if( editor.isDisposed() ){
				monitor.done();
				return Status.OK_STATUS;
			}
			
			IRegion[] regions = getCommentRegions( editor.getDocument() );
			
			SpellingService service = EditorsUI.getSpellingService();
			SpellingContext context = new SpellingContext();
			service.check( editor.getDocument(), regions, context, collector, new SubProgressMonitor( monitor, 1 ) );
			
			monitor.done();
			return Status.OK_STATUS;
		}
	}
	
}





