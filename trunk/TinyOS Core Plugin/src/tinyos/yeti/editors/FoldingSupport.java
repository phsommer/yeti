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
package tinyos.yeti.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

import tinyos.yeti.ep.parser.IFoldingRegion;

/**
 * Support to manage {@link IFoldingRegion}s.
 * @author Benjamin Sigg
 *
 */
public class FoldingSupport{
	private Map<Annotation, Position> added;
	private Annotation[] removed;
	
	/**
	 * Updates the contents of this support.
	 * @param document the document to udpate
	 * @param model the model with the current annotations
	 * @param region the new regions
	 * @throws BadLocationException 
	 */
	@SuppressWarnings("unchecked")
	public void update( IDocument document, IAnnotationModel model, IFoldingRegion[] regions ) throws BadLocationException{
		// calculate new regions
		Set<Position> newRegions = new HashSet<Position>();
		int documentLength = document.getLength();
		
		for( IFoldingRegion region : regions ){
			Position position = toPosition( document, documentLength, region );
			if( position != null ){
				newRegions.add( position );
			}
		}
		
		// find old regions
		Iterator<Annotation> annotations = model.getAnnotationIterator();
		Map<Position, Annotation> oldRegions = new HashMap<Position, Annotation>();
		
		while( annotations.hasNext() ){
			Annotation next = annotations.next();
			if( next instanceof ProjectionAnnotation ){
				oldRegions.put( model.getPosition( next ), next );
			}
		}
		
		// find added regions
		added = new HashMap<Annotation, Position>();
		for( Position position : newRegions ){
			if( !oldRegions.containsKey( position )){
				added.put( new ProjectionAnnotation(), position );
			}
		}
		
		// find removed regions
		List<Annotation> removed = new ArrayList<Annotation>();
		for( Map.Entry<Position, Annotation> entry : oldRegions.entrySet() ){
			if( !newRegions.contains( entry.getKey() )){
				removed.add( entry.getValue() );
			}
		}
		
		this.removed = removed.toArray( new Annotation[ removed.size() ] );
	}
	
	private Position toPosition( IDocument document, int documentLength, IFoldingRegion region ) throws BadLocationException{
    	int foldingOffset = Math.max( 0, Math.min( documentLength-1, region.getOffset() ) );
    	int foldingLength = Math.min( documentLength-foldingOffset, region.getLength() );
    	
    	if( foldingLength <= 0 ){
    		return null;
    	}
    	
        int lineBegin = document.getLineOfOffset( foldingOffset );
        int offsetEnd = foldingOffset + foldingLength - 1;
        
        while( offsetEnd > 0 && Character.isWhitespace( document.getChar( offsetEnd ) )){
            offsetEnd--;
        }

        int lineEnd = document.getLineOfOffset( offsetEnd );

        if( lineBegin < lineEnd ){
            int offset = document.getLineOffset( lineBegin );
            int length = document.getLineOffset( lineEnd ) + document.getLineLength( lineEnd ) - offset;
            
            if( offset < 0 ){
                length += offset;
                offset = 0;
            }
            
            if( offset >= documentLength )
                offset = documentLength-1;
            
            if( offset + length > documentLength )
                length = documentLength - offset;

            return new Position( offset, length );
        }
        return null;
	}
	
	public Annotation[] getRemovedAnnotations(){
		return removed;
	}
	
	public Map<Annotation, Position> getAddedAnnotations(){
		return added;
	}
}
