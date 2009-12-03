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

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;

/**
 * Used to resolve declarations and to find the ast-node that declared the
 * declaration.
 * @author Benjamin Sigg
 */
public interface DeclarationResolver{
    public IDeclaration resolve( String name, IProgressMonitor monitor, Kind... kind );
    public IDeclaration[] resolveAll( IProgressMonitor monitor, Kind... kind );
    
    public ModelNode resolve( ModelConnection connection, IProgressMonitor monitor );
    public ModelNode resolve( IDeclaration declaration, IProgressMonitor monitor );
    public ModelNode resolve( IASTModelPath path, IProgressMonitor monitor );
    
    public IASTModelPath resolvePath( ModelConnection connection, IProgressMonitor monitor );
    
    public void parsingFinished();
}
