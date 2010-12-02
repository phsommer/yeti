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

import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.utility.preferences.IMultiPreferenceProvider;

public interface INesCPresentationReconcilerDefaults {
	public static final String SCANNER_NAME_NESC_DOC = "doc";
	public static final String SCANNER_NAME_SINGLELINE_COMMENT = "singleline_comment";
	public static final String SCANNER_NAME_MULTILINE_COMMENT = "multiline_comment";
	public static final String SCANNER_NAME_PREPROCESSOR = "preprocessor";
	public static final String SCANNER_NAME_DEFAULT = "default";
	public static final String SCANNER_NAME_STRING = "string";
	
	public NesCEditor getEditor();
	
    public IMultiPreferenceProvider getPreferences();
    public String getDocumentPartitioning();
    
    public DefaultDamagerRepairer createPreprocessorDirectiveRepairer();
    public String getPreprocessorDirectiveContentType();
    
    public DefaultDamagerRepairer createNesCDocRepairer();
    public String getNesCDocContentType();
    
    public DefaultDamagerRepairer createMultilineCommentRepairer();
    public String getMultilineCommentContentType();
    
    public DefaultDamagerRepairer createSinglelineCommentRepairer();
    public String getSinglelineCommentContentType();
    
    public DefaultDamagerRepairer createStringRepairer();
    public String getStringContentType();
    
    public IEditorDamageRepairer createDefaultRepairer();
    public String getDefaultContentType();
}
