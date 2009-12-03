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
package tinyos.yeti.preprocessor.parser.stream;

import tinyos.yeti.preprocessor.lexer.Symbols;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class SkipableElementStream extends ElementStream{
    private PreprocessorElement skipped;
    
    public SkipableElementStream( PreprocessorElement root, boolean parentFirst ) {
        super( root, parentFirst );
    }
    
    @Override
    public boolean hasNext() {
        return skipped != null || super.hasNext();
    }
    
    @Override
    public PreprocessorElement next() {
        if( skipped != null ){
            PreprocessorElement next = skipped;
            skipped = null;
            return next;
        }
        return super.next();
    }

    /**
     * Reads and throws away all elements that match the condition until
     * one element does not match the condition.
     * @param condition some condition, not <code>null</code>
     */
    public void skip( SkipCondition condition ){
        while( hasNext() ){
            PreprocessorElement next = next();
            if( !condition.skip( next )){
                skipped = next;
                break;
            }
        }
    }
    
    public void skipWhitespaces(){
        skip( or( missingToken(), whitespace() ));
    }
    
    public void skipNewline(){
        skip( or( missingToken(), newline() ));
    }
    
    public static SkipCondition newline(){
        return new SkipCondition(){
            public boolean skip( PreprocessorElement element ) {
                return element.getToken().getKind() == Symbols.NEWLINE;
            }
        };
    }
    
    public static SkipCondition whitespace(){
        return new SkipCondition(){
            public boolean skip( PreprocessorElement element ) {
                return element.getToken().getKind() == Symbols.WHITESPACE;
            }
        };
    }
    
    public static SkipCondition not( final SkipCondition condition ){
        return new SkipCondition(){
            public boolean skip( PreprocessorElement element ) {
                return !condition.skip( element );
            }
        };
    }
    
    public static SkipCondition missingToken(){
        return new SkipCondition(){
            public boolean skip( PreprocessorElement element ) {
                return element.getToken() == null;
            }
        };
    }
    
    public static SkipCondition missingText(){
        return new SkipCondition(){
            public boolean skip( PreprocessorElement element ) {
                if( element.getToken() == null )
                    return true;
                
                if( element.getToken().getText() == null )
                    return true;
                
                return false;
            }
        };
    }
    
    public static SkipCondition or( final SkipCondition a, final SkipCondition b ){
        return new SkipCondition(){
            public boolean skip( PreprocessorElement element ) {
                return a.skip( element ) || b.skip( element );
            }
        };
    }
    
    public static SkipCondition and( final SkipCondition a, final SkipCondition b ){
        return new SkipCondition(){
            public boolean skip( PreprocessorElement element ) {
                return a.skip( element ) && b.skip( element );
            }
        };
    }
    
    public interface SkipCondition{
        public boolean skip( PreprocessorElement element );
    }
}
