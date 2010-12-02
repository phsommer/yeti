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
package tinyos.yeti.nesc12.ep.declarations;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.ReferenceFactory;

/**
 * A declaration that just marks some element as present, but does not
 * specify any more information about that element. There should be no
 * error message emitted when an {@link IgnoreDeclaration} is found.
 * @author Benjamin Sigg
 */
public class IgnoreDeclaration extends BaseDeclaration{
    public static final IGenericFactory<IgnoreDeclaration> FACTORY = new ReferenceFactory<IgnoreDeclaration>( BaseDeclaration.FACTORY ){
        public IgnoreDeclaration create(){
            return new IgnoreDeclaration();
        }
    };
    
    protected IgnoreDeclaration(){
        // nothing
    }

    public IgnoreDeclaration( Kind kind, String name, String label, IParseFile file, ASTModelPath path, TagSet tags ){
        super( kind, name, label, file, path, tags );
    }
}
