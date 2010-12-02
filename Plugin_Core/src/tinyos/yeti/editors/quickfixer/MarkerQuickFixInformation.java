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
package tinyos.yeti.editors.quickfixer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.texteditor.MarkerUtilities;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.IQuickFixInformation;

public class MarkerQuickFixInformation implements IQuickFixInformation{
	private NesCEditor editor;
	private Annotation annotation;
	private IMarker marker;
	private ICompletionProposal[] proposals;
	
	public MarkerQuickFixInformation( NesCEditor editor, Annotation annotation, IMarker marker, IMarkerResolution[] resolutions ){
		this.editor = editor;
		this.annotation = annotation;
		this.marker = marker;
		if( resolutions == null ){
			proposals = new ICompletionProposal[]{};
		}
		else{
			proposals = new ICompletionProposal[ resolutions.length ];
			for( int i = 0; i < resolutions.length; i++ ){
				proposals[i] = new MarkerCompletion( marker, resolutions[i] );
			}
		}
	}
	
	public Annotation getAnnotation(){
		return annotation;
	}

	public ICompletionProposal[] getCompletionProposals(){
		return proposals;
	}

	public int getOffset(){
		return MarkerUtilities.getCharStart( marker );
	}

	public ISourceViewer getViewer(){
		return editor.getEditorSourceViewer();
	}

	public void proposalApplied(){
		// ignore
	}

}
