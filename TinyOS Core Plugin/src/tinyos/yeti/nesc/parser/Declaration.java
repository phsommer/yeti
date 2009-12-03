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

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc.scanner.Token;



public class Declaration implements IDeclaration{

    // possible types
    // NesCParser2.TYPEDEF_NAME
    // NesCParser2.IDENTIFIER

    // declaration types
    final static int DECLARATION_TYPE_NAME_SPACE_DECL = 4342342;
    // NesCParser2.STRUCT


    public String name = null;

    public int type = -1;
    public int decl_type = -1;
    public int scope_level = -1;

    public Token ident = null;

    public File file = null;
    public Token token = null;

    private IParseFile parseFile;

    public Declaration(File file, IParseFile parseFile) {
        this.file = file;
        this.parseFile = parseFile;
    }

    public boolean isTypeDef() {
        return (type == NesCparser.TYPEDEF_NAME);
    }

    public String getName() {
        return name;
    }

    public Kind getKind() {
        if( type == NesCparser.TYPEDEF_NAME )
            return Kind.TYPEDEF;

        return Kind.FIELD;
    }

    public IParseFile getParseFile() {
        return parseFile;
    }

    public String getLabel() {
        return getName();
    }

    public IASTModelPath getPath() {
        return new ASTModelPath( getParseFile(), getName() );
    }

    public TagSet getTags() {
        return null;
    }

    @Override
    public String toString() {
        String t = "";
        switch (type) {
            case NesCparser.TYPEDEF_NAME : t = "TYPEDEF_NAME"; break;
            case NesCparser.IDENTIFIER   : t = "IDENTIFIER"; break;
        }
        String d = "";
        switch (decl_type) {
            case NesCparser.STRUCT : d = "STRUCT_DECLARATION"; break;
            case DECLARATION_TYPE_NAME_SPACE_DECL : d = "NAME_SPACE_DECL"; break; 
        }

        if (ident != null) {
            return "(Name: "+name+" | type: "+t +" | decl_type: "+d+" | level: "+scope_level+") " +
            "\nToken offset="+ident.offset+" length="+ident.length()+" line="+ident.line;
        } else {
            return "(Name: "+name+" | type: "+t +" | decl_type: "+d+" | level: "+scope_level+")";
        }
    }

}
