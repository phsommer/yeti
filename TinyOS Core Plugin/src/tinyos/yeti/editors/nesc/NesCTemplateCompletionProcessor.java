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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.preferences.PreferenceConstants;

public abstract class NesCTemplateCompletionProcessor extends TemplateCompletionProcessor{

    protected static final class ProposalComparator implements Comparator<TemplateProposal> {
        public int compare(TemplateProposal o1, TemplateProposal o2) {
            return o2.getRelevance() - o1.getRelevance();
        }
    }

    @Override
    protected int getRelevance(Template template, String prefix) {
        if (template.getName().startsWith(prefix.toLowerCase()))
            return 90;
        return 0;
    }

    @Override
    protected Template[] getTemplates(String contextTypeId) {

        Template[] t = TinyOSPlugin.getDefault().getTemplateStore().getTemplates(contextTypeId);

        return t;
    }

    @Override
    protected Image getImage(Template template) {
        return NesCIcons.icons().get(NesCIcons.ICON_TEMPLATE);
    }

    /**
     * modified to take spaces/tabs into account
     */
    @Override
    protected TemplateProposal createProposal(Template template, TemplateContext context, IRegion region, int relevance) {
        String templatePattern = template.getPattern();
        boolean tabs = TinyOSPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.USE_TABS);
        if (!tabs) {
            templatePattern =  templatePattern.replaceAll("\t", PreferenceConstants.spacesPerTab() );
        } else {
            // leave it be...
        }
        Template t = new Template(template.getName(),template.getDescription(),template.getContextTypeId(), templatePattern, true);

        return new TemplateProposal(t, context, region, getImage(template), relevance);
    }

    protected static final Comparator<TemplateProposal> fgProposalComparator= new ProposalComparator();

    /*
     * modified to get right identation-level
     */
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

        ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

        // adjust offset to end of normalized selection
        if (selection.getOffset() == offset)
            offset= selection.getOffset() + selection.getLength();

        String prefix= extractPrefix(viewer, offset);
        Region region= new Region(offset - prefix.length(), prefix.length());
        TemplateContext context= createContext(viewer, region);
        if (context == null)
            return new ICompletionProposal[0];

        context.setVariable("selection", selection.getText()); // name of the selection variables {line, word}_selection //$NON-NLS-1$

        Template[] templates= getTemplates(context.getContextType().getId());

        List<TemplateProposal> matches= new ArrayList<TemplateProposal>();
        String lowerPrefix = prefix.toLowerCase();
        
        for (int i= 0; i < templates.length; i++) {
            Template template= templates[i];
            try {
                context.getContextType().validate(template.getPattern());
            } catch (TemplateException e) {
                continue;
            }
            
            if( template.getName().toLowerCase().startsWith( lowerPrefix ) && template.matches(prefix, context.getContextType().getId()) ) {
                // get identation of first word on the same line like the offset
                String delim = "";
                try {
                    int line = viewer.getDocument().getLineOfOffset(offset);
                    IDocument document = viewer.getDocument();
                    
                    int offsetFirstNonWhiteSpaceCharacter = document.getLineOffset(line);
                    while( offsetFirstNonWhiteSpaceCharacter < offset ){
                        char c = document.getChar( offsetFirstNonWhiteSpaceCharacter );
                        if( Character.isWhitespace( c )){
                            delim += c;
                            offsetFirstNonWhiteSpaceCharacter++;
                        }
                        else
                            break;
                    }
                } catch (BadLocationException e) {
                    // ignore
                    // e.printStackTrace();
                }
                // insert identation 
                String pattern = template.getPattern().replaceAll("\n","\n"+delim);
                Template t = new Template(template.getName(),template.getDescription(),template.getContextTypeId(),pattern, true);
                matches.add(createProposal(t, context, (IRegion) region, getRelevance(template, prefix)));
            }
        }

        TemplateProposal[] result = matches.toArray(new TemplateProposal[matches.size()]);
        Arrays.sort( result, fgProposalComparator );
        return result; 
    }

}
