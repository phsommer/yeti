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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.nesc.IMultiReader;

/**
 * A parser that can parse one nesc-file.
 * @author Benjamin Sigg
 */
public interface INesCParser {
    /**
     * Whether to follow include directives or not.
     * @param follow <code>true</code> if includes should be resolved, <code>false</code>
     * if they should be ignored
     */
    public void setFollowIncludes( boolean follow );
    
    /**
     * Informs this parser about the name of the file that will be parsed. The
     * argument <code>name</code> should be the result of {@link IASTModelNode#getParseFile()}
     * for all {@link IASTModelNode}s that will be created in the next run of this
     * parser.
     * @param file the file, might be <code>null</code>
     */
    public void setParseFile( IParseFile file );
    
    /**
     * Sets whether this parser should create warnings and errors or not.
     * @param create <code>true</code> if output is required, <code>false</code>
     * otherwise
     * @see #getMessages()
     */
    public void setCreateMessages( boolean create );
    
    /**
     * Gets a list of messages that were found during parsing.
     * @return the list of messages
     * @see #setCreateMessages(boolean)
     */
    public IMessage[] getMessages();
    
    /**
     * Tells this parser whether {@link IDeclaration}s should be created
     * and collected for later use.
     * @param create <code>true</code> if declarations should be created
     */
    public void setCreateDeclarations( boolean create );
    
    /**
     * Gets all the global declarations (= visible from another file) that
     * were found. These declarations will later be given to another parser
     * that needs to parse another file.
     * @return the new declarations
     * @see #setCreateDeclarations(boolean)
     */
    public IDeclaration[] getDeclarations();
    
    /**
     * Adds an additional set of declarations to this parser. The declarations
     * were created by another parser which had the same {@link INesCParserFactory}
     * as this one.
     * @param declarations the additional declarations, for parsing only. These
     * declarations must not be returned by {@link #getDeclarations()}.
     */
    public void addDeclarations( IDeclaration[] declarations );
    
    /**
     * Adds a macro that should be processed by the preprocessor.
     * @param macro the additional macro
     */
    public void addMacro( IMacro macro );
    
    /**
     * Sets the model into which the parser should store its output.
     * @param model the model to store the output in. The model was
     * created by the {@link INesCParserFactory} that created this parser. 
     * Can be <code>null</code> to indicate that the output of this parser
     * will be used in another way
     */
    public void setASTModel( IASTModel model );
    
    /**
     * Gets the model which was set using {@link #setASTModel(IASTModel)}.
     * @return the argument of the last call of {@link #setASTModel(IASTModel)}
     */
    public IASTModel getASTModel();
    
    /**
     * If set, then this parser should record all missing resources like files
     * or declarations that should be available but were not found.
     * @param recorder the recorder to use or <code>null</code> 
     */
    public void setMissingResourceRecorder( IMissingResourceRecorder recorder );
    
    /**
     * Gets the recorder used for reporting missing resources.
     * @return the argument of {@link #setMissingResourceRecorder(IMissingResourceRecorder)}
     */
    public IMissingResourceRecorder getMissingResourceRecorder();
    
    /**
     * If set, then the parser should create {@link IFoldingRegion}s.
     * @param create <code>true</code> if folding regions are to be created,
     * <code>false</code> otherwise
     */
    public void setCreateFoldingRegions( boolean create );
    
    /**
     * Gets the folding regions which the parser created by the last
     * invocation of {@link #parse(IMultiReader, IProgressMonitor)}. This
     * method returns <code>null</code> if {@link #setCreateFoldingRegions(boolean)}
     * was set to <code>false</code>.
     * @return the regions, can be <code>null</code>
     */
    public IFoldingRegion[] getFoldingRegions();
    
    /**
     * If set to <code>true</code> then the parser should resolve the full
     * {@link IASTModel}, otherwise only the parts that are accessible
     * from outside need to be resolved.
     * @param full whether to resolve the model fully
     */
    public void setResolveFullModel( boolean full );
    
    /**
     * Tells this parser to create a full abstract syntax tree for later use.
     * @param create <code>true</code> if a full AST should be created
     * @see #getAST()
     */
    public void setCreateAST( boolean create );
    
    /**
     * Gets the full abstract syntax tree that was created during parsing.
     * @return the AST or <code>null</code> if {@link #setCreateAST(boolean)} 
     * was set to <code>false</code>
     * @see #setCreateAST(boolean)
     */
    public INesCAST getAST();
    
    /**
     * Informs this parser that it should create an {@link INesCInspector}. Not
     * every parser is able to create an inspector.
     * @param create whether to create an inspector
     */
    public void setCreateInspector( boolean create );
    
    /**
     * Gets the inspector that was created by this parser. The inspector might
     * be <code>null</code> even if {@link #setCreateInspector(boolean)} was
     * called. It can either be <code>null</code> because this parser does not
     * support inspectors at all, or because the parsed source code has an
     * unexpected form. 
     * @return the inspector or <code>null</code>
     */
    public INesCInspector getInspector();
    
    /**
     * Informs this parser it should create a list of {@link IASTReference}s. 
     * This is an optional feature, parsers not supporting references should
     * just assume <code>create</code> is always <code>false</code>.
     * @param create whether to create references
     * @see #getReferences()
     */
    public void setCreateReferences( boolean create );
    
    /**
     * Gets the references pointing from the parsed file to another file
     * or to itself. The behavior of this method is unspecified if
     * {@link #setCreateReferences(boolean)} was called with <code>false</code>.
     * @return the references, may be <code>null</code>.
     * @see #setCreateReferences(boolean)
     */
    public IASTReference[] getReferences();
    
    /**
     * Parses all content that can be obtained through <code>reader</code>.
     * @param reader the reader to read from
     * @param monitor used to inform the user about the state of the parser and
     * also used to cancel the parser.
     * @return <code>true</code> if the file could be parsed,
     * <code>false</code> if an error occurred. If <code>false</code> is returned,
     * then any output of this parser might be <code>null</code> or faulty. If
     * canceled, <code>false</code>.
     * @throws IOException if the reader throws an IOException
     */
    public boolean parse( IMultiReader reader, IProgressMonitor monitor ) throws IOException;
}
