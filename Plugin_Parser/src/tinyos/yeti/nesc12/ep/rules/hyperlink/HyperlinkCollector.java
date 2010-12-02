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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.parser.IDocumentRegion;
import tinyos.yeti.ep.parser.IFileHyperlink;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.DocumentRegionInformation;
import tinyos.yeti.preprocessor.IncludeProvider;

/**
 * Used by {@link IHyperlinkRule}s to collect hyperlinks. 
 * @author Benjamin Sigg
 */
public class HyperlinkCollector extends DocumentRegionInformation{
    private List<IFileHyperlink> links = new ArrayList<IFileHyperlink>();
    
    public HyperlinkCollector( NesC12AST ast, IDocumentRegion location, DeclarationResolver declarationResolver, IncludeProvider includeProvider ){
    	super( ast, location, declarationResolver, includeProvider );
    }
        
    /**
     * Stores a new link in this collector
     * @param hyperlink the new link
     */
    public void add( IFileHyperlink hyperlink ){
        links.add( hyperlink );
    }
    
    public IFileHyperlink[] getHyperlinks(){
        if( links.isEmpty() )
            return null;
        
        return links.toArray( new IFileHyperlink[ links.size() ] );
    }
}
