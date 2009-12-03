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

import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.ep.parser.standard.ASTModelNodeConnection;

public class RenamedASTModelNodeConnection extends ASTModelNodeConnection{
    private String rename;
    
    public RenamedASTModelNodeConnection( ASTModelNode parent,
            boolean reference, String identifier, String rename, String label,
            IFileRegion[] regions, TagSet tags ){
        super( parent, reference, identifier, label, regions, tags );
        
        if( rename == null )
            this.rename = identifier;
        else
            this.rename = rename;
    }
    
    public String getRename(){
        return rename;
    }
}
