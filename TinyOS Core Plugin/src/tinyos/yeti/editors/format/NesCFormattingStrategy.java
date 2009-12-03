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
package tinyos.yeti.editors.format;

import java.util.LinkedList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class NesCFormattingStrategy extends  ContextBasedFormattingStrategy  {

    /** Documents to be formatted by this strategy */
    private final LinkedList<IDocument> fDocuments= new LinkedList<IDocument>();

    /** Partitions to be formatted by this strategy */
    private final LinkedList<TypedPosition> fPartitions= new LinkedList<TypedPosition>();

    @Override
    public void formatterStops() {
        fPartitions.clear();
        fDocuments.clear();
        super.formatterStops();
    }

    @Override
    public void format() {
        super.format();
        final IDocument document= fDocuments.removeFirst();
        final TypedPosition position= fPartitions.removeFirst();
        if (document == null || position == null) return;

        TextEdit edit= null;

        /*
        try {
            int sourceOffset= document.getLineOffset(document.getLineOfOffset(position.getOffset()));


        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         */

        try {
            if (edit != null)
                edit.apply(document);
        } catch (MalformedTreeException x) {

        } catch (BadLocationException x) {

        }

    }

    @Override
    public void formatterStarts(IFormattingContext context) {
        super.formatterStarts(context);

        fPartitions.addLast( (TypedPosition)context.getProperty(FormattingContextProperties.CONTEXT_PARTITION));
        fDocuments.addLast( (IDocument)context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM));
    }

    /*
    private String getIdent() {
        boolean tabs = TinyOSPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.quickFixTabs);
        if (tabs) {
            return "\t";
        } else {
            return PreferenceConstants.spaces;
        }
    }
     */

}
