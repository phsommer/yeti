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
package tinyos.yeti.nesc12.ep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclarationFactory;
import tinyos.yeti.ep.storage.IStorage;

public class NesC12DeclarationFactory implements IDeclarationFactory{
    public int getVersion(){
        return 1;
    }

    public void write( IDeclaration declaration, DataOutputStream out, ProjectTOS project ) throws IOException{
        IStorage storage = new NesC12GenericStorage( project, out, null );
        storage.write( declaration );
    }
    

    public IDeclaration read( int version, DataInputStream in, ProjectTOS project ) throws IOException{
        IStorage storage = new NesC12GenericStorage( project, in, null );
        return storage.read();
    }
}
