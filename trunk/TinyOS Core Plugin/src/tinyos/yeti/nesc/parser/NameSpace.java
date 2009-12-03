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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nesc.parser.language.elements.PreprocessorDefineConstant;
import tinyos.yeti.nesc.scanner.ITypeNames;
import tinyos.yeti.nesc.scanner.Token;


/**
 * TODO: change original impl to resizeable array
 * @author rschuler
 *
 */
public class NameSpace implements ITypeNames {

    private final int MAX_NUM_LEVELS = 1000;
    private final int MAX_LEVEL = MAX_NUM_LEVELS-1;

    File file = null;

    private IParseFile parseFile;

    /* Counts the number of scopes within the name-space */
    int scope_level = 0;
    /* For nested struct-declarations */
    int decl_level = 0; 

    /* If one, this name-space is not
     * applicable. Always return IDENTIFIER
     * in that case.
     */
    boolean idents_only[] = new boolean[MAX_NUM_LEVELS]; 

    /* Declarations in progress. */
    Declaration decls[] = new Declaration[MAX_NUM_LEVELS];

    /* Hash tables, one for each scope */
    Hashtable tables[] = new Hashtable[MAX_NUM_LEVELS];

    /* Hash table for define constants */
    Hashtable defineConstants = new Hashtable();

    public void putDefineConstant(Token token) {	
        PreprocessorDefineConstant pdc = new PreprocessorDefineConstant(token,file);
        defineConstants.put(pdc.text, pdc);	
    }
    public void putDefineConstant(Token token, String replacement) {
        PreprocessorDefineConstant pdc = new PreprocessorDefineConstant(token,file);
        pdc.setReplacement(replacement);
        defineConstants.put(pdc.text, pdc);
    }

    public Hashtable getDefineConstants() {
        return defineConstants;
    }

    private NameSpace() {

    }

    public NameSpace(File f) {
        this.file = f;
    }

    public NameSpace(Declaration[] d, File f) {
        this.file = f;
        tables[0] = new Hashtable();
        for (int i = 0; i < d.length; i++) {
            hash_put(tables[0],d[i]);
        }
    }

    public NameSpace(String types[], File f) {
        this.file = f;
        addTypesToGlobalScope(types);
    }

    public void addTypesToGlobalScope(String types[]) {
        if (types == null) {
            return;
        }
        if (tables[0] == null)   {
            tables[0] = new Hashtable();
        }
        for (int i = 0; i < types.length; i++) {
            Declaration d = new Declaration(file,null);
            d.decl_type = Declaration.DECLARATION_TYPE_NAME_SPACE_DECL;
            d.name = types[i];
            d.scope_level = 0;
            d.type = NesCparser.TYPEDEF_NAME;
            hash_put(tables[0],d);
        }

    }

    public void setParseFile( IParseFile parseFile ) {
        this.parseFile = parseFile;
    }

    public IParseFile getParseFile() {
        return parseFile;
    }

    public boolean get_idents_only() {
        return idents_only[decl_level]; 
    }

    /* Find an equivalent entry. If there is none, return NULL.
     * Do not change the table.
     */
    private Declaration hash_get(Hashtable h, String name) {

        if (h == null) {
            //System.out.println("hash_get("+h+","+name+") -> null");
            return null;
        }
        //System.out.println("hash_get("+h+","+name+") -> "+(Declaration)h.get(name));
        return (Declaration)h.get(name);
    }

    public int type_of_name(String name) {
        Declaration find = find_decl(name);

        if (typeNamesC.contains(name)) {
            return NesCparser.TYPEDEF_NAME;
        }
        if (find == null) {
            //System.out.println("type_of_name(String "+name+") -> Identifier" );
            return NesCparser.IDENTIFIER;
        }
        //System.out.println("type_of_name(String "+name+") -> "+find.type);
        return find.type;	
    }

    /* Put a new entry into the table. If there was previously an
     * equivalent entry in the table, return it.
     * If there was not, return NULL.
     */
    private IDeclaration hash_put(Hashtable t, IDeclaration d) {
        //System.out.println("hash_put(Hashtable t,"+d+")");
        Object temp = t.get(d.getName());

        if ( temp == null ) {		// no entry in hashtable
            t.put(d.getName(),d);
            return null;
        } else {					// entry
            return (IDeclaration) temp;
        }
    }

    /*
     * Create a new scope in the identifier/typedef/enum-const
     * name-space.
     */
    public void scope_push() {
        //System.out.println("scope_push() ->"+(scope_level+1));
        scope_level++;

        if(scope_level > MAX_LEVEL) {
            // should threw error
        }
        tables[scope_level] = new Hashtable();
    }

    /*
     * Turn it off.
     */
    public void ntd() {
        //System.out.println("ntd()");
        idents_only[decl_level] = true;
    }

    /*
     * Turn on typedef_name (and enum-constant) recognition)
     */
    public void td() {
        //System.out.println("td()");
        idents_only[decl_level] = false;
    }

    /*
     * Finish a structure declaration
     */
    public void struct_pop() {
        //System.out.println("struct_pop() -> decl_level "+(decl_level-1));
        decl_level--;
    }

    /*
     * Look for the name in the name-space, beginning with the outermost scope.
     */
    private Declaration find_decl(String name) {

        int look;

        for(look = scope_level; look >= 0; look--) {

            Declaration find = hash_get(tables[look], name);

            if (find != null) {
                //System.out.println("find_decl("+name+") -> found");
                return find;
            } 
        }
        //System.out.println("find_decl("+name+") -> not found");
        return null;
    }

    /*
     * Create a new name-space for a structure declaration.
     */
    public void struct_push() {
        //System.out.println("struct_push()");
        decl_level++;	
    }

    /*
     * Destroy an old scope in the identifier/typedef/enum-const
     * name-space.
     */
    public void scope_pop() {
        //System.out.println("scope_pop() ->"+(scope_level-1));
        tables[scope_level] = null;
        scope_level--;
    }

    /*
     * Remember that this declaration defines a typename,
     * not an identifier.
     */
    public void set_typedef() {
        //System.out.println("set_typedef() at level "+decl_level+" current declaration = "+decls[decl_level]);
        decls[decl_level].type = NesCparser.TYPEDEF_NAME;
    }

    public void set_token(Token t) {
        decls[decl_level].token = t;
    }

    /* finish the declarator */
    public void direct_declarator() {
        //System.out.println("direct_declarator()");

        put_name();
    }

    /* finish the declarator */
    public void pointer_declarator() {
        //System.out.println("pointer_declarator()");
        put_name();
    }

    /*
     * Begin a new declaration with default values.
     */
    public void new_declaration(int type) {
        //System.out.println("new_declaration("+type+") in level "+decl_level);
        decls[decl_level] = new Declaration(file,getParseFile()) ;
        decls[decl_level].decl_type = type;
        decls[decl_level].type = NesCparser.IDENTIFIER;
        decls[decl_level].scope_level = scope_level;
        decls[decl_level].name = "";
    }

    /*
     * Remember the name of the declarator being defined.
     */
    public void declarator_id(String name) {
        if (decls[decl_level] == null) {
            //System.out.println("declarator_id -> decls["+decl_level+"] -> null");
            return;
        }
        //System.out.println("declarator_id("+name+") -> "+"decls["+decl_level+"].name = "+name);
        decls[decl_level].name = name;
    }

    private void put_name() {
        //System.out.println("put_name()");
        Declaration decl = decls[decl_level];
        if (decl == null)return;
        if (decl.decl_type == NesCparser.STRUCT) {
            /* Should export the decl to the structure's name-space.
             * ... for now, just boot it.
             */
            return;
        }

        /*
         * If the declaration was already there, we should do
         * the right thing... for now, to heck with it.
         */
        if (tables[scope_level] == null) {
            tables[scope_level] = new Hashtable();
        }

        hash_put(tables[scope_level],decl);		
    }

    public void printTypes() {
//      if (tables[0] == null) return;
//      Enumeration e = tables[0].elements();
//      while(e.hasMoreElements()) {
//      Declaration d = (Declaration) e.nextElement();
//      System.out.println(d);
//      }
    }

    public void printTypes2() {
        if (tables[0] == null) return;
        Enumeration e = tables[0].elements();
        System.out.println("---TYPEDEFS------");
        while(e.hasMoreElements()) {
            Declaration d = (Declaration) e.nextElement();
            System.out.println("\""+d.name+"\",");
        }
        System.out.println("---END TYPEDEFS------");
    }

//  public Declaration[] getTypeDefs() {
//  if (tables[0] == null) return null;

//  ArrayList typedefs = new ArrayList();
//  Enumeration e = tables[0].elements();
//  while(e.hasMoreElements()) {
//  Declaration d = (Declaration) e.nextElement();
//  if (d.type == NesCparser.TYPEDEF_NAME){
//  typedefs.add(d);
//  }
//  }
//  return (Declaration[]) typedefs.toArray(new Declaration[typedefs.size()]);
//  }

    public Declaration[] getDeclarations() {
        if (tables[0] == null) return null;

        ArrayList<Declaration> declar = new ArrayList<Declaration>();

        for (int i = 0; i < tables.length; i++) {
            if (tables[i] == null) continue;
            Enumeration e = tables[i].elements();
            while(e.hasMoreElements()) {
                declar.add((Declaration)e.nextElement());
            }
        }

        return (Declaration[]) declar.toArray(new Declaration[declar.size()]);
    }

    public void setTypeDefs( Declaration[] d) {
        if (d == null) return;
        if (tables[0] == null)   {
            tables[0] = new Hashtable();
        }
        for (int i = 0; i < d.length; i++) {
            hash_put(tables[0],d[i]);
        }
    }

    public PreprocessorDefineConstant[] getEnumerationConstants() {
        ArrayList al = new ArrayList();
        Enumeration e = defineConstants.elements();
        while(e.hasMoreElements()) {
            al.add((PreprocessorDefineConstant)e.nextElement());
        }
        return (PreprocessorDefineConstant[]) al.toArray(new PreprocessorDefineConstant[al.size()]);
    }

    public void setEnumerationConstants(PreprocessorDefineConstant[] p) {
        for (int i = 0 ; i < p.length; i++) {
            defineConstants.put(p[i].text,p[i]);
        }
    }

    public void declarator_id(Token token) {
        if (decls[decl_level] == null) {
            return;
        }
        decls[decl_level].name = token.getText();
        decls[decl_level].ident = token;
    }

    public void printPreprocessorConstants() {
        Enumeration e = defineConstants.elements();
        while(e.hasMoreElements()) {
            PreprocessorDefineConstant d = (PreprocessorDefineConstant) e.nextElement();
            System.out.println(d);
        }
    }

    public String[] getTypeDefs() {
        ArrayList<String> l = new ArrayList<String>();
        Declaration d[] =getDeclarations();
        for (int i = 0; i < d.length; i++) {
            if (d[i].type == NesCparser.TYPEDEF_NAME) {
                l.add(d[i].name);
            }
        }
        return (String[]) l.toArray(new String[l.size()]);
    }


}
