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

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDocumentRegion;
import tinyos.yeti.ep.parser.IFileHyperlink;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IHoverInformation;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.ep.parser.INesCCompletionProposal;
import tinyos.yeti.ep.parser.ProposalLocation;
import tinyos.yeti.nesc12.ep.rules.DocumentRegionInformation;
import tinyos.yeti.nesc12.ep.rules.hover.IHoverInformationRule;
import tinyos.yeti.nesc12.ep.rules.hyperlink.HyperlinkCollector;
import tinyos.yeti.nesc12.ep.rules.hyperlink.IHyperlinkRule;
import tinyos.yeti.nesc12.ep.rules.proposals.IProposalRule;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.meta.GenericRangedCollection;
import tinyos.yeti.nesc12.parser.meta.NamedType;
import tinyos.yeti.nesc12.parser.meta.QuickLinks;
import tinyos.yeti.nesc12.parser.meta.RangedCollection;
import tinyos.yeti.preprocessor.IncludeProvider;
import tinyos.yeti.preprocessor.PreprocessorReader;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos_parser.NesC12ParserPlugin;

public class NesC12AST implements INesCAST {
    private IParseFile parseFile;
    private ASTNode root;
    private PreprocessorReader reader;
    
    private DeclarationResolver resolver;
    private IncludeProvider provider;
    private BindingResolver bindingResolver = new StandardBindingResolver();
    
    private RangedCollection<NamedType> typedefs;
    private GenericRangedCollection ranges;
    private QuickLinks macroLinks;
    
    public NesC12AST( 
            IParseFile parseFile, 
            ASTNode root,
            PreprocessorReader reader, 
            RangedCollection<NamedType> typedefs,
            GenericRangedCollection ranges,
            QuickLinks macroLinks ){
        this.parseFile = parseFile;
        this.root = root;
        this.reader = reader;
        this.typedefs = typedefs;
        this.ranges = ranges;
        this.macroLinks = macroLinks;
    }
    
    public RangedCollection<NamedType> getTypedefs(){
        return typedefs;
    }
    
    public GenericRangedCollection getRanges(){
        return ranges;
    }
    
    public QuickLinks getMacroLinks(){
		return macroLinks;
	}
    
    public void setResolver( DeclarationResolver resolver ){
        this.resolver = resolver;
    }
    
    public DeclarationResolver getResolver(){
        return resolver;
    }
    
    public void setProvider( IncludeProvider provider ){
        this.provider = provider;
    }
    
    public BindingResolver getBindingResolver(){
        return bindingResolver;
    }
    
    public IncludeProvider getProvider(){
        return provider;
    }

    public IParseFile getParseFile() {
        return parseFile;
    }
    
    public ASTNode getRoot(){
        return root;
    }
    
    public INesC12Location getOffsetInput( final int inputOffset ){
        return new INesC12Location(){
            private boolean resolved = false;
            private int outputOffset;
            
            public int getInputfileOffset(){
                return inputOffset;
            }
            
            public int getPreprocessedOffset(){
                if( !resolved ){
                    resolved = true;
                    outputOffset = reader.getPreprocessedOffset( inputOffset );
                }
                return outputOffset;
            }
        };
    }
    
    public INesC12Location getOffsetAtBegin( ASTNode node ){
        return getOffsetPreprocessed( node.getRange().getLeft() );
    }
    
    public INesC12Location getOffsetAtEnd( ASTNode node ){
        return getOffsetPreprocessed( node.getRange().getRight() );
    }
    
    public INesC12Location getOffsetPreprocessed( final int outputOffset ){
        return new INesC12Location(){
            private int inputOffset = -1;
            
            public int getInputfileOffset(){
                if( inputOffset == -1 ){
                    inputOffset = 0;
                    RangeDescription range = reader.range( outputOffset, outputOffset, true );
                    for( int i = 0, n = range.getRootCount(); i<n; i++ ){
                    	inputOffset = Math.max( inputOffset, range.getRoot( i ).right() );
                    }
                }
                
                return inputOffset;
            }
            public int getPreprocessedOffset(){
                return outputOffset;
            }
        };
    }
    
    public IFileRegion getRegion( ASTNode node ){
        RangeDescription range = reader.range( node.getRange().getLeft(), node.getRange().getRight(), true );
        if( range.getRootCount() == 0 )
            return null;
        
        return new FileRegion( range.getRoot( 0 ));
    }
    
    public IFileHyperlink[] getHyperlinks( IDocumentRegion location ){
        HyperlinkCollector collector = new HyperlinkCollector( this, location, resolver, provider );
        
        IHyperlinkRule[] rules = NesC12ParserPlugin.getDefault().getHyperlinkRules();
        for( IHyperlinkRule rule : rules ){
            rule.search( this, collector );
        }
        
        return collector.getHyperlinks();
    }
    
    public IHoverInformation getHoverInformation( IDocumentRegion location ){
    	DocumentRegionInformation region = new DocumentRegionInformation( this, location, resolver, provider );
    	IHoverInformationRule[] rules = NesC12ParserPlugin.getDefault().getHoverRules();
    	for( IHoverInformationRule rule : rules ){
    		IHoverInformation result = rule.getInformation( this, region );
    		if( result != null )
    			return result;
    	}
    	return null;
    }

    public INesCCompletionProposal[] getProposals( ProposalLocation location ) {
    	CompletionProposalCollector collector = new CompletionProposalCollector( location );
    	 
    	IProposalRule[] rules = NesC12ParserPlugin.getDefault().getProposalRules();
    	for( IProposalRule rule : rules ){
    	    rule.propose( this, collector );
    	}
    	
    	return collector.getProposals();
    }
}
