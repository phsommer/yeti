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
package tinyos.yeti.utility.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;

public abstract class TextAttributeConstants{
    // comments
    public static final String COMMENT_SINGLE_LINE = "comment_single";
    public static final String COMMENT_MULTI_LINE = "comment_multi_line";
    
    // Nesc Java-Doc like comments
    public static final String NESC_DOC_COMMENT = "nesc_doc_comment";
    public static final String NESC_DOC_KEYWORD = "nesc_doc_keyword";
    public static final String NESC_DOC_TAG = "nesc_doc_tag";
    public static final String NESC_DOC_LINK = "nesc_doc_link";

    // keywords
    public static final String KEYWORDS1 = "nesc_keywords_1";
    public static final String KEYWORDS2 = "nesc_keywords_2";
    public static final String KEYWORDS3 = "nesc_keywords_3";

    // other
    public static final String FUNCTION = "nesc_function";
    public static final String ATTRIBUTE = "nesc_attribute";
    public static final String CKEYWORDS = "nesc_c_keywords";
    public static final String VARTYPES = "nesc_vartypes";
    public static final String STRING = "nesc_string";

    public static final String OPERATOR = "nesc_operator";
    
    // Preprocessor
    public static final String PREPROCESSOR = "nesc_preprocessor";
    public static final String PREPROCESSOR_DIRECTIVE = "nesc_preprocessor_directive";

    // default
    public static final String DEFAULT = "nesc_default";

    
    public static final String[] ALL_KEYS = { 
        COMMENT_SINGLE_LINE,
        COMMENT_MULTI_LINE,
        
        NESC_DOC_COMMENT,
        NESC_DOC_KEYWORD,
        NESC_DOC_TAG,
        NESC_DOC_LINK,
        
        KEYWORDS1,
        KEYWORDS2,
        KEYWORDS3,
        
        FUNCTION,
        ATTRIBUTE,
        CKEYWORDS,
        VARTYPES,
        STRING,
        
        OPERATOR,
        
        PREPROCESSOR,
        PREPROCESSOR_DIRECTIVE,
        
        DEFAULT
    };
    
    public static final String[] ALL_LABELS = {
        "Comment (single line)",
        "Comment (multi line)",
        
        "Nesdoc: standard",
        "Nesdoc: Keyword",
        "Nesdoc: Tag",
        "Nesdoc: Link",
        
        "NesC Keyword (components)",
        "NesC Keyword (wiring)",
        "NesC Keyword",
        
        "NesC Function",
        "Attribute",
        "c - Keyword",
        "Types",
        "String",
        
        "Operator / Bracket",
        
        "Preprocessor",
        "Preprocessor-Directive",
        
        "Default"
    };
    
    public static void writeDefaults( IPreferenceStore store ){
        // comments
        write( COMMENT_SINGLE_LINE, store, ColorConstants.GREEN, SWT.NONE );
        write( COMMENT_MULTI_LINE, store, ColorConstants.GREEN, SWT.NONE );
        
        // Nesc Java-Doc like comments
        write( NESC_DOC_COMMENT, store, ColorConstants.BLUE, SWT.NONE );
        write( NESC_DOC_KEYWORD, store, ColorConstants.BLUE, SWT.BOLD );
        write( NESC_DOC_TAG, store, ColorConstants.BLUE, SWT.ITALIC );
        write( NESC_DOC_LINK, store, ColorConstants.BLUE3, SWT.NONE );
        
        // keywords
        write( KEYWORDS1, store, ColorConstants.BLUE4, SWT.BOLD );
        write( KEYWORDS2, store, ColorConstants.BLACK, SWT.BOLD );
        write( KEYWORDS3, store, ColorConstants.VIOLET, SWT.BOLD );
        
        // other
        write( FUNCTION, store, ColorConstants.VIOLET, SWT.NONE );
        write( ATTRIBUTE, store, ColorConstants.BLACK, SWT.ITALIC );
        write( CKEYWORDS, store, ColorConstants.VIOLET, SWT.BOLD );
        write( VARTYPES, store, ColorConstants.VIOLET, SWT.NONE );
        write( STRING, store, ColorConstants.BLUE2, SWT.NONE );
        
        write( OPERATOR, store, ColorConstants.BLACK, SWT.NONE );
        
        write( PREPROCESSOR, store, ColorConstants.GREY, SWT.NONE );
        write( PREPROCESSOR_DIRECTIVE, store, ColorConstants.GREY, SWT.BOLD );
        
        write( DEFAULT, store, ColorConstants.BLACK, SWT.NONE );
    }
    
    private static void write( String key, IPreferenceStore store, String color, int style ){
        store.setDefault( toColorKey( key ), color );
        store.setDefault( toStyleBoldKey( key ), (style & SWT.BOLD) != 0 );
        store.setDefault( toStyleItalicKey( key ), (style & SWT.ITALIC) != 0 );
    }
    
    private TextAttributeConstants(){
        // nothing
    }
    
    public static String toColorKey( String key ){
        return key + "_color";
    }
    
    public static String toStyleBoldKey( String key ){
        return key + "_style_bold";
    }
    
    public static String toStyleItalicKey( String key ){
        return key + "_style_italic";
    }

    public static String toNormalKey( String key ){
        if( key.endsWith( "_color" ))
            return key.substring( 0, key.length() - 6 );
        
        if( key.endsWith( "_style_bold" ))
            return key.substring( 0, key.length() - 11 );
        
        if( key.endsWith( "_style_italic" ))
            return key.substring( 0, key.length() - 13 );
        
        return null;
    }
}
