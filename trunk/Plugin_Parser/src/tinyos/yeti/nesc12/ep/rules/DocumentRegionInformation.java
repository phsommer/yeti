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
package tinyos.yeti.nesc12.ep.rules;

import tinyos.yeti.ep.parser.IDocumentRegion;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.preprocessor.IncludeProvider;

public class DocumentRegionInformation{
    private IDocumentRegion location;
    private DeclarationResolver declarationResolver;
    private IncludeProvider includeProvider;
    
    private INesC12Location astLocation;
    private ASTNode node;
    private IFileRegion sourceRegion;
    
    public DocumentRegionInformation( NesC12AST ast, IDocumentRegion location, DeclarationResolver declarationResolver, IncludeProvider includeProvider ){
        this.location = location;
        this.declarationResolver = declarationResolver;
        this.includeProvider = includeProvider;
        
        astLocation = ast.getOffsetInput( location.getRegion().getOffset() );
        node = RuleUtility.nodeAt( astLocation, ast.getRoot() );
        if( node != null )
            sourceRegion = ast.getRegion( node );
    }
    
    /**
     * Gets the region that is occupied by {@link #getNode()}.
     * @return the region
     */
    public IFileRegion getSourceRegion(){
        return sourceRegion;
    }
    
    /**
     * Gets the leaf node which is selected
     * @return
     */
    public ASTNode getNode(){
        return node;
    }
    
    /**
     * Gets the current position of the mouse
     * @return the position
     */
    public INesC12Location getOffset(){
        return astLocation;
    }
    
    /**
     * Gets the location where the user currently is in the document and
     * tries to open a link.
     * @return the source location of the link
     */
    public IDocumentRegion getLocation(){
        return location;
    }
    
    /**
     * Gets a resolver to find elements outside the file.
     * @return the resolver, might be <code>null</code>
     */
    public DeclarationResolver getDeclarationResolver(){
        return declarationResolver;
    }
    
    /**
     * Gets a provider to find other files.
     * @return the provider, might be <code>null</code>
     */
    public IncludeProvider getIncludeProvider(){
        return includeProvider;
    }
}
