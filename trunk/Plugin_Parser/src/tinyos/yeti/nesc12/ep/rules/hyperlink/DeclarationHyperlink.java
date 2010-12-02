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

import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.FileHyperlink;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

/**
 * A HyperlinkRule that follows an {@link IDeclaration}.
 * @author Benjamin Sigg
 *
 */
public abstract class DeclarationHyperlink implements IHyperlinkRule{
    private Kind[] kinds;
    
    /**
     * Creates a new rule.
     * @param kinds the kind of declarations this rule should discover
     */
    public DeclarationHyperlink( Kind... kinds ){
        this.kinds = kinds;
    }
    
    protected void setKinds( Kind[] kinds ){
        this.kinds = kinds;
    }
    
    /**
     * Tells whether the node <code>node</code> is a valid selection and whether
     * this link should search a declaration for <code>node</code>
     * @param node some identifier
     * @return <code>true</code> if this node can be the source of a hyperlinks
     */
    protected abstract boolean valid( Identifier node );

    public void search( NesC12AST ast, HyperlinkCollector collector ){
        DeclarationResolver resolver = collector.getDeclarationResolver();
        if( resolver == null )
            return;

        INesC12Location location = ast.getOffsetInput( collector.getLocation().getRegion().getOffset() );
        ASTNode node = RuleUtility.nodeAt( location, ast.getRoot() );

        if( !(node instanceof Identifier ))
            return;
        
        Identifier id = (Identifier)node;
        if( !valid( id ))
            return;
        
        IDeclaration declaration = resolver.resolve( id.getName(), null, kinds );
        if( declaration == null )
            return;

        IFileRegion sourceRegion = ast.getRegion( node );
        if( sourceRegion == null )
            return;

        IFileRegion targetRegion = ((BaseDeclaration)declaration).getFileRegion();
        if( targetRegion == null ){
        	if( declaration.getParseFile() != null && declaration.getParseFile() != NullParseFile.NULL ){
        		collector.add( new FileHyperlink( sourceRegion, declaration.getParseFile() ) );
        	}
        }
        else{
            collector.add( new FileHyperlink( sourceRegion, targetRegion ) );
        }
    }
}
