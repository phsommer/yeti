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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTModelConnectionFilter;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AttributeList;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.preprocessor.comment.NesCDocComment;

/**
 * A {@link ModelNode} is an implementation of an {@link IASTModelNode}. 
 * {@link ModelNode}s get connected through the {@link NodeStack}, clients
 * should not connect them directly.
 * @author Benjamin Sigg
 */
public abstract class ModelNode implements IASTModelNode{
    public static final IGenericFactory<ModelNode> FACTORY = new IGenericFactory<ModelNode>(){
        public ModelNode create(){
            return null;
        }
        
        public void write( ModelNode value, IStorage storage ) throws IOException{
            DataOutputStream out = storage.out();
            
            // children
            int size = value.children == null ? 0 : value.children.size();
            out.writeInt( size );
            if( size > 0 ){
                for( ModelConnection connection : value.children ){
                    storage.write( connection );
                }
            }
            
            // id, label
            storage.writeString( value.identifier );
            storage.writeString( value.label );
            
            // parse file
            storage.write( value.parseFile );
            
            // path
            storage.write( value.path );
            
            // tags
            storage.write( value.tags );
            
            // attributes
            storage.write( value.attributes );
            
            // documentation
            storage.writeString( value.documentation );
            
            // location
            size = value.regions == null ? 0 : value.regions.size();
            out.writeInt( size );
            if( size > 0 ){
                for( FileRegion region : value.regions ){
                    storage.write( region, FileRegion.FACTORY );
                }
            }
            
            // content
            storage.write( value.content );
        }
        
        public ModelNode read( ModelNode value, IStorage storage ) throws IOException{
            DataInputStream in = storage.in();
            
            // children
            int size = in.readInt();
            value.children = new ArrayList<ModelConnection>( size );
            for( int i = 0; i < size; i++ ){
                value.children.add( storage.<ModelConnection>read() );
            }
            
            // id, label
            value.identifier = storage.readString();
            value.label = storage.readString();
            
            // parse file
            value.parseFile = storage.read();
            
            // path
            value.path = storage.read();
            
            // tags
            value.tags = storage.read();
            
            // attributes
            value.attributes = storage.read();
            
            // documentation
            value.documentation = storage.readString();

            // location
            size = in.readInt();
            value.regions = new ArrayList<FileRegion>();
            for( int i = 0; i < size; i++ ){
                value.regions.add( storage.read( FileRegion.FACTORY ) );
            }
            
            // content
            value.content = storage.read();
            
            return value;
        }
    };
    
    private List<ModelConnection> children;
    private IASTFigureContent content;
    private String identifier;
    private String label;
    private IParseFile parseFile;
    private ASTModelPath path;
    private TagSet tags;
    private String documentation;
    private ModelAttribute[] attributes;
    
    private DeclarationResolver resolver;
    
    private List<FileRegion> regions = new ArrayList<FileRegion>();
    
    protected ModelNode(){
        // nothing
    }
    
    public ModelNode( String identifier, boolean leaf, Tag... tags ){
        if( !leaf )
            children = new ArrayList<ModelConnection>();
        
        this.identifier = identifier;
        if( tags != null && tags.length > 0 && tags[0] != null )
            this.tags = TagSet.get( tags );
        else
        	this.tags = TagSet.get();
    }
    
    public void setParent( ModelNode parent ){
        setParseFile( parent.getParseFile() );
        setPath( parent.getPath().getChild( this ) );
    }
    
    public void finishCreating(){
        resolveNameRanges();
        if( children != null ){
            for( ModelConnection child : children )
                child.finishCreating();
        }
    }
    
    /**
     * Called after the parser is finished. Should call 
     * {@link Name#resolveRange()} on all names.
     */
    protected abstract void resolveNameRanges();
    
    /**
     * Sets the resolver. The resolver is needed to find out, what various
     * {@link IDeclaration}s mean.
     * @param resolver the resolver
     */
    public void setDeclarationResolver( DeclarationResolver resolver ){
    	if( this.resolver != resolver ){
    		this.resolver = resolver;
    		if( children != null ){
    			for( ModelConnection child : children ){
    				child.setDeclarationResolver( resolver );
    			}
    		}
    	}
    }
    
    public DeclarationResolver getDeclarationResolver(){
        return resolver;
    }
    
    public ModelConnection addReference( ModelNode node, ASTNode ast ){
        return addReference( node, false, ast );
    }
    
    public ModelConnection addReference( ModelNode node, boolean omitWarnings, ASTNode ast ){
        ModelConnection connection = new StandardModelConnection( node.getIdentifier(), ast );
        if( omitWarnings ){
            TagSet tags = node.getTags();
            if( tags != null ){
                TagSet copy = tags.copy();
                copy.remove( NesC12ASTModel.ERROR );
                copy.remove( NesC12ASTModel.WARNING );
                connection.setTags( copy );
            }
        }
        else{
            connection.setTags( node.getTags() );
        }
        connection.setLabel( node.getLabel() );;
        connection.setReference( true );
        connection.addRegions( node.getRegions() );
        connection.setReferencedPath( node.getPath() );
        addChild( connection );
        return connection;
    }
    
    public ModelConnection addReferenceOnce( ModelNode node, boolean omitWarnings, ASTNode ast ){
        for( int i = 0, n = getConnectionCount(); i<n; i++ ){
            ModelConnection connection = getConnection( i );
            if( !connection.isReference() )
                continue;
            
            if( !connection.getIdentifier().equals( node.getIdentifier() ))
                continue;
            
            if( !connection.getTags().equals( node.getTags() ))
                continue;
            
            return connection;
        }
        
        return addReference( node, omitWarnings, ast );
    }
    
    public ModelConnection addChild( ModelNode node, ASTNode ast ){
        ModelConnection connection = new StandardModelConnection( node.getIdentifier(), ast );
        connection.setTags( node.getTags() );
        connection.setLabel( node.getLabel() );
        connection.setReference( false );
        connection.addRegions( node.getRegions() );
        addChild( connection );
        return connection;
    }
    
    public void addChild( ModelConnection child ){
        if( children == null )
            children = new ArrayList<ModelConnection>();
        
        children.add( child );
        child.setPath( getPath() );
        child.setParseFile( getParseFile() );
        child.setDeclarationResolver( getDeclarationResolver() );
    }
    
    public void removeConnections( IASTModelConnectionFilter filter ){
        if( children != null ){
            Iterator<ModelConnection> list = children.iterator();
            while( list.hasNext() ){
                if( filter.include( this, list.next() ))
                    list.remove();
            }
        }
    }
    
    public void setAttributes( ModelAttribute[] attributes ){
		this.attributes = attributes;
	}
    
    public void setAttributes( AttributeList list ){
    	setAttributes( list == null ? null : list.resolveModelAttributes() );
    }
    
    public ModelAttribute[] getAttributes(){
    	return attributes;
    }
    
    public int getConnectionCount(){
        return children.size();
    }
    
    public ModelConnection getConnection( int index ){
        return children.get( index );
    }
    
    /**
     * Returns the same as {@link #getChildren()} put as instance of
     * {@link ModelConnection}.
     * @return the children
     */
    public ModelConnection[] getConnections(){
        if( children == null )
            return null;
        
        return children.toArray( new ModelConnection[ children.size() ] );
    }
    
    public IASTModelNodeConnection[] getChildren(){
        if( children == null )
            return null;
        
        return children.toArray( new IASTModelNodeConnection[ children.size() ] );
    }
    
    public boolean putErrorFlag(){
        return getTags().add( NesC12ASTModel.ERROR );
    }
    
    public boolean putWarningFlag(){
        return getTags().add( NesC12ASTModel.WARNING );
    }
    
    public void setContent( IASTFigureContent content ){
        this.content = content;
    }

    public IASTFigureContent getContent(){
        return content;
    }

    public void setIdentifier( String identifier ){
        this.identifier = identifier;
        if( path != null ){
            setPath( path.getParent().getChild( this ) );
        }
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
        if( children != null ){
            for( ModelConnection child : children ){
                child.setPath( path );
            }
        }
    }
    
    public ASTModelPath getPath(){
        return path;
    }
    
    public IASTModelPath getLogicalPath(){
    	ASTModelPath path = getPath();
    	if( path == null )
    		return null;
    	
    	TagSet tags = getTags();
    	if( tags == null || !tags.contains( Tag.INCLUDED ))
    		return path;
    	
    	IFileRegion region = getRegion();
    	if( region == null )
    		return path;
    	
    	IParseFile file = region.getParseFile();
    	if( file == null )
    		return path;
    	
    	return path.replaceFile( file );
    }

    public IFileRegion getRegion(){
        if( regions.size() == 0 )
            return null;
        
        return regions.get( 0 );
    }

    public void addRegion( FileRegion region ){
        regions.add( region );
    }
    
    public FileRegion[] getRegions(){
        return regions.toArray( new FileRegion[ regions.size() ] );
    }

    public void setTags( TagSet tags ){
        this.tags = tags;
    }
    
    public TagSet getTags(){
        return tags;
    }
    
    public void setDocumentation( String documentation ){
		this.documentation = documentation;
	}
    
    public void setDocumentation( NesCDocComment[] comments ){
    	if( comments == null || comments.length == 0 )
    		documentation = null;
    	else{
    		documentation = comments[0].getComment();
    	}
    }
    
    public NesC12DocComment getDocumentation(){
	    if( documentation == null || path == null )
	    	return null;
	    return new NesC12DocComment( documentation, path );
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "node[" );
        builder.append( "id=" );
        builder.append( identifier );
        builder.append( ", tags=" );
        builder.append( tags );
        builder.append( ", label=" );
        builder.append( label );
        builder.append( "]" );
        return builder.toString();
    }
}
