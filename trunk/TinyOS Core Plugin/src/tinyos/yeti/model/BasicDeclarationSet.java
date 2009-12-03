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
package tinyos.yeti.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.macros.ConstantMacro;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.MakeTypedef;
import tinyos.yeti.model.ProjectModel.DeclarationFilter;
import tinyos.yeti.nesc.StringMultiReader;
import tinyos.yeti.widgets.helper.NoCancelMonitor;

/**
 * Manages the set of declarations which are present in all files.<br>
 * <ul>
 * 	<li>Basic element are elements that are always present in the current configuration</li>
 *  <li>Global elements are elements that are read from a set of files, the plugin 
 *  	assumes that those files are included in all other files</li>
 * </ul>
 * @author Benjamin Sigg
 */
public class BasicDeclarationSet{
	/** the model for which this set is used */
    private ProjectModel model;

    /**
     * Basic declarations that are present in any file, these include typedefs
     * like "uint16_t" and the typedefs declared in the current {@link MakeTarget},
     * available from {@link MakeTarget#getTypedefs()}.
     */
    private IDeclaration[] basicTypes;
    
    /**
     * Basic declarations present in any file, these declarations include
     * fields like "uniqueN" and annotations like "@integer".
     */
    private IDeclaration[] basicDeclarations;

    /**
     * Files which are included into any other file automatically. These files
     * are determined by the current {@link IEnvironment} and its method
     * {@link IEnvironment#getStandardInclusionFiles()}. Also the current
     * {@link MakeTarget} allows to define additional global inclusion files.
     */
    private File[] globalInclusionFiles;
    
    /**
     * All public visible declarations that are found in the
     * {@link #globalInclusionFiles}.
     */
    private IDeclaration[] globalDeclarations;
    
    /**
     * All macros that are defined in the {@link #globalInclusionFiles}.
     */
    private IMacro[] globalMacros;
    
    /**
     * The combination of {@link #basicTypes}, {@link #basicDeclarations} and
     * {@link #globalDeclarations}, where the order of the appearance of items
     * matters, only the first appearance is listed in this array.
     */
    private IDeclaration[] orderedBasicDeclarations;

    private IDeclarationCollection collection;

    private boolean onStartup = true;
    private boolean primaryStartup = true;
    private int startupRecursion = 0;

    public BasicDeclarationSet( ProjectModel model ){
        this.model = model;
    }

    public void fillDeclarations( Collection<? super IDeclaration> declarations, DeclarationFilter filter ){
        ensureCollection();
        collection.fillDeclarations( declarations, filter );
    }

    public void fillDeclarations( Collection<? super IDeclaration> declarations, String name, Kind... kinds ){
        ensureCollection();
        collection.fillDeclarations( declarations, name, kinds );
    }

    public void fillDeclarations( Collection<? super IDeclaration> declarations, Kind... kinds ){
        ensureCollection();
        collection.fillDeclarations( declarations, kinds );
    }

    private void ensureCollection(){
        if( collection == null )
            collection = ProjectModel.toCollection( orderedBasicDeclarations );
    }

    /**
     * Adds the basic declarations which are always present in this model
     * to <code>parser</code>.
     * @param monitor to report progress
     * @param parser the parser to update
     */
    private void addGlobalDeclarations( INesCParser parser, IProgressMonitor monitor ){
        ensureGlobalDeclarations( monitor );
        if( globalDeclarations != null ){
            parser.addDeclarations( globalDeclarations );
        }
    }

    private void addGlobalMacros( INesCParser parser, IParseFile file ){
    	if( globalMacros != null ){
    		for( IMacro macro : globalMacros ){
    			parser.addMacro( macro );
    		}
    	}
    }

    private void ensureGlobalDeclarations( IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Global Declarations", 2 );
        
        globalDeclarations = model.getDefinitionCollection().getBasicDeclarations( new SubProgressMonitor( monitor, 1 ) );
        globalMacros = model.getDefinitionCollection().getBasicMacros( new SubProgressMonitor( monitor, 1 ));
        
        monitor.done();
    }
    
    /**
     * Gets a list of all files that are to be included in any parsed file.
     * @return the list of global files
     */
    public File[] listGlobalInclusionFiles(){
        if( globalInclusionFiles != null )
            return globalInclusionFiles;
        
        LinkedHashSet<File> files = new LinkedHashSet<File>();

        ProjectTOS tos = model.getProject();
        IEnvironment environment = TinyOSPlugin.getDefault().getEnvironments().getEnvironment( tos.getProject() );
        if( environment != null ){
            File[] includes = environment.getStandardInclusionFiles();
            if( includes != null ){
                for( File file : includes ){
                    files.add( file );
                }
            }
        }

        MakeTarget target = tos.getMakeTarget();
        if( target != null ){
            MakeInclude[] includes = target.getIncludes();
            if( includes != null ){
                for( MakeInclude include : includes ){
                    if( include.isGlobal() ){
                        File file = new File( include.getPath() );
                        addAllFiles( file, files, include.isRecursive() );
                    }
                }
            }
            
            IPlatform platform = target.getPlatform();
            if( platform != null ){
                File[] platformIncludes = platform.getGlobalIncludes();
                if( platformIncludes != null ){
                    for( File file : platformIncludes ){
                        addAllFiles( file, files, false );
                    }
                }
            }
        }

        globalInclusionFiles = files.toArray( new File[ files.size() ] );
        return globalInclusionFiles;
    }
    
    /**
     * Lists all the files that are directly or indirectly included into all
     * the other files.
     * @param monitor to report progress
     * @return the files
     */
    public IParseFile[] listAllGlobalInclusionFiles( IProgressMonitor monitor ){
    	return model.getDefinitionCollection().getBasicFiles( monitor );
    }

    private void addAllFiles( File file, Collection<File> files, boolean recursive ){
        if( file.exists() ){
            if( file.isFile() ){
                files.add( file );
            }
            if( file.isDirectory() ){
                File[] children = file.listFiles();
                if( children != null ){
                    if( recursive ){
                        for( File child : children ){
                            addAllFiles( child, files, true );
                        }
                    }
                    else{
                        for( File child : children ){
                            if( child.exists() && child.isFile() ){
                                files.add( child );
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds the basic typedefs, fields and macros to <code>parser</code>. The
     * elements added by this method are normally generated by this model and
     * not read from any files. However some can be overridden if a file is
     * found that contains the definitions as well.
     * @param parser some parser
     * @param file the file for which the parser will be used
     * @param monitor to report progress
     * @see INesCParser#addDeclarations(IDeclaration[])
     */
    public void addBasics( INesCParser parser, IParseFile file, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor = new NoCancelMonitor( monitor );
        monitor.beginTask( "List Basic Declarations", onStartup ? (orderedBasicDeclarations == null ? 3 : 2 ) : 2 );
        
        if( onStartup ){
            boolean primaryStartup = this.primaryStartup;
            this.primaryStartup = false;
            try{
                ensureBasics( new SubProgressMonitor( monitor, 1 ) );
                addBasicMacros( parser );
                
                if( orderedBasicDeclarations == null ){
                    addBasicTypes( parser );
                    addBasicDeclarations( parser );
                    if( !isGlobalInclusionFile( file, new SubProgressMonitor( monitor, 1 ) )){
	                    addGlobalDeclarations( parser, new SubProgressMonitor( monitor, 1 ) );
	                    addGlobalMacros( parser, file );
                    }
                }
                else{
                	if( isGlobalInclusionFile( file, new SubProgressMonitor( monitor, 1 ) )){
                		addBasicTypes( parser );
                    	addBasicDeclarations( parser );
                	}
                	else{
                		parser.addDeclarations( orderedBasicDeclarations );
                		addGlobalMacros( parser, file );
                	}
                }
                rebuildOrderedBasicDeclarations();

            }
            finally{
                if( primaryStartup ){
                    onStartup = false;
                }
                this.primaryStartup = primaryStartup;
            }
        }
        else{
            ensureBasics( new SubProgressMonitor( monitor, 1 ));
            addBasicMacros( parser );
            if( isGlobalInclusionFile( file, new SubProgressMonitor( monitor, 1 ) )){
            	addBasicTypes( parser );
            	addBasicDeclarations( parser );
            }
            else{
            	parser.addDeclarations( orderedBasicDeclarations );
            	addGlobalMacros( parser, file );
            }
        }
    }
    
    /**
     * Tells whether <code>file</code> is one of the files that is automatically
     * included in all the other files.
     * @param file the file to check
     * @param monitor to report progress
     * @return <code>true</code> if <code>file</code> is automatically included
     */
    public boolean isGlobalInclusionFile( IParseFile file, IProgressMonitor monitor ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	monitor.beginTask( "is global inclusion file", 10 );
    	
        if( file == null ){
        	monitor.done();
            return false;
        }
        
        IParseFile[] check = listAllGlobalInclusionFiles( new SubProgressMonitor( monitor, 9 ) );
        if( monitor.isCanceled() ){
        	monitor.done();
        	return false;
        }
        
        for( IParseFile checkFile : check ){
            if( file.equals( checkFile )){
            	monitor.done();
                return true;
            }
        }
        
        monitor.done();
        return false;
    }

    private void ensureBasics( IProgressMonitor monitor ){
        if( onStartup ){
            startupRecursion++;
            try{
                ensureBasicTypes();
                ensureBasicDeclarations();
                ensureGlobalDeclarations( monitor );

                rebuildOrderedBasicDeclarations();
            }
            finally{
                startupRecursion--;
            }
        }
        else{
            monitor.beginTask( "Null", 1 );
            monitor.done();
        }
    }

    private void rebuildOrderedBasicDeclarations(){
        collection = null;

        Map<String, IDeclaration> map = new HashMap<String, IDeclaration>();
        IDeclaration[][] checks = new IDeclaration[][]{
                basicTypes,
                basicDeclarations,
                globalDeclarations
        };

        for( IDeclaration[] check : checks ){
            if( check != null ){
                for( IDeclaration declaration : check ){
                    String name = declaration.getName();
                    IDeclaration.Kind kind = declaration.getKind();

                    String key = kind + name;
                    map.put( key, declaration );
                }
            }
        }

        orderedBasicDeclarations = map.values().toArray( new IDeclaration[ map.size() ] );
    }
    
    public IDeclaration[] listAllDeclarations(){
    	if( orderedBasicDeclarations == null )
    		return new IDeclaration[]{};
    	return orderedBasicDeclarations;
    }

    /**
     * Adds the basic types to <code>parser</code>.
     * @param parser the parser where to add the basic types
     * @see INesCParser#addDeclarations(IDeclaration[])
     */
    private void addBasicTypes( INesCParser parser ){
        ensureBasicTypes();
        parser.addDeclarations( basicTypes );
    }
    
    public IDeclaration[] listBasicTypes(){
    	ensureBasicTypes();
    	return basicTypes;
    }

    private void ensureBasicTypes(){
        if( basicTypes == null ){
            synchronized( this ){
                if( basicTypes == null ){
                    String[] definitions = {
                            "signed char",              "int8_t",
                            "int8_t",                   "nx_int8_t",
                            "int8_t",                   "nxle_int8_t",
                            "signed int",               "int16_t",
                            "int16_t",                  "nx_int16_t",
                            "int16_t",                  "nxle_int16_t",
                            "signed long int",          "int32_t",
                            "int32_t",                  "nx_int32_t",
                            "int32_t",                  "nxle_int32_t",
                            "signed long long int",     "int64_t",
                            "int64_t",                  "nx_int64_t",
                            "int64_t",                  "nxle_int64_t",
                            "unsigned char",            "uint8_t",
                            "uint8_t",                  "nx_uint8_t",
                            "uint8_t",                  "nxle_uint8_t",
                            "unsigned int",             "uint16_t",
                            "uint16_t",                 "nx_uint16_t",
                            "uint16_t",                 "nxle_uint16_t",
                            "unsigned long int",        "uint32_t",
                            "uint32_t",                 "nx_uint32_t",
                            "uint32_t",                 "nxle_uint32_t",
                            "unsigned long long int",   "uint64_t",
                            "uint64_t",                 "nx_uint64_t",
                            "uint64_t",                 "nxle_uint64_t",
                            "uint8_t",                  "bool",
                            "bool",						"nx_bool",
                            "int",                      "result_t",
                            "int",                      "link_t",
                            "int",                      "event_t",
                            "int",                      "TOS_Msg",
                            "unsigned short int",       "error_t",

                            // further elements of gnu gcc will be added by the parser-plugin
                    };

                    INesCParserFactory factory = TinyOSPlugin.getDefault().getParserFactory();
                    List<IDeclaration> declarations = new ArrayList<IDeclaration>();

                    for( int i = 0, n = definitions.length; i<n; i += 2 ){
                        declarations.add( 
                                factory.toBasicType( 
                                        definitions[i], 
                                        definitions[i+1],
                                        declarations.toArray( new IDeclaration[ declarations.size() ] ) ) );
                    }

                    MakeTarget target = model.getProject().getMakeTarget();
                    if( target != null ){
                        MakeTypedef[] typedefs = target.getTypedefs();
                        if( typedefs != null ){
                            for( MakeTypedef typedef : typedefs ){
                                declarations.add( 
                                        factory.toBasicType( 
                                                typedef.getType(), 
                                                typedef.getName(),
                                                declarations.toArray( new IDeclaration[ declarations.size() ] ) ) );
                            }
                        }
                    }

                    basicTypes = declarations.toArray( new IDeclaration[ declarations.size() ] );
                }
            }
        }
    }

    /**
     * Adds all the basic macros to <code>parser</code>.
     * @param parser the parser to which macros will be added
     * @see INesCParser#addMacro(IMacro)
     */
    private void addBasicMacros( INesCParser parser ){
        for( IMacro macro : listBasicMacros() ){
            parser.addMacro( macro );
        }
    }

    /**
     * Gets a list of all the basic macros that a preprocessor should handle.
     * @return the list of macros
     */
    public IMacro[] listBasicMacros(){
    	List<IMacro> macros = new ArrayList<IMacro>();
    	macros.add( new ConstantMacro( "NULL", "(void*)0" ) );
    	
    	MakeTarget target = model.getProject().getMakeTarget();
    	if( target != null ){
    		IMacro[] targetMacros = target.getMacros();
    		if( targetMacros != null ){
    			for( IMacro macro : targetMacros ){
    				macros.add( macro );
    			}
    		}
    	}
    	
    	return macros.toArray( new IMacro[ macros.size() ] );
    }
    
    /**
     * Gets the list of global macros, these macros are defined in the files
     * that are included globally.
     * @return the global macros
     */
    public IMacro[] listGlobalMacros(){
        if( globalMacros == null )
            return new IMacro[]{};
        return globalMacros;
    }

    public IDeclaration[] listGlobalDeclarations(){
    	if( globalDeclarations == null )
    		return new IDeclaration[]{};
    	return globalDeclarations;
    }
    
    /**
     * Adds the declarations of basic declarations to <code>parser</code>.
     * @param parser the parser where to add basic declarations
     * @see INesCParser#addDeclarations(IDeclaration[])
     */
    private void addBasicDeclarations( INesCParser parser ){
        ensureBasicDeclarations();
        parser.addDeclarations( basicDeclarations );
    }
    
    public IDeclaration[] listBasicDeclarations(){
    	ensureBasicDeclarations();
    	return basicDeclarations;
    }

    private void ensureBasicDeclarations(){
        if( basicDeclarations == null ){
            synchronized( this ){
                if( basicDeclarations == null ){
                    String[] definitions = new String[]{
                            "unsigned int unique( char *identifier );",
                            "unsigned int uniqueN( char *identifier, int n );",
                            "unsigned int uniqueCount( char *identifier );",
                            "enum{ TRUE = 1, FALSE = 0 };",
                            "struct @spontaneous{};",
                            "struct @combine{ char* name; };",
                            "struct @C{};",
                            "struct @number{};",
                            "struct @integer{};",
                            "struct @hwevent{};",
                            "struct @atomic_hwevent{};"
                    };

                    List<IDeclaration> resolutions = new ArrayList<IDeclaration>();

                    for( String definition : definitions ){
                        try{
                            INesCParserFactory factory = TinyOSPlugin.getDefault().getParserFactory();
                            INesCParser parser = factory.createParser( null );
                            parser.setCreateDeclarations( true );
                            parser.parse( new StringMultiReader( definition ), null );
                            IDeclaration[] newDeclarations = parser.getDeclarations();
                            if( newDeclarations != null ){
                                for( IDeclaration newDeclaration : newDeclarations ){
                                    resolutions.add( newDeclaration );
                                }
                            }
                        }
                        catch ( IOException e ){
                            e.printStackTrace();
                        }
                    }

                    basicDeclarations = resolutions.toArray( new IDeclaration[ resolutions.size() ] );
                }
            }
        }
    }
}
