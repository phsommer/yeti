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
package tinyos.yeti.nesc12.ep.rules.hyperlink;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.standard.FileHyperlink;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.preprocessor.IncludeFile;
import tinyos.yeti.preprocessor.IncludeProvider;

public class IncludeHyperlink implements IHyperlinkRule{
    public static void main( String[] args ){
        IncludeHyperlink link = new IncludeHyperlink();
        
        link.search( null, 14, new Region( 0, 20 ), "d # include <test>", null );
    }
    
    private boolean systemFile;

    public void search( NesC12AST ast, HyperlinkCollector collector ){
        try{
            IDocument document = collector.getLocation().getDocument().getDocument();
            int offset = collector.getLocation().getRegion().getOffset();

            IRegion lineRegion = document.getLineInformationOfOffset( offset );
            String line = document.get( lineRegion.getOffset(), lineRegion.getLength() );

            search( ast, offset, lineRegion, line, collector );
        }
        catch ( BadLocationException e ){
            // this is not a mission critical method, if it does not
            // work just ignore it 
        }
    }

    private void search( NesC12AST ast, int offset, IRegion lineRegion, String line, HyperlinkCollector collector ){
        offset -= lineRegion.getOffset();

        IRegion filename = filename( line, offset );
        if( filename == null )
            return;

        int includeEnd = RuleUtility.reverseWhitespace( filename.getOffset()-2, line );
        if( includeEnd <= 0 )
            return;

        int includeBegin = RuleUtility.reverseNonWhitespace( includeEnd, line );
        if( includeBegin < 0 )
            return;

        String sub = line.substring( includeBegin, includeEnd+1 );
        if( sub.equals( "#include" )){
            found( ast, lineRegion, filename, line, collector );
        }
        else if( sub.equals( "include" )){
            int sharpEnd = RuleUtility.reverseWhitespace( includeBegin-1, line );
            if( sharpEnd < 0 )
                return;

            int sharpBegin = RuleUtility.reverseNonWhitespace( sharpEnd, line );
            if( sharpBegin != sharpEnd )
                return;

            if( line.charAt( sharpBegin ) != '#' )
                return;

            found( ast, lineRegion, filename, line, collector );
        }

    }    

    private void found( NesC12AST ast, IRegion lineRegion, IRegion filename, String line, HyperlinkCollector collector ){
        String name = line.substring( filename.getOffset(), filename.getOffset()+filename.getLength() );
        IncludeProvider provider = collector.getIncludeProvider();
        if( provider == null )
            return;
        
        IncludeFile file;
        if( systemFile ){
            file = provider.searchSystemFile( name, null );
        }
        else{
            file = provider.searchUserFile( name, null );
        }
        if( file == null )
            return;
        
        IParseFile parseFile = ((NesC12FileInfo)file.getFile()).getParseFile();
        if( parseFile == null )
            return;
        
        FileHyperlink hyperlink = new FileHyperlink( 
                new FileRegion( ast.getParseFile(), lineRegion.getOffset() + filename.getOffset(), filename.getLength(), -1 ), parseFile );
        
        collector.add( hyperlink );
    }

    private IRegion filename( String line, int offset ){
        int left = -1;
        int right = -1;

        for( int i = offset-1; i >= 0; i-- ){
            if( line.charAt( i ) == '<' ){
                systemFile = true;
                left = i+1;
                break;
            }
            if( line.charAt( i ) == '"' ){
                systemFile = false;
                left = i+1;
                break;
            }
        }

        if( left == -1 )
            return null;

        for( int i = offset, n = line.length(); i<n; i++ ){
            if( systemFile && line.charAt( i ) == '>' ){
                right = i-1;
                break;
            }
            else if( !systemFile && line.charAt( i ) == '"' ){
                right = i-1;
                break;
            }
        }

        if( right == -1 || right <= left )
            return null;

        return new Region( left, right - left + 1 );
    }
}
