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

import java.util.Map;

import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;

/**
 * A binding resolver is used to create a momentary snapshot of an {@link IASTModel}.
 * The snapshot will contain the same information as the model itself,
 * but in a much more compact and typesafe way.
 * @author Benjamin Sigg
 */
public interface BindingResolver{
    public Binding getBinding( IASTModelPath path, String identifier );
    public Binding getBinding( IASTModelPath path, String identifier, Map<GenericType, Type> generics );
    
    public void putBinding( IASTModelPath path, String identifier, Binding binding );
    public void putBinding( IASTModelPath path, String identifier, Map<GenericType, Type> generics, Binding binding );
    
    /**
     * Gets the monitor used to cancel the operation, must not return <code>null</code>.
     * @return the monitor, not <code>null</code>
     */
    public ICancellationMonitor getCancellationMonitor();
}
