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
package tinyos.yeti.nesc12.ep.editor;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

import tinyos.yeti.nesc12.ep.NesC12CodeScanner;
import tinyos.yeti.nesc12.parser.meta.NamedType;
import tinyos.yeti.nesc12.parser.meta.RangedCollection;

/**
 * A {@link RangedWordRule} uses a {@link RangedCollection} to find matching
 * words for a specific location.
 * @author Benjamin Sigg
 */
public class RangedWordRule implements IRule{
    private RangedCollection<NamedType> ranges;
    
    private NesC12CodeScanner scanner;
    private IToken token;
    
    private StringBuilder buffer = new StringBuilder();
    private IWordDetector detector;
    
    public RangedWordRule( NesC12CodeScanner scanner, IWordDetector detector, IToken token ){
        this.scanner = scanner;
        this.detector = detector;
        this.token = token;
    }
    
    public void setRanges( RangedCollection<NamedType> ranges ) {
        this.ranges = ranges;
    }

    public IToken evaluate( ICharacterScanner scanner ) {
        if( ranges == null )
            return Token.UNDEFINED;

        readWord( scanner );
        if( buffer.length() == 0 )
            return Token.UNDEFINED;

        Search search = new Search( buffer.toString() );
        ranges.visit( this.scanner.getOffset(), search );
        if( search.getResult() )
            return token;

        for( int i = buffer.length(); i > 0; --i )
            scanner.unread();

        return Token.UNDEFINED;
    }
    
    private void readWord( ICharacterScanner scanner ){
        int c= scanner.read();
        buffer.setLength(0);
        if( c != ICharacterScanner.EOF && detector.isWordStart( (char)c) ) {
            do {
                buffer.append( (char)c );
                c= scanner.read();
            } 
            while( c != ICharacterScanner.EOF && detector.isWordPart( (char)c) );
            scanner.unread();
        }
        if( buffer.length() == 0 )
            scanner.unread();
    }
    
    private static class Search implements RangedCollection.Visitor<NamedType>{
        private String name;
        private boolean result = false;
        
        public Search( String name ){
            this.name = name;
        }
        
        public boolean getResult(){
            return result;
        }
        
        public boolean visit( NamedType value ){
            result = value.getName().toIdentifier().equals( name );
            return !result;
        }
    }
}
