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
package tinyos.yeti.nesc;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.nesc.parser.language.elements.DeclarationElement;

public class FunctionASTModelNode extends ASTModelNode {
    private int parameterCount;
    private String skeleton;
    
    public FunctionASTModelNode( IASTModelNode parent, String identifier,
            String name, String label, IParseFile file, IFileRegion[] origin, DeclarationElement declaration, Tag... tags ) {
        super( parent, identifier, null, label, file, origin, tags );
        read( declaration );
    }

    public FunctionASTModelNode( IASTModelNode parent, String identifier,
            String label, IParseFile file, IFileRegion[] origin, DeclarationElement declaration, TagSet tags ) {
        super( parent, identifier, null, label, file, origin, tags );
        read( declaration );
    }
    
    public void read( DeclarationElement declaration ){
        parameterCount = declaration.getParameterCount();
        setNodeName( declaration.getFunctionName() );
        skeleton = declaration.getSkeleton( "#noname#" );
    }
    
    public int getParameterCount() {
        return parameterCount;
    }
    
    public String getSkeleton( String renamedInterfaceName ){
        if( renamedInterfaceName == null )
            return skeleton.replace( "#noname#.", "" );
        else
            return skeleton.replace( "#noname#", renamedInterfaceName );
    }
}
