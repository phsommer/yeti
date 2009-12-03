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
package tinyos.yeti.nesc12.ep;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.INesCEditorParserClient;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.IEditorTokenScanner;
import tinyos.yeti.editors.nesc.util.NesCWhitespaceDetector;
import tinyos.yeti.editors.nesc.util.NesCWordDetector;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.editor.RangedWordRule;
import tinyos.yeti.nesc12.ep.rules.scanner.AttributeRule;
import tinyos.yeti.nesc12.ep.rules.scanner.OperatorRule;
import tinyos.yeti.utility.preferences.IPreferenceProvider;
import tinyos.yeti.utility.preferences.PreferenceToken;
import tinyos.yeti.utility.preferences.TextAttributeConstants;

public class NesC12CodeScanner extends RuleBasedScanner implements IEditorTokenScanner, INesCEditorParserClient{
    private static final String[] Keywords1 = 
        {   "configuration", "module", "component", "implementation" };
    
    private static final String[] Keywords2 = 
        {   "uses", "provides", "command", "event", "task", "generic", "new" };
    
    private static final String[] Keywords3 =
        {   "__attribute__", "norace", "async", "as", "atomic", "call", 
            "components", "default", "includes", "interface", "post", "signal" };
    
    private static final String[] Functions = 
        {   "unique", "uniqueN", "uniqueCount", "rcombine", "rcombine3", "rcombine4"};
    
    private static final String[] CKeywords =
    {   "auto", "break", "case", "const", "__const__", "continue", "default", "do",
        "else", "extern", "for", "goto", "if", "register", "__register__", "return",
        "signed", "sizeof", "static", "switch", "typedef", "unsigned",
        "void", "volatile", "__volatile__", "while", "inline", "__inline__", 
        "asm", "__asm__", "extension", "__extension__" };
        
    private static final String[] VarTypes =
    {   "types", "char", "float", "double", "int", "long", "short", "char",
        "void", "nx_union", "union", "enum", "nx_struct", "struct" };
    
    /*
    private static final String[] VarTypes =
    {   "types", "char", "float", "int", "long", "short", "bool",
        "char", "uint32_t", "uint16_t", "uint8_t", "int32_t",
        "int16_t", "int8_t", "void", "union", "enum", "struct" };
   */

    private NesCEditor editor;
    private RangedWordRule typedefRule;
    
    public NesC12CodeScanner() {
        // create tokens
        IPreferenceProvider<TextAttribute> provider = TinyOSPlugin.getDefault().getPreferences().getTextAttributes();
        
        IToken keyword1Token = new PreferenceToken<TextAttribute>( TextAttributeConstants.KEYWORDS1, provider );
        IToken keyword2Token = new PreferenceToken<TextAttribute>( TextAttributeConstants.KEYWORDS2, provider );
        IToken keyword3Token = new PreferenceToken<TextAttribute>( TextAttributeConstants.KEYWORDS3, provider );

        IToken functionToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.FUNCTION, provider );
        IToken attributeToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.ATTRIBUTE, provider );
        IToken cKeywordsToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.CKEYWORDS, provider );
        IToken varTypesToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.VARTYPES, provider );

        IToken operatorToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.OPERATOR, provider );

        IToken otherToken = new PreferenceToken<TextAttribute>( TextAttributeConstants.DEFAULT, provider );
        setDefaultReturnToken( otherToken );

        // construct rule
        List<IRule> rules= new ArrayList<IRule>();

        // typedefs
        typedefRule = new RangedWordRule( this, new NesCWordDetector(), varTypesToken );
        rules.add( typedefRule );
        
        // Different types of keywords
        WordRule wordRule= new WordRule(new NesCWordDetector(), otherToken);
        
        for (int i = 0; i < Keywords1.length; i++) {
            wordRule.addWord(Keywords1[i], keyword1Token);
        }   
        for (int i = 0; i < Keywords2.length; i++) {
            wordRule.addWord(Keywords2[i], keyword2Token);
        }
        for (int i = 0; i < Keywords3.length; i++) {
            wordRule.addWord(Keywords3[i], keyword3Token);
        }
        for (int i = 0; i < Functions.length; i++) {
            wordRule.addWord(Functions[i], functionToken);
        }
        for (int i = 0; i < CKeywords.length; i++) {
            wordRule.addWord(CKeywords[i], cKeywordsToken);
        }
        for (int i = 0; i < VarTypes.length; i++) {
            wordRule.addWord(VarTypes[i], varTypesToken);
        }

        rules.add(wordRule);
        
        // Add rule for operators and brackets
        rules.add(new OperatorRule(operatorToken));
        
        // Add generic whitespace rule.
        rules.add(new WhitespaceRule(new NesCWhitespaceDetector()));
        
        // varopis
        rules.add( new AttributeRule( attributeToken ) );

        IRule[] result= new IRule[rules.size()];
        result = rules.toArray(result);
        setRules(result);
    }
    
    /**
     * Gets the offset of the character stream.
     * @return the location of the next character this stream will return
     */
    public int getOffset(){
        return fOffset;
    }
    
    public void setEditor( NesCEditor editor ){
        if( this.editor != null )
            this.editor.removeParserClient( this );
        
        this.editor = editor;
        if( this.editor != null ){
            this.editor.addParserClient( this );
            this.editor.reconcileAsync();
        }
    }
    
    public void setupParser( NesCEditor editor, INesCParser nescParser ){
        Parser parser = (Parser)nescParser;
        parser.setCreateTypedefRangedCollection( true );
    }
    
    public void closeParser( NesCEditor editor, boolean successful, INesCParser nescParser ){
        Parser parser = (Parser)nescParser;
        typedefRule.setRanges( parser.getTypedefRangedCollection() );
        
        editor.invalidateTextPresentation();
    }
}