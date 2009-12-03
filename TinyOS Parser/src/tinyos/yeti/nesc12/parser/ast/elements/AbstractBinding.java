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
package tinyos.yeti.nesc12.parser.ast.elements;


/**
 * A binding that returns <code>null</code> for {@link #getBindingValue()}
 * and a constant value for {@link #getBindingType()}.
 * @author Benjamin Sigg
 */
public abstract class AbstractBinding implements Binding{
    private String type;
    
    public AbstractBinding( String type ){
        this.type = type;
    }
    
    public String getBindingType() {
        return type;
    }
    
    public String getBindingValue() {
        return null;
    }
}
