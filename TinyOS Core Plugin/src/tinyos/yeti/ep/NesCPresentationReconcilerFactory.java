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
package tinyos.yeti.ep;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;

import tinyos.yeti.editors.NesCEditor;

/**
 * Standard implementation for NesC 1.1
 * @author Benjamin Sigg
 */
public class NesCPresentationReconcilerFactory implements INesCPresentationReconcilerFactory {
    public IPresentationReconciler create(
            ISourceViewer sourceViewer,
            INesCPresentationReconcilerDefaults defaults ) {

        Reconciler reconciler = new Reconciler( defaults.getEditor() );

        reconciler.setDocumentPartitioning( defaults.getDocumentPartitioning());

        DefaultDamagerRepairer repairer;
        
        // NesC Doc - Comment
        repairer = defaults.createNesCDocRepairer();
        reconciler.setDamager( repairer, defaults.getNesCDocContentType() );
        reconciler.setRepairer( repairer, defaults.getNesCDocContentType() );
        
        // Multiline - Comment
        repairer = defaults.createMultilineCommentRepairer();
        reconciler.setDamager( repairer, defaults.getMultilineCommentContentType() );
        reconciler.setRepairer( repairer, defaults.getMultilineCommentContentType() );

        // SingleLine - Comment
        repairer = defaults.createSinglelineCommentRepairer();
        reconciler.setDamager( repairer, defaults.getSinglelineCommentContentType() );
        reconciler.setRepairer( repairer, defaults.getSinglelineCommentContentType() );

        // Preprocessor
        repairer = defaults.createPreprocessorDirectiveRepairer();
        reconciler.setDamager( repairer, defaults.getPreprocessorDirectiveContentType() );
        reconciler.setRepairer( repairer, defaults.getPreprocessorDirectiveContentType() );
        
        // String
        repairer = defaults.createStringRepairer();
        reconciler.setDamager( repairer, defaults.getStringContentType() );
        reconciler.setRepairer( repairer, defaults.getStringContentType() );
        
        // default
        final IEditorDamageRepairer defaultRepairer = defaults.createDefaultRepairer();
        reconciler.addRepairer( defaultRepairer );
        reconciler.setDamager( defaultRepairer.asDamager(), defaults.getDefaultContentType() );
        reconciler.setRepairer( defaultRepairer.asRepairer(), defaults.getDefaultContentType() );

        return reconciler;
    }
    
    private static class Reconciler extends PresentationReconciler {
        private List<IEditorDamageRepairer> repairers = new ArrayList<IEditorDamageRepairer>();
        private NesCEditor editor;
        private int installed = 0;
        
        public Reconciler( NesCEditor editor ){
            this.editor = editor;
        }
        
        public void addRepairer( IEditorDamageRepairer repairer ){
            repairers.add( repairer );
        }
        
        @Override
        public void install( ITextViewer viewer ){
            super.install( viewer );
            if( installed == 0 ){
                for( IEditorDamageRepairer repairer : repairers )
                    repairer.setEditor( editor );
            }
            installed++;
        }
        
        @Override
        public void uninstall(){
            super.uninstall();
            installed--;
            if( installed == 0 ){
                for( IEditorDamageRepairer repairer : repairers )
                    repairer.setEditor( null );
            }
        }
    }
}
