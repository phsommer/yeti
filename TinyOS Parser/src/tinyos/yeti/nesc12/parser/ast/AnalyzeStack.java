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
package tinyos.yeti.nesc12.parser.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IFoldingRegion;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.reference.ASTReference;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.parser.standard.FoldingRegion;
import tinyos.yeti.nesc12.CancellationException;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.ep.declarations.FieldDeclaration;
import tinyos.yeti.nesc12.ep.declarations.TypedDeclaration;
import tinyos.yeti.nesc12.ep.nodes.ComponentModelNode;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.InterfaceModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ModelDeclarationResolver;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ParserInsights;
import tinyos.yeti.nesc12.parser.ast.elements.CombinedName;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCComponent;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypedefType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.AttributeDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.DeclarationStack;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusher;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;
import tinyos.yeti.nesc12.parser.ast.util.pushers.TagPusher;
import tinyos.yeti.nesc12.parser.meta.GenericRangedCollection;
import tinyos.yeti.nesc12.parser.meta.GenericRangedCollector;
import tinyos.yeti.nesc12.parser.meta.NamedType;
import tinyos.yeti.nesc12.parser.meta.RangedCollection;
import tinyos.yeti.nesc12.parser.meta.TypedefRangedCollector;
import tinyos.yeti.nesc12.parser.preprocessor.include.IncludeLinker;
import tinyos.yeti.nesc12.parser.preprocessor.macro.MacroLinker;
import tinyos.yeti.preprocessor.IncludeProvider;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.output.Insight;

public class AnalyzeStack {
    /** index for map containing {@link Key} - value pairs */
    private static final int VALUES     = 0;
    /** index for map containing {@link Field}s */
    private static final int FIELDS     = 10;
    /** index for map containing {@link Type typedefs} */
    private static final int TYPEDEFS   = 20;
    private static final int TYPEDEFS_MODEL = 21;
    private static final int TYPEDEFS_PATH = 22;

    /** index for map containing names and types, for example for
     * "struct point{ int x, y; };", point would be the key */
    private static final int TYPETAGS = 30;
    private static final int TYPETAGS_MODEL = 31;
    private static final int TYPETAGS_PATH = 32;
    /** index for map containing {@link NesCInterface}s */
    private static final int INTERFACES = 40;
    /** index for map containing {@link NesCComponent}s */
    private static final int COMPONENTS = 50;
    /** index for map containing the values of a "uses" or "provides" clause */
    private static final int INTERFACE_REFERENCE = 60;
    /** index for map containing the values of a "components" clause */
    private static final int COMPONENT_REFERENCE = 70;
    /** index for map containing attributes */
    private static final int ATTRIBUTES = 80;
    private static final int ATTRIBUTES_MODEL = 81;
    /** index for map containing enumerations */
    private static final int ENUMERATIONS = 100;

    /** index for map storing {@link FieldPusher}s */
    private static final int FIELD_PUSHERS = 110;
    /** index for map storing {@link TagPusher}s */
    private static final int TAG_PUSHERS = 120;

    private final MapKey MAP_KEY = new MapKey( 0, null );

    private ASTMessageHandler messageHandler;
    private Level global = new Level( null, true, null );
    private Level scope = global;

    private boolean reportErrors = true;

    private boolean createDeclarations = true;
    private boolean collectorDeclarations = false;
    private DeclarationResolver declarationResolver;

    private AnalyzeBindingResolver bindingResolver;

    private List<BaseDeclaration> predefinedDeclarations = new ArrayList<BaseDeclaration>();

    private NesC12ASTModel model;
    private boolean resolveFull;
    private NodeStack nodeStack;
    private DeclarationStack declarationStack;
    private MacroLinker macroLinker;
    
    private IncludeProvider includeProvider;
    private IncludeLinker includeLinker;

    private Parser parser;

    private IProgressMonitor monitor;
    private int remainingTicks = 10000;

    private TypedefRangedCollector rangedTypedefCollector;
    private GenericRangedCollector rangedCollector;
    
    private List<IFoldingRegion> folding;
    private List<IASTReference> references;
    
    private ICancellationMonitor cancellationMonitor = new ICancellationMonitor(){
        public IProgressMonitor getProgressMonitor(){
            return requestProgressMonitor();
            // return new SubProgressMonitor( monitor, 0 );
        }

        public void checkCancellation() throws CancellationException{
            AnalyzeStack.this.checkCancellation();
        }
    };

    public AnalyzeStack( Parser parser, IProgressMonitor monitor, ASTMessageHandler handler, DeclarationResolver declarationResolver, 
    		IncludeProvider includeProvider, MacroLinker macroLinker, IncludeLinker includeLinker, boolean resolveFull ){
        bindingResolver = new AnalyzeBindingResolver( this );

        if( handler == null ){
            handler = new ASTMessageHandler(){
                public void report( Severity severity, String message, Insight insight, RangeDescription... ranges ){
                    // ignore
                }

                public void report( Severity severity, String message, Insight insight, ASTNode... nodes ) {
                    // ignore
                }
            };
        }

        this.reportErrors = parser.isCreateMessages();
        this.createDeclarations = parser.isCreateDeclarations();

        this.monitor = monitor;
        monitor.beginTask( "Analyze", remainingTicks );

        model = parser.getModel();
        this.resolveFull = resolveFull;
        if( model != null ){
            nodeStack = new NodeStack( this, model );
        }
        if( model == null && isReportErrors() ){
            model = new NesC12ASTModel( parser.getProject(), null );
            this.declarationResolver = new ModelDeclarationResolver( model );
            model.setDeclarationResolver( this.declarationResolver );
        }
        else{
            this.declarationResolver = declarationResolver;
        }

        this.parser = parser;
        this.messageHandler = handler;

        if( parser.isCreateTypedefRangedCollection() ){
            rangedTypedefCollector = new TypedefRangedCollector( this );
        }
        if( parser.isCreateAST() ){
            rangedCollector = new GenericRangedCollector( this );
        }
        if( parser.isCreateFoldingRegions() ){
            folding = new ArrayList<IFoldingRegion>();
        }
        if( parser.isCreateReferences() ){
        	references = new ArrayList<IASTReference>();
        }

        this.includeProvider = includeProvider;
        declarationStack = new DeclarationStack( this );
        
        this.macroLinker = macroLinker;
        this.includeLinker = includeLinker;
    }

    public Parser getParser(){
        return parser;
    }

    public NodeStack getNodeStack(){
        return nodeStack;
    }
    
    public MacroLinker getMacroLinker(){
		return macroLinker;
	}
    
    public IncludeLinker getIncludeLinker(){
		return includeLinker;
	}

    public BindingResolver getBindingResolver(){
        return bindingResolver;
    }

    public LazyRangeDescription range( ASTNode location ){
        return new LazyRangeDescription( location, getParser() );
    }
    
    public Name name( Identifier name ){
        return new SimpleName( range( name ), name.getName() );
    }

    public Name name( ASTNode location, String name ){
        return new SimpleName( range( location ), name );
    }

    public Name name( ASTNode location, Name... names ){
        return new CombinedName( range( location ), names );
    }

    public ASTMessageHandler getMessageHandler(){
        return messageHandler;
    }

    public DeclarationResolver getDeclarationResolver(){
        return declarationResolver;
    }

    public IncludeProvider getIncludeProvider(){
        return includeProvider;
    }

    public boolean isCreateModel(){
        return model != null;
    }

    public boolean isCreateFullModel(){
        return isCreateModel() && resolveFull;
    }

    public boolean isCreateGraph(){
        return isCreateModel();
    }
    
    public boolean isCreateReferences(){
    	return references != null;
    }

    /**
     * Whether to report errors or just to ignore faulty code.
     * @return <code>true</code> if errors should be reported
     */
    public boolean isReportErrors(){
        return reportErrors;
    }

    /**
     * Whether to create {@link IDeclaration}s or not.
     * @return <code>true</code> if declarations should be created
     */
    public boolean isCreateDeclarations(){
        return createDeclarations;
    }

    public void setCreateCollectorDeclarations( boolean collectorDeclarations ) {
        this.collectorDeclarations = collectorDeclarations;
    }

    public boolean isCreateCollectorDeclarations(){
        return collectorDeclarations;
    }

    public IDeclaration[] getDeclarations(){
        return declarationStack.getDeclarations();
    }

    private BaseDeclaration getDeclaration( String name, Kind... kind ){
        for( BaseDeclaration declaration : declarationStack ){
            if( declaration.getName().equals( name )){
                Kind declKind = declaration.getKind();
                for( Kind check : kind ){
                    if( check == declKind )
                        return declaration;
                }
            }
        }
        return null;        
    }

    public void addPredefinedDeclaration( BaseDeclaration declaration ){
        predefinedDeclarations.add( declaration );
        
        if( declaration instanceof TypedDeclaration ){
	        if( declaration.getKind() == Kind.TYPEDEF ){
	        	putPredefinedType( declaration.createName(), ((TypedDeclaration)declaration).getType(), declaration.getPath() );
	        }
	        else if( declaration.getKind() == Kind.UNION || declaration.getKind() == Kind.STRUCT || declaration.getKind() == Kind.ENUMERATION ){
	        	putTypeTag( declaration.createName(), ((TypedDeclaration)declaration).getType(), null, 0, declaration.getPath() );
	        }
        }
        
        if( rangedCollector != null ){
            if( declaration instanceof FieldDeclaration ){
                FieldDeclaration fieldDeclaration = (FieldDeclaration)declaration;
                Field field = fieldDeclaration.toField();

                rangedCollector.putField( field, 0, 0, false );
            }
        }
    }

    private BaseDeclaration getPredefinedDeclaration( String name, Kind... kind ){
        for( BaseDeclaration declaration : predefinedDeclarations ){
            if( declaration.getName().equals( name )){
                Kind declKind = declaration.getKind();
                for( Kind check : kind ){
                    if( check == declKind )
                        return declaration;
                }
            }
        }
        return null;
    }

    /**
     * Tries to find a declaration for <code>name</code> of type <code>kind</code>,
     * this method will first search all predefined declarations, then all
     * new declarations and finally resolve to search the global declarations.
     * @param name the name to look for
     * @param global whether to perform a global search or not
     * @param kind the type of the searched declaration
     * @return the declaration meeting the criteria
     */
    public IDeclaration resolveDeclaration( String name, boolean global, Kind... kind ){
        IDeclaration result = getPredefinedDeclaration( name, kind );
        if( result != null )
            return result;

        result = getDeclaration( name, kind );
        if( result != null )
            return result;

        if( !global )
            return null;

        if( declarationResolver == null )
            return null;

        return declarationResolver.resolve( name, requestProgressMonitor(), kind );
    }

    public ASTModel getModel() {
        return model;
    }
    

    public void reference( RangeDescription source, IASTModelPath target ){
    	if( references != null && source != null && target != null ){
    		reference( getRegion( source ), target );
    	}
    }

    public void reference( IFileRegion source, IASTModelPath target ){
    	if( references != null && source != null && target != null ){
    		references.add( new ASTReference( source, target ) );
    	}
    }
    

    public void reference( ASTNode source, IASTModelPath target ){
    	if( references != null && source != null && target != null ){
    		reference( getRegion( source ), target );
    	}
    }
    
    public void reference( ASTNode source, ModelConnection target ){
    	if( references != null && source != null && target != null ){
    		reference( getRegion( source ), getDeclarationResolver().resolvePath( target, requestProgressMonitor() ));
    	}
    }
    
    public IASTReference[] getReferences(){
    	if( references == null )
    		return null;
    	return references.toArray( new IASTReference[ references.size() ] );
    }
    
    /**
     * Gets the ranged collection with the typedefs that was created during resolve.
     * @param length the number of characters in the input file
     * @return the collection or <code>null</code>
     */
    public RangedCollection<NamedType> getTypedefRangedCollection( int length ){
        if( rangedTypedefCollector != null ){
            // +1: to be sure not ending to early
            return rangedTypedefCollector.close( length+1 );
        }

        return null;
    }

    /**
     * Gets the ranged collection of various elements.
     * @param length the length of the input file
     * @return the collection or <code>null</code> if not created
     */
    public GenericRangedCollection getRangeCollection( int length ){
        if( rangedCollector != null )
            return rangedCollector.close( length );

        return null;
    }

    /**
     * Sets the error-flag for all {@link ModelNode}s which
     * are currently pushed onto the stack.
     */
    public void nodeError(){
        if( nodeStack != null ){
            nodeStack.putErrorFlag();
        }
    }

    public void nodeWarning(){
        if( nodeStack != null ){
            nodeStack.putWarningFlag();
        }
    }

    public DeclarationStack getDeclarationStack(){
		return declarationStack;
	}
    
    /**
     * Gets the file that is currently analyzed.
     * @return the analyzed file
     */
    public IParseFile getParseFile(){
        return parser.getParseFile();
    }

    /**
     * Checks whether the parse files name equals <code>name</code>.
     * @param name the name of to check
     * @param extension the expected extension of the file, may be <code>null</code> to
     * indicate that the extension does not matter
     * @return <code>true</code> if name and extension match, also
     * <code>true</code> if no parse file is defined
     */
    public boolean isParseFileName( String name, String extension ){
        IParseFile file = getParseFile();
        if( file == null )
            return true;

        String fileName = file.getName();
        int dot = fileName.lastIndexOf( '.' );
        if( dot < 0 ){
            return extension == null && name.equals( fileName ); 
        }

        String begin = fileName.substring( 0, dot );
        if( !name.equals( begin ))
            return false;

        if( extension == null )
            return true;

        if( dot+1 >= fileName.length() )
            return false;

        return extension.equals( fileName.substring( dot+1 ) );
    }
    
    /**
     * Tries to find the file region which describes <code>node</code>
     * @param node some node
     * @return the location in the input file, might be <code>null</code>
     */
    public FileRegion getRegion( ASTNode node ){
        RangeDescription range = parser.resolveLocation( true, node );
        return getRegion( range );
    }

    public FileRegion getRegion( RangeDescription range ){
    	return RuleUtility.source( range );
    }

    public void message( String message, Token... tokens ){
        message( message, null, tokens );
    }

    public void message( String message, Insight insight, Token... tokens ){
        messageHandler.report( ASTMessageHandler.Severity.MESSAGE, message, insight, parser.resolveLocation( true, tokens ));
    }

    public void message( String message, ASTNode... nodes ){
        message( message, null, nodes );
    }

    public void message( String message, Insight insight, ASTNode... nodes ){
        messageHandler.report( ASTMessageHandler.Severity.MESSAGE, message, insight, parser.resolveLocation( true, nodes ));
    }

    public void message( String message, RangeDescription... ranges ){
        message( message, null, ranges );
    }

    public void message( String message, Insight insight, RangeDescription... ranges ){
        messageHandler.report( ASTMessageHandler.Severity.MESSAGE, message, insight, ranges );
    }

    public void warning( String message, ASTNode... nodes ){
        warning( message, null, nodes );
    }

    public void warning( String message, Insight insight, ASTNode... nodes ){
        messageHandler.report( ASTMessageHandler.Severity.WARNING, message, insight, parser.resolveLocation( true, nodes ));
        nodeWarning();
    }

    public void warning( String message, RangeDescription... ranges ){
        warning( message, null, ranges );
    }

    public void warning( String message, Insight insight, RangeDescription... ranges ){
        messageHandler.report( ASTMessageHandler.Severity.WARNING, message, insight, ranges );
        nodeWarning();
    }

    public void warning( String message, Token... tokens ){
        warning( message, null, tokens );
    }

    public void warning( String message, Insight insight, Token... tokens ){
        messageHandler.report( ASTMessageHandler.Severity.WARNING, message, insight, parser.resolveLocation( true, tokens ));
        nodeWarning();        
    }

    public void error( String message, Token... tokens ){
        error( message, null, tokens );
    }

    public void error( String message, Insight insight, Token... tokens ){
        messageHandler.report( ASTMessageHandler.Severity.ERROR, message, insight, parser.resolveLocation( true, tokens ));
        nodeError();
    }

    public void error( String message, ASTNode... nodes ){
        error( message, null, nodes );
    }

    public void error( String message, Insight insight, ASTNode... nodes ){
        messageHandler.report( ASTMessageHandler.Severity.ERROR, message, insight, parser.resolveLocation( true, nodes ));
        nodeError();
    }

    public void error( String message, RangeDescription... ranges ){
        error( message, null, ranges );
    }

    public void error( String message, Insight insight, RangeDescription... ranges ){
        messageHandler.report( ASTMessageHandler.Severity.ERROR, message, insight, ranges );
        nodeError();
    }

    public ICancellationMonitor getCancellationMonitor(){
        return cancellationMonitor;
    }

    public IProgressMonitor requestProgressMonitor(){
        int ticks = remainingTicks / 10;
        remainingTicks -= ticks;
        return new SubProgressMonitor( monitor, ticks );
    }

    public void checkCancellation(){
        if( monitor.isCanceled() )
            throw new CancellationException();
    }

    /**
     * Opens a new level within the current scope.
     * @param factory how to handle declarations, can be <code>null</code>
     */
    public void push( FieldPusherFactory factory ){
        pushRangedCollectors();
        scope = new Level( scope, false, factory );
    }

    /**
     * Opens a new scope.
     * @param factory how to handle declarations, can be <code>null</code>
     */
    public void pushScope( FieldPusherFactory factory ){
        pushRangedCollectors();
        scope = new Level( scope, true, factory );
    }

    /**
     * Closes the current scope.
     * @param outputLocation the location in the output file at which the
     * scope gets popped
     */
    public void pop( int outputLocation ){
        popRangedCollectors( outputLocation );

        scope.close();
        scope = scope.parent;
        if( scope == null ){
            scope = global;
        }
    }

    /**
     * Closes the current scope.
     * @param outputLocation the location in the output file at which the
     * scope gets popped
     */
    public void popScope( int outputLocation ){
        popRangedCollectors( outputLocation );

        scope.close();
        scope = scope.parent;
        if( scope == null ){
            scope = global;
        }
    }

    private void pushRangedCollectors(){
        if( rangedTypedefCollector != null )
            rangedTypedefCollector.push();

        if( rangedCollector != null )
            rangedCollector.push();
    }

    private void popRangedCollectors( int location ){
        if( rangedTypedefCollector != null )
            rangedTypedefCollector.pop( location );

        if( rangedCollector != null )
            rangedCollector.pop( location );
    }

    /**
     * Whether the folding structure should be created or not. The method
     * {@link #folding(ASTNode)} will only work if this is <code>true</code>
     * @return whether the folding structure should be created
     */
    public boolean isFolding(){
        return folding != null;
    }
    
    /**
     * Gets the folding regions which were reported until now.
     * @return the folding regions or <code>null</code>
     */
    public IFoldingRegion[] getFolding(){
        if( folding == null )
            return null;
        
        return folding.toArray( new IFoldingRegion[ folding.size() ] );
    }
    
    /**
     * Creates a folding region for <code>node</code>
     * @param node some node which can be folded
     */
    public void folding( ASTNode node ){
        if( !isFolding() )
            return;
        
        if( getParseFile() == null )
            return;
        
        RangeDescription ranges = parser.resolveLocation( false, node );
        boolean set = false;
        int left = 0;
        int right = 0;
        
        for( int i = 0, n = ranges.getRootCount(); i<n; i++ ){
        	RangeDescription.Range root = ranges.getRoot( i );
        
            NesC12FileInfo info = (NesC12FileInfo)root.file();
            IParseFile file = info == null ? null : info.getParseFile();
            if( getParseFile().equals( file )){
                if( !set ){
                    set = true;
                    left = root.left();
                    right = root.right();
                }
                else{
                    left = Math.min( left, root.left() );
                    right = Math.max( right, root.right() );
                }
            }
        }
        
        if( set ){
            if( left > right ){
                int temp = left;
                left = right;
                right = temp;
            }
            
            folding( left, right-left );
        }
    }
    
    /**
     * Stores an additional folding region.
     * @param offset the first character within the region
     * @param length the number of characters within the region
     */
    public void folding( int offset, int length ){
        if( folding != null ){
            folding.add( new FoldingRegion( offset, length ) );
        }
    }
    
    public void close(){
        int length = parser.getPreprocessorReader().getFileLength()+1;

        while( scope != null ){
            popRangedCollectors( length );
            scope.close( );
            scope = scope.parent;
        }

        scope = global;
        bindingResolver.setStack( null );
    }

    /**
     * Tells whether the current scope is the global scope.
     * @return <code>true</code> if the scope is global
     */
    public boolean isGlobal(){
        return scope == global;
    }

    public void put( Flag flag ){
        scope.put( flag );
    }

    public boolean present( Flag flag ){
        return scope.present( flag, true );
    }

    public boolean presentScope( Flag flag ){
        return scope.present( flag, false );
    }

    public boolean presentLevel( Flag flag ){
        return scope.flags.contains( flag );
    }

    public void remove( Flag flag ){
        scope.remove( flag );
    }

    /**
     * Stores some key value pair in the current scope. When leaving the scope,
     * the pair will get replaced by the previous pair.
     * @param <A> the type of value to store
     * @param key the name under which to store the value
     * @param value the value to store, can be <code>null</code> 
     */
    public <A> void put( Key<A> key, A value ){
        if( value == null ){
            remove( key );
        }
        else{
            scope.put( VALUES, key, value );
        }
    }

    @SuppressWarnings( "unchecked" )
    public <A> A get( Key<A> key ){
        return (A)scope.get( VALUES, key );
    }

    @SuppressWarnings( "unchecked" )
    public <A> A getScope( Key<A> key ){
        return (A)scope.getScope( VALUES, key );
    }

    @SuppressWarnings( "unchecked" )
    public <A> A getLevel( Key<A> key ){
        return (A)scope.getLevel( VALUES, key );
    }

    public <A> void remove( Key<A> key ){
        scope.remove( VALUES, key );
    }

    public void putEnum( Field field, int top ){
        Name name = field.getName();

        if( isReportErrors() ){
            Field old = (Field)scope.getScope( ENUMERATIONS, name );
            if( old != null ){
                error( "redefinition of '" + name + "'", name.getRange() );
                warning( "'" + name + "' gets redefined", old.getRange() );
                ModelNode node = old.asNode();
                if( node != null )
                	getNodeStack().putWarningFlag( node, top );
            }
            else{
                old = (Field)scope.get( ENUMERATIONS, name );
                if( old != null ){
                    warning( "shadowing of '" + name + "'", name.getRange(), old.getRange() );
                }
            }
        }

        if( rangedCollector != null )
            rangedCollector.putField( field, field.getRange().getRight(), 0, false );

        scope.put( ENUMERATIONS, name, field );
    }

    public Field getEnum( Name name ){
        return (Field)scope.get( ENUMERATIONS, name );
    }
    
    /**
     * Defines a new field. If a field with the same name in the same
     * scope already exists, then that field gets replaced.
     * @param field the new field
     * @param accessable
     * @param forwardDeclaration whether the declaration is a forward declaration 
     * or not
     */
    public void putField( Field field, int accessable, boolean forwardDeclaration ){
        putField( field, accessable, forwardDeclaration, 0 );
    }

    /**
     * Defines a new field in this or a parent scope.
     * @param field the new field
     * @param accessable the location from which on the field is visible
     * @param forwardDeclaration whether the declaration is a forward declaration 
     * or not
     * @param top how far to jump down on the stack, at least 0
     */
    public void putField( Field field, int accessable, boolean forwardDeclaration, int top ){
        Name name = field.getName();
        if( name == null )
        	return;
        
        if( rangedTypedefCollector != null )
            rangedTypedefCollector.notypedef( name, top );

        if( rangedCollector != null ){
            rangedCollector.putField( field, accessable, top, !forwardDeclaration );
        }

        Level scope = this.scope;
        while( top > 0 ){
            scope = scope.parent;
            top--;
        }

        if( isReportErrors() ){
            FieldPusher pusher = scope.getFieldPusher( name );
            pusher.pushField( name, field, forwardDeclaration, field.asNode() );

            Field old = (Field)scope.getScope( FIELDS, name );
            if( old == null ){
                old = (Field)scope.get( FIELDS, name );
                if( old != null ){
                	Insight insight = ParserInsights.shadowingField( 
                			field.getFieldName(), field.getRange(), old.getRange() );
                	warning( "shadowing of '" + name + "'", insight, name.getRange(), old.getRange() );
                }
            }
        }

        scope.put( FIELDS, name, field );
    }

    /**
     * Searches the top level field with name <code>name</code>
     * @param name the name of the field
     * @return the field or <code>null</code>
     */
    public Field getField( Name name ){
        return (Field)scope.get( FIELDS, name );
    }
    
    /**
     * Searches for a field with name <code>name</code> in the current
     * scope.
     * @param name the name of the field to search
     * @param top how many scopes to get upward until the search begins
     * @return the name
     */
    public Field getFieldScope( Name name, int top ){
    	return (Field)scope.getScope( FIELDS, name, top );
    }

    /**
     * Stores an additional typedef type on this stack. The type <code>type</code>
     * can be a {@link TypedefType} but does not have to be, this method will
     * ensure that it is stored as a {@link TypedefType} with the corrent
     * name <code>name</code>.
     * @param name the name of the type
     * @param type the type itself
     * @param attributes attributes related ot the type
     * @param model the representation of the typedef, can be <code>null</code>
     */
    public void putTypedef( Name name, Type type, ModelAttribute[] attributes, ModelNode model ){
    	putTypedef( name, type, attributes, model, 0 );
    }
    
  /**
    * Stores an additional typedef type on this stack. The type <code>type</code>
    * can be a {@link TypedefType} but does not have to be, this method will
    * ensure that it is stored as a {@link TypedefType} with the corrent
    * name <code>name</code>.
    * @param name the name of the type
    * @param type the type itself
    * @param attributes attributes related to the type
    * @param model the representation of the typedef, can be <code>null</code>
    * @param top how far from the top to store the typedef, only applied to
    * the {@link RangedCollection}s 
    */
   public void putTypedef( Name name, Type type, ModelAttribute[] attributes, ModelNode model, int top ){
        type = toTypedef( name, type );
        
        if( rangedTypedefCollector != null )
            rangedTypedefCollector.typedef( name, type, attributes, top );

        if( isReportErrors() ){
            FieldPusher pusher = scope.getFieldPusher( name );
            pusher.pushType( name, type, model );
        }

        scope.put( TYPEDEFS, name, type );

        if( model != null )
            scope.put( TYPEDEFS_MODEL, name.toIdentifier(), model );
    }

    /**
     * Stores a type that was defined outside the parsed file.
     * @param name the name of the type
     * @param type the type
     * @param path path to the type, might be <code>null</code>
     */
    private void putPredefinedType( Name name, Type type, ASTModelPath path ){
        if( rangedTypedefCollector != null )
            rangedTypedefCollector.typedef( name, type, null, 0 );

        scope.put( TYPEDEFS, name, toTypedef( name, type ) );
        if( path != null ){
        	scope.put( TYPEDEFS_PATH, name, path );
        }
    }

    private TypedefType toTypedef( Name name, Type type ){
        String identifier = name.toIdentifier();
        TypedefType result = type.asTypedefType();
        
        if( result != null ){
            if( !result.getName().equals( identifier )){
                result = new TypedefType( identifier, type );
            }
        }
        else{
            result = new TypedefType( identifier, type );
        }
        
        return result;
    }

    public TypedefType getTypedef( Name name ){
        return (TypedefType)scope.get( TYPEDEFS, name );
    }

    public ModelNode getTypedefModel( String name ){
        return (ModelNode)scope.get( TYPEDEFS_MODEL, name );
    }
    
    public ASTModelPath getTypedefPath( Name name ){
    	return (ASTModelPath)scope.get( TYPEDEFS_PATH, name );
    }

    /**
     * Puts a new type tag on the stack.
     * @param name the name of the tag
     * @param type the type of the tag
     * @param attributes attributes related to the type
     * @param accessible the location from which on the type is accessible
     */
    public void putTypeTag( Identifier name, Type type, ModelAttribute[] attributes, int accessible ){
        if( isReportErrors() ){
            scope.getTagPusher( name ).push( name, type );
        }
        putTypeTag( name( name ), type, attributes, accessible, null );
    }
    
    public void putTypeTag( Name name, Type type, ModelAttribute[] attributes, int accessible, ASTModelPath path ){
        scope.put( TYPETAGS, name.toIdentifier(), type );
        scope.put( TYPETAGS_PATH, name.toIdentifier(), path );
        
        if( rangedCollector != null ){
            rangedCollector.putTypeTag( name, type, attributes, accessible, 0 );
        }
    }

    public Type getTypeTag( String name ){
        return (Type)scope.get( TYPETAGS, name );
    }

    public ASTModelPath getTypeTagPath( String name ){
    	return (ASTModelPath)scope.get( TYPETAGS_PATH, name );
    }
    
    public void putTypeTagModel( String name, ModelNode node ){
        if( node != null )
            scope.put( TYPETAGS_MODEL, name, node );
    }

    public ModelNode getTypeTagModel( String name ){
        return (ModelNode)scope.get( TYPETAGS_MODEL, name );
    }

    public void putInterface( InterfaceModelNode interfaze ){
        scope.put( INTERFACES, interfaze.getIdentifier(), interfaze );
    }

    public InterfaceModelNode getInterface( String name ){
        InterfaceModelNode result = (InterfaceModelNode)scope.get( INTERFACES, name );
        if( result == null ){
            IDeclaration declaration = getDeclarationResolver().resolve( name, requestProgressMonitor(), Kind.INTERFACE );
            if( declaration != null ){
                result = (InterfaceModelNode)getDeclarationResolver().resolve( declaration, requestProgressMonitor() );
            }
        }
        return result;
    }

    public void putComponent( ComponentModelNode component ){
        if( isReportErrors() ){
            ComponentModelNode old = (ComponentModelNode)scope.getScope( COMPONENTS, component.getName() );
            if( old != null ){
                error( "redefinition of '" + component.getName() + "'", component.getName().getRange(), old.getName().getRange() );
            }
        }

        scope.put( COMPONENTS, component.getName(), component );
    }

    public ComponentModelNode getComponent( Name name ){
        return (ComponentModelNode)scope.get( COMPONENTS, name );
    }

    /**
     * Adds a reference to an interface. Reference to interfaces
     * are in the uses/provides clause.
     * @param reference the new reference
     * @param accessable the location from which on the reference is visible
     */
    public void putInterfaceReference( InterfaceReferenceModelConnection reference, int accessable ){
        if( isReportErrors() ){
            InterfaceReferenceModelConnection old = (InterfaceReferenceModelConnection)scope.get( INTERFACE_REFERENCE, reference.getName() );
            if( old != null ){
                error( "redefinition of '" + reference.getName() + "'", reference.getName().getRange(), old.getName().getRange() ); 
            }
        }

        scope.put( INTERFACE_REFERENCE, reference.getName(), reference );

        if( rangedCollector != null )
            rangedCollector.putInterfaceReference( reference, accessable, 0 );
    }

    /**
     * Searches a reference in the current scope.
     * @param name the name of the reference
     * @return the reference or <code>null</code>
     */
    public InterfaceReferenceModelConnection getInterfaceReference( Name name ){
        return (InterfaceReferenceModelConnection)scope.get( INTERFACE_REFERENCE, name );
    }

    public void putComponentReference( ComponentReferenceModelConnection reference, int outputLocation ){
        // TODO report errors when redefining component or interface
        scope.put( COMPONENT_REFERENCE, reference.getName(), reference );
        if( rangedCollector != null ){
            rangedCollector.putComponentReference( reference, outputLocation, 0 );
        }
    }

    public ComponentReferenceModelConnection getComponentReference( String name ){
        return (ComponentReferenceModelConnection)scope.getScope( COMPONENT_REFERENCE, name );
    }

    public void putAttribute( AttributeDeclaration decl, ModelNode node ){
        scope.put( ATTRIBUTES, decl.getName(), decl.resolveType() );
        scope.put( ATTRIBUTES_MODEL, decl.getName(), node );
    }

    public DataObjectType getAttribute( Identifier name ){
        Type type = (Type)scope.get( ATTRIBUTES, name );
        if( type == null )
            return null;
        return type.asDataObjectType();
    }
    
    public ModelNode getAttributeModel( Identifier name ){
    	return (ModelNode)scope.get( ATTRIBUTES_MODEL, name );
    }

    /**
     * Tells whether the function with the name <code>name</code> is a
     * constant function, a constant function can be evaluated during
     * compile time.
     * @param name the name of a function
     * @return <code>true</code> if the function is constant
     */
    public boolean isConstantFunction( String name ){
    	return
    		"unique".equals( name ) ||
    		"uniqueN".equals( name ) ||
    		"uniqueCount".equals( name );
    }

    /**
     * A modifiable key for an entry in a {@link Level}
     */
    private class MapKey{
        private int index;
        private Object key;

        public MapKey( int index, Object key ){
            this.index = index;
            this.key = key;
        }

        public void put( int index, Object key ){
            this.index = index;
            this.key = key;
        }

        @Override
        public boolean equals( Object obj ) {
            MapKey other = (MapKey)obj;
            return index == other.index && key.equals( other.key );
        }

        @Override
        public int hashCode() {
            return index + key.hashCode();
        }
    }

    /**
     * A single level in the stack.
     */
    private class Level{
        private Map<MapKey, Object> map = new HashMap<MapKey, Object>();

        private Set<Flag> flags = new HashSet<Flag>();

        private Level parent;

        /** whether this is the root of a scope, set to <code>true</code> if this was created using {@link AnalyzeStack#pushScope(FieldPusherFactory)} */
        private boolean scopeLevel;

        private FieldPusherFactory fieldPusherFactory;

        public Level( Level parent, boolean scopeLevel, FieldPusherFactory fieldPusherFactory ){
            this.parent = parent;
            this.scopeLevel = scopeLevel;
            this.fieldPusherFactory = fieldPusherFactory;
        }

        public FieldPusherFactory getFieldPusherFactory(){
            if( fieldPusherFactory != null )
                return fieldPusherFactory;

            if( parent != null )
                return parent.getFieldPusherFactory();

            return FieldPusherFactory.STANDARD;
        }

        public FieldPusher getFieldPusher( Name name ){
            FieldPusher pusher = (FieldPusher)getLevel( FIELD_PUSHERS, name );
            if( pusher == null ){
                pusher = getFieldPusherFactory().create( name.toIdentifier(), AnalyzeStack.this );
                put( FIELD_PUSHERS, name, pusher );
            }

            return pusher;
        }

        public TagPusher getTagPusher( Identifier name ){
            TagPusher pusher = (TagPusher)getLevel( TAG_PUSHERS, name );
            if( pusher == null ){
                pusher = new TagPusher( name.getName(), AnalyzeStack.this );
                put( TAG_PUSHERS, name, pusher );
            }
            return pusher;
        }

        public void close(){
            if( isReportErrors() ){
                for( Object pusher : getAllLevel( FIELD_PUSHERS )){
                    ((FieldPusher)pusher).resolve();
                }
                for( Object pusher : getAllLevel( TAG_PUSHERS )){
                    ((TagPusher)pusher).resolve();
                }
            }
        }

        public void put( Flag flag ){
            flags.add( flag );
        }
        public boolean present( Flag flag, boolean outsideScope ){
            if( flags.contains( flag ))
                return true;

            if( parent != null ){
                if( outsideScope || !scopeLevel )
                    return parent.present( flag, outsideScope );
            }

            return false;
        }

        public void remove( Flag flag ){
            flags.remove( flag );
        }

        @SuppressWarnings( "unchecked" )
        public <A> A put( int index, Object key, A value ){
            return (A)map.put( new MapKey( index, key ), value );
        }

        public void remove( int index, Object key ){
            MAP_KEY.put( index, key );
            map.remove( MAP_KEY );
        }

        public Object getLevel( int index, Object key ){
            MAP_KEY.put( index, key );
            return map.get( MAP_KEY );
        }

        public Collection<Object> getAllLevel( int index ){
            List<Object> list = new ArrayList<Object>();
            for( Map.Entry<MapKey, Object> entry : map.entrySet() ){
                if( entry.getKey().index == index ){
                    list.add( entry.getValue() );
                }
            }
            return list;
        }

        public Object getScope( int index, Object key ){
            MAP_KEY.put( index, key );
            Level level = this;
            while( level != null ){
                Object value = level.map.get( MAP_KEY );
                if( value != null )
                    return value;

                if( level.scopeLevel )
                    level = null;
                else
                    level = level.parent;
            }
            return null;
        }
        
        public Object getScope( int index, Object key, int top ){
        	Level level = this;
        	while( level != null ){
        		if( top == 0 )
            		return level.getScope( index, key );
            	
        		if( level.scopeLevel ){
        			top--;
        		}
        		
        		level = level.parent;
        	}
        	
        	return null;
        }

        public Object get( int index, Object key ){
            MAP_KEY.put( index, key );
            Level level = this;
            while( level != null ){
                Object value = level.map.get( MAP_KEY );
                if( value != null )
                    return value;

                level = level.parent;
            }
            return null;            
        }
    }
}   
