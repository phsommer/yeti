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
package tinyos.yeti.editors.nesc;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Information about a {@link IQuickFixableAnnotation}.
 * @author Benjamin Sigg
 */
public interface IQuickFixInformation{
	/**
	 * Gets the annotation for which this object provides information
	 * @return the associated annotation
	 */
	public Annotation getAnnotation();
	
	/**
	 * Gets proposals how to fix the problem that is described by
	 * {@link #getAnnotation() the annotation}.
	 * @return the proposals
	 */
	public ICompletionProposal[] getCompletionProposals();
	
	/**
	 * Called after a proposal of this {@link IQuickFixInformation} has
	 * been applied.
	 */
	public void proposalApplied();
	
	/**
	 * Gets the source viewer in which the annotation is shown and the
	 * quickfixes are applied.
	 * @return the source viewer
	 */
	public ISourceViewer getViewer();
	
	/**
	 * Gets the offset of the annotation in the viewer.
	 * @return the offset
	 */
	public int getOffset();
}
