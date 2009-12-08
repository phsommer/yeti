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
package tinyos.yeti.ep.parser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.storage.GenericStorage;
import tinyos.yeti.ep.storage.IStorage;

/**
 * A factory that creates new {@link INesCParser}s and objects that support
 * this parser.
 * @author Benjamin Sigg
 */
public interface INesCParserFactory {
    /**
     * Gets a list of possible views of the {@link IASTModel} that will
     * be created by this factory.
     * @return the list of possible views.
     */
    public ASTView[] getViews();

    /**
     * Gets an image that best describes the nodes that have the
     * tags <code>tags</code>.
     * @param tags the tags that describe the image, can be <code>null</code>
     * or an invalid combination
     * @return the image or <code>null</code>
     */
    public ImageDescriptor getImageFor( TagSet tags );
    
    /**
     * Gets the set of {@link Tag}s that is actually used by the parser
     * and model that are created by this factory.
     * @return the set of used tags
     */
    public TagSet getSupportedTags();
    
    /**
     * Gets tags which decorate an icon.
     * @return the decorating tags
     */
    public TagSet getDecoratingTags();
    
    /**
     * Gets a new comparator that can compare {@link TagSet}s. The comparator
     * will be used to order the elements on the outline view and maybe elsewhere.
     * The comparator created by this method does not have to be very sophisticated,
     * it suffices if the comparator handles the most common cases just to group
     * together the most important elements (for example all specifications to
     * the top).
     * @return the new comparator
     */
    public Comparator<TagSet> createComparator();

    /**
     * Creates new generic storage for reading some data from the disk.
     * @param project the project to work for
     * @param in the input stream to be used in the storage
     * @param monitor a monitor that will be used by the storage to cancel
     * its operations
     * @return the new storage
     * @see GenericStorage
     */
    public IStorage createStorage( ProjectTOS project, DataInputStream in, IProgressMonitor monitor ) throws IOException;
    
    /**
     * Creates a new generic storage for writing some data to the disk.
     * @param project the project to work for
     * @param out the output stream to be used by the storage
     * @param monitor a monitor that will be used by the storage to cancel
     * its operations
     * @return the new storage
     * @see GenericStorage
     */
    public IStorage createStorage( ProjectTOS project, DataOutputStream out, IProgressMonitor monitor ) throws IOException;
    
    /**
     * A factory that can read and write {@link IDeclaration}s to files.
     * @return the factory or <code>null</code>
     */
    public IDeclarationFactory getDeclarationFactory();
    
    /**
     * A factory that can read and write {@link IASTModelNode}s to files.
     * @return the factory or <code>null</code>
     */
    public IASTModelNodeFactory getModelNodeFactory();
    
    /**
     * Creates a new initializer that will be used during initialization of
     * <code>project</code>.
     * @param project the project that will be initialized
     * @return the new initializer for <code>project</code>
     */
    public INesCInitializer createInitializer( IProject project );
    
    /**
     * Creates a new parser that will parse the file <code>resource</code>.
     * @param project the project for which the file will be parsed, can be <code>null</code>
     * @return the new parser
     */
    public INesCParser createParser( IProject project );
    
    /**
     * Creates a new collector which is used to collect definitions for
     * the parser.
     * @param project the project for which the collector will be used
     * @param parseFile the file that will be handled by the collector
     * @return the new collector, can be <code>null</code>
     */
    public INesCDefinitionCollector createCollector( ProjectTOS project, IParseFile parseFile );
    
    /**
     * Creates a new empty model for a given project. There is no need to populate
     * the model, that will be done by the core plugin.
     * @return the project for which the model will be used, can be <code>null</code>
     */
    public IASTModel createModel( IProject project );
    
    /**
     * Gets a new or a cached instance of a path that describes an imaginary
     * root of all {@link IASTModelPath}s.
     * @param project the project for which the path is needed, can be <code>null</code>
     * @return the path
     */
    public IASTModelPath createRoot( IProject project );
    
    /**
     * Tries to find some completion proposals for the given location. This
     * method is called if the parser could not create a {@link INesCAST}. Parsers
     * which always create an ast, do not have to implement this method.
     * @param location the location where to proposals will be applied
     * @return the proposals or <code>null</code>
     */
    public INesCCompletionProposal[] getProposals( ProposalLocation location );
    
    /**
     * Creates a declaration that represents a typedef with type <code>type</code>
     * and name <code>name</code>. The result of this method will be given to
     * the parser through {@link INesCParser#addDeclarations(IDeclaration[])}.
     * The result of this method will never be read by the core plugin, so
     * what's actually inside the declaration is not interesting.
     * @param type the type of the definition
     * @param name the name of the definition
     * @param typedefs other typedefs created by this method that might influence the parsing
     * @return a declaration for the typedef
     * @throws IllegalArgumentException if the type cannot be parsed 
     */
    public IDeclaration toBasicType( String type, String name, IDeclaration[] typedefs );
}
