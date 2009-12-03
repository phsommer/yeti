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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.TemplateContextType;

import tinyos.yeti.TinyOSPlugin;

/**
 * modified to take identation-level into account..
 * @author dcg
 *
 */
public class CTemplateCompletionProcessor extends NesCTemplateCompletionProcessor {

    @Override
    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
        return TinyOSPlugin.getDefault().getContextTypeRegistry().getContextType(NescContextType.NESC_CONTEXTTYPE);
    }
}
