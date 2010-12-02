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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

import tinyos.yeti.editors.NesCEditor;

public class NesCSpellingCollector implements ISpellingProblemCollector{
	private NesCEditor editor;
	
	private List<SpellingAnnotation> annotations = new ArrayList<SpellingAnnotation>();
	private SpellingJob job = new SpellingJob();
	
	public NesCSpellingCollector( NesCEditor editor ){
		this.editor = editor;	
	}

	public void accept( SpellingProblem problem ){
		annotations.add( new SpellingAnnotation( problem ) );
	}

	public void beginCollecting(){
		annotations.clear();	
	}

	public void endCollecting(){
		SpellingAnnotation[] annotations = this.annotations.toArray( new SpellingAnnotation[ this.annotations.size() ] );
		this.annotations.clear();
		job.schedule( annotations );
	}

	private class SpellingJob extends UIJob{
		private SpellingAnnotation[] annotations;
		
		public SpellingJob(){
			super( "Update spelling" );
			setSystem( true );
			setPriority( INTERACTIVE );
		}
		
		public void schedule( SpellingAnnotation[] annotations ){
			this.annotations = annotations;
			schedule();
		}

		@SuppressWarnings("unchecked")
		@Override
		public IStatus runInUIThread( IProgressMonitor monitor ){
			monitor.beginTask( "Set spelling errors", 2 );
			IAnnotationModel model = editor.getAnnotationModel();
			if( model == null ){
				monitor.done();
				return Status.OK_STATUS;
			}
			
			// remove old errors
			IProgressMonitor monitor2 = new SubProgressMonitor( monitor, 1 );
			monitor2.beginTask( "Set spelling errors", 1 );
			Iterator<Annotation> iterator = model.getAnnotationIterator();
			List<Annotation> toRemove = new ArrayList<Annotation>();
			while( iterator.hasNext() ){
				Annotation next = iterator.next();
				if( next instanceof SpellingAnnotation ){
					toRemove.add( next );
				}
			}
			for( Annotation annotation : toRemove ){
				model.removeAnnotation( annotation );
			}
			monitor2.done();
			
			// add new errors
			SpellingAnnotation[] annotations = this.annotations;
			if( annotations == null ){
				monitor.done();
			}
			else{
				monitor2 = new SubProgressMonitor( monitor, 1 );
				monitor2.beginTask( "Set spelling errors", annotations.length );
				for( int i = 0; i < annotations.length; i++ ){
					SpellingAnnotation annotation = annotations[i];
					SpellingProblem problem = annotation.getSpellingProblem();
					model.addAnnotation( annotation, new Position( problem.getOffset(), problem.getLength() ) );
					monitor2.worked( 1 );
				}
				monitor2.done();
			}
			monitor.done();
			return Status.OK_STATUS;
		}
		
	}
}
