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

import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Behaves exactly like {@link ICompletionProposal} (including the extended
 * interfaces) but has also information about where to show this proposal.<br>
 * If two {@link INesCCompletionProposal}s have the same hashcode and are
 * equal, then only one of them will be shown.
 * @author Benjamin Sigg
 */
public interface INesCCompletionProposal extends ICompletionProposal{
    /**
     * Tells whether this proposal will insert something from this file, or
     * from another file.
     * @return <code>true</code> if this proposal inserts something from this
     * file, <code>false</code> otherwise
     */
    public boolean inFile();
}
