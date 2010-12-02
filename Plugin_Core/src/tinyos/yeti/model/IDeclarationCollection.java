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
package tinyos.yeti.model;

import java.util.Collection;

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.ProjectModel.DeclarationFilter;

/**
 * A collection containing some {@link IDeclaration}s. It is up to the collection
 * how the declarations are stored. These collections are unmodifiable.
 * @author Benjamin Sigg
 */
public interface IDeclarationCollection{
    /**
     * Gets all declarations which are stored in this collection.<br>
     * Note: this method will be called often.
     * @return an array containing all declarations
     */
    public IDeclaration[] toArray();
    
    /**
     * Searches all declarations which have one of the given <code>kind</code>
     * and the name <code>name</code> and fills them into <code>declarations</code>.
     * @param declarations some collection to fill with declarations
     * @param name the name of the declarations to search
     * @param kind the kind of declarations that are searched
     */
    public void fillDeclarations( Collection<? super IDeclaration> declarations, String name, Kind... kind );
    
    /**
     * Searches all declarations which have one of the given <code>kind</code> and
     * fills them into <code>declarations</code>.
     * @param declarations some collection to fill with declarations
     * @param kind the kind of declaration to search
     */
    public void fillDeclarations( Collection<? super IDeclaration> declarations, Kind... kind );
    
    /**
     * Fills all declarations which pass through <code>filter</code> into
     * <code>declarations</code>.
     * @param declarations some collection to fill with declarations
     * @param filter the filter which must be passed
     */
    public void fillDeclarations( Collection<? super IDeclaration> declarations, DeclarationFilter filter );
}
