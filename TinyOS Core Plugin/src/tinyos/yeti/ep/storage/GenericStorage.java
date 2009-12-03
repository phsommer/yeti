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
package tinyos.yeti.ep.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.nesc.util.ConfigurationElementContent;
import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.ep.figures.BlockContent;
import tinyos.yeti.ep.figures.ConfigurationContent;
import tinyos.yeti.ep.figures.GraphContent;
import tinyos.yeti.ep.figures.InterfaceContent;
import tinyos.yeti.ep.figures.LabelContent;
import tinyos.yeti.ep.figures.LazyContent;
import tinyos.yeti.ep.figures.ModuleContent;
import tinyos.yeti.ep.parser.EmptyTagSet;
import tinyos.yeti.ep.parser.IASTModelAttribute;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.reference.ASTReference;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.model.missing.IMissingResource;
import tinyos.yeti.model.missing.MissingDeclaration;
import tinyos.yeti.model.missing.MissingSystemFile;
import tinyos.yeti.model.missing.MissingUserFile;
import tinyos.yeti.model.standard.StandardFileModel;
import tinyos.yeti.model.standard.StandardFileModel.StandardParseFile;
import tinyos.yeti.utility.Icon;

/**
 * The standard implementation of {@link IStorage} uses {@link IGenericFactory}s
 * to store at least these kind of objects:<br>
 * <ul>
 *      <li>IParseFile</li>
 *      <li>ASTModelPath</li>
 *      <li>TagSet</li>
 *      <li>IASTFigureContent</li>
 * </ul><br>
 * <b>Note: </b> each new type requires an additional factory, use {@link #put(String, Class, IGenericFactory)}
 * to add more factories
 * @author Benjamin Sigg
 *
 */
public class GenericStorage implements IStorage{
	private static final int VERSION = 5;
	
    private Map<String, IGenericFactory<?>> factories = new HashMap<String, IGenericFactory<?>>();
    private Map<Class<?>, String> classIdentifiers = new HashMap<Class<?>, String>();
    
    private DataInputStream in;
    private DataOutputStream out;
    
    private ProjectTOS project;
    
    private IProgressMonitor monitor;
    
    public GenericStorage( ProjectTOS project, DataInputStream in, IProgressMonitor monitor ) throws IOException{
        this.project = project;
        this.in = in;
        this.monitor = monitor;
        
        if( in.readInt() != 47 )
        	throw new IOException( "file format outdated, ignore and rebuild file" );
        
        int version = in.readInt();
        
        if( version < VERSION )
        	throw new IOException( "file format outdated, ignore and rebuild file" );
        
        if( version > VERSION )
        	throw new IOException( "file from the future, ignore and rebuild file" );
        
        createFactories();
    }
    
    public GenericStorage( ProjectTOS project, DataOutputStream out, IProgressMonitor monitor ) throws IOException{
        this.project = project;
        this.out = out;
        this.monitor = monitor;
       
        // most often used number in the universe
        out.writeInt( 47 );
        
        // version
        out.writeInt( VERSION );
        
        createFactories();
    }
    
    public IProgressMonitor getMonitor(){
        return monitor;
    }
    
    protected void createFactories(){
        put( "tinyOS.model.standard.StandardFileModel.StandardParseFile", StandardParseFile.class, StandardFileModel.PARSE_FILE_FACTORY );
        put( "tinyOS.ep.NullParseFile", NullParseFile.class, NullParseFile.FACTORY );
        
        put( "tinyOS.ep.parser.standard.ASTModelPath", ASTModelPath.class, ASTModelPath.FACTORY );
        
        put( "tinyOS.ep.parser.TagSet", TagSet.class, createTagsetFactory() );
        put( "tinyOS.ep.parser.EmptyTagSet", EmptyTagSet.class, EmptyTagSet.FACTORY );
        
        put( "tinyos.yeti.utility.Icon", Icon.class, Icon.FACTORY );
        
        put( "tinyOS.ep.parser.ASTReference", ASTReference.class, ASTReference.FACTORY );
        put( "tinyOS.ep.parser.IASTReference[]", IASTReference[].class, IASTReference.ARRAY_FACTORY );
        
        put( "tinyos.yeti.ep.parser.IASTModelAttribute[]", IASTModelAttribute[].class, IASTModelAttribute.ARRAY_FACTORY );
        
        put( "tinyOS.ep.figures.BlockContent", BlockContent.class, BlockContent.FACTORY );
            put( "tinyOS.ep.figures.InterfaceContent", InterfaceContent.class, InterfaceContent.FACTORY );
            put( "tinyOS.ep.figures.ModuleContent", ModuleContent.class, ModuleContent.FACTORY );
        put( "tinyOS.ep.figures.GraphContent", GraphContent.class, GraphContent.FACTORY );
            put( "tinyOS.ep.figures.ConfigurationContent", ConfigurationContent.class, ConfigurationContent.FACTORY );
                put( "tinyOS.editors.nesc.util.ConfigurationElementContent", ConfigurationElementContent.class, ConfigurationElementContent.FACTORY );
        put( "tinyOS.ep.figures.LabelContent", LabelContent.class, LabelContent.FACTORY );
        put( "tinyOS.ep.figures.LazyContent", LazyContent.class, LazyContent.FACTORY );
        
        put( "tinyOS.ep.parser.IDeclaration.Kind", IDeclaration.Kind.class, IDeclaration.FACTORY_KIND );
        
        put( "tinyOS.model.missing.IMissingResource[]", IMissingResource[].class, IMissingResource.ARRAY_FACTORY );
            put( "tinyOS.model.missing.MissingSystemFile", MissingSystemFile.class, MissingSystemFile.FACTORY );
            put( "tinyOS.model.missing.MissingUserFile", MissingUserFile.class, MissingUserFile.FACTORY );
            put( "tinyOS.model.missing.MissingDeclaration", MissingDeclaration.class, MissingDeclaration.FACTORY );
    }
    
    protected IGenericFactory<TagSet> createTagsetFactory(){
        return new TagSetFactory();
    }
    
    public ProjectTOS getProject(){
        return project;
    }
    
    public DataInputStream in(){
        return in;
    }
    
    public DataOutputStream out(){
        return out;
    }
    
    public <V> void put( String uniqueIdentifier, Class<V> clazz, IGenericFactory<V> factory ){
        factories.put( uniqueIdentifier, factory );
        classIdentifiers.put( clazz, uniqueIdentifier );
    }
    
    @SuppressWarnings("unchecked")
    public <V> void write( V value ) throws IOException{
        if( value == null ){
            out.writeBoolean( false );
        }
        else{
            out.writeBoolean( true );
            String name = classIdentifiers.get( value.getClass() );
            if( name == null )
                throw new IOException( "no mapping for class: " + value.getClass() );
            
            IGenericFactory<V> factory = (tinyos.yeti.ep.storage.IGenericFactory<V> )factories.get( name );
            if( factory == null )
                throw new IOException( "unknown kind of object: " + value.getClass() );
            
            out.writeUTF( name );
            factory.write( value, this );
        }
    }
    
    public <V> void write( V value, IGenericFactory<V> factory ) throws IOException{
        if( value == null ){
            out.writeBoolean( false );
        }
        else{
            out.writeBoolean( true );
            factory.write( value, this );
        }
    }
    
    @SuppressWarnings("unchecked")
    public <V> V read() throws IOException{
        if( in.readBoolean() ){
            String name = in.readUTF();
            IGenericFactory<V> factory = (tinyos.yeti.ep.storage.IGenericFactory<V> )factories.get( name );
            if( factory == null )
                throw new IOException( "unknown kind of object: " + name );

            V value = factory.create();
            value = factory.read( value, this );
            if( value == null )
                throw new IOException( "can't create value of kind: " + name );
            return value;
        }
        return null;
    }
    
    public <V> V read( IGenericFactory<V> factory ) throws IOException{
        if( in.readBoolean() ){
            V value = factory.create();
            value = factory.read( value, this );
            if( value == null )
                throw new IOException( "can't create value" );
            return value;
        }
        return null;
    }
    
    public void writeString( String value ) throws IOException{
        if( value == null ){
            out.writeBoolean( false );
        }
        else{
            out.writeBoolean( true );
            out.writeUTF( value );
        }
    }
    
    public String readString() throws IOException{
        if( in.readBoolean() ){
            return in.readUTF();
        }
        return null;
    }
}
