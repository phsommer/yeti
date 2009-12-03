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
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import tinyos.yeti.editors.INesCPartitions;

/**
 * This advanced version of NesC-partition scanner can handle comments in
 * preprocessor directives.
 * @author Benjamin Sigg
 */
public class NesCPartitionScanner2 extends ProjectingPartitionScanner implements INesCPartitions {
	private static IToken preprocessorDirective = new Token( PREPROCESSOR_DIRECTIVE );
    private static IToken multiLineComment= new Token( MULTI_LINE_COMMENT );
    private static IToken singleLineComment= new Token( NESC_SINGLE_LINE_COMMENT );
    private static IToken nesCDoc= new Token( NESC_DOC );
    private static IToken stringToken = new Token( NESC_STRING );
    private static IToken defaultToken = new Token( "default" );


    /**
     * Detector for empty comments.
     */
    static class EmptyCommentDetector implements IWordDetector {

        /*
         * @see IWordDetector#isWordStart
         */
        public boolean isWordStart(char c) {
            return (c == '/');
        }

        /*
         * @see IWordDetector#isWordPart
         */
        public boolean isWordPart(char c) {
            return (c == '*' || c == '/');
        }
    }


    /**
     * Word rule for empty comments.
     */
    static class EmptyCommentRule extends WordRule implements IPredicateRule {

        private IToken fSuccessToken;
        /**
         * Constructor for EmptyCommentRule.
         * @param successToken
         */
        public EmptyCommentRule(IToken successToken) {
            super(new EmptyCommentDetector());
            fSuccessToken= successToken;
            addWord("/**/", fSuccessToken); //$NON-NLS-1$
        }

        /*
         * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
         */
        public IToken evaluate(ICharacterScanner scanner, boolean resume) {
            return evaluate(scanner);
        }

        /*
         * @see IPredicateRule#getSuccessToken()
         */
        public IToken getSuccessToken() {
            return fSuccessToken;
        }
    }

    public NesCPartitionScanner2() {
        super( new TopLevelPartitionScanner() );
        
        setContentScanner( NESC_DOC, new SinglePartitionScanner( nesCDoc ));
        setContentScanner( NESC_SINGLE_LINE_COMMENT, new SinglePartitionScanner( singleLineComment ));
        setContentScanner( MULTI_LINE_COMMENT, new SinglePartitionScanner( multiLineComment ));
        
        RuleBasedPartitionScanner content = new RuleBasedPartitionScanner();
        List<IPredicateRule> rules= new ArrayList<IPredicateRule>();

        // preprocessor
        rules.add(new WhitespaceStartRule(new EndOfLineRuleWithWhitespace( "#", preprocessorDirective, '\\' )));
        
        rules.add(new SingleLineRule("\"", "\"", stringToken, '\\'));
        rules.add(new SingleLineRule("L\"", "\"", stringToken, '\\'));
        rules.add(new SingleLineRule("'", "'", stringToken, '\\'));
        rules.add(new SingleLineRule("L'", "'", stringToken, '\\'));
        
        content.setPredicateRules( rules.toArray( new IPredicateRule[ rules.size() ] ) );
        setContentScanner( "default", content );
    }
    
    private static class SinglePartitionScanner implements IPartitionTokenScanner{
    	private IToken token;
    	private int offset;
    	private int length;
    	
    	private boolean called = false;
    	
    	public SinglePartitionScanner( IToken token ){
    		this.token = token;
    	}

		public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
			setRange( document, offset, length );
		}
		
		public void setRange(IDocument document, int offset, int length) {
			this.offset = offset;
			this.length = length;
			called = false;
		}
		
		public int getTokenLength() {
			return length;
		}

		public int getTokenOffset() {
			return offset;
		}

		public IToken nextToken() {
			if( called ){
				offset += length;
				length = -1;
				return Token.EOF;
			}
		
			called = true;
			return token;
		}    	
    }
    
    /**
     * The top level partition scanner finds comments and leaves
     * the rest for the lower scanners.
     * @author Benjamin Sigg
     */
    private static class TopLevelPartitionScanner extends RuleBasedPartitionScanner{
    	public TopLevelPartitionScanner() {
            List<IPredicateRule> rules= new ArrayList<IPredicateRule>();
            
            setDefaultReturnToken( defaultToken );
            // rules.add(new EndOfLineRule("//", singleLineComment));
            rules.add( new EndOfLineRuleExcludingNewline( "//", singleLineComment ));

            // Add special case word rule.
            EmptyCommentRule wordRule= new EmptyCommentRule(multiLineComment);
            rules.add(wordRule);

            // Add rules for multi-line comments and javadoc.
            rules.add(new SingleLineRule("\"", "\"", defaultToken, '\\'));
            rules.add(new SingleLineRule("L\"", "\"", defaultToken, '\\'));
            rules.add(new SingleLineRule("'", "'", defaultToken, '\\'));
            rules.add(new SingleLineRule("L'", "'", defaultToken, '\\'));
            
            rules.add(new MultiLineRule("/**", "*/", nesCDoc));
            rules.add(new MultiLineRule("/*", "*/", multiLineComment));	


            IPredicateRule[] result= new IPredicateRule[rules.size()];
            rules.toArray(result);
            setPredicateRules(result);
		}
    }
}
