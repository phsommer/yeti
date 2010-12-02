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
package tinyos.yeti.preprocessor.lexer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import tinyos.yeti.preprocessor.CommentCallback;
import tinyos.yeti.preprocessor.FileInfo;
import tinyos.yeti.preprocessor.FileInfoFactory;
import tinyos.yeti.preprocessor.IncludeCallback;
import tinyos.yeti.preprocessor.IncludeProvider;
import tinyos.yeti.preprocessor.MacroCallback;
import tinyos.yeti.preprocessor.MessageHandler;
import tinyos.yeti.preprocessor.MessageHandler.Severity;
import tinyos.yeti.preprocessor.lexer.Macro.VarArg;
import tinyos.yeti.preprocessor.lexer.macro.*;
import tinyos.yeti.preprocessor.lexer.streams.IncludeStream;
import tinyos.yeti.preprocessor.lexer.streams.MacroStream;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos.yeti.preprocessor.output.Insights;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.elements.*;
import tinyos.yeti.preprocessor.process.CancellationException;

/**
 * A {@link State} holds the different states of the preprocessor. For
 * example the list of defined makros.
 * @author Benjamin Sigg
 */
public class State {
	/** how many includes may be stacked before an error is thrown */
    private static final int MAX_INCLUDE_NESTING = 25;
    
    /** the base scanner which reads the file */
    private PreprocessorLexer base;
    
    /** scanner placing macros */
    private MacroLexer macroLexer;
    
    /** scanner detecting if-else-directives and swalloing tokens */
    private ConditionalLexer conditionalLexer;
    
    /** all the macros that are currently defined */
    private Map<String, Macro> macros = new HashMap<String, Macro>();
    
    /** interface to the parser, reading directly from {@link #conditionalLexer} */
    private Scanner scanner;
    
    /** resolves requests for files */
    private IncludeProvider includeProvider;
    
    /** any message is forwarded to this handler */
    private MessageHandler messageHandler;
    
    /** any comment is forwarded to this handler */
    private CommentCallback commentObserver;
    
    /**
     * Tells for the current position of the stream how many 'if' and 'else'
     * blocks that should be swallowed are currently nested. The default value
     * is <code>0</code>. Any directive 'if', 'else', 'elif', etc... which
     * indicates a block that is not included increases this field. Any
     * 'endif' decreases the field.
     */
    private int noReadIfNesting = 0;
    
    /**
     * Counts how deep the stream currently is in 'if' and 'else' blocks,
     * any 'if' increases this field, any 'endif' decreases this field. The
     * default value is 0.
     */
    private int ifNesting = 0;
    
    /**
     * Tells for a chain of 'if', 'elif' and 'else' blocks whether one
     * block already was evaluated. Only one block in such a chain can
     * be evaluated, all other are swallowed. A value of <code>false</code>
     * indicates that no block has yet passed, a value of <code>true</code>
     * indicates that exactly one block has passed.
     */
    private LinkedList<Boolean> ifState = new LinkedList<Boolean>();

    /** 
     * Counts how deep the stream currently is in including files. Any
     * 'include' directive increases this field, any end-of-file token
     * decreases this field.
     */
    private int includeNesting = 0;
    
    /** Tells the exact position of the stream in respect of included files and applied macros. */
    private InclusionPath inclusionPath;

    /** Used to create meta-information about the current file */
    private FileInfoFactory fileInfoFactory;
    
    /** checked for cancellation */
    private IProgressMonitor monitor;
    /** remaining ticks in {@link #monitor}, may be used to create a sub-progress monitor */
    private int remainingMonitorTicks;
    
    /** callback to inform about the usage of macros */
    private MacroCallback macroCallback;
    /** callback to inform about the inclusion of files */
    private IncludeCallback includeCallback;
    
    public State( FileInfo filename, FileInfoFactory fileInfoFactory, PurgingReader reader, IncludeProvider includeProvider, MessageHandler messageHandler, CommentCallback commentObserver, IProgressMonitor monitor ){
        if( filename == null )
            throw new IllegalArgumentException( "filename must not be null" );
        
        remainingMonitorTicks = 10000;
        monitor.beginTask( "Preprocess '" + filename.getName() + "'", remainingMonitorTicks );
        this.monitor = monitor;
        
        this.fileInfoFactory = fileInfoFactory;
        this.commentObserver = commentObserver;
        
        base = new PreprocessorLexer( this, filename, reader );
        
        macroLexer = new MacroLexer( base );
        macroLexer.setStates( this );
        
        conditionalLexer = new ConditionalLexer( macroLexer );
        conditionalLexer.setStates( this );
        
        scanner = new Scanner(){
            public Symbol next_token() throws Exception {
                checkCancellation();
                Symbol next = conditionalLexer.next();
                // System.out.println( next.value );
                return next;
            }
        };
        
        this.includeProvider = includeProvider;
        this.messageHandler = messageHandler;
        
        putMacro( new LineMacro() );
        putMacro( new FileMacro() );
        putMacro( new DateMacro() );
        putMacro( new TimeMacro() );
        putMacro( new CounterMacro() );
        putMacro( new Attribute() );
        putMacro( new GenericMacro( "__STDC__", new String[]{}, VarArg.NO, "1" ) );
    }
    
    public void setMacroCallback( MacroCallback macroCallback ){
        this.macroCallback = macroCallback;
    }
    
    public void checkCancellation(){
        if( monitor.isCanceled() ){
            throw new CancellationException();
        }
    }
    
    public IProgressMonitor requestMonitor(){
        int ticks = remainingMonitorTicks / 5;
        remainingMonitorTicks -= ticks;
        return new SubProgressMonitor( monitor, ticks );
    }
    
    public PreprocessorLexer getBase() {
        return base;
    }
    
    public Scanner getScanner(){
        return scanner;
    }
    
    public MacroLexer getDefineLexer() {
        return macroLexer;
    }
    
    public ConditionalLexer getConditionalLexer() {
        return conditionalLexer;
    }
    
    public IncludeProvider getIncludeProvider() {
        return includeProvider;
    }
    
    public FileInfoFactory getFileInfoFactory(){
        return fileInfoFactory;
    }
    
    public CommentCallback getCommentObserver(){
		return commentObserver;
	}
    
    public void reportError( String message, Insight information, PreprocessorElement... elements ){
        messageHandler.handle( Severity.ERROR, message, information, elements );
    }

    public void reportWarning( String message, Insight information, PreprocessorElement... elements ){
        messageHandler.handle( Severity.WARNING, message, information, elements );
    }

    public void reportMessage( String message, Insight information, PreprocessorElement... elements ){
        messageHandler.handle( Severity.MESSAGE, message, information, elements );
    }
    
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }
    
    /**
     * Whether we are currently in a block of code that will be ignored
     * or not.
     * @return <code>true</code> if the current code should be ignored
     */
    public boolean shouldIgnore(){
    	return noReadIfNesting != 0;
    }
    
    /**
     * Whether macros should be evaluated or not in the current block.
     * @return <code>true</code> if macros should be ignored
     */
    public boolean shouldIgnoreMacros(){
    	switch( noReadIfNesting ){
    		case 0: return false;
    		case 1: return !conditionalLexer.isReadingElifPart();
    		default: return true;
    	}
    }
    
    public void line( int line, String filename ){
        getBase().setLine( line );
    }
    
    public void changeIncludeNesting( IncludeStream stream ){
        if( stream != null ){
            includeNesting++;
            inclusionPath = InclusionPath.include( inclusionPath, stream.getFilename() );
        }
        else{
            includeNesting--;
            inclusionPath = inclusionPath.getParent();
        }
    }
    
    public int getIncludeNesting(){
        return includeNesting;
    }
    
    public InclusionPath getInclusionPath() {
        return inclusionPath;
    }
    
    public void changeMacroNesting( MacroStream stream ){
        if( stream != null ){
            inclusionPath = InclusionPath.macro( inclusionPath, stream.getIdentifier() );
            if( macroCallback != null ){
            	macroCallback.applied( stream.getMacro(), stream.getIdentifier() );
            }
        }
        else{
            inclusionPath = inclusionPath.getParent();
        }
    }
    
    /**
     * Checks the contents of <code>token</code> and whether it can be read
     * right now. If not, than a replacement for <code>token</code> is created
     * that is valid in the current situation.
     * @param token some token
     * @return a valid version of <code>token</code>, can be <code>token</code> itself
     */
    public Symbol symbol( PreprocessorToken token ){
        if( token != null ){        
            token = new PreprocessorToken( token.getKind(), token.getFile(), token.getLine(), token.getBegin(), token.getEnd(), token.getText(), inclusionPath );
        }
        
        return new Symbol( token.getKind().sym(), token );
    }
    
    public void include( final Include include ) throws IOException{
        if( include != null && !shouldIgnore() ){
            if( includeNesting >= MAX_INCLUDE_NESTING ){
                reportError( "include nesting too deep", Insights.includeNestingToDeep( MAX_INCLUDE_NESTING ), include );
            }
            else{
                Stream stream = include.stream( this );
                if( stream != null ){
                    base.push( stream );
                }
            }
        }
    }
    
    public void setIncludeCallback( IncludeCallback includeCallback ){
		this.includeCallback = includeCallback;
	}
    
    public IncludeCallback getIncludeCallback(){
		return includeCallback;
	}
    
    /**
     * Starts a new 'if'-block. The condition of <code>command</code>
     * is only evaluated if the stream is not in a swallowed block.
     * @param command the condition, can be <code>null</code> if the
     * stream is in a swallowed block
     */
    public void ifpart( IfPart command ){
        ifNesting++;
        
        if( noReadIfNesting > 0 ){
            noReadIfNesting++;
            pushIfState( false );
        }
        else{
            boolean condition = command.evaluateCondition( this );
            if( !condition ){
                noReadIfNesting++;
            }
            pushIfState( condition );
        }
    }
    
    public void ifdefpart( IfdefPart command ){
        ifNesting++;
        
        if( noReadIfNesting > 0 ){
            noReadIfNesting++;
            pushIfState( false );
        }
        else{
            boolean condition = command.valid() && getMacro( command.identifier() ) != null;
            if( !condition ){
                noReadIfNesting++;
            }
            pushIfState( condition );
        }
    }
    
    public void ifndefpart( IfndefPart command ){
        ifNesting++;
        
        if( noReadIfNesting > 0 ){
            noReadIfNesting++;
            pushIfState( false );
        }
        else{
            boolean condition = command.valid() && getMacro( command.identifier() ) == null;
            if( !condition ){
                noReadIfNesting++;
            }
            pushIfState( condition );
        }        
    }
    
    public void elifpart( ElifPart command ){
        if( ifNesting > 0 ){
            if( noReadIfNesting == 1 ){
                boolean state = peekIfState();
                if( !state ){
                    boolean condition = command.evaluateCondition( this );
                    if( condition )
                        noReadIfNesting--;
    
                    setIfState( condition );
                }
            }
            else if( noReadIfNesting == 0 ){
                noReadIfNesting++;
            }
        }
    }
    
    public boolean elifpartWillBeEvaluated(){
        return ifNesting > 0 && noReadIfNesting == 1;
    }
    
    public void elsepart( PreprocessorElement location ){
        if( ifNesting > 0 ){
            if( noReadIfNesting == 1 ){
                boolean state = peekIfState();
                if( !state ){
                    noReadIfNesting--;
                    setIfState( true );
                }
            }
            else if( noReadIfNesting == 0 ){
                noReadIfNesting++;
            }
        }
        else{
            reportError( "Directive #else without #if, #ifdef or #ifndef", Insights.elseWithoutBegin(), location );
        }
    }
    
    public void endifpart( PreprocessorElement location ){
        if( ifNesting == 0 ){
            reportError( "Directive #endif without #if, #ifdef or #ifndef", Insights.endifWithoutBegin(), location );
        }
        else{
            ifNesting--;
            
            if( noReadIfNesting > 0 ){
                popIfState();
                noReadIfNesting--;
            }
        }
    }
    
    /**
     * Pushes a new if-state on the top of the if-state-stack. The if-state
     * tells whether a block in an if-elif-else-end statement was already
     * executed or not.<br>
     * An if-statement would set the if-state to the value of its condition.<br>
     * An elif-statement checks its state only if the if-state is <code>false</code>, and
     * then an elif-statement sets the value of its condition.<br>
     * An else-statement would always pop the if-state. An else-statement only
     * executes its block when the if-state is <code>false</code>. 
     * @param executed whether the block was executed or not
     */
    public void pushIfState( boolean executed ){
        ifState.addLast( Boolean.valueOf( executed ));
    }
    
    /**
     * Gets the topmost if state, see {@link #pushIfState(boolean)}.
     * @return the state
     */
    public boolean peekIfState(){
        return ifState.getLast().booleanValue();
    }
    
    /**
     * Sets the topmost if state, see {@link #pushIfState(boolean)}.
     * @param executed the new value
     */
    public void setIfState( boolean executed ){
        popIfState();
        pushIfState( executed );
    }
    
    /**
     * Gets and removes the topmost if state, see {@link #pushIfState(boolean)}.
     * @return the state
     */
    public boolean popIfState(){
        return ifState.removeLast().booleanValue();
    }
    
    public Macro getMacro( String name ) {
        return macros.get( name );
    }
    
    public Macro putMacro( Macro def ){
        if( macroCallback != null ){
            macroCallback.declared( def );
        }
        return macros.put( def.getName(), def );
    }
    
    public Macro removeMacro( String name ){
        Macro result = macros.remove( name );
        
        if( macroCallback != null ){
            macroCallback.undeclared( name, result );
        }
        
        return result;
    }
    
    public void define( Define def ){
        if( def != null && !shouldIgnore() ){
            Macro old = putMacro( def );
            if( old != null ){
                if( !old.validSubstitution( def )){
                    PreprocessorElement location = old.getLocation();
                    if( location == null )
                        reportWarning( "Macro \"" + def.getName() + "\" redefined", 
                                Insights.macroRedefined( def.getName() ), def.getLocation() );
                    else
                        reportWarning( "Macro \"" + def.getName() + "\" redefined", 
                                Insights.macroRedefined( def.getName() ), def.getLocation(), location );
                }
            }
        }
    }
    
    public void undef( Undef undef ){
        if( !shouldIgnore() ){
            removeMacro( undef.identifier() );
        }
    }
}
