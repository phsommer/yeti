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
package tinyos.yeti.editors.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import tinyos.yeti.editors.INesCEditorParserClient;
import tinyos.yeti.editors.NesCDocumentMap;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.parser.IFileHyperlink;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.ep.parser.INesCParser;

public class NesCHyperlinkDetector extends AbstractHyperlinkDetector implements IHyperlinkDetector, INesCEditorParserClient{
    private boolean setup = false;
    
    private NesCEditor editor;
    private INesCAST ast;
    
    public IHyperlink[] detectHyperlinks( ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks ){
        ensureSetup();
        if( ast != null ){
            IFileHyperlink[] links = ast.getHyperlinks( new DocumentRegion( textViewer, region, new NesCDocumentMap( editor.getDocument() ) ));
            if( links != null ){
                IHyperlink[] result = new IHyperlink[ links.length ];
                for( int i = 0, n = result.length; i<n; i++ )
                    result[i] = new NesCHyperlink( links[i] );
                
                return result;
            }
        }
        
        return null;
    }
    
    private void ensureSetup(){
        if( setup )
            return;
        
        setup = true;
        editor = getEditor();
        
        editor.addParserClient( this );
        ast = editor.getAST();
    }

    public void setupParser( NesCEditor editor, INesCParser parser ){
        // nothing to do
    }
    
    public void closeParser( NesCEditor editor, boolean successful, INesCParser parser ){
        INesCAST ast = parser.getAST();
        if( ast != null || this.ast == null )
            this.ast = ast;
    }
    
    private NesCEditor getEditor(){
        return (NesCEditor)getAdapter( NesCEditor.class );
    }
    
    @Override
    public void dispose(){
        if( editor != null ){
            editor.removeParserClient( this );
        }
        super.dispose();
    }
}
