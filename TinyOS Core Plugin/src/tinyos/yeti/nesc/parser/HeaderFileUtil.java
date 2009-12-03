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
package tinyos.yeti.nesc.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.parser.language.elements.Element;
import tinyos.yeti.nesc.parser.language.elements.IncludesElement;
import tinyos.yeti.nesc.scanner.HeaderScanner;

public class HeaderFileUtil {

	public static Element resolve(IResource r, Element e, Declaration d[], LinkedList fileHistory) {
		//System.out.println("locate and parse component: "+e.getName());
		//System.out.println("  dependecy : "+r.getName()+"-> "+e.getName());
		
		File f;
		if (e instanceof IncludesElement) {
			f = TinyOSPlugin.getDefault().locate(r ,e.getName()+".h", false, null );
		} else {
			f = TinyOSPlugin.getDefault().locate(r ,e.getName()+".nc", false, null );
		}
		 
		// No file found, perhaps the include paths where incorrectly set or
		// the specified default target does not inlcude the file requestet
		if (f==null) {
			String timestamp = new Timestamp(System.currentTimeMillis()).toString();
			if (e instanceof IncludesElement) {
				writeToConsole(timestamp+": Parser: IncludesElement -> file "+e.getName()+".h not found...");
			} else {
				writeToConsole(timestamp+": Parser: "+e.getName()+".nc not found...");
			}	
		} else {
			try{
			    ProjectTOS project = TinyOSPlugin.getDefault().getProjectTOS( r.getProject() );
			    
				if (e instanceof IncludesElement) {
					// First check if declarations of header are in cache..
				    IDeclaration[] declarations = project.getModel().getFileDeclarations( f );
				    
					// not in cache, parse
					if (declarations == null){
					    HeaderFileParser hfp = new HeaderFileParser(r,r.getLocation().toFile(),fileHistory);
						
						hfp.getNameSpace().setTypeDefs(d);
						
						declarations = resolveCTypes(e.getName()+".h",hfp,false,fileHistory);	
						
						// set cache..
						// TinyOSPlugin.getDefault().getProjectTOS(
						//		r.getProject()).setDeclarations(
						//		f,d_header);
					}
					
					List<Declaration> decls = new ArrayList<Declaration>();
					for( IDeclaration decl : declarations ){
					    if( decl instanceof Declaration )
					        decls.add( (Declaration)decl );
					}
					
					e.setDeclarations( decls.toArray( new Declaration[decls.size()] ));
				} else {
	//				System.out.println("ComponentElement/SpecificationElement -> file "+f.getAbsolutePath());
				    IDeclaration[] declarations = project.getModel().getFileDeclarations( f );
				    if( declarations == null ){
				        try{
				            project.getModel().update( f, null );
				            declarations = project.getModel().getFileDeclarations( f );
				        }
				        catch( IOException ex ){
				            ex.printStackTrace();
				        }
				    }
				    
				    if( declarations != null ){
				        List<Declaration> decls = new ArrayList<Declaration>();
		                for( IDeclaration decl : declarations ){
		                    if( decl instanceof Declaration )
		                        decls.add( (Declaration)decl );
		                }
		                
		                e.setDeclarations( decls.toArray( new Declaration[decls.size()] ));
				    }
				}
			}
			catch( MissingNatureException ex ){
				// silent
			}
		}
		return e;
	}




	public static Declaration[] resolveCTypes(File f, HeaderFileParser old, LinkedList fileHistory) {
		//printTypes(old.getNameSpace().getDeclarations());
//		System.out.println("-- Resolve : "+f.getAbsolutePath());
		
		FileReader is;
		HeaderFileParser hp = null;
		try {
			is = new FileReader(f);
//			StringReader sr = new StringReader(IOConversion.getStringFromStream(is));
//			is.close();
			
			HeaderScanner scanner = new HeaderScanner(is);
			hp = new HeaderFileParser(f, fileHistory);
		//	types to parser
        if (old != null) {
        	hp.getNameSpace().setTypeDefs(old.getNameSpace().getDeclarations());
        	hp.setOrigin(old.getOrigin());
        	// 	add types to parser
        	hp.getNameSpace().setTypeDefs(old.getNameSpace().getDeclarations());
        }
		// .......
  	   	scanner.setCallback(hp);
  	   	
  	   	//jay.yydebug.yyDebug debug = new jay.yydebug.yyDebugAdapter();
  	   	//hp.yyparse(scanner, debug);
  	   	
  	   	hp.yyparse(scanner);
		is.close();
		// return declarations..
		return hp.getNameSpace().getDeclarations();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			writeToConsole("Parser: Header Include "+f.getAbsolutePath()+" not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (yyException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("yyException in file: "+f.getAbsolutePath()+" "+e.getMessage());
			return hp.getNameSpace().getDeclarations();	
		} 

		return null;
		
	}
	private static void printTypes(Declaration[] typeDefs) {
		System.out.println(typeDefs.length+" Types");
		System.out.println("-----TYPES-------");
		for (int i = 0; i < typeDefs.length; i++) {
			//if (typeDefs[i].decl_type == NesCparser.TYPEDEF_NAME) {
				System.out.print("\""+typeDefs[i].name+"\",");
				System.out.println(" " +typeDefs[i].type);
			//}
		}
		System.out.println("------------------------");
	}

	/**
	 * @param filename 
	 * @param parser
	 * @param fileHistory 
	 * @param b		if true search local (equals include "name")
	 */
	public static Declaration[] resolveCTypes(String filename, HeaderFileParser parser, boolean local, LinkedList fileHistory) {
		if (local) {
			File current = parser.file;
			File newFile = new File(current.getParent(), filename);
			if (!newFile.exists()) {
				newFile = TinyOSPlugin.getDefault().locate(parser.getOrigin(),filename,false,null);	
				if ((newFile!=null)&&(!newFile.exists())) {	
					TinyOSPlugin.getDefault().wirteToConsole("File "+filename+" (0) not found..");
				}
			} else {
				return resolveCTypes(newFile, parser, fileHistory);
			}
		} else {
			IResource root = parser.getOrigin();
			File newFile = TinyOSPlugin.getDefault().locate(root,filename,false,null);
			if ((newFile==null)||(!newFile.exists())) {
				TinyOSPlugin.getDefault().wirteToConsole("File "+filename+" (1) not found..");
			} else {
				return resolveCTypes(newFile, parser, fileHistory);
			}
		}
		return null;
	}

	/**
	 * @param dir
	 * @param filename
	 * @param parser
	 * @param b		if true search local (equals include "dir/name")
	 */
	public static Declaration[] resolveCTypes(String dir, String filename, HeaderFileParser parser, boolean local, LinkedList fileHistory) {
		//System.out.println("resolveCTypes("+dir+","+filename+","+parser+","+ local);
		
		if (local) {
			File current = parser.file;
			File newFile = new File(current.getParent()+dir, filename);
			if (!newFile.exists()) {
				System.out.println("File "+filename+" (2) not found..");
				//System.exit(85);
			} else {
				return resolveCTypes(newFile, parser, fileHistory);
			}
		} else {
			IResource root = parser.getOrigin();
			// not sure if works
			File newFile = TinyOSPlugin.getDefault().locate(root,dir+filename,false,null);		
			if ((newFile==null)||(!newFile.exists())) {
				TinyOSPlugin.getDefault().log("File "+dir+filename+" (3) not found..");
				//System.exit(85);
			} else {
				//System.out.println("File "+filename+" in Dir "+dir+" -> "+newFile.getAbsolutePath());
				return resolveCTypes(newFile, parser, fileHistory);
			}
		}
		return null;
	}

	private static void writeToConsole(String s) {
		TinyOSPlugin.getDefault().wirteToConsole(s);
	}
}