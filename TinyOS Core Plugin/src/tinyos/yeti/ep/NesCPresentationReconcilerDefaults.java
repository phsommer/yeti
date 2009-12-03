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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.INesCPartitions;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.utility.preferences.IMultiPreferenceProvider;

public class NesCPresentationReconcilerDefaults implements INesCPresentationReconcilerDefaults{
	private NesCEditor editor;
	private String documentPartitioning;
	
	public NesCPresentationReconcilerDefaults( NesCEditor editor, String documentPartitioning ){
		this.editor = editor;
		this.documentPartitioning = documentPartitioning;
	}
	
    public NesCEditor getEditor(){
     	return editor;
    }

    public IEditorDamageRepairer createDefaultRepairer(){
        return new DefaultEditorDamageRepairer( TinyOSPlugin.getDefault().getScanner( SCANNER_NAME_DEFAULT ) );
    }

    public DefaultDamagerRepairer createMultilineCommentRepairer(){
    	return new DefaultEditorDamageRepairer( TinyOSPlugin.getDefault().getScanner( SCANNER_NAME_MULTILINE_COMMENT ) );
    }

    public DefaultDamagerRepairer createNesCDocRepairer(){
    	return new DefaultEditorDamageRepairer( TinyOSPlugin.getDefault().getScanner( SCANNER_NAME_NESC_DOC ) );
    }
    
    public DefaultDamagerRepairer createPreprocessorDirectiveRepairer() {
    	return new DefaultEditorDamageRepairer( TinyOSPlugin.getDefault().getScanner( SCANNER_NAME_PREPROCESSOR ) );
    }

    public DefaultDamagerRepairer createSinglelineCommentRepairer(){
    	return new DefaultEditorDamageRepairer( TinyOSPlugin.getDefault().getScanner( SCANNER_NAME_SINGLELINE_COMMENT ) );
    }
    
    public DefaultDamagerRepairer createStringRepairer() {
    	return new DefaultEditorDamageRepairer( TinyOSPlugin.getDefault().getScanner( SCANNER_NAME_STRING ) );
    }

    public IMultiPreferenceProvider getPreferences(){
        return TinyOSPlugin.getDefault().getPreferences();
    }

    public String getDefaultContentType(){
        return IDocument.DEFAULT_CONTENT_TYPE;
    }
    
    public String getPreprocessorDirectiveContentType() {
    	return INesCPartitions.PREPROCESSOR_DIRECTIVE;
    }

    public String getMultilineCommentContentType(){
        return INesCPartitions.MULTI_LINE_COMMENT;
    }

    public String getNesCDocContentType(){
        return INesCPartitions.NESC_DOC;
    }

    public String getSinglelineCommentContentType(){
        return INesCPartitions.NESC_SINGLE_LINE_COMMENT;
    }
    
    public String getStringContentType(){
    	return INesCPartitions.NESC_STRING;
    }

    public String getDocumentPartitioning(){
        return documentPartitioning;
    }
}
