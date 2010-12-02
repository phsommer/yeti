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

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.IQuickFixInformation;

public class SpellingQuickFixInformation implements IQuickFixInformation{
	private SpellingAnnotation annotation;
	private NesCEditor editor;
	
	public SpellingQuickFixInformation( SpellingAnnotation annotation, NesCEditor editor ){
		this.annotation = annotation;
		this.editor = editor;
	}
	
	public Annotation getAnnotation(){
		return annotation;
	}

	public ICompletionProposal[] getCompletionProposals(){
		return annotation.getSpellingProblem().getProposals();
	}

	public int getOffset(){
		return annotation.getSpellingProblem().getOffset();
	}

	public ISourceViewer getViewer(){
		return editor.getEditorSourceViewer();
	}
	
	public void proposalApplied(){
		editor.getSpellingSupport().recheck();
	}
}
