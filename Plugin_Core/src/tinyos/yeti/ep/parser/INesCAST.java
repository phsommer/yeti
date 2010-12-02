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

import tinyos.yeti.ep.IParseFile;

/**
 * An {@link INesCAST} is the abstract syntax tree that is created by the
 * {@link INesCParser}. An {@link INesCAST} is a heavyweight component which 
 * carries a lot of information. Clients should not use too many of these
 * {@link INesCAST}s at the same time.
 * @author Benjamin Sigg
 */
public interface INesCAST {    
    /**
     * Gets the name of the file that was parsed to obtain this 
     * {@link INesCAST}.
     * @return the file that was parsed, may be <code>null</code> if it was
     * not given to the parser
     */
    public IParseFile getParseFile();
    
    /**
     * Tries to find completion proposals for the file that is represented
     * by this model.
     * @param location the location where the cursor currently is
     * @return the proposals, can be <code>null</code> but must not contain
     * <code>null</code> entries
     */
    public INesCCompletionProposal[] getProposals( ProposalLocation location );
    
    /**
     * Gets all the hyperlinks to which one could go from <code>location</code>.
     * @param location some location
     * @return the hyperlinks at that location, usally only one hyperlink
     * should be returned. <code>null</code> indicates that there are no
     * hyperlinks at that location.
     */
    public IFileHyperlink[] getHyperlinks( IDocumentRegion location );
    
    /**
     * Gets information for a hover that is shown over the selected region.
     * @param location the location of the mouse
     * @return hover information or <code>null</code>
     */
    public IHoverInformation getHoverInformation( IDocumentRegion location );
}
