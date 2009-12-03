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

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelPath;

/**
 * A very simple implementation of {@link IDeclaration}.
 * @author Benjamin Sigg
 */
public class SimpleDeclaration implements IDeclaration{
    private Kind kind;
    private String name;
    private IParseFile file;
    private TagSet tags;
    
    public SimpleDeclaration( String name, IParseFile file, Kind kind, TagSet tags ){
        this.name = name;
        this.file = file;
        this.kind = kind;
        this.tags = tags;
    }
    
    public Kind getKind() {
        return kind;
    }

    public String getLabel() {
        return name;
    }

    public String getName() {
        return name;
    }

    public IParseFile getParseFile() {
        return file;
    }

    public IASTModelPath getPath() {
        return new ASTModelPath( file, name );
    }

    public TagSet getTags() {
        return tags;
    }

}
