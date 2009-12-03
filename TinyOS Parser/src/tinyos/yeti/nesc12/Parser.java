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
package tinyos.yeti.nesc12;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFoldingRegion;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.ep.parser.IMissingResourceRecorder;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.IMultiReader;
import tinyos.yeti.nesc.StringMultiReader;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.ep.declarations.TypedDeclaration;
import tinyos.yeti.nesc12.lexer.NesCLexer;
import tinyos.yeti.nesc12.parser.AdvancedParser;
import tinyos.yeti.nesc12.parser.ListDeclarationResolver;
import tinyos.yeti.nesc12.parser.ModelDeclarationResolver;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.NesC12FileInfoFactory;
import tinyos.yeti.nesc12.parser.NesC12IncludeProvider;
import tinyos.yeti.nesc12.parser.NesC12Inspector;
import tinyos.yeti.nesc12.parser.ParserMessageTranslator;
import tinyos.yeti.nesc12.parser.ProjectDeclarationResolver;
import tinyos.yeti.nesc12.parser.ProjectIncludeProvider;
import tinyos.yeti.nesc12.parser.RawLexer;
import tinyos.yeti.nesc12.parser.RawParser;
import tinyos.yeti.nesc12.parser.RecordingDeclarationResolver;
import tinyos.yeti.nesc12.parser.RecordingIncludeProvider;
import tinyos.yeti.nesc12.parser.ScopeStack;
import tinyos.yeti.nesc12.parser.SelfReferenceIncludeProvider;
import tinyos.yeti.nesc12.parser.SplitMessageHandler;
import tinyos.yeti.nesc12.parser.StandardParserMessageHandler;
import tinyos.yeti.nesc12.parser.SynchronizedDeclarationResolver;
import tinyos.yeti.nesc12.parser.ast.ASTMessageHandler;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypeName;
import tinyos.yeti.nesc12.parser.ast.visitors.ASTVisitorAdapter;
import tinyos.yeti.nesc12.parser.ast.visitors.ConvergingASTVisitor;
import tinyos.yeti.nesc12.parser.meta.NamedType;
import tinyos.yeti.nesc12.parser.meta.RangedCollection;
import tinyos.yeti.nesc12.parser.preprocessor.comment.CommentCollection;
import tinyos.yeti.nesc12.parser.preprocessor.comment.FoldingCommentCallback;
import tinyos.yeti.nesc12.parser.preprocessor.comment.MultiCommentCallback;
import tinyos.yeti.nesc12.parser.preprocessor.include.IncludeLinker;
import tinyos.yeti.nesc12.parser.preprocessor.macro.MacroLinker;
import tinyos.yeti.nesc12.parser.preprocessor.macro.MacroQuickLinkCreator;
import tinyos.yeti.nesc12.parser.preprocessor.macro.MultiMacroCallback;
import tinyos.yeti.nesc12.parser.preprocessor.macro.PredefinedMacro;
import tinyos.yeti.preprocessor.IncludeProvider;
import tinyos.yeti.preprocessor.MacroCallback;
import tinyos.yeti.preprocessor.MessageHandler;
import tinyos.yeti.preprocessor.Preprocessor;
import tinyos.yeti.preprocessor.PreprocessorReader;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.output.FlaggedAreaRecognizer;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * This parser parser NesC 1.2 files, C-files and is able to apply a preprocessor to the files
 * before parsing them.
 * @author Benjamin Sigg
 */
public class Parser implements INesCParser{
    public static void main( String[] args ){
        Type t = Parser.parseType( "int [5]", new IDeclaration[]{} );
        System.out.println( t );
    }
    
    /**
     * Parses <code>code</code>, prints out any messages and searches for the
     * first occurrence of a node of type <code>result</code>.
     * @param code the code to parse
     * @param result the type of node to return
     * @return either a node or <code>null</code> if non was found or an
     * error occurred
     */
    public static <A extends ASTNode> A quickParser( String code, final Class<A> result ){
        return quickParser( code, result, 0 );
    }

    /**
     * Parses <code>code</code>, prints out any messages and searches for the
     * first occurrence of a node of type <code>result</code>.
     * @param code the code to parse
     * @param result the type of node to return
     * @param index if all the occurences of <code>result</code> are put into
     * an array, then the index'th element is returned
     * @return either a node or <code>null</code> if non was found or an
     * error occurred
     */
    @SuppressWarnings("unchecked")
    public static <A extends ASTNode> A quickParser( String code, final Class<A> result, final int index ){
        Parser parser = new Parser( null );
        parser.setMessageHandler( new ParserMessageHandler(){

            public void error( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
                print( "error", message, ranges );
            }

            public void message( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
                print( "warning", message, ranges );
            }

            public void warning( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
                print( "message", message, ranges );
            }

            private void print( String severity, String message, RangeDescription... ranges ){
                LinkedHashSet<Integer> lines = new LinkedHashSet<Integer>();
                for( RangeDescription range : ranges ){
                	for( int i = 0, n = range.getRootCount(); i<n; i++ ){
                        lines.add( range.getRoot( i ).line() );
                    }
                }

                System.out.print( "[" + severity + ", line(s): " );
                boolean first = true;
                for( int i : lines ){
                    if( first )
                        first = false;
                    else
                        System.out.print( ", " );

                    System.out.print( i );
                }
                System.out.println( "] " + message);
            }
        });

        try {
            parser.parse( new StringMultiReader( code ), null );
        }
        catch( IOException e ) {
            e.printStackTrace();
            return null;
        }

        ASTNode root = parser.getRootASTNode();
        if( root == null )
            return null;

        final ASTNode[] back = new ASTNode[1];
        root.accept( new ConvergingASTVisitor(){
            int jump = index;

            @Override
            public boolean convergedVisit( ASTNode node ) {
                if( back[0] != null )
                    return false;

                if( result.isInstance( node ) ){
                    if( jump == 0 ){
                        back[0] = node;
                        return false;
                    }
                    else
                        jump--;
                }

                return true;
            }
            @Override
            public void convergedEndVisit( ASTNode node ) {
                // ignore   
            }
        });

        return (A)back[0];
    }

    public static IDeclaration parseType( String type, String name, IDeclaration[] typedefs ){
        Type resolved = parseType( type, typedefs );
        if( resolved == null )
            return null;

        TypedDeclaration declaration = new TypedDeclaration(
                IDeclaration.Kind.TYPEDEF, resolved, name, resolved.toLabel( name, Type.Label.SMALL ),
                null, null, null );
        
        return declaration;
    }

    /**
     * Parses the "type name" <code>type</code> and returns the type.
     * @param type some typename
     * @param typedefs additional typedefs to consider, can be <code>null</code>
     * @return the type that <code>type</code> represents or <code>null</code> if the
     * type was not valid
     */
    public static Type parseType( String type, IDeclaration[] typedefs ){
        Parser parser = new Parser( null );
        if( typedefs != null ){
            parser.addDeclarations( typedefs );
        }
        try {
            Set<String> keys = new HashSet<String>();
            if( typedefs != null ){
                for( IDeclaration typedef : typedefs )
                    keys.add( typedef.getName() );
            }

            int index = 0;
            if( typedefs != null ){
                while( keys.contains( "n" + index ))
                    index++;
            }

            parser.setResolve( true );
            parser.parse( new StringMultiReader( "int n" + index + " = (" + type + ")0;" ), null );
        }
        catch( IOException e ) {
            // should never happen
            e.printStackTrace();
            return null;
        }

        ASTNode root = parser.getRootASTNode();
        if( root == null )
            return null;

        final Type[] result = new Type[1];
        root.accept( new ASTVisitorAdapter(){
            @Override
            public boolean visit( TypeName node ){
                result[0] = node.resolveType();
                return false;
            }
        });

        return result[0];
    }

    /** the resource that currently gets parsed */
    private IParseFile file;

    /** Tells how to handle messages from the parser. */
    private ParserMessageHandler messageHandler;

    /** whether to create messages or not */
    private boolean createMessages = false;

    /** handler for messages */
    private ParserMessageTranslator translator;

    /** whether to create {@link IDeclaration}s or not */
    private boolean createDeclarations = false;

    /** whether to resolve bindings or not */
    private boolean resolve = false;

    /** whether to resolve include directives or not */
    private boolean followIncludes = false;

    /** set of macros that will be used by the preprocessor */
    private List<Macro> macros = new ArrayList<Macro>();

    /** the errors which were found in the last run of the parser */
    private List<IMessage> errors;

    /** some predefined typedef names */
    private List<BaseDeclaration> declarations = new ArrayList<BaseDeclaration>();

    /** the new declarations */
    private IDeclaration[] collectedDeclarations;

    /** used to resolve declarations */
    private DeclarationResolver declarationResolver;

    private ASTNode root;

    private NesC12ASTModel model;

    private boolean full = false;

    /** whether to create {@link #typedefRangedCollection}*/
    private boolean createTypedefRangedCollection = false;

    /** tells for each typedef where it is accessible in the input file */
    private RangedCollection<NamedType> typedefRangedCollection;

    /** currently used preprocessor */
    private PreprocessorReader preprocessorReader;

    /** the project whose files this parser processes */
    private IProject project;

    /** whether to create an ast or not */
    private boolean createAST = false;
    
    /** the ast of this parser */
    private NesC12AST ast;
    
    /** records missing files and declarations */
    private IMissingResourceRecorder recorder;
    
    /** will be added to the preprocessor and informs about any new macro */
    private MacroCallback macroCallback;
    
    /** whether to create {@link #foldingRegions} */
    private boolean createFolding = false;
    
    /** the list of regions that can be folded */
    private IFoldingRegion[] foldingRegions = null;
    
    private boolean createInspector;
    
    private INesCInspector inspector;
    
    private boolean createReferences;
    
    private IASTReference[] references;
    
    public Parser( IProject project ){
        this.project = project;
        GccBuiltin.addAllTo( this );
    }

    /**
     * Set the message handler directly, overriding the default handler that
     * would create a list of messages.
     * @param messageHandler the explicitly set message handler
     */
    public void setMessageHandler( ParserMessageHandler messageHandler ) {
        this.messageHandler = messageHandler;
    }

    public void setCreateDeclarations( boolean create ) {
        createDeclarations = create;
    }

    public boolean isCreateDeclarations(){
        return createDeclarations;
    }

    public IDeclaration[] getDeclarations() {
        return collectedDeclarations;
    }

    public void addDeclarations( IDeclaration[] declarations ) {
        for( IDeclaration declaration : declarations ){
            if( declaration instanceof BaseDeclaration ){
            	Kind kind = declaration.getKind();
            	if( kind == Kind.FIELD || kind == Kind.FUNCTION || kind == Kind.TYPEDEF || kind == Kind.STRUCT || kind == Kind.UNION || kind == Kind.ENUMERATION || kind == Kind.ENUMERATION_CONSTANT ){
            		BaseDeclaration base = (BaseDeclaration)declaration;
            		this.declarations.add( base );
            	}
            }
        }
    }

    public void addMacro( IMacro macro ){
    	macros.add( PredefinedMacro.instance( macro ));
    }

    public void setParseFile( IParseFile name ) {
        file = name;
    }

    public IParseFile getParseFile(){
        if( file == null )
            return NullParseFile.NULL;

        return file;
    }

    public IProject getProject(){
		return project;
	}
    
    public void setCreateAST( boolean create ) {
        createAST = create;
    }
    
    public boolean isCreateAST(){
        return createAST;
    }

    public INesCAST getAST() {
        return ast;
    }
    
    public void setCreateInspector( boolean create ){
    	createInspector = create;
    }
    
    public boolean isCreateInspector(){
		return createInspector;
	}
    
    public INesCInspector getInspector(){
	    return inspector;
    }
    
    public void setCreateReferences( boolean create ){
	    createReferences = create;	
    }
    
    public boolean isCreateReferences(){
		return createReferences;
	}
    
    public IASTReference[] getReferences(){
	    return references;
    }

    public void setASTModel( IASTModel model ) {
        this.model = (NesC12ASTModel)model;
    }

    public IASTModel getASTModel() {
        return model;
    }

    public void setResolveFullModel( boolean full ) {
        this.full = full;
    }

    public boolean isResolveFullModel() {
        return full;
    }

    public IMessage[] getMessages() {
        if( errors == null )
            return new IMessage[]{};
        return errors.toArray( new IMessage[ errors.size() ] );
    }

    public NesC12ASTModel getModel(){
        return model;
    }

    public void setDeclarationResolver( DeclarationResolver declarationResolver ){
        this.declarationResolver = declarationResolver;
    }
    
    public void setMissingResourceRecorder( IMissingResourceRecorder recorder ){
        this.recorder = recorder;
    }
    
    public IMissingResourceRecorder getMissingResourceRecorder(){
        return recorder;
    }

    /**
     * Gets the root of the AST that was the result of the last call
     * to {@link #parse(IMultiReader, IProgressMonitor)}.
     * @return the result of the last call to {@link #parse(IMultiReader, IProgressMonitor)} or <code>null</code>
     * if an error occurred
     */
    public ASTNode getRootASTNode(){
        return root;
    }
    
    public void setMacroCallback( MacroCallback macroCallback ){
        this.macroCallback = macroCallback;
    }

    public boolean parse( IMultiReader reader, IProgressMonitor monitor ) throws IOException {
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        // set up monitor
        final int TICKS_PREPROCESSOR = 100;
        final int TICKS_PARSER = 100;
        final int TICKS_ANALYZE = 800;

        boolean resolve = this.resolve || createMessages || (model != null) || createInspector || createReferences ;
        boolean minorResolve = createTypedefRangedCollection || resolve || createAST || createFolding || createDeclarations;

        int ticks = TICKS_PREPROCESSOR + TICKS_PARSER;
        if( minorResolve )
            ticks += TICKS_ANALYZE;

        monitor.beginTask( "Parse '" + getParseFile().getName() + "'", ticks );
        
        // clean up for next round
        root = null;
        errors = null;
        collectedDeclarations = null;
        translator = null;
        ast = null;
        foldingRegions = null;
        references = null;

        if( monitor.isCanceled() ){
            monitor.done();
            return false;
        }
        
        // setup message handler
        Debug.info("file="+file);

        StandardParserMessageHandler errorCollector = null;

        if( createMessages || messageHandler != null ){
            if( createMessages )
                errorCollector = new StandardParserMessageHandler( getParseFile() );

            ParserMessageHandler finalHandler;

            if( createMessages && messageHandler != null )
                finalHandler = new SplitMessageHandler( errorCollector, messageHandler );
            else if( messageHandler != null )
                finalHandler = messageHandler;
            else
                finalHandler = errorCollector;

            translator = new ParserMessageTranslator( this, finalHandler );
        }
        
        MacroLinker macroLinker = null;
        if( model != null || isCreateReferences() || isCreateAST() ){
        	macroLinker = new MacroLinker( this );
        }

        MacroQuickLinkCreator macroQuickLinker = null;
        if( isCreateAST() ){
        	macroQuickLinker = new MacroQuickLinkCreator( this );
        }
        
        IncludeLinker includeLinker = null;
        if( model != null || isCreateReferences() || isCreateAST() ){
        	includeLinker = new IncludeLinker( this );
        }
        
        // search associated tos-project
        ProjectTOS tos = null;
        if( project != null ){
            TinyOSPlugin plugin = TinyOSPlugin.getDefault();
            if( plugin != null ){
            	try{
            		tos = plugin.getProjectTOS( project );
            	}
            	catch( MissingNatureException ex ){
            		// silent
            	}
            }
        }

        // Create preprocessor
        Preprocessor pre = new Preprocessor();
        pre.setMessageHandler( translator == null ? new NullHandler() : translator );
        pre.setFileInfoFactory( new NesC12FileInfoFactory() );
        
        if( macroCallback != null || macroLinker != null || macroQuickLinker != null ){
        	pre.setMacroCallback( new MultiMacroCallback( macroCallback, macroLinker, macroQuickLinker ));
        }
        
        if( includeLinker != null ){
        	pre.setIncludeCallback( includeLinker );
        }
        
        RecordingIncludeProvider includeProvider = null;

        // setup interaction with outside world for preprocessor
        if( followIncludes && tos != null ){
            if( file == null )
                includeProvider = new RecordingIncludeProvider( provider( new ProjectIncludeProvider( tos ) ), recorder );
            else
                includeProvider = new RecordingIncludeProvider( provider( new SelfReferenceIncludeProvider( file, reader, tos ) ), recorder );

            pre.setIncludeProvider( includeProvider );
        }

        for( Macro macro : macros )
            pre.addMacro( macro );

        // monitor comments
        FoldingCommentCallback foldingComments = null;
        CommentCollection comments = null;
        
        if( resolve ){
        	comments = new CommentCollection();
        }
        
        if( createFolding ){
        	foldingComments = new FoldingCommentCallback();
        }
        
        if( comments != null || foldingComments != null ){
        	if( comments != null && foldingComments != null ){
        		pre.setComments( new MultiCommentCallback( comments, foldingComments ) );
        	}
        	else if( comments != null ){
        		pre.setComments( comments );
        	}
        	else if( foldingComments != null ){
        		pre.setComments( foldingComments );
        	}
        }
        
        // ** START preprocessor **
        Reader input = reader.open();
        preprocessorReader = pre.open( new NesC12FileInfo( getParseFile() ), input, new SubProgressMonitor( monitor, TICKS_PREPROCESSOR ) );
        if( monitor.isCanceled() )
            return false;

        if( translator != null )
            translator.preprocessorDone();

        if( macroLinker != null )
        	macroLinker.preprocessorDone();
        
        if( includeLinker != null )
        	includeLinker.preprocessorDone();
        
        try{
            // prepare lexer
            RawLexer lexer = createLexer( preprocessorReader );

            RawParser parser = createParser( lexer );

            ScopeStack scopes = parser.scopes();

            lexer.setScopeStack( scopes );

            // store declarations that need to be known for parsing
            for( BaseDeclaration declaration : declarations ){
            	if( declaration.getKind() == Kind.TYPEDEF ){
            		scopes.addTypedef( declaration.getName() );
            	}
            	else if( declaration.getKind() == Kind.ENUMERATION_CONSTANT ){
            		scopes.addEnumToplevel( declaration.getName() );
            	}
            }
            
            monitor.worked( TICKS_PARSER );
            preprocessorReader.close();
            
            if( monitor.isCanceled() )
                return false;

            root = parser.parseAST();
            
            if( minorResolve ){
            	resolve( resolve, tos, includeProvider, foldingComments, comments,
            			macroLinker, macroQuickLinker, includeLinker, new SubProgressMonitor( monitor, TICKS_ANALYZE ) );
            	if( monitor.isCanceled() )
            		return false;
            }

            if( includeProvider != null ){
                includeProvider.parsingFinished();
            }
            
            if( errorCollector != null ){
                errors = errorCollector.getErrors();
            }

            return true;
        }
        catch( CancellationException ex ){
            return false;
        }
        catch( OperationCanceledException ex ){
            return false;
        }
        catch( Exception ex ){
            ex.printStackTrace();
            return false;
        }
        catch( Error er ){
            er.printStackTrace();
            return false;
        }
        finally{
            translator = null;
            preprocessorReader = null;
            monitor.done();

            if( input != null ){
                input.close();
            }
        }
    }
    
    /**
     * Analyzes the current AST.
     * @param resolve whether to resolve the full tree or to resolve only minor things
     * @param tos the project to which the current file belongs
     * @param includeProvider to find other files in the project or the computer
     * @param foldingComments folding regions that were found during preprocessing, can be <code>null</code>
     * @param comments comments found during preprocessing, may be <code>null</code>
     * @param macroLinker to detect and link macros
     * @param macroQuickLinker to create quick links for macros
     * @param includeLinker to detect and link include directives
     * @param monitor to report progress or to cancel the operation
     */
    private void resolve( boolean resolve, ProjectTOS tos, IncludeProvider includeProvider,
    		FoldingCommentCallback foldingComments, CommentCollection comments,
    		MacroLinker macroLinker, MacroQuickLinkCreator macroQuickLinker,
    		IncludeLinker includeLinker, IProgressMonitor monitor ){
    	
        resolveIncluded( root );

        DeclarationResolver resolver = declarationResolver;

        if( resolve && model == null ){
            model = new NesC12ASTModel( getProject(), null );
        }

        if( model == null && createAST ){
            if( resolver == null ){
                if( tos != null ){
                    resolver = new ProjectDeclarationResolver( tos );
                }
                resolver = new SynchronizedDeclarationResolver( tos, resolver );
            }
        }
        else if( model != null ){
            if( resolver == null && tos != null ){
                if( model != tos.getModel().getCacheModel() ){
                    resolver = new ListDeclarationResolver(
                            new ModelDeclarationResolver( model ),
                            new ProjectDeclarationResolver( tos ) );
                }
                else{
                    resolver = new ProjectDeclarationResolver( tos );
                }

                resolver = new SynchronizedDeclarationResolver( tos, resolver );
            }
            else if( resolver == null ){
                resolver = new ModelDeclarationResolver( model );
            }
        }
        
        if( resolver != null && recorder != null ){
            resolver = new RecordingDeclarationResolver( resolver, recorder );
        }
        
        if( model != null && resolver != null ){
            model.setDeclarationResolver( resolver );
        }

        AnalyzeStack stack = new AnalyzeStack( this, monitor, translator, resolver, includeProvider, macroLinker, includeLinker, resolve );
        
        if( comments != null ){
        	comments.redistribute( root, stack );
        }
        
        for( BaseDeclaration declaration : declarations ){
        	stack.addPredefinedDeclaration( declaration );
        }
        
        informResolve( stack );
        
        root.resolve( stack );
        if( monitor.isCanceled() )
            return;
        
        if( isCreateReferences() && macroLinker != null ){
        	macroLinker.transmitReferences( stack );
        }
        
        if( isCreateReferences() && includeLinker != null ){
        	includeLinker.transmitReferences( stack );
        }
        
        stack.close();
        monitor.done();
        
        if( model != null ){
            // for( IASTModelNode node : model.getNodes( ASTNodeFilterFactory.all() ) ){
        	for( IASTModelNode node : model.getNodes() ){
                ((ModelNode)node).finishCreating();
            }
        }

        if( createDeclarations ){
            collectedDeclarations = stack.getDeclarations();

            if( collectedDeclarations != null ){
                for( IDeclaration declaration : collectedDeclarations ){
                    ((BaseDeclaration)declaration).resolveRanges();
                }
            }
        }

        typedefRangedCollection = stack.getTypedefRangedCollection( preprocessorReader.getFileLength() );
                
        if( createReferences ){
        	references = stack.getReferences();
        }
                
        if( createAST || createInspector ){
            ast = new NesC12AST(
                    getParseFile(), 
                    root, 
                    preprocessorReader,
                    typedefRangedCollection,
                    stack.getRangeCollection( preprocessorReader.getFileLength() ),
                    macroQuickLinker.toLinks() );
            ast.setProvider( includeProvider );
            ast.setResolver( resolver );
        }
        
        if( createFolding ){
        	foldingComments.transfer( stack );
            foldingRegions = stack.getFolding();
        }
        
        if( createInspector ){
        	inspector = new NesC12Inspector( getProject(), ast );
        }
        

        
        if( resolver != null ){
            resolver.parsingFinished();
        }
    }

    protected RawParser createParser( RawLexer lexer ){
        return new AdvancedParser( this, lexer );
    }

    protected RawLexer createLexer( Reader input ){
        return new NesCLexer( input );
    }

    /**
     * Can be used to replace or setup the current include provider.
     * @param provider the default provider
     * @return the provider that gets used
     */
    protected IncludeProvider provider( NesC12IncludeProvider provider ){
        return provider;
    }

    protected void informResolve( AnalyzeStack stack ){
        // ignore
    }
    
    public void setCreateFoldingRegions( boolean create ){
        createFolding = create;
    }
    
    public boolean isCreateFoldingRegions(){
        return createFolding;
    }
    
    public IFoldingRegion[] getFoldingRegions(){
        return foldingRegions;
    }

    public void setCreateMessages( boolean create ) {
        createMessages = create;
    }

    /**
     * Sets whether to collect the positions of typedefs in the input file.
     * @param createTypedefRangedCollection <code>true</code> if the positions should be collected
     */
    public void setCreateTypedefRangedCollection( boolean createTypedefRangedCollection) {
        this.createTypedefRangedCollection = createTypedefRangedCollection;
    }

    public boolean isCreateTypedefRangedCollection() {
        return createTypedefRangedCollection || createAST;
    }

    public RangedCollection<NamedType> getTypedefRangedCollection() {
        return typedefRangedCollection;
    }

    /**
     * Sets the general switch that either lets this parser only generate the
     * AST, or do also a small semantic analysis.
     * @param resolve if <code>true</code>, then this parser performs a 
     * small semantic analysis that will later allow clients to use the
     * "resolve..." methods that provide more information for some
     * nodes of the AST.
     */
    public void setResolve( boolean resolve ) {
        this.resolve = resolve;
        if( resolve ){
            setResolveFullModel( resolve );
        }
    }

    public void setFollowIncludes( boolean follow ) {
        followIncludes = follow;
    }

    public boolean isCreateMessages() {
        return createMessages;
    }
    

    /**
     * Reports an error to the message handler if there is any.
     * @param message the error
     * @param left the left side of the range in which the error occurred
     * @param right the right side of the range in which the error occurred
     */
    public void reportError( String message, int left, int right ){
        reportError( message, null, left, right );
    }

    /**
     * Reports an error to the message handler if there is any.
     * @param message the error
     * @param insight internal information of the message, might be <code>null</code>
     * @param left the left side of the range in which the error occurred
     * @param right the right side of the range in which the error occurred
     */
    public void reportError( String message, Insight insight, int left, int right ){
        if( translator != null ){
            RangeDescription range = resolveLocation( true, left, right );
            translator.getHandler().error( message, false, insight, range );
        }
    }

    /**
     * Sets the {@link ASTNode#setIncluded(boolean) included} flag of each
     * node.
     * @param root the root node
     */
    private void resolveIncluded( ASTNode root ){
        FlaggedAreaRecognizer included = preprocessorReader.queryAreaIncluded();
        resolveIncluded( root, included );
    }

    private boolean resolveIncluded( ASTNode node, FlaggedAreaRecognizer included ){
        boolean result = true;
        Range range = node.getRange();

        ASTNode child = null;

        for( int i = 0, n = node.getChildrenCount(); i<n; i++ ){
            ASTNode next = node.getChild( i );
            if( next != null ){
                if( result ){
                    if( child == null ){
                        int left = next.getRange().getLeft();
                        if( range.getLeft() < left ){
                            included.begin( range.getLeft() );
                            result = included.end( left );
                        }            
                    }
                    else{
                        int left = child.getRange().getRight();
                        int right = next.getRange().getLeft();

                        if( left < right ){
                            included.begin( left );
                            result = included.end( right );
                        }
                    }
                }
                result = resolveIncluded( next, included ) & result;
                child = next;
            }
        }

        if( result ){
            if( child == null ){
                included.begin( range.getLeft() );
                result = included.end( range.getRight() );
            }
            else{
                int right = child.getRange().getRight();
                if( right < range.getRight() ){
                    included.begin( right );
                    result = included.end( range.getRight() );
                }
            }
        }

        node.setIncluded( result );
        return result;
    }

    /**
     * Gets the reader which represents the whole output file.
     * @return the output file
     */
    public PreprocessorReader getPreprocessorReader(){
        return preprocessorReader;
    }
    
    /**
     * Uses the {@link PreprocessorReader} to find the range in the original
     * file that matches <code>left</code> to <code>right</code>.
     * @param inclusion whether to search upwards if the range lies within
     * an included file
     * @param left the left side in the output of the preprocessor
     * @param right the right side in the output of the preprocessor
     * @return the range that is described by <code>left/right</code>
     */
    public RangeDescription resolveLocation( boolean inclusion, int left, int right ){
        return preprocessorReader.range( left, right, inclusion );
    }
    
    public RangeDescription resolveInputLocation( int inputLeft, int inputRight ){
    	return preprocessorReader.inputRange( inputLeft, inputRight );
    }

    public RangeDescription[] resolveLocation( boolean inclusion, tinyos.yeti.nesc12.lexer.Token... tokens ){
        RangeDescription[] ranges = new RangeDescription[ tokens.length ];
        for( int i = 0, n = ranges.length; i<n; i++ ){
            ranges[i] = resolveLocation( inclusion, tokens[i].getLeft(), tokens[i].getRight() );
        }
        return ranges;
    }

    public RangeDescription[] resolveLocation( boolean inclusion, ASTNode[] location ) {
        RangeDescription[] ranges = new RangeDescription[ location.length ];
        for( int i = 0, n = ranges.length; i<n; i++ ){
            Range range = location[i].getRange();
            ranges[i] = preprocessorReader.range( range.getLeft(), range.getRight(), inclusion );
        }
        return ranges;
    }

    public RangeDescription resolveLocation( boolean inclusion, ASTNode location ) {
        Range range = location.getRange();
        if( preprocessorReader == null )
        	return null;
        return preprocessorReader.range( range.getLeft(), range.getRight(), inclusion );
    }

    public RangeDescription[] resolveLocation( boolean inclusion, PreprocessorElement... location ) {
        RangeDescription[] ranges = new RangeDescription[ location.length ];
        for( int i = 0, n = ranges.length; i<n; i++ ){
            ranges[i] = preprocessorReader.range( location[i], inclusion );
        }
        return ranges;
    }

    public RangeDescription resolveLocation( boolean inclusion, PreprocessorElement location ){
    	return preprocessorReader.range( location, inclusion );
    }

    public int getNearestInputLocation( int locations ){
        return preprocessorReader.inputLocation( locations, true );
    }

    private static class NullHandler implements MessageHandler, ASTMessageHandler{
        public void handle( MessageHandler.Severity severity, String message, Insight information, PreprocessorElement... elements ){
            // ignore
        }
        
        public void report( ASTMessageHandler.Severity severity, String message, Insight insight, ASTNode... nodes ) {
            // ignore
        }
        public void report( ASTMessageHandler.Severity severity, String message, Insight insight, RangeDescription... ranges ) {
            // ignore
        }
    }
}
