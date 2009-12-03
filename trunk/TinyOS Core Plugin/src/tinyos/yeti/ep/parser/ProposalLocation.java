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
package tinyos.yeti.ep.parser;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.IDocumentMap;

/**
 * Used to identify the location where an {@link ICompletionProposal}
 * might be inserted.
 * @author Benjamin Sigg
 *
 */
public class ProposalLocation {
    private int offset;
    private String prefix;
    private ITextViewer viewer;
    private IDocumentMap document;
    private ProjectTOS project;
    
    public ProposalLocation( ProjectTOS project, ITextViewer viewer, IDocumentMap document, int offset, String prefix ){
        this.project = project;
        this.viewer = viewer;
        this.document = document;
        this.offset = offset;
        this.prefix = prefix;
    }
    
    /**
     * Gets the project in which the edited file lies.
     * @return the project, might be <code>null</code>
     */
    public ProjectTOS getProject() {
        return project;
    }
    
    /**
     * Gets the exact location of the cursor.
     * @return the location
     */
    public int getOffset() {
        return offset;
    }
    
    /**
     * Gets the word that is already typed in.
     * @return the word which the user just is writing
     */
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * Gets the viewer for which the proposals are needed
     * @return the viewer
     */
    public ITextViewer getViewer() {
        return viewer;
    }
    
    /**
     * Gets the document which the user currently editing.
     * @return the edited document
     */
    public IDocumentMap getDocument() {
        return document;
    }
}
