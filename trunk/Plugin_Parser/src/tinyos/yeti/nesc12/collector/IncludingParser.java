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
package tinyos.yeti.nesc12.collector;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.INesCDefinitionCollector;
import tinyos.yeti.ep.parser.INesCDefinitionCollectorCallback;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.IMultiReader;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.ep.declarations.TypedDeclaration;
import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.AdvancedParser;
import tinyos.yeti.nesc12.parser.NesC12IncludeProvider;
import tinyos.yeti.nesc12.parser.RawLexer;
import tinyos.yeti.nesc12.parser.RawParser;
import tinyos.yeti.nesc12.parser.ScopeStack;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.preprocessor.IncludeProvider;
import tinyos.yeti.preprocessor.MacroCallback;
import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * This parser only collects declarations of global fields, functions and typedefs and
 * discards everything else.
 * @author Benjamin Sigg
 */
public class IncludingParser implements INesCDefinitionCollector{
    private INesCDefinitionCollectorCallback callback;
    private List<IDeclaration> declarations = new ArrayList<IDeclaration>();
    
    private ScopeStack scopes;
    private AnalyzeStack stack;
    
    private ProjectTOS project;
    private IParseFile parseFile;
    
    private boolean reportIncludesOnly;
    private boolean reportMacros;
    
    private List<IMacro> macros = new ArrayList<IMacro>();
    
    public IncludingParser( ProjectTOS project, IParseFile parseFile ){
        this.project = project;
        this.parseFile = parseFile;
    }
    
    public void setReportIncludesOnly( boolean includesOnly ){
    	reportIncludesOnly = includesOnly;
    }
    
    public void setReportMacros( boolean macros ){
        reportMacros = macros;
    }
    
    public void addDeclaration( IDeclaration declaration ){
        declarations.add( declaration );
        TagSet tags = declaration.getTags();
        
        if( tags != null && tags.contains( NesC12ASTModel.TYPEDEF )){
            if( scopes != null ){
                scopes.addTypedef( declaration.getName() );
            }
            if( stack != null ){
                Type type = ((TypedDeclaration)declaration).getType();
                if( type != null ){
                    stack.putTypedef( new SimpleName( null, declaration.getName() ), type, null, null );
                }
            }
        }
        if( stack != null ){
            stack.addPredefinedDeclaration( (BaseDeclaration)declaration );
        }
    }
    
    private void setScopes( ScopeStack scopes ){
        this.scopes = scopes;
        for( IDeclaration declaration : declarations ){
            if( declaration.getTags().contains( NesC12ASTModel.TYPEDEF )){
                scopes.addTypedef( declaration.getName() );
            }
        }
    }
    
    private void setStack( AnalyzeStack stack ){
        this.stack = stack;
        for( IDeclaration declaration : declarations ){
            if( declaration.getTags().contains( NesC12ASTModel.TYPEDEF )){
                Type type = ((TypedDeclaration)declaration).getType();
                if( type != null ){
                    stack.putTypedef( new SimpleName( null, declaration.getName() ), type, null, null );
                }
            }
            stack.addPredefinedDeclaration( (BaseDeclaration)declaration );
        }
    }

    public void addMacro( IMacro macro ){
        macros.add( macro );
    }
    
    public void parse( IMultiReader reader, INesCDefinitionCollectorCallback callback, IProgressMonitor monitor ) throws IOException{
        this.callback = callback;
        
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Parse", 1000 );
        
        try{
            Environment environment = new Environment();
            
            if( reportMacros ){
                environment.setMacroCallback( new MacroCallback(){
                    public void declared( Macro macro ){
                        IncludingParser.this.callback.macroDefined( new MacroWrapper( macro ) );
                    }
                    public void undeclared( String name, Macro macro ){
                        IncludingParser.this.callback.macroUndefined( name );
                    }
                    public void applied( Macro macro, PreprocessorElement identifier ){
                    	// ignore
                    }
                });
            }
            
            environment.setFollowIncludes( true );
            environment.setCreateDeclarations( !reportIncludesOnly );
            environment.setParseFile( parseFile );
            
            for( IMacro macro : macros ){
                environment.addMacro( macro );
            }
            
            ProjectModel model = project.getModel();
            
            model.getBasicDeclarations().addBasics( environment, null, new SubProgressMonitor( monitor, 100 ) );
            
            environment.parse( reader, new SubProgressMonitor( monitor, 900 ) );
            
            if( !reportIncludesOnly ){
            	IDeclaration[] found = environment.getDeclarations();
            	if( found != null ){
            	    for( IDeclaration declaration : found ){
            	        callback.declarationFound( declaration );
            	    }
            	}
            }
        }
        finally{
            this.callback = null;
            this.scopes = null;
            monitor.done();
        }
    }

    private class SmallParser extends parser{
        private RawLexer lexer;
        private Environment environment;
        
        public SmallParser( Environment environment, RawLexer lexer ){
            super( lexer );
            this.lexer = lexer;
            scopes = new ScopeStack( environment );
            this.environment = environment;
        }

        public ASTNode parseAST() throws Exception{
            Symbol result;
            if( lexer.nextIsEOF() ){
                // an empty file, not much to look at...
                result = new Symbol( 0, new TranslationUnit() );
            }
            else{
                result = parse();
            }
            
            return AdvancedParser.parseAST( result, stack, remainingErrors() );
        }
        
        
        @Override
        public void includeInterface( Token inclusion ){
            callback.elementIncluded( inclusion.getText(), environment.requestMonitor(), Kind.INTERFACE );
        }
        
        @Override
        public void includeComponent( Token inclusion ){
            callback.elementIncluded( inclusion.getText(), environment.requestMonitor(), Kind.MODULE, Kind.BINARY_COMPONENT, Kind.CONFIGURATION );
        }
        
        @Override
        public void includeFile( Token inclusion ){
            String name = inclusion.getText();
            if( !name.endsWith( ".h" ))
                name += ".h";
            callback.fileIncluded( name, true, environment.requestMonitor() );
        }
    }
    
    private class Environment extends Parser{
        private IProgressMonitor monitor;
        private int remainingTicks;
        
        public Environment(){
            super( project.getProject() );
        }

        @Override
        protected RawLexer createLexer( Reader input ){
            return new CollectorLexer( input ){
                @Override
                public void setScopeStack( ScopeStack scopes ){
                    super.setScopeStack( scopes );
                    setScopes( scopes );
                }
            };
        }
        
        @Override
        public boolean parse( IMultiReader reader, IProgressMonitor monitor ) throws IOException{
            if( monitor == null )
                monitor = new NullProgressMonitor();
            
            monitor.beginTask( "Parsing", 1000 );
            remainingTicks = 250;
            
            this.monitor = monitor;
            boolean result = super.parse( reader, new SubProgressMonitor( monitor, 750 ) );
            this.monitor.done();
            this.monitor = null;
            return result;
        }
        
        public IProgressMonitor requestMonitor(){
            if( remainingTicks > 10 ){
                remainingTicks -= 10;
                return new SubProgressMonitor( monitor, 10 );
            }
            else{
                int ticks = remainingTicks;
                remainingTicks = 0;
                return new SubProgressMonitor( monitor, ticks );
            }
        }
        
        @Override
        protected IncludeProvider provider(NesC12IncludeProvider provider) {
        	provider.setCallback( callback );
        	return provider;
        }
        
        @Override
        protected RawParser createParser( RawLexer scanner ){
            return new SmallParser( this, scanner );
        }
        
        @Override
        protected void informResolve( AnalyzeStack stack ){
            setStack( stack );
            if( !reportIncludesOnly ){
                stack.setCreateCollectorDeclarations( true );
            }
        }
    }
}
