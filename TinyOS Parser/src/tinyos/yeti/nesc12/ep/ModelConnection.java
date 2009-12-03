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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelAttribute;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCAttribute;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

/**
 * A {@link ModelConnection} connects two {@link ModelNode}s. Connections
 * should not be added directly to its parent {@link ModelNode}, they
 * should be added through a {@link NodeStack}.
 * @author Benjamin Sigg
 *
 */
public abstract class ModelConnection implements IASTModelNodeConnection{
    public static final IGenericFactory<ModelConnection> FACTORY = new IGenericFactory<ModelConnection>(){
        public ModelConnection create(){
            return null;
        }
        
        public void write( ModelConnection value, IStorage storage ) throws IOException{
            // id, label
            storage.writeString( value.identifier );
            storage.writeString( value.label );
            
            // parse file
            storage.write( value.parseFile );
            
            // path
            storage.write( value.path );
            
            // reference, referenced
            storage.out().writeBoolean( value.reference );
            storage.write( value.referenced );
            
            // tags
            storage.write( value.tags );
            
            // attributes
            storage.write( value.attributes );
            
            // regions
            int size = value.regions == null ? 0 : value.regions.size();
            storage.out().writeInt( size );
            if( size > 0 ){
                for( FileRegion region : value.regions ){
                    storage.write( region, FileRegion.FACTORY );
                }
            }
        }
        
        public ModelConnection read( ModelConnection value, IStorage storage ) throws IOException{
            // id, label
            value.identifier = storage.readString();
            value.label = storage.readString();
            
            // parse file
            value.parseFile = storage.read();
            
            // path
            value.path = storage.read();
            
            // reference, referenced
            value.reference = storage.in().readBoolean();
            value.referenced = storage.read();
            
            // tags
            value.tags = storage.read();
            
            // attributes
            value.attributes = storage.read();
            
            // regions
            int size = storage.in().readInt();
            if( size > 0 ){
                value.regions = new ArrayList<FileRegion>( size );
                for( int i = 0; i < size; i++ ){
                    value.regions.add( storage.read( FileRegion.FACTORY ) );
                }
            }
            
            return value;
        }
    };
    
    private String identifier;
    private String label;
    private IParseFile parseFile;
    private ASTModelPath path;
    private ASTModelPath referenced;
    private List<FileRegion> regions = new ArrayList<FileRegion>();
    private TagSet tags;
    private boolean reference;
    private ASTNode ast;
    private IASTModelAttribute[] attributes;
    
    private DeclarationResolver declarationResolver;
    
    protected ModelConnection(){
    	// nothing
    }
    
    public ModelConnection( String identifier, ASTNode node ){
        this.identifier = identifier;
        this.ast = node;
    }
    
    public void finishCreating(){
        resolveNameRanges();
        ast = null;
    }
    
    /**
     * Gets the {@link ASTNode} which created this connection, this property
     * is <code>null</code> as soon as the parser finished its resolve-phase.
     * @return the node, can be <code>null</code>
     */
    public ASTNode getAST(){
		return ast;
	}
    
    /**
     * Called after the parser is finished. Should call 
     * {@link Name#resolveRange()} on all names.
     */
    protected abstract void resolveNameRanges();
    
    
    public void setDeclarationResolver( DeclarationResolver declarationResolver ){
        this.declarationResolver = declarationResolver;
    }
    
    public DeclarationResolver getDeclarationResolver(){
        return declarationResolver;
    }
    
    public void setIdentifier( String identifier ){
        this.identifier = identifier;
    }
    
    public String getIdentifier(){
        return identifier;
    }

    public void setLabel( String label ){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }

    public void setParseFile( IParseFile parseFile ){
        this.parseFile = parseFile;
    }
    
    public IParseFile getParseFile(){
        return parseFile;
    }

    public void setPath( ASTModelPath path ){
        this.path = path;
    }
    
    public ASTModelPath getPath(){
        return path;
    }
    
    public void setAttributes( IASTModelAttribute[] attributes ){
		this.attributes = attributes;
	}
    
    public void setAttributes( NesCAttribute[] attributes ){
    	if( attributes == null )
    		this.attributes = null;
    	else{
    		List<IASTModelAttribute> list = new ArrayList<IASTModelAttribute>( attributes.length );
    		
    		for( int i = 0; i < attributes.length; i++ ){
    			String name = attributes[i].getAttributeName();
    			if( name != null ){
    				list.add( new ModelAttribute( name ) );
    			}
    		}
    		
    		this.attributes = list.toArray( new IASTModelAttribute[ list.size() ] );
    	}
    }
    
    public IASTModelAttribute[] getAttributes(){
	    return attributes;
    }

    public IFileRegion getRegion(){
        if( regions.size() == 0 )
            return null;
        
        return regions.get( 0 );
    }

    public void addRegion( FileRegion region ){
        regions.add( region );
    }
    
    public void addRegions( FileRegion[] regions ){
        for( FileRegion region : regions )
            this.regions.add( region );
    }
    
    public IFileRegion[] getRegions(){
        return regions.toArray( new IFileRegion[ regions.size() ] );
    }

    public void setTags( TagSet tags ){
        this.tags = tags;
    }
    
    public TagSet getTags(){
        return tags;
    }

    public void setReference( boolean reference ){
        this.reference = reference;
    }
    
    public boolean isReference(){
        return reference;
    }

    public void setReferencedPath( ASTModelPath referenced ){
        this.referenced = referenced;
    }
    
    public IASTModelPath getReferencedPath(){
        return referenced;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "connection[" );
        builder.append( "id=" );
        builder.append( identifier );
        builder.append( ", reference=" );
        builder.append( reference );
        builder.append( ", tags=" );
        builder.append( tags );
        builder.append( ", label=" );
        builder.append( label );
        builder.append( "]" );
        return builder.toString();
    }
}
